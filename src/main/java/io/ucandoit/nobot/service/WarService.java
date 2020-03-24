package io.ucandoit.nobot.service;

import io.ucandoit.nobot.enums.WarStatus;
import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.model.Account;
import io.ucandoit.nobot.model.Parameter;
import io.ucandoit.nobot.model.WarConfig;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.repository.ParameterRepository;
import io.ucandoit.nobot.repository.WarConfigRepository;
import io.ucandoit.nobot.task.CompleteQuestTask;
import io.ucandoit.nobot.task.WarTask;
import io.ucandoit.nobot.util.HttpUtils;
import io.ucandoit.nobot.util.NobotUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

@Service
@Slf4j
public class WarService {

  @Value("${scheduler.war.enable:true}")
  private boolean enable;

  @Resource private AccountRepository accountRepository;

  @Resource private WarConfigRepository warConfigRepository;

  @Resource private BeanFactory beanFactory;

  @Resource private ParameterRepository parameterRepository;

  @Resource private HttpClient httpClient;

  @Resource private CacheService cacheService;

  private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(200);

  private ExecutorService questExecutorService = Executors.newFixedThreadPool(50);

  private Map<String, ScheduledFuture<?>> futureMap = new HashMap<>();

  @Scheduled(cron = "${scheduler.war.stop}")
  public void dailyStop() {
    if (enable) {
      log.info("Daily stop.");
      stopAll();
    } else {
      log.info("Scheduler for stop war disabled.");
    }
  }

  @Scheduled(cron = "${scheduler.war.start}")
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
      executorService = Executors.newScheduledThreadPool(200);
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

  public void setLineForGroup(String group, int line) {
    List<WarConfig> warConfigList = warConfigRepository.findByGroup(group);
    if (warConfigList != null) {
      for (WarConfig warConfig : warConfigList) {
        warConfig.setLine(line);
        warConfigRepository.save(warConfig);
      }
    }
  }

  public void setAllFP(boolean fp) {
    List<WarConfig> warConfigList = warConfigRepository.findAll();
    for (WarConfig warConfig : warConfigList) {
      warConfig.setFp(fp);
      warConfigRepository.save(warConfig);
    }
  }

  public void setFPForGroup(String group, boolean fp) {
    List<WarConfig> warConfigList = warConfigRepository.findByGroup(group);
    if (warConfigList != null) {
      for (WarConfig warConfig : warConfigList) {
        warConfig.setFp(fp);
        warConfigRepository.save(warConfig);
      }
    }
  }

  public void setNPCForGroup(String group, boolean npc) {
    List<WarConfig> warConfigList = warConfigRepository.findByGroup(group);
    if (warConfigList != null) {
      for (WarConfig warConfig : warConfigList) {
        warConfig.setNpc(npc);
        warConfigRepository.save(warConfig);
      }
    }
  }

  public void setEnabledForGroup(String group, boolean enabled) {
    List<WarConfig> warConfigList = warConfigRepository.findByGroup(group);
    if (warConfigList != null) {
      for (WarConfig warConfig : warConfigList) {
        warConfig.setEnabled(enabled);
        warConfigRepository.save(warConfig);
      }
    }
  }

  public List<WarConfig> getWarConfigList() {
    List<WarConfig> warConfigList = warConfigRepository.findAll(Sort.by("login").ascending());
    warConfigList.forEach(
        warConfig -> {
          ScheduledFuture<?> future = futureMap.get(warConfig.getLogin());
          if (future != null && !future.isDone()) {
            warConfig.setAuto(true);
          } else {
            warConfig.setAuto(false);
          }
        });
    return warConfigList;
  }

  public void completeQuest(String login, List<Integer> questIds) {
    CompleteQuestTask completeQuestTask =
        (CompleteQuestTask) beanFactory.getBean("completeQuestTask");
    completeQuestTask.setLogin(login);
    completeQuestTask.setQuestIds(questIds);
    questExecutorService.submit(completeQuestTask);
  }

  public void completeQuestByGroup(String group, List<Integer> questIds) {
    List<WarConfig> warConfigList = warConfigRepository.findByGroup(group);
    for (WarConfig warConfig: warConfigList) {
      completeQuest(warConfig.getLogin(), questIds);
    }
  }

  public void checkWar() {
    CompletableFuture.allOf(
            warConfigRepository.findAll().stream()
                .map(
                    warConfig ->
                        CompletableFuture.runAsync(
                            () ->
                                cacheService
                                    .getToken(warConfig.getLogin())
                                    .ifPresent(
                                        token -> {
                                          ResponseEntity<String> response =
                                              httpClient.makePOSTRequest(
                                                  NobotUtils.MAP_URL, "GET", null, token);
                                          JSONObject obj =
                                              HttpUtils.responseToJsonObject(response.getBody());
                                          Document doc =
                                              Jsoup.parse(
                                                  obj.getJSONObject(NobotUtils.MAP_URL)
                                                      .getString("body"));
                                          Elements elements = doc.select(".map_point_w");
                                          String warField = "";
                                          String warHost = "";
                                          List<String> warFields = new ArrayList<>();
                                          List<String> warHosts = new ArrayList<>();
                                          boolean pc = false;
                                          for (Element element : elements) {
                                            if (element.parent().tagName().equals("a")) {
                                              warField = element.attr("alt");
                                              response =
                                                  httpClient.makePOSTRequest(
                                                      NobotUtils.WAR_SETUP_URL, "GET", null, token);
                                              obj =
                                                  HttpUtils.responseToJsonObject(
                                                      response.getBody());
                                              doc =
                                                  Jsoup.parse(
                                                      obj.getJSONObject(NobotUtils.WAR_SETUP_URL)
                                                          .getString("body"));
                                              Element checkboxPcBattle =
                                                  doc.selectFirst("#chstat_pcb");
                                              if (checkboxPcBattle != null) {
                                                pc =
                                                    checkboxPcBattle
                                                        .attr("checked")
                                                        .equals("checked");
                                              }
                                              Elements entries = doc.select(".warinfo_daimyo");
                                              for (Element entry : entries) {
                                                if (entry.selectFirst(".war_entry") != null) {
                                                  if (warHost.equals("")) {
                                                    warHost = entry.selectFirst("img").attr("alt");
                                                  } else {
                                                    warHost = "";
                                                  }
                                                }
                                                warHosts.add(entry.selectFirst("img").attr("alt"));
                                              }
                                            }
                                            warFields.add(element.attr("alt"));
                                          }

                                          warConfig.setPc(pc);
                                          JSONObject jsonObject = new JSONObject();
                                          jsonObject.put("warField", warField);
                                          jsonObject.put("warHost", warHost);
                                          jsonObject.put("warFields", warFields);
                                          jsonObject.put("warHosts", warHosts);
                                          warConfig.setStatus(jsonObject.toString());
                                          warConfigRepository.save(warConfig);
                                        })))
                .toArray(CompletableFuture[]::new))
        .join();
  }

  public void goToWarField(String login, String warField) {
    cacheService
        .getToken(login)
        .ifPresent(
            token -> {
              ResponseEntity<String> response =
                  httpClient.makePOSTRequest(NobotUtils.MAP_URL, "GET", null, token);
              JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
              Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.MAP_URL).getString("body"));
              Element form =
                  doc.selectFirst("img[alt=" + warField + "]")
                      .parent()
                      .nextElementSibling()
                      .selectFirst("form");
              httpClient.makePOSTRequest(
                  NobotUtils.MAP_URL, "POST", HttpUtils.buildPostData(form), token);
            });
  }

  public void chooseWarHost(String login, int warHost) {
    cacheService
        .getToken(login)
        .ifPresent(
            token -> {
              httpClient.makePOSTRequest(
                  NobotUtils.WAR_SETUP_URL, "POST", "action=entry_war&target=" + warHost, token);
            });
  }

  public void setPc(String login, boolean pc) {
    cacheService
        .getToken(login)
        .ifPresent(
            token -> {
              ResponseEntity<String> response =
                  httpClient.makePOSTRequest(
                      NobotUtils.WAR_SETUP_URL,
                      "POST",
                      "action=chstat_pcbattle&pcbattle_stat=" + pc,
                      token);
              JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
              Document doc =
                  Jsoup.parse(obj.getJSONObject(NobotUtils.WAR_SETUP_URL).getString("body"));
              Element checkboxPcBattle = doc.selectFirst("#chstat_pcb");
              boolean pcb = checkboxPcBattle.attr("checked").equals("checked");
              WarConfig warConfig = warConfigRepository.getOne(login);
              warConfig.setPc(pcb);
              warConfigRepository.save(warConfig);
            });
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
