package com.conduit.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.conduit.shared.exception.ApiException;
import com.conduit.user.domain.model.User;
import com.conduit.user.domain.port.out.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, passwordEncoder);
  }

  @Nested
  @DisplayName("register")
  class Register {

    @Test
    @DisplayName("should register a new user successfully")
    void success() {
      when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
      when(userRepository.existsByUsername("testuser")).thenReturn(false);
      when(passwordEncoder.encode("password123")).thenReturn("hashed");
      when(userRepository.save(any(User.class)))
          .thenAnswer(
              inv -> {
                User u = inv.getArgument(0);
                return new User(
                    1L, u.getEmail(), u.getUsername(), u.getPassword(), u.getBio(), u.getImage());
              });

      User result = userService.register("testuser", "test@test.com", "password123");

      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getEmail()).isEqualTo("test@test.com");
      assertThat(result.getUsername()).isEqualTo("testuser");
      assertThat(result.getPassword()).isEqualTo("hashed");
    }

    @Test
    @DisplayName("should throw when email is already taken")
    void duplicateEmail() {
      when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

      assertThatThrownBy(() -> userService.register("user", "taken@test.com", "password123"))
          .isInstanceOf(ApiException.ValidationException.class)
          .hasMessageContaining("email has already been taken");
    }

    @Test
    @DisplayName("should throw when username is already taken")
    void duplicateUsername() {
      when(userRepository.existsByEmail(anyString())).thenReturn(false);
      when(userRepository.existsByUsername("taken")).thenReturn(true);

      assertThatThrownBy(() -> userService.register("taken", "new@test.com", "password123"))
          .isInstanceOf(ApiException.ValidationException.class)
          .hasMessageContaining("username has already been taken");
    }
  }

  @Nested
  @DisplayName("login")
  class Login {

    @Test
    @DisplayName("should login with valid credentials")
    void success() {
      User user = new User(1L, "test@test.com", "testuser", "hashed", null, null);
      when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
      when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);

      User result = userService.login("test@test.com", "password123");

      assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should throw when email not found")
    void emailNotFound() {
      when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.login("wrong@test.com", "password123"))
          .isInstanceOf(ApiException.UnauthorizedException.class);
    }

    @Test
    @DisplayName("should throw when password is wrong")
    void wrongPassword() {
      User user = new User(1L, "test@test.com", "testuser", "hashed", null, null);
      when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
      when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

      assertThatThrownBy(() -> userService.login("test@test.com", "wrong"))
          .isInstanceOf(ApiException.UnauthorizedException.class);
    }
  }

  @Nested
  @DisplayName("getCurrentUser")
  class GetCurrentUser {

    @Test
    @DisplayName("should return current user")
    void success() {
      User user = new User(1L, "test@test.com", "testuser", "hashed", "bio", null);
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));

      User result = userService.getCurrentUser(1L);

      assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("should throw when user not found")
    void notFound() {
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.getCurrentUser(999L))
          .isInstanceOf(ApiException.NotFoundException.class);
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("should update user fields")
    void success() {
      User existing = new User(1L, "old@test.com", "olduser", "hashed", null, null);
      when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
      when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
      when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

      User result = userService.update(1L, "new@test.com", null, null, "new bio", null);

      assertThat(result.getEmail()).isEqualTo("new@test.com");
      assertThat(result.getBio()).isEqualTo("new bio");
      assertThat(result.getUsername()).isEqualTo("olduser");
    }

    @Test
    @DisplayName("should throw when new email is taken by another user")
    void duplicateEmail() {
      User existing = new User(1L, "old@test.com", "user", "hashed", null, null);
      when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
      when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

      assertThatThrownBy(() -> userService.update(1L, "taken@test.com", null, null, null, null))
          .isInstanceOf(ApiException.ValidationException.class)
          .hasMessageContaining("email has already been taken");
    }
  }
}
