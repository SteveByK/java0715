package com.stevebyk.java0715;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stevebyk.java0715.auth.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end web security tests for login, authentication and RBAC decisions.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldLoginAndReadProtectedAccount() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(get("/api/v1/accounts/AC_DEMO_CNY_001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectProtectedApiWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/AC_DEMO_CNY_001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAuditorWriteOperation() throws Exception {
        String token = login("auditor", "auditor123");

        mockMvc.perform(post("/api/v1/accounts/AC_DEMO_CNY_001/deposits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "auth-test-deposit-001",
                                  "amount": 1.00,
                                  "currency": "CNY",
                                  "remark": "should be forbidden"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRotateRefreshToken() throws Exception {
        JsonNode login = loginResponse("teller", "teller123");
        String refreshToken = login.at("/data/refreshToken").asText();

        String refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(refreshResponse);
        assertThat(json.at("/data/accessToken").asText()).isNotBlank();
        assertThat(json.at("/data/refreshToken").asText()).isNotEqualTo(refreshToken);
    }

    private String login(String username, String password) throws Exception {
        return loginResponse(username, password).at("/data/accessToken").asText();
    }

    private JsonNode loginResponse(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }
}
