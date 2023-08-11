package com.rue.pomelo.kafka.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Alex Kouremenos
 * */
public interface CustomSerde {

    ObjectMapper MAPPER = new ObjectMapper();

}
