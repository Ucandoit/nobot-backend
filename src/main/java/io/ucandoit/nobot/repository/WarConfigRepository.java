package io.ucandoit.nobot.repository;

import io.ucandoit.nobot.model.WarConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface WarConfigRepository extends JpaRepository<WarConfig, String> {
  List<WarConfig> findByGroup(String group);
}
