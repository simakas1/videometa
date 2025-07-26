package lt.svaskevicius.videometa.service.video;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.svaskevicius.videometa.dal.model.Video;
import lt.svaskevicius.videometa.dal.model.VideoStatistics;
import lt.svaskevicius.videometa.dal.repository.VideoRepository;
import lt.svaskevicius.videometa.dal.repository.VideoStatisticRepository;
import lt.svaskevicius.videometa.exception.VideoMetaException;
import lt.svaskevicius.videometa.exception.VideoMetaException.VideoMetaErrorCode;
import lt.svaskevicius.videometa.integration.mockoon.MockoonClient;
import lt.svaskevicius.videometa.service.mapper.MockoonVideoMapper;
import lt.svaskevicius.videometa.web.model.video.VideoFilterDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

  private final VideoRepository videoRepository;
  private final MockoonClient mockoonClient;
  private final MockoonVideoMapper mockoonVideoMapper;
  private final VideoStatisticRepository videoStatisticRepository;

  @CacheEvict(value = "video_statistics", allEntries = true)
  public void importVideoFromSource(final UUID traceId) {
    mockoonClient.getVideos().stream()
        .map(mockoonVideoMapper::toVideo)
        .forEach(videoRepository::upsertVideo);

    log.info("Imported video from source, traceId: {}", traceId);
  }

  public Page<Video> getVideos(final Pageable pageable, final VideoFilterDto filterDto) {
    final Specification<Video> specification = VideoSpecificationService.buildSpecification(filterDto);
    return videoRepository.findAll(specification, pageable);
  }

  public Video getVideoById(final UUID id) {
    return videoRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Could not find video with id: {}", id);
          return new VideoMetaException(VideoMetaErrorCode.NOT_FOUND, "Video not found");
        });
  }

  public List<VideoStatistics> getVideoStatistics() {
    return videoStatisticRepository.findAllStats();
  }
}
