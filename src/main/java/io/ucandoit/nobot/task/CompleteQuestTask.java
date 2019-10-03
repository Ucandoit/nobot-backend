package io.ucandoit.nobot.task;

import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component("completeQuestTask")
@Scope("prototype")
public class CompleteQuestTask implements Runnable {

  private static final String MISSION_URL = "http://210.140.157.168/tutorial/mission.htm";
  @Resource private HttpClient httpClient;
  private String login;
  private String cookie;
  private List<Integer> questIds;

  @Override
  public void run() {
    log.info("Complete war quests for {}", login);
    HttpUtils.requestToken(httpClient, cookie)
        .ifPresent(
            token -> {
              for (Integer questId : questIds) {
                log.info("Accept quest {} for {}", questId, login);
                httpClient.makePOSTRequest(MISSION_URL, "POST", "contract=" + questId, token);
                log.info("Complete quest {} for {}", questId, login);
                httpClient.makePOSTRequest(MISSION_URL, "POST", "complete=" + questId, token);
              }
            });
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public void setCookie(String cookie) {
    this.cookie = cookie;
  }

  public void setQuestIds(List<Integer> questIds) {
    this.questIds = questIds;
  }
}
