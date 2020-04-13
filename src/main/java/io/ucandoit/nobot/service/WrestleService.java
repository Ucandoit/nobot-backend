package io.ucandoit.nobot.service;

import io.ucandoit.nobot.model.Account;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.task.CountryWrestleTask;
import io.ucandoit.nobot.task.WrestleTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Service
@Slf4j
public class WrestleService {

  @Resource private AccountRepository accountRepository;

  @Resource private BeanFactory beanFactory;

  private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(50);

  private ExecutorService countryWrestleExecutorService = Executors.newFixedThreadPool(100);

  private Map<String, ScheduledFuture<?>> futureMap = new HashMap<>();

  private Map<String, Future<?>> countryWrestleMap = new HashMap<>();

  public void startWrestling(String login) {
    Account account = accountRepository.getOne(login);
    if (account != null) {
      log.info("Start wrestling for account {}.", login);
      if (account.getExpirationDate().after(new Date())) {
        ScheduledFuture<?> future = futureMap.get(login);
        if (future != null && !future.isDone()) {
          future.cancel(true);
        }
        WrestleTask wrestleTask = (WrestleTask) beanFactory.getBean("wrestleTask");
        wrestleTask.setLogin(account.getLogin());
        future = executorService.scheduleAtFixedRate(wrestleTask, 0, 5, TimeUnit.SECONDS);
        futureMap.put(login, future);
      } else {
        log.error("Cookie expired for account {}.", login);
      }
    } else {
      log.error("Account {} not found.", login);
    }
  }

  public void stopWrestling(String login) {
    ScheduledFuture<?> future = futureMap.get(login);
    if (future != null && !future.isDone()) {
      future.cancel(true);
    }
  }

  public void startCountryWrestling(String login, Integer times) {
    Future<?> future = countryWrestleMap.get(login);
    if (future != null && !future.isDone()) {
      future.cancel(true);
    }
    CountryWrestleTask countryWrestleTask =
        (CountryWrestleTask) beanFactory.getBean("countryWrestleTask");
    countryWrestleTask.setLogin(login);
    if (times != null) {
      countryWrestleTask.setTimes(times);
    }
    future = countryWrestleExecutorService.submit(countryWrestleTask);
    countryWrestleMap.put(login, future);
  }

  public void stopCountryWrestling(String login) {
    log.info("Stop country wrestle for {}.", login);
    Future<?> future = countryWrestleMap.get(login);
    if (future != null && !future.isDone()) {
      future.cancel(true);
    }
  }
}
