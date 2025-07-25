package lt.svaskevicius.videometa.service;

import lombok.RequiredArgsConstructor;
import lt.svaskevicius.videometa.security.JwtTokenProvider;
import lt.svaskevicius.videometa.security.model.TokenData;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentityService {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;

  public Authentication authenticate(final String username, final String password) {
    final var authObject = new UsernamePasswordAuthenticationToken(username, password);
    return authenticationManager.authenticate(authObject);
  }

  public TokenData generateToken(final Authentication authentication) {
    return jwtTokenProvider.generateToken(authentication);
  }
}
