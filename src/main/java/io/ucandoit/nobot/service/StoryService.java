package io.ucandoit.nobot.service;

import io.ucandoit.nobot.model.Account;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.task.StoryTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
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
public class StoryService {

  @Resource private AccountRepository accountRepository;

  @Resource private BeanFactory beanFactory;

  private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(50);

  private Map<String, ScheduledFuture<?>> futureMap = new HashMap<>();

  public void startAll() {
    stopAll();
    List<Account> accounts = accountRepository.findAll();
    for (Account account : accounts) {
      startStory(account);
    }
  }

  public void stopAll() {
    if (executorService != null) {
      executorService.shutdown();
      executorService = Executors.newScheduledThreadPool(50);
      futureMap = new HashMap<>();
    }
  }

  public void startStory(String login) {
    startStory(accountRepository.getOne(login));
  }

  public void stopStory(String login) {
    ScheduledFuture<?> future = futureMap.get(login);
    if (future != null && !future.isDone()) {
      future.cancel(true);
    }
  }

  private void startStory(Account account) {
    if (account != null) {
      if (account.getExpirationDate().after(new Date())) {
        ScheduledFuture<?> future = futureMap.get(account.getLogin());
        if (future != null && !future.isDone()) {
          future.cancel(true);
        }
        StoryTask storyTask = (StoryTask) beanFactory.getBean("storyTask");
        storyTask.setLogin(account.getLogin());
        storyTask.setCookie(account.getCookie());
        future = executorService.scheduleAtFixedRate(storyTask, 0, 60, TimeUnit.SECONDS);
        futureMap.put(account.getLogin(), future);
      } else {
        log.error("Cookie expired for account {}.", account.getLogin());
      }
    } else {
      log.error("Account {} not found.", account.getLogin());
    }
  }
}
