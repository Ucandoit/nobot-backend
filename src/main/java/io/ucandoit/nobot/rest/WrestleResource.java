package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.service.WrestleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/rest/wrestle")
public class WrestleResource {

  @Resource private WrestleService wrestleService;

  @RequestMapping(
      value = "/startWrestling",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> startWrestling(@RequestParam String login) {
    wrestleService.startWrestling(login);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/stopWrestling",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> stopWrestling(@RequestParam String login) {
    wrestleService.stopWrestling(login);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }
}
