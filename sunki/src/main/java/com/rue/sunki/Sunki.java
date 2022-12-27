package com.rue.sunki;

import com.rue.sunki.mqtt.SunkiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class Sunki {

    public Sunki() { }

    @Bean(destroyMethod = "shutdown")
    private SunkiClient getSunkiClient() { return new SunkiClient(); }

}
