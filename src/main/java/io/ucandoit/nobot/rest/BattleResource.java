package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.service.BattleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/rest/battle")
public class BattleResource {

    @Resource
    private BattleService battleService;

    @RequestMapping(value = "/startBattle/{login}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Boolean> startSniping(@PathVariable String login) {
        battleService.startBattle(login);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

}
