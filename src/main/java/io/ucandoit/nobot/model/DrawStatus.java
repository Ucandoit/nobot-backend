package io.ucandoit.nobot.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "draw_status")
@Data
public class DrawStatus {

  @Id
  @Column(name = "login")
  private String login;

  @Column(name = "fu_number")
  private int fuNumber;

  @Column(name = "ji_number")
  private int jiNumber;

  @Column(name = "fukubiki_number")
  private int fukubikiNumber;
}
