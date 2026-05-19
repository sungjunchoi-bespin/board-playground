package com.conduit.user.application.service;

import com.conduit.shared.exception.ApiException;
import com.conduit.user.domain.model.User;
import com.conduit.user.domain.port.in.GetCurrentUserUseCase;
import com.conduit.user.domain.port.in.LoginUserUseCase;
import com.conduit.user.domain.port.in.RegisterUserUseCase;
import com.conduit.user.domain.port.in.UpdateUserUseCase;
import com.conduit.user.domain.port.out.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService
    implements RegisterUserUseCase, LoginUserUseCase, GetCurrentUserUseCase, UpdateUserUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public User register(String username, String email, String password) {
    if (userRepository.existsByEmail(email)) {
      throw new ApiException.ValidationException("email has already been taken");
    }
    if (userRepository.existsByUsername(username)) {
      throw new ApiException.ValidationException("username has already been taken");
    }

    User user = User.create(email, username, passwordEncoder.encode(password));
    return userRepository.save(user);
  }

  @Override
  @Transactional(readOnly = true)
  public User login(String email, String password) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException.UnauthorizedException("invalid email or password"));

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new ApiException.UnauthorizedException("invalid email or password");
    }

    return user;
  }

  @Override
  @Transactional(readOnly = true)
  public User getCurrentUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ApiException.NotFoundException("user not found"));
  }

  @Override
  public User update(Long userId, String email, String username, String password, String bio,
      String image) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ApiException.NotFoundException("user not found"));

    if (email != null && !email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
      throw new ApiException.ValidationException("email has already been taken");
    }
    if (username != null && !username.equals(user.getUsername())
        && userRepository.existsByUsername(username)) {
      throw new ApiException.ValidationException("username has already been taken");
    }

    String hashedPassword = password != null ? passwordEncoder.encode(password) : null;
    user.update(email, username, hashedPassword, bio, image);
    return userRepository.save(user);
  }
}
