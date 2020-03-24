package io.ucandoit.nobot.util;

import io.ucandoit.nobot.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HttpUtils {

  public static Optional<String> requestToken(HttpClient httpClient, String cookie) {
    ResponseEntity<String> response =
        httpClient.makeGETRequest("http://yahoo-mbga.jp/game/12004455/play", cookie);
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      Optional<String> url = getIframeUrl(response.getBody());
      if (url.isPresent()) {
//        log.info("Game URL: {}", url.get());
        return getToken(url.get());
      } else {
        log.error("Unable to find the iframe url. Body: {}.", response.getBody());
      }
    } else {
      log.error(
          "Error while requesting Token. Code: {}, Body: {}.",
          response.getStatusCode(),
          response.getBody());
    }
    return Optional.empty();
  }

  public static JSONObject responseToJsonObject(String response) {
    return new JSONObject(response.substring(response.indexOf('{')));
  }

  public static String buildPostData(Element form) {
    StringBuilder postData = new StringBuilder();
    for (Element input : form.children()) {
      if (postData.length() > 0) {
        postData.append("&");
      }
      postData.append(input.attr("name")).append("=").append(input.attr("value"));
    }
    return postData.toString();
  }

  private static Optional<String> getIframeUrl(String text) {
    Pattern pattern = Pattern.compile("(<iframe id=\"ymbga_app\" src=\")(.+?)(\".+</iframe>)");
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      return Optional.ofNullable(matcher.group(2));
    }
    return Optional.empty();
  }

  private static Optional<String> getToken(String url) {
    Pattern pattern = Pattern.compile("(http://.+&st=)(.+?)(#rpctoken.+)");
    Matcher matcher = pattern.matcher(url);
    if (matcher.find()) {
      try {
        return Optional.ofNullable(URLDecoder.decode(matcher.group(2), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        log.error("UnsupportedEncoding while requesting token.", e);
        return Optional.empty();
      }
    }
    return Optional.empty();
  }
}
