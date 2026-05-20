package com.conduit.profile.adapter.out.persistence;

import com.conduit.article.adapter.out.persistence.FollowJpaRepository;
import com.conduit.profile.domain.model.Profile;
import com.conduit.profile.domain.port.out.ProfileUserRepository;
import com.conduit.user.adapter.out.persistence.UserJpaEntity;
import com.conduit.user.adapter.out.persistence.UserJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileUserPersistenceAdapter implements ProfileUserRepository {

  private final UserJpaRepository userJpaRepository;
  private final FollowJpaRepository followJpaRepository;

  public ProfileUserPersistenceAdapter(
      UserJpaRepository userJpaRepository, FollowJpaRepository followJpaRepository) {
    this.userJpaRepository = userJpaRepository;
    this.followJpaRepository = followJpaRepository;
  }

  @Override
  public Optional<Profile> findByUsername(String username, Long currentUserId) {
    return userJpaRepository
        .findByUsername(username)
        .map(entity -> toProfile(entity, currentUserId));
  }

  @Override
  public boolean existsByUsername(String username) {
    return userJpaRepository.existsByUsername(username);
  }

  @Override
  public Long findUserIdByUsername(String username) {
    return userJpaRepository
        .findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + username))
        .getId();
  }

  private Profile toProfile(UserJpaEntity entity, Long currentUserId) {
    boolean following = false;
    if (currentUserId != null) {
      following =
          followJpaRepository.existsByFollowerIdAndFolloweeId(currentUserId, entity.getId());
    }
    return new Profile(entity.getUsername(), entity.getBio(), entity.getImage(), following);
  }
}
