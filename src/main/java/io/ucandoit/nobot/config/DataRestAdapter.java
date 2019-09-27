package io.ucandoit.nobot.config;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import javax.persistence.Entity;
import java.util.Set;

@Configuration
@Slf4j
public class DataRestAdapter implements RepositoryRestConfigurer {

  private static final String BASE_PATH = "/api/rest";

  @Value("io.ucandoit.nobot.domain")
  private String entitiesBasePackage;

  @Override
  public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
    config.setBasePath(BASE_PATH);
    config.setReturnBodyOnCreate(true);
    config.setReturnBodyOnUpdate(true);
    Reflections reflections =
        new Reflections(
            entitiesBasePackage, new SubTypesScanner(false), new TypeAnnotationsScanner());
    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    for (Class<?> entity : entities) {
      log.info("Expose Ids for {}", entity);
      config.exposeIdsFor(entity);
    }
  }
}
