package io.ucandoit.nobot.repository;

import io.ucandoit.nobot.model.DrawHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface DrawHistoryRepository extends JpaRepository<DrawHistory, Integer> {}
