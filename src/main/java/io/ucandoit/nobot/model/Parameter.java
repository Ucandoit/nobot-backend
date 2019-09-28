package io.ucandoit.nobot.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "parameter")
@Data
public class Parameter {

  @Id
  @Column(name = "code")
  private String code;

  @Column(name = "value")
  private String value;
}
