package lt.svaskevicius.videometa.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lt.svaskevicius.videometa.web.model.SortDirection;
import lt.svaskevicius.videometa.web.model.video.VideoFilterDto;
import lt.svaskevicius.videometa.web.model.video.VideoPageResponseDto;
import lt.svaskevicius.videometa.web.model.video.VideoResponseDto;
import lt.svaskevicius.videometa.web.model.video.VideoStatisticsResponseDto;
import lt.svaskevicius.videometa.web.service.VideoApiService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/videos")
public class VideoController {

  private final VideoApiService videoApiService;

  public VideoController(final VideoApiService videoApiService) {
    this.videoApiService = videoApiService;
  }

  @PostMapping("/import")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyAuthority('ADMIN', 'VIDEO_IMPORTER')")
  public void importVideo() {
    videoApiService.importVideos();
  }

  @GetMapping
  public VideoPageResponseDto getVideos(
      @RequestParam(defaultValue = "1") final int page,
      @RequestParam(defaultValue = "10") final int size,
      @RequestParam(defaultValue = "id") final String sortBy,
      @RequestParam(defaultValue = "ASC") final SortDirection sortDirection,
      @RequestParam(required = false) final String source,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate uploadDateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate uploadDateTo,
      @RequestParam(required = false) final Integer durationFrom,
      @RequestParam(required = false) final Integer durationTo) {
    final VideoFilterDto filterDto = VideoFilterDto.builder()
        .source(source)
        .uploadDateFrom(uploadDateFrom)
        .uploadDateTo(uploadDateTo)
        .durationFrom(durationFrom)
        .durationTo(durationTo)
        .build();
    return videoApiService.getVideos(page, size, sortBy, sortDirection, filterDto);
  }

  @GetMapping("/{id}")
  public VideoResponseDto getVideoById(@PathVariable final UUID id) {
    return videoApiService.getVideoById(id);
  }

  @GetMapping("/stats")
  @PreAuthorize("hasAnyAuthority('ADMIN', 'VIDEO_ANALYTICS')")
  public List<VideoStatisticsResponseDto> getVideoStats() {
    return videoApiService.getVideoStatistics();
  }
}
