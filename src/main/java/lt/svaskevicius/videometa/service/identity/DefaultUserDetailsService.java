package lt.svaskevicius.videometa.service.identity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.svaskevicius.videometa.dal.model.User;
import lt.svaskevicius.videometa.dal.repository.UserRepository;
import lt.svaskevicius.videometa.service.model.CurrentUserDetails;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Cacheable(value = "userDetails", key = "#username")
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
    final User user = userRepository.findByUsername(username)
        .orElseThrow(() -> {
          log.warn("User not found: {}", username);
          return new UsernameNotFoundException("No user found");
        });

    return new CurrentUserDetails(user);
  }
}
