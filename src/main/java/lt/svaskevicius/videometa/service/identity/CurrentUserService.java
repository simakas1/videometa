package lt.svaskevicius.videometa.service.identity;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.svaskevicius.videometa.exception.VideoMetaException;
import lt.svaskevicius.videometa.exception.VideoMetaException.VideoMetaErrorCode;
import lt.svaskevicius.videometa.service.model.CurrentUserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentUserService {

  public CurrentUserDetails getCurrentUser() {
    final Authentication authentication = SecurityContextHolder.getContext()
        .getAuthentication();

    if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
      throw new VideoMetaException(VideoMetaErrorCode.UNEXPECTED);
    }

    if (authentication.getPrincipal() instanceof final CurrentUserDetails userDetails) {
      return userDetails;
    }

    throw new VideoMetaException(VideoMetaErrorCode.UNEXPECTED);
  }

  public UUID getCurrentUserId() {
    return UUID.fromString(getCurrentUser().getId());
  }
}
