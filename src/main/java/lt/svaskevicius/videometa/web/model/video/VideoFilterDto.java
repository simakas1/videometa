package lt.svaskevicius.videometa.web.model.video;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record VideoFilterDto(
    String source,
    LocalDate uploadDateFrom,
    LocalDate uploadDateTo,
    Integer durationFrom,
    Integer durationTo
) {

}
