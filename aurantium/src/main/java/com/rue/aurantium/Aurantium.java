package com.rue.aurantium;

import com.rue.aurantium.mqtt.AurantiumClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service("aurantium")
public class Aurantium {

    public Aurantium() { }

    @Bean(destroyMethod = "shutdown")
    private AurantiumClient getAurantiumClient() {

        String filePath = System.getProperty("file.path");
        return new AurantiumClient(filePath);

    }

}
