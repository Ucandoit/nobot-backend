package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.service.AuctionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/rest/auction")
public class AuctionResource {

    @Resource
    private AuctionService auctionService;

    @RequestMapping(value = "/startSniping/{login}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Boolean> startSniping(@PathVariable String login) {
        auctionService.snipeAH(login);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @RequestMapping(value = "stopSniping/{login}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Boolean> stopSniping(@PathVariable String login) {
        auctionService.stopSnipeAH(login);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @RequestMapping(value = "startAll", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Boolean> startAll() {
        auctionService.snipeAH();
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

}
