package io.ucandoit.nobot.service;

import io.ucandoit.nobot.dto.CardInfo;
import io.ucandoit.nobot.http.HttpClient;
import io.ucandoit.nobot.util.HttpUtils;
import io.ucandoit.nobot.util.NobotUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class CardService {

  @Resource private HttpClient httpClient;

  @Resource private CacheService cacheService;

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
              //              log.info(doc.toString());
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
}
