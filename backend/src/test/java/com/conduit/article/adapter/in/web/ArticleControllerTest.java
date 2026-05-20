package com.conduit.article.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conduit.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
class ArticleControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtTokenProvider jwtTokenProvider;

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

    return com.jayway.jsonpath.JsonPath.read(
        result.getResponse().getContentAsString(), "$.user.token");
  }

  private String createArticle(String token, String title, String desc, String body, String tags)
      throws Exception {
    String requestBody =
        """
        {"article":{"title":"%s","description":"%s","body":"%s","tagList":[%s]}}
        """
            .formatted(title, desc, body, tags);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/articles")
                    .header("Authorization", "Token " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isOk())
            .andReturn();

    return com.jayway.jsonpath.JsonPath.read(
        result.getResponse().getContentAsString(), "$.article.slug");
  }

  @Nested
  @DisplayName("POST /api/articles")
  class CreateArticle {

    @Test
    @DisplayName("should create article with tags")
    void success() throws Exception {
      String token = registerAndGetToken("jacob", "jake@jake.com", "jakejake1");

      mockMvc
          .perform(
              post("/api/articles")
                  .header("Authorization", "Token " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"article":{"title":"How to train your dragon","description":"Ever wonder how?","body":"It takes a village...","tagList":["dragons","training"]}}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.slug").value("how-to-train-your-dragon"))
          .andExpect(jsonPath("$.article.title").value("How to train your dragon"))
          .andExpect(jsonPath("$.article.description").value("Ever wonder how?"))
          .andExpect(jsonPath("$.article.body").value("It takes a village..."))
          .andExpect(jsonPath("$.article.tagList[0]").value("dragons"))
          .andExpect(jsonPath("$.article.tagList[1]").value("training"))
          .andExpect(jsonPath("$.article.favoritesCount").value(0))
          .andExpect(jsonPath("$.article.author.username").value("jacob"))
          .andExpect(jsonPath("$.article.createdAt").isNotEmpty())
          .andExpect(jsonPath("$.article.updatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("should create article without tags")
    void withoutTags() throws Exception {
      String token = registerAndGetToken("jacob", "jake@jake.com", "jakejake1");

      mockMvc
          .perform(
              post("/api/articles")
                  .header("Authorization", "Token " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"article":{"title":"No Tags Article","description":"Desc","body":"Body"}}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.slug").value("no-tags-article"))
          .andExpect(jsonPath("$.article.tagList").isEmpty());
    }

    @Test
    @DisplayName("should return 401 without authentication")
    void unauthorized() throws Exception {
      mockMvc
          .perform(
              post("/api/articles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"article":{"title":"Test","description":"Desc","body":"Body"}}
                      """))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should append suffix for duplicate slug")
    void duplicateSlug() throws Exception {
      String token = registerAndGetToken("jacob", "jake@jake.com", "jakejake1");

      createArticle(token, "Same Title", "Desc 1", "Body 1", "");

      mockMvc
          .perform(
              post("/api/articles")
                  .header("Authorization", "Token " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"article":{"title":"Same Title","description":"Desc 2","body":"Body 2"}}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.slug").value("same-title-1"));
    }
  }

  @Nested
  @DisplayName("GET /api/articles/{slug}")
  class GetArticle {

    @Test
    @DisplayName("should get article by slug without authentication")
    void success() throws Exception {
      String token = registerAndGetToken("jacob", "jake@jake.com", "jakejake1");
      String slug = createArticle(token, "My Article", "Desc", "Body", "\"tag1\"");

      mockMvc
          .perform(get("/api/articles/" + slug))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.slug").value(slug))
          .andExpect(jsonPath("$.article.title").value("My Article"))
          .andExpect(jsonPath("$.article.author.username").value("jacob"));
    }

    @Test
    @DisplayName("should return 404 for nonexistent slug")
    void notFound() throws Exception {
      mockMvc.perform(get("/api/articles/nonexistent")).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("PUT /api/articles/{slug}")
  class UpdateArticle {

    @Test
    @DisplayName("should update article title")
    void success() throws Exception {
      String token = registerAndGetToken("jacob", "jake@jake.com", "jakejake1");
      String slug = createArticle(token, "Old Title", "Desc", "Body", "");

      mockMvc
          .perform(
              put("/api/articles/" + slug)
                  .header("Authorization", "Token " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"article":{"title":"New Title"}}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.title").value("New Title"))
          .andExpect(jsonPath("$.article.slug").value("new-title"));
    }

    @Test
    @DisplayName("should return 403 when non-author tries to update")
    void forbidden() throws Exception {
      String authorToken = registerAndGetToken("author", "author@test.com", "password123");
      String otherToken = registerAndGetToken("other", "other@test.com", "password123");
      String slug = createArticle(authorToken, "Author Article", "Desc", "Body", "");

      mockMvc
          .perform(
              put("/api/articles/" + slug)
                  .header("Authorization", "Token " + otherToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"article":{"title":"Hacked Title"}}
                      """))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return 401 without authentication")
    void unauthorized() throws Exception {
      mockMvc
          .perform(
              put("/api/articles/some-slug")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"article":{"title":"New Title"}}
                      """))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("DELETE /api/articles/{slug}")
  class DeleteArticle {

    @Test
    @DisplayName("should delete article by author")
    void success() throws Exception {
      String token = registerAndGetToken("jacob", "jake@jake.com", "jakejake1");
      String slug = createArticle(token, "To Delete", "Desc", "Body", "");

      mockMvc
          .perform(
              delete("/api/articles/" + slug).header("Authorization", "Token " + token))
          .andExpect(status().isNoContent());

      // Verify article is deleted
      mockMvc.perform(get("/api/articles/" + slug)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 403 when non-author tries to delete")
    void forbidden() throws Exception {
      String authorToken = registerAndGetToken("author", "author@test.com", "password123");
      String otherToken = registerAndGetToken("other", "other@test.com", "password123");
      String slug = createArticle(authorToken, "Author Article", "Desc", "Body", "");

      mockMvc
          .perform(
              delete("/api/articles/" + slug).header("Authorization", "Token " + otherToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return 401 without authentication")
    void unauthorized() throws Exception {
      mockMvc.perform(delete("/api/articles/some-slug")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return 404 for nonexistent article")
    void notFound() throws Exception {
      String token = registerAndGetToken("jacob", "jake@jake.com", "jakejake1");

      mockMvc
          .perform(
              delete("/api/articles/nonexistent").header("Authorization", "Token " + token))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("POST /api/articles/{slug}/favorite")
  class FavoriteArticle {

    @Test
    @DisplayName("should favorite article and return updated count")
    void success() throws Exception {
      String token = registerAndGetToken("jacob", "jake@jake.com", "jakejake1");
      String slug = createArticle(token, "Fav Article", "Desc", "Body", "");

      mockMvc
          .perform(
              post("/api/articles/" + slug + "/favorite")
                  .header("Authorization", "Token " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.slug").value(slug))
          .andExpect(jsonPath("$.article.favorited").value(true))
          .andExpect(jsonPath("$.article.favoritesCount").value(1));
    }

    @Test
    @DisplayName("should be idempotent - favoriting twice returns same count")
    void idempotent() throws Exception {
      String token = registerAndGetToken("jacob", "jake@jake.com", "jakejake1");
      String slug = createArticle(token, "Fav Article", "Desc", "Body", "");

      mockMvc
          .perform(
              post("/api/articles/" + slug + "/favorite")
                  .header("Authorization", "Token " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favoritesCount").value(1));

      mockMvc
          .perform(
              post("/api/articles/" + slug + "/favorite")
                  .header("Authorization", "Token " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favoritesCount").value(1));
    }

    @Test
    @DisplayName("should return 401 without authentication")
    void unauthorized() throws Exception {
      mockMvc
          .perform(post("/api/articles/some-slug/favorite"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return 404 for nonexistent article")
    void notFound() throws Exception {
      String token = registerAndGetToken("jacob", "jake@jake.com", "jakejake1");

      mockMvc
          .perform(
              post("/api/articles/nonexistent/favorite")
                  .header("Authorization", "Token " + token))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("DELETE /api/articles/{slug}/favorite")
  class UnfavoriteArticle {

    @Test
    @DisplayName("should unfavorite article and return updated count")
    void success() throws Exception {
      String token = registerAndGetToken("jacob", "jake@jake.com", "jakejake1");
      String slug = createArticle(token, "Unfav Article", "Desc", "Body", "");

      // Favorite first
      mockMvc
          .perform(
              post("/api/articles/" + slug + "/favorite")
                  .header("Authorization", "Token " + token))
          .andExpect(status().isOk());

      // Unfavorite
      mockMvc
          .perform(
              delete("/api/articles/" + slug + "/favorite")
                  .header("Authorization", "Token " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.slug").value(slug))
          .andExpect(jsonPath("$.article.favorited").value(false))
          .andExpect(jsonPath("$.article.favoritesCount").value(0));
    }

    @Test
    @DisplayName("should be safe to unfavorite when not favorited")
    void idempotent() throws Exception {
      String token = registerAndGetToken("jacob", "jake@jake.com", "jakejake1");
      String slug = createArticle(token, "Never Fav Article", "Desc", "Body", "");

      mockMvc
          .perform(
              delete("/api/articles/" + slug + "/favorite")
                  .header("Authorization", "Token " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favoritesCount").value(0));
    }

    @Test
    @DisplayName("should return 401 without authentication")
    void unauthorized() throws Exception {
      mockMvc
          .perform(delete("/api/articles/some-slug/favorite"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should track multiple users favoriting same article")
    void multipleUsers() throws Exception {
      String token1 = registerAndGetToken("user1", "user1@test.com", "password123");
      String token2 = registerAndGetToken("user2", "user2@test.com", "password123");
      String slug = createArticle(token1, "Popular Article", "Desc", "Body", "");

      mockMvc
          .perform(
              post("/api/articles/" + slug + "/favorite")
                  .header("Authorization", "Token " + token1))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favoritesCount").value(1));

      mockMvc
          .perform(
              post("/api/articles/" + slug + "/favorite")
                  .header("Authorization", "Token " + token2))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favoritesCount").value(2));

      // User 1 unfavorites
      mockMvc
          .perform(
              delete("/api/articles/" + slug + "/favorite")
                  .header("Authorization", "Token " + token1))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favoritesCount").value(1));
    }
  }
}
