package com.conduit.article.adapter.in.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conduit.article.adapter.out.persistence.ArticleJpaRepository;
import com.conduit.article.adapter.out.persistence.FavoriteJpaEntity;
import com.conduit.article.adapter.out.persistence.FavoriteJpaRepository;
import com.conduit.article.adapter.out.persistence.FollowJpaEntity;
import com.conduit.article.adapter.out.persistence.FollowJpaRepository;
import com.conduit.shared.security.JwtTokenProvider;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Article List & Feed Integration Tests")
class ArticleListFeedIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtTokenProvider jwtTokenProvider;
  @Autowired private FollowJpaRepository followJpaRepository;
  @Autowired private FavoriteJpaRepository favoriteJpaRepository;
  @Autowired private ArticleJpaRepository articleJpaRepository;

  // === Helper types and methods ===

  private record UserInfo(String token, Long userId) {}

  private UserInfo registerUser(String username, String email, String password) throws Exception {
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

    String token = JsonPath.read(result.getResponse().getContentAsString(), "$.user.token");
    Long userId = jwtTokenProvider.extractUserId(token);
    return new UserInfo(token, userId);
  }

  private String createArticle(
      String token, String title, String description, String articleBody, String tagsJson)
      throws Exception {
    String requestBody =
        """
        {"article":{"title":"%s","description":"%s","body":"%s","tagList":[%s]}}
        """
            .formatted(title, description, articleBody, tagsJson);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/articles")
                    .header("Authorization", "Token " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isOk())
            .andReturn();

    return JsonPath.read(result.getResponse().getContentAsString(), "$.article.slug");
  }

  private Long findArticleIdBySlug(String slug) {
    return articleJpaRepository
        .findBySlug(slug)
        .orElseThrow(() -> new IllegalStateException("Article not found: " + slug))
        .getId();
  }

  // === GET /api/articles (List Articles) ===

  @Nested
  @DisplayName("GET /api/articles")
  class ListArticles {

    @Test
    @DisplayName("should return empty list when no articles exist")
    void emptyList() throws Exception {
      mockMvc
          .perform(get("/api/articles"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles").isArray())
          .andExpect(jsonPath("$.articles", hasSize(0)))
          .andExpect(jsonPath("$.articlesCount").value(0));
    }

    @Test
    @DisplayName("should return articles list with count")
    void returnsList() throws Exception {
      UserInfo user = registerUser("jacob", "jake@jake.com", "jakejake1");
      createArticle(user.token(), "First Article", "Desc 1", "Body 1", "");
      createArticle(user.token(), "Second Article", "Desc 2", "Body 2", "");

      mockMvc
          .perform(get("/api/articles"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles", hasSize(2)))
          .andExpect(jsonPath("$.articlesCount").value(2));
    }

    @Test
    @DisplayName("should order articles by most recent first")
    void mostRecentFirst() throws Exception {
      UserInfo user = registerUser("jacob", "jake@jake.com", "jakejake1");
      createArticle(user.token(), "First Article", "Desc 1", "Body 1", "");
      createArticle(user.token(), "Second Article", "Desc 2", "Body 2", "");

      mockMvc
          .perform(get("/api/articles"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles[0].title").value("Second Article"))
          .andExpect(jsonPath("$.articles[1].title").value("First Article"));
    }

    @Test
    @DisplayName("should filter articles by tag")
    void filterByTag() throws Exception {
      UserInfo user = registerUser("jacob", "jake@jake.com", "jakejake1");
      createArticle(user.token(), "Tagged Article", "Desc", "Body", "\"dragons\"");
      createArticle(user.token(), "No Tag Article", "Desc", "Body", "");

      mockMvc
          .perform(get("/api/articles").param("tag", "dragons"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles", hasSize(1)))
          .andExpect(jsonPath("$.articlesCount").value(1))
          .andExpect(jsonPath("$.articles[0].title").value("Tagged Article"));
    }

    @Test
    @DisplayName("should filter articles by author username")
    void filterByAuthor() throws Exception {
      UserInfo user1 = registerUser("author1", "author1@test.com", "password123");
      UserInfo user2 = registerUser("author2", "author2@test.com", "password123");
      createArticle(user1.token(), "Author1 Article", "Desc", "Body", "");
      createArticle(user2.token(), "Author2 Article", "Desc", "Body", "");

      mockMvc
          .perform(get("/api/articles").param("author", "author1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles", hasSize(1)))
          .andExpect(jsonPath("$.articlesCount").value(1))
          .andExpect(jsonPath("$.articles[0].author.username").value("author1"));
    }

    @Test
    @DisplayName("should filter articles favorited by username")
    void filterByFavorited() throws Exception {
      UserInfo author = registerUser("theauthor", "author@test.com", "password123");
      UserInfo favUser = registerUser("favuser", "fav@test.com", "password123");

      String favSlug =
          createArticle(author.token(), "Favorited Article", "Desc", "Body", "");
      createArticle(author.token(), "Normal Article", "Desc", "Body", "");

      Long favArticleId = findArticleIdBySlug(favSlug);
      favoriteJpaRepository.saveAndFlush(new FavoriteJpaEntity(favUser.userId(), favArticleId));

      mockMvc
          .perform(get("/api/articles").param("favorited", "favuser"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles", hasSize(1)))
          .andExpect(jsonPath("$.articlesCount").value(1))
          .andExpect(jsonPath("$.articles[0].title").value("Favorited Article"));
    }

    @Test
    @DisplayName("should support pagination with limit and offset")
    void pagination() throws Exception {
      UserInfo user = registerUser("jacob", "jake@jake.com", "jakejake1");
      createArticle(user.token(), "Article 1", "Desc", "Body", "");
      createArticle(user.token(), "Article 2", "Desc", "Body", "");
      createArticle(user.token(), "Article 3", "Desc", "Body", "");

      mockMvc
          .perform(get("/api/articles").param("limit", "2").param("offset", "0"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles", hasSize(2)))
          .andExpect(jsonPath("$.articlesCount").value(3))
          .andExpect(jsonPath("$.articles[0].title").value("Article 3"))
          .andExpect(jsonPath("$.articles[1].title").value("Article 2"));

      mockMvc
          .perform(get("/api/articles").param("limit", "2").param("offset", "2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles", hasSize(1)))
          .andExpect(jsonPath("$.articlesCount").value(3))
          .andExpect(jsonPath("$.articles[0].title").value("Article 1"));
    }

    @Test
    @DisplayName("should include all expected fields in response format")
    void responseFormat() throws Exception {
      UserInfo user = registerUser("jacob", "jake@jake.com", "jakejake1");
      createArticle(user.token(), "Test Article", "Desc", "Body", "\"tag1\"");

      mockMvc
          .perform(get("/api/articles"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles[0].slug").isNotEmpty())
          .andExpect(jsonPath("$.articles[0].title").value("Test Article"))
          .andExpect(jsonPath("$.articles[0].description").value("Desc"))
          .andExpect(jsonPath("$.articles[0].tagList[0]").value("tag1"))
          .andExpect(jsonPath("$.articles[0].createdAt").isNotEmpty())
          .andExpect(jsonPath("$.articles[0].updatedAt").isNotEmpty())
          .andExpect(jsonPath("$.articles[0].favorited").value(false))
          .andExpect(jsonPath("$.articles[0].favoritesCount").value(0))
          .andExpect(jsonPath("$.articles[0].author.username").value("jacob"))
          .andExpect(jsonPath("$.articles[0].author.following").value(false));
    }

    @Test
    @DisplayName("should show favorited=true when authenticated user has favorited the article")
    void favoritedWhenAuthenticated() throws Exception {
      UserInfo author = registerUser("author", "author@test.com", "password123");
      UserInfo reader = registerUser("reader", "reader@test.com", "password123");
      String slug = createArticle(author.token(), "Great Article", "Desc", "Body", "");

      Long articleId = findArticleIdBySlug(slug);
      favoriteJpaRepository.saveAndFlush(new FavoriteJpaEntity(reader.userId(), articleId));

      mockMvc
          .perform(get("/api/articles").header("Authorization", "Token " + reader.token()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles[0].favorited").value(true));
    }

    @Test
    @DisplayName("should show following=true when authenticated user follows the author")
    void followingWhenAuthenticated() throws Exception {
      UserInfo author = registerUser("author", "author@test.com", "password123");
      UserInfo reader = registerUser("reader", "reader@test.com", "password123");
      createArticle(author.token(), "Great Article", "Desc", "Body", "");

      followJpaRepository.saveAndFlush(
          new FollowJpaEntity(reader.userId(), author.userId()));

      mockMvc
          .perform(get("/api/articles").header("Authorization", "Token " + reader.token()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles[0].author.following").value(true));
    }

    @Test
    @DisplayName("should show favorited=false and following=false without authentication")
    void noAuthDefaults() throws Exception {
      UserInfo author = registerUser("author", "author@test.com", "password123");
      UserInfo reader = registerUser("reader", "reader@test.com", "password123");
      String slug = createArticle(author.token(), "Great Article", "Desc", "Body", "");

      Long articleId = findArticleIdBySlug(slug);
      followJpaRepository.saveAndFlush(
          new FollowJpaEntity(reader.userId(), author.userId()));
      favoriteJpaRepository.saveAndFlush(new FavoriteJpaEntity(reader.userId(), articleId));

      mockMvc
          .perform(get("/api/articles"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles[0].favorited").value(false))
          .andExpect(jsonPath("$.articles[0].author.following").value(false));
    }
  }

  // === GET /api/articles/feed ===

  @Nested
  @DisplayName("GET /api/articles/feed")
  class FeedArticles {

    @Test
    @DisplayName("should return 401 without authentication")
    void unauthorized() throws Exception {
      mockMvc.perform(get("/api/articles/feed")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return empty feed when user follows nobody")
    void emptyFeed() throws Exception {
      UserInfo user = registerUser("jacob", "jake@jake.com", "jakejake1");

      mockMvc
          .perform(
              get("/api/articles/feed").header("Authorization", "Token " + user.token()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles", hasSize(0)))
          .andExpect(jsonPath("$.articlesCount").value(0));
    }

    @Test
    @DisplayName("should return articles from followed users only")
    void followedUserArticles() throws Exception {
      UserInfo follower = registerUser("follower", "follower@test.com", "password123");
      UserInfo followed = registerUser("followed", "followed@test.com", "password123");
      UserInfo stranger = registerUser("stranger", "stranger@test.com", "password123");

      createArticle(followed.token(), "Followed Article", "Desc", "Body", "");
      createArticle(stranger.token(), "Stranger Article", "Desc", "Body", "");

      followJpaRepository.saveAndFlush(
          new FollowJpaEntity(follower.userId(), followed.userId()));

      mockMvc
          .perform(
              get("/api/articles/feed")
                  .header("Authorization", "Token " + follower.token()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles", hasSize(1)))
          .andExpect(jsonPath("$.articlesCount").value(1))
          .andExpect(jsonPath("$.articles[0].title").value("Followed Article"))
          .andExpect(jsonPath("$.articles[0].author.username").value("followed"));
    }

    @Test
    @DisplayName("should support pagination for feed")
    void feedPagination() throws Exception {
      UserInfo follower = registerUser("follower", "follower@test.com", "password123");
      UserInfo followed = registerUser("followed", "followed@test.com", "password123");

      createArticle(followed.token(), "Article 1", "Desc", "Body", "");
      createArticle(followed.token(), "Article 2", "Desc", "Body", "");
      createArticle(followed.token(), "Article 3", "Desc", "Body", "");

      followJpaRepository.saveAndFlush(
          new FollowJpaEntity(follower.userId(), followed.userId()));

      mockMvc
          .perform(
              get("/api/articles/feed")
                  .header("Authorization", "Token " + follower.token())
                  .param("limit", "2")
                  .param("offset", "0"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles", hasSize(2)))
          .andExpect(jsonPath("$.articlesCount").value(3));
    }

    @Test
    @DisplayName("should include proper response fields in feed")
    void feedResponseFormat() throws Exception {
      UserInfo follower = registerUser("follower", "follower@test.com", "password123");
      UserInfo followed = registerUser("followed", "followed@test.com", "password123");

      createArticle(followed.token(), "Feed Article", "Desc", "Body", "\"tech\"");

      followJpaRepository.saveAndFlush(
          new FollowJpaEntity(follower.userId(), followed.userId()));

      mockMvc
          .perform(
              get("/api/articles/feed")
                  .header("Authorization", "Token " + follower.token()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles[0].slug").isNotEmpty())
          .andExpect(jsonPath("$.articles[0].title").value("Feed Article"))
          .andExpect(jsonPath("$.articles[0].tagList[0]").value("tech"))
          .andExpect(jsonPath("$.articles[0].favorited").value(false))
          .andExpect(jsonPath("$.articles[0].favoritesCount").value(0))
          .andExpect(jsonPath("$.articles[0].author.username").value("followed"))
          .andExpect(jsonPath("$.articles[0].author.following").value(true));
    }

    @Test
    @DisplayName("should show favorited=true in feed when user has favorited the article")
    void feedFavoritedArticle() throws Exception {
      UserInfo follower = registerUser("follower", "follower@test.com", "password123");
      UserInfo followed = registerUser("followed", "followed@test.com", "password123");

      String slug = createArticle(followed.token(), "Feed Article", "Desc", "Body", "");

      followJpaRepository.saveAndFlush(
          new FollowJpaEntity(follower.userId(), followed.userId()));

      Long articleId = findArticleIdBySlug(slug);
      favoriteJpaRepository.saveAndFlush(new FavoriteJpaEntity(follower.userId(), articleId));

      mockMvc
          .perform(
              get("/api/articles/feed")
                  .header("Authorization", "Token " + follower.token()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.articles[0].favorited").value(true))
          .andExpect(jsonPath("$.articles[0].author.following").value(true));
    }
  }
}
