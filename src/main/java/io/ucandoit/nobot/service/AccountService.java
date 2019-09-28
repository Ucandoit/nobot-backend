package io.ucandoit.nobot.service;

import io.ucandoit.nobot.dto.AccountInfo;
import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.model.Account;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountService {

  @Resource private AccountRepository accountRepository;

  @Resource private HttpClient httpClient;

  public Page<AccountInfo> getAccountsGeneralInfo()
      throws ExecutionException, InterruptedException {
    Pageable pageable = PageRequest.of(0, 5);
    long total = accountRepository.count();
    Page<Account> accounts = accountRepository.findAll(pageable);
    List<CompletableFuture<AccountInfo>> futures =
        accounts.stream()
            .map(
                account ->
                    CompletableFuture.supplyAsync(
                        () -> {
                          Optional<String> token =
                              HttpUtils.requestToken(httpClient, account.getCookie());
                          if (token.isPresent()) {
                            String homeUrl = "http://210.140.157.168/village.htm";
                            ResponseEntity<String> response =
                                httpClient.makePOSTRequest(homeUrl, "GET", null, token.get());
                            JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
                            Document doc =
                                Jsoup.parse(obj.getJSONObject(homeUrl).getString("body"));
                            AccountInfo accountInfo = new AccountInfo();
                            accountInfo.setLogin(account.getLogin());
                            accountInfo.setName(account.getName());
                            accountInfo.setNp(getIntValueById(doc, "lottery_point"));
                            accountInfo.setFood(getIntValueById(doc, "element_food"));
                            accountInfo.setMaxFood(getIntValueById(doc, "max_food"));
                            accountInfo.setFire(getIntValueById(doc, "element_fire"));
                            accountInfo.setEarth(getIntValueById(doc, "element_earth"));
                            accountInfo.setWind(getIntValueById(doc, "element_wind"));
                            accountInfo.setWater(getIntValueById(doc, "element_water"));
                            accountInfo.setSky(getIntValueById(doc, "element_sky"));
                            accountInfo.setMaxFire(getIntValueById(doc, "max_fire"));
                            accountInfo.setMaxEarth(getIntValueById(doc, "max_earth"));
                            accountInfo.setMaxWind(getIntValueById(doc, "max_wind"));
                            accountInfo.setMaxWater(getIntValueById(doc, "max_water"));
                            accountInfo.setMaxSky(getIntValueById(doc, "max_sky"));
                            return accountInfo;
                          }
                          return null;
                        }))
            .collect(Collectors.toList());
    CompletableFuture<List<AccountInfo>> allCompletableFuture =
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(
                future ->
                    futures.stream()
                        .map(CompletableFuture::join)
                        .sorted(Comparator.comparing(AccountInfo::getLogin))
                        .collect(Collectors.toList()));
    return new PageImpl<>(allCompletableFuture.get(), pageable, total);
  }

  /**
   * Get a node's integer value by id
   *
   * @param element root element
   * @param id id of the node
   * @return the value (-1 if node does not exist)
   */
  private int getIntValueById(Element element, String id) {
    Element node = element.selectFirst("#" + id);
    if (node != null) {
      return Integer.parseInt(node.text());
    } else {
      log.error("Element with id {} doesn't exist.", id);
      return -1;
    }
  }
}
