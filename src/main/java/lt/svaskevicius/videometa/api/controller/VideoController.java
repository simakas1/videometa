package lt.svaskevicius.videometa.api.controller;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/videos")
public class VideoController {

  @PostMapping("/import")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public void importVideo() {
    //TODO: Implement video import
  }

  @GetMapping
  public void getVideos() {
    // TODO: Implement get all videos + pagination
  }

  @GetMapping("/{id}")
  public void getVideoById(@PathVariable UUID id) {
    // TODO: Implement get video by ID
  }

  @GetMapping("/stats")
  public void getVideoStats() {
    // TODO: Implement video statistics
  }
}
