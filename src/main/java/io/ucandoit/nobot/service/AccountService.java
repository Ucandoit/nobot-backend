package io.ucandoit.nobot.service;

import io.ucandoit.nobot.dto.AccountInfo;
import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.model.Account;
import io.ucandoit.nobot.model.Parameter;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.repository.ParameterRepository;
import io.ucandoit.nobot.util.HttpUtils;
import io.ucandoit.nobot.util.NobotUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountService {

  @Value("${scheduler.enable:true}")
  private boolean enable;

  @Resource private AccountRepository accountRepository;

  @Resource private HttpClient httpClient;

  @Resource private CacheService cacheService;

  @Resource private ParameterRepository parameterRepository;

  public List<AccountInfo> getAccountsGeneralInfo()
      throws ExecutionException, InterruptedException {
    //    Pageable pageable = PageRequest.of(0, 5);
    //    long total = accountRepository.count();
    //    Page<Account> accounts = accountRepository.findAll(pageable);
    List<Account> accounts =
        accountRepository.findByLoginIn(
            Arrays.asList(
                "ucandoit",
                "xzdykerik_2",
                "xzdykerik_3",
                "xzdykerik_6",
                "xzdykerik_7",
                "xzdykerik_8",
                "xzdykerik_9",
                "xzdykerik_10",
                "xzdykerik_51"));
    List<CompletableFuture<AccountInfo>> futures =
        accounts.stream()
            .map(
                account ->
                    CompletableFuture.supplyAsync(
                        () -> {
                          Optional<String> token = cacheService.getToken(account.getLogin());
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
    return allCompletableFuture.get();
  }

  public void trade(String login) {
    Account account = accountRepository.getOne(login);
    cacheService
        .getToken(login)
        .ifPresent(
            token -> {
              String villageUrl = "http://210.140.157.168/village.htm";
              ResponseEntity<String> response =
                  httpClient.makePOSTRequest(villageUrl, "GET", null, token);
              JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
              Document doc = Jsoup.parse(obj.getJSONObject(villageUrl).getString("body"));
              Element form = doc.selectFirst("#trade-all-form");
              String actionUrl = form.attr("action");
              StringBuilder postData = new StringBuilder();
              for (Element input : form.children()) {
                if (postData.length() > 0) {
                  postData.append("&");
                }
                postData.append(input.attr("name")).append("=").append(input.attr("value"));
              }
              httpClient.makePOSTRequest(actionUrl, "POST", postData.toString(), token);
            });
  }

  @Scheduled(cron = "0 */30 * * * *")
  @Transactional
  public void updateTokens() {
    CompletableFuture.allOf(
            accountRepository.findAll().stream()
                .map(
                    account ->
                        CompletableFuture.runAsync(
                            () -> cacheService.updateToken(account.getLogin())))
                .toArray(CompletableFuture[]::new))
        .join();
  }

  @Scheduled(cron = "0 1 17 * * *")
  @Transactional
  public void dailyLogin() {
    if (enable) {
      CompletableFuture.allOf(
              accountRepository.findAll().stream()
                  .map(
                      account ->
                          CompletableFuture.runAsync(
                              () ->
                                  cacheService
                                      .getToken(account.getLogin())
                                      .ifPresent(
                                          token ->
                                              httpClient.makePOSTRequest(
                                                  "http://210.140.157.168/world_list.htm",
                                                  "GET",
                                                  null,
                                                  token))))
                  .toArray(CompletableFuture[]::new))
          .join();
    } else {
      log.info("Scheduler for login disabled.");
    }
  }

  public String getLocation(String login) {
    AtomicReference<String> location = new AtomicReference<>("");
    cacheService
        .getToken(login)
        .ifPresent(
            token -> {
              ResponseEntity<String> response =
                  httpClient.makePOSTRequest(NobotUtils.MAP_URL, "GET", null, token);
              JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
              Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.MAP_URL).getString("body"));
              location.set(doc.selectFirst("#notify_count_title span").text());
            });
    return location.get();
  }

  @Scheduled(cron = "0 5 18 * * *")
  @Transactional
  public void linkGame100SanGuo() {
    Parameter parameter = parameterRepository.getOne("100sanguo.login");
    if (enable && parameter.getValue() == "1") {
      CompletableFuture.allOf(
              accountRepository.findAll().stream()
                  .map(
                      account ->
                          CompletableFuture.runAsync(
                              () ->
                                  cacheService
                                      .getToken(account.getLogin())
                                      .ifPresent(
                                          token -> {
                                            String url =
                                                "http://40e7b82553f00715f4a4027b8c4798c4b1b8c789.app.mbga-platform.jp/gadgets/makeRequest";
                                            Map<String, Object> params = new HashMap<>();
                                            params.put(
                                                "url",
                                                "http://103spym.gamecity.ne.jp/100RTKSpecial/browser/tutorial_v2/tut01.php?guid=ON&isFlash=1&date="
                                                    + new Date().getTime());
                                            params.put("st", token);
                                            params.put("authz", "signed");
                                            httpClient.makePOSTRequest(url, params);
                                          })))
                  .toArray(CompletableFuture[]::new))
          .join();
    } else {
      log.info("Scheduler for 100sanguo disabled.");
    }
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
