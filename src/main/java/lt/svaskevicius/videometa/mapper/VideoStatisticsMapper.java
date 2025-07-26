package lt.svaskevicius.videometa.mapper;

import java.util.List;
import lt.svaskevicius.videometa.dal.model.VideoStatistics;
import lt.svaskevicius.videometa.web.model.video.VideoStatisticsResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VideoStatisticsMapper {

  VideoStatisticsResponseDto toVideoStatisticsResponseDto(VideoStatistics videoStatistics);

  List<VideoStatisticsResponseDto> toVideoStatisticsResponseDto(List<VideoStatistics> videoStatistics);
}
