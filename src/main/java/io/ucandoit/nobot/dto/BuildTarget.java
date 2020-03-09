package io.ucandoit.nobot.dto;

import io.ucandoit.nobot.enums.Building;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BuildTarget {

  private Building building;

  private boolean extend;

  private int x;

  private int y;

  private int seconds;

  public String getPosition() {
    return "x=" + x + "&y=" + y;
  }
}
