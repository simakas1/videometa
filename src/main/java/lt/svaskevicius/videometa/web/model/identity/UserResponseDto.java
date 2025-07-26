package lt.svaskevicius.videometa.web.model.identity;

import java.util.List;
import lombok.Builder;

@Builder
public record UserResponseDto(
    String id,
    String username,
    boolean active,
    List<String> authorities
) {

}
