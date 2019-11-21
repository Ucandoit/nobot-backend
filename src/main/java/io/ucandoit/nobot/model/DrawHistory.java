package io.ucandoit.nobot.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "draw_history")
@Data
public class DrawHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "login")
  private Account account;

  @Column(name = "card_rarity")
  private String rarity;

  @Column(name = "card_name")
  private String name;

  @Column(name = "draw_type")
  private String drawType;

  @Column(name = "draw_time")
  private Date drawTime;
}
