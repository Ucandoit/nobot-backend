package io.ucandoit.nobot.task;

import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.util.HttpUtils;
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
import java.util.Date;

@Slf4j
@Component("wrestleTask")
@Scope("prototype")
public class WrestleTask implements Runnable {

  @Resource private HttpClient httpClient;

  private String token;

  private String cookie;

  private String login;

  private Date tokenGenerateTime;

  public WrestleTask() {
  }

  @Override
  public void run() {
    try {
      checkToken();
      if (token != null) {
        boolean available = checkAvailability();
        log.info("Wrestle task: " + (available ? "Start wrestling for {}" : "Not available yet for {}"), login);
        if (available) {
          String wrestleUrl = "http://210.140.157.168/wrestle/wrestle_setup.htm";
          ResponseEntity<String> response = httpClient.makePOSTRequest(wrestleUrl, "GET", "", token);
          if (response.getStatusCode() == HttpStatus.OK) {
            httpClient.makePOSTRequest(wrestleUrl, "POST", "action=btl", token);
          }
        }
      }
    } catch (Exception e) {
      log.error("Wrestle task: error for " + login + " : ", e);
      if ("Stop".equals(e.getMessage())) {
        throw e;
      }
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
      log.info("Wrestle task: updating token for {}", login);
      HttpUtils.requestToken(httpClient, cookie).ifPresent(s -> token = s);
      tokenGenerateTime = new Date();
    }
  }

  private boolean checkAvailability() throws RuntimeException {
    log.info("Wrestle task: Checking availability for {}", login);
    String url = "http://210.140.157.168/village.htm";
    ResponseEntity<String> response = httpClient.makePOSTRequest(url, "GET", "", token);
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
      String html = obj.getJSONObject(url).getString("body");
      if (html == null || html.length() == 0) {
        return false;
      }
      Document doc = Jsoup.parse(html);
      int currentFood = Integer.parseInt(doc.selectFirst("#element_food").text());
      if (currentFood < 206) {
        throw new RuntimeException("Stop");
      } else {
        Element doing = doc.selectFirst("#doing");
        return !doing.toString().contains("ねこ場所から帰還中です");
      }
    } else {
      return false;
    }
  }

  public void setCookie(String cookie) {
    this.cookie = cookie;
  }

  public void setLogin(String login) {
    this.login = login;
  }
}
