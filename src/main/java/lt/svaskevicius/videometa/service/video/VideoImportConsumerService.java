package lt.svaskevicius.videometa.service.video;

import java.util.UUID;
import lt.svaskevicius.videometa.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class VideoImportConsumerService {

  private final VideoService videoService;

  public VideoImportConsumerService(final VideoService videoService) {
    this.videoService = videoService;
  }

  @RabbitListener(queues = RabbitMqConfig.VIDEO_IMPORT_QUEUE)
  public void consume(final UUID traceId) {
    videoService.importVideoFromSource(traceId);
  }
}
