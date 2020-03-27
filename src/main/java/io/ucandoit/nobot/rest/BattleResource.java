package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.enums.FriendshipLevel;
import io.ucandoit.nobot.service.BattleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/rest/battle")
public class BattleResource {

  @Resource private BattleService battleService;

  @RequestMapping(
      value = "/start/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> startBattle(@PathVariable String login, Integer times) {
    battleService.startBattle(login, times);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/stop/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> stopBattle(@PathVariable String login) {
    battleService.stopBattle(login);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/friendships/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Map<String, FriendshipLevel>> getFriendships(@PathVariable String login) {
    return new ResponseEntity<>(battleService.getFriendships(login), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/status",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<List<String>> getBattlingAccounts() {
    return new ResponseEntity<>(battleService.getBattlingAccounts(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/status/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> getStatus(@PathVariable String login) {
    return new ResponseEntity<>(battleService.getStatus(login), HttpStatus.OK);
  }
}
