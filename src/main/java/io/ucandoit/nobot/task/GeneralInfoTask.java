package io.ucandoit.nobot.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component("generalInfoTask")
@Scope("prototype")
public class GeneralInfoTask implements Supplier {

    private String login;

    @Override
    public Object get() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
