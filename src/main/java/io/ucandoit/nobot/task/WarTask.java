package io.ucandoit.nobot.task;

import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Slf4j
@Component("warTask")
@Scope("prototype")
public class WarTask implements Runnable {

  @Resource private HttpClient httpClient;

  private String cookie;

  private String login;

  private String token;

  private Date tokenGenerateTime;

  private boolean fp;

  private boolean npc;

  private int line;

  private boolean isLastDay;

  @Override
  public void run() {
    try {
      checkToken();
      if (token != null) {
        boolean availability = checkAvailability();
        if (availability) {
          String startUrl = setupWar();
          String warId = startUrl.split("\\?")[1];
          String postData =
              warId + "&action=go&mc=0&fp=" + (fp ? "1" : "0") + "&npc=" + (npc ? "1" : "0");
          httpClient.makePOSTRequest(startUrl.split("\\?")[0], "POST", postData, token);
        }
      }
    } catch (Exception e) {
      log.error("War task error:", e);
    }
  }

  private void checkToken() {
    boolean updateToken = false;
    if (token == null || token.equals("")) {
      updateToken = true;
    } else {
      if (tokenGenerateTime != null) {
        long diff = new Date().getTime() - tokenGenerateTime.getTime();
        if (diff > 30 * 60 * 1000) {
          updateToken = true;
        }
      }
    }
    if (updateToken) {
      log.info("Updating token for {}", login);
      HttpUtils.requestToken(httpClient, cookie).ifPresent(s -> token = s);
      tokenGenerateTime = new Date();
    }
  }

  private boolean checkAvailability() {
    log.info("Checking availability for {}.", login);
    String villageUrl = "http://210.140.157.168/village.htm";
    ResponseEntity<String> response = httpClient.makePOSTRequest(villageUrl, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(villageUrl).getString("body"));

    Element doing = doc.selectFirst("#doing");
    if (doing != null && doing.toString().contains("合戦")) {
      log.info("{} is in war.", login);
      return false;
    }

    int currentFood = Integer.parseInt(doc.selectFirst("#element_food").text());
    int currentFire = Integer.parseInt(doc.selectFirst("#element_fire").text());
    int currentEarth = Integer.parseInt(doc.selectFirst("#element_earth").text());
    int currentWind = Integer.parseInt(doc.selectFirst("#element_wind").text());
    int currentWater = Integer.parseInt(doc.selectFirst("#element_water").text());
    int currentSky = Integer.parseInt(doc.selectFirst("#element_sky").text());

    if (currentFire >= 3000
        || currentEarth >= 3000
        || currentWind >= 3000
        || currentWater >= 3000
        || currentSky >= 3000) {
      int convertedFood =
          currentFire / 20
              + currentEarth / 20
              + currentWind / 20
              + currentWater / 20
              + currentSky / 20;
      if (convertedFood > 0 && convertedFood + currentFood <= 7500) {
        currentFood += convertedFood;
        trade(doc);
      } else {
        log.warn("Food exceeded for {}.", login);
      }
    }

    if ((fp && currentFood < 1860) || (npc && currentFood < 1860) || (!fp && currentFood < 620)) {
      log.warn("Short in food for {}.", login);
      return false;
    }

    // optimise for next day
    if (isLastDay) {
      log.info("Last day, skip save for next day.");
      return true;
    }
    Date now = new Date();
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setTime(now);
    Date nextDay = calculateNextDayStartTime(calendar);
    long diff = nextDay.getTime() - now.getTime();
    int willGain = (int) (310 * diff / 3600000);
    log.info("{} will gain {} food til next day.", login, willGain);
    if (currentFood + willGain < 7500) {
      log.info("Short in food for next day {}.", login);
      return false;
    }
    return true;
  }

  private Date calculateNextDayStartTime(Calendar calendar) {
    Calendar calendarNextDay = GregorianCalendar.getInstance();
    calendarNextDay.setTime(calendar.getTime());
    calendarNextDay.set(Calendar.MILLISECOND, 0);
    calendarNextDay.set(Calendar.SECOND, 0);
    calendarNextDay.set(Calendar.MINUTE, 0);
    calendarNextDay.set(Calendar.HOUR_OF_DAY, 7);
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    if (hour >= 7) {
      calendarNextDay.add(Calendar.DATE, 1);
    }
    return calendarNextDay.getTime();
  }

  private void trade(Document doc) {
    Element form = doc.selectFirst("#trade-all-form");
    String actionUrl = form.attr("action");
    StringBuilder postData = new StringBuilder();
    for (Element input : form.children()) {
      if (postData.length() > 0) {
        postData.append("&");
      }
      postData.append(input.attr("name")).append("=").append(input.attr("value"));
    }
    httpClient.makePOSTRequest(actionUrl, "POST", postData.toString(), token);
  }

  private String setupWar() throws UnsupportedEncodingException {
    log.info("Setup war for {} at line {}, fp {}.", login, line, fp);
    String warUrl = "http://210.140.157.168/war/war_setup.htm";
    ResponseEntity<String> response = httpClient.makePOSTRequest(warUrl, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(warUrl).getString("body"));
    String laneSelectorPrefix = isLastDay ? "#rank_lane_boss_" : "#rank_lane_";
    Element form =
        doc.selectFirst(laneSelectorPrefix + line)
            .parent()
            .nextElementSibling()
            .selectFirst("form");
    StringBuilder postData = new StringBuilder();
    for (Element input : form.children()) {
      if (postData.length() > 0) {
        postData.append("&");
      }
      postData.append(input.attr("name")).append("=").append(input.attr("value"));
    }
    response = httpClient.makePOSTRequest(warUrl, "POST", postData.toString(), token);
    obj = HttpUtils.responseToJsonObject(response.getBody());
    return URLDecoder.decode(
        obj.getJSONObject(warUrl)
            .getJSONObject("headers")
            .getJSONArray("location")
            .get(0)
            .toString(),
        "UTF-8");
  }

  public void setCookie(String cookie) {
    this.cookie = cookie;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public void setFp(boolean fp) {
    this.fp = fp;
  }

  public void setNpc(boolean npc) {
    this.npc = npc;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public void setLastDay(boolean lastDay) {
    isLastDay = lastDay;
  }
}
