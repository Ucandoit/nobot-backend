package io.ucandoit.nobot.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Task")
@Data
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "login")
  private Account account;

  @Column(name = "task_type")
  private String taskType;

  @Column(name = "start_time")
  private Date startTime;

  @Column(name = "stop_time")
  private Date stopTime;

  @Column(name = "repeat")
  private Integer repeat;
}
