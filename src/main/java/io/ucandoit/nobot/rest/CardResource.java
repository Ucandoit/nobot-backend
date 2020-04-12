package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.dto.CardInfo;
import io.ucandoit.nobot.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/rest/card")
public class CardResource {

  @Resource private CardService cardService;

  @RequestMapping(
      value = "/{cardId}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<CardInfo> accountInfo(
      @PathVariable String cardId, @RequestParam String login) {
    return new ResponseEntity<>(cardService.getCardInfo(login, cardId), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/scan-books/{login}",
      method = RequestMethod.GET,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity scanBooks(@PathVariable String login) {
    cardService.scanBooks(login);
    return new ResponseEntity(HttpStatus.OK);
  }

  @RequestMapping(
          value = "/scan-books/all",
          method = RequestMethod.GET,
          produces = "application/json; charset=UTF-8")
  public ResponseEntity scanBooksForAll() {
    cardService.scanBooksForAll();
    return new ResponseEntity(HttpStatus.OK);
  }
}
