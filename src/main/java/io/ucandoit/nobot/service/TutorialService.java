package io.ucandoit.nobot.service;

import io.ucandoit.nobot.task.TutorialTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class TutorialService {

  @Resource private BeanFactory beanFactory;

  private ExecutorService executorService = Executors.newFixedThreadPool(50);

  private Map<String, Future<?>> futureMap = new HashMap<>();

  public void startTutorial(String login) {
    Future<?> future = futureMap.get(login);
    if (future != null && !future.isDone()) {
      future.cancel(true);
    }
    TutorialTask tutorialTask = (TutorialTask) beanFactory.getBean("tutorialTask");
    tutorialTask.setLogin(login);
    future = executorService.submit(tutorialTask);
    futureMap.put(login, future);
  }
}
