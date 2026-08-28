package com.musicwall.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicwall.entity.Wallpaper;
import com.musicwall.repository.MusicWallRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WallpaperIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MusicWallRepository musicWallRepository;

    @Test
    void wallpaperEnumIsDeserializedSerializedAndPersistedAsAString() throws Exception {
        String registrationBody = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "wallpaper_enum_user",
                                  "password": "password1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = objectMapper.readTree(registrationBody).get("token").asText();

        String createdBody = mockMvc.perform(post("/api/walls")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Enum wallpaper wall",
                                  "wallpaper": "IMAGE_3",
                                  "wallColor": "#123456"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.wallpaper").value("IMAGE_3"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long wallId = objectMapper.readTree(createdBody).get("id").asLong();
        assertEquals(Wallpaper.IMAGE_3, musicWallRepository.findById(wallId).orElseThrow().getWallpaper());

        mockMvc.perform(put("/api/walls/{id}/appearance", wallId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "wallpaper": "NONE",
                                  "wallColor": "#654321"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wallpaper").value("NONE"));
        assertEquals(Wallpaper.NONE, musicWallRepository.findById(wallId).orElseThrow().getWallpaper());

        mockMvc.perform(post("/api/walls")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Invalid wallpaper wall",
                                  "wallpaper": "IMAGE_10",
                                  "wallColor": "#123456"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
