package com.conduit.profile.adapter.out.persistence;

import com.conduit.article.adapter.out.persistence.FollowId;
import com.conduit.article.adapter.out.persistence.FollowJpaEntity;
import com.conduit.article.adapter.out.persistence.FollowJpaRepository;
import com.conduit.profile.domain.port.out.ProfileFollowRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ProfileFollowPersistenceAdapter implements ProfileFollowRepository {

    private final FollowJpaRepository followJpaRepository;

    public ProfileFollowPersistenceAdapter(FollowJpaRepository followJpaRepository) {
        this.followJpaRepository = followJpaRepository;
    }

    @Override
    public boolean isFollowing(Long followerId, Long followeeId) {
        return followJpaRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    @Override
    public void follow(Long followerId, Long followeeId) {
        followJpaRepository.save(new FollowJpaEntity(followerId, followeeId));
    }

    @Override
    public void unfollow(Long followerId, Long followeeId) {
        followJpaRepository.deleteById(new FollowId(followerId, followeeId));
    }
}
