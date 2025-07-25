package lt.svaskevicius.videometa.api.controller;

import lombok.RequiredArgsConstructor;
import lt.svaskevicius.videometa.api.model.identity.AuthRequestDto;
import lt.svaskevicius.videometa.api.model.identity.AuthResponseDto;
import lt.svaskevicius.videometa.api.service.AuthApiService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthApiService authApiService;

  //TODO: Rate limit this endpoint
  @PostMapping("/login")
  public AuthResponseDto authenticate(@Validated @RequestBody final AuthRequestDto authenticationRequest) {
    return authApiService.authenticate(authenticationRequest.username(), authenticationRequest.password());
  }
}
