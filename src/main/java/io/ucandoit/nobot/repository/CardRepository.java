package io.ucandoit.nobot.repository;

import io.ucandoit.nobot.model.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface CardRepository extends JpaRepository<Card, Integer> {
  Page<Card> findCardsByRarity(String rarity, Pageable pageable);
}
