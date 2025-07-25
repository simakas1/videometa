package lt.svaskevicius.videometa.api.model.identity;

import jakarta.validation.constraints.NotBlank;

public record AuthRequestDto(
    @NotBlank String username,
    @NotBlank String password
) {

}
