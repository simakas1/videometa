package lt.svaskevicius.videometa.integration.mockoon.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockoonClientCircuitBreakerConfig {

  @Bean
  public CircuitBreaker mockCircuitBreaker() {
    final CircuitBreakerConfig config = getDefaultConfig();
    return CircuitBreakerRegistry.of(config).circuitBreaker("mockoonClientCircuitBreaker");
  }

  private CircuitBreakerConfig getDefaultConfig() {
    return CircuitBreakerConfig.custom()
        .failureRateThreshold(50)
        .waitDurationInOpenState(java.time.Duration.ofSeconds(30))
        .slidingWindowSize(10)
        .build();
  }
}
