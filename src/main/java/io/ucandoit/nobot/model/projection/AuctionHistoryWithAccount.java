package io.ucandoit.nobot.model.projection;

import io.ucandoit.nobot.model.AuctionHistory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.Date;

@Projection(
    name = "withAccount",
    types = {AuctionHistory.class})
public interface AuctionHistoryWithAccount {

  Integer getId();

  String getRarity();

  String getName();

  Integer getPrice();

  Date getSnipeTime();

  @Value("#{target.account.login}")
  String getLogin();

  Boolean getBought();
}
