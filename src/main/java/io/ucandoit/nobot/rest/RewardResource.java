package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.service.RewardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/rest/reward")
public class RewardResource {

  @Resource private RewardService rewardService;

  @RequestMapping(
      value = "/itembox",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<Boolean> getItemboxReward() {
    rewardService.getItemboxReward();
    return new ResponseEntity<>(true, HttpStatus.OK);
  }
}
