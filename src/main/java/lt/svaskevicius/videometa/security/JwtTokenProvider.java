package lt.svaskevicius.videometa.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.svaskevicius.videometa.config.properties.SecurityProperties;
import lt.svaskevicius.videometa.security.model.TokenData;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  private static final int MINIMUM_SECRET_KEY_LENGTH = 32;

  private final SecurityProperties securityProperties;

  public TokenData generateToken(final Authentication authentication) {
    if (authentication == null || !StringUtils.hasText(authentication.getName())) {
      throw new IllegalArgumentException("Authentication and username cannot be null or empty");
    }

    validateSecretKey();

    final String username = authentication.getName();
    final Instant now = Instant.now();
    final Instant expiration = now.plus(securityProperties.expirationTimeInMillis(), ChronoUnit.MILLIS);

    final String token = Jwts.builder()
        .subject(username)
        .issuer(securityProperties.issuer())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiration))
        .signWith(getSigningKey())
        .compact();

    return TokenData.builder()
        .issuedAt(Date.from(now))
        .expiredAt(Date.from(expiration))
        .token(token)
        .build();
  }

  private void validateSecretKey() {
    if (!StringUtils.hasText(securityProperties.secretKey())
        || securityProperties.secretKey().getBytes(StandardCharsets.UTF_8).length < MINIMUM_SECRET_KEY_LENGTH) {
      throw new IllegalStateException("JWT secret key must be at least " + MINIMUM_SECRET_KEY_LENGTH + " bytes long");
    }
  }

  private SecretKey getSigningKey() {
    final byte[] keyBytes = securityProperties.secretKey().getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String getUsernameFromToken(final String token) {
    if (!StringUtils.hasText(token)) {
      throw new IllegalArgumentException("Token cannot be null or empty");
    }

    final Claims claims = parseToken(token);
    final String username = claims.getSubject();

    if (!StringUtils.hasText(username)) {
      throw new JwtException("Token subject (username) is null or empty");
    }

    return username;
  }

  public boolean validateToken(final String token) {
    if (!StringUtils.hasText(token)) {
      return false;
    }

    try {
      final Claims claims = parseToken(token);
      final Date expiration = claims.getExpiration();
      final Date now = new Date();

      if (!securityProperties.issuer().equals(claims.getIssuer())) {
        log.warn("Invalid issuer {}", claims.getIssuer());
        return false;
      }

      if (expiration.before(now)) {
        return false;
      }

      final Date issuedAt = claims.getIssuedAt();

      return issuedAt == null || !issuedAt.after(now);
    } catch (final Exception e) {
      log.warn("Unexpected error during token validation", e);
      return false;
    }
  }

  private Claims parseToken(final String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}