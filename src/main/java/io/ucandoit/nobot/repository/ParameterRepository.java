package io.ucandoit.nobot.repository;

import io.ucandoit.nobot.model.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ParameterRepository extends JpaRepository<Parameter, String> {}
