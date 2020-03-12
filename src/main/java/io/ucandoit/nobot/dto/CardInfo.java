package io.ucandoit.nobot.dto;

import io.ucandoit.nobot.enums.Military;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CardInfo {

  private String id;
  private String name;
  private String realName;
  private int rarityCode;
  private String rarity;
  private String property;
  private Military military;
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
