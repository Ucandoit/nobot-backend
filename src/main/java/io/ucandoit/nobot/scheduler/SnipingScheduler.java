package io.ucandoit.nobot.scheduler;

import io.ucandoit.nobot.service.AuctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;

@Component
public class SnipingScheduler {

    @Resource
    private AuctionService auctionService;

    private static final Logger log = LoggerFactory.getLogger(SnipingScheduler.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

//    @Scheduled(cron = "0 * * * * *")
//    public void reportCurrentTime() {
//        log.info("The time is now {}", dateFormat.format(new Date()));
//        auctionService.snipeAH();
//    }

}
