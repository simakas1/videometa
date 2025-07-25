package lt.svaskevicius.videometa.service;

import lombok.RequiredArgsConstructor;
import lt.svaskevicius.videometa.repository.UserRepository;
import lt.svaskevicius.videometa.repository.model.UserEntity;
import lt.svaskevicius.videometa.service.model.UserPrincipal;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Cacheable(value = "userDetails", key = "#username")
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
    final UserEntity user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));

    return new UserPrincipal(user);
  }
}
