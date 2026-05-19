package com.conduit.tag.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.conduit.tag.domain.model.Tag;
import com.conduit.tag.domain.port.out.TagRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TagService Unit Tests")
class TagServiceTest {

  private TagService tagService;
  private StubTagRepository stubTagRepository;

  @BeforeEach
  void setUp() {
    stubTagRepository = new StubTagRepository();
    tagService = new TagService(stubTagRepository);
  }

  @Test
  @DisplayName("should return empty list when no tags exist")
  void emptyTags() {
    stubTagRepository.tags = List.of();

    List<String> result = tagService.getAllTagNames();

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("should return all tag names")
  void allTagNames() {
    stubTagRepository.tags =
        List.of(new Tag(1L, "java"), new Tag(2L, "spring"), new Tag(3L, "react"));

    List<String> result = tagService.getAllTagNames();

    assertThat(result).containsExactly("java", "react", "spring");
  }

  @Test
  @DisplayName("should return tag names sorted alphabetically")
  void sortedAlphabetically() {
    stubTagRepository.tags =
        List.of(new Tag(1L, "zebra"), new Tag(2L, "alpha"), new Tag(3L, "middle"));

    List<String> result = tagService.getAllTagNames();

    assertThat(result).containsExactly("alpha", "middle", "zebra");
  }

  @Test
  @DisplayName("should return single tag")
  void singleTag() {
    stubTagRepository.tags = List.of(new Tag(1L, "solo"));

    List<String> result = tagService.getAllTagNames();

    assertThat(result).containsExactly("solo");
  }

  static class StubTagRepository implements TagRepository {
    List<Tag> tags = List.of();

    @Override
    public List<Tag> findAll() {
      return tags;
    }
  }
}
