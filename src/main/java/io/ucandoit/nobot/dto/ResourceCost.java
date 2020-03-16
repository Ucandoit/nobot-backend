package io.ucandoit.nobot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCost {

  private int fire;

  private int earth;

  private int wind;

  private int water;

  private int sky;

  private int seconds;

  private int reducedSeconds;

  public ResourceCost(int fire, int earth, int wind, int water, int sky, int seconds) {
    this.fire = fire;
    this.earth = earth;
    this.wind = wind;
    this.water = water;
    this.sky = sky;
    this.seconds = seconds;
    this.reducedSeconds = seconds / 6;
  }
}
