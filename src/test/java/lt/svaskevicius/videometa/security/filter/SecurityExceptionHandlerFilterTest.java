package lt.svaskevicius.videometa.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import lt.svaskevicius.videometa.exception.GlobalExceptionHandler.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityExceptionHandlerFilter Tests")
class SecurityExceptionHandlerFilterTest {

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @Mock
  private PrintWriter printWriter;

  @InjectMocks
  private SecurityExceptionHandlerFilter filter;

  @BeforeEach
  void setUp() throws IOException {
    when(response.getWriter()).thenReturn(printWriter);
  }

  @Test
  @DisplayName("Should handle AuthenticationException with UNAUTHORIZED status")
  void shouldHandleAuthenticationException() throws Exception {
    // Given
    final String exceptionMessage = "Invalid credentials";
    final AuthenticationException authException = new BadCredentialsException(exceptionMessage);
    final String expectedJson = "{\"error\":\"UNAUTHORIZED\"}";

    doThrow(authException).when(filterChain).doFilter(request, response);
    when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn(expectedJson);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    verify(response).setCharacterEncoding(StandardCharsets.UTF_8.name());

    final ArgumentCaptor<ErrorResponse> errorResponseCaptor = ArgumentCaptor.forClass(ErrorResponse.class);
    verify(objectMapper).writeValueAsString(errorResponseCaptor.capture());

    final ErrorResponse capturedResponse = errorResponseCaptor.getValue();
    assertThat(capturedResponse.error()).isEqualTo("UNAUTHORIZED");
    assertThat(capturedResponse.message()).isEqualTo(exceptionMessage);
    assertThat(capturedResponse.timestamp()).isNotNull();

    verify(printWriter).write(expectedJson);
  }

  @Test
  @DisplayName("Should handle AccessDeniedException with FORBIDDEN status")
  void shouldHandleAccessDeniedException() throws Exception {
    // Given
    final AccessDeniedException accessException = new AccessDeniedException("Access denied");
    final String expectedJson = "{\"error\":\"FORBIDDEN\"}";

    doThrow(accessException).when(filterChain).doFilter(request, response);
    when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn(expectedJson);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    verify(response).setCharacterEncoding(StandardCharsets.UTF_8.name());

    final ArgumentCaptor<ErrorResponse> errorResponseCaptor = ArgumentCaptor.forClass(ErrorResponse.class);
    verify(objectMapper).writeValueAsString(errorResponseCaptor.capture());

    final ErrorResponse capturedResponse = errorResponseCaptor.getValue();
    assertThat(capturedResponse.error()).isEqualTo("FORBIDDEN");
    assertThat(capturedResponse.message()).isEqualTo("Insufficient permissions");
    assertThat(capturedResponse.timestamp()).isNotNull();

    verify(printWriter).write(expectedJson);
  }

  @Test
  @DisplayName("Should handle generic Exception with INTERNAL_SERVER_ERROR status")
  void shouldHandleGenericException() throws Exception {
    // Given
    final RuntimeException genericException = new RuntimeException("Unexpected error");
    final String expectedJson = "{\"error\":\"INTERNAL_SERVER_ERROR\"}";

    doThrow(genericException).when(filterChain).doFilter(request, response);
    when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn(expectedJson);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    verify(response).setCharacterEncoding(StandardCharsets.UTF_8.name());

    final ArgumentCaptor<ErrorResponse> errorResponseCaptor = ArgumentCaptor.forClass(ErrorResponse.class);
    verify(objectMapper).writeValueAsString(errorResponseCaptor.capture());

    final ErrorResponse capturedResponse = errorResponseCaptor.getValue();
    assertThat(capturedResponse.error()).isEqualTo("INTERNAL_SERVER_ERROR");
    assertThat(capturedResponse.message()).isEqualTo("An unexpected error occurred. Please try again later.");
    assertThat(capturedResponse.timestamp()).isNotNull();

    verify(printWriter).write(expectedJson);
  }

  @Test
  @DisplayName("Should handle ServletException as generic exception")
  void shouldHandleServletException() throws Exception {
    // Given
    final ServletException servletException = new ServletException("Servlet error");
    final String expectedJson = "{\"error\":\"INTERNAL_SERVER_ERROR\"}";

    doThrow(servletException).when(filterChain).doFilter(request, response);
    when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn(expectedJson);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    verify(response).setCharacterEncoding(StandardCharsets.UTF_8.name());

    final ArgumentCaptor<ErrorResponse> errorResponseCaptor = ArgumentCaptor.forClass(ErrorResponse.class);
    verify(objectMapper).writeValueAsString(errorResponseCaptor.capture());

    final ErrorResponse capturedResponse = errorResponseCaptor.getValue();
    assertThat(capturedResponse.error()).isEqualTo("INTERNAL_SERVER_ERROR");
    assertThat(capturedResponse.message()).isEqualTo("An unexpected error occurred. Please try again later.");
    assertThat(capturedResponse.timestamp()).isNotNull();
  }

  @Test
  @DisplayName("Should handle IOException during response writing")
  void shouldHandleResponseWritingException() throws Exception {
    // Given
    final AuthenticationException authException = new BadCredentialsException("Invalid credentials");
    final String expectedJson = "{\"error\":\"UNAUTHORIZED\"}";
    final IOException writeException = new IOException("Failed to write response");

    doThrow(authException).when(filterChain).doFilter(request, response);
    when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn(expectedJson);
    when(response.getWriter()).thenThrow(writeException);

    // When / Then
    try {
      filter.doFilterInternal(request, response, filterChain);
    } catch (final IOException e) {
      assertThat(e).isEqualTo(writeException);
    }

    verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    verify(response).setCharacterEncoding(StandardCharsets.UTF_8.name());
    verify(objectMapper).writeValueAsString(any(ErrorResponse.class));
  }

  @Test
  @DisplayName("Should create ErrorResponse with correct timestamp")
  void shouldCreateErrorResponseWithCorrectTimestamp() throws Exception {
    // Given
    final Instant beforeTest = Instant.now();
    final AuthenticationException authException = new BadCredentialsException("Invalid credentials");
    final String expectedJson = "{\"error\":\"UNAUTHORIZED\"}";

    doThrow(authException).when(filterChain).doFilter(request, response);
    when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn(expectedJson);

    // When
    filter.doFilterInternal(request, response, filterChain);
    final Instant afterTest = Instant.now();

    // Then
    final ArgumentCaptor<ErrorResponse> errorResponseCaptor = ArgumentCaptor.forClass(ErrorResponse.class);
    verify(objectMapper).writeValueAsString(errorResponseCaptor.capture());

    final ErrorResponse capturedResponse = errorResponseCaptor.getValue();
    assertThat(capturedResponse.timestamp()).isBetween(beforeTest, afterTest);
  }

  @Test
  @DisplayName("Should handle null exception message in AuthenticationException")
  void shouldHandleNullExceptionMessageInAuthenticationException() throws Exception {
    // Given
    final AuthenticationException authException = new BadCredentialsException(null);
    final String expectedJson = "{\"error\":\"UNAUTHORIZED\"}";

    doThrow(authException).when(filterChain).doFilter(request, response);
    when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn(expectedJson);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    final ArgumentCaptor<ErrorResponse> errorResponseCaptor = ArgumentCaptor.forClass(ErrorResponse.class);
    verify(objectMapper).writeValueAsString(errorResponseCaptor.capture());

    final ErrorResponse capturedResponse = errorResponseCaptor.getValue();
    assertThat(capturedResponse.error()).isEqualTo("UNAUTHORIZED");
    assertThat(capturedResponse.message()).isNull();
    assertThat(capturedResponse.timestamp()).isNotNull();
  }

  @Test
  @DisplayName("Should verify response headers are set correctly")
  void shouldVerifyResponseHeadersAreSetCorrectly() throws Exception {
    // Given
    final AuthenticationException authException = new BadCredentialsException("Invalid credentials");
    final String expectedJson = "{\"error\":\"UNAUTHORIZED\"}";

    doThrow(authException).when(filterChain).doFilter(request, response);
    when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn(expectedJson);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(response).setStatus(401);
    verify(response).setContentType("application/json");
    verify(response).setCharacterEncoding("UTF-8");
  }
}