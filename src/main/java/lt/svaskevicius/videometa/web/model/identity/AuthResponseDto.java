package lt.svaskevicius.videometa.web.model.identity;

import java.util.Date;
import lombok.Builder;

@Builder
public record AuthResponseDto(
    String token,
    Date expiresAt,
    Date issuedAt
) {

}
