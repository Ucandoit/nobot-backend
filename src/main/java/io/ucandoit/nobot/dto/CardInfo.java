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
  private String refineTotal;
  private String refineAtk;
  private String refineDef;
  private String refineSpd;
  private String refineVir;
  private String refineStg;
  private String skill1;
  private String skill2;
  private String skill3;
}
