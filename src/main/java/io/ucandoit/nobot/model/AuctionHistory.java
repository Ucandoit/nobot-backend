package io.ucandoit.nobot.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "auction_history")
@Data
public class AuctionHistory {

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

  @Column(name = "card_price")
  private Integer price;

  @Column(name = "snipe_time")
  private Date snipeTime;

  @Column(name = "bought")
  private Boolean bought;
}
