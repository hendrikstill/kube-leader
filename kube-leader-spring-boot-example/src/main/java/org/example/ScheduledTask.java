package org.example;

import de.gammas.kubeleader.spring.annotation.IfIsKubeLeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ScheduledTask {
    private Logger log = LoggerFactory.getLogger(ScheduledTask.class);
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    @IfIsKubeLeader
    public void doStuffEverySecond(){

        log.info("Hey I'm doing something");
    }
}
