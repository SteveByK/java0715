package com.stevebyk.java0715.auth;

import com.stevebyk.java0715.common.BusinessException;
import com.stevebyk.java0715.common.ddd.ApplicationServiceRole;
import java.time.Instant;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication application service.
 *
 * <p>Coordinates password verification, login failure lockout, permission
 * resolution, refresh-token rotation and append-only security auditing.</p>
 */
@Service
@ApplicationServiceRole
public class AuthService {

    private final AuthUserRepository userRepository;
    private final AuthRolePermissionRepository rolePermissionRepository;
    private final AuthRefreshTokenRepository refreshTokenRepository;
    private final AuthLoginLogRepository loginLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthProperties authProperties;

    public AuthService(AuthUserRepository userRepository,
                       AuthRolePermissionRepository rolePermissionRepository,
                       AuthRefreshTokenRepository refreshTokenRepository,
                       AuthLoginLogRepository loginLogRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService,
                       AuthProperties authProperties) {
        this.userRepository = userRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginLogRepository = loginLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.authProperties = authProperties;
    }

    /**
     * Authenticates one username/password request and returns rotated tokens.
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        AuthUserEntity user = userRepository.findByUsername(request.username())
                .orElse(null);
        if (user == null) {
            audit(null, request.username(), LoginResult.BAD_CREDENTIALS, "USER_NOT_FOUND", ipAddress, userAgent);
            throw new BusinessException("INVALID_CREDENTIALS", "username or password is invalid");
        }
        if (user.getStatus() != AuthUserStatus.ACTIVE) {
            audit(user, request.username(), LoginResult.DISABLED, "USER_DISABLED", ipAddress, userAgent);
            throw new BusinessException("USER_DISABLED", "user is not active");
        }
        if (isLocked(user)) {
            audit(user, request.username(), LoginResult.LOCKED, "USER_LOCKED", ipAddress, userAgent);
            throw new BusinessException("USER_LOCKED", "too many failed login attempts");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            registerFailedLogin(user, ipAddress, userAgent);
            throw new BusinessException("INVALID_CREDENTIALS", "username or password is invalid");
        }
        Instant now = Instant.now();
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        user.setUpdatedAt(now);
        audit(user, request.username(), LoginResult.SUCCESS, null, ipAddress, userAgent);
        return issueTokens(user);
    }

    /**
     * Rotates a refresh token and issues a new access token.
     */
    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        String tokenHash = jwtTokenService.hashRefreshToken(request.refreshToken());
        AuthRefreshTokenEntity storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN", "refresh token is invalid"));
        Instant now = Instant.now();
        if (storedToken.getRevokedAt() != null || !storedToken.getExpiresAt().isAfter(now)) {
            throw new BusinessException("INVALID_REFRESH_TOKEN", "refresh token is expired or revoked");
        }
        AuthUserEntity user = userRepository.findByUserId(storedToken.getUserId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "user not found"));
        if (user.getStatus() != AuthUserStatus.ACTIVE || isLocked(user)) {
            throw new BusinessException("USER_NOT_ALLOWED", "user cannot refresh tokens");
        }
        storedToken.setRevokedAt(now);
        storedToken.setUpdatedAt(now);
        return issueTokens(user);
    }

    /**
     * Revokes one refresh token during explicit logout.
     */
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenHash(jwtTokenService.hashRefreshToken(request.refreshToken()))
                .ifPresent(token -> {
                    Instant now = Instant.now();
                    token.setRevokedAt(now);
                    token.setUpdatedAt(now);
                });
    }

    private LoginResponse issueTokens(AuthUserEntity user) {
        List<String> roles = rolePermissionRepository.findRoleCodesByUserId(user.getUserId());
        List<String> permissions = rolePermissionRepository.findPermissionCodesByUserId(user.getUserId());
        String accessToken = jwtTokenService.issueAccessToken(user, roles, permissions);
        IssuedRefreshToken refreshToken = jwtTokenService.issueRefreshToken();
        Instant now = Instant.now();
        AuthRefreshTokenEntity entity = new AuthRefreshTokenEntity();
        entity.setTokenId(refreshToken.tokenId());
        entity.setTokenHash(refreshToken.tokenHash());
        entity.setUserId(user.getUserId());
        entity.setExpiresAt(now.plus(authProperties.refreshTokenTtl()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        refreshTokenRepository.save(entity);
        return new LoginResponse(
                accessToken,
                refreshToken.token(),
                "Bearer",
                authProperties.accessTokenTtl().toSeconds(),
                user.getUserId(),
                user.getUsername(),
                user.getDisplayName(),
                roles,
                permissions);
    }

    private void registerFailedLogin(AuthUserEntity user, String ipAddress, String userAgent) {
        Instant now = Instant.now();
        int failedCount = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(failedCount);
        if (failedCount >= authProperties.maxFailedLogin()) {
            user.setLockedUntil(now.plus(authProperties.lockDuration()));
            audit(user, user.getUsername(), LoginResult.LOCKED, "MAX_FAILED_LOGIN", ipAddress, userAgent);
        } else {
            audit(user, user.getUsername(), LoginResult.BAD_CREDENTIALS, "PASSWORD_MISMATCH", ipAddress, userAgent);
        }
        user.setUpdatedAt(now);
    }

    private boolean isLocked(AuthUserEntity user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now());
    }

    private void audit(AuthUserEntity user,
                       String username,
                       LoginResult result,
                       String failureCode,
                       String ipAddress,
                       String userAgent) {
        AuthLoginLogEntity log = new AuthLoginLogEntity();
        log.setUserId(user == null ? null : user.getUserId());
        log.setUsername(username);
        log.setResult(result);
        log.setFailureCode(failureCode);
        log.setIpAddress(trim(ipAddress, 64));
        log.setUserAgent(trim(userAgent, 256));
        log.setCreatedAt(Instant.now());
        loginLogRepository.save(log);
    }

    private static String trim(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
