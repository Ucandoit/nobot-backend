package io.ucandoit.nobot.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BuildCost {

  private int fire;

  private int earth;

  private int wind;

  private int water;

  private int sky;

  private int seconds;

  public BuildCost(int fire, int earth, int wind, int water, int sky, int seconds) {
    this.fire = fire;
    this.earth = earth;
    this.wind = wind;
    this.water = water;
    this.sky = sky;
    this.seconds = seconds;
  }
}
