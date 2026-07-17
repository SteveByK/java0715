package com.stevebyk.java0715.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stevebyk.java0715.common.BusinessException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/**
 * Minimal JWT and refresh-token cryptography service.
 *
 * <p>The access token is a signed HMAC-SHA256 JWT. Refresh tokens are opaque,
 * random values and are persisted only as SHA-256 hashes.</p>
 */
@Service
public class JwtTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private final ObjectMapper objectMapper;
    private final AuthProperties authProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public JwtTokenService(ObjectMapper objectMapper, AuthProperties authProperties) {
        this.objectMapper = objectMapper;
        this.authProperties = authProperties;
    }

    /**
     * Signs one access JWT containing identity, role and permission claims.
     */
    public String issueAccessToken(AuthUserEntity user, List<String> roles, List<String> permissions) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(authProperties.accessTokenTtl());
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getUserId());
        payload.put("username", user.getUsername());
        payload.put("displayName", user.getDisplayName());
        payload.put("roles", roles);
        payload.put("permissions", permissions);
        payload.put("typ", "access");
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());
        return sign(header, payload);
    }

    /**
     * Validates a JWT and converts its claims into a Spring Security principal.
     */
    public AuthenticatedUser parseAccessToken(String token) {
        Map<String, Object> claims = parseAndValidate(token);
        if (!"access".equals(claims.get("typ"))) {
            throw new BusinessException("INVALID_TOKEN", "token type is not access");
        }
        long expiresAt = ((Number) claims.get("exp")).longValue();
        if (Instant.now().getEpochSecond() >= expiresAt) {
            throw new BusinessException("TOKEN_EXPIRED", "access token expired");
        }
        return new AuthenticatedUser(
                String.valueOf(claims.get("sub")),
                String.valueOf(claims.get("username")),
                String.valueOf(claims.get("displayName")),
                stringList(claims.get("roles")),
                stringList(claims.get("permissions")));
    }

    /**
     * Creates an opaque refresh token and its durable hash.
     */
    public IssuedRefreshToken issueRefreshToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String tokenId = UUID.randomUUID().toString();
        String secret = base64Url(randomBytes);
        String token = "RT." + tokenId + "." + secret;
        return new IssuedRefreshToken(token, tokenId, sha256(token));
    }

    /**
     * Hashes a submitted refresh token for database lookup.
     */
    public String hashRefreshToken(String refreshToken) {
        return sha256(refreshToken);
    }

    private String sign(Map<String, Object> header, Map<String, Object> payload) {
        try {
            String headerPart = base64Url(objectMapper.writeValueAsBytes(header));
            String payloadPart = base64Url(objectMapper.writeValueAsBytes(payload));
            String signingInput = headerPart + "." + payloadPart;
            String signature = base64Url(hmac(signingInput));
            return signingInput + "." + signature;
        } catch (Exception exception) {
            throw new BusinessException("TOKEN_ISSUE_FAILED", exception.getMessage());
        }
    }

    private Map<String, Object> parseAndValidate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BusinessException("INVALID_TOKEN", "malformed access token");
            }
            String signingInput = parts[0] + "." + parts[1];
            String expectedSignature = base64Url(hmac(signingInput));
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new BusinessException("INVALID_TOKEN", "invalid token signature");
            }
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(payload, MAP_TYPE);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("INVALID_TOKEN", "invalid access token");
        }
    }

    private byte[] hmac(String value) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new BusinessException("TOKEN_HASH_FAILED", exception.getMessage());
        }
    }

    private static String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    @SuppressWarnings("unchecked")
    private static List<String> stringList(Object value) {
        if (value instanceof List<?> values) {
            return values.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
