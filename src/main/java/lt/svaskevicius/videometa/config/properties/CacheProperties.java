package lt.svaskevicius.videometa.config.properties;

import java.util.Collections;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache")
public record CacheProperties(Map<String, Long> entries) {

  public CacheProperties {
    entries = entries != null ? Map.copyOf(entries) : Collections.emptyMap();
  }

  public Map<String, Long> entries() {
    return entries;
  }
}