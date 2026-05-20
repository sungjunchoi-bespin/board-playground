package com.conduit.user.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration payload")
public record RegisterRequest(
    @NotBlank(message = "can't be blank") @Schema(description = "Unique username", example = "jake")
        String username,
    @NotBlank(message = "can't be blank")
        @Email(message = "is invalid")
        @Schema(description = "Email address", example = "jake@jake.jake")
        String email,
    @NotBlank(message = "can't be blank")
        @Size(min = 8, message = "is too short")
        @Schema(description = "Password (min 8 characters)", example = "jakejake")
        String password) {}
