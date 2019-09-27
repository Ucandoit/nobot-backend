package io.ucandoit.nobot.task;

import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

@Slf4j
@Component("battleTask")
@Scope("prototype")
public class BattleTask implements Runnable {

  @Resource private HttpClient httpClient;

  private String cookie;

  private String login;

  @Override
  public void run() {
    try {
      Optional<String> token = HttpUtils.requestToken(httpClient, cookie);
    } catch (Exception e) {
      log.error("Error while battling for account {}.", login);
    }
  }

  public void setCookie(String cookie) {
    this.cookie = cookie;
  }

  public void setLogin(String login) {
    this.login = login;
  }
}
