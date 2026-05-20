package com.conduit;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
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

/** Full API Integration Test — 19 endpoints, cross-module scenarios, error cases. Issue #23. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FullApiIntegrationTest {

  @Autowired private MockMvc mockMvc;

  private static int counter = 0;

  private String uniqueUsername() {
    return "intuser" + (++counter) + System.nanoTime();
  }

  private String registerAndGetToken(String username) throws Exception {
    String email = username + "@test.com";
    String body =
        """
        {"user":{"username":"%s","email":"%s","password":"password123"}}
        """
            .formatted(username, email);
    MvcResult result =
        mockMvc
            .perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.user.token");
  }

  private String createArticle(String token, String title, String... tags) throws Exception {
    StringBuilder tagList = new StringBuilder("[");
    for (int i = 0; i < tags.length; i++) {
      if (i > 0) tagList.append(",");
      tagList.append("\"").append(tags[i]).append("\"");
    }
    tagList.append("]");
    String body =
        """
        {"article":{"title":"%s","description":"desc","body":"body content","tagList":%s}}
        """
            .formatted(title, tagList);
    MvcResult result =
        mockMvc
            .perform(
                post("/api/articles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Token " + token)
                    .content(body))
            .andExpect(status().isOk())
            .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.article.slug");
  }

  // ─── 1. User Endpoints (4) ────────────────────────────

  @Nested
  @DisplayName("User Endpoints")
  class UserEndpoints {

    @Test
    @DisplayName("POST /api/users — register success")
    void registerSuccess() throws Exception {
      String username = uniqueUsername();
      mockMvc
          .perform(
              post("/api/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"user":{"username":"%s","email":"%s@test.com","password":"password123"}}
                      """
                          .formatted(username, username)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.user.username", is(username)))
          .andExpect(jsonPath("$.user.email", is(username + "@test.com")))
          .andExpect(jsonPath("$.user.token", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/users — duplicate email → 422")
    void registerDuplicateEmail() throws Exception {
      String username = uniqueUsername();
      registerAndGetToken(username);
      String username2 = uniqueUsername();
      mockMvc
          .perform(
              post("/api/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"user":{"username":"%s","email":"%s@test.com","password":"password123"}}
                      """
                          .formatted(username2, username)))
          .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/users/login — login success")
    void loginSuccess() throws Exception {
      String username = uniqueUsername();
      registerAndGetToken(username);
      mockMvc
          .perform(
              post("/api/users/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"user":{"email":"%s@test.com","password":"password123"}}
                      """
                          .formatted(username)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.user.token", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/users/login — wrong password → 401")
    void loginWrongPassword() throws Exception {
      String username = uniqueUsername();
      registerAndGetToken(username);
      mockMvc
          .perform(
              post("/api/users/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"user":{"email":"%s@test.com","password":"wrongpassword"}}
                      """
                          .formatted(username)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/user — get current user")
    void getCurrentUser() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      mockMvc
          .perform(get("/api/user").header("Authorization", "Token " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.user.username", is(username)))
          .andExpect(jsonPath("$.user.email", is(username + "@test.com")));
    }

    @Test
    @DisplayName("GET /api/user — no token → 401")
    void getCurrentUserUnauthorized() throws Exception {
      mockMvc.perform(get("/api/user")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/user — update user")
    void updateUser() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      mockMvc
          .perform(
              put("/api/user")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Token " + token)
                  .content(
                      """
                      {"user":{"bio":"Updated bio","image":"https://example.com/img.png"}}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.user.bio", is("Updated bio")))
          .andExpect(jsonPath("$.user.image", is("https://example.com/img.png")));
    }

    @Test
    @DisplayName("PUT /api/user — no token → 401")
    void updateUserUnauthorized() throws Exception {
      mockMvc
          .perform(
              put("/api/user")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"user":{"bio":"Nope"}}
                      """))
          .andExpect(status().isUnauthorized());
    }
  }

  // ─── 2. Article Endpoints (6) ─────────────────────────

  @Nested
  @DisplayName("Article Endpoints")
  class ArticleEndpoints {

    @Test
    @DisplayName("POST /api/articles — create article with tags")
    void createArticle() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      mockMvc
          .perform(
              post("/api/articles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Token " + token)
                  .content(
                      """
                      {"article":{"title":"Test Article","description":"desc","body":"body","tagList":["java","spring"]}}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.title", is("Test Article")))
          .andExpect(jsonPath("$.article.slug", notNullValue()))
          .andExpect(jsonPath("$.article.tagList", hasSize(2)))
          .andExpect(jsonPath("$.article.author.username", is(username)));
    }

    @Test
    @DisplayName("POST /api/articles — no token → 401")
    void createArticleUnauthorized() throws Exception {
      mockMvc
          .perform(
              post("/api/articles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"article":{"title":"X","description":"d","body":"b","tagList":[]}}
                      """))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/articles/{slug} — get single article")
    void getArticle() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      String slug = FullApiIntegrationTest.this.createArticle(token, "Get Test");
      mockMvc
          .perform(get("/api/articles/" + slug))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.title", is("Get Test")))
          .andExpect(jsonPath("$.article.author.username", is(username)));
    }

    @Test
    @DisplayName("GET /api/articles/{slug} — not found → 404")
    void getArticleNotFound() throws Exception {
      mockMvc.perform(get("/api/articles/nonexistent-slug")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/articles/{slug} — update article")
    void updateArticle() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      String slug = FullApiIntegrationTest.this.createArticle(token, "Original Title");
      mockMvc
          .perform(
              put("/api/articles/" + slug)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Token " + token)
                  .content(
                      """
                      {"article":{"title":"Updated Title"}}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.title", is("Updated Title")));
    }

    @Test
    @DisplayName("PUT /api/articles/{slug} — non-author → 403")
    void updateArticleForbidden() throws Exception {
      String author = uniqueUsername();
      String authorToken = registerAndGetToken(author);
      String slug = FullApiIntegrationTest.this.createArticle(authorToken, "Author Only");
      String other = uniqueUsername();
      String otherToken = registerAndGetToken(other);
      mockMvc
          .perform(
              put("/api/articles/" + slug)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Token " + otherToken)
                  .content(
                      """
                      {"article":{"title":"Hacked"}}
                      """))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/articles/{slug} — delete by author")
    void deleteArticle() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      String slug = FullApiIntegrationTest.this.createArticle(token, "To Delete");
      mockMvc
          .perform(delete("/api/articles/" + slug).header("Authorization", "Token " + token))
          .andExpect(status().isNoContent());
      mockMvc.perform(get("/api/articles/" + slug)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/articles/{slug} — non-author → 403")
    void deleteArticleForbidden() throws Exception {
      String author = uniqueUsername();
      String authorToken = registerAndGetToken(author);
      String slug = FullApiIntegrationTest.this.createArticle(authorToken, "Protected");
      String other = uniqueUsername();
      String otherToken = registerAndGetToken(other);
      mockMvc
          .perform(delete("/api/articles/" + slug).header("Authorization", "Token " + otherToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/articles — list articles")
    void listArticles() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      FullApiIntegrationTest.this.createArticle(token, "List A", "tagA");
      FullApiIntegrationTest.this.createArticle(token, "List B", "tagB");
      mockMvc
          .perform(get("/api/articles"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles", hasSize(greaterThanOrEqualTo(2))))
          .andExpect(jsonPath("$.articlesCount", greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("GET /api/articles?tag=X — filter by tag")
    void listArticlesByTag() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      FullApiIntegrationTest.this.createArticle(token, "Tagged Java", "java");
      FullApiIntegrationTest.this.createArticle(token, "Tagged Go", "golang");
      mockMvc
          .perform(get("/api/articles").param("tag", "java"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles[0].tagList", hasItem("java")));
    }

    @Test
    @DisplayName("GET /api/articles?author=X — filter by author")
    void listArticlesByAuthor() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      FullApiIntegrationTest.this.createArticle(token, "By Author");
      mockMvc
          .perform(get("/api/articles").param("author", username))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles[0].author.username", is(username)));
    }

    @Test
    @DisplayName("GET /api/articles/feed — authenticated user feed")
    void feedArticles() throws Exception {
      // author creates article
      String author = uniqueUsername();
      String authorToken = registerAndGetToken(author);
      FullApiIntegrationTest.this.createArticle(authorToken, "Feed Article");

      // follower follows author
      String follower = uniqueUsername();
      String followerToken = registerAndGetToken(follower);
      mockMvc
          .perform(
              post("/api/profiles/" + author + "/follow")
                  .header("Authorization", "Token " + followerToken))
          .andExpect(status().isOk());

      // follower's feed should contain author's article
      mockMvc
          .perform(get("/api/articles/feed").header("Authorization", "Token " + followerToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles", hasSize(greaterThanOrEqualTo(1))))
          .andExpect(jsonPath("$.articles[0].author.username", is(author)));
    }

    @Test
    @DisplayName("GET /api/articles/feed — no token → 401")
    void feedArticlesUnauthorized() throws Exception {
      mockMvc.perform(get("/api/articles/feed")).andExpect(status().isUnauthorized());
    }
  }

  // ─── 3. Comment Endpoints (3) ─────────────────────────

  @Nested
  @DisplayName("Comment Endpoints")
  class CommentEndpoints {

    @Test
    @DisplayName("POST /api/articles/{slug}/comments — add comment")
    void addComment() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      String slug = createArticle(token, "Commentable");
      mockMvc
          .perform(
              post("/api/articles/" + slug + "/comments")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Token " + token)
                  .content(
                      """
                      {"comment":{"body":"Great article!"}}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.comment.body", is("Great article!")))
          .andExpect(jsonPath("$.comment.author.username", is(username)));
    }

    @Test
    @DisplayName("POST /api/articles/{slug}/comments — no token → 401")
    void addCommentUnauthorized() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      String slug = createArticle(token, "Comment Unauth");
      mockMvc
          .perform(
              post("/api/articles/" + slug + "/comments")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"comment":{"body":"Nope"}}
                      """))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/articles/{slug}/comments — article not found → 404")
    void addCommentArticleNotFound() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      mockMvc
          .perform(
              post("/api/articles/nonexistent/comments")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Token " + token)
                  .content(
                      """
                      {"comment":{"body":"Nope"}}
                      """))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/articles/{slug}/comments — list comments")
    void listComments() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      String slug = createArticle(token, "With Comments");
      // Add two comments
      for (String text : new String[] {"Comment 1", "Comment 2"}) {
        mockMvc.perform(
            post("/api/articles/" + slug + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Token " + token)
                .content(
                    """
                    {"comment":{"body":"%s"}}
                    """
                        .formatted(text)));
      }
      mockMvc
          .perform(get("/api/articles/" + slug + "/comments"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.comments", hasSize(2)));
    }

    @Test
    @DisplayName("DELETE /api/articles/{slug}/comments/{id} — delete own comment")
    void deleteComment() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      String slug = createArticle(token, "Delete Comment Art");
      MvcResult result =
          mockMvc
              .perform(
                  post("/api/articles/" + slug + "/comments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Token " + token)
                      .content(
                          """
                          {"comment":{"body":"To delete"}}
                          """))
              .andExpect(status().isOk())
              .andReturn();
      int commentId = JsonPath.read(result.getResponse().getContentAsString(), "$.comment.id");
      mockMvc
          .perform(
              delete("/api/articles/" + slug + "/comments/" + commentId)
                  .header("Authorization", "Token " + token))
          .andExpect(status().isOk());
      // Verify deleted
      mockMvc
          .perform(get("/api/articles/" + slug + "/comments"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.comments", hasSize(0)));
    }

    @Test
    @DisplayName("DELETE /api/articles/{slug}/comments/{id} — non-author → 403")
    void deleteCommentForbidden() throws Exception {
      String author = uniqueUsername();
      String authorToken = registerAndGetToken(author);
      String slug = createArticle(authorToken, "Forbid Delete");
      MvcResult result =
          mockMvc
              .perform(
                  post("/api/articles/" + slug + "/comments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Token " + authorToken)
                      .content(
                          """
                          {"comment":{"body":"My comment"}}
                          """))
              .andExpect(status().isOk())
              .andReturn();
      int commentId = JsonPath.read(result.getResponse().getContentAsString(), "$.comment.id");
      String other = uniqueUsername();
      String otherToken = registerAndGetToken(other);
      mockMvc
          .perform(
              delete("/api/articles/" + slug + "/comments/" + commentId)
                  .header("Authorization", "Token " + otherToken))
          .andExpect(status().isForbidden());
    }
  }

  // ─── 4. Favorite Endpoints (2) ────────────────────────

  @Nested
  @DisplayName("Favorite Endpoints")
  class FavoriteEndpoints {

    @Test
    @DisplayName("POST /api/articles/{slug}/favorite — favorite article")
    void favoriteArticle() throws Exception {
      String author = uniqueUsername();
      String authorToken = registerAndGetToken(author);
      String slug = createArticle(authorToken, "To Favorite");
      String fan = uniqueUsername();
      String fanToken = registerAndGetToken(fan);
      mockMvc
          .perform(
              post("/api/articles/" + slug + "/favorite")
                  .header("Authorization", "Token " + fanToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favorited", is(true)))
          .andExpect(jsonPath("$.article.favoritesCount", is(1)));
    }

    @Test
    @DisplayName("POST /api/articles/{slug}/favorite — idempotent")
    void favoriteIdempotent() throws Exception {
      String author = uniqueUsername();
      String authorToken = registerAndGetToken(author);
      String slug = createArticle(authorToken, "Fav Idem");
      String fan = uniqueUsername();
      String fanToken = registerAndGetToken(fan);
      mockMvc.perform(
          post("/api/articles/" + slug + "/favorite").header("Authorization", "Token " + fanToken));
      mockMvc
          .perform(
              post("/api/articles/" + slug + "/favorite")
                  .header("Authorization", "Token " + fanToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favoritesCount", is(1)));
    }

    @Test
    @DisplayName("DELETE /api/articles/{slug}/favorite — unfavorite article")
    void unfavoriteArticle() throws Exception {
      String author = uniqueUsername();
      String authorToken = registerAndGetToken(author);
      String slug = createArticle(authorToken, "To Unfavorite");
      String fan = uniqueUsername();
      String fanToken = registerAndGetToken(fan);
      mockMvc.perform(
          post("/api/articles/" + slug + "/favorite").header("Authorization", "Token " + fanToken));
      mockMvc
          .perform(
              delete("/api/articles/" + slug + "/favorite")
                  .header("Authorization", "Token " + fanToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favorited", is(false)))
          .andExpect(jsonPath("$.article.favoritesCount", is(0)));
    }

    @Test
    @DisplayName("POST /api/articles/{slug}/favorite — not found → 404")
    void favoriteNotFound() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      mockMvc
          .perform(
              post("/api/articles/nonexistent/favorite").header("Authorization", "Token " + token))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/articles/{slug}/favorite — no token → 401")
    void favoriteUnauthorized() throws Exception {
      mockMvc
          .perform(post("/api/articles/some-slug/favorite"))
          .andExpect(status().isUnauthorized());
    }
  }

  // ─── 5. Profile Endpoints (3) ─────────────────────────

  @Nested
  @DisplayName("Profile Endpoints")
  class ProfileEndpoints {

    @Test
    @DisplayName("GET /api/profiles/{username} — get profile")
    void getProfile() throws Exception {
      String username = uniqueUsername();
      registerAndGetToken(username);
      mockMvc
          .perform(get("/api/profiles/" + username))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.profile.username", is(username)))
          .andExpect(jsonPath("$.profile.following", is(false)));
    }

    @Test
    @DisplayName("GET /api/profiles/{username} — authenticated, following=true after follow")
    void getProfileFollowing() throws Exception {
      String target = uniqueUsername();
      registerAndGetToken(target);
      String follower = uniqueUsername();
      String followerToken = registerAndGetToken(follower);
      mockMvc.perform(
          post("/api/profiles/" + target + "/follow")
              .header("Authorization", "Token " + followerToken));
      mockMvc
          .perform(get("/api/profiles/" + target).header("Authorization", "Token " + followerToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.profile.following", is(true)));
    }

    @Test
    @DisplayName("POST /api/profiles/{username}/follow — follow user")
    void followUser() throws Exception {
      String target = uniqueUsername();
      registerAndGetToken(target);
      String follower = uniqueUsername();
      String followerToken = registerAndGetToken(follower);
      mockMvc
          .perform(
              post("/api/profiles/" + target + "/follow")
                  .header("Authorization", "Token " + followerToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.profile.following", is(true)));
    }

    @Test
    @DisplayName("POST /api/profiles/{username}/follow — no token → 401")
    void followUnauthorized() throws Exception {
      mockMvc.perform(post("/api/profiles/someone/follow")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/profiles/{username}/follow — unfollow user")
    void unfollowUser() throws Exception {
      String target = uniqueUsername();
      registerAndGetToken(target);
      String follower = uniqueUsername();
      String followerToken = registerAndGetToken(follower);
      mockMvc.perform(
          post("/api/profiles/" + target + "/follow")
              .header("Authorization", "Token " + followerToken));
      mockMvc
          .perform(
              delete("/api/profiles/" + target + "/follow")
                  .header("Authorization", "Token " + followerToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.profile.following", is(false)));
    }
  }

  // ─── 6. Tags Endpoint (1) ────────────────────────────

  @Nested
  @DisplayName("Tags Endpoint")
  class TagEndpoints {

    @Test
    @DisplayName("GET /api/tags — list tags from articles")
    void listTags() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);
      createArticle(token, "Tag Art 1", "java", "spring");
      createArticle(token, "Tag Art 2", "java", "react");
      mockMvc
          .perform(get("/api/tags"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.tags", hasItem("java")))
          .andExpect(jsonPath("$.tags", hasItem("spring")))
          .andExpect(jsonPath("$.tags", hasItem("react")));
    }

    @Test
    @DisplayName("GET /api/tags — no auth required")
    void listTagsPublic() throws Exception {
      mockMvc.perform(get("/api/tags")).andExpect(status().isOk());
    }
  }

  // ─── 7. Cross-Module Scenario ─────────────────────────

  @Nested
  @DisplayName("Cross-Module Scenario")
  class CrossModuleScenario {

    @Test
    @DisplayName("Full flow: register → article → tags → comment → favorite → follow → feed")
    void fullEndToEndFlow() throws Exception {
      // === Step 1: Register user A ===
      String userA = uniqueUsername();
      String tokenA = registerAndGetToken(userA);

      // === Step 2: User A creates articles with tags ===
      String slug1 = createArticle(tokenA, "Java Guide", "java", "tutorial");
      String slug2 = createArticle(tokenA, "React Guide", "react", "frontend");

      // Verify articles appear in list
      mockMvc
          .perform(get("/api/articles").param("author", userA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articlesCount", is(2)));

      // Verify tags exist
      mockMvc
          .perform(get("/api/tags"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.tags", hasItem("java")))
          .andExpect(jsonPath("$.tags", hasItem("react")));

      // === Step 3: Register user B ===
      String userB = uniqueUsername();
      String tokenB = registerAndGetToken(userB);

      // === Step 4: User B comments on user A's article ===
      MvcResult commentResult =
          mockMvc
              .perform(
                  post("/api/articles/" + slug1 + "/comments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Token " + tokenB)
                      .content(
                          """
                          {"comment":{"body":"Awesome Java guide!"}}
                          """))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.comment.author.username", is(userB)))
              .andReturn();
      int commentId =
          JsonPath.read(commentResult.getResponse().getContentAsString(), "$.comment.id");

      // Verify comment appears in list
      mockMvc
          .perform(get("/api/articles/" + slug1 + "/comments"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.comments", hasSize(1)))
          .andExpect(jsonPath("$.comments[0].body", is("Awesome Java guide!")));

      // === Step 5: User B favorites user A's article ===
      mockMvc
          .perform(
              post("/api/articles/" + slug1 + "/favorite")
                  .header("Authorization", "Token " + tokenB))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favorited", is(true)))
          .andExpect(jsonPath("$.article.favoritesCount", is(1)));

      // Verify favorited=true when user B gets the article
      mockMvc
          .perform(get("/api/articles/" + slug1).header("Authorization", "Token " + tokenB))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favorited", is(true)));

      // Verify favorited=false for unauthenticated
      mockMvc
          .perform(get("/api/articles/" + slug1))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favorited", is(false)));

      // Verify favorited filter works
      mockMvc
          .perform(get("/api/articles").param("favorited", userB))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articlesCount", is(1)))
          .andExpect(jsonPath("$.articles[0].slug", is(slug1)));

      // === Step 6: User B follows user A ===
      mockMvc
          .perform(
              post("/api/profiles/" + userA + "/follow").header("Authorization", "Token " + tokenB))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.profile.following", is(true)));

      // === Step 7: User B's feed contains user A's articles ===
      mockMvc
          .perform(get("/api/articles/feed").header("Authorization", "Token " + tokenB))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles", hasSize(2)))
          .andExpect(jsonPath("$.articlesCount", is(2)));

      // Verify following=true in article response for user B
      mockMvc
          .perform(get("/api/articles/" + slug1).header("Authorization", "Token " + tokenB))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.author.following", is(true)));

      // === Step 8: User B unfavorites ===
      mockMvc
          .perform(
              delete("/api/articles/" + slug1 + "/favorite")
                  .header("Authorization", "Token " + tokenB))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favoritesCount", is(0)));

      // === Step 9: User B unfollows user A ===
      mockMvc
          .perform(
              delete("/api/profiles/" + userA + "/follow")
                  .header("Authorization", "Token " + tokenB))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.profile.following", is(false)));

      // Feed should now be empty for user B
      mockMvc
          .perform(get("/api/articles/feed").header("Authorization", "Token " + tokenB))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles", empty()));

      // === Step 10: User B deletes own comment ===
      mockMvc
          .perform(
              delete("/api/articles/" + slug1 + "/comments/" + commentId)
                  .header("Authorization", "Token " + tokenB))
          .andExpect(status().isOk());

      mockMvc
          .perform(get("/api/articles/" + slug1 + "/comments"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.comments", hasSize(0)));

      // === Step 11: User A updates profile ===
      mockMvc
          .perform(
              put("/api/user")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Token " + tokenA)
                  .content(
                      """
                      {"user":{"bio":"Java expert"}}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.user.bio", is("Java expert")));

      // Verify profile reflects update
      mockMvc
          .perform(get("/api/profiles/" + userA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.profile.bio", is("Java expert")));

      // === Step 12: User A deletes article ===
      mockMvc
          .perform(delete("/api/articles/" + slug1).header("Authorization", "Token " + tokenA))
          .andExpect(status().isNoContent());

      mockMvc.perform(get("/api/articles/" + slug1)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Multi-user favorites: count reflects distinct users")
    void multiUserFavorites() throws Exception {
      String author = uniqueUsername();
      String authorToken = registerAndGetToken(author);
      String slug = createArticle(authorToken, "Popular Article");

      // 3 users favorite the same article
      for (int i = 0; i < 3; i++) {
        String fan = uniqueUsername();
        String fanToken = registerAndGetToken(fan);
        mockMvc.perform(
            post("/api/articles/" + slug + "/favorite")
                .header("Authorization", "Token " + fanToken));
      }

      // Count should be 3
      mockMvc
          .perform(get("/api/articles/" + slug))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.article.favoritesCount", is(3)));
    }

    @Test
    @DisplayName("Article list shows correct favorited/following per authenticated user")
    void listArticlesAuthContext() throws Exception {
      String author = uniqueUsername();
      String authorToken = registerAndGetToken(author);
      String slug = createArticle(authorToken, "Auth Context Art");

      String viewer = uniqueUsername();
      String viewerToken = registerAndGetToken(viewer);

      // Favorite and follow
      mockMvc.perform(
          post("/api/articles/" + slug + "/favorite")
              .header("Authorization", "Token " + viewerToken));
      mockMvc.perform(
          post("/api/profiles/" + author + "/follow")
              .header("Authorization", "Token " + viewerToken));

      // List with auth → favorited=true, following=true
      mockMvc
          .perform(
              get("/api/articles")
                  .param("author", author)
                  .header("Authorization", "Token " + viewerToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles[0].favorited", is(true)))
          .andExpect(jsonPath("$.articles[0].author.following", is(true)));

      // List without auth → favorited=false, following=false
      mockMvc
          .perform(get("/api/articles").param("author", author))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles[0].favorited", is(false)))
          .andExpect(jsonPath("$.articles[0].author.following", is(false)));
    }
  }

  // ─── 8. Error Scenarios ───────────────────────────────

  @Nested
  @DisplayName("Error Scenarios")
  class ErrorScenarios {

    @Test
    @DisplayName("401 — all protected endpoints reject missing token")
    void unauthorizedEndpoints() throws Exception {
      mockMvc.perform(get("/api/user")).andExpect(status().isUnauthorized());
      mockMvc
          .perform(put("/api/user").contentType(MediaType.APPLICATION_JSON).content("{}"))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(post("/api/articles").contentType(MediaType.APPLICATION_JSON).content("{}"))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(put("/api/articles/x").contentType(MediaType.APPLICATION_JSON).content("{}"))
          .andExpect(status().isUnauthorized());
      mockMvc.perform(delete("/api/articles/x")).andExpect(status().isUnauthorized());
      mockMvc.perform(get("/api/articles/feed")).andExpect(status().isUnauthorized());
      mockMvc.perform(post("/api/articles/x/favorite")).andExpect(status().isUnauthorized());
      mockMvc.perform(delete("/api/articles/x/favorite")).andExpect(status().isUnauthorized());
      mockMvc
          .perform(
              post("/api/articles/x/comments")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{}"))
          .andExpect(status().isUnauthorized());
      mockMvc.perform(delete("/api/articles/x/comments/1")).andExpect(status().isUnauthorized());
      mockMvc.perform(post("/api/profiles/x/follow")).andExpect(status().isUnauthorized());
      mockMvc.perform(delete("/api/profiles/x/follow")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("404 — non-existent resources")
    void notFoundResources() throws Exception {
      String username = uniqueUsername();
      String token = registerAndGetToken(username);

      mockMvc.perform(get("/api/articles/no-such-slug")).andExpect(status().isNotFound());
      mockMvc
          .perform(
              post("/api/articles/no-such-slug/comments")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Token " + token)
                  .content(
                      """
                      {"comment":{"body":"test"}}
                      """))
          .andExpect(status().isNotFound());
      mockMvc
          .perform(
              post("/api/articles/no-such-slug/favorite").header("Authorization", "Token " + token))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("403 — article operations by non-author")
    void forbiddenOperations() throws Exception {
      String author = uniqueUsername();
      String authorToken = registerAndGetToken(author);
      String slug = createArticle(authorToken, "Forbidden Art");

      String other = uniqueUsername();
      String otherToken = registerAndGetToken(other);

      // Add a comment by author
      MvcResult result =
          mockMvc
              .perform(
                  post("/api/articles/" + slug + "/comments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Token " + authorToken)
                      .content(
                          """
                          {"comment":{"body":"Author comment"}}
                          """))
              .andReturn();
      int commentId = JsonPath.read(result.getResponse().getContentAsString(), "$.comment.id");

      // Other user can't update article
      mockMvc
          .perform(
              put("/api/articles/" + slug)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization", "Token " + otherToken)
                  .content(
                      """
                      {"article":{"title":"Hacked"}}
                      """))
          .andExpect(status().isForbidden());

      // Other user can't delete article
      mockMvc
          .perform(delete("/api/articles/" + slug).header("Authorization", "Token " + otherToken))
          .andExpect(status().isForbidden());

      // Other user can't delete author's comment
      mockMvc
          .perform(
              delete("/api/articles/" + slug + "/comments/" + commentId)
                  .header("Authorization", "Token " + otherToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("422 — validation errors")
    void validationErrors() throws Exception {
      String username = uniqueUsername();
      registerAndGetToken(username);

      // Duplicate email
      String other = uniqueUsername();
      mockMvc
          .perform(
              post("/api/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"user":{"username":"%s","email":"%s@test.com","password":"password123"}}
                      """
                          .formatted(other, username)))
          .andExpect(status().isUnprocessableEntity());

      // Duplicate username
      String otherEmail = uniqueUsername();
      mockMvc
          .perform(
              post("/api/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"user":{"username":"%s","email":"%s@test.com","password":"password123"}}
                      """
                          .formatted(username, otherEmail)))
          .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Invalid JWT tokens rejected")
    void invalidJwtRejected() throws Exception {
      mockMvc
          .perform(get("/api/user").header("Authorization", "Token invalidtoken"))
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(get("/api/user").header("Authorization", "Bearer validformat"))
          .andExpect(status().isUnauthorized());
    }
  }
}
