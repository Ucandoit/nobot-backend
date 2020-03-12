package io.ucandoit.nobot.task;

import io.ucandoit.nobot.dto.AccountInfo;
import io.ucandoit.nobot.dto.BuildCost;
import io.ucandoit.nobot.dto.BuildTarget;
import io.ucandoit.nobot.dto.MapArea;
import io.ucandoit.nobot.enums.Building;
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

@Slf4j
@Component("buildTask")
@Scope("prototype")
public class BuildTask implements Runnable {

  @Resource private CacheService cacheService;

  @Resource private AccountService accountService;

  @Resource private HttpClient httpClient;

  @Resource private Map<Building, Map<Integer, BuildCost>> buildCostMap;

  private String login;

  private String token;

  private List<MapArea> areas;

  private AccountInfo currentInfo = new AccountInfo();

  @Override
  public void run() {
    try {
      cacheService.getToken(login).ifPresent(s -> token = s);
      if (token != null) {
        getMapInfo();
        checkBuild();
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

  private void getMapInfo() {
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.VILLAGE_URL, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.VILLAGE_URL).getString("body"));
    areas = accountService.getMapInfo(doc);
    currentInfo.setNp(NobotUtils.getIntValueById(doc, "lottery_point"));
    currentInfo.setFood(NobotUtils.getIntValueById(doc, "element_food"));
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
  }

  private void checkBuild() throws InterruptedException {
    boolean stop = false;
    while (!stop) {
      // wait until finish
      boolean finish = checkFinish();
      while (!finish) {
        log.info("Build task: Waiting for build finish for {}", login);
        Thread.sleep(10 * 1000);
        finish = checkFinish();
      }
      BuildTarget buildTarget = getBuildTarget();
      if (buildTarget == null) {
        stop = true;
        log.info("Build task: Stop build for {}", login);
      } else {
        build(buildTarget);
      }
    }
  }

  private BuildTarget getBuildTarget() {
    // refresh map info
    getMapInfo();
    // build storage
    BuildTarget buildTarget = getBuildTarget(Building.STORAGE, 3);
    if (buildTarget != null) {
      return buildTarget;
    }
    // build food
    buildTarget = getBuildTarget(Building.FOOD, 3);
    if (buildTarget != null) {
      return buildTarget;
    }
    // build paddy
    buildTarget = getBuildTarget(Building.PADDY, 3);
    if (buildTarget != null) {
      return buildTarget;
    }
    // build market
    buildTarget = getBuildTarget(Building.MARKET, 1);
    if (buildTarget != null) {
      return buildTarget;
    }
    // build home
    buildTarget = getBuildTarget(Building.HOME_BASIC, 1);
    if (buildTarget != null) {
      return buildTarget;
    }
    // build fire
    buildTarget = getBuildTarget(Building.FIRE, 1);
    if (buildTarget != null) {
      return buildTarget;
    }
    // build earth
    buildTarget = getBuildTarget(Building.EARTH, 1);
    if (buildTarget != null) {
      return buildTarget;
    }
    // build wind
    buildTarget = getBuildTarget(Building.WIND, 1);
    if (buildTarget != null) {
      return buildTarget;
    }
    // build water
    buildTarget = getBuildTarget(Building.WATER, 1);
    if (buildTarget != null) {
      return buildTarget;
    }
    // build sky
    buildTarget = getBuildTarget(Building.SKY, 1);
    if (buildTarget != null) {
      return buildTarget;
    }
    log.info("Nothing to build for {}", login);
    return null;
  }

  private BuildTarget getBuildTarget(Building building, int max) {
    if (countBuildings(building.getType()) < max) {
      MapArea area = getEmptyArea();
      BuildCost buildCost = buildCostMap.get(building).get(area.getLevel());
      if (costEnough(buildCost)) {
        return new BuildTarget(building, false, area.getX(), area.getY(), buildCost.getSeconds());
      } else {
        log.info("Build task: Short of food to build {} for {}", building.getTitle(), login);
      }
    } else {
      MapArea area = getLowestArea(building.getType());
      if (area != null) {
        BuildCost buildCost = buildCostMap.get(building).get(area.getLevel());
        if (costEnough(buildCost)) {
          return new BuildTarget(building, true, area.getX(), area.getY(), buildCost.getSeconds());
        } else {
          log.info("Build task: Short of food to extend {} for {}", building.getTitle(), login);
        }
      }
    }
    return null;
  }

  private void build(BuildTarget buildTarget) throws InterruptedException {
    int waitSeconds = buildTarget.getSeconds();
    if (currentInfo.isNewUser()) {
      waitSeconds = waitSeconds / 6;
    }
    if (buildTarget.isExtend()) {
      log.info("Build task: Extend {} for {}", buildTarget.getBuilding().getTitle(), login);
      httpClient.makePOSTRequest(
          NobotUtils.COMMAND_URL, "POST", buildTarget.getPosition() + "&command=extend", token);
    } else {
      log.info("Build task: Build {} for {}", buildTarget.getBuilding().getTitle(), login);
      httpClient.makePOSTRequest(
          NobotUtils.BUILD_URL,
          "POST",
          buildTarget.getPosition() + "&facility=" + buildTarget.getBuilding().getFacility(),
          token);
    }
    log.info("Build task: Waiting for build to complete in {} seconds for {}", waitSeconds, login);
    Thread.sleep(waitSeconds * 1000);
    // update token after wait because it takes time
    cacheService.getToken(login).ifPresent(s -> token = s);
  }

  private boolean checkFinish() throws RuntimeException {
    log.info("Build task: Checking build finish for {}", login);
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.VILLAGE_URL, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.VILLAGE_URL).getString("body"));
    Element doing = doc.selectFirst("#doing");
    return !doing.toString().contains("建設中") && !doing.toString().contains("増築中");
  }

  private int countBuildings(String type) {
    int count = 0;
    for (MapArea area : areas) {
      if (area.getBuildingType().equals(type)) {
        count++;
      }
    }
    return count;
  }

  private MapArea getEmptyArea() {
    for (MapArea area : areas) {
      if (area.getBuildingType().equals("type00")) {
        return area;
      }
    }
    log.error("Build task: No empty place for {}.", login);
    throw new RuntimeException("Stop");
  }

  private MapArea getLowestArea(String type) {
    MapArea ret = null;
    int level = 9;
    for (MapArea area : areas) {
      if (area.getBuildingType().equals(type)
          && area.getLevel() < 9
          && area.getLevel() < level
          && !area.isConstructing()) {
        ret = area;
        level = area.getLevel();
      }
    }
    return ret;
  }

  private boolean costEnough(BuildCost buildCost) {
    return buildCost.getFire() <= currentInfo.getFire()
        && buildCost.getEarth() <= currentInfo.getEarth()
        && buildCost.getWind() <= currentInfo.getWind()
        && buildCost.getWater() <= currentInfo.getWater()
        && buildCost.getSky() <= currentInfo.getSky();
  }

  public void setLogin(String login) {
    this.login = login;
  }
}
