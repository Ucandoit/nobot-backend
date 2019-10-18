package io.ucandoit.nobot.repository;

import io.ucandoit.nobot.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findByLoginIn(List<String> loginList);

}
