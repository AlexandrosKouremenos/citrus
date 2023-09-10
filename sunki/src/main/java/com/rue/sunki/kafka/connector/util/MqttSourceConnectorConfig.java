package com.rue.sunki.kafka.connector.util;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static java.util.UUID.randomUUID;
import static org.apache.kafka.clients.producer.ProducerConfig.*;
import static org.apache.kafka.common.config.ConfigDef.Importance.*;
import static org.apache.kafka.common.config.ConfigDef.Type.*;
/**
 * @author Alex Kouremenos
 * */
public class MqttSourceConnectorConfig extends AbstractConfig {

    public static final String MQTT_HOST = "mqtt.host";
    public static final String MQTT_HOST_DOC = "Hostname for MQTT broker.";

    public static final String MQTT_PORT = "mqtt.port";
    public static final String MQTT_PORT_DOC = "Port for MQTT broker.";

    public static final String MQTT_CLIENT_ID = "mqtt.clientID";
    public static final String MQTT_CLIENT_ID_DOC = "ID for the MQTT client.";

    public static final String MQTT_TOPIC_PREFIX = "mqtt.topic.prefix";
    public static final String MQTT_TOPIC_DOC = "Prefix of MQTT topics.";

    public static final String KAFKA_TOPIC = "kafka.topic";
    public static final String KAFKA_TOPIC_DOC = "Kafka topic.";

    private static final String ACKS_CONF_ENV = System.getenv("acks.conf");
    public static final String ACKS_CONF = ACKS_CONF_ENV == null ? "all" : ACKS_CONF_ENV;
    public static final String ACKS_DOC = "Acknowledgement";

    private static final String BATCH_SIZE_ENV = System.getenv("batch.size");
    public static final String BATCH_SIZE_CONF = BATCH_SIZE_ENV == null ? "16384" : BATCH_SIZE_ENV;
    public static final String BATCH_SIZE_DOC = "Batch size";

    private static final String LINGER_MS_ENV = System.getenv("linger.ms");
    public static final Long LINGER_MS_CONF = LINGER_MS_ENV == null ? 0L : Long.parseLong(LINGER_MS_ENV);
    public static final String LINGER_MS_CONF_DOC = "linger ms";

    public MqttSourceConnectorConfig(Map<?, ?> originals) { super(configDef(), originals); }

    public static ConfigDef configDef() {

        return new ConfigDef()
                .define(MQTT_HOST, STRING, HIGH, MQTT_HOST_DOC)
                .define(MQTT_PORT, INT, 1883, HIGH, MQTT_PORT_DOC)
                .define(MQTT_CLIENT_ID, STRING, randomUUID().toString(), HIGH, MQTT_CLIENT_ID_DOC)
                .define(MQTT_TOPIC_PREFIX, STRING, HIGH, MQTT_TOPIC_DOC)
                .define(KAFKA_TOPIC, STRING, HIGH, KAFKA_TOPIC_DOC)
                .define(ACKS_CONFIG, STRING, ACKS_CONF, LOW, ACKS_DOC)
                .define(BATCH_SIZE_CONFIG, STRING, BATCH_SIZE_CONF, LOW, BATCH_SIZE_DOC)
                .define(LINGER_MS_CONFIG, LONG, LINGER_MS_CONF, MEDIUM, LINGER_MS_CONF_DOC);

    }

    public static class Version {

        private static final String VERSION = System.getProperty("source.connector.version", "sunki-mqtt-kafka-connect");

        public static String getVersion() { return VERSION; }

    }

}
