package io.ucandoit.nobot.repository;

import io.ucandoit.nobot.model.AuctionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Integer> {}
