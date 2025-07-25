package lt.svaskevicius.videometa.api.service;

import lombok.RequiredArgsConstructor;
import lt.svaskevicius.videometa.api.model.identity.AuthResponseDto;
import lt.svaskevicius.videometa.security.model.TokenData;
import lt.svaskevicius.videometa.service.IdentityService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApiService {

  private final IdentityService identityService;

  public AuthResponseDto authenticate(final String username, final String password) {
    final Authentication authentication = identityService.authenticate(username, password);
    final TokenData tokenData = identityService.generateToken(authentication);
    
    return AuthResponseDto.builder()
        .token(tokenData.token())
        .expiresAt(tokenData.expiredAt())
        .issuedAt(tokenData.issuedAt())
        .build();
  }
}
