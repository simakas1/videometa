package lt.svaskevicius.videometa.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "lt.svaskevicius.videometa")
public class GlobalExceptionHandler {

  @ExceptionHandler(VideoMetaException.class)
  public ResponseEntity<ErrorResponse> handleVideoMetaException(final VideoMetaException ex) {
    log.error("VideoMetaException: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);
    final ErrorResponse errorResponse = new ErrorResponse(
        ex.getErrorCode().name(),
        ex.getMessage(),
        Instant.now()
    );
    return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(final MethodArgumentNotValidException ex) {
    log.warn("Validation failed: {}", ex.getMessage());
    final Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
        fieldErrors.put(error.getField(), error.getDefaultMessage())
    );
    final ValidationErrorResponse errorResponse = new ValidationErrorResponse(
        HttpStatus.BAD_REQUEST.name(),
        "Validation failed",
        fieldErrors,
        Instant.now()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(
      final ConstraintViolationException ex) {
    log.warn("Constraint violation: {}", ex.getMessage());
    final Map<String, String> fieldErrors = new HashMap<>();
    ex.getConstraintViolations().forEach(violation ->
        fieldErrors.put(violation.getPropertyPath().toString(), violation.getMessage())
    );
    final ValidationErrorResponse errorResponse = new ValidationErrorResponse(
        HttpStatus.BAD_REQUEST.name(),
        "Validation failed",
        fieldErrors,
        Instant.now()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(final Exception ex) {
    log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
    final ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.name(),
        "An unexpected error occurred. Please try again later.",
        Instant.now()
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ErrorResponse(
      String error,
      String message,
      Instant timestamp
  ) {

  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ValidationErrorResponse(
      String error,
      String message,
      Map<String, String> fieldErrors,
      Instant timestamp
  ) {

  }
}