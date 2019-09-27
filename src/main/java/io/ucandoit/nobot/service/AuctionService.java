package io.ucandoit.nobot.service;

import io.ucandoit.nobot.model.Account;
import io.ucandoit.nobot.model.Task;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.repository.TaskRepository;
import io.ucandoit.nobot.task.SearchAHTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuctionService {

    @Resource
    private AccountRepository accountRepository;

    @Resource
    private TaskRepository taskRepository;

    @Resource
    private BeanFactory beanFactory;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(50);

    private Map<String, SearchAHTask> taskMap = new HashMap<>();

    @Scheduled(cron = "0 0 17 * * *")
    public void restart() {
        log.info("Daily restart.");
        if (executorService != null) {
            executorService.shutdown();
            executorService = Executors.newScheduledThreadPool(50);
        }
        taskMap = new HashMap<>();
        taskRepository.deleteAll();

        snipeAH();
    }

    public void snipeAH() {
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            if (account.getEnabled()) {
                startSnipeAH(account, 0,false);
            }
        }
    }

    public void snipeAH(String login) {
        SearchAHTask searchAHTask = taskMap.get(login);
        if (searchAHTask != null) {
            if (searchAHTask.getFuture() != null && !searchAHTask.getFuture().isDone()) {
                log.info("Snipe for account {} is already in progress", login);
                return;
            }
            Task task = taskRepository.getOne(searchAHTask.getTaskId());
            if (task != null && task.getRepeat() >= 2000) {
                log.info("Snipe for account {} is finished today.", login);
            } else {
                // continue if max attempts not reached
                Account account = accountRepository.getOne(login);
                startSnipeAH(account, task.getRepeat(),true);
            }
        } else {
            Account account = accountRepository.getOne(login);
            startSnipeAH(account, 0,true);
        }
    }

    public void stopSnipeAH(String login) {
        log.info("Stop sniping AH for account {}.", login);
        SearchAHTask searchAHTask = taskMap.get(login);
        if (searchAHTask != null) {
            Task task = taskRepository.getOne(searchAHTask.getTaskId());
            if (task != null) {
                task.setStopTime(new Date());
                task.setRepeat(searchAHTask.getCount());
                taskRepository.save(task);
            }
            ScheduledFuture future = searchAHTask.getFuture();
            future.cancel(true);
        }
    }

    private void startSnipeAH(Account account, int startCount, boolean immediate) {
        try {
            if (account.getExpirationDate().after(new Date())) {
                log.info("Start sniping AH for account {}.", account.getLogin());

                Task task = new Task();
                task.setAccount(account);
                task.setTaskType("SNIPE_AH");
                task.setStartTime(new Date());
                task.setRepeat(0);
                task = taskRepository.save(task);

                SearchAHTask searchAHTask = (SearchAHTask) beanFactory.getBean("searchAHTask");
                searchAHTask.setCookie(account.getCookie());
                searchAHTask.setTaskId(task.getId());
                searchAHTask.setLogin(account.getLogin());
                searchAHTask.setCount(startCount);
                int initialDelay = immediate ? 0 : calculateInitialDelay(account.getStartHour());
                ScheduledFuture future = executorService.scheduleAtFixedRate(searchAHTask, initialDelay, 5, TimeUnit.SECONDS);
                searchAHTask.setFuture(future);

                taskMap.put(account.getLogin(), searchAHTask);
            } else {
                log.warn("Cookie expired for account {}.", account.getLogin());
            }
        } catch (Exception e) {
            log.error("Error while sniping AH for account {}.", account.getLogin());
        }
    }

    private int calculateInitialDelay(int startHour) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        Calendar targetCalendar = GregorianCalendar.getInstance();
        targetCalendar.setTime(calendar.getTime());
        if (currentHour >= startHour) {
            if (currentHour < startHour + 3) {
                return 0;
            } else {
                targetCalendar.add(Calendar.DATE, 1);
            }
        }
        targetCalendar.set(Calendar.HOUR_OF_DAY, startHour);
        targetCalendar.set(Calendar.MINUTE, 0);
        targetCalendar.set(Calendar.SECOND, 30);
        targetCalendar.set(Calendar.MILLISECOND, 0);
        long diff = targetCalendar.getTime().getTime() - new Date().getTime();
        return (int) (diff / 1000);
    }

}
