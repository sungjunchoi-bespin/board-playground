package com.conduit.profile.domain.port.in;

import com.conduit.profile.domain.model.Profile;

public interface FollowUserUseCase {

    Profile follow(String username, Long currentUserId);
}
