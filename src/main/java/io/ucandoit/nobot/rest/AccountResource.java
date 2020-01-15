package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.dto.AccountInfo;
import io.ucandoit.nobot.service.AccountService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(value = "/api/rest/account")
public class AccountResource {

  @Resource private AccountService accountService;

  @RequestMapping(
      value = "/info",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<List<AccountInfo>> generalInfo()
      throws ExecutionException, InterruptedException {
    return new ResponseEntity<>(accountService.getAccountsGeneralInfo(), HttpStatus.OK);
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
}
