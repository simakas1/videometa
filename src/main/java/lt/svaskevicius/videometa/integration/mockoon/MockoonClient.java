package lt.svaskevicius.videometa.integration.mockoon;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import lt.svaskevicius.videometa.integration.mockoon.model.MockoonVideoDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j(topic = "MockoonClient")
@Component
public class MockoonClient {

  private static final String ENDPOINT_VIDEOS = "/videos";

  private final CircuitBreaker circuitBreaker;
  private final RestClient restClient;

  public MockoonClient(final CircuitBreaker mockoonCircuitBreaker, final RestClient mockoonRestClient) {
    this.circuitBreaker = mockoonCircuitBreaker;
    this.restClient = mockoonRestClient;
  }

  public List<MockoonVideoDto> getVideos() {
    try {
      return circuitBreaker.executeSupplier(() -> Arrays.stream(Objects.requireNonNull(restClient.get()
              .uri(UriComponentsBuilder.fromPath(ENDPOINT_VIDEOS).toUriString())
              .retrieve()
              .body(MockoonVideoDto[].class)))
          .toList());
    } catch (final RestClientException e) {
      log.error("Failed to fetch videos from Mockoon", e);
      throw new RuntimeException("Failed to fetch videos from Mockoon", e);
    }
  }
}
