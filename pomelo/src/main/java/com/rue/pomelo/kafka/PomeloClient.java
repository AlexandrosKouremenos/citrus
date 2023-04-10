package com.rue.pomelo.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Machine;
import protobuf.SensorValue;

import java.util.Properties;
import java.util.regex.Pattern;

import static com.rue.pomelo.Pomelo.KAFKA_TOPIC_PREFIX;
import static java.lang.Float.*;

public class PomeloClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(PomeloClient.class);

    private KafkaStreams streams;

    private final Properties properties;

    public PomeloClient(Properties properties) {

        this.properties = properties;
        start();

    }

    private void start() {

        StreamsBuilder builder = new StreamsBuilder();

        String topicPrefix = properties.getProperty(KAFKA_TOPIC_PREFIX);
        Pattern pattern = Pattern.compile(topicPrefix + ".*");

        KStream<String, Bytes> stream = builder.stream(pattern,
                Consumed.with(Serdes.String(), Serdes.Bytes()));

        KStream<String, Machine> machineData = stream.mapValues(value -> {

            try { return Machine.parseFrom(value.get()); }
            catch (InvalidProtocolBufferException e) { throw new RuntimeException(e); }

        });

        KStream<String, Machine> validateSensorValues = machineData.filter((key, value) ->
                value.getSensorValuesList()
                        .stream()
                        .noneMatch(PomeloClient::outOfRange));

        validateSensorValues.foreach((key, value) ->
                LOGGER.info("@DATA:" + key + ", " + value.toString()));

//        KStream<String, Bytes> outputData = validateSensorValues.mapValues(value -> Bytes.wrap(value.toByteArray()));
//        outputData.to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.Bytes()));

        Topology topology = builder.build();
        streams = new KafkaStreams(topology, properties);
        streams.start();

    }

    public void shutdown() {

        LOGGER.info("Closing Stream Consumer...");
        streams.close();

    }

    private static boolean outOfRange(SensorValue sensor) {

        return sensor == null ||
                isNaN(sensor.getMetrics()) ||
                sensor.getMetrics() == MAX_VALUE ||
                isInfinite(sensor.getMetrics());

    }

}
