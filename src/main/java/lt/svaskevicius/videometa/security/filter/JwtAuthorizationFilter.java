package lt.svaskevicius.videometa.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.svaskevicius.videometa.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final int BEARER_PREFIX_LENGTH = 7;

  private final UserDetailsService userDetailsService;
  private final JwtTokenProvider jwtTokenProvider;

  @Override
  protected void doFilterInternal(
      @NonNull final HttpServletRequest request,
      @NonNull final HttpServletResponse response,
      @NonNull final FilterChain filterChain
  ) throws IOException, ServletException {
    final Optional<String> jwtToken = extractJwtFromRequest(request);

    if (jwtToken.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
      authenticateUser(jwtToken.get(), request);
    }

    filterChain.doFilter(request, response);
  }

  private Optional<String> extractJwtFromRequest(final HttpServletRequest request) {
    final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

    if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
      final String token = authorizationHeader.substring(BEARER_PREFIX_LENGTH);
      return StringUtils.hasText(token) ? Optional.of(token) : Optional.empty();
    }

    return Optional.empty();
  }

  private void authenticateUser(final String jwtToken, final HttpServletRequest request) {
    if (!jwtTokenProvider.validateToken(jwtToken)) {
      throw new BadCredentialsException("Invalid or expired JWT token");
    }

    final String username = jwtTokenProvider.getUsernameFromToken(jwtToken);
    if (!StringUtils.hasText(username)) {
      throw new BadCredentialsException("Username not found in JWT token");
    }

    final UserDetails userDetails = loadUserDetails(username);
    if (!username.equals(userDetails.getUsername())) {
      throw new BadCredentialsException("Token username mismatch") {
      };
    }

    final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
    );

    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    log.debug("User {} authenticated successfully", username);
  }

  private UserDetails loadUserDetails(final String username) {
    try {
      return userDetailsService.loadUserByUsername(username);
    } catch (final UsernameNotFoundException e) {
      log.warn("User not found: {}", username);
      throw new BadCredentialsException("User not found: " + username, e);
    }
  }
}