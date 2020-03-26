package io.ucandoit.nobot.task;

import io.ucandoit.nobot.dto.AccountInfo;
import io.ucandoit.nobot.dto.MapArea;
import io.ucandoit.nobot.dto.ResourceCost;
import io.ucandoit.nobot.enums.Training;
import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.service.AccountService;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component("trainingTask")
@Scope("prototype")
public class TrainingTask implements Runnable {

  @Resource private CacheService cacheService;

  @Resource private HttpClient httpClient;

  @Resource private AccountService accountService;

  @Resource private Map<Training, Map<Integer, ResourceCost>> trainingCostMap;

  private String login;

  private String cardId;

  private Training training;

  private int level;

  private int targetLevel;

  private String token;

  private AccountInfo currentInfo = new AccountInfo();

  @Override
  public void run() {
    try {
      cacheService.getToken(login).ifPresent(s -> token = s);
      if (token != null) {
        checkTraining();
      }

    } catch (Exception e) {
      log.error("Tutorial task: error for " + login + " : ", e);
      if ("Stop".equals(e.getMessage())) {
        try {
          throw e;
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  private void checkTraining() throws InterruptedException {
    boolean stop = false;
    while (!stop) {
      // wait until finish
      boolean finish = checkFinish();
      while (!finish) {
        log.info("Training task: Waiting for cat {} to finish for {}", cardId, login);
        Thread.sleep(10 * 1000);
        finish = checkFinish();
      }

      if (level < targetLevel) {
        ResponseEntity<String> response =
            httpClient.makePOSTRequest(NobotUtils.VILLAGE_URL, "GET", null, token);
        JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
        Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.VILLAGE_URL).getString("body"));
        currentInfo.setFire(NobotUtils.getIntValueById(doc, "element_fire"));
        currentInfo.setEarth(NobotUtils.getIntValueById(doc, "element_earth"));
        currentInfo.setWind(NobotUtils.getIntValueById(doc, "element_wind"));
        currentInfo.setWater(NobotUtils.getIntValueById(doc, "element_water"));
        currentInfo.setSky(NobotUtils.getIntValueById(doc, "element_sky"));
        if (doc.selectFirst("#newuserbutton") != null) {
          currentInfo.setNewUser(true);
        } else {
          currentInfo.setNewUser(false);
        }
        ResourceCost trainingCost = trainingCostMap.get(training).get(level);
        if (NobotUtils.costEnough(trainingCost, currentInfo)) {
          List<MapArea> areas = accountService.getMapInfo(doc);
          // find training building that is not running
          Optional<MapArea> building =
              areas.stream()
                  .filter(
                      area ->
                          area.getBuilding() != null
                              && area.getBuilding().equals(training.getTrainingBuilding())
                              && !area.isRunning())
                  .findFirst();
          if (building.isPresent()) {
            if ((building.get().getLevel() + 1) * 2 > level) {
              log.info(
                  "Training task: Training level {} {} for {} of {}",
                  level,
                  training.name(),
                  cardId,
                  login);
              httpClient.makePOSTRequest(
                  NobotUtils.COMMAND_URL,
                  "POST",
                  "targetlv=-1&"
                      + building.get().getPosition()
                      + "&cardid="
                      + cardId
                      + "&command="
                      + training.getCommand(),
                  token);
              int waitSeconds =
                  currentInfo.isNewUser()
                      ? trainingCost.getReducedSeconds()
                      : trainingCost.getSeconds();
              log.info(
                  "Training task: Waiting for train to complete in {} seconds for {} of {}",
                  waitSeconds,
                  cardId,
                  login);
              Thread.sleep(waitSeconds * 1000);
              level++;
              // update token after wait because it takes time
              cacheService.getToken(login).ifPresent(s -> token = s);
            } else {
              stop = true;
              log.info(
                  "Training task: Training building doesn't have enough level for {} of {}",
                  cardId,
                  login);
            }
          } else {
            stop = true;
            log.info(
                "Training task: No training building is available for {} of {}", cardId, login);
          }
        } else {
          stop = true;
          log.info("Training task: Short of resource for training {} of {}", cardId, login);
        }
      } else {
        stop = true;
        log.info("Training task: training finish for {} of {}", cardId, login);
      }
    }
  }

  private boolean checkFinish() throws RuntimeException {
    log.info("Training task: Checking training status for card {} of {}", cardId, login);
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.VILLAGE_URL, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.VILLAGE_URL).getString("body"));
    Element card = doc.selectFirst(".face-card-id" + cardId);
    if (card == null) {
      log.error("Unable to find card {} of {}.", cardId, login);
      throw new RuntimeException("Stop");
    }
    return !card.hasClass("action");
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public void setCardId(String cardId) {
    this.cardId = cardId;
  }

  public void setTraining(Training training) {
    this.training = training;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public void setTargetLevel(int targetLevel) {
    this.targetLevel = targetLevel;
  }
}
