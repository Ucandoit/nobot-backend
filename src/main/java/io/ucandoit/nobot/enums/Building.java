package io.ucandoit.nobot.enums;

import lombok.Getter;

@Getter
public enum Building {
  STORAGE("type02", "宝物庫", "storage"),
  FOOD("type16", "兵糧庫", "food"),
  PADDY("type17", "水田", "paddy"),
  FIRE("type03", "修練場【火】", "fire"),
  EARTH("type04", "修練場【地】", "earth"),
  WIND("type05", "修練場【風】", "wind"),
  WATER("type06", "修練場【水】", "water"),
  SKY("type07", "修練場【空】", "sky"),
  DEV_BASIC("type09", "奥義開発所", "dev_basic"),
  MARKET("type13", "楽市楽座", "market"),
  HOME_BASIC("type01", "館", "home_basic");

  private String type;

  private String title;

  private String facility;

  Building(String type, String title, String facility) {
    this.type = type;
    this.title = title;
    this.facility = facility;
  }
}
