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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component("tutorialTask")
@Scope("prototype")
public class TutorialTask implements Runnable {

  @Resource private CacheService cacheService;

  @Resource private HttpClient httpClient;

  private String login;

  private String token;

  @Override
  public void run() {
    try {
      cacheService.getToken(login).ifPresent(s -> token = s);
      if (token != null) {
        battle1();
        readReport();
        drawCard();
        extendBase();
        moveCard();
        replaceCard();
        buildFoodFacility();
        trainFire();
        battle2();
        watchReplay();
      }
    } catch (Exception e) {
      log.error("Tutorial task: error for " + login + " : ", e);
      if ("Stop".equals(e.getMessage())) {
        try {
          throw e;
        } catch (InterruptedException | UnsupportedEncodingException ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  private void battle1() throws InterruptedException {
    log.info("Tutorial task: battle for {}.", login);
    battle();
    log.info("Tutorial task: complete quest battle for {}.", login);
    httpClient.makePOSTRequest(NobotUtils.TUTORIAL_URL, "GET", null, token);
    httpClient.makePOSTRequest(NobotUtils.TUTORIAL_URL, "POST", "complete=1", token);
  }

  private void readReport() {
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.REPORT_LIST_URL, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.REPORT_LIST_URL).getString("body"));
    String reportUrl =
        NobotUtils.BASE_URL
            + doc.selectFirst("#content table tr:nth-child(2) td:nth-child(3)")
                .selectFirst("a")
                .attr("href");
    log.info("Tutorial task: read report for {}.", login);
    httpClient.makePOSTRequest(reportUrl, "GET", null, token);
    log.info("Tutorial task: complete quest read report for {}.", login);
    httpClient.makePOSTRequest(NobotUtils.TUTORIAL_URL, "POST", "complete=2", token);
  }

  private void drawCard() {
    log.info("Tutorial task: draw card for {}.", login);
    httpClient.makePOSTRequest(NobotUtils.DRAW_URL, "POST", "type=1", token);
    log.info("Tutorial task: complete quest draw card for {}.", login);
    httpClient.makePOSTRequest(NobotUtils.TUTORIAL_URL, "POST", "complete=3", token);
  }

  private void extendBase() throws InterruptedException {
    log.info("Tutorial task: extend base for {}.", login);
    httpClient.makePOSTRequest(
        NobotUtils.COMMAND_URL, "POST", "tabid=&x=0&y=4&command=extend", token);
    log.info("Tutorial task: wait for extend base to complete for {}.", login);
    Thread.sleep(7000);
    log.info("Tutorial task: complete quest extend base for {}.", login);
    httpClient.makePOSTRequest(NobotUtils.TUTORIAL_URL, "POST", "complete=4", token);
  }

  private void moveCard() {
    log.info("Tutorial task: move card for {}.", login);
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.MANAGE_STORE_CARD_URL, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc =
        Jsoup.parse(obj.getJSONObject(NobotUtils.MANAGE_STORE_CARD_URL).getString("body"));
    Element cardElement = doc.selectFirst(".card");
    AtomicReference<String> cardId = new AtomicReference<>("");
    cardElement
        .classNames()
        .forEach(
            className -> {
              if (className.startsWith("card-id")) {
                cardId.set(className.replace("card-id", ""));
              }
            });
    AtomicReference<String> fileId = new AtomicReference<>("");
    cardElement
        .parent()
        .selectFirst(".recruit-button")
        .classNames()
        .forEach(
            className -> {
              if (className.startsWith("file-id")) {
                fileId.set(className.replace("file-id", ""));
              }
            });
    String recruitUrl =
        NobotUtils.RECRUIT_CARD_URL
            + "?fileid="
            + fileId
            + "&cardid="
            + cardId
            + "&status=2&mode=1";
    httpClient.makePOSTRequest(recruitUrl, "GET", null, token);
    log.info("Tutorial task: complete quest move card for {}.", login);
    httpClient.makePOSTRequest(NobotUtils.TUTORIAL_URL, "POST", "complete=5", token);
  }

  private void replaceCard() throws UnsupportedEncodingException {
    log.info("Tutorial task: replace card for {}.", login);
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.MANAGE_DECK_URL, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.MANAGE_DECK_URL).getString("body"));
    Element form = doc.selectFirst("#work").selectFirst("form");
    String deckCardId = "";
    String deckBaseId = "";
    for (Element input : form.children()) {
      String name = input.attr("name");
      if (name.equals("reserve-card1")) {
        deckCardId = input.attr("value");
      } else if (name.equals("reserve-base1")) {
        deckBaseId = input.attr("value");
      }
    }
    StringBuilder postData = new StringBuilder();
    for (Element input : form.children()) {
      String name = input.attr("name");
      if (name.startsWith("reserve-") || name.equals("")) {
        continue;
      }
      if (postData.length() > 0) {
        postData.append("&");
      }
      postData.append(name).append("=");
      switch (name) {
        case "deck-card1":
        case "leader-value":
          postData.append(deckCardId);
          break;
        case "deck-base1":
          postData.append(deckBaseId);
          break;
        case "joint-skill-data":
          postData.append(URLEncoder.encode(input.attr("value"), "UTF-8"));
          break;
        default:
          postData.append(input.attr("value"));
          break;
      }
    }
    httpClient.makePOSTRequest(NobotUtils.MANAGE_DECK_URL, "POST", postData.toString(), token);
    log.info("Tutorial task: complete quest replace card for {}.", login);
    httpClient.makePOSTRequest(NobotUtils.TUTORIAL_URL, "POST", "complete=6", token);
  }

  private void buildFoodFacility() throws InterruptedException {
    log.info("Tutorial task: construct food facility for {}.", login);
    httpClient.makePOSTRequest(NobotUtils.BUILD_URL, "POST", "x=0&y=0&facility=food", token);
    log.info("Tutorial task: wait for construct food facility to complete for {}.", login);
    Thread.sleep(7000);
    httpClient.makePOSTRequest(NobotUtils.NOTIFY_UPDATE_URL, "POST", "notify_flag_1=128", token);
    log.info("Tutorial task: complete quest construct food facility for {}.", login);
    httpClient.makePOSTRequest(NobotUtils.TUTORIAL_URL, "POST", "complete=7", token);
  }

  private void trainFire() throws InterruptedException {
    log.info("Tutorial task: train fire for {}.", login);
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.VILLAGE_URL, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.VILLAGE_URL).getString("body"));
    AtomicReference<String> cardId = new AtomicReference<>("");
    doc.selectFirst("img[alt=キャットう清正]")
        .classNames()
        .forEach(
            className -> {
              if (className.startsWith("face-card-id")) {
                cardId.set(className.replace("face-card-id", ""));
              }
            });
    String dropUrl = NobotUtils.DROP_DIALOG_URL + "?cardid=" + cardId + "&index=6&type=3";
    response = httpClient.makePOSTRequest(dropUrl, "GET", null, token);
    obj = HttpUtils.responseToJsonObject(response.getBody());
    doc = Jsoup.parse(obj.getJSONObject(dropUrl).getString("body"));
    Element form = doc.selectFirst("#drop-command");
    String actionUrl = form.attr("action");
    httpClient.makePOSTRequest(actionUrl, "POST", HttpUtils.buildPostData(form), token);
    log.info("Tutorial task: wait for train fire to complete for {}.", login);
    Thread.sleep(7000);
    log.info("Tutorial task: complete quest train fire for {}.", login);
    httpClient.makePOSTRequest(NobotUtils.TUTORIAL_URL, "POST", "complete=11", token);
  }

  private void battle2() throws InterruptedException {
    log.info("Tutorial task: battle2 for {}.", login);
    battle();
    httpClient.makePOSTRequest(NobotUtils.NOTIFY_UPDATE_URL, "POST", "notify_flag_1=64", token);
    battle();
    httpClient.makePOSTRequest(
        NobotUtils.NOTIFY_UPDATE_URL, "POST", "notify_flag_1=4&notify_flag_2=16384", token);
    log.info("Tutorial task: complete quest battle2 for {}.", login);
    httpClient.makePOSTRequest(NobotUtils.TUTORIAL_URL, "GET", null, token);
    httpClient.makePOSTRequest(NobotUtils.TUTORIAL_URL, "POST", "complete=33", token);
  }

  private void watchReplay() {
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.REPORT_LIST_URL, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.REPORT_LIST_URL).getString("body"));
    String replayUrl =
        NobotUtils.BASE_URL
            + doc.selectFirst("#content table tr:nth-child(2) td:nth-child(3)")
                .selectFirst(".right")
                .selectFirst("a")
                .attr("href");
    log.info("Tutorial task: watch replay for {}.", login);
    httpClient.makePOSTRequest(replayUrl, "GET", null, token);
    log.info("Tutorial task: complete quest watch replay for {}.", login);
    httpClient.makePOSTRequest(NobotUtils.TUTORIAL_URL, "POST", "complete=17", token);
  }

  private void battle() throws InterruptedException {
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.MAP_URL, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.MAP_URL).getString("body"));
    Element form =
        doc.selectFirst("img.map_point_e").parent().nextElementSibling().selectFirst("form");
    log.info("Tutorial task: target found for {}.", login);
    response =
        httpClient.makePOSTRequest(
            NobotUtils.MAP_URL, "POST", HttpUtils.buildPostData(form), token);
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
    form = doc.selectFirst("#command_ok").selectFirst("form");
    httpClient.makePOSTRequest(NobotUtils.BATTLE_URL, "POST", HttpUtils.buildPostData(form), token);
    log.info("Tutorial task: start battle for {}.", login);
    Thread.sleep(7000);
    httpClient.makePOSTRequest(NobotUtils.MAP_URL, "GET", null, token);
  }

  public void setLogin(String login) {
    this.login = login;
  }
}
