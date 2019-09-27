package io.ucandoit.nobot.config;

import io.ucandoit.nobot.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HttpClientConfig {

    @Bean
    public HttpClient httpClient() {
        log.info("Initialising HTTP client");
        return new HttpClient();
    }
}
