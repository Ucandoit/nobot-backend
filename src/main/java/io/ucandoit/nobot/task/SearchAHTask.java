package io.ucandoit.nobot.task;

import io.ucandoit.nobot.dto.Card;
import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.model.Task;
import io.ucandoit.nobot.repository.TaskRepository;
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
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component("searchAHTask")
@Scope("prototype")
public class SearchAHTask implements Runnable {

  @Resource private HttpClient httpClient;

  @Resource private TaskRepository taskRepository;

  private String cookie;

  private Integer taskId;

  private ScheduledFuture future;

  private String login;

  private String searchUrl;

  private String token;

  private int count = 0;

  @Override
  @Transactional
  public void run() {
    try {
      count++;
      if (count > 2000) {
        // stop running after reaching max attempts
        Task task = taskRepository.getOne(taskId);
        task.setRepeat(count);
        task.setStopTime(new Date());
        taskRepository.save(task);
        future.cancel(true);
        return;
      }
      log.info("Searching {} for {}", count, login);
      if (token == null || token.equals("")) {
        token = HttpUtils.requestToken(httpClient, cookie);
      }
      if (searchUrl == null || searchUrl.equals("")) {
        searchUrl = requestAHPage(token);
      }
      // register every 50 attempts
      if (count % 50 == 0) {
        Task task = taskRepository.getOne(taskId);
        task.setRepeat(count);
        taskRepository.save(task);
      }
      if (count % 500 == 0) {
        log.info("Updating token for {}", login);
        updateToken();
      }
      String response = requestAHSearch(searchUrl, token);
      if (response != null) {
        JSONObject obj = responseToJsonObject(response);
        String html = obj.getJSONObject(searchUrl).getString("body");
        Card card = getCardFromHtml(html);
        if (card != null) {
          if (card.getCurrentNP() > card.getPrice()) {
            log.info("card found {} for {}. Trying to buy...", card, login);
            requestBuy(card.getRequestParams(), token);
          }
        } else {
          log.info("Nothing found for {}.", login);
        }
      }
    } catch (Exception e) {
      log.error("error:", e);
    }
  }

  public void setCookie(String cookie) {
    this.cookie = cookie;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public Integer getTaskId() {
    return taskId;
  }

  public void setTaskId(Integer taskId) {
    this.taskId = taskId;
  }

  public ScheduledFuture getFuture() {
    return future;
  }

  public void setFuture(ScheduledFuture future) {
    this.future = future;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  private String requestAHPage(String token) throws UnsupportedEncodingException {
    String url = "http://210.140.157.168/card/trade_buy.htm";
    ResponseEntity<String> response = httpClient.makePOSTRequest(url, "GET", null, token);
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
      return URLDecoder.decode(
          obj.getJSONObject(url)
              .getJSONObject("headers")
              .getJSONArray("location")
              .get(0)
              .toString(),
          "UTF-8");
    } else {
      log.error(
          "Error while requesting AH page. Code: {}, Body: {}.",
          response.getStatusCode(),
          response.getBody());
    }
    return null;
  }

  private String requestAHSearch(String search, String token) {
    ResponseEntity<String> response = httpClient.makePOSTRequest(search, "GET", null, token);
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      return response.getBody();
    } else {
      log.error(
          "Error while requesting AH search. Code: {}, Body: {}.",
          response.getStatusCode(),
          response.getBody());
    }
    return null;
  }

  private void requestBuy(String buyData, String token) {
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(
            "http://210.140.157.168/card/trade_buy.htm", "POST", buyData, token);
    if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
      log.error(
          "Error while requesting Token. Code: {}, Body: {}.",
          response.getStatusCode(),
          response.getBody());
    }
  }

  private void updateToken() {
    Map<String, Object> params = new HashMap<>();
    params.put("st", token);
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(
            "http://yahoo-mbga.jp/game/12004455/update_token", params, cookie);
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      JSONObject obj = new JSONObject(response.getBody());
      this.token = obj.getString("token");
    } else {
      log.error(
          "Error while updating token. Code: {}, Body: {}.",
          response.getStatusCode(),
          response.getBody());
    }
  }

  private JSONObject responseToJsonObject(String response) {
    return new JSONObject(response.substring(response.indexOf('{')));
  }

  private Card getCardFromHtml(String html) {
    Document doc = Jsoup.parse(html);
    Element cat = doc.selectFirst("#buy-list1");
    Element form = doc.selectFirst("#form");
    if (cat != null && form != null) {
      Card card = new Card();
      card.setCurrentNP(Integer.parseInt(doc.select("#lottery_point").text()));
      for (String className : cat.classNames()) {
        if (className.startsWith("trade-buy-id")) {
          card.setTradeBuyId(className.replace("trade-buy-id", ""));
        } else if (className.startsWith("card-buy-id")) {
          card.setCardBuyId(className.replace("card-buy-id", ""));
        }
      }
      card.setRarity(getRarity(cat.child(0).select(".rank_image_new").attr("src")));
      card.setName(cat.child(1).select("u").text());
      card.setPrice(Integer.parseInt(cat.child(6).select(".point").text()));

      StringBuilder requestParams = new StringBuilder();
      for (Element input : form.select("input")) {
        String key = input.attr("name");
        String value;
        if (key.equals("trade-id")) {
          value = card.getTradeBuyId();
        } else {
          value = input.attr("value");
        }
        if (requestParams.length() != 0) {
          requestParams.append("&");
        }
        requestParams.append(key).append("=").append(value);
      }
      card.setRequestParams(requestParams.toString());
      return card;
    }

    return null;
  }

  private String getRarity(String img) {
    Pattern pattern = Pattern.compile("(.+/rare_0)(.?)(_.+)");
    Matcher matcher = pattern.matcher(img);
    if (matcher.find()) {
      switch (matcher.group(2)) {
        case "1":
          return "並";
        case "2":
          return "珍";
        case "3":
          return "稀";
        case "4":
          return "極";
        case "7":
          return "煌";
        case "6":
          return "誉";
        default:
          return "Unknown";
      }
    } else {
      return "Unknown";
    }
  }
}
