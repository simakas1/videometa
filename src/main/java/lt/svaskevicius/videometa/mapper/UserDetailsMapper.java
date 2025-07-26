package lt.svaskevicius.videometa.mapper;

import lt.svaskevicius.videometa.service.model.CurrentUserDetails;
import lt.svaskevicius.videometa.web.model.identity.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserDetailsMapper {

  @Mappings({
      @Mapping(source = "enabled", target = "active"),
      @Mapping(target = "authorities", expression = "java(currentUserDetails.getAuthorities().stream().map(a -> a.getAuthority()).toList())")
  })
  UserResponseDto toUserResponseDto(CurrentUserDetails currentUserDetails);
}
