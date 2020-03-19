package io.ucandoit.nobot.model;

import io.ucandoit.nobot.model.userType.CustomJsonType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Entity
@Table(name = "war_config")
@TypeDef(name = "jsonb", typeClass = CustomJsonType.class)
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

  @Column(name = "npc")
  private boolean npc;

  @Column(name = "pc")
  private Boolean pc;

  @Transient private boolean auto;

  @Type(type = "jsonb")
  @Column(name = "status")
  private String status;

  @Column(name = "war_group")
  private String group;
}
