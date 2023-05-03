package com.rue.pomelo.kafka.process;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rue.pomelo.kafka.serdes.HashMapSerde;
import com.rue.pomelo.kafka.serdes.MeanSensorValueSerde;
import com.rue.pomelo.kafka.serdes.MeanSensorValueSerde.MeanSensorValue;
import com.rue.pomelo.kafka.serdes.SensorListSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Machine;
import protobuf.SensorValue;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import static com.rue.pomelo.Pomelo.KAFKA_TOPIC_PREFIX;
import static java.lang.Float.*;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.NUM_STREAM_THREADS_CONFIG;

public class ProcessingDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingDemo.class);

    private static final String KAFKA_TOPIC = "machine";

    private static final String OUTPUT_TOPIC = "out.0";

    private static final Properties PROPS;

    static {

        PROPS = new Properties();
        PROPS.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-processing");
        PROPS.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        PROPS.setProperty(NUM_STREAM_THREADS_CONFIG, "1");

        PROPS.setProperty(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, "0");
        PROPS.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "true");
        PROPS.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        PROPS.setProperty(KAFKA_TOPIC_PREFIX, KAFKA_TOPIC);

    }

    public static void main(String[] args) {

        InferenceClient client = new InferenceClient(PROPS);

        Scanner scanner = new Scanner(System.in);
        boolean r = true;
        while (r) {

            if (scanner.hasNext()) {

                String in = scanner.next();

                if (in.equals("s")) client.start();
                else if (in.equals("q")) {

                    client.close();
                    r = false;

                }

            }

        }

        Runtime.getRuntime().exit(0);

    }

    public static class InferenceClient {

        private final Properties properties;

        private KafkaStreams streams;

        public InferenceClient(Properties properties) { this.properties = properties; }

        public void start() {

            // Create a stream builder
            StreamsBuilder builder = new StreamsBuilder();

            String topicPrefix = properties.getProperty(KAFKA_TOPIC_PREFIX);
            Pattern topicPattern = Pattern.compile(topicPrefix + ".*");

            // Create a KStream from the input topic
            KStream<String, Bytes> input = builder.stream(topicPattern,
                    Consumed.with(Serdes.String(), Serdes.Bytes()));

            KStream<String, Machine> machineData = input.mapValues(value -> {

                try {
                    return Machine.parseFrom(value.get());
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }

            });

            KStream<String, List<SensorValue>> sensorStream = machineData
                    .filter((key, value) -> value.getSensorValuesList()
                                    .stream()
                                    .noneMatch(InferenceClient::outOfRange))
                    .mapValues(Machine::getSensorValuesList);

            AtomicLong count = new AtomicLong(0);
            KStream<Windowed<String>, HashMap<String, Float>> meanSensorValues = sensorStream
                    .groupByKey(Grouped.with(Serdes.String(), SensorListSerde.SensorList()))
                    .windowedBy(getSlidingWindows(5, 500))
                    .emitStrategy(EmitStrategy.onWindowClose())
                    .aggregate(HashMap::new,
                            (key, sensors, meanValues) -> {

                                count.getAndIncrement();
                                sensors.forEach(sensor -> {

                                    if (sensor.getId().equals("Type")) return;

                                    meanValues.put(sensor.getId(),
                                            computeMeanValue(count.get(),
                                            meanValues,
                                            sensor));

                                });

                                return meanValues;

                            }, Materialized.with(Serdes.String(), HashMapSerde.HashMap()))
                    .suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded()))
                    .toStream();

            LOGGER.info("Windowed stream with mean values computed");

            KStream<String, MeanSensorValue> outputSensors = meanSensorValues
                    .mapValues(mean -> {

                        MeanSensorValue meanValue = null;
                        for (String sensorId : mean.keySet()) {

                            meanValue = new MeanSensorValue(sensorId, mean.get(sensorId));

                        }

                        return meanValue;

                    }).map((key, value) -> KeyValue.pair(key.key(), value));

            LOGGER.info("Stream with mean values built.");

            Serde<MeanSensorValue> meanSensorValueSerde = MeanSensorValueSerde.MeanSensorValue();
            outputSensors.to(OUTPUT_TOPIC, Produced.with(Serdes.String(), meanSensorValueSerde));

            // Create the Kafka Streams application and start it
            streams = new KafkaStreams(builder.build(), properties);
            streams.start();

        }

        public void close() { streams.close(); }

        private float computeMeanValue(Long count,
                                       HashMap<String, Float> meanValues,
                                       SensorValue sensor) {

            if (meanValues.get(sensor.getId()) != null) {

                LOGGER.info("SENSOR TYPE IS [{}]", sensor.getId());
                LOGGER.info("MEAN VALUE TYPE IS [{}]", meanValues.get(sensor.getId()));

            }

            Float prevMeanValue = meanValues.get(sensor.getId());
            if (prevMeanValue != null)
                return (prevMeanValue + sensor.getMetrics()) / count.floatValue();
            else return sensor.getMetrics();

        }

        @NotNull
        private static SlidingWindows getSlidingWindows(long range, long grace) {
            return SlidingWindows.ofTimeDifferenceAndGrace(ofSeconds(range), ofMillis(grace));
        }

        private static boolean outOfRange(SensorValue sensor) {

            return sensor == null ||
                    isNaN(sensor.getMetrics()) ||
                    sensor.getMetrics() == MAX_VALUE ||
                    isInfinite(sensor.getMetrics());

        }

        private static double normalize(double value) {

            double min = 0.0;
            double max = 100.0;
            return (value - min) / (max - min);

        }

    }

}
