package com.rue.pomelo.kafka.serdes;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.SensorValue;

public class SensorValueSerde implements CustomSerde {

    private static class SensorValueSerializer implements Serializer<SensorValue> {

        @Override
        public byte[] serialize(String topic, SensorValue data) { return data.toByteArray(); }

    }

    private static class SensorValueDeserializer implements Deserializer<SensorValue> {

        private static final Logger LOGGER = LoggerFactory.getLogger(SensorValueDeserializer.class);

        @Override
        public SensorValue deserialize(String topic, byte[] data) {

            try { return SensorValue.parseFrom(data); }
            catch (InvalidProtocolBufferException e) {

                LOGGER.error("Failed to deserialize SensorValue due to", e);
                throw new RuntimeException(e);

            }

        }

    }

    public static Serde<SensorValue> SensorValue() {
        return Serdes.serdeFrom(new SensorValueSerializer(), new SensorValueDeserializer());
    }

}
