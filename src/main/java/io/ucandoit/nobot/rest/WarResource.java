package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.model.WarConfig;
import io.ucandoit.nobot.service.WarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

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
  public ResponseEntity<List<WarConfig>> getWarConfigList() {
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
      value = "/line/{group}/{line}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> setLineForGroup(
      @PathVariable String group, @PathVariable int line) {
    warService.setLineForGroup(group, line);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/fp/{group}/{fp}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> setFPForGroup(
      @PathVariable String group, @PathVariable boolean fp) {
    warService.setFPForGroup(group, fp);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/npc/{group}/{npc}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> setNPCForGroup(
      @PathVariable String group, @PathVariable boolean npc) {
    warService.setNPCForGroup(group, npc);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/enabled/{group}/{enabled}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> setEnabledForGroup(
      @PathVariable String group, @PathVariable boolean enabled) {
    warService.setEnabledForGroup(group, enabled);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/completePreQuest/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> completePreQuest(@PathVariable String login, Integer[] questIds) {
    if (questIds == null || questIds.length == 0) {
      questIds = new Integer[] {54, 76, 94};
    }
    warService.completeQuest(login, Arrays.asList(questIds));
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

  @RequestMapping(
      value = "/completeQuestByGroup/{group}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> completeQuestByGroup(
      @PathVariable String group, Integer[] questIds) {
    if (questIds == null || questIds.length == 0) {
      questIds = new Integer[] {139, 158, 218, 219, 181, 182};
    }
    warService.completeQuestByGroup(group, Arrays.asList(questIds));
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/checkWar",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> checkWar() {
    warService.checkWar();
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/field",
      method = RequestMethod.POST,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> goToWarField(
      @RequestParam String login, @RequestParam String warField) {
    warService.goToWarField(login, warField);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/host",
      method = RequestMethod.POST,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> chooseWarHost(
      @RequestParam String login, @RequestParam int warHost) {
    warService.chooseWarHost(login, warHost);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/pc/{login}/{pc}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> setPc(@PathVariable String login, @PathVariable boolean pc) {
    warService.setPc(login, pc);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }
}
