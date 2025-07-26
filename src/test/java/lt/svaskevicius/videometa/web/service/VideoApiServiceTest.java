package lt.svaskevicius.videometa.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lt.svaskevicius.videometa.dal.model.Video;
import lt.svaskevicius.videometa.dal.model.VideoStatistics;
import lt.svaskevicius.videometa.exception.VideoMetaException;
import lt.svaskevicius.videometa.exception.VideoMetaException.VideoMetaErrorCode;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class VideoApiServiceTest {

  @Mock
  private VideoImportProducerService videoImportProducerService;

  @Mock
  private VideoService videoService;

  @Mock
  private VideoMapper videoMapper;

  @Mock
  private VideoStatisticsMapper videoStatisticsMapper;

  @Mock
  private CurrentUserService currentUserService;

  @InjectMocks
  private VideoApiService videoApiService;

  @Captor
  private ArgumentCaptor<VideoFilterDto> filterCaptor;

  @Test
  @DisplayName("Should import videos successfully")
  void shouldImportVideosSuccessfully() {
    // Given
    final UUID currentUserId = UUID.randomUUID();
    when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);

    // When
    videoApiService.importVideos();

    // Then
    verify(currentUserService).getCurrentUserId();
    verify(videoImportProducerService).sendToQueue(any(UUID.class));
  }

  @Test
  @DisplayName("Should get videos with source filter applied")
  void shouldGetVideosWithSourceFilterApplied() {
    // Given
    final int page = 0;
    final int size = 10;
    final String sortBy = "title";
    final SortDirection sortDirection = SortDirection.ASC;
    final String source = "youtube";

    final Video video1 = createVideo(UUID.randomUUID(), "YouTube Video", "youtube");
    final List<Video> videos = List.of(video1);
    final Pageable expectedPageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
    final Page<Video> videoPage = new PageImpl<>(videos, expectedPageable, 1);

    final VideoResponseDto dto1 = createVideoResponseDto(video1.getId(), "YouTube Video");
    final VideoPageResponseDto expectedResponse = createVideoPageResponseDto(List.of(dto1), 1);

    when(videoService.getVideos(eq(expectedPageable), any(VideoFilterDto.class))).thenReturn(videoPage);
    when(videoMapper.toVideoPageResponseDto(videoPage)).thenReturn(expectedResponse);

    final VideoFilterDto filter = VideoFilterDto.builder()
        .source(source)
        .build();

    // When
    final VideoPageResponseDto result = videoApiService.getVideos(page, size, sortBy, sortDirection, filter);

    // Then
    assertThat(result).isEqualTo(expectedResponse);
    verify(videoService).getVideos(eq(expectedPageable), filterCaptor.capture());
    verify(videoMapper).toVideoPageResponseDto(videoPage);

    final VideoFilterDto capturedFilter = filterCaptor.getValue();
    assertThat(capturedFilter.source()).isEqualTo("youtube");
    assertThat(capturedFilter.uploadDateFrom()).isNull();
    assertThat(capturedFilter.uploadDateTo()).isNull();
    assertThat(capturedFilter.durationFrom()).isNull();
    assertThat(capturedFilter.durationTo()).isNull();
  }

  @Test
  @DisplayName("Should get videos with date range filter applied")
  void shouldGetVideosWithDateRangeFilterApplied() {
    // Given
    final int page = 0;
    final int size = 10;
    final String sortBy = "uploadDate";
    final SortDirection sortDirection = SortDirection.DESC;
    final LocalDate uploadDateFrom = LocalDate.of(2024, 1, 1);
    final LocalDate uploadDateTo = LocalDate.of(2024, 12, 31);

    final Video video1 = createVideo(UUID.randomUUID(), "2024 Video", "source1");
    video1.setUploadDate(LocalDate.of(2024, 6, 15));
    final List<Video> videos = List.of(video1);
    final Pageable expectedPageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
    final Page<Video> videoPage = new PageImpl<>(videos, expectedPageable, 1);

    final VideoResponseDto dto1 = createVideoResponseDto(video1.getId(), "2024 Video");
    final VideoPageResponseDto expectedResponse = createVideoPageResponseDto(List.of(dto1), 1);

    when(videoService.getVideos(eq(expectedPageable), any(VideoFilterDto.class))).thenReturn(videoPage);
    when(videoMapper.toVideoPageResponseDto(videoPage)).thenReturn(expectedResponse);

    final VideoFilterDto filter = VideoFilterDto.builder()
        .uploadDateFrom(uploadDateFrom)
        .uploadDateTo(uploadDateTo)
        .build();

    // When
    final VideoPageResponseDto result = videoApiService.getVideos(page, size, sortBy, sortDirection, filter);

    // Then
    assertThat(result).isEqualTo(expectedResponse);
    verify(videoService).getVideos(eq(expectedPageable), filterCaptor.capture());
    verify(videoMapper).toVideoPageResponseDto(videoPage);

    final VideoFilterDto capturedFilter = filterCaptor.getValue();
    assertThat(capturedFilter.source()).isNull();
    assertThat(capturedFilter.uploadDateFrom()).isEqualTo(uploadDateFrom);
    assertThat(capturedFilter.uploadDateTo()).isEqualTo(uploadDateTo);
    assertThat(capturedFilter.durationFrom()).isNull();
    assertThat(capturedFilter.durationTo()).isNull();
  }

  @Test
  @DisplayName("Should get videos with duration range filter applied")
  void shouldGetVideosWithDurationRangeFilterApplied() {
    // Given
    final int page = 0;
    final int size = 10;
    final String sortBy = "duration";
    final SortDirection sortDirection = SortDirection.ASC;
    final Integer durationFrom = 60;
    final Integer durationTo = 300;

    final Video video1 = createVideo(UUID.randomUUID(), "Short Video", "source1");
    video1.setDuration(180);
    final List<Video> videos = List.of(video1);
    final Pageable expectedPageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
    final Page<Video> videoPage = new PageImpl<>(videos, expectedPageable, 1);

    final VideoResponseDto dto1 = createVideoResponseDto(video1.getId(), "Short Video");
    final VideoPageResponseDto expectedResponse = createVideoPageResponseDto(List.of(dto1), 1);

    when(videoService.getVideos(eq(expectedPageable), any(VideoFilterDto.class))).thenReturn(videoPage);
    when(videoMapper.toVideoPageResponseDto(videoPage)).thenReturn(expectedResponse);

    final VideoFilterDto filter = VideoFilterDto.builder()
        .durationFrom(durationFrom)
        .durationTo(durationTo)
        .build();

    // When
    final VideoPageResponseDto result = videoApiService.getVideos(page, size, sortBy, sortDirection, filter);

    // Then
    assertThat(result).isEqualTo(expectedResponse);
    verify(videoService).getVideos(eq(expectedPageable), filterCaptor.capture());
    verify(videoMapper).toVideoPageResponseDto(videoPage);

    final VideoFilterDto capturedFilter = filterCaptor.getValue();
    assertThat(capturedFilter.source()).isNull();
    assertThat(capturedFilter.uploadDateFrom()).isNull();
    assertThat(capturedFilter.uploadDateTo()).isNull();
    assertThat(capturedFilter.durationFrom()).isEqualTo(durationFrom);
    assertThat(capturedFilter.durationTo()).isEqualTo(durationTo);
  }

  @Test
  @DisplayName("Should get videos with all filters applied")
  void shouldGetVideosWithAllFiltersApplied() {
    // Given
    final int page = 0;
    final int size = 10;
    final String sortBy = "title";
    final SortDirection sortDirection = SortDirection.ASC;
    final String source = "youtube";
    final LocalDate uploadDateFrom = LocalDate.of(2024, 1, 1);
    final LocalDate uploadDateTo = LocalDate.of(2024, 12, 31);
    final Integer durationFrom = 60;
    final Integer durationTo = 300;

    final Video video1 = createVideo(UUID.randomUUID(), "Filtered Video", "youtube");
    video1.setUploadDate(LocalDate.of(2024, 6, 15));
    video1.setDuration(180);
    final List<Video> videos = List.of(video1);
    final Pageable expectedPageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
    final Page<Video> videoPage = new PageImpl<>(videos, expectedPageable, 1);

    final VideoResponseDto dto1 = createVideoResponseDto(video1.getId(), "Filtered Video");
    final VideoPageResponseDto expectedResponse = createVideoPageResponseDto(List.of(dto1), 1);

    when(videoService.getVideos(eq(expectedPageable), any(VideoFilterDto.class))).thenReturn(videoPage);
    when(videoMapper.toVideoPageResponseDto(videoPage)).thenReturn(expectedResponse);

    final VideoFilterDto filter = VideoFilterDto.builder()
        .source(source)
        .uploadDateFrom(uploadDateFrom)
        .uploadDateTo(uploadDateTo)
        .durationFrom(durationFrom)
        .durationTo(durationTo)
        .build();

    // When
    final VideoPageResponseDto result = videoApiService.getVideos(page, size, sortBy, sortDirection, filter);

    // Then
    assertThat(result).isEqualTo(expectedResponse);
    verify(videoService).getVideos(eq(expectedPageable), filterCaptor.capture());
    verify(videoMapper).toVideoPageResponseDto(videoPage);

    final VideoFilterDto capturedFilter = filterCaptor.getValue();
    assertThat(capturedFilter.source()).isEqualTo(source);
    assertThat(capturedFilter.uploadDateFrom()).isEqualTo(uploadDateFrom);
    assertThat(capturedFilter.uploadDateTo()).isEqualTo(uploadDateTo);
    assertThat(capturedFilter.durationFrom()).isEqualTo(durationFrom);
    assertThat(capturedFilter.durationTo()).isEqualTo(durationTo);
  }

  @Test
  @DisplayName("Should get video by ID successfully")
  void shouldGetVideoByIdSuccessfully() {
    // Given
    final UUID videoId = UUID.randomUUID();
    final Video video = createVideo(videoId, "Test Video", "source1");
    final VideoResponseDto expectedDto = createVideoResponseDto(videoId, "Test Video");

    when(videoService.getVideoById(videoId)).thenReturn(video);
    when(videoMapper.toVideoResponseDto(video)).thenReturn(expectedDto);

    // When
    final VideoResponseDto result = videoApiService.getVideoById(videoId);

    // Then
    assertThat(result).isEqualTo(expectedDto);
    verify(videoService).getVideoById(videoId);
    verify(videoMapper).toVideoResponseDto(video);
  }

  @Test
  @DisplayName("Should throw exception when video not found by ID")
  void shouldThrowExceptionWhenVideoNotFoundById() {
    // Given
    final UUID videoId = UUID.randomUUID();

    when(videoService.getVideoById(videoId))
        .thenThrow(new VideoMetaException(VideoMetaErrorCode.NOT_FOUND, "Video not found"));

    // When / Then
    assertThatThrownBy(() -> videoApiService.getVideoById(videoId))
        .isInstanceOf(VideoMetaException.class)
        .hasFieldOrPropertyWithValue("errorCode", VideoMetaErrorCode.NOT_FOUND);
    verify(videoService).getVideoById(videoId);
  }

  @Test
  @DisplayName("Should get video statistics successfully")
  void shouldGetVideoStatisticsSuccessfully() {
    // Given
    final VideoStatistics stats1 = createVideoStatistics("YouTube", 100L, 300.5);
    final VideoStatistics stats2 = createVideoStatistics("Vimeo", 50L, 250.0);
    final List<VideoStatistics> statistics = Arrays.asList(stats1, stats2);

    final VideoStatisticsResponseDto dto1 = createVideoStatisticsResponseDto("YouTube", 100L, 300.5);
    final VideoStatisticsResponseDto dto2 = createVideoStatisticsResponseDto("Vimeo", 50L, 250.0);
    final List<VideoStatisticsResponseDto> expectedDtos = Arrays.asList(dto1, dto2);

    when(videoService.getVideoStatistics()).thenReturn(statistics);
    when(videoStatisticsMapper.toVideoStatisticsResponseDto(statistics)).thenReturn(expectedDtos);

    // When
    final List<VideoStatisticsResponseDto> result = videoApiService.getVideoStatistics();

    // Then
    assertThat(result).isEqualTo(expectedDtos);
    assertThat(result).hasSize(2);
    assertThat(result.get(0).source()).isEqualTo("YouTube");
    assertThat(result.get(1).source()).isEqualTo("Vimeo");
    verify(videoService).getVideoStatistics();
    verify(videoStatisticsMapper).toVideoStatisticsResponseDto(statistics);
  }

  @Test
  @DisplayName("Should return empty list when no video statistics exist")
  void shouldReturnEmptyListWhenNoVideoStatisticsExist() {
    // Given
    final List<VideoStatistics> emptyStatistics = List.of();
    final List<VideoStatisticsResponseDto> emptyDtos = List.of();

    when(videoService.getVideoStatistics()).thenReturn(emptyStatistics);
    when(videoStatisticsMapper.toVideoStatisticsResponseDto(emptyStatistics)).thenReturn(emptyDtos);

    // When
    final List<VideoStatisticsResponseDto> result = videoApiService.getVideoStatistics();

    // Then
    assertThat(result).isEmpty();
    verify(videoService).getVideoStatistics();
    verify(videoStatisticsMapper).toVideoStatisticsResponseDto(emptyStatistics);
  }

  @Test
  @DisplayName("Should handle partial filters")
  void shouldHandlePartialFilters() {
    // Given
    final int page = 0;
    final int size = 10;
    final String sortBy = "uploadDate";
    final SortDirection sortDirection = SortDirection.DESC;
    final LocalDate uploadDateFrom = LocalDate.of(2024, 6, 1);
    final Integer durationFrom = 120;

    final Video video = createVideo(UUID.randomUUID(), "Partial Filter Video", "source1");
    final List<Video> videos = List.of(video);
    final Pageable expectedPageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
    final Page<Video> videoPage = new PageImpl<>(videos, expectedPageable, 1);

    final VideoResponseDto dto = createVideoResponseDto(video.getId(), "Partial Filter Video");
    final VideoPageResponseDto expectedResponse = createVideoPageResponseDto(List.of(dto), 1);

    when(videoService.getVideos(eq(expectedPageable), any(VideoFilterDto.class))).thenReturn(videoPage);
    when(videoMapper.toVideoPageResponseDto(videoPage)).thenReturn(expectedResponse);

    final VideoFilterDto filter = VideoFilterDto.builder()
        .uploadDateFrom(uploadDateFrom)
        .durationFrom(durationFrom)
        .build();

    // When
    final VideoPageResponseDto result = videoApiService.getVideos(page, size, sortBy, sortDirection, filter);

    // Then
    assertThat(result).isEqualTo(expectedResponse);
    verify(videoService).getVideos(eq(expectedPageable), filterCaptor.capture());
    verify(videoMapper).toVideoPageResponseDto(videoPage);

    final VideoFilterDto capturedFilter = filterCaptor.getValue();
    assertThat(capturedFilter.source()).isNull();
    assertThat(capturedFilter.uploadDateFrom()).isEqualTo(uploadDateFrom);
    assertThat(capturedFilter.uploadDateTo()).isNull();
    assertThat(capturedFilter.durationFrom()).isEqualTo(durationFrom);
    assertThat(capturedFilter.durationTo()).isNull();
  }

  private Video createVideo(final UUID id, final String title, final String source) {
    final Video video = new Video();
    video.setId(id);
    video.setTitle(title);
    video.setSource(source);
    video.setUrl("http://example.com/video");
    video.setDuration(180);
    video.setUploadDate(LocalDate.now());
    video.setCreatedAt(LocalDateTime.now());
    video.setUpdatedAt(LocalDateTime.now());
    return video;
  }

  private VideoResponseDto createVideoResponseDto(final UUID id, final String title) {
    return new VideoResponseDto(
        id.toString(),
        title,
        "source1",
        "http://example.com/video",
        180
    );
  }

  private VideoPageResponseDto createVideoPageResponseDto(final List<VideoResponseDto> videos,
      final long totalElements) {
    return new VideoPageResponseDto(0, 10, totalElements, 1, videos);
  }

  private VideoStatistics createVideoStatistics(final String source, final Long totalVideos,
      final Double averageDuration) {
    final VideoStatistics stats = new VideoStatistics();
    stats.setSource(source);
    stats.setTotalVideos(totalVideos);
    stats.setAverageDuration(averageDuration);
    return stats;
  }

  private VideoStatisticsResponseDto createVideoStatisticsResponseDto(final String source, final Long totalVideos,
      final Double averageDuration) {
    return new VideoStatisticsResponseDto(source, totalVideos, averageDuration);
  }
}