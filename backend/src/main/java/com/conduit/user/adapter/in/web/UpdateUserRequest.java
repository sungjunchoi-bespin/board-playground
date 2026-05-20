package com.conduit.user.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User profile update payload (all fields optional)")
public record UpdateUserRequest(
    @Schema(description = "New email address", example = "jake@jake.jake") String email,
    @Schema(description = "New username", example = "jake") String username,
    @Schema(description = "New password", example = "newpassword") String password,
    @Schema(description = "Short bio", example = "I like to skateboard") String bio,
    @Schema(description = "Avatar image URL", example = "https://i.stack.imgur.com/xHWG8.jpg")
        String image) {}
