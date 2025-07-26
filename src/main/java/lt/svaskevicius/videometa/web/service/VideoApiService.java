package lt.svaskevicius.videometa.web.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.svaskevicius.videometa.mapper.VideoMapper;
import lt.svaskevicius.videometa.mapper.VideoStatisticsMapper;
import lt.svaskevicius.videometa.service.identity.CurrentUserService;
import lt.svaskevicius.videometa.service.video.VideoImportProducerService;
import lt.svaskevicius.videometa.service.video.VideoService;
import lt.svaskevicius.videometa.web.model.SortDirection;
import lt.svaskevicius.videometa.web.model.video.VideoFilterDto;
import lt.svaskevicius.videometa.web.model.video.VideoPageResponseDto;
import lt.svaskevicius.videometa.web.model.video.VideoResponseDto;
import lt.svaskevicius.videometa.web.model.video.VideoStatisticsResponseDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoApiService {

  private final VideoImportProducerService videoImportProducerService;
  private final VideoService videoService;
  private final VideoMapper videoMapper;
  private final VideoStatisticsMapper videoStatisticsMapper;
  private final CurrentUserService currentUserService;

  public void importVideos() {
    final UUID initiatorUserId = currentUserService.getCurrentUserId();
    final UUID traceId = UUID.randomUUID();
    videoImportProducerService.sendToQueue(traceId);

    log.info("Video import initiated by user with ID: {}, traceId: {}", initiatorUserId, traceId);
  }

  public VideoPageResponseDto getVideos(final int page, final int size,
      final String sortBy, final SortDirection sortDirection, final VideoFilterDto filter) {

    final Sort sort = SortDirection.DESC.equals(sortDirection) ?
        Sort.by(sortBy).descending() :
        Sort.by(sortBy).ascending();

    final Pageable pageable = PageRequest.of(page, size, sort);

    return videoMapper.toVideoPageResponseDto(videoService.getVideos(pageable, filter));
  }

  public VideoResponseDto getVideoById(final UUID id) {
    return videoMapper.toVideoResponseDto(videoService.getVideoById(id));
  }

  @Cacheable(value = "video_statistics", unless = "#result.isEmpty()")
  public List<VideoStatisticsResponseDto> getVideoStatistics() {
    return videoStatisticsMapper.toVideoStatisticsResponseDto(videoService.getVideoStatistics());
  }
}
