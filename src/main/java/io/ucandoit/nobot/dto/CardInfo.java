package io.ucandoit.nobot.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CardInfo {

  private String id;
  private String name;
  private boolean tradable;
  private String imgUrl;
}
