package io.ucandoit.nobot.service;

import io.ucandoit.nobot.model.Account;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.task.WrestleTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WrestleService {

  @Resource private AccountRepository accountRepository;

  @Resource private BeanFactory beanFactory;

  private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(50);

  private Map<String, ScheduledFuture<?>> futureMap = new HashMap<>();

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
        wrestleTask.setCookie(account.getCookie());
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
}
