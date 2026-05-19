package com.conduit.user.adapter.in.web;

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
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtTokenProvider jwtTokenProvider;

  private String registerUser(String username, String email, String password) throws Exception {
    String body = """
        {"user":{"username":"%s","email":"%s","password":"%s"}}
        """.formatted(username, email, password);

    MvcResult result = mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andReturn();

    return result.getResponse().getContentAsString();
  }

  @Nested
  @DisplayName("POST /api/users")
  class Registration {

    @Test
    @DisplayName("should register a new user")
    void success() throws Exception {
      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"user":{"username":"jacob","email":"jake@jake.com","password":"jakejake1"}}
                  """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.user.email").value("jake@jake.com"))
          .andExpect(jsonPath("$.user.username").value("jacob"))
          .andExpect(jsonPath("$.user.token").isNotEmpty())
          .andExpect(jsonPath("$.user.bio").doesNotExist())
          .andExpect(jsonPath("$.user.image").doesNotExist());
    }

    @Test
    @DisplayName("should return 422 for duplicate email")
    void duplicateEmail() throws Exception {
      registerUser("first", "dupe@test.com", "password123");

      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"user":{"username":"second","email":"dupe@test.com","password":"password123"}}
                  """))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("should return 422 for duplicate username")
    void duplicateUsername() throws Exception {
      registerUser("taken", "first@test.com", "password123");

      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"user":{"username":"taken","email":"second@test.com","password":"password123"}}
                  """))
          .andExpect(status().isUnprocessableEntity());
    }
  }

  @Nested
  @DisplayName("POST /api/users/login")
  class Login {

    @Test
    @DisplayName("should login with valid credentials")
    void success() throws Exception {
      registerUser("jacob", "jake@jake.com", "jakejake1");

      mockMvc.perform(post("/api/users/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"user":{"email":"jake@jake.com","password":"jakejake1"}}
                  """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.user.email").value("jake@jake.com"))
          .andExpect(jsonPath("$.user.username").value("jacob"))
          .andExpect(jsonPath("$.user.token").isNotEmpty());
    }

    @Test
    @DisplayName("should return 401 for wrong password")
    void wrongPassword() throws Exception {
      registerUser("jacob", "jake@jake.com", "jakejake1");

      mockMvc.perform(post("/api/users/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"user":{"email":"jake@jake.com","password":"wrongwrong"}}
                  """))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return 401 for unknown email")
    void unknownEmail() throws Exception {
      mockMvc.perform(post("/api/users/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"user":{"email":"noone@test.com","password":"password123"}}
                  """))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/user")
  class GetCurrentUser {

    @Test
    @DisplayName("should return current user with valid JWT")
    void success() throws Exception {
      registerUser("jacob", "jake@jake.com", "jakejake1");

      // Login to get a valid token
      MvcResult loginResult = mockMvc.perform(post("/api/users/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"user":{"email":"jake@jake.com","password":"jakejake1"}}
                  """))
          .andExpect(status().isOk())
          .andReturn();

      String token = com.jayway.jsonpath.JsonPath
          .read(loginResult.getResponse().getContentAsString(), "$.user.token");

      mockMvc.perform(get("/api/user")
              .header("Authorization", "Token " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.user.email").value("jake@jake.com"))
          .andExpect(jsonPath("$.user.username").value("jacob"));
    }

    @Test
    @DisplayName("should return 401 without JWT")
    void unauthorized() throws Exception {
      mockMvc.perform(get("/api/user"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("PUT /api/user")
  class UpdateUser {

    @Test
    @DisplayName("should update user bio and image")
    void success() throws Exception {
      registerUser("jacob", "jake@jake.com", "jakejake1");

      MvcResult loginResult = mockMvc.perform(post("/api/users/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"user":{"email":"jake@jake.com","password":"jakejake1"}}
                  """))
          .andExpect(status().isOk())
          .andReturn();

      String token = com.jayway.jsonpath.JsonPath
          .read(loginResult.getResponse().getContentAsString(), "$.user.token");

      mockMvc.perform(put("/api/user")
              .header("Authorization", "Token " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"user":{"bio":"I like to code","image":"https://i.imgur.com/test.jpg"}}
                  """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.user.bio").value("I like to code"))
          .andExpect(jsonPath("$.user.image").value("https://i.imgur.com/test.jpg"))
          .andExpect(jsonPath("$.user.email").value("jake@jake.com"));
    }
  }
}
