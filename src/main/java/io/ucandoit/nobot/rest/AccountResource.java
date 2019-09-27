package io.ucandoit.nobot.rest;

import io.ucandoit.nobot.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/rest/account")
public class AccountResource {

    @Resource
    private AccountService accountService;

    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Boolean> generalInfo() {
        accountService.getAccountsGeneralInfo();
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

}
