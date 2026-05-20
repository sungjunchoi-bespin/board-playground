package com.conduit.profile.domain.port.in;

import com.conduit.profile.domain.model.Profile;

public interface GetProfileUseCase {

  Profile getProfile(String username, Long currentUserId);
}
