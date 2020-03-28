package io.ucandoit.nobot.enums;

import lombok.Getter;

@Getter
public enum FriendshipLevel {
  LEVEL_0("疎遠", 0),
  LEVEL_1("面識", 1),
  LEVEL_2("関心", 2),
  LEVEL_3("好意", 3),
  LEVEL_4("友好", 4),
  LEVEL_5("親密", 5),
  LEVEL_6("信頼", 6);

  private String label;
  private int level;

  FriendshipLevel(String label, int level) {
    this.label = label;
    this.level = level;
  }

  public static FriendshipLevel fromLabel(String label) {
    for (FriendshipLevel friendshipLevel : values()) {
      if (friendshipLevel.getLabel().equals(label)) {
        return friendshipLevel;
      }
    }
    return null;
  }
}
