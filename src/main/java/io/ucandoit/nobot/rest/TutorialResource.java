package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.service.TutorialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/rest/tutorial")
public class TutorialResource {

  @Resource private TutorialService tutorialService;

  @RequestMapping(
      value = "/start/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> startTutorial(@PathVariable String login) {
    tutorialService.startTutorial(login);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/build/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> build(@PathVariable String login) {
    tutorialService.startBuild(login);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/stopBuild/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> stopBuild(@PathVariable String login) {
    tutorialService.stopBuild(login);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }
}
