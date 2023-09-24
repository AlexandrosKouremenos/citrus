package com.rue.aurantium;

import com.rue.aurantium.mqtt.AurantiumClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * @author Alex Kouremenos
 * */
@Service("aurantium")
public class Aurantium {

    public Aurantium() { }

    @Bean(destroyMethod = "shutdown")
    private AurantiumClient getAurantiumClient() {

        String filePath = System.getenv("filePath");
        if (filePath == null) throw new RuntimeException("File path is null.");

        return new AurantiumClient(filePath);

    }

}
