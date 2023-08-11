package com.rue.pomelo.kafka;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import protobuf.Machine;
import protobuf.SensorValue;

import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.singletonMap;

/**
 * @author Alex Kouremenos
 * */
public class ProducerDemo {

    public static final String KAFKA_TOPIC = "machine.0";

    public static final String OUTPUT_TOPIC = "out.0";

    public static void main(String[] args) {

        String bootstrapServers = "127.0.0.1:9092";

        // create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class.getName());

        KafkaProducer<String, Bytes> producer = new KafkaProducer<>(properties);

        Machine machine = getMachine();

        // create a producer record
        ProducerRecord<String, Bytes> producerRecord =
                new ProducerRecord<>(KAFKA_TOPIC, "test", Bytes.wrap(machine.toByteArray()));

        try (Admin admin = Admin.create(properties)) {

            int partitions = 1;
            short replicationFactor = 1;

            // Create a compacted topic
            CreateTopicsResult result = admin.createTopics(Arrays.asList(
                    new NewTopic(KAFKA_TOPIC, partitions, replicationFactor)
                            .configs(singletonMap(TopicConfig.CLEANUP_POLICY_CONFIG,
                                    TopicConfig.CLEANUP_POLICY_COMPACT)),
                    new NewTopic(OUTPUT_TOPIC, partitions, replicationFactor)
                            .configs(singletonMap(TopicConfig.CLEANUP_POLICY_CONFIG,
                                    TopicConfig.CLEANUP_POLICY_COMPACT)))
            );

            // Call values() to get the result for a specific topic
            KafkaFuture<Void> future = result.values().get(KAFKA_TOPIC);

            // Call get() to block until the topic creation is complete or has failed
            // if creation failed the ExecutionException wraps the underlying cause.
            future.get();

            future = result.values().get(OUTPUT_TOPIC);
            future.get();

        } catch (ExecutionException | InterruptedException e) { throw new RuntimeException(e); }

        Scanner scanner = new Scanner(System.in);
        boolean r = true;
        while (r) {

            if (scanner.hasNext()) {

                String in = scanner.next();

                if (in.equals("s")) producer.send(producerRecord);
                else if (in.equals("q")) r = false;

            }

        }

        producer.flush();
        producer.close();

    }

    private static Machine getMachine() {

        SensorValue typeSensor = SensorValue.newBuilder()
                .setId("Type")
                .setMetrics(1F)
                .build();

        SensorValue airSensor = SensorValue.newBuilder()
                .setId("Air")
                .setMetrics(12)
                .build();

        SensorValue processSensor = SensorValue.newBuilder()
                .setId("Process")
                .setMetrics(112)
                .build();

        SensorValue rotationalSensor = SensorValue.newBuilder()
                .setId("Rotational")
                .setMetrics(1122)
                .build();

        SensorValue torqueSensor = SensorValue.newBuilder()
                .setId("Torque")
                .setMetrics(14)
                .build();

        return Machine.newBuilder()
                .setId("0")
                .addSensorValues(typeSensor)
                .addSensorValues(airSensor)
                .addSensorValues(processSensor)
                .addSensorValues(rotationalSensor)
                .addSensorValues(torqueSensor)
                .build();

    }

}
