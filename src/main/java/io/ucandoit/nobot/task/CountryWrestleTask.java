package io.ucandoit.nobot.task;

import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.service.CacheService;
import io.ucandoit.nobot.util.HttpUtils;
import io.ucandoit.nobot.util.NobotUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component("countryWrestleTask")
@Scope("prototype")
public class CountryWrestleTask implements Runnable {

  @Resource private HttpClient httpClient;

  @Resource private CacheService cacheService;

  private String token;

  private String login;

  private int times = -1;

  @Override
  public void run() {
    try {
      boolean stop = false;
      while (!stop) {
        cacheService.getToken(login).ifPresent(s -> token = s);
        if (token != null) {
          if (times != 0) {
            boolean available = checkAvailability();
            log.info(
                "Country wrestle task: "
                    + (available ? "Start country wrestling for {}" : "Not available yet for {}"),
                login);
            if (available) {
              ResponseEntity<String> response =
                  httpClient.makePOSTRequest(NobotUtils.COUNTRY_BATTLE_URL, "GET", "", token);
              if (response.getStatusCode() == HttpStatus.OK) {
                httpClient.makePOSTRequest(
                    NobotUtils.COUNTRY_BATTLE_URL, "POST", "action=btl", token);
              }
              if (times > 0) {
                times--;
                log.info("Country wrestle task: {} times left for {}", times, login);
              }
              log.info("Country wrestle task: waiting 60 seconds for {}", login);
              Thread.sleep(60 * 1000);
            } else {
              log.info("Country wrestle task: waiting to recheck for {}", login);
              Thread.sleep(10 * 1000);
            }
          } else {
            log.info("Country wrestle task: times reached, stop for {}", login);
            stop = true;
          }
        } else {
          stop = true;
        }
      }
    } catch (Exception e) {
      log.error("Wrestle task: error for " + login + " : ", e);
      if ("Stop".equals(e.getMessage())) {
        try {
          throw e;
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  private boolean checkAvailability() throws RuntimeException {
    log.info("Country wrestle task: Checking availability for {}", login);
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.VILLAGE_URL, "GET", "", token);
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
      String html = obj.getJSONObject(NobotUtils.VILLAGE_URL).getString("body");
      if (html == null || html.length() == 0) {
        return false;
      }
      Document doc = Jsoup.parse(html);
      int currentFood = Integer.parseInt(doc.selectFirst("#element_food").text());
      if (currentFood < 50) {
        throw new RuntimeException("Stop");
      } else {
        Element doing = doc.selectFirst("#doing");
        return !doing.toString().contains("全国対戦場");
      }
    } else {
      return false;
    }
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public void setTimes(int times) {
    this.times = times;
  }
}
