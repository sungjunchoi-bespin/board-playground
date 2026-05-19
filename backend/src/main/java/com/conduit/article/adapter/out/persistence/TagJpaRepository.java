package com.conduit.article.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagJpaRepository extends JpaRepository<TagJpaEntity, Long> {

  Optional<TagJpaEntity> findByName(String name);

  List<TagJpaEntity> findByNameIn(List<String> names);
}
