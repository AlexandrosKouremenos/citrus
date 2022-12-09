package com.rue.aurantium;

import com.rue.aurantium.mqtt.AurantiumClient;
import com.rue.aurantium.util.EventPublisher;
import com.rue.aurantium.util.StartEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service("aurantium")
public class Aurantium {

    final EventPublisher eventPublisher;

    private AurantiumClient aurantiumClient;

    public Aurantium(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Bean(destroyMethod = "shutdown")
    private AurantiumClient getAurantiumClient() {

        String filePath = System.getProperty("file.path");
        return new AurantiumClient(filePath, eventPublisher);

    }

    @EventListener(ApplicationReadyEvent.class)
    public void createClient() { aurantiumClient = getAurantiumClient(); }

    @EventListener(StartEvent.class)
    public void start() { aurantiumClient.start(); }

}
