package com.musicwall.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicwall.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void registrationCrossesControllerServiceSecurityAndRepositoryLayers() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "short_password_user",
                                  "password": "short77"
                                }
                                """))
                .andExpect(status().isBadRequest());

        String responseBody = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "integration_user",
                                  "password": "password1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("integration_user"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode response = objectMapper.readTree(responseBody);
        var savedUser = userRepository.findByUsername("integration_user").orElseThrow();
        assertNotEquals("password1", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("password1", savedUser.getPassword()));
        assertTrue(response.get("token").asText().split("\\.").length == 3);
    }

    @Test
    void authenticatedUserCanChangePassword() throws Exception {
        String requestBody = """
                {
                  "currentPassword": "password1",
                  "newPassword": "password2"
                }
                """;
        mockMvc.perform(put("/api/profiles/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());

        String registrationBody = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "password_change_user",
                                  "password": "password1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = objectMapper.readTree(registrationBody).get("token").asText();

        mockMvc.perform(put("/api/profiles/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "password1",
                                  "newPassword": "short77"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/profiles/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());

        var savedUser = userRepository.findByUsername("password_change_user").orElseThrow();
        assertTrue(passwordEncoder.matches("password2", savedUser.getPassword()));
    }

    @Test
    void profileBioAcceptsThreeHundredCharactersAndRejectsMore() throws Exception {
        String registrationBody = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "bio_limit_user",
                                  "password": "password1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = objectMapper.readTree(registrationBody).get("token").asText();

        mockMvc.perform(put("/api/profiles/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bio", "a".repeat(300)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("a".repeat(300)));

        mockMvc.perform(put("/api/profiles/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bio", "a".repeat(301)))))
                .andExpect(status().isBadRequest());
    }
}
