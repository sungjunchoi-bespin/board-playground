package com.conduit.tag.adapter.in.web;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tag Integration Tests")
class TagIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("GET /api/tags should return empty array when no articles with tags")
  void emptyTags() throws Exception {
    mockMvc
        .perform(get("/api/tags"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tags").isArray())
        .andExpect(jsonPath("$.tags", hasSize(0)));
  }

  @Test
  @DisplayName("GET /api/tags should return tags from created articles")
  void tagsFromArticles() throws Exception {
    String token = registerAndGetToken("taguser", "tag@test.com", "password123");

    // Create article with tags
    mockMvc
        .perform(
            post("/api/articles")
                .header("Authorization", "Token " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
            {"article":{"title":"Test Article","description":"desc","body":"body","tagList":["java","spring"]}}
            """))
        .andExpect(status().isOk());

    // GET /api/tags should include the tags
    mockMvc
        .perform(get("/api/tags"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tags").isArray())
        .andExpect(jsonPath("$.tags", hasSize(2)))
        .andExpect(jsonPath("$.tags", hasItem("java")))
        .andExpect(jsonPath("$.tags", hasItem("spring")));
  }

  @Test
  @DisplayName("tags should be deduplicated across articles")
  void deduplicatedTags() throws Exception {
    String token = registerAndGetToken("taguser2", "tag2@test.com", "password123");

    // Create two articles with overlapping tags
    mockMvc
        .perform(
            post("/api/articles")
                .header("Authorization", "Token " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
            {"article":{"title":"Article 1","description":"desc","body":"body","tagList":["java","spring"]}}
            """))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/articles")
                .header("Authorization", "Token " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
            {"article":{"title":"Article 2","description":"desc","body":"body","tagList":["java","react"]}}
            """))
        .andExpect(status().isOk());

    // Should return 3 unique tags, not 4
    mockMvc
        .perform(get("/api/tags"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tags", hasSize(3)))
        .andExpect(jsonPath("$.tags", hasItem("java")))
        .andExpect(jsonPath("$.tags", hasItem("spring")))
        .andExpect(jsonPath("$.tags", hasItem("react")));
  }

  @Test
  @DisplayName("GET /api/tags should not require authentication")
  void publicAccess() throws Exception {
    mockMvc.perform(get("/api/tags")).andExpect(status().isOk());
  }

  private String registerAndGetToken(String username, String email, String password)
      throws Exception {
    String body =
        """
        {"user":{"username":"%s","email":"%s","password":"%s"}}
        """
            .formatted(username, email, password);

    MvcResult result =
        mockMvc
            .perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andReturn();

    return JsonPath.read(result.getResponse().getContentAsString(), "$.user.token");
  }
}
