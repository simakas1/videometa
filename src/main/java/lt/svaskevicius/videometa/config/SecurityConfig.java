package lt.svaskevicius.videometa.config;

import lombok.RequiredArgsConstructor;
import lt.svaskevicius.videometa.security.filter.JwtAuthorizationFilter;
import lt.svaskevicius.videometa.security.filter.SecurityExceptionHandlerFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserDetailsService userDetailsService;
  private final JwtAuthorizationFilter jwtAuthorizationFilter;
  private final SecurityExceptionHandlerFilter securityExceptionHandlerFilter;

  private static final String[] PUBLIC_ENDPOINTS = {
      "/auth/login",
      "/actuator/health",
      "/swagger-ui/**",
      "/v3/api-docs/**",
  };

  @Bean
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
            .anyRequest().hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(securityExceptionHandlerFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(customAuthenticationEntryPoint())
            .accessDeniedHandler(customAccessDeniedHandler())
        )
        .build();
  }

  @Bean
  public AuthenticationEntryPoint customAuthenticationEntryPoint() {
    return (request, response, authException) -> {
      throw new BadCredentialsException(
          "Authentication required: " + authException.getMessage()
      );
    };
  }

  @Bean
  public AccessDeniedHandler customAccessDeniedHandler() {
    return (request, response, accessDeniedException) -> {
      throw new AccessDeniedException(
          "Access denied: Insufficient permissions"
      );
    };
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
    authenticationProvider.setHideUserNotFoundExceptions(true);
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    return authenticationProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(final AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}