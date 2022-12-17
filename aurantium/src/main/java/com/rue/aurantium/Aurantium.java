package com.rue.aurantium;

import com.rue.aurantium.mqtt.AurantiumClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service("aurantium")
public class Aurantium {

    public Aurantium() { }

    @Bean(destroyMethod = "shutdown")
    private AurantiumClient getAurantiumClient() {

        String filePath = System.getenv("filePath");
//        String filePath = "/home/alex/Repos/citrus/aurantium/src/main/resources/iot-data/building0-short/dataset_2013-12-07-short.csv";
        return new AurantiumClient(filePath);

    }

}
