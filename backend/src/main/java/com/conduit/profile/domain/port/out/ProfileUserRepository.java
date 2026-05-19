package com.conduit.profile.domain.port.out;

import com.conduit.profile.domain.model.Profile;

import java.util.Optional;

public interface ProfileUserRepository {

    Optional<Profile> findByUsername(String username, Long currentUserId);

    boolean existsByUsername(String username);

    Long findUserIdByUsername(String username);
}
