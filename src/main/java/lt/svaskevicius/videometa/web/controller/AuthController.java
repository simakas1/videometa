package lt.svaskevicius.videometa.web.controller;

import lombok.RequiredArgsConstructor;
import lt.svaskevicius.videometa.web.model.identity.AuthRequestDto;
import lt.svaskevicius.videometa.web.model.identity.AuthResponseDto;
import lt.svaskevicius.videometa.web.model.identity.UserResponseDto;
import lt.svaskevicius.videometa.web.service.AuthApiService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthApiService authApiService;

  @PostMapping("/login")
  public AuthResponseDto authenticate(@Validated @RequestBody final AuthRequestDto authenticationRequest) {
    return authApiService.authenticate(authenticationRequest.username(), authenticationRequest.password());
  }

  @GetMapping("/whoami")
  public UserResponseDto whoAmI() {
    return authApiService.getCurrentUser();
  }
}
