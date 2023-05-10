package com.rue.pomelo.kafka.serdes;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Machine;
import protobuf.SensorValue;

import java.io.IOException;
import java.util.List;

public class SensorListSerde implements CustomSerde {

    public static class SensorListSerializer implements Serializer<List<SensorValue>> {

        public static final SensorListSerializer SENSOR_LIST_SERIALIZER =
                new SensorListSerializer();

        @Override
        public byte[] serialize(String topic, List<SensorValue> data) {

            if (data == null) return null;

            return Machine.newBuilder()
                    .addAllSensorValues(data)
                    .setId("dummy-serializer")
                    .build()
                    .toByteArray();

        }

    }

    public static class SensorListDeserializer implements Deserializer<List<SensorValue>> {

        private static final Logger LOGGER = LoggerFactory.getLogger(SensorListDeserializer.class);

        @Override
        public List<SensorValue> deserialize(String topic, byte[] data) {

            if (data == null) return null;

            try { return Machine.parseFrom(data).getSensorValuesList(); }
            catch (IOException e) {

                LOGGER.error("Failed to serialize HashMap due to", e);
                throw new RuntimeException(e);

            }

        }
    }

    public static Serde<List<SensorValue>> SensorList() {
        return Serdes.serdeFrom(new SensorListSerializer(), new SensorListDeserializer());
    }

}
