package io.ucandoit.nobot.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
public class HttpClient {

  private RestTemplate restTemplate;

  public HttpClient() {
    restTemplate = new RestTemplate();
  }

  public ResponseEntity<String> makeGETRequest(String url, String cookie) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", cookie);
    HttpEntity entity = new HttpEntity(headers);

    return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
  }

  public ResponseEntity<String> makePOSTRequest(
      String url, String method, String postData, String token) {
    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("url", url);
    map.add("httpMethod", method);
    if (!StringUtils.isEmpty(postData)) {
      map.add("postData", postData);
      map.add("headers", "Content-Type=application%2Fx-www-form-urlencoded");
    }
    map.add("authz", "signed");
    map.add("st", token);
    map.add("contentType", "TEXT");
    map.add("numEntries", 3);
    map.add("getSummaries", false);
    map.add("signOwner", true);
    map.add("signViewer", true);
    map.add("gadget", "http://210.140.157.168/gadget.xml");
    map.add("container", "default");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity entity = new HttpEntity<>(map, headers);

    return restTemplate.exchange(
        "http://e824549fb2ec8582e96abe565514e1aa9a3fca00.app.mbga-platform.jp/gadgets/makeRequest",
        HttpMethod.POST,
        entity,
        String.class);
  }

  public ResponseEntity<String> makePOSTRequest(String url, Map<String, Object> params) {
    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    for (Map.Entry<String, Object> entry : params.entrySet()) {
      map.add(entry.getKey(), entry.getValue());
    }
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity entity = new HttpEntity<>(map, headers);

    return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
  }
}
