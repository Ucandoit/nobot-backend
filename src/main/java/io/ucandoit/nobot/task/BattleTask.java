package io.ucandoit.nobot.task;

import io.ucandoit.nobot.enums.FriendshipLevel;
import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.service.BattleService;
import io.ucandoit.nobot.service.CacheService;
import io.ucandoit.nobot.util.HttpUtils;
import io.ucandoit.nobot.util.NobotUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component("battleTask")
@Scope("prototype")
public class BattleTask implements Runnable {

  private static final String NOT_CLEAR = "NOT_CLEAR";
  private static final String STOP = "STOP";
  private static final String CLEAR = "CLEAR";
  @Resource Map<String, String> countryMap;
  @Resource private HttpClient httpClient;
  @Resource private CacheService cacheService;
  @Resource private BattleService battleService;
  private String login;
  private String token;
  private int times;

  @Override
  public void run() {
    try {
      cacheService.getToken(login).ifPresent(s -> token = s);
      if (token != null) {
        Map<String, FriendshipLevel> friendshipLevelMap =
            battleService.getFriendshipsByToken(token);
        boolean stop = false;
        String targetCountry = "";
        while (!stop) {
          targetCountry = getLowestFriendshipCountry(friendshipLevelMap, targetCountry);
          String targetCity = countryMap.get(targetCountry);
          log.info("Battle task: target city: {} for {}", targetCity, login);
          goToCity(targetCity);
          String status = cityClearStatus();
          while (status.equals(NOT_CLEAR)) {
            status = cityClearStatus();
          }
          if (status.equals(STOP)) {
            stop = true;
          } else {
            log.info("Battle task: target city {} clear for {}", targetCity, login);
            friendshipLevelMap = battleService.getFriendshipsByToken(token);
          }
        }
      }
    } catch (Exception e) {
      log.error("Battle task: error for " + login + " : ", e);
      if ("Stop".equals(e.getMessage())) {
        try {
          throw e;
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  private String getLowestFriendshipCountry(
      Map<String, FriendshipLevel> map, String exceptCountry) {
    int level = -1;
    String country = "";
    for (Map.Entry<String, FriendshipLevel> entry : map.entrySet()) {
      int cityLevel = entry.getValue().getLevel();
      if ((level > cityLevel || level == -1) && !entry.getKey().equals(exceptCountry)) {
        level = cityLevel;
        country = entry.getKey();
      }
    }
    return country;
  }

  private void goToCity(String targetCity) throws InterruptedException {
    String currentCity = "";
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.MAP_URL, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.MAP_URL).getString("body"));
    Elements cities = doc.select(".map_point_c");
    for (Element city : cities) {
      if (!city.parent().hasClass("dialog")) {
        currentCity = city.attr("title");
        break;
      }
    }
    if (currentCity.equals(targetCity)) {
      log.info("Battle task: Already at target city for {}", login);
    } else {
      log.info("Battle task: going to target city: {} for {}", targetCity, login);
      Element cityDetail =
          doc.selectFirst(".map_point_c[alt=" + targetCity + "]").parent().nextElementSibling();
      int seconds = getSeconds(cityDetail.text());
      httpClient.makePOSTRequest(
          NobotUtils.MAP_URL,
          "POST",
          HttpUtils.buildPostData(cityDetail.selectFirst("form")),
          token);
      log.info("Battle task: waiting for {} seconds to go to target city for {}", seconds, login);
      Thread.sleep(seconds * 1000);
      // update token after wait
      cacheService.getToken(login).ifPresent(s -> token = s);
    }
  }

  private String cityClearStatus() throws InterruptedException {
    String clearStatus = NOT_CLEAR;
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.MAP_URL, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.MAP_URL).getString("body"));
    int currentFood = Integer.parseInt(doc.selectFirst("#element_food").text());
    if (currentFood < 620) {
      log.info("Battle task: not enough food for {}", login);
      return STOP;
    }
    if (times == 0) {
      log.info("Battle task: times reached for {}", login);
      return STOP;
    }
    Element target = doc.selectFirst(".map_point_e[alt=敵軍]");
    if (target == null) {
      target = doc.selectFirst(".map_point_e");
    } else {
      clearStatus = CLEAR;
    }
    // go to next city
    if (target == null) {
      return CLEAR;
    }
    log.info("Battle task: set up battle for {}.", login);
    String dialogId = target.parent().attr("id");
    String detailId = dialogId.replace("dialog", "detail");
    int seconds = getSeconds(doc.select("#" + detailId + " .quest_info").get(3).text());
    response =
        httpClient.makePOSTRequest(
            NobotUtils.MAP_URL,
            "POST",
            HttpUtils.buildPostData(doc.selectFirst("#" + detailId + " form")),
            token);
    obj = HttpUtils.responseToJsonObject(response.getBody());
    String nextUrl =
        obj.getJSONObject(NobotUtils.MAP_URL)
            .getJSONObject("headers")
            .getJSONArray("location")
            .get(0)
            .toString();
    response = httpClient.makePOSTRequest(nextUrl, "GET", null, token);
    obj = HttpUtils.responseToJsonObject(response.getBody());
    doc = Jsoup.parse(obj.getJSONObject(nextUrl).getString("body"));
    httpClient.makePOSTRequest(
        NobotUtils.BATTLE_URL,
        "POST",
        HttpUtils.buildPostData(doc.selectFirst("#command_ok").selectFirst("form")),
        token);
    log.info("Battle task: start battle for {}.", login);
    log.info("Battle task: waiting for {} seconds to finish battle for {}", seconds, login);
    Thread.sleep(seconds * 1000);
    if (times > 0) {
      times--;
    }
    return clearStatus;
  }

  private int getSeconds(String text) {
    Pattern pattern = Pattern.compile("(.*00:)(.{2})(:)(.{2})(.*)");
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      int minute = Integer.parseInt(matcher.group(2));
      int second = Integer.parseInt(matcher.group(4));
      return minute * 60 + second;
    } else {
      return 15 * 60;
    }
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public void setTimes(int times) {
    this.times = times;
  }
}
