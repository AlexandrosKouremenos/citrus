package com.rue.pomelo;

import com.rue.pomelo.kafka.PomeloClient;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@Service
public class Pomelo {

    public static final Properties PROPS;

    public static final String GROUP_ID = System.getenv("kafka.group.id");

    public static final String SERVERS = System.getenv("kafka.bootstrap.servers");

    public static final String MQTT_TOPIC = "mqtt.topic";

    public static final String KAFKA_TOPIC = "kafka.topic";

    private static final String MQTT_TOPIC_PROP = System.getenv("mqtt.topic");

    private static final String KAFKA_TOPIC_PROP = System.getenv("kafka.topic");

    static {

        PROPS = new Properties();
        PROPS.setProperty(BOOTSTRAP_SERVERS_CONFIG, SERVERS);
        PROPS.setProperty(GROUP_ID_CONFIG, GROUP_ID);

        PROPS.setProperty(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        PROPS.setProperty(VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        PROPS.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest");
        PROPS.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "true");
        PROPS.setProperty(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

        PROPS.setProperty(KAFKA_TOPIC, KAFKA_TOPIC_PROP);
        PROPS.setProperty(MQTT_TOPIC, MQTT_TOPIC_PROP);

    }

    public Pomelo() { }

    @Bean(destroyMethod = "shutdown")
    private PomeloClient getPomeloClient() {

        PomeloClient pomeloClient = new PomeloClient(PROPS);
        pomeloClient.run();
        return pomeloClient;

    }

}
