package lt.svaskevicius.videometa.web.service;

import lombok.RequiredArgsConstructor;
import lt.svaskevicius.videometa.mapper.TokenDataMapper;
import lt.svaskevicius.videometa.mapper.UserDetailsMapper;
import lt.svaskevicius.videometa.security.model.TokenData;
import lt.svaskevicius.videometa.service.identity.CurrentUserService;
import lt.svaskevicius.videometa.service.identity.IdentityService;
import lt.svaskevicius.videometa.service.model.CurrentUserDetails;
import lt.svaskevicius.videometa.web.model.identity.AuthResponseDto;
import lt.svaskevicius.videometa.web.model.identity.UserResponseDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApiService {

  private final IdentityService identityService;
  private final CurrentUserService currentUserService;
  private final TokenDataMapper tokenDataMapper;
  private final UserDetailsMapper userDetailsMapper;

  public AuthResponseDto authenticate(final String username, final String password) {
    final Authentication authentication = identityService.authenticate(username, password);
    final TokenData tokenData = identityService.generateToken(authentication);

    return tokenDataMapper.toAuthResponseDto(tokenData);
  }

  public UserResponseDto getCurrentUser() {
    final CurrentUserDetails currentUser = currentUserService.getCurrentUser();
    return userDetailsMapper.toUserResponseDto(currentUser);
  }
}
