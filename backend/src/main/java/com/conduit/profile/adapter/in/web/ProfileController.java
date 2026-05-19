package com.conduit.profile.adapter.in.web;

import com.conduit.profile.domain.model.Profile;
import com.conduit.profile.domain.port.in.FollowUserUseCase;
import com.conduit.profile.domain.port.in.GetProfileUseCase;
import com.conduit.profile.domain.port.in.UnfollowUserUseCase;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final GetProfileUseCase getProfileUseCase;
    private final FollowUserUseCase followUserUseCase;
    private final UnfollowUserUseCase unfollowUserUseCase;

    public ProfileController(GetProfileUseCase getProfileUseCase,
                              FollowUserUseCase followUserUseCase,
                              UnfollowUserUseCase unfollowUserUseCase) {
        this.getProfileUseCase = getProfileUseCase;
        this.followUserUseCase = followUserUseCase;
        this.unfollowUserUseCase = unfollowUserUseCase;
    }

    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getProfile(
            @PathVariable String username,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        Profile profile = getProfileUseCase.getProfile(username, currentUserId);
        return ResponseEntity.ok(Map.of("profile", toResponse(profile)));
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<Map<String, Object>> follow(
            @PathVariable String username,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        Profile profile = followUserUseCase.follow(username, currentUserId);
        return ResponseEntity.ok(Map.of("profile", toResponse(profile)));
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<Map<String, Object>> unfollow(
            @PathVariable String username,
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
                "username", profile.username(),
                "bio", profile.bio() != null ? profile.bio() : "",
                "image", profile.image() != null ? profile.image() : "",
                "following", profile.following()
        );
    }
}
