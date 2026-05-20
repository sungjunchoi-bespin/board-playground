package com.conduit.user.adapter.out.persistence;

import com.conduit.user.domain.model.User;
import com.conduit.user.domain.port.out.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserPersistenceAdapter implements UserRepository {

  private final UserJpaRepository jpaRepository;

  public UserPersistenceAdapter(UserJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public User save(User user) {
    UserJpaEntity entity;
    if (user.getId() != null) {
      entity =
          jpaRepository
              .findById(user.getId())
              .orElseThrow(() -> new IllegalStateException("User not found: " + user.getId()));
      entity.setEmail(user.getEmail());
      entity.setUsername(user.getUsername());
      entity.setPassword(user.getPassword());
      entity.setBio(user.getBio());
      entity.setImage(user.getImage());
    } else {
      entity =
          new UserJpaEntity(
              user.getEmail(),
              user.getUsername(),
              user.getPassword(),
              user.getBio(),
              user.getImage());
    }
    UserJpaEntity saved = jpaRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public Optional<User> findById(Long id) {
    return jpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaRepository.findByEmail(email).map(this::toDomain);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return jpaRepository.findByUsername(username).map(this::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaRepository.existsByEmail(email);
  }

  @Override
  public boolean existsByUsername(String username) {
    return jpaRepository.existsByUsername(username);
  }

  private User toDomain(UserJpaEntity entity) {
    return new User(
        entity.getId(),
        entity.getEmail(),
        entity.getUsername(),
        entity.getPassword(),
        entity.getBio(),
        entity.getImage());
  }
}
