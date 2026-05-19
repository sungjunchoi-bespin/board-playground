package com.conduit.tag.adapter.out.persistence;

import com.conduit.article.adapter.out.persistence.TagJpaEntity;
import com.conduit.article.adapter.out.persistence.TagJpaRepository;
import com.conduit.tag.domain.model.Tag;
import com.conduit.tag.domain.port.out.TagRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class TagPersistenceAdapter implements TagRepository {

  private final TagJpaRepository tagJpaRepository;

  public TagPersistenceAdapter(TagJpaRepository tagJpaRepository) {
    this.tagJpaRepository = tagJpaRepository;
  }

  @Override
  public List<Tag> findAll() {
    return tagJpaRepository.findAll().stream()
        .map(entity -> new Tag(entity.getId(), entity.getName()))
        .toList();
  }
}
