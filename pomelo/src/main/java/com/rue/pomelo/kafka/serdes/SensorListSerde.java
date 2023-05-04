package com.rue.pomelo.kafka.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.SensorValue;

import java.io.IOException;
import java.util.List;

public class SensorListSerde implements CustomSerde {

    private static class SensorListSerializer implements Serializer<List<SensorValue>> {

        private static final Logger LOGGER = LoggerFactory.getLogger(SensorListSerializer.class);

        @Override
        public byte[] serialize(String topic, List<SensorValue> data) {

            if (data == null) return null;

            try {

                return MAPPER.writeValueAsBytes(data);

            } catch (JsonProcessingException e) {

                LOGGER.error("Failed to serialize List due to", e);
                throw new RuntimeException(e);

            }

        }

    }

    private static class SensorListDeserializer implements Deserializer<List<SensorValue>> {

        private static final Logger LOGGER = LoggerFactory.getLogger(SensorListDeserializer.class);

        @Override
        public List<SensorValue> deserialize(String topic, byte[] data) {

            if (data == null) return null;

            try {

                return MAPPER.readValue(data, new TypeReference<>() { });

            } catch (IOException e) {

                LOGGER.error("Failed to serialize HashMap due to", e);
                throw new RuntimeException(e);

            }

        }
    }

    public static Serde<List<SensorValue>> SensorList() {
        return Serdes.serdeFrom(new SensorListSerializer(), new SensorListDeserializer());
    }

}
