package io.ucandoit.nobot.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "war_config")
@Data
public class WarConfig {

  @Id
  @Column(name = "login")
  private String login;

  @Column(name = "line")
  private int line;

  @Column(name = "fp")
  private boolean fp;

  @Column(name = "enabled")
  private boolean enabled;

  @Column(name = "endday")
  private Integer endDay;
}
