package com.conduit.user.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "can't be blank") String username,
    @NotBlank(message = "can't be blank") @Email(message = "is invalid") String email,
    @NotBlank(message = "can't be blank") @Size(min = 8, message = "is too short") String password
) {}
