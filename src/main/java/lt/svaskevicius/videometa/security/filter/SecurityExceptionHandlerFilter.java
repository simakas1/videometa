package lt.svaskevicius.videometa.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.svaskevicius.videometa.exception.GlobalExceptionHandler.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityExceptionHandlerFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      @NonNull final HttpServletRequest request,
      @NonNull final HttpServletResponse response,
      @NonNull final FilterChain filterChain
  ) throws IOException {

    try {
      filterChain.doFilter(request, response);
    } catch (final AuthenticationException ex) {
      handleSecurityException(response, ex, HttpStatus.UNAUTHORIZED,
          "Authentication required: " + ex.getMessage());
    } catch (final AccessDeniedException ex) {
      handleSecurityException(response, ex, HttpStatus.FORBIDDEN,
          "Access denied: Insufficient permissions");
    } catch (final Exception ex) {
      log.error("Unexpected error in security filter chain", ex);
      handleSecurityException(response, ex, HttpStatus.INTERNAL_SERVER_ERROR,
          "An unexpected error occurred. Please try again later.");
    }
  }

  private void handleSecurityException(
      final HttpServletResponse response,
      final Exception ex,
      final HttpStatus status,
      final String message
  ) throws IOException {

    log.warn("Security exception in filter: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());

    final ErrorResponse errorResponse = new ErrorResponse(
        status.name(),
        message,
        Instant.now()
    );

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    final String jsonResponse = objectMapper.writeValueAsString(errorResponse);
    response.getWriter().write(jsonResponse);
  }
}
