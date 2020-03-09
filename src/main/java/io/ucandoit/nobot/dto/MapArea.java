package io.ucandoit.nobot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class MapArea {

  private String mapId;

  private String buildingType;

  private String title;

  private int level;

  private int x;

  private int y;
}
