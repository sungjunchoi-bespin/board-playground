package com.conduit.profile.adapter.in.web;

import com.conduit.profile.domain.model.Profile;
import com.conduit.profile.domain.port.in.FollowUserUseCase;
import com.conduit.profile.domain.port.in.GetProfileUseCase;
import com.conduit.profile.domain.port.in.UnfollowUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profile", description = "User profiles and follow/unfollow")
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

  private final GetProfileUseCase getProfileUseCase;
  private final FollowUserUseCase followUserUseCase;
  private final UnfollowUserUseCase unfollowUserUseCase;

  public ProfileController(
      GetProfileUseCase getProfileUseCase,
      FollowUserUseCase followUserUseCase,
      UnfollowUserUseCase unfollowUserUseCase) {
    this.getProfileUseCase = getProfileUseCase;
    this.followUserUseCase = followUserUseCase;
    this.unfollowUserUseCase = unfollowUserUseCase;
  }

  @Operation(
      summary = "Get a profile",
      description = "Get a user profile by username. No auth required.")
  @ApiResponse(responseCode = "200", description = "Profile data")
  @ApiResponse(responseCode = "404", description = "User not found")
  @GetMapping("/{username}")
  public ResponseEntity<Map<String, Object>> getProfile(
      @Parameter(description = "Username of the profile") @PathVariable String username,
      Authentication authentication) {
    Long currentUserId = getCurrentUserId(authentication);
    Profile profile = getProfileUseCase.getProfile(username, currentUserId);
    return ResponseEntity.ok(Map.of("profile", toResponse(profile)));
  }

  @Operation(
      summary = "Follow a user",
      description = "Follow a user by username. Auth required.",
      security = @SecurityRequirement(name = "Token"))
  @ApiResponse(responseCode = "200", description = "User followed")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "404", description = "User not found")
  @PostMapping("/{username}/follow")
  public ResponseEntity<Map<String, Object>> follow(
      @Parameter(description = "Username to follow") @PathVariable String username,
      Authentication authentication) {
    Long currentUserId = getCurrentUserId(authentication);
    Profile profile = followUserUseCase.follow(username, currentUserId);
    return ResponseEntity.ok(Map.of("profile", toResponse(profile)));
  }

  @Operation(
      summary = "Unfollow a user",
      description = "Unfollow a user by username. Auth required.",
      security = @SecurityRequirement(name = "Token"))
  @ApiResponse(responseCode = "200", description = "User unfollowed")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "404", description = "User not found")
  @DeleteMapping("/{username}/follow")
  public ResponseEntity<Map<String, Object>> unfollow(
      @Parameter(description = "Username to unfollow") @PathVariable String username,
      Authentication authentication) {
    Long currentUserId = getCurrentUserId(authentication);
    Profile profile = unfollowUserUseCase.unfollow(username, currentUserId);
    return ResponseEntity.ok(Map.of("profile", toResponse(profile)));
  }

  private Long getCurrentUserId(Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof Long userId) {
      return userId;
    }
    return null;
  }

  private Map<String, Object> toResponse(Profile profile) {
    return Map.of(
        "username",
        profile.username(),
        "bio",
        profile.bio() != null ? profile.bio() : "",
        "image",
        profile.image() != null ? profile.image() : "",
        "following",
        profile.following());
  }
}
