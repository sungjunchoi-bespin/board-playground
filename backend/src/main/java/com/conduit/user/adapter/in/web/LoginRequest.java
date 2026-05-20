package com.conduit.user.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login payload")
public record LoginRequest(
    @NotBlank(message = "can't be blank")
        @Schema(description = "Email address", example = "jake@jake.jake")
        String email,
    @NotBlank(message = "can't be blank") @Schema(description = "Password", example = "jakejake")
        String password) {}
