package lt.svaskevicius.videometa.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import lt.svaskevicius.videometa.security.JwtTokenProvider;
import lt.svaskevicius.videometa.security.filter.JwtAuthorizationFilter;
import lt.svaskevicius.videometa.web.model.identity.AuthRequestDto;
import lt.svaskevicius.videometa.web.model.identity.AuthResponseDto;
import lt.svaskevicius.videometa.web.service.AuthApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthApiService authApiService;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private JwtAuthorizationFilter jwtAuthorizationFilter;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void authenticate_ShouldReturnAuthResponse_WhenValidCredentials() throws Exception {
    // Given
    final Date expiresAt = new Date(System.currentTimeMillis() + 3600L);
    final Date issuedAt = new Date();
    final AuthRequestDto authRequest = new AuthRequestDto("testuser", "password123");
    final AuthResponseDto expectedResponse = new AuthResponseDto("jwt-token", expiresAt,
        issuedAt);

    when(authApiService.authenticate("testuser", "password123"))
        .thenReturn(expectedResponse);

    // When / Then
    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isOk());
  }
}