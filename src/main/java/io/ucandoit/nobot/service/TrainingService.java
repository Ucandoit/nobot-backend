package io.ucandoit.nobot.service;

import io.ucandoit.nobot.enums.Training;
import io.ucandoit.nobot.task.TrainingTask;
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
public class TrainingService {

  @Resource private BeanFactory beanFactory;

  private ExecutorService executorService = Executors.newFixedThreadPool(200);

  //  private Map<String, Map<String, Future<?>>> futureMap = new HashMap<>();

  private Map<String, Future<?>> futureMap = new HashMap<>();

  public void startTraining(
      String login, String cardId, Training training, int level, int targetLevel) {
    Future<?> future = futureMap.get(cardId);
    if (future != null && !future.isDone()) {
      future.cancel(true);
    }
    TrainingTask trainingTask = (TrainingTask) beanFactory.getBean("trainingTask");
    trainingTask.setLogin(login);
    trainingTask.setCardId(cardId);
    trainingTask.setTraining(training);
    trainingTask.setLevel(level);
    trainingTask.setTargetLevel(targetLevel);
    future = executorService.submit(trainingTask);
    futureMap.put(cardId, future);
  }
}
