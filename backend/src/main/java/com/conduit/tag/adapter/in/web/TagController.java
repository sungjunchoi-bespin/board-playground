package com.conduit.tag.adapter.in.web;

import com.conduit.tag.domain.port.in.GetTagsUseCase;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TagController {

  private final GetTagsUseCase getTagsUseCase;

  public TagController(GetTagsUseCase getTagsUseCase) {
    this.getTagsUseCase = getTagsUseCase;
  }

  @GetMapping("/tags")
  public ResponseEntity<Map<String, List<String>>> getTags() {
    List<String> tags = getTagsUseCase.getAllTagNames();
    return ResponseEntity.ok(Map.of("tags", tags));
  }
}
