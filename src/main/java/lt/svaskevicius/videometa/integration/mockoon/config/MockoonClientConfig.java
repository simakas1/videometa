package lt.svaskevicius.videometa.integration.mockoon.config;

import java.time.Duration;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MockoonClientConfig {

  @Bean
  public RestClient mockoonRestClient(final RestClient.Builder builder,
      final MockoonClientConfigProperties mockoonClientConfigProperties) {
    final var requestFactory = ClientHttpRequestFactorySettings.defaults().withConnectTimeout(Duration.ofSeconds(30))
        .withReadTimeout(Duration.ofSeconds(30));

    return builder.baseUrl(mockoonClientConfigProperties.baseUrl())
        .requestFactory(ClientHttpRequestFactoryBuilder.detect().build(requestFactory))
        .build();
  }
}
