package io.ucandoit.nobot.service;

import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.model.Account;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.task.WrestleTask;
import io.ucandoit.nobot.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class WrestleService {

    @Resource
    private HttpClient httpClient;

    @Resource
    private AccountRepository accountRepository;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    public void startWrestling(String login) {
        Account account = accountRepository.getOne(login);
        if (account != null) {
            log.info("Start wrestling for account {}.", account.getName());
            String token = HttpUtils.requestToken(httpClient, account.getCookie());
            if (token != null) {
                executorService.submit(new WrestleTask(httpClient, token, account.getCookie()));
            }
        }
    }

}
