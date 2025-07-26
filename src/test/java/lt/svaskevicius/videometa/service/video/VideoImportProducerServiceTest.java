package lt.svaskevicius.videometa.service.video;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import lt.svaskevicius.videometa.config.RabbitMqConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class VideoImportProducerServiceTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  @InjectMocks
  private VideoImportProducerService videoImportProducerService;

  @Test
  @DisplayName("Should send message to video import queue successfully")
  void shouldSendMessageToVideoImportQueueSuccessfully() {
    // Given
    final UUID traceId = UUID.randomUUID();

    // When
    videoImportProducerService.sendToQueue(traceId);

    // Then
    verify(rabbitTemplate).convertAndSend(
        RabbitMqConfig.VIDEO_IMPORT_QUEUE,
        traceId
    );
  }

  @Test
  @DisplayName("Should handle multiple queue sends")
  void shouldHandleMultipleQueueSends() {
    // Given
    final UUID traceId1 = UUID.randomUUID();
    final UUID traceId2 = UUID.randomUUID();
    final UUID traceId3 = UUID.randomUUID();

    // When
    videoImportProducerService.sendToQueue(traceId1);
    videoImportProducerService.sendToQueue(traceId2);
    videoImportProducerService.sendToQueue(traceId3);

    // Then
    verify(rabbitTemplate).convertAndSend(RabbitMqConfig.VIDEO_IMPORT_QUEUE, traceId1);
    verify(rabbitTemplate).convertAndSend(RabbitMqConfig.VIDEO_IMPORT_QUEUE, traceId2);
    verify(rabbitTemplate).convertAndSend(RabbitMqConfig.VIDEO_IMPORT_QUEUE, traceId3);
  }

  @Test
  @DisplayName("Should send null trace ID to queue")
  void shouldSendNullTraceIdToQueue() {
    // Given
    final UUID traceId = null;

    // When
    videoImportProducerService.sendToQueue(traceId);

    // Then
    verify(rabbitTemplate).convertAndSend(
        RabbitMqConfig.VIDEO_IMPORT_QUEUE,
        traceId
    );
  }
}