package com.conduit.user.domain.port.in;

import com.conduit.user.domain.model.User;

public interface GetCurrentUserUseCase {

  User getCurrentUser(Long userId);
}
