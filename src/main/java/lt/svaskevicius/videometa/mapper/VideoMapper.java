package lt.svaskevicius.videometa.mapper;

import java.util.List;
import lt.svaskevicius.videometa.dal.model.Video;
import lt.svaskevicius.videometa.web.model.video.VideoPageResponseDto;
import lt.svaskevicius.videometa.web.model.video.VideoResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.data.domain.Page;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VideoMapper {

  @Mapping(target = "id", expression = "java(video.getId().toString())")
  VideoResponseDto toVideoResponseDto(Video video);

  List<VideoResponseDto> toVideoResponseDtoList(List<Video> videos);

  default VideoPageResponseDto toVideoPageResponseDto(final Page<Video> videoPage) {
    return new VideoPageResponseDto(
        videoPage.getNumber(),
        videoPage.getSize(),
        videoPage.getTotalElements(),
        videoPage.getTotalPages(),
        toVideoResponseDtoList(videoPage.getContent())
    );
  }
}