package com.conduit.user.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "can't be blank") String email,
    @NotBlank(message = "can't be blank") String password
) {}
