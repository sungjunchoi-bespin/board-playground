package com.conduit.user.domain.port.in;

import com.conduit.user.domain.model.User;

public interface UpdateUserUseCase {

  User update(Long userId, String email, String username, String password, String bio,
      String image);
}
