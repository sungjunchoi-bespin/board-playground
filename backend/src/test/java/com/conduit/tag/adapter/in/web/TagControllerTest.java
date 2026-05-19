package com.conduit.tag.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("TagController Tests")
class TagControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("GET /api/tags should return 200 with tags array")
  void getTags() throws Exception {
    mockMvc
        .perform(get("/api/tags"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.tags").isArray());
  }

  @Test
  @DisplayName("GET /api/tags should be accessible without authentication")
  void getTagsPublic() throws Exception {
    mockMvc.perform(get("/api/tags")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /api/tags response should have correct content type")
  void contentType() throws Exception {
    mockMvc
        .perform(get("/api/tags"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }
}
