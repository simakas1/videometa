package lt.svaskevicius.videometa.mapper;

import lt.svaskevicius.videometa.security.model.TokenData;
import lt.svaskevicius.videometa.web.model.identity.AuthResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TokenDataMapper {

  AuthResponseDto toAuthResponseDto(TokenData tokenData);
}
