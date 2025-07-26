package lt.svaskevicius.videometa.service.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.UUID;
import lt.svaskevicius.videometa.dal.model.User;
import lt.svaskevicius.videometa.security.JwtTokenProvider;
import lt.svaskevicius.videometa.security.model.TokenData;
import lt.svaskevicius.videometa.service.model.CurrentUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @InjectMocks
  private IdentityService identityService;

  @Test
  @DisplayName("Should authenticate user successfully")
  void shouldAuthenticateUserSuccessfully() {
    // Given
    final String username = "testuser";
    final String password = "password123";
    final User user = createUser(UUID.randomUUID(), username, "USER");
    final CurrentUserDetails userDetails = new CurrentUserDetails(user);
    final Authentication authResult = new UsernamePasswordAuthenticationToken(
        userDetails, password, userDetails.getAuthorities());

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authResult);

    // When
    final Authentication result = identityService.authenticate(username, password);

    // Then
    assertThat(result).isEqualTo(authResult);
    assertThat(result.getPrincipal()).isEqualTo(userDetails);
    assertThat(result.getCredentials()).isEqualTo(password);
    assertThat(result.getAuthorities()).extracting("authority").contains("USER");
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  @DisplayName("Should authenticate admin user successfully")
  void shouldAuthenticateAdminUserSuccessfully() {
    // Given
    final String username = "admin";
    final String password = "adminpass";
    final User adminUser = createUser(UUID.randomUUID(), username, "ADMIN");
    final CurrentUserDetails adminDetails = new CurrentUserDetails(adminUser);
    final Authentication authResult = new UsernamePasswordAuthenticationToken(
        adminDetails, password, adminDetails.getAuthorities());

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authResult);

    // When
    final Authentication result = identityService.authenticate(username, password);

    // Then
    assertThat(result).isEqualTo(authResult);
    assertThat(result.getPrincipal()).isEqualTo(adminDetails);
    assertThat(result.getAuthorities()).extracting("authority").contains("ADMIN");
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  @DisplayName("Should throw BadCredentialsException for invalid credentials")
  void shouldThrowBadCredentialsExceptionForInvalidCredentials() {
    // Given
    final String username = "testuser";
    final String password = "wrongpassword";

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    // When / Then
    assertThatThrownBy(() -> identityService.authenticate(username, password))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Bad credentials");
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  @DisplayName("Should generate token successfully")
  void shouldGenerateTokenSuccessfully() {
    // Given
    final User user = createUser(UUID.randomUUID(), "testuser", "USER");
    final CurrentUserDetails userDetails = new CurrentUserDetails(user);
    final Authentication authentication = new UsernamePasswordAuthenticationToken(
        userDetails, "password", userDetails.getAuthorities());
    final Date now = new Date();
    final Date expiresAt = new Date(now.getTime() + 3600000);
    final TokenData expectedToken = new TokenData("jwt-token", now, expiresAt);

    when(jwtTokenProvider.generateToken(authentication)).thenReturn(expectedToken);

    // When
    final TokenData result = identityService.generateToken(authentication);

    // Then
    assertThat(result).isEqualTo(expectedToken);
    assertThat(result.token()).isEqualTo("jwt-token");
    assertThat(result.issuedAt()).isEqualTo(now);
    assertThat(result.expiresAt()).isEqualTo(expiresAt);
    verify(jwtTokenProvider).generateToken(authentication);
  }

  @Test
  @DisplayName("Should generate token for admin user successfully")
  void shouldGenerateTokenForAdminUserSuccessfully() {
    // Given
    final User adminUser = createUser(UUID.randomUUID(), "admin", "ADMIN");
    final CurrentUserDetails adminDetails = new CurrentUserDetails(adminUser);
    final Authentication authentication = new UsernamePasswordAuthenticationToken(
        adminDetails, "password", adminDetails.getAuthorities());
    final Date now = new Date();
    final Date expiresAt = new Date(now.getTime() + 3600000);
    final TokenData expectedToken = new TokenData("admin-jwt-token", now, expiresAt);

    when(jwtTokenProvider.generateToken(authentication)).thenReturn(expectedToken);

    // When
    final TokenData result = identityService.generateToken(authentication);

    // Then
    assertThat(result).isEqualTo(expectedToken);
    assertThat(result.token()).isEqualTo("admin-jwt-token");
    assertThat(result.issuedAt()).isEqualTo(now);
    assertThat(result.expiresAt()).isEqualTo(expiresAt);
    verify(jwtTokenProvider).generateToken(authentication);
  }

  @Test
  @DisplayName("Should handle null username in authentication")
  void shouldHandleNullUsernameInAuthentication() {
    // Given
    final String password = "password";

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    // When / Then
    assertThatThrownBy(() -> identityService.authenticate(null, password))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Bad credentials");
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  @DisplayName("Should handle null password in authentication")
  void shouldHandleNullPasswordInAuthentication() {
    // Given
    final String username = "testuser";

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    // When / Then
    assertThatThrownBy(() -> identityService.authenticate(username, null))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Bad credentials");
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  @DisplayName("Should handle empty credentials in authentication")
  void shouldHandleEmptyCredentialsInAuthentication() {
    // Given
    final String username = "";
    final String password = "";

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    // When / Then
    assertThatThrownBy(() -> identityService.authenticate(username, password))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Bad credentials");
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  @DisplayName("Should create correct authentication token for authenticate method")
  void shouldCreateCorrectAuthenticationTokenForAuthenticateMethod() {
    // Given
    final String username = "testuser";
    final String password = "password123";
    final User user = createUser(UUID.randomUUID(), username, "USER");
    final CurrentUserDetails userDetails = new CurrentUserDetails(user);
    final Authentication authResult = new UsernamePasswordAuthenticationToken(
        userDetails, password, userDetails.getAuthorities());

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authResult);

    // When
    identityService.authenticate(username, password);

    // Then
    verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(username, password));
  }

  private User createUser(final UUID id, final String username, final String authorities) {
    final User user = new User();
    user.setId(id);
    user.setUsername(username);
    user.setPassword("password");
    user.setAuthorities(authorities);
    return user;
  }
}