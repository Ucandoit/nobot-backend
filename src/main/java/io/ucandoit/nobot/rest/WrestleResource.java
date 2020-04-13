package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.service.WrestleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

  @RequestMapping(
      value = "/country/start/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> startCountryWrestling(@PathVariable String login, Integer times) {
    wrestleService.startCountryWrestling(login, times);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/country/stop/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> stopCountryWrestling(@PathVariable String login) {
    wrestleService.stopCountryWrestling(login);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }
}
