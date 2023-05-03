package com.rue.pomelo.kafka.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface CustomSerde {

    ObjectMapper MAPPER = new ObjectMapper();

}
