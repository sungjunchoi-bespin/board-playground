package com.conduit.shared.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
    log.warn("API error [{}]: {}", ex.getStatus().value(), ex.getMessage());
    ErrorResponse response = ErrorResponse.of("body", ex.getMessage());
    return ResponseEntity.status(ex.getStatus()).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, List<String>> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            fieldError -> {
              errors
                  .computeIfAbsent(fieldError.getField(), k -> new ArrayList<>())
                  .add(fieldError.getDefaultMessage());
            });
    log.warn("Validation error: {}", errors);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ErrorResponse.of(errors));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
    String message = ex.getMostSpecificCause().getMessage();
    String field = "body";

    if (message != null) {
      String lowerMessage = message.toLowerCase();
      if (lowerMessage.contains("email")) {
        field = "email";
      } else if (lowerMessage.contains("username")) {
        field = "username";
      }
    }

    log.warn("Data integrity violation: field={}", field);
    ErrorResponse response = ErrorResponse.of(field, "has already been taken");
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
    log.error("Unexpected error", ex);
    ErrorResponse response = ErrorResponse.of("server", "internal server error");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
