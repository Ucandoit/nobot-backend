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

  private String rarity;

  private String name;

  private Integer price;

  private Date snipeTime;

  private Boolean bought;
}
