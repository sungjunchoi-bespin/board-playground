package com.conduit.user.adapter.in.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth Flow Integration Tests")
class AuthFlowIntegrationTest {

  @Autowired private MockMvc mockMvc;

  // === Helper methods ===

  private String registerAndGetToken(String username, String email, String password)
      throws Exception {
    String body = """
        {"user":{"username":"%s","email":"%s","password":"%s"}}
        """.formatted(username, email, password);

    MvcResult result =
        mockMvc
            .perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andReturn();

    return JsonPath.read(result.getResponse().getContentAsString(), "$.user.token");
  }

  private String loginAndGetToken(String email, String password) throws Exception {
    String body = """
        {"user":{"email":"%s","password":"%s"}}
        """.formatted(email, password);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andReturn();

    return JsonPath.read(result.getResponse().getContentAsString(), "$.user.token");
  }

  // === Full flow test ===

  @Nested
  @DisplayName("Full Auth Flow: Register -> Login -> Get -> Update")
  class FullFlow {

    @Test
    @DisplayName("should complete entire auth lifecycle")
    void fullAuthLifecycle() throws Exception {
      // 1. Register
      MvcResult registerResult =
          mockMvc
              .perform(
                  post("/api/users")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(
                          """
                  {"user":{"username":"flowuser","email":"flow@test.com","password":"password123"}}
                  """))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.user.email").value("flow@test.com"))
              .andExpect(jsonPath("$.user.username").value("flowuser"))
              .andExpect(jsonPath("$.user.token").isNotEmpty())
              .andReturn();

      String registerToken =
          JsonPath.read(registerResult.getResponse().getContentAsString(), "$.user.token");

      // 2. Login with same credentials
      MvcResult loginResult =
          mockMvc
              .perform(
                  post("/api/users/login")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(
                          """
                  {"user":{"email":"flow@test.com","password":"password123"}}
                  """))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.user.email").value("flow@test.com"))
              .andExpect(jsonPath("$.user.username").value("flowuser"))
              .andExpect(jsonPath("$.user.token").isNotEmpty())
              .andReturn();

      String loginToken =
          JsonPath.read(loginResult.getResponse().getContentAsString(), "$.user.token");

      // 3. Get current user with login token
      mockMvc
          .perform(get("/api/user").header("Authorization", "Token " + loginToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.user.email").value("flow@test.com"))
          .andExpect(jsonPath("$.user.username").value("flowuser"))
          .andExpect(jsonPath("$.user.token").isNotEmpty());

      // 4. Update user profile
      mockMvc
          .perform(
              put("/api/user")
                  .header("Authorization", "Token " + loginToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
              {"user":{"bio":"Updated bio","image":"https://example.com/pic.jpg"}}
              """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.user.bio").value("Updated bio"))
          .andExpect(jsonPath("$.user.image").value("https://example.com/pic.jpg"))
          .andExpect(jsonPath("$.user.email").value("flow@test.com"))
          .andExpect(jsonPath("$.user.username").value("flowuser"));

      // 5. Verify update persisted via GET
      mockMvc
          .perform(get("/api/user").header("Authorization", "Token " + loginToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.user.bio").value("Updated bio"))
          .andExpect(jsonPath("$.user.image").value("https://example.com/pic.jpg"));
    }
  }

  // === Error response format validation ===

  @Nested
  @DisplayName("Error Response Format: {\"errors\":{\"field\":[\"msg\"]}}")
  class ErrorFormat {

    @Test
    @DisplayName("duplicate email should return errors object with field array")
    void duplicateEmailErrorFormat() throws Exception {
      registerAndGetToken("first", "taken@test.com", "password123");

      mockMvc
          .perform(
              post("/api/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
              {"user":{"username":"second","email":"taken@test.com","password":"password123"}}
              """))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.errors").isMap())
          .andExpect(jsonPath("$.errors.body").isArray());
    }

    @Test
    @DisplayName("duplicate username should return errors object")
    void duplicateUsernameErrorFormat() throws Exception {
      registerAndGetToken("takenname", "first@test.com", "password123");

      mockMvc
          .perform(
              post("/api/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
              {"user":{"username":"takenname","email":"second@test.com","password":"password123"}}
              """))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.errors").isMap())
          .andExpect(jsonPath("$.errors.body").isArray());
    }

    @Test
    @DisplayName("invalid login should return errors object")
    void invalidLoginErrorFormat() throws Exception {
      mockMvc
          .perform(
              post("/api/users/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
              {"user":{"email":"noone@test.com","password":"password123"}}
              """))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.errors").isMap())
          .andExpect(jsonPath("$.errors.body").isArray());
    }
  }

  // === JWT security tests ===

  @Nested
  @DisplayName("JWT Security")
  class JwtSecurity {

    @Test
    @DisplayName("GET /api/user without JWT should return 401")
    void missingJwt() throws Exception {
      mockMvc.perform(get("/api/user")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/user with tampered JWT should return 401")
    void tamperedJwt() throws Exception {
      String tamperedToken = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjk5OX0.INVALID_SIGNATURE";

      mockMvc
          .perform(get("/api/user").header("Authorization", "Token " + tamperedToken))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/user with random string as JWT should return 401")
    void garbageJwt() throws Exception {
      mockMvc
          .perform(get("/api/user").header("Authorization", "Token not-a-real-jwt"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/user without JWT should return 401")
    void updateWithoutJwt() throws Exception {
      mockMvc
          .perform(
              put("/api/user")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("""
              {"user":{"bio":"hacked"}}
              """))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authorization header with wrong prefix should return 401")
    void wrongPrefix() throws Exception {
      String token = registerAndGetToken("bearer", "bearer@test.com", "password123");

      mockMvc
          .perform(get("/api/user").header("Authorization", "Bearer " + token))
          .andExpect(status().isUnauthorized());
    }
  }

  // === Content-Type validation (R-N-01) ===

  @Nested
  @DisplayName("Content-Type Validation (R-N-01)")
  class ContentType {

    @Test
    @DisplayName("register response should have application/json content type")
    void registerContentType() throws Exception {
      mockMvc
          .perform(
              post("/api/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
              {"user":{"username":"ctuser","email":"ct@test.com","password":"password123"}}
              """))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("login response should have application/json content type")
    void loginContentType() throws Exception {
      registerAndGetToken("ctlogin", "ctlogin@test.com", "password123");

      mockMvc
          .perform(
              post("/api/users/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
              {"user":{"email":"ctlogin@test.com","password":"password123"}}
              """))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("get current user response should have application/json content type")
    void getCurrentUserContentType() throws Exception {
      String token = registerAndGetToken("ctget", "ctget@test.com", "password123");

      mockMvc
          .perform(get("/api/user").header("Authorization", "Token " + token))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
  }

  // === Update user edge cases ===

  @Nested
  @DisplayName("Update User Edge Cases")
  class UpdateEdgeCases {

    @Test
    @DisplayName("should update email to a new unique email")
    void updateEmail() throws Exception {
      String token = registerAndGetToken("emailuser", "old@test.com", "password123");

      mockMvc
          .perform(
              put("/api/user")
                  .header("Authorization", "Token " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("""
              {"user":{"email":"new@test.com"}}
              """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.user.email").value("new@test.com"));
    }

    @Test
    @DisplayName("should return 422 when updating to duplicate email")
    void updateDuplicateEmail() throws Exception {
      registerAndGetToken("other", "existing@test.com", "password123");
      String token = registerAndGetToken("updater", "updater@test.com", "password123");

      mockMvc
          .perform(
              put("/api/user")
                  .header("Authorization", "Token " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
              {"user":{"email":"existing@test.com"}}
              """))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    @DisplayName("should update password and login with new password")
    void updatePassword() throws Exception {
      String token = registerAndGetToken("pwuser", "pw@test.com", "oldpassword1");

      mockMvc
          .perform(
              put("/api/user")
                  .header("Authorization", "Token " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("""
              {"user":{"password":"newpassword1"}}
              """))
          .andExpect(status().isOk());

      // Login with new password should succeed
      loginAndGetToken("pw@test.com", "newpassword1");

      // Login with old password should fail
      mockMvc
          .perform(
              post("/api/users/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
              {"user":{"email":"pw@test.com","password":"oldpassword1"}}
              """))
          .andExpect(status().isUnauthorized());
    }
  }
}
