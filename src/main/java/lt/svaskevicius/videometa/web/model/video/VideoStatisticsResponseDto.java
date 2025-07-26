package lt.svaskevicius.videometa.web.model.video;

import java.io.Serializable;

public record VideoStatisticsResponseDto(
    String source,
    Long totalVideos,
    Double averageDuration
) implements Serializable {

}
