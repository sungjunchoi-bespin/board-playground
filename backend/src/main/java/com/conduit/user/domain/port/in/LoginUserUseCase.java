package com.conduit.user.domain.port.in;

import com.conduit.user.domain.model.User;

public interface LoginUserUseCase {

  User login(String email, String password);
}
