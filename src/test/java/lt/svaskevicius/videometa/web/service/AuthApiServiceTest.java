package lt.svaskevicius.videometa.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import lt.svaskevicius.videometa.dal.model.User;
import lt.svaskevicius.videometa.mapper.TokenDataMapper;
import lt.svaskevicius.videometa.mapper.UserDetailsMapper;
import lt.svaskevicius.videometa.security.model.TokenData;
import lt.svaskevicius.videometa.service.identity.CurrentUserService;
import lt.svaskevicius.videometa.service.identity.IdentityService;
import lt.svaskevicius.videometa.service.model.CurrentUserDetails;
import lt.svaskevicius.videometa.web.model.identity.AuthResponseDto;
import lt.svaskevicius.videometa.web.model.identity.UserResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthApiServiceTest {

  @Mock
  private IdentityService identityService;

  @Mock
  private TokenDataMapper tokenDataMapper;

  @Mock
  private CurrentUserService currentUserService;

  @Mock
  private UserDetailsMapper userDetailsMapper;

  @InjectMocks
  private AuthApiService authApiService;

  @Test
  @DisplayName("Should authenticate user successfully and return auth response")
  void shouldAuthenticateUserSuccessfullyAndReturnAuthResponse() {
    // Given
    final String username = "testuser";
    final String password = "password123";
    final User user = createUser(UUID.randomUUID());
    final CurrentUserDetails userDetails = new CurrentUserDetails(user);
    final Authentication authentication = new UsernamePasswordAuthenticationToken(
        userDetails, password, userDetails.getAuthorities());

    final Date issuedAt = new Date();
    final Date expiresAt = new Date(issuedAt.getTime() + 3600000);
    final TokenData tokenData = new TokenData("jwt-token-123", issuedAt, expiresAt);
    final AuthResponseDto authResponseDto = new AuthResponseDto("jwt-token-123", expiresAt, issuedAt);

    when(tokenDataMapper.toAuthResponseDto(tokenData)).thenReturn(authResponseDto);
    when(identityService.authenticate(username, password)).thenReturn(authentication);
    when(identityService.generateToken(authentication)).thenReturn(tokenData);

    // When
    final AuthResponseDto result = authApiService.authenticate(username, password);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.token()).isEqualTo("jwt-token-123");
    assertThat(result.issuedAt()).isEqualTo(issuedAt);
    assertThat(result.expiresAt()).isEqualTo(expiresAt);
    verify(identityService).authenticate(username, password);
    verify(identityService).generateToken(authentication);
  }

  @Test
  @DisplayName("Should handle empty credentials")
  void shouldHandleEmptyCredentials() {
    // Given
    final String username = "";
    final String password = "";

    when(identityService.authenticate(username, password))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    // When / Then
    assertThatThrownBy(() -> authApiService.authenticate(username, password))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Bad credentials");
    verify(identityService).authenticate(username, password);
  }

  @Test
  @DisplayName("Should return current user details")
  void shouldReturnCurrentUserDetails() {
    // Given
    final UUID userId = UUID.randomUUID();
    final User user = createUser(userId);
    final CurrentUserDetails currentUserDetails = new CurrentUserDetails(user);
    final UserResponseDto userResponseDto = UserResponseDto.builder()
        .id(userId.toString())
        .username("testuser")
        .authorities(
            List.of("USER"))
        .active(true)
        .build();

    when(currentUserService.getCurrentUser()).thenReturn(currentUserDetails);
    when(userDetailsMapper.toUserResponseDto(currentUserDetails)).thenReturn(userResponseDto);

    // When
    final UserResponseDto result = authApiService.getCurrentUser();

    // Then
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(currentUserDetails.getId());
    assertThat(result.username()).isEqualTo("testuser");
    verify(currentUserService).getCurrentUser();
  }

  private User createUser(final UUID id) {
    final User user = new User();
    user.setId(id);
    user.setUsername("testuser");
    user.setPassword("password");
    user.setAuthorities("USER");
    return user;
  }
}