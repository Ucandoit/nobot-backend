package io.ucandoit.nobot.service;

import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.util.HttpUtils;
import io.ucandoit.nobot.util.NobotUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class RewardService {

  @Resource private CacheService cacheService;

  @Resource private AccountRepository accountRepository;

  @Resource private HttpClient httpClient;

  public void getItemboxReward() {
    CompletableFuture.allOf(
            accountRepository.findAllWithCookieNotExpired().stream()
                .map(
                    account ->
                        CompletableFuture.runAsync(
                            () ->
                                cacheService
                                    .getToken(account.getLogin())
                                    .ifPresent(
                                        token -> {
                                          httpClient.makePOSTRequest(
                                              NobotUtils.VILLAGE_URL, "GET", null, token);
                                          ResponseEntity<String> response =
                                              httpClient.makePOSTRequest(
                                                  NobotUtils.ITEMBOX_LIST_URL, "GET", null, token);
                                          JSONObject obj =
                                              HttpUtils.responseToJsonObject(response.getBody());
                                          Document doc =
                                              Jsoup.parse(
                                                  new JSONObject(
                                                          obj.getJSONObject(
                                                                  NobotUtils.ITEMBOX_LIST_URL)
                                                              .getString("body"))
                                                      .getString("body"));
                                          Elements items = doc.select(".itembox-item");
                                          if (items != null) {
                                            for (Element item : items) {
                                              String id =
                                                  item.selectFirst(".itembox-receive")
                                                      .id()
                                                      .replace("receive-item-", "");
                                              log.info(
                                                  "Get itembox reward {} for {}.",
                                                  id,
                                                  account.getLogin());
                                              httpClient.makePOSTRequest(
                                                  NobotUtils.ITEMBOX_LIST_URL,
                                                  "POST",
                                                  "item_id=" + id,
                                                  token);
                                            }
                                          }
                                        })))
                .toArray(CompletableFuture[]::new))
        .join();
  }

  public void getFriendCodeReward() {
    CompletableFuture.runAsync(
        () ->
            CompletableFuture.allOf(
                    accountRepository.findAllWithCookieNotExpired().stream()
                        .map(
                            account ->
                                CompletableFuture.runAsync(
                                    () -> claimFriendCodeReward(account.getLogin())))
                        .toArray(CompletableFuture[]::new))
                .join());
  }

  public void claimFriendCodeReward(String login) {
    cacheService
        .getToken(login)
        .ifPresent(
            token -> {
              log.info("Get friend code reward for {}", login);
              ResponseEntity<String> response =
                  httpClient.makePOSTRequest(NobotUtils.FRIEND_CODE_URL, "GET", null, token);
              JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
              Document doc =
                  Jsoup.parse(obj.getJSONObject(NobotUtils.FRIEND_CODE_URL).getString("body"));
              doc.select("form")
                  .forEach(
                      form -> {
                        httpClient.makePOSTRequest(
                            NobotUtils.FRIEND_CODE_URL,
                            "POST",
                            HttpUtils.buildPostData(form),
                            token);
                      });
            });
  }
}
