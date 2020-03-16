package io.ucandoit.nobot.dto;

import io.ucandoit.nobot.enums.Building;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class MapArea {

  private String mapId;

  private Building building;

  private String title;

  private int level;

  private int x;

  private int y;

  private boolean constructing;

  private boolean running;

  public String getPosition() {
    return "x=" + x + "&y=" + y;
  }
}
