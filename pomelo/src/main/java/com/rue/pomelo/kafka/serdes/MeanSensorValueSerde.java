package com.rue.pomelo.kafka.serdes;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class MeanSensorValueSerde implements CustomSerde {

    private static class MeanSensorSerializer implements Serializer<MeanSensorValue> {

        private static final Logger LOGGER = LoggerFactory.getLogger(MeanSensorSerializer.class);

        @Override
        public byte[] serialize(String topic, MeanSensorValue data) {

            if (data == null) return null;

            try (
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    DataOutputStream dataOut = new DataOutputStream(outputStream)
            ) {
                return MAPPER.writeValueAsBytes(data);

//                dataOut.writeUTF(data.id());
//                dataOut.writeFloat(data.meanValue());
//                return outputStream.toByteArray();

            } catch (IOException e) {

                LOGGER.error("Failed to serialize MeanSensorValue due to", e);
                throw new RuntimeException(e);

            }

        }


    }


    private static class MeanSensorDeserializer implements Deserializer<MeanSensorValue> {

        private static final Logger LOGGER = LoggerFactory.getLogger(MeanSensorDeserializer.class);

        @Override
        public MeanSensorValue deserialize(String topic, byte[] data) {

            if (data == null) return null;

            try (
                    DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data))
            ) {

                return MAPPER.readValue(data, MeanSensorValue.class);

//                String id = inputStream.readUTF();
//                float meanValue = inputStream.readFloat();
//
//                return new MeanSensorValue(id, meanValue);

            } catch (IOException e) {

                LOGGER.error("Failed to deserialize MeanSensorValue due to", e);
                throw new RuntimeException(e);

            }

        }

    }

    public static Serde<MeanSensorValue> MeanSensorValue() {
        return Serdes.serdeFrom(new MeanSensorSerializer(), new MeanSensorDeserializer());
    }

    public record MeanSensorValue(String id, float meanValue) { }

}
