package com.conduit.user.adapter.in.web;

public record UpdateUserRequest(
    String email,
    String username,
    String password,
    String bio,
    String image
) {}
