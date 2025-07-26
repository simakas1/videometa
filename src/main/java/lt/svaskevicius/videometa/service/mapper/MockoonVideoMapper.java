package lt.svaskevicius.videometa.service.mapper;

import lt.svaskevicius.videometa.dal.model.Video;
import lt.svaskevicius.videometa.integration.mockoon.model.MockoonVideoDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MockoonVideoMapper {

  @Mapping(target = "id", ignore = true)
  Video toVideo(MockoonVideoDto source);
}
