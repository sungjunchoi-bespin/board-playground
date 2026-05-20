package com.conduit.user.adapter.in.web;

import com.conduit.shared.security.JwtTokenProvider;
import com.conduit.user.domain.model.User;
import com.conduit.user.domain.port.in.GetCurrentUserUseCase;
import com.conduit.user.domain.port.in.LoginUserUseCase;
import com.conduit.user.domain.port.in.RegisterUserUseCase;
import com.conduit.user.domain.port.in.UpdateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User and Authentication", description = "Register, login, and manage user profile")
@RestController
@RequestMapping("/api")
public class UserController {

  private final RegisterUserUseCase registerUserUseCase;
  private final LoginUserUseCase loginUserUseCase;
  private final GetCurrentUserUseCase getCurrentUserUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final JwtTokenProvider jwtTokenProvider;

  public UserController(
      RegisterUserUseCase registerUserUseCase,
      LoginUserUseCase loginUserUseCase,
      GetCurrentUserUseCase getCurrentUserUseCase,
      UpdateUserUseCase updateUserUseCase,
      JwtTokenProvider jwtTokenProvider) {
    this.registerUserUseCase = registerUserUseCase;
    this.loginUserUseCase = loginUserUseCase;
    this.getCurrentUserUseCase = getCurrentUserUseCase;
    this.updateUserUseCase = updateUserUseCase;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Operation(summary = "Register a new user", description = "Create a new user account")
  @ApiResponse(responseCode = "200", description = "User registered successfully")
  @ApiResponse(responseCode = "422", description = "Validation error or duplicate email/username")
  @PostMapping("/users")
  public ResponseEntity<Map<String, UserResponse>> register(
      @Valid @RequestBody Map<String, RegisterRequest> body) {
    RegisterRequest req = body.get("user");
    User user = registerUserUseCase.register(req.username(), req.email(), req.password());
    String token = jwtTokenProvider.generateToken(user.getId());
    return ResponseEntity.ok(Map.of("user", UserResponse.from(user, token)));
  }

  @Operation(summary = "Login", description = "Login with email and password")
  @ApiResponse(responseCode = "200", description = "Login successful")
  @ApiResponse(responseCode = "401", description = "Invalid email or password")
  @PostMapping("/users/login")
  public ResponseEntity<Map<String, UserResponse>> login(
      @Valid @RequestBody Map<String, LoginRequest> body) {
    LoginRequest req = body.get("user");
    User user = loginUserUseCase.login(req.email(), req.password());
    String token = jwtTokenProvider.generateToken(user.getId());
    return ResponseEntity.ok(Map.of("user", UserResponse.from(user, token)));
  }

  @Operation(
      summary = "Get current user",
      description = "Get the currently logged-in user. Auth required.",
      security = @SecurityRequirement(name = "Token"))
  @ApiResponse(responseCode = "200", description = "Current user data")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @GetMapping("/user")
  public ResponseEntity<Map<String, UserResponse>> getCurrentUser(Authentication authentication) {
    Long userId = (Long) authentication.getPrincipal();
    User user = getCurrentUserUseCase.getCurrentUser(userId);
    String token = jwtTokenProvider.generateToken(user.getId());
    return ResponseEntity.ok(Map.of("user", UserResponse.from(user, token)));
  }

  @Operation(
      summary = "Update current user",
      description = "Update user profile fields. Auth required.",
      security = @SecurityRequirement(name = "Token"))
  @ApiResponse(responseCode = "200", description = "User updated")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "422", description = "Validation error")
  @PutMapping("/user")
  public ResponseEntity<Map<String, UserResponse>> updateUser(
      Authentication authentication, @RequestBody Map<String, UpdateUserRequest> body) {
    Long userId = (Long) authentication.getPrincipal();
    UpdateUserRequest req = body.get("user");
    User user =
        updateUserUseCase.update(
            userId, req.email(), req.username(), req.password(), req.bio(), req.image());
    String token = jwtTokenProvider.generateToken(user.getId());
    return ResponseEntity.ok(Map.of("user", UserResponse.from(user, token)));
  }
}
