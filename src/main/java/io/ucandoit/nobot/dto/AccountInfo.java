package io.ucandoit.nobot.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class AccountInfo {

  private String login;

  private String name;

  private int np;

  private int food;

  private int maxFood;

  private int fire;

  private int earth;

  private int wind;

  private int water;

  private int sky;

  private int maxFire;

  private int maxEarth;

  private int maxWind;

  private int maxWater;

  private int maxSky;

  private boolean newUser;

  private List<MapArea> areas;

  private List<CardInfo> deckCards;

  private List<CardInfo> reserveCards;
}
