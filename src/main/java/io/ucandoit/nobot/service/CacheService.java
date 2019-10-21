package io.ucandoit.nobot.service;

import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.model.Account;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class CacheService {

  @Resource private AccountRepository accountRepository;

  @Resource private HttpClient httpClient;

  @Cacheable("tokens")
  @Transactional
  public Optional<String> getToken(String login) {
    Account account = accountRepository.getOne(login);
    if (account != null) {
      if (account.getExpirationDate().after(new Date())) {
        log.info("Retrieving token for {}.", login);
        return HttpUtils.requestToken(httpClient, account.getCookie());
      } else {
        log.error("Cookie expired for {}.", login);
      }
    } else {
      log.error("Account {} not found.", login);
    }
    return null;
  }

  @CachePut(value = "tokens", key = "#login")
  @Transactional
  public Optional<String> updateToken(String login) {
    log.info("Updating token for {}.", login);
    return getToken(login);
  }

  @CacheEvict(value = "tokens", key = "#login")
  public void evictToken(String login) {}
}
