package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.service.StoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/rest/story")
public class StoryResource {

  @Resource private StoryService storyService;

  @RequestMapping(
      value = "/startAll",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> startAll() {
    storyService.startAll();
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/stopAll",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> stopAll() {
    storyService.stopAll();
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/start/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> startStory(@PathVariable String login) {
    storyService.startStory(login);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/stop/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> stopStory(@PathVariable String login) {
    storyService.stopStory(login);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
          value = "/reward/{login}",
          method = RequestMethod.GET,
          produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> getReward(@PathVariable String login, @RequestParam int type) {
    storyService.getReward(login, type);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @RequestMapping(
          value = "/allReward",
          method = RequestMethod.GET,
          produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> getAllReward(@RequestParam int type) {
    storyService.getAllReward(type);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }
}
