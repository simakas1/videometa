package lt.svaskevicius.videometa.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record SecurityProperties(
    @NotNull String secretKey,
    @NotNull String issuer,
    long expirationTimeInMillis
) {

}
