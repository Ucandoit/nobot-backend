package io.ucandoit.nobot.service;

import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.model.Account;
import io.ucandoit.nobot.model.WarConfig;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.repository.WarConfigRepository;
import io.ucandoit.nobot.task.WarTask;
import io.ucandoit.nobot.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WarService {

  @Resource private AccountRepository accountRepository;

  @Resource private HttpClient httpClient;

  @Resource private WarConfigRepository warConfigRepository;

  @Resource private BeanFactory beanFactory;

  private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(50);

  private Map<String, ScheduledFuture<?>> futureMap = new HashMap<>();

  //    @Scheduled(cron = "0 59 5 * * *")
  public void dailyStop() {
    log.info("Daily stop.");
    stopAll();
  }

  //    @Scheduled(cron = "0 1 7 * * *")
  //    @Transactional
  public void dailyStart() {
    log.info("Daily start.");
    startAll();
  }

  @Scheduled(cron = "0 1 17 * * *")
  public void dailyLogin() {
    login();
  }

  public void login() {
    List<Account> accounts = accountRepository.findAll();
    for (Account account : accounts) {
      String token = HttpUtils.requestToken(httpClient, account.getCookie());
      if (token != null) {
        httpClient.makePOSTRequest("http://210.140.157.168/world_list.htm", "GET", null, token);
      }
    }
  }

  public void stopAll() {
    if (executorService != null) {
      executorService.shutdown();
      executorService = Executors.newScheduledThreadPool(50);
      futureMap = new HashMap<>();
    }
  }

  public void startAll() {
    stopAll();
    List<WarConfig> warConfigList = warConfigRepository.findAll();
    for (WarConfig warConfig : warConfigList) {
      if (warConfig.isEnabled()) {
        Account account = accountRepository.getOne(warConfig.getLogin());
        if (account != null) {
          if (account.getExpirationDate().after(new Date())) {
            WarTask warTask = (WarTask) beanFactory.getBean("warTask");
            warTask.setCookie(account.getCookie());
            warTask.setLogin(account.getLogin());
            warTask.setLine(warConfig.getLine());
            warTask.setFp(warConfig.isFp());
            warTask.setEndDay(warConfig.getEndDay());
            ScheduledFuture<?> future =
                executorService.scheduleAtFixedRate(warTask, 0, 120, TimeUnit.SECONDS);
            futureMap.put(account.getLogin(), future);
          } else {
            log.error("Cookie expired for account {}.", warConfig.getLogin());
          }
        } else {
          log.error("Account {} not found.", warConfig.getLogin());
        }
      }
    }
  }

  public void startWar(String login, String line, Boolean fp, Boolean npc) {
    Account account = accountRepository.getOne(login);
    if (account != null) {
      if (account.getExpirationDate().after(new Date())) {
        ScheduledFuture<?> future = futureMap.get(login);
        if (future != null && !future.isDone()) {
          future.cancel(false);
        }
        WarConfig warConfig = warConfigRepository.getOne(login);
        WarTask warTask = (WarTask) beanFactory.getBean("warTask");
        warTask.setCookie(account.getCookie());
        warTask.setLogin(account.getLogin());
        warTask.setLine(Integer.parseInt(line));
        warTask.setFp(fp != null && fp);
        warTask.setNpc(npc != null && npc);
        if (warConfig != null) {
          warTask.setEndDay(warConfig.getEndDay());
        }
        future = executorService.scheduleAtFixedRate(warTask, 0, 120, TimeUnit.SECONDS);
        futureMap.put(account.getLogin(), future);
      } else {
        log.error("Cookie expired for account {}.", login);
      }
    } else {
      log.error("Account {} not found.", login);
    }
  }

  public void stopWar(String login) {
    ScheduledFuture<?> future = futureMap.get(login);
    if (future != null && !future.isDone()) {
      future.cancel(false);
    }
  }

  public void setAllLine(int line) {
    List<WarConfig> warConfigList = warConfigRepository.findAll();
    for (WarConfig warConfig : warConfigList) {
      warConfig.setLine(line);
      warConfigRepository.save(warConfig);
    }
  }

  public void setLine(String login, int line) {
    WarConfig warConfig = warConfigRepository.getOne(login);
    warConfig.setLine(line);
    warConfigRepository.save(warConfig);
  }

  public void setAllFP(boolean fp) {
    List<WarConfig> warConfigList = warConfigRepository.findAll();
    for (WarConfig warConfig : warConfigList) {
      warConfig.setFp(fp);
      warConfigRepository.save(warConfig);
    }
  }

  public void setFP(String login, boolean fp) {
    WarConfig warConfig = warConfigRepository.getOne(login);
    warConfig.setFp(fp);
    warConfigRepository.save(warConfig);
  }

  public String getWarConfigList() {
    List<WarConfig> warConfigList = warConfigRepository.findAll();
    StringBuffer sb = new StringBuffer();
    for (WarConfig warConfig : warConfigList) {
      sb.append("ID : ")
          .append(warConfig.getLogin())
          .append("; Line : ")
          .append(warConfig.getLine())
          .append("; FP: ")
          .append(warConfig.isFp());
      ScheduledFuture<?> future = futureMap.get(warConfig.getLogin());
      sb.append(" ; Status: ");
      if (future != null && !future.isDone()) {
        sb.append("Started");
      } else {
        sb.append("Stopped");
      }
      sb.append(System.lineSeparator());
    }
    return sb.toString();
  }
}
