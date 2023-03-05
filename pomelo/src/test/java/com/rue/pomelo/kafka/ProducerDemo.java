package com.rue.pomelo.kafka;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;

public class ProducerDemo {

    private static final Logger logger = LoggerFactory.getLogger(ProducerDemo.class);

    public static final String KAFKA_TOPIC = "kafka-test";

    public static void main(String[] args) {

        String bootstrapServers = "127.0.0.1:9092";

        // create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        KafkaProducer<String, byte[]> producer = new KafkaProducer<>(properties);

        // create a producer record
        ProducerRecord<String, byte[]> producerRecord =
                new ProducerRecord<>(KAFKA_TOPIC, "test","hello world".getBytes());

        try (Admin admin = Admin.create(properties)) {

            String topicName = KAFKA_TOPIC;
            int partitions = 1;
            short replicationFactor = 1;

            // Create a compacted topic
            CreateTopicsResult result = admin.createTopics(
                    singleton(new NewTopic(topicName, partitions, replicationFactor)
                            .configs(singletonMap(TopicConfig.CLEANUP_POLICY_CONFIG,
                                    TopicConfig.CLEANUP_POLICY_COMPACT))));

            // Call values() to get the result for a specific topic
            KafkaFuture<Void> future = result.values().get(topicName);

            // Call get() to block until the topic creation is complete or has failed
            // if creation failed the ExecutionException wraps the underlying cause.
            future.get();

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

/*
*
* kafka.topic=kafka-test;mqtt.topic=mqtt-test;kafka.bootstrap.servers=localhost:9092;kafka.group.id=1
*
* */

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

}
