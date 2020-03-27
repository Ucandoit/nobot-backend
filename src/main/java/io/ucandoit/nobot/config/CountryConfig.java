package io.ucandoit.nobot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class CountryConfig {

  @Bean
  public Map<String, String> countryMap() {
    log.info("Initialising country map.");
    Map<String, String> countryMap = new LinkedHashMap<>();
    countryMap.put("伊達家", "米沢城");
    countryMap.put("最上家", "山形城");
    countryMap.put("北条家", "小田原城");
    countryMap.put("武田家", "躑躅ヶ崎館");
    countryMap.put("上杉家", "春日山城");
    countryMap.put("徳川家", "浜松城");
    countryMap.put("織田家", "清洲城");
    countryMap.put("斎藤家", "稲葉山城");
    countryMap.put("三好家", "飯盛山城");
    countryMap.put("足利家", "二条御所");
    countryMap.put("毛利家", "吉田郡山城");
    countryMap.put("尼子家", "月山富田城");
    countryMap.put("長宗我部家", "岡豊城");
    countryMap.put("龍造寺家", "佐嘉城");
    countryMap.put("大友家", "府内城");
    countryMap.put("島津家", "内城");
    return countryMap;
  }
}
