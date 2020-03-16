package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.enums.Training;
import io.ucandoit.nobot.service.TrainingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/rest/training")
public class TrainingResource {

  @Resource private TrainingService trainingService;

  @RequestMapping(
      value = "start/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> startTraining(
      @PathVariable String login,
      @RequestParam String catId,
      @RequestParam Training training,
      @RequestParam int level,
      Boolean once) {
    trainingService.startTraining(login, catId, training, level, once != null && once);
    return new ResponseEntity<>(true, HttpStatus.OK);
  }
}
