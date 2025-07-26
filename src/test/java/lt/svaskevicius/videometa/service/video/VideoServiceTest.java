package lt.svaskevicius.videometa.service.video;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lt.svaskevicius.videometa.dal.model.Video;
import lt.svaskevicius.videometa.dal.model.VideoStatistics;
import lt.svaskevicius.videometa.dal.repository.VideoRepository;
import lt.svaskevicius.videometa.dal.repository.VideoStatisticRepository;
import lt.svaskevicius.videometa.exception.VideoMetaException;
import lt.svaskevicius.videometa.exception.VideoMetaException.VideoMetaErrorCode;
import lt.svaskevicius.videometa.integration.mockoon.MockoonClient;
import lt.svaskevicius.videometa.integration.mockoon.model.MockoonVideoDto;
import lt.svaskevicius.videometa.service.mapper.MockoonVideoMapper;
import lt.svaskevicius.videometa.web.model.video.VideoFilterDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

  @Mock
  private VideoRepository videoRepository;

  @Mock
  private MockoonClient mockoonClient;

  @Mock
  private MockoonVideoMapper mockoonVideoMapper;

  @Mock
  private VideoStatisticRepository videoStatisticRepository;

  @InjectMocks
  private VideoService videoService;

  @Test
  @DisplayName("Should import videos from source successfully")
  void shouldImportVideosFromSourceSuccessfully() {
    // Given
    final UUID traceId = UUID.randomUUID();
    final MockoonVideoDto mockoonVideo1 = createMockoonVideo("Video 1", "source1");
    final MockoonVideoDto mockoonVideo2 = createMockoonVideo("Video 2", "source2");
    final List<MockoonVideoDto> mockoonVideos = Arrays.asList(mockoonVideo1, mockoonVideo2);

    final Video video1 = createVideo(UUID.randomUUID(), "Video 1", "source1");
    final Video video2 = createVideo(UUID.randomUUID(), "Video 2", "source2");

    when(mockoonClient.getVideos()).thenReturn(mockoonVideos);
    when(mockoonVideoMapper.toVideo(mockoonVideo1)).thenReturn(video1);
    when(mockoonVideoMapper.toVideo(mockoonVideo2)).thenReturn(video2);

    // When
    videoService.importVideoFromSource(traceId);

    // Then
    verify(mockoonClient).getVideos();
    verify(mockoonVideoMapper).toVideo(mockoonVideo1);
    verify(mockoonVideoMapper).toVideo(mockoonVideo2);
    verify(videoRepository).upsertVideo(video1);
    verify(videoRepository).upsertVideo(video2);
  }

  @Test
  @DisplayName("Should handle empty video list from source")
  void shouldHandleEmptyVideoListFromSource() {
    // Given
    final UUID traceId = UUID.randomUUID();
    when(mockoonClient.getVideos()).thenReturn(List.of());

    // When
    videoService.importVideoFromSource(traceId);

    // Then
    verify(mockoonClient).getVideos();
    verify(mockoonVideoMapper, times(0)).toVideo(any());
    verify(videoRepository, times(0)).upsertVideo(any());
  }

  @Test
  @DisplayName("Should get videos with pagination successfully when no filters applied")
  void shouldGetVideosWithPaginationSuccessfullyWhenNoFiltersApplied() {
    // Given
    final Pageable pageable = PageRequest.of(0, 10);
    final VideoFilterDto filterDto = VideoFilterDto.builder().build();
    final Video video1 = createVideo(UUID.randomUUID(), "Video 1", "source1");
    final Video video2 = createVideo(UUID.randomUUID(), "Video 2", "source2");
    final List<Video> videos = Arrays.asList(video1, video2);
    final Page<Video> videoPage = new PageImpl<>(videos, pageable, 2);

    when(videoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(videoPage);

    // When
    final Page<Video> result = videoService.getVideos(pageable, filterDto);

    // Then
    assertThat(result).isEqualTo(videoPage);
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent()).contains(video1, video2);
    assertThat(result.getTotalElements()).isEqualTo(2);
    verify(videoRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  @DisplayName("Should get videos with source filter applied")
  void shouldGetVideosWithSourceFilterApplied() {
    // Given
    final Pageable pageable = PageRequest.of(0, 10);
    final VideoFilterDto filterDto = VideoFilterDto.builder()
        .source("youtube")
        .build();
    final Video video1 = createVideo(UUID.randomUUID(), "Video 1", "youtube");
    final List<Video> videos = Arrays.asList(video1);
    final Page<Video> videoPage = new PageImpl<>(videos, pageable, 1);

    when(videoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(videoPage);

    // When
    final Page<Video> result = videoService.getVideos(pageable, filterDto);

    // Then
    assertThat(result).isEqualTo(videoPage);
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getSource()).isEqualTo("youtube");
    assertThat(result.getTotalElements()).isEqualTo(1);
    verify(videoRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  @DisplayName("Should get videos with date range filter applied")
  void shouldGetVideosWithDateRangeFilterApplied() {
    // Given
    final Pageable pageable = PageRequest.of(0, 10);
    final LocalDate fromDate = LocalDate.of(2024, 1, 1);
    final LocalDate toDate = LocalDate.of(2024, 12, 31);
    final VideoFilterDto filterDto = VideoFilterDto.builder()
        .uploadDateFrom(fromDate)
        .uploadDateTo(toDate)
        .build();
    final Video video1 = createVideo(UUID.randomUUID(), "Video 1", "source1");
    video1.setUploadDate(LocalDate.of(2024, 6, 15));
    final List<Video> videos = Arrays.asList(video1);
    final Page<Video> videoPage = new PageImpl<>(videos, pageable, 1);

    when(videoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(videoPage);

    // When
    final Page<Video> result = videoService.getVideos(pageable, filterDto);

    // Then
    assertThat(result).isEqualTo(videoPage);
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getUploadDate()).isEqualTo(LocalDate.of(2024, 6, 15));
    assertThat(result.getTotalElements()).isEqualTo(1);
    verify(videoRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  @DisplayName("Should get videos with duration range filter applied")
  void shouldGetVideosWithDurationRangeFilterApplied() {
    // Given
    final Pageable pageable = PageRequest.of(0, 10);
    final VideoFilterDto filterDto = VideoFilterDto.builder()
        .durationFrom(60)
        .durationTo(300)
        .build();
    final Video video1 = createVideo(UUID.randomUUID(), "Video 1", "source1");
    video1.setDuration(180);
    final List<Video> videos = Arrays.asList(video1);
    final Page<Video> videoPage = new PageImpl<>(videos, pageable, 1);

    when(videoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(videoPage);

    // When
    final Page<Video> result = videoService.getVideos(pageable, filterDto);

    // Then
    assertThat(result).isEqualTo(videoPage);
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getDuration()).isEqualTo(180);
    assertThat(result.getTotalElements()).isEqualTo(1);
    verify(videoRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  @DisplayName("Should get videos with all filters applied")
  void shouldGetVideosWithAllFiltersApplied() {
    // Given
    final Pageable pageable = PageRequest.of(0, 10);
    final LocalDate fromDate = LocalDate.of(2024, 1, 1);
    final LocalDate toDate = LocalDate.of(2024, 12, 31);
    final VideoFilterDto filterDto = VideoFilterDto.builder()
        .source("youtube")
        .uploadDateFrom(fromDate)
        .uploadDateTo(toDate)
        .durationFrom(60)
        .durationTo(300)
        .build();
    final Video video1 = createVideo(UUID.randomUUID(), "Video 1", "youtube");
    video1.setUploadDate(LocalDate.of(2024, 6, 15));
    video1.setDuration(180);
    final List<Video> videos = Arrays.asList(video1);
    final Page<Video> videoPage = new PageImpl<>(videos, pageable, 1);

    when(videoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(videoPage);

    // When
    final Page<Video> result = videoService.getVideos(pageable, filterDto);

    // Then
    assertThat(result).isEqualTo(videoPage);
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getSource()).isEqualTo("youtube");
    assertThat(result.getContent().get(0).getUploadDate()).isEqualTo(LocalDate.of(2024, 6, 15));
    assertThat(result.getContent().get(0).getDuration()).isEqualTo(180);
    assertThat(result.getTotalElements()).isEqualTo(1);
    verify(videoRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  @DisplayName("Should get empty page when no videos match filters")
  void shouldGetEmptyPageWhenNoVideosMatchFilters() {
    // Given
    final Pageable pageable = PageRequest.of(0, 10);
    final VideoFilterDto filterDto = VideoFilterDto.builder()
        .source("nonexistent")
        .build();
    final Page<Video> emptyPage = new PageImpl<>(List.of(), pageable, 0);

    when(videoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

    // When
    final Page<Video> result = videoService.getVideos(pageable, filterDto);

    // Then
    assertThat(result).isEqualTo(emptyPage);
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isEqualTo(0);
    verify(videoRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  @DisplayName("Should get empty page when no videos exist")
  void shouldGetEmptyPageWhenNoVideosExist() {
    // Given
    final Pageable pageable = PageRequest.of(0, 10);
    final VideoFilterDto filterDto = VideoFilterDto.builder().build();
    final Page<Video> emptyPage = new PageImpl<>(List.of(), pageable, 0);

    when(videoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

    // When
    final Page<Video> result = videoService.getVideos(pageable, filterDto);

    // Then
    assertThat(result).isEqualTo(emptyPage);
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isEqualTo(0);
    verify(videoRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  @DisplayName("Should get video by ID successfully")
  void shouldGetVideoByIdSuccessfully() {
    // Given
    final UUID videoId = UUID.randomUUID();
    final Video video = createVideo(videoId, "Test Video", "source1");

    when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));

    // When
    final Video result = videoService.getVideoById(videoId);

    // Then
    assertThat(result).isEqualTo(video);
    assertThat(result.getId()).isEqualTo(videoId);
    assertThat(result.getTitle()).isEqualTo("Test Video");
    verify(videoRepository).findById(videoId);
  }

  @Test
  @DisplayName("Should throw VideoMetaException when video not found by ID")
  void shouldThrowVideoMetaExceptionWhenVideoNotFoundById() {
    // Given
    final UUID videoId = UUID.randomUUID();

    when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> videoService.getVideoById(videoId))
        .isInstanceOf(VideoMetaException.class)
        .hasFieldOrPropertyWithValue("errorCode", VideoMetaErrorCode.NOT_FOUND)
        .hasMessage("Video not found");
    verify(videoRepository).findById(videoId);
  }

  @Test
  @DisplayName("Should get video statistics successfully")
  void shouldGetVideoStatisticsSuccessfully() {
    // Given
    final VideoStatistics stats1 = createVideoStatistics("YouTube", 100L, 300.5);
    final VideoStatistics stats2 = createVideoStatistics("Vimeo", 50L, 250.0);
    final List<VideoStatistics> statistics = Arrays.asList(stats1, stats2);

    when(videoStatisticRepository.findAllStats()).thenReturn(statistics);

    // When
    final List<VideoStatistics> result = videoService.getVideoStatistics();

    // Then
    assertThat(result).isEqualTo(statistics);
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getSource()).isEqualTo("YouTube");
    assertThat(result.get(0).getTotalVideos()).isEqualTo(100L);
    assertThat(result.get(0).getAverageDuration()).isEqualTo(300.5);
    assertThat(result.get(1).getSource()).isEqualTo("Vimeo");
    verify(videoStatisticRepository).findAllStats();
  }

  @Test
  @DisplayName("Should return empty list when no video statistics exist")
  void shouldReturnEmptyListWhenNoVideoStatisticsExist() {
    // Given
    when(videoStatisticRepository.findAllStats()).thenReturn(List.of());

    // When
    final List<VideoStatistics> result = videoService.getVideoStatistics();

    // Then
    assertThat(result).isEmpty();
    verify(videoStatisticRepository).findAllStats();
  }

  @Test
  @DisplayName("Should handle large pageable request with filters")
  void shouldHandleLargePageableRequestWithFilters() {
    // Given
    final Pageable largePage = PageRequest.of(10, 100);
    final VideoFilterDto filterDto = VideoFilterDto.builder()
        .source("youtube")
        .build();
    final Page<Video> emptyPage = new PageImpl<>(List.of(), largePage, 0);

    when(videoRepository.findAll(any(Specification.class), eq(largePage))).thenReturn(emptyPage);

    // When
    final Page<Video> result = videoService.getVideos(largePage, filterDto);

    // Then
    assertThat(result).isEqualTo(emptyPage);
    assertThat(result.getNumber()).isEqualTo(10);
    assertThat(result.getSize()).isEqualTo(100);
    verify(videoRepository).findAll(any(Specification.class), eq(largePage));
  }

  @Test
  @DisplayName("Should handle filter with only duration from")
  void shouldHandleFilterWithOnlyDurationFrom() {
    // Given
    final Pageable pageable = PageRequest.of(0, 10);
    final VideoFilterDto filterDto = VideoFilterDto.builder()
        .durationFrom(120)
        .build();
    final Video video1 = createVideo(UUID.randomUUID(), "Long Video", "source1");
    video1.setDuration(300);
    final List<Video> videos = Arrays.asList(video1);
    final Page<Video> videoPage = new PageImpl<>(videos, pageable, 1);

    when(videoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(videoPage);

    // When
    final Page<Video> result = videoService.getVideos(pageable, filterDto);

    // Then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getDuration()).isEqualTo(300);
    verify(videoRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  @DisplayName("Should handle filter with only upload date from")
  void shouldHandleFilterWithOnlyUploadDateFrom() {
    // Given
    final Pageable pageable = PageRequest.of(0, 10);
    final LocalDate fromDate = LocalDate.of(2024, 6, 1);
    final VideoFilterDto filterDto = VideoFilterDto.builder()
        .uploadDateFrom(fromDate)
        .build();
    final Video video1 = createVideo(UUID.randomUUID(), "Recent Video", "source1");
    video1.setUploadDate(LocalDate.of(2024, 8, 15));
    final List<Video> videos = Arrays.asList(video1);
    final Page<Video> videoPage = new PageImpl<>(videos, pageable, 1);

    when(videoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(videoPage);

    // When
    final Page<Video> result = videoService.getVideos(pageable, filterDto);

    // Then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getUploadDate()).isEqualTo(LocalDate.of(2024, 8, 15));
    verify(videoRepository).findAll(any(Specification.class), eq(pageable));
  }

  private MockoonVideoDto createMockoonVideo(final String title, final String source) {
    return MockoonVideoDto.builder()
        .title(title)
        .source(source)
        .url("http://example.com/video")
        .duration(180)
        .uploadDate(LocalDate.now())
        .build();
  }

  private Video createVideo(final UUID id, final String title, final String source) {
    final Video video = new Video();
    video.setId(id);
    video.setTitle(title);
    video.setSource(source);
    video.setUrl("http://example.com/video");
    video.setDuration(180);
    video.setUploadDate(LocalDate.now());
    return video;
  }

  private VideoStatistics createVideoStatistics(final String source, final Long totalVideos,
      final Double averageDuration) {
    final VideoStatistics stats = new VideoStatistics();
    stats.setSource(source);
    stats.setTotalVideos(totalVideos);
    stats.setAverageDuration(averageDuration);
    return stats;
  }
}