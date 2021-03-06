package io.ucandoit.nobot.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "Account")
@Data
public class Account {

  @Id
  @Column(name = "login")
  private String login;

  @Column(name = "name")
  private String name;

  @Column(name = "cookie")
  private String cookie;

  @Column(name = "expiration_date")
  private Date expirationDate;

  @Column(name = "start_hour")
  private int startHour;

  @Column(name = "daily_search")
  private int dailySearch;

  @Column(name = "enabled")
  private Boolean enabled;

  @Column(name = "recruit_id")
  private String friendCode;

  @Column(name = "np")
  private Integer np;
}
