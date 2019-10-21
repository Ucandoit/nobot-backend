package io.ucandoit.nobot.service;

import io.ucandoit.nobot.enums.WarStatus;
import io.ucandoit.nobot.model.Account;
import io.ucandoit.nobot.model.Parameter;
import io.ucandoit.nobot.model.WarConfig;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.repository.ParameterRepository;
import io.ucandoit.nobot.repository.WarConfigRepository;
import io.ucandoit.nobot.task.CompleteQuestTask;
import io.ucandoit.nobot.task.WarTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@Slf4j
public class WarService {

  @Value("${scheduler.enable:true}")
  private boolean enable;

  @Resource private AccountRepository accountRepository;

  @Resource private WarConfigRepository warConfigRepository;

  @Resource private BeanFactory beanFactory;

  @Resource private ParameterRepository parameterRepository;

  private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(50);

  private ExecutorService questExecutorService = Executors.newFixedThreadPool(50);

  private Map<String, ScheduledFuture<?>> futureMap = new HashMap<>();

  @Scheduled(cron = "0 59 5 * * *")
  public void dailyStop() {
    if (enable) {
      log.info("Daily stop.");
      stopAll();
    } else {
      log.info("Scheduler for stop war disabled.");
    }
  }

  @Scheduled(cron = "0 1 7 * * *")
  @Transactional
  public void dailyStart() {
    if (enable) {
      log.info("Daily start.");
      startAll();
    } else {
      log.info("Scheduler for start war disabled.");
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
    WarStatus warStatus = checkWarStatus(new Date());
    if (warStatus == WarStatus.STOP) {
      log.info("War is stopped");
      return;
    }
    List<WarConfig> warConfigList = warConfigRepository.findAll();
    for (WarConfig warConfig : warConfigList) {
      if (warConfig.isEnabled()) {
        Account account = accountRepository.getOne(warConfig.getLogin());
        if (account != null) {
          if (account.getExpirationDate().after(new Date())) {
            WarTask warTask = (WarTask) beanFactory.getBean("warTask");
            warTask.setLogin(account.getLogin());
            warTask.setLine(warConfig.getLine());
            warTask.setFp(warConfig.isFp());
            warTask.setNpc(warConfig.isNpc());
            warTask.setLastDay(warStatus == WarStatus.LAST_DAY);
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
    WarStatus warStatus = checkWarStatus(new Date());
    if (warStatus == WarStatus.STOP) {
      log.info("War is stopped");
      return;
    }
    Account account = accountRepository.getOne(login);
    if (account != null) {
      if (account.getExpirationDate().after(new Date())) {
        ScheduledFuture<?> future = futureMap.get(login);
        if (future != null && !future.isDone()) {
          future.cancel(true);
        }
        WarTask warTask = (WarTask) beanFactory.getBean("warTask");
        warTask.setLogin(account.getLogin());
        warTask.setLine(Integer.parseInt(line));
        warTask.setFp(fp != null && fp);
        warTask.setNpc(npc != null && npc);
        warTask.setLastDay(warStatus == WarStatus.LAST_DAY);
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
      future.cancel(true);
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
    StringBuilder sb = new StringBuilder();
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

  public void completeQuest(String login, List<Integer> questIds) {
    CompleteQuestTask completeQuestTask =
        (CompleteQuestTask) beanFactory.getBean("completeQuestTask");
    completeQuestTask.setLogin(login);
    completeQuestTask.setQuestIds(questIds);
    questExecutorService.submit(completeQuestTask);
  }

  private WarStatus checkWarStatus(Date date) {
    try {
      Parameter parameter = parameterRepository.getOne("war.lastDay");
      Date lastDay = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(parameter.getValue());
      if (lastDay.after(date)) {
        return WarStatus.START;
      } else {
        if (date.getTime() - lastDay.getTime() < 24 * 60 * 60 * 1000) {
          return WarStatus.LAST_DAY;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return WarStatus.STOP;
  }
}
