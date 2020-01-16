package io.ucandoit.nobot.repository;

import io.ucandoit.nobot.model.DrawStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface DrawStatusRepository extends JpaRepository<DrawStatus, String> {}
