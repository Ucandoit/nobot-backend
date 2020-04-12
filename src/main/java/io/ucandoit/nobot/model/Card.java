package io.ucandoit.nobot.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Card")
@Data
public class Card {

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "number")
  private int number;

  @Column(name = "name")
  private String name;

  @Column(name = "real_name")
  private String realName;

  @Column(name = "rarity")
  private String rarity;

  @Column(name = "property")
  private String property;

  @Column(name = "cost")
  private float cost;

  @Column(name = "military")
  private String military;

  @Column(name = "job")
  private String job;

  @Column(name = "star")
  private int star;

  @Column(name = "face_url")
  private String faceUrl;

  @Column(name = "illust_url")
  private String illustUrl;

  @Column(name = "initial_atk")
  private Integer initialAtk;

  @Column(name = "initial_def")
  private Integer initialDef;

  @Column(name = "initial_spd")
  private Integer initialSpd;

  @Column(name = "initial_vir")
  private Integer initialVir;

  @Column(name = "initial_stg")
  private Integer initialStg;

  @Column(name = "final_atk")
  private Integer finalAtk;

  @Column(name = "final_def")
  private Integer finalDef;

  @Column(name = "final_spd")
  private Integer finalSpd;

  @Column(name = "final_vir")
  private Integer finalVir;

  @Column(name = "final_stg")
  private Integer finalStg;

  @Column(name = "personality")
  private String personality;

  @Column(name = "slogan")
  private String slogan;

  @Column(name = "history")
  private String history;

  @Column(name = "train_skills")
  private String trainSkills;
}
