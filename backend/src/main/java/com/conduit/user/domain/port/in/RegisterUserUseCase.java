package com.conduit.user.domain.port.in;

import com.conduit.user.domain.model.User;

public interface RegisterUserUseCase {

  User register(String username, String email, String password);
}
