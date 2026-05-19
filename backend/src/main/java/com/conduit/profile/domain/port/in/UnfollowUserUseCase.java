package com.conduit.profile.domain.port.in;

import com.conduit.profile.domain.model.Profile;

public interface UnfollowUserUseCase {

    Profile unfollow(String username, Long currentUserId);
}
