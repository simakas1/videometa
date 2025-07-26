package lt.svaskevicius.videometa.integration.mockoon.model;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record MockoonVideoDto(
    String id,
    String title,
    String source,
    String url,
    int duration,
    LocalDate uploadDate
) {

}
