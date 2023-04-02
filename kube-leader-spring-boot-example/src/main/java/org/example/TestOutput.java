package org.example;

import de.gammas.kubeleader.core.KubeLeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TestOutput {

    private final Logger log = LoggerFactory.getLogger(TestOutput.class);
    @Autowired
    private KubeLeader kubeLeader;

    @EventListener
    public void startUp(ContextRefreshedEvent event) throws InterruptedException {
        Thread.sleep(10_000L);
        log.info("Am I currently leader? {}",kubeLeader.amILeader());
    }
}
