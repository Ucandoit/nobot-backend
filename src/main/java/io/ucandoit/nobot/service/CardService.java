package io.ucandoit.nobot.service;

import io.ucandoit.nobot.dto.CardInfo;
import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.model.Card;
import io.ucandoit.nobot.repository.AccountRepository;
import io.ucandoit.nobot.repository.CardRepository;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class CardService {

  @Resource private HttpClient httpClient;

  @Resource private CacheService cacheService;

  @Resource private CardRepository cardRepository;

  @Resource private AccountRepository accountRepository;

  public CardInfo getCardInfo(String login, String cardId) {
    CardInfo cardInfo = new CardInfo();
    cacheService
        .getToken(login)
        .ifPresent(
            token -> {
              ResponseEntity<String> response =
                  httpClient.makePOSTRequest(
                      NobotUtils.CARD_DETAIL_URL, "POST", "cardid=" + cardId, token);
              JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
              Document doc =
                  Jsoup.parse(obj.getJSONObject(NobotUtils.CARD_DETAIL_URL).getString("body"));
              //                            log.info(doc.toString());
              cardInfo.setId(cardId);
              cardInfo.setName(doc.selectFirst(".card-name").text());
              cardInfo.setRealName(doc.selectFirst(".card-real-name").text());
              Element rarity = doc.selectFirst(".card-rarity");
              cardInfo.setProperty(
                  NobotUtils.getProperty(doc.selectFirst(".card-property").attr("src")));
              cardInfo.setRarityCode(
                  rarity != null ? NobotUtils.getRarityCode(rarity.attr("src")) : 1);
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
                  doc.selectFirst(".card-skill1").text()
                      + doc.selectFirst(".card-skill-lv1").text());
              cardInfo.setSkill2(
                  doc.selectFirst(".card-skill2").text()
                      + doc.selectFirst(".card-skill-lv2").text());
              cardInfo.setSkill3(
                  doc.selectFirst(".card-skill3").text()
                      + doc.selectFirst(".card-skill-lv3").text());
            });
    return cardInfo;
  }

  public void scanBooks(String login) {
    cacheService
        .getToken(login)
        .ifPresent(
            token -> {
              Integer nextPage = 1;
              while (nextPage != null) {
                log.info("Scan books page {} of {}", nextPage, login);
                nextPage = scanBooksPage(token, nextPage);
              }
            });
  }

  public void scanBooksForAll() {
    CompletableFuture.runAsync(
        () -> {
          accountRepository.findAllWithCookieNotExpired().stream()
              .forEach(account -> scanBooks(account.getLogin()));
        });
  }

  public void scanForAll() {
    CompletableFuture.runAsync(
        () -> {
          int cardId = 1;
          while (cardId < 2100) {
            if (!cardRepository.existsById(cardId)) {
              log.info("Add card by id {}.", cardId);
              int finalCardId = cardId;
              cacheService
                  .getToken("xzdykerik")
                  .ifPresent(
                      token -> {
                        ResponseEntity<String> response =
                            httpClient.makePOSTRequest(
                                NobotUtils.GET_REWARD_CARD_DETAIL,
                                "POST",
                                "cardid=" + finalCardId,
                                token);
                        JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
                        Document doc =
                            Jsoup.parse(
                                obj.getJSONObject(NobotUtils.GET_REWARD_CARD_DETAIL)
                                    .getString("body"));
                        if (doc.select(".card") != null) {
                          Card card = fromDetail(doc);
                          card.setId(finalCardId);
                          card.setNumber(9999);
                          cardRepository.save(card);
                        }
                      });
            }
            cardId++;
          }
        });
  }

  private Integer scanBooksPage(String token, int page) {
    String url = NobotUtils.MANAGE_BOOKS + "?pages=" + page;
    ResponseEntity<String> response = httpClient.makePOSTRequest(url, "GET", null, token);
    JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
    Document doc = Jsoup.parse(obj.getJSONObject(url).getString("body"));
    Elements books = doc.select("div[id^=books-member]");
    CompletableFuture.allOf(
            books.stream()
                .map(book -> CompletableFuture.runAsync(() -> readBookInfo(book, token)))
                .toArray(CompletableFuture[]::new))
        .join();
    Integer nextPage = null;
    for (Element currentPage : doc.select(".current-page")) {
      Element next = currentPage.nextElementSibling();
      if (next != null && next.hasClass("other-page")) {
        nextPage = Integer.parseInt(currentPage.nextElementSibling().selectFirst("a").text());
        break;
      }
    }
    return nextPage;
  }

  private void readBookInfo(Element element, String token) {
    Element face = element.selectFirst(".face-frame");
    int cardId = 0;
    for (String className : face.classNames()) {
      if (className.startsWith("face-card")) {
        cardId = Integer.parseInt(className.replace("face-card", ""));
        break;
      }
    }
    if (!cardRepository.existsById(cardId)) {
      log.info("Save new card {}", element.selectFirst(".books-name").text());
      ResponseEntity<String> response =
          httpClient.makePOSTRequest(NobotUtils.GET_BOOK_DETAIL, "POST", "cardid=" + cardId, token);
      JSONObject obj = HttpUtils.responseToJsonObject(response.getBody());
      Document doc = Jsoup.parse(obj.getJSONObject(NobotUtils.GET_BOOK_DETAIL).getString("body"));
      Card card = fromDetail(doc);
      card.setId(cardId);
      card.setNumber(
          Integer.parseInt(element.selectFirst(".books-number").text().replace("No.", "")));
      card.setFaceUrl(element.selectFirst(".books-catface").attr("src"));
      // elements only exist in book detail
      card.setFinalAtk(
          NobotUtils.imagesToNumber(
              doc.select(".rcard-ability-atk img").stream()
                  .map(img -> img.attr("src"))
                  .collect(Collectors.toList())));
      card.setFinalDef(
          NobotUtils.imagesToNumber(
              doc.select(".rcard-ability-def img").stream()
                  .map(img -> img.attr("src"))
                  .collect(Collectors.toList())));
      card.setFinalSpd(
          NobotUtils.imagesToNumber(
              doc.select(".rcard-ability-spd img").stream()
                  .map(img -> img.attr("src"))
                  .collect(Collectors.toList())));
      card.setFinalVir(
          NobotUtils.imagesToNumber(
              doc.select(".rcard-ability-vir img").stream()
                  .map(img -> img.attr("src"))
                  .collect(Collectors.toList())));
      card.setFinalStg(
          NobotUtils.imagesToNumber(
              doc.select(".rcard-ability-stg img").stream()
                  .map(img -> img.attr("src"))
                  .collect(Collectors.toList())));
      card.setTrainSkills(doc.selectFirst(".card-train-skill-name").text());
      cardRepository.save(card);
    }
  }

  private Card fromDetail(Element doc) {
    Card card = new Card();
    card.setIllustUrl(doc.selectFirst(".card-illust").attr("src"));
    card.setProperty(NobotUtils.getProperty(doc.selectFirst(".card-property").attr("src")));
    Element rarity = doc.selectFirst(".card-rarity");
    if (rarity != null) {
      card.setRarity(NobotUtils.getRarity(rarity.attr("src")));
      card.setStar(NobotUtils.getStar(rarity.attr("src")));
    } else {
      card.setRarity(NobotUtils.getRarity(1));
      card.setStar(0);
    }
    card.setMilitary(NobotUtils.getMilitary(doc.selectFirst(".card-military").attr("src")));
    card.setInitialAtk(
        NobotUtils.imagesToNumber(
            doc.select(".card-ability-atk img").stream()
                .map(img -> img.attr("src"))
                .collect(Collectors.toList())));
    card.setInitialDef(
        NobotUtils.imagesToNumber(
            doc.select(".card-ability-def img").stream()
                .map(img -> img.attr("src"))
                .collect(Collectors.toList())));
    card.setInitialSpd(
        NobotUtils.imagesToNumber(
            doc.select(".card-ability-spd img").stream()
                .map(img -> img.attr("src"))
                .collect(Collectors.toList())));
    card.setInitialVir(
        NobotUtils.imagesToNumber(
            doc.select(".card-ability-vir img").stream()
                .map(img -> img.attr("src"))
                .collect(Collectors.toList())));
    card.setInitialStg(
        NobotUtils.imagesToNumber(
            doc.select(".card-ability-stg img").stream()
                .map(img -> img.attr("src"))
                .collect(Collectors.toList())));
    card.setCost(
        NobotUtils.imagesToCost(
            doc.select(".card-cost img").stream()
                .map(img -> img.attr("src"))
                .collect(Collectors.toList())));
    card.setName(doc.selectFirst(".card-name").text());
    card.setRealName(doc.selectFirst(".card-real-name").text());
    card.setPersonality(doc.selectFirst(".card-personality").text());
    card.setJob(doc.selectFirst(".card-job").text());
    card.setSlogan(doc.selectFirst(".card-slogan").text());
    card.setHistory(doc.selectFirst(".card-history").text());
    return card;
  }
}
