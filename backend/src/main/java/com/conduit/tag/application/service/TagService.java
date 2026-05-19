package com.conduit.tag.application.service;

import com.conduit.tag.domain.model.Tag;
import com.conduit.tag.domain.port.in.GetTagsUseCase;
import com.conduit.tag.domain.port.out.TagRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TagService implements GetTagsUseCase {

  private final TagRepository tagRepository;

  public TagService(TagRepository tagRepository) {
    this.tagRepository = tagRepository;
  }

  @Override
  public List<String> getAllTagNames() {
    return tagRepository.findAll().stream().map(Tag::getName).sorted().toList();
  }
}
