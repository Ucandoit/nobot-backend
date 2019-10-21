package io.ucandoit.nobot.task;

import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.service.CacheService;
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

@Slf4j
@Component("storyTask")
@Scope("prototype")
public class StoryTask implements Runnable {

  private static final String MAP_URL = "http://210.140.157.168/area_map.htm";

  private static final String BATTLE_URL = "http://210.140.157.168/battle/setup.htm";

  @Resource private HttpClient httpClient;

  @Resource private CacheService cacheService;

  private String login;

  private String token;

  @Override
  public void run() {
    try {
      cacheService.getToken(login).ifPresent(s -> token = s);
      if (token != null) {
        httpClient.makePOSTRequest(MAP_URL, "GET", null, token);
        notifyUpdate();
        ResponseEntity<String> response = httpClient.makePOSTRequest(MAP_URL, "GET", null, token);
        JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
        Document doc = Jsoup.parse(obj.getJSONObject(MAP_URL).getString("body"));
        int currentFood = Integer.parseInt(doc.selectFirst("#element_food").text());
        if (currentFood < 620) {
          log.info("Story task: short in food for {}.", login);
          throw new RuntimeException("Stop");
        } else {
          Element doing = doc.selectFirst("#notify_count_main");
          if (doing != null && doing.toString().contains("移動")) {
            log.info("Story task: still in battle for {}.", login);
            return;
          }
        }
        Element form =
            doc.selectFirst("img[alt=ねこ戦記]").parent().nextElementSibling().selectFirst("form");
        log.info("Story task: target found for {}.", login);
        response =
            httpClient.makePOSTRequest(MAP_URL, "POST", HttpUtils.buildPostData(form), token);
        obj = HttpUtils.responseToJsonObject(response.getBody());
        String nextUrl =
            obj.getJSONObject(MAP_URL)
                .getJSONObject("headers")
                .getJSONArray("location")
                .get(0)
                .toString();
        response = httpClient.makePOSTRequest(nextUrl, "GET", null, token);
        obj = HttpUtils.responseToJsonObject(response.getBody());
        doc = Jsoup.parse(obj.getJSONObject(nextUrl).getString("body"));
        form = doc.selectFirst("#command_ok").selectFirst("form");
        httpClient.makePOSTRequest(BATTLE_URL, "POST", HttpUtils.buildPostData(form), token);
        log.info("Story task: start battle for {}.", login);
      }
    } catch (Exception e) {
      log.error("Story task: error for " + login + " : ", e);
      if ("Stop".equals(e.getMessage())) {
        throw e;
      }
    }
  }

  private void notifyUpdate() {
    httpClient.makePOSTRequest(
        "http://210.140.157.168/notify_update.htm",
        "POST",
        "notify_flag_1=0&notify_flag_2=0&notify_flag_3=0&notify_flag_4=0&notify_flag_5=0&notify_flag_6=32&notify_flag_7=0&notify_flag_8=0&notify_flag_9=0",
        token);
  }

  public void setLogin(String login) {
    this.login = login;
  }
}
