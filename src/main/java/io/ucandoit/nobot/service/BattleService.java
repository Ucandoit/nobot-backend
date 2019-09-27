package io.ucandoit.nobot.service;

import io.ucandoit.nobot.model.Account;
import io.ucandoit.nobot.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class BattleService {

  @Resource private AccountRepository accountRepository;

  private ExecutorService executorService = Executors.newFixedThreadPool(20);

  public void startBattle(String login) {
    Account account = accountRepository.getOne(login);
    if (account != null) {
      log.info("Start battling for account {}.", account.getLogin());
    }
  }
}
