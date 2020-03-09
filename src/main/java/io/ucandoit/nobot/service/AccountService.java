package io.ucandoit.nobot.service;

import io.ucandoit.nobot.dto.AccountInfo;
import io.ucandoit.nobot.dto.CardInfo;
import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.model.Account;
import io.ucandoit.nobot.model.DrawHistory;
import io.ucandoit.nobot.model.DrawStatus;
import io.ucandoit.nobot.model.Parameter;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.repository.DrawHistoryRepository;
import io.ucandoit.nobot.repository.DrawStatusRepository;
import io.ucandoit.nobot.repository.ParameterRepository;
import io.ucandoit.nobot.util.HttpUtils;
import io.ucandoit.nobot.util.NobotUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountService {

  @Value("${scheduler.account.enable:true}")
  private boolean enable;

  @Resource private AccountRepository accountRepository;

  @Resource private HttpClient httpClient;

  @Resource private CacheService cacheService;

  @Resource private ParameterRepository parameterRepository;

  @Resource private DrawHistoryRepository drawHistoryRepository;

  @Resource private DrawStatusRepository drawStatusRepository;

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

  @Scheduled(cron = "${scheduler.account.tokenUpdate}")
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

  @Scheduled(cron = "${scheduler.account.login}")
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

  public void drawCard(String login, int type, Integer times) {
    if (type == 0) {
      drawFukubiki(login);
    } else {
      drawFuji(login, type, times);
    }
  }

  @Scheduled(cron = "${scheduler.account.100sanguo}")
  @Transactional
  public void linkGame100SanGuo() {
    Parameter parameter = parameterRepository.getOne("100sanguo.login");
    if (enable && parameter.getValue().equals("1")) {
      linkSanGuo();
      linkSanGuo();
    } else {
      log.info("Scheduler for 100sanguo disabled.");
    }
  }

  private void linkSanGuo() {
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
                                          String request =
                                              "http://103spym.gamecity.ne.jp/100RTKSpecial/browser/top/home.php?guid=ON&date="
                                                  + new Date().getTime();
                                          String url =
                                              "http://40e7b82553f00715f4a4027b8c4798c4b1b8c789.app.mbga-platform.jp/gadgets/makeRequest";
                                          Map<String, Object> params = new HashMap<>();
                                          params.put("url", request);
                                          params.put("st", token);
                                          params.put("authz", "signed");
                                          httpClient.makePOSTRequest(url, params);
                                        })))
                .toArray(CompletableFuture[]::new))
        .join();
  }

  public void updateRecruitStatus() {
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
                                          ResponseEntity<String> response =
                                              httpClient.makePOSTRequest(
                                                  NobotUtils.FRIEND_CODE_URL, "GET", null, token);
                                          JSONObject obj =
                                              HttpUtils.responseToJsonObject(response.getBody());
                                          Document doc =
                                              Jsoup.parse(
                                                  obj.getJSONObject(NobotUtils.FRIEND_CODE_URL)
                                                      .getString("body"));
                                          String friendCode =
                                              doc.selectFirst("#main .twitter-share-button")
                                                  .parent()
                                                  .parent()
                                                  .selectFirst("div")
                                                  .text();
                                          account.setFriendCode(friendCode);
                                          accountRepository.save(account);
                                        })))
                .toArray(CompletableFuture[]::new))
        .join();
  }

  public void attachFriendCode(List<String> sources, String target) {
    sources.forEach(source -> attachFriendCode(source, target));
  }

  public void attachFriendCode(String source, String target) {
    String friendCode = accountRepository.getOne(target).getFriendCode();
    cacheService
        .getToken(source)
        .ifPresent(
            token -> {
              httpClient.makePOSTRequest(
                  NobotUtils.FRIEND_CODE_URL, "POST", "friendCode=" + friendCode, token);
            });
  }

  public void inviterReward(String login) {
    cacheService
        .getToken(login)
        .ifPresent(
            token -> {
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

  public void updateDrawStatus() {
    CompletableFuture.allOf(
            accountRepository.findAll().stream()
                .map(
                    account ->
                        CompletableFuture.runAsync(
                            () ->
                                cacheService
                                    .getToken(account.getLogin())
                                    .ifPresent(token -> saveDrawStatus(token, account.getLogin()))))
                .toArray(CompletableFuture[]::new))
        .join();
  }

  public void saveDrawStatus(String token, String login) {
    DrawStatus drawStatus = new DrawStatus();
    drawStatus.setLogin(login);
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(NobotUtils.DRAW_URL, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.DRAW_URL).getString("body"));
    Element fukubikiZone = doc.selectFirst("#fstart");
    if (fukubikiZone != null) {
      Integer fukubikiNumber =
          Integer.parseInt(
              doc.selectFirst("#fstart")
                  .parent()
                  .parent()
                  .selectFirst("div")
                  .selectFirst(".red")
                  .text());
      drawStatus.setFukubikiNumber(fukubikiNumber);
    }
    Element fuZone = doc.selectFirst("img[alt=ニャオみくじ・福]");
    if (fuZone != null) {
      Integer fuNumber =
          Integer.parseInt(
              fuZone.parent().selectFirst(".lot-explain").select(".red").last().text());
      drawStatus.setFuNumber(fuNumber);
    }
    Element jiZone = doc.selectFirst("img[alt=ニャオみくじ・吉]");
    if (jiZone != null) {
      Integer jiNumber =
          Integer.parseInt(
              jiZone.parent().selectFirst(".lot-explain").select(".red").last().text());
      drawStatus.setJiNumber(jiNumber);
    }
    drawStatusRepository.save(drawStatus);
  }

  public void drawFukubiki(String login) {
    DrawStatus drawStatus = drawStatusRepository.getOne(login);
    AtomicInteger number = new AtomicInteger(drawStatus.getFukubikiNumber());
    cacheService
        .getToken(login)
        .ifPresent(
            token -> {
              while (number.get() >= 10) {
                httpClient.makePOSTRequest(
                    NobotUtils.FUKUBIKI_START_URL, "POST", "num=" + number.get(), token);
                ResponseEntity<String> response =
                    httpClient.makePOSTRequest(NobotUtils.FUKUBIKU_RESULT_URL, "GET", null, token);
                JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
                Document doc =
                    Jsoup.parse(
                        obj.getJSONObject(NobotUtils.FUKUBIKU_RESULT_URL).getString("body"));
                saveIfRare(login, doc, 0);
                number.addAndGet(-10);
              }
            });
  }

  public List<CardInfo> getReserveCards(String login) {
    AtomicReference<List<CardInfo>> cardInfos = new AtomicReference<>(new ArrayList<>());
    cacheService
        .getToken(login)
        .ifPresent(
            token -> {
              ResponseEntity<String> response =
                  httpClient.makePOSTRequest(NobotUtils.VILLAGE_URL, "GET", null, token);
              JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
              Document doc =
                  Jsoup.parse(obj.getJSONObject(NobotUtils.VILLAGE_URL).getString("body"));
              Elements cards = doc.select(".reserve-rect");
              cardInfos.set(
                  cards.stream()
                      .map(
                          card -> {
                            Element img = card.selectFirst(".reserve-face");
                            CardInfo cardInfo = new CardInfo();

                            cardInfo.setId(
                                img.className().split(" ")[0].replace("face-card-id", ""));
                            cardInfo.setName(img.attr("title"));
                            cardInfo.setTradable(
                                !img.hasClass("protected") && !img.hasClass("trade-limit"));
                            cardInfo.setImgUrl(img.attr("src"));
                            return cardInfo;
                          })
                      .collect(Collectors.toList()));
              CompletableFuture.allOf(
                      cardInfos.get().stream()
                          .map(
                              cardInfo ->
                                  CompletableFuture.runAsync(() -> getCardDetail(token, cardInfo)))
                          .toArray(CompletableFuture[]::new))
                  .join();
            });
    return cardInfos.get();
  }

  public int updateNp() {
    AtomicInteger total = new AtomicInteger();
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
                                          ResponseEntity<String> response =
                                              httpClient.makePOSTRequest(
                                                  NobotUtils.VILLAGE_URL, "GET", null, token);
                                          JSONObject obj =
                                              HttpUtils.responseToJsonObject(response.getBody());
                                          Document doc =
                                              Jsoup.parse(
                                                  obj.getJSONObject(NobotUtils.VILLAGE_URL)
                                                      .getString("body"));
                                          account.setNp(getIntValueById(doc, "lottery_point"));
                                          accountRepository.save(account);
                                          total.addAndGet(account.getNp());
                                        })))
                .toArray(CompletableFuture[]::new))
        .join();
    return total.get();
  }

  public Map<String, String> getJi() {
    Map<String, String> map = new HashMap<>();
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
                                          List<CardInfo> reserveCards =
                                              getReserveCards(account.getLogin());
                                          reserveCards.forEach(
                                              card -> {
                                                if (card.getRarityCode() == 4
                                                    && card.isTradable()) {
                                                  String list = map.get(card.getName());
                                                  if (list == null) {
                                                    list = "";
                                                  }
                                                  list += ", " + account.getLogin();
                                                  map.put(card.getName(), list);
                                                }
                                              });
                                        })))
                .toArray(CompletableFuture[]::new))
        .join();
    return map;
  }

  public void comeback() {
    CompletableFuture.runAsync(
        () -> {
          accountRepository
              .findAll()
              .forEach(
                  account -> {
                    cacheService
                        .getToken(account.getLogin())
                        .ifPresent(
                            token -> {
                              int times = 0;
                              while (times < 3) {
                                httpClient.makePOSTRequest(
                                    NobotUtils.PROFILE_URL, "GET", null, token);
                                ResponseEntity<String> response =
                                    httpClient.makePOSTRequest(
                                        NobotUtils.COMEBACK_LIST_URL, "GET", null, token);
                                JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
                                Document doc =
                                    Jsoup.parse(
                                        obj.getJSONObject(NobotUtils.COMEBACK_LIST_URL)
                                            .getString("body"));
                                Elements comebacks = doc.select(".comeback");
                                int i = 0;
                                while (times < 3 && i < comebacks.size()) {
                                  if (canComeback(
                                      NobotUtils.BASE_URL + comebacks.get(i).attr("href"), token)) {
                                    log.info("Call comeback for {}", account.getLogin());
                                    times++;
                                  }
                                  i++;
                                }
                              }
                            });
                  });
        });
  }

  private boolean canComeback(String url, String token) {
    ResponseEntity<String> response = httpClient.makePOSTRequest(url, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(url).getString("body"));
    if (doc.selectFirst("img[alt=送信する]").attr("name").equals("")) {
      return false;
    } else {
      String target = url.split("=")[1];
      httpClient.makePOSTRequest(NobotUtils.COMEBACK_URL, "POST", "target=" + target, token);
      return true;
    }
  }

  private void getCardDetail(String token, CardInfo cardInfo) {
    ResponseEntity<String> response =
        httpClient.makePOSTRequest(
            NobotUtils.CARD_DETAIL_URL, "POST", "cardid=" + cardInfo.getId(), token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.CARD_DETAIL_URL).getString("body"));
    Element rarity = doc.selectFirst(".card-rarity");
    cardInfo.setRarityCode(rarity != null ? NobotUtils.getRarityCode(rarity.attr("src")) : 1);
    cardInfo.setRarity(NobotUtils.getRarity(cardInfo.getRarityCode()));
    if (doc.selectFirst(".card-refine-total") != null) {
      cardInfo.setRefineTotal(doc.selectFirst(".card-refine-total").text());
    } else if (doc.selectFirst(".card-refine-total-left") != null) {
      cardInfo.setRefineTotal(doc.selectFirst(".card-refine-total-left").text());
    }
    cardInfo.setRefineAtk(doc.selectFirst(".card-refine-atk").text());
    cardInfo.setRefineDef(doc.selectFirst(".card-refine-def").text());
    cardInfo.setRefineSpd(doc.selectFirst(".card-refine-spd").text());
    cardInfo.setRefineVir(doc.selectFirst(".card-refine-vir").text());
    cardInfo.setRefineStg(doc.selectFirst(".card-refine-stg").text());
    cardInfo.setSkill1(
        doc.selectFirst(".card-skill1").text() + doc.selectFirst(".card-skill-lv1").text());
    cardInfo.setSkill2(
        doc.selectFirst(".card-skill2").text() + doc.selectFirst(".card-skill-lv2").text());
    cardInfo.setSkill3(
        doc.selectFirst(".card-skill3").text() + doc.selectFirst(".card-skill-lv3").text());
  }

  private void drawFuji(String login, int type, Integer times) {
    log.info("Start drawing cards for {}", login);
    AtomicInteger numbers = new AtomicInteger(getDrawNumbers(login, type, times));
    cacheService
        .getToken(login)
        .ifPresent(
            token -> {
              while (numbers.get() > 0) {
                ResponseEntity<String> response =
                    httpClient.makePOSTRequest(NobotUtils.DRAW_URL, "POST", "type=" + type, token);
                JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
                String cardUrl =
                    obj.getJSONObject(NobotUtils.DRAW_URL)
                        .getJSONObject("headers")
                        .getJSONArray("location")
                        .get(0)
                        .toString();
                response = httpClient.makePOSTRequest(cardUrl, "GET", null, token);
                obj = HttpUtils.responseToJsonObject(response.getBody());
                Document doc = Jsoup.parse(obj.getJSONObject(cardUrl).getString("body"));
                saveIfRare(login, doc, type);
                numbers.getAndDecrement();
              }
            });
    log.info("Stop drawing cards for {}", login);
  }

  private int getDrawNumbers(String login, int type, Integer times) {
    if (times != null) {
      return times;
    }
    DrawStatus drawStatus = drawStatusRepository.getOne(login);
    switch (type) {
      case 0:
        return drawStatus.getFukubikiNumber();
      case 4:
        return drawStatus.getJiNumber();
      case 5:
        return drawStatus.getFuNumber();
      default:
        return 0;
    }
  }

  private void saveIfRare(String login, Document doc, int type) {
    Element rarity = doc.selectFirst(".card-rarity");
    if (rarity != null) {
      int rarityCode = NobotUtils.getRarityCode(rarity.attr("src"));
      if (rarityCode > 2) {
        DrawHistory drawHistory = new DrawHistory();
        drawHistory.setAccount(accountRepository.getOne(login));
        drawHistory.setDrawType(NobotUtils.getDrawType(type));
        drawHistory.setDrawTime(new Date());
        drawHistory.setName(doc.selectFirst(".card-name").text());
        drawHistory.setRarity(NobotUtils.getRarity(rarityCode));
        drawHistoryRepository.save(drawHistory);
      }
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
