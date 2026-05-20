package com.conduit.user.adapter.in.web;

import com.conduit.user.domain.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authenticated user data")
public record UserResponse(
    @Schema(description = "Email address", example = "jake@jake.jake") String email,
    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiJ9...") String token,
    @Schema(description = "Username", example = "jake") String username,
    @Schema(description = "Short bio") String bio,
    @Schema(description = "Avatar image URL") String image) {

  public static UserResponse from(User user, String token) {
    return new UserResponse(
        user.getEmail(), token, user.getUsername(), user.getBio(), user.getImage());
  }
}
