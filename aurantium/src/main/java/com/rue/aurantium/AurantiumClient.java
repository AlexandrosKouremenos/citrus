package com.rue.aurantium;

import com.rue.aurantium.data.DataPublisher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service("aurantiumClient")
public class AurantiumClient {

    public AurantiumClient() { }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {

        DataPublisher dataPublisher = getDataPublisher();
        dataPublisher.start();

    }

    @Bean(destroyMethod = "shutdown")
    private DataPublisher getDataPublisher() {

        String filePath = System.getProperty("file.path");
        return new DataPublisher(filePath);

    }

}
