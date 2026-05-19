package com.conduit.shared.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
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

    ErrorResponse response = ErrorResponse.of(field, "has already been taken");
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
    ErrorResponse response = ErrorResponse.of("server", "internal server error");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
