package lt.svaskevicius.videometa.service.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import lt.svaskevicius.videometa.dal.model.User;
import lt.svaskevicius.videometa.dal.repository.UserRepository;
import lt.svaskevicius.videometa.service.model.CurrentUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class DefaultUserDetailsServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private DefaultUserDetailsService userDetailsService;

  @Test
  @DisplayName("Should load user by username successfully")
  void shouldLoadUserByUsernameSuccessfully() {
    // Given
    final String username = "testuser";
    final User user = createUser(UUID.randomUUID(), username, "USER");
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

    // When
    final UserDetails result = userDetailsService.loadUserByUsername(username);

    // Then
    assertThat(result).isInstanceOf(CurrentUserDetails.class);
    assertThat(result.getUsername()).isEqualTo(username);
    assertThat(result.getPassword()).isEqualTo("password");
    assertThat(result.getAuthorities()).extracting("authority").contains("USER");
    verify(userRepository).findByUsername(username);
  }

  @Test
  @DisplayName("Should load admin user by username successfully")
  void shouldLoadAdminUserByUsernameSuccessfully() {
    // Given
    final String username = "admin";
    final User adminUser = createUser(UUID.randomUUID(), username, "ADMIN");
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

    // When
    final UserDetails result = userDetailsService.loadUserByUsername(username);

    // Then
    assertThat(result).isInstanceOf(CurrentUserDetails.class);
    assertThat(result.getUsername()).isEqualTo(username);
    assertThat(result.getAuthorities()).extracting("authority").contains("ADMIN");
    verify(userRepository).findByUsername(username);
  }

  @Test
  @DisplayName("Should throw UsernameNotFoundException when user not found")
  void shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
    // Given
    final String username = "nonexistent";
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage("No user found");
    verify(userRepository).findByUsername(username);
  }

  @Test
  @DisplayName("Should return CurrentUserDetails with correct user data")
  void shouldReturnCurrentUserDetailsWithCorrectUserData() {
    // Given
    final UUID userId = UUID.randomUUID();
    final String username = "testuser";
    final String password = "encodedPassword";
    final String authorities = "USER,VIDEO_ANALYTICS";

    final User user = new User();
    user.setId(userId);
    user.setUsername(username);
    user.setPassword(password);
    user.setAuthorities(authorities);

    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

    // When
    final UserDetails result = userDetailsService.loadUserByUsername(username);

    // Then
    final CurrentUserDetails currentUserDetails = (CurrentUserDetails) result;
    assertThat(currentUserDetails.getId()).isEqualTo(userId.toString());
    assertThat(currentUserDetails.getUsername()).isEqualTo(username);
    assertThat(currentUserDetails.getPassword()).isEqualTo(password);
    assertThat(currentUserDetails.getAuthorities()).hasSize(2);
    assertThat(currentUserDetails.getAuthorities()).extracting("authority")
        .contains("USER", "VIDEO_ANALYTICS");
  }

  @Test
  @DisplayName("Should call repository only once for same username")
  void shouldCallRepositoryOnlyOnceForSameUsername() {
    // Given
    final String username = "testuser";
    final User user = createUser(UUID.randomUUID(), username, "USER");
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

    // When
    userDetailsService.loadUserByUsername(username);
    userDetailsService.loadUserByUsername(username);

    // Then
    verify(userRepository, times(2)).findByUsername(username);
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