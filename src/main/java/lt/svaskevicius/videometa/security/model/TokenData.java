package lt.svaskevicius.videometa.security.model;

import java.util.Date;
import lombok.Builder;

@Builder
public record TokenData(
    String token,
    Date issuedAt,
    Date expiresAt
) {

}
