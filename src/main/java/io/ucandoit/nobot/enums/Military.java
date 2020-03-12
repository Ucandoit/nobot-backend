package io.ucandoit.nobot.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum Military {
  Mounted("騎馬"),
  Soldier("足軽"),
  Gunner("鉄砲"),
  Unknown("Unknown");

  Military(String title) {
    this.title = title;
  }

  private String title;
}
