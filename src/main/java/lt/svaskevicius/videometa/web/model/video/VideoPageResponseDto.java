package lt.svaskevicius.videometa.web.model.video;

import java.util.List;

public record VideoPageResponseDto(
    int page,
    int size,
    long totalElements,
    int totalPages,
    List<VideoResponseDto> content
) {

}
