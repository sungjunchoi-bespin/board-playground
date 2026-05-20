package com.conduit.tag.adapter.in.web;

import com.conduit.tag.domain.port.in.GetTagsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tags", description = "Tags for articles")
@RestController
@RequestMapping("/api")
public class TagController {

  private final GetTagsUseCase getTagsUseCase;

  public TagController(GetTagsUseCase getTagsUseCase) {
    this.getTagsUseCase = getTagsUseCase;
  }

  @Operation(summary = "Get tags", description = "Get all tags used across articles")
  @ApiResponse(responseCode = "200", description = "List of tags")
  @GetMapping("/tags")
  public ResponseEntity<Map<String, List<String>>> getTags() {
    List<String> tags = getTagsUseCase.getAllTagNames();
    return ResponseEntity.ok(Map.of("tags", tags));
  }
}
