package lt.svaskevicius.videometa.web.model.identity;

import jakarta.validation.constraints.NotBlank;

public record AuthRequestDto(
    @NotBlank String username,
    @NotBlank String password
) {

}
