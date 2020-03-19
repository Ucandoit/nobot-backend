package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.dto.AccountInfo;
import io.ucandoit.nobot.dto.CardInfo;
import io.ucandoit.nobot.service.AccountService;
import io.ucandoit.nobot.service.CacheService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(value = "/api/rest/account")
public class AccountResource {

  @Resource private AccountService accountService;

  @Resource private CacheService cacheService;

  @RequestMapping(
      value = "/info",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<List<AccountInfo>> generalInfo()
      throws ExecutionException, InterruptedException {
    return new ResponseEntity<>(accountService.getAccountsGeneralInfo(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/info/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<AccountInfo> accountInfo(@PathVariable String login) {
    return new ResponseEntity<>(accountService.getAccountInfo(login), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/trade/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> trade(@PathVariable String login) {
    accountService.trade(login);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/login",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> login() {
    accountService.dailyLogin();
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/location/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<String> getLocation(@PathVariable String login) {
    String location = accountService.getLocation(login);
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("location", location);
    return new ResponseEntity<>(jsonObject.toString(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/draw/update_status",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> updateDrawStatus() {
    accountService.updateDrawStatus();
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/draw/{login}/{type}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> drawCard(
      @PathVariable String login, @PathVariable Integer type, Integer times) {
    accountService.drawCard(login, type, times);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/sanguo",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> linkGame100SanGuo() {
    accountService.linkGame100SanGuo();
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/recruit/update_status",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> updateRecruitStatus() {
    accountService.updateRecruitStatus();
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/recruit/multi_attach_code/{target}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> multipleAttachCode(
      @PathVariable String target, @RequestParam List<String> sources) {
    accountService.attachFriendCode(sources, target);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/recruit/attach_code/{target}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> attachCode(
      @PathVariable String target, @RequestParam String source) {
    accountService.attachFriendCode(source, target);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/recruit/inviterReward/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> inviterReward(@PathVariable String login) {
    accountService.inviterReward(login);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/cardJi",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Map<String, String>> cardJi() {
    return new ResponseEntity<>(accountService.getJi(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/updateNp",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Integer> getNp() {
    return new ResponseEntity<>(accountService.updateNp(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "reserveCards/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<List<CardInfo>> reserveCards(@PathVariable String login) {
    return new ResponseEntity<>(accountService.getReserveCards(login), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/comeback",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> comback() {
    accountService.comeback();
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/celebrate9",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> celebrate9() {
    accountService.celebrate9();
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
          value = "/test/{login}",
          method = RequestMethod.GET,
          produces = "application/json; charset=UTF-8")
  public ResponseEntity<AccountInfo> test(@PathVariable String login) {
    return new ResponseEntity<>(cacheService.getAccountInfo(login), HttpStatus.OK);
  }

  @RequestMapping(
          value = "/testUpdate/{login}",
          method = RequestMethod.GET,
          produces = "application/json; charset=UTF-8")
  public ResponseEntity<AccountInfo> testUpdate(@PathVariable String login) {
    return new ResponseEntity<>(cacheService.updateAccountInfo(login), HttpStatus.OK);
  }

  @RequestMapping(
          value = "/testDelete/{login}",
          method = RequestMethod.GET,
          produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> testDelete(@PathVariable String login) {
    cacheService.evictAccountInfo(login);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }
}
