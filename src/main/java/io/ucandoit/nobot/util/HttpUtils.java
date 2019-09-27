package io.ucandoit.nobot.util;

import io.ucandoit.nobot.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HttpUtils {

  public static String requestToken(HttpClient httpClient, String cookie) {
    ResponseEntity<String> response =
        httpClient.makeGETRequest("http://yahoo-mbga.jp/game/12004455/play", cookie);
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      String url = getIframeUrl(response.getBody());
      log.info("Game URL: {}", url);
      if (url != null) {
        return getToken(url);
      } else {
        log.error("Unable to find the iframe url. Body: {}.", response.getBody());
      }
    } else {
      log.error(
          "Error while requesting Token. Code: {}, Body: {}.",
          response.getStatusCode(),
          response.getBody());
    }
    return null;
  }

  public static JSONObject responseToJsonObject(String response) {
    return new JSONObject(response.substring(response.indexOf('{')));
  }

  private static String getIframeUrl(String text) {
    Pattern pattern = Pattern.compile("(<iframe id=\"ymbga_app\" src=\")(.+?)(\".+</iframe>)");
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      return matcher.group(2);
    }
    return null;
  }

  private static String getToken(String url) {
    Pattern pattern = Pattern.compile("(http://.+&st=)(.+?)(#rpctoken.+)");
    Matcher matcher = pattern.matcher(url);
    if (matcher.find()) {
      try {
        return URLDecoder.decode(matcher.group(2), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        log.error("UnsupportedEncoding while requesting token.", e);
        return null;
      }
    }
    return null;
  }
}
