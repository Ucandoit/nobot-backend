package io.ucandoit.nobot.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "requestParams")
public class Card {

  private String tradeBuyId;

  private String cardBuyId;

  private String rarity;

  private String name;

  private Integer price;

  private Integer currentNP;

  private String requestParams;
}
