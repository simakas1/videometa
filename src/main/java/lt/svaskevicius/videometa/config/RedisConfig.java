package lt.svaskevicius.videometa.config;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lt.svaskevicius.videometa.config.properties.CacheProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig {

  private static final Duration DEFAULT_TTL = Duration.ofSeconds(300);

  private final CacheProperties cacheProperties;

  @Bean
  public RedisCacheManager cacheManager(final RedisConnectionFactory connectionFactory) {
    final RedisCacheConfiguration defaultConfig = createDefaultCacheConfiguration();
    final Map<String, RedisCacheConfiguration> cacheConfigurations = buildCacheConfigurations(defaultConfig);

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(cacheConfigurations)
        .build();
  }

  private RedisCacheConfiguration createDefaultCacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
        .disableCachingNullValues()
        .entryTtl(DEFAULT_TTL);
  }

  private Map<String, RedisCacheConfiguration> buildCacheConfigurations(final RedisCacheConfiguration baseConfig) {
    return cacheProperties.entries().entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> baseConfig.entryTtl(Duration.ofSeconds(entry.getValue()))
        ));
  }
}