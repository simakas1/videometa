package lt.svaskevicius.videometa.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class VideoMetaException extends RuntimeException {

  private final VideoMetaErrorCode errorCode;

  public VideoMetaException(final VideoMetaErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public VideoMetaException(final VideoMetaErrorCode errorCode,
      final String additionalMessage) {
    super(additionalMessage);
    this.errorCode = errorCode;
  }

  public HttpStatus getHttpStatus() {
    return errorCode.getHttpStatus();
  }

  @Getter
  public enum VideoMetaErrorCode {
    UNEXPECTED("Unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND("Resource not found", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS("Invalid credentials provided", HttpStatus.UNAUTHORIZED);


    private final String message;
    private final HttpStatus httpStatus;

    VideoMetaErrorCode(final String message, final HttpStatus httpStatus) {
      this.message = message;
      this.httpStatus = httpStatus;
    }
  }
}