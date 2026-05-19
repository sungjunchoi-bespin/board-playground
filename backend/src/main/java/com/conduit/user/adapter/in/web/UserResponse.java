package com.conduit.user.adapter.in.web;

import com.conduit.user.domain.model.User;

public record UserResponse(
    String email,
    String token,
    String username,
    String bio,
    String image
) {

  public static UserResponse from(User user, String token) {
    return new UserResponse(user.getEmail(), token, user.getUsername(), user.getBio(),
        user.getImage());
  }
}
