package lt.svaskevicius.videometa.service.video;

import java.util.UUID;
import lt.svaskevicius.videometa.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class VideoImportProducerService {

  private final RabbitTemplate rabbitTemplate;

  public VideoImportProducerService(final RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void sendToQueue(final UUID traceId) {
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.VIDEO_IMPORT_QUEUE,
        traceId
    );
  }
}
