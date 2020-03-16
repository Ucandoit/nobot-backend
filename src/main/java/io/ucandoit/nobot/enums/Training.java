package io.ucandoit.nobot.enums;

import lombok.Getter;

@Getter
public enum Training {
  FIRE("train_fire", Building.FIRE),
  EARTH("train_earth", Building.EARTH),
  WIND("train_wind", Building.WIND),
  WATER("train_water", Building.WATER),
  SKY("train_sky", Building.SKY);

  private String command;
  private Building trainingBuilding;

  Training(String command, Building trainingBuilding) {
    this.command = command;
    this.trainingBuilding = trainingBuilding;
  }
}
