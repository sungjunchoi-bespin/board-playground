package com.conduit.shared.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(description = "Error response wrapper")
public record ErrorResponse(
    @Schema(
            description = "Map of field names to error messages",
            example = "{\"body\": [\"can't be blank\"]}")
        Map<String, List<String>> errors) {

  public static ErrorResponse of(String field, String message) {
    return new ErrorResponse(Map.of(field, List.of(message)));
  }

  public static ErrorResponse of(Map<String, List<String>> errors) {
    return new ErrorResponse(errors);
  }
}
