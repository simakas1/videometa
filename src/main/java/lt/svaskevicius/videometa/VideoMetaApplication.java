package lt.svaskevicius.videometa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "lt.svaskevicius.videometa")
public class VideoMetaApplication {

  public static void main(final String[] args) {
    SpringApplication.run(VideoMetaApplication.class, args);
  }

}
