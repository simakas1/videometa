package lt.svaskevicius.videometa.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.SneakyThrows;
import lt.svaskevicius.videometa.dal.model.User;
import lt.svaskevicius.videometa.security.JwtTokenProvider;
import lt.svaskevicius.videometa.service.model.CurrentUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthorizationFilter Tests")
class JwtAuthorizationFilterTest {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String VALID_TOKEN = "valid.jwt.token";
  private static final String INVALID_TOKEN = "invalid.jwt.token";
  private static final String USERNAME = "testuser";
  private static final String BEARER_TOKEN = "Bearer " + VALID_TOKEN;

  @Mock
  private UserDetailsService userDetailsService;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @Mock
  private SecurityContext securityContext;

  @InjectMocks
  private JwtAuthorizationFilter filter;

  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    final User user = new User();
    user.setId(UUID.randomUUID());
    user.setUsername(USERNAME);
    user.setPassword("password");
    user.setAuthorities("USER");
    userDetails = new CurrentUserDetails(user);

    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @SneakyThrows
  @DisplayName("Should pass through request when no authorization header is present")
  void shouldPassThroughWhenNoAuthorizationHeader() {
    // Given
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    verify(jwtTokenProvider, never()).validateToken(anyString());
    verify(userDetailsService, never()).loadUserByUsername(anyString());
    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  @SneakyThrows
  @DisplayName("Should pass through request when authorization header doesn't start with Bearer")
  void shouldPassThroughWhenNoBearerPrefix() {
    // Given
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("Basic basicAuthEncodedPasswordAndUsername");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    verify(jwtTokenProvider, never()).validateToken(anyString());
    verify(userDetailsService, never()).loadUserByUsername(anyString());
    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  @SneakyThrows
  @DisplayName("Should pass through request when Bearer token is empty")
  void shouldPassThroughWhenBearerTokenIsEmpty() {
    // Given
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("Bearer ");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    verify(jwtTokenProvider, never()).validateToken(anyString());
    verify(userDetailsService, never()).loadUserByUsername(anyString());
    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  @SneakyThrows
  @DisplayName("Should pass through request when Bearer token is only whitespace")
  void shouldPassThroughWhenBearerTokenIsWhitespace() {
    // Given
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("Bearer   ");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    verify(jwtTokenProvider, never()).validateToken(anyString());
    verify(userDetailsService, never()).loadUserByUsername(anyString());
    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  @SneakyThrows
  @DisplayName("Should pass through request when user is already authenticated")
  void shouldPassThroughWhenUserAlreadyAuthenticated() {
    // Given
    final Authentication existingAuth = mock(Authentication.class);
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);
    when(securityContext.getAuthentication()).thenReturn(existingAuth);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    verify(jwtTokenProvider, never()).validateToken(anyString());
    verify(userDetailsService, never()).loadUserByUsername(anyString());
    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  @SneakyThrows
  @DisplayName("Should authenticate user successfully with valid JWT token")
  void shouldAuthenticateUserSuccessfully() {
    // Given
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);
    when(securityContext.getAuthentication()).thenReturn(null);
    when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn(USERNAME);
    when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    verify(jwtTokenProvider).validateToken(VALID_TOKEN);
    verify(jwtTokenProvider).getUsernameFromToken(VALID_TOKEN);
    verify(userDetailsService).loadUserByUsername(USERNAME);

    verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  @DisplayName("Should throw BadCredentialsException when JWT token is invalid")
  void shouldThrowExceptionWhenTokenInvalid() {
    // Given
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("Bearer " + INVALID_TOKEN);
    when(securityContext.getAuthentication()).thenReturn(null);
    when(jwtTokenProvider.validateToken(INVALID_TOKEN)).thenReturn(false);

    // When / Then
    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Invalid or expired JWT token");

    verify(jwtTokenProvider).validateToken(INVALID_TOKEN);
    verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
    verify(userDetailsService, never()).loadUserByUsername(anyString());
    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  @DisplayName("Should throw BadCredentialsException when username is null in token")
  void shouldThrowExceptionWhenUsernameIsNull() {
    // Given
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);
    when(securityContext.getAuthentication()).thenReturn(null);
    when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn(null);

    // When / Then
    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Username not found in JWT token");

    verify(jwtTokenProvider).validateToken(VALID_TOKEN);
    verify(jwtTokenProvider).getUsernameFromToken(VALID_TOKEN);
    verify(userDetailsService, never()).loadUserByUsername(anyString());
    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  @DisplayName("Should throw BadCredentialsException when username is empty in token")
  void shouldThrowExceptionWhenUsernameIsEmpty() {
    // Given
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);
    when(securityContext.getAuthentication()).thenReturn(null);
    when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn("");

    // When / Then
    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Username not found in JWT token");

    verify(jwtTokenProvider).validateToken(VALID_TOKEN);
    verify(jwtTokenProvider).getUsernameFromToken(VALID_TOKEN);
    verify(userDetailsService, never()).loadUserByUsername(anyString());
    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  @DisplayName("Should throw BadCredentialsException when username is only whitespace in token")
  void shouldThrowExceptionWhenUsernameIsWhitespace() {
    // Given
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);
    when(securityContext.getAuthentication()).thenReturn(null);
    when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn("   ");

    // When / Then
    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Username not found in JWT token");

    verify(jwtTokenProvider).validateToken(VALID_TOKEN);
    verify(jwtTokenProvider).getUsernameFromToken(VALID_TOKEN);
    verify(userDetailsService, never()).loadUserByUsername(anyString());
    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  @DisplayName("Should throw BadCredentialsException when user is not found")
  void shouldThrowExceptionWhenUserNotFound() {
    // Given
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);
    when(securityContext.getAuthentication()).thenReturn(null);
    when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn(USERNAME);
    when(userDetailsService.loadUserByUsername(USERNAME))
        .thenThrow(new UsernameNotFoundException("User not found"));

    // When / Then
    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("User not found")
        .hasCauseInstanceOf(UsernameNotFoundException.class);

    verify(jwtTokenProvider).validateToken(VALID_TOKEN);
    verify(jwtTokenProvider).getUsernameFromToken(VALID_TOKEN);
    verify(userDetailsService).loadUserByUsername(USERNAME);
    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  @DisplayName("Should throw BadCredentialsException when token username doesn't match user details username")
  void shouldThrowExceptionWhenUsernameMismatch() {
    // Given
    final String differentUsername = "differentuser";
    final User user = new User();
    user.setId(UUID.randomUUID());
    user.setUsername(differentUsername);
    user.setPassword("password");
    user.setAuthorities("USER");
    final UserDetails userDetailsWithDifferentUsername = new CurrentUserDetails(user);

    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);
    when(securityContext.getAuthentication()).thenReturn(null);
    when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn(USERNAME);
    when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetailsWithDifferentUsername);

    // When / Then
    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Token username mismatch");

    verify(jwtTokenProvider).validateToken(VALID_TOKEN);
    verify(jwtTokenProvider).getUsernameFromToken(VALID_TOKEN);
    verify(userDetailsService).loadUserByUsername(USERNAME);
    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  @SneakyThrows
  @DisplayName("Should set authentication details correctly")
  void shouldSetAuthenticationDetailsCorrectly() {
    // Given
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);
    when(securityContext.getAuthentication()).thenReturn(null);
    when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn(USERNAME);
    when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(securityContext).setAuthentication(argThat(auth -> {
      final UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) auth;
      return token.getPrincipal().equals(userDetails) &&
          token.getCredentials() == null &&
          token.getAuthorities().equals(userDetails.getAuthorities()) &&
          token.getDetails() instanceof WebAuthenticationDetails;
    }));
  }

  @Test
  @SneakyThrows
  @DisplayName("Should extract JWT token correctly from Bearer format")
  void shouldExtractJwtTokenCorrectly() {
    // Given
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);
    when(securityContext.getAuthentication()).thenReturn(null);
    when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn(USERNAME);
    when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(jwtTokenProvider).validateToken(VALID_TOKEN);
    verify(jwtTokenProvider).getUsernameFromToken(VALID_TOKEN);
    verify(userDetailsService).loadUserByUsername(USERNAME);
    verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  @SneakyThrows
  @DisplayName("Should handle IOException from filter chain")
  void shouldHandleIOExceptionFromFilterChain() {
    // Given
    final IOException expectedException = new IOException("Filter chain error");
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);
    doThrow(expectedException).when(filterChain).doFilter(request, response);

    // When / Then
    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isEqualTo(expectedException);
  }

  @Test
  @SneakyThrows
  @DisplayName("Should handle ServletException from filter chain")
  void shouldHandleServletExceptionFromFilterChain() {
    // Given
    final ServletException expectedException = new ServletException("Filter chain error");
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);
    doThrow(expectedException).when(filterChain).doFilter(request, response);

    // When / Then
    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isEqualTo(expectedException);
  }

  @Test
  @DisplayName("Should clear security context after test")
  void shouldClearSecurityContextAfterTest() {
    SecurityContextHolder.clearContext();
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}