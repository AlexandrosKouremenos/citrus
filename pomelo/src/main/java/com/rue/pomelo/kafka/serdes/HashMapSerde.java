package com.rue.pomelo.kafka.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

public class HashMapSerde implements CustomSerde {

    private static class HashMapSerializer implements Serializer<HashMap<String, Float>> {

        private static final Logger LOGGER = LoggerFactory.getLogger(HashMapSerializer.class);
        
        @Override
        public byte[] serialize(String topic, HashMap<String, Float> data) {
            
            if (data == null) return null;

            try {

                return MAPPER.writeValueAsBytes(data);

            } catch (JsonProcessingException e) {
                
                LOGGER.error("Failed to serialize HashMap due to", e);
                throw new RuntimeException(e);
                
            }
       
        }

    }

    private static class HashMapDeserializer implements Deserializer<HashMap<String, Float>> {

        private static final Logger LOGGER = LoggerFactory.getLogger(HashMapDeserializer.class);
        
        @Override
        public HashMap<String, Float> deserialize(String topic, byte[] data) {

            if (data == null) return null;

            try {

                return MAPPER.readValue(data, new TypeReference<>() { });

            } catch (IOException e) {

                LOGGER.error("Failed to serialize HashMap due to", e);
                throw new RuntimeException(e);

            }

        }

    }

    public static Serde<HashMap<String, Float>> HashMap() {
        return Serdes.serdeFrom(new HashMapSerializer(), new HashMapDeserializer());
    }
    
}
