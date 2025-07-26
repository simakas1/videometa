package lt.svaskevicius.videometa.integration.mockoon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.mockoon")
public record MockoonClientConfigProperties(
    String baseUrl
) {

}
