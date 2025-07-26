package lt.svaskevicius.videometa.web.model.video;

public record VideoResponseDto(
    String id,
    String title,
    String source,
    String url,
    int duration
) {

}
