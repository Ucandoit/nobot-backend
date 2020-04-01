package io.ucandoit.nobot.task;

import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.service.CacheService;
import io.ucandoit.nobot.util.NobotUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component("completeQuestTask")
@Scope("prototype")
public class CompleteQuestTask implements Runnable {

  @Resource private HttpClient httpClient;
  @Resource private CacheService cacheService;
  private String login;
  private List<Integer> questIds;

  @Override
  public void run() {
    log.info("Complete war quests for {}", login);
    cacheService
        .getToken(login)
        .ifPresent(
            token -> {
              for (Integer questId : questIds) {
                log.info("Accept quest {} for {}", questId, login);
                httpClient.makePOSTRequest(
                    NobotUtils.MISSION_URL, "POST", "contract=" + questId, token);
                log.info("Complete quest {} for {}", questId, login);
                httpClient.makePOSTRequest(
                    NobotUtils.MISSION_URL, "POST", "complete=" + questId, token);
              }
            });
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public void setQuestIds(List<Integer> questIds) {
    this.questIds = questIds;
  }
}
