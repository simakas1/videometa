package lt.svaskevicius.videometa.service.video;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VideoImportConsumerServiceTest {

  @Mock
  private VideoService videoService;

  @InjectMocks
  private VideoImportConsumerService videoImportConsumerService;

  @Test
  @DisplayName("Should consume message and call video service successfully")
  void shouldConsumeMessageAndCallVideoServiceSuccessfully() {
    // Given
    final UUID traceId = UUID.randomUUID();

    // When
    videoImportConsumerService.consume(traceId);

    // Then
    verify(videoService).importVideoFromSource(traceId);
  }

  @Test
  @DisplayName("Should consume message with specific UUID successfully")
  void shouldConsumeMessageWithSpecificUuidSuccessfully() {
    // Given
    final UUID traceId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    // When
    videoImportConsumerService.consume(traceId);

    // Then
    verify(videoService).importVideoFromSource(traceId);
  }

  @Test
  @DisplayName("Should handle multiple message consumption")
  void shouldHandleMultipleMessageConsumption() {
    // Given
    final UUID traceId1 = UUID.randomUUID();
    final UUID traceId2 = UUID.randomUUID();
    final UUID traceId3 = UUID.randomUUID();

    // When
    videoImportConsumerService.consume(traceId1);
    videoImportConsumerService.consume(traceId2);
    videoImportConsumerService.consume(traceId3);

    // Then
    verify(videoService).importVideoFromSource(traceId1);
    verify(videoService).importVideoFromSource(traceId2);
    verify(videoService).importVideoFromSource(traceId3);
  }

  @Test
  @DisplayName("Should handle null trace ID consumption")
  void shouldHandleNullTraceIdConsumption() {
    // Given
    final UUID traceId = null;

    // When
    videoImportConsumerService.consume(traceId);

    // Then
    verify(videoService).importVideoFromSource(traceId);
  }
}