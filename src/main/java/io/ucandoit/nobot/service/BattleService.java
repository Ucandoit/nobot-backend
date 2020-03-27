package io.ucandoit.nobot.service;

import io.ucandoit.nobot.enums.FriendshipLevel;
import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.task.BattleTask;
import io.ucandoit.nobot.util.HttpUtils;
import io.ucandoit.nobot.util.NobotUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class BattleService {

  @Resource private BeanFactory beanFactory;
  @Resource private HttpClient httpClient;
  @Resource private CacheService cacheService;

  private ExecutorService executorService = Executors.newFixedThreadPool(200);

  private Map<String, Future<?>> futureMap = new HashMap<>();

  public void startBattle(String login, Integer times) {
    Future<?> future = futureMap.get(login);
    if (future != null && !future.isDone()) {
      future.cancel(true);
    }
    BattleTask battleTask = (BattleTask) beanFactory.getBean("battleTask");
    battleTask.setLogin(login);
    battleTask.setTimes(times == null ? -1 : times);
    future = executorService.submit(battleTask);
    futureMap.put(login, future);
  }

  public void stopBattle(String login) {
    log.info("Stop battle for {}.", login);
    Future<?> future = futureMap.get(login);
    if (future != null && !future.isDone()) {
      future.cancel(true);
    }
  }

  public Map<String, FriendshipLevel> getFriendships(String login) {
    Map<String, FriendshipLevel> friendshipMap = new LinkedHashMap<>();
    Optional<String> token = cacheService.getToken(login);
    if (token.isPresent()) {
      friendshipMap = getFriendshipsByToken(token.get());
    }
    return friendshipMap;
  }

  public Map<String, FriendshipLevel> getFriendshipsByToken(String token) {
    Map<String, FriendshipLevel> friendshipMap = new LinkedHashMap<>();
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.PROFILE_URL, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.PROFILE_URL).getString("body"));
    Elements friendships = doc.select(".friendship");
    if (friendships != null) {
      for (Element friendship : friendships) {
        friendshipMap.put(
            friendship.selectFirst("th").text(),
            FriendshipLevel.fromLabel(friendship.selectFirst("td").text()));
      }
    }
    return friendshipMap;
  }

  public List<String> getBattlingAccounts() {
    List<String> accounts = new ArrayList<>();
    if (futureMap != null) {
      for (Map.Entry<String, Future<?>> entry : futureMap.entrySet()) {
        Future<?> future = entry.getValue();
        if (future != null && !future.isDone()) {
          accounts.add(entry.getKey());
        }
      }
    }
    return accounts;
  }

  public boolean getStatus(String login) {
    Future<?> future = futureMap.get(login);
    return future != null && !future.isDone();
  }
}
