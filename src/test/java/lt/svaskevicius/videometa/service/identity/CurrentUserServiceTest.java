package lt.svaskevicius.videometa.service.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import lt.svaskevicius.videometa.dal.model.User;
import lt.svaskevicius.videometa.exception.VideoMetaException;
import lt.svaskevicius.videometa.exception.VideoMetaException.VideoMetaErrorCode;
import lt.svaskevicius.videometa.service.model.CurrentUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

  @Mock
  private SecurityContext securityContext;

  @InjectMocks
  private CurrentUserService currentUserService;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Should return current user ID when authentication is valid")
  void shouldReturnCurrentUserIdWhenAuthenticationIsValid() {
    // Given
    final UUID userId = UUID.randomUUID();
    final User user = createUser(userId, "testuser", "USER");
    final CurrentUserDetails userDetails = new CurrentUserDetails(user);
    final Authentication authentication = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());

    try (final MockedStatic<SecurityContextHolder> mockedHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
      mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(authentication);

      // When
      final UUID result = currentUserService.getCurrentUserId();

      // Then
      assertThat(result).isEqualTo(userId);
    }
  }

  @Test
  @DisplayName("Should throw VideoMetaException when authentication is null")
  void shouldThrowExceptionWhenAuthenticationIsNull() {
    // Given
    try (final MockedStatic<SecurityContextHolder> mockedHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
      mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(null);

      // When / Then
      assertThatThrownBy(() -> currentUserService.getCurrentUser())
          .isInstanceOf(VideoMetaException.class)
          .hasFieldOrPropertyWithValue("errorCode", VideoMetaErrorCode.UNEXPECTED);
    }
  }

  @Test
  @DisplayName("Should throw VideoMetaException when principal is not CurrentUserDetails")
  void shouldThrowExceptionWhenPrincipalIsNotCurrentUserDetails() {
    // Given
    final Authentication authentication = new UsernamePasswordAuthenticationToken(
        "stringPrincipal", null, List.of(new SimpleGrantedAuthority("USER")));

    try (final MockedStatic<SecurityContextHolder> mockedHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
      mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(authentication);

      // When / Then
      assertThatThrownBy(() -> currentUserService.getCurrentUser())
          .isInstanceOf(VideoMetaException.class)
          .hasFieldOrPropertyWithValue("errorCode", VideoMetaErrorCode.UNEXPECTED);
    }
  }

  @Test
  @DisplayName("Should throw VideoMetaException when getCurrentUserId called with null authentication")
  void shouldThrowExceptionWhenGetCurrentUserIdCalledWithNullAuthentication() {
    // Given
    try (final MockedStatic<SecurityContextHolder> mockedHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
      mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(null);

      // When / Then
      assertThatThrownBy(() -> currentUserService.getCurrentUserId())
          .isInstanceOf(VideoMetaException.class)
          .hasFieldOrPropertyWithValue("errorCode", VideoMetaErrorCode.UNEXPECTED);
    }
  }

  @Test
  @DisplayName("Should handle admin user correctly")
  void shouldHandleAdminUserCorrectly() {
    // Given
    final UUID adminId = UUID.randomUUID();
    final User adminUser = createUser(adminId, "admin", "ADMIN");
    final CurrentUserDetails adminDetails = new CurrentUserDetails(adminUser);
    final Authentication authentication = new UsernamePasswordAuthenticationToken(
        adminDetails, null, adminDetails.getAuthorities());

    try (final MockedStatic<SecurityContextHolder> mockedHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
      mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(authentication);

      // When
      final CurrentUserDetails result = currentUserService.getCurrentUser();

      // Then
      assertThat(result).isEqualTo(adminDetails);
      assertThat(result.getId()).isEqualTo(adminId.toString());
      assertThat(result.getUsername()).isEqualTo("admin");
      assertThat(result.getAuthorities()).extracting("authority").contains("ADMIN");
    }
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