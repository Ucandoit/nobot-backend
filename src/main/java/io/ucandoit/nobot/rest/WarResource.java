package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.service.WarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;

@RestController
@RequestMapping(value = "/api/rest/war")
public class WarResource {

  @Resource private WarService warService;

  @RequestMapping(
      value = "/start/{login}/{line}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> startWar(
      @PathVariable String login, @PathVariable String line, Boolean fp, Boolean npc) {
    warService.startWar(login, line, fp, npc);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/startAll",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> startAll() {
    warService.startAll();
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/stop/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> stopWar(@PathVariable String login) {
    warService.stopWar(login);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/stopAll",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> stopAll() {
    warService.stopAll();
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/warConfigList",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<String> getWarConfigList() {
    return new ResponseEntity<>(warService.getWarConfigList(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/allLine/{line}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> setAllLine(@PathVariable int line) {
    warService.setAllLine(line);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/allFP/{fp}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> setAllFP(@PathVariable boolean fp) {
    warService.setAllFP(fp);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/line/{login}/{line}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> setLine(@PathVariable String login, @PathVariable int line) {
    warService.setLine(login, line);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/fp/{login}/{fp}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> setFP(@PathVariable String login, @PathVariable boolean fp) {
    warService.setFP(login, fp);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/completeQuest/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> completeQuest(@PathVariable String login, Integer[] questIds) {
    if (questIds == null || questIds.length == 0) {
      questIds = new Integer[] {139, 158, 218, 219, 181, 182};
    }
    warService.completeQuest(login, Arrays.asList(questIds));
    return new ResponseEntity<>(true, HttpStatus.OK);
  }
}
