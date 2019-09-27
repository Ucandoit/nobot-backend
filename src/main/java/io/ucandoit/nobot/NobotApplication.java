package io.ucandoit.nobot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class NobotApplication extends SpringBootServletInitializer {

  private static final String SPRING_PROFILE_DEVELOPMENT = "dev";

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(NobotApplication.class);
    app.setAdditionalProfiles(addDefaultProfile(new SimpleCommandLinePropertySource(args)));
    app.run(args);
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.profiles(addDefaultProfile(null)).sources(NobotApplication.class);
  }

  /**
   * Add a default profile.
   *
   * <p>Please use -Dspring.profiles.active= xxx
   *
   * @param source SimpleCommandLinePropertySource
   * @return profile
   */
  private static String addDefaultProfile(SimpleCommandLinePropertySource source) {
    String profile;
    if (source != null && source.containsProperty("spring.profiles.active")) {
      profile = source.getProperty("spring.profiles.active");
    } else {
      profile = System.getProperty("spring.profiles.active");
    }

    if (profile != null) {
      log.info("Running with Spring profile(s) : {}", profile);
      return profile;
    } else {
      log.warn("No Spring profile configured, running with development configuration");
      return SPRING_PROFILE_DEVELOPMENT;
    }
  }
}
