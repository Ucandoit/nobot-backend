package io.ucandoit.nobot.task;

import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.util.HttpUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
public class WrestleTask implements Runnable {

  private HttpClient httpClient;

  private String searchUrl;

  private String token;

  private String cookie;

  private boolean stop;

  public WrestleTask(HttpClient httpClient, String token, String cookie) {
    this.httpClient = httpClient;
    this.token = token;
    this.cookie = cookie;
    this.stop = false;
  }

  @Override
  public void run() {
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.scheduleAtFixedRate(
        () -> {
          boolean available = checkAvailability();
          log.info(available ? "Start wrestling." : "Not available yet.");
          if (available) {
            try {
              String wrestleUrl = "http://210.140.157.168/wrestle/wrestle_setup.htm";
              ResponseEntity<String> response =
                  httpClient.makePOSTRequest(wrestleUrl, "GET", "", token);
              if (response.getStatusCode() == HttpStatus.OK) {
                httpClient.makePOSTRequest(wrestleUrl, "POST", "action=btl", token);
              }
            } catch (Exception e) {
              log.error("Error:", e);
            }
          }
        },
        0,
        5,
        TimeUnit.SECONDS);
  }

  private boolean checkAvailability() throws RuntimeException {
    log.info("Checking availability.");
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
}
