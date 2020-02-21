package io.ucandoit.nobot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NobotUtils {

  public static final String BASE_URL = "http://210.140.157.168";

  public static final String MAP_URL = "http://210.140.157.168/area_map.htm";

  public static final String PROFILE_URL = "http://210.140.157.168/user/profile.htm";

  public static final String WAR_SETUP_URL = "http://210.140.157.168/war/war_setup.htm";

  public static final String DRAW_URL = "http://210.140.157.168/nyaomikuji/nyaomikuji.htm";

  public static final String STORY_CHAPTER_REWARD =
      "http://210.140.157.168/cattale/chapter_reward_list.htm";

  public static final String STORY_POINT_REWARD =
      "http://210.140.157.168/cattale/point_reward_list.htm";

  public static final String BATTLE_URL = "http://210.140.157.168/battle/setup.htm";

  public static final String TUTORIAL_URL = "http://210.140.157.168/tutorial/tutorial.htm";

  public static final String REPORT_LIST_URL = "http://210.140.157.168/report/list.htm";

  public static final String COMMAND_URL = "http://210.140.157.168/command.htm";

  public static final String MANAGE_CARD_URL = "http://210.140.157.168/card/manage_card.htm";

  public static final String MANAGE_STORE_CARD_URL = MANAGE_CARD_URL + "?status=2";

  public static final String RECRUIT_CARD_URL = "http://210.140.157.168/card/sub/recurit_card.htm";

  public static final String MANAGE_DECK_URL = "http://210.140.157.168/card/manage_deck.htm";

  public static final String BUILD_URL = "http://210.140.157.168/build.htm";

  public static final String VILLAGE_URL = "http://210.140.157.168/village.htm";

  public static final String DROP_DIALOG_URL = "http://210.140.157.168/dropdlg.htm";

  public static final String NOTIFY_UPDATE_URL = "http://210.140.157.168/notify_update.htm";

  public static final String FRIEND_CODE_URL = "http://210.140.157.168/user/friend_code.htm";

  public static final String FUKUBIKI_START_URL =
      "http://210.140.157.168/nyaomikuji/fukubiki_start.htm";

  public static final String FUKUBIKU_RESULT_URL =
      "http://210.140.157.168/nyaomikuji/fukubiki_result.htm";

  public static final String CARD_DETAIL_URL = "http://210.140.157.168/card/sub/get_card_detail.htm";

  /**
   * Get card rarity code from image url
   *
   * @param img image url
   * @return card rarity code
   */
  public static int getRarityCode(String img) {
    Pattern pattern = Pattern.compile("(.+/rare_0)(.?)(_.+)");
    Matcher matcher = pattern.matcher(img);
    if (matcher.find()) {
      return Integer.parseInt(matcher.group(2));
    } else {
      return -1;
    }
  }

  /**
   * Get card rarity from image url
   *
   * @param img image url
   * @return card rarity
   */
  public static String getRarity(String img) {
    return getRarity(getRarityCode(img));
  }

  /**
   * Get card rarity from code
   *
   * @param code rarity code
   * @return card rarity
   */
  public static String getRarity(int code) {
    switch (code) {
      case 1:
        return "並";
      case 2:
        return "珍";
      case 3:
        return "稀";
      case 4:
        return "極";
      case 7:
        return "煌";
      case 6:
        return "誉";
      default:
        return "Unknown";
    }
  }

  /**
   * Get draw type from code
   *
   * @param code draw code
   * @return draw type
   */
  public static String getDrawType(int code) {
    switch (code) {
      case 4:
        return "吉";
      case 5:
        return "福";
      case 0:
        return "新春福引";
      default:
        return "Unknown";
    }
  }
}
