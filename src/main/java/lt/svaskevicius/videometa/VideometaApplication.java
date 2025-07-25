package lt.svaskevicius.videometa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "lt.svaskevicius.videometa.config.properties")
public class VideometaApplication {

  public static void main(final String[] args) {
    SpringApplication.run(VideometaApplication.class, args);
  }

}
