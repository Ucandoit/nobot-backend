package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.service.WrestleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping(value = "/api/rest/wrestle")
public class WrestleResource {

    @Resource
    private WrestleService wrestleService;

    @RequestMapping(value = "/startWrestling", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Boolean> startWrestling(@RequestParam String login) throws UnsupportedEncodingException {
        wrestleService.startWrestling(login);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
