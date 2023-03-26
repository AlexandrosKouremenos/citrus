package com.rue.sunki.kafka.connector.util;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;

import java.util.Map;
import java.util.UUID;

import static org.apache.kafka.common.config.ConfigDef.Type;

public class MqttSourceConnectorConfig extends AbstractConfig {

    public static final String MQTT_HOST = "mqtt.host";
    public static final String MQTT_HOST_DOC = "Hostname for MQTT broker.";

    public static final String MQTT_PORT = "mqtt.port";
    public static final String MQTT_PORT_DOC = "Port for MQTT broker.";

    public static final String MQTT_CLIENT_ID = "mqtt.clientID";
    public static final String MQTT_CLIENT_ID_DOC = "ID for the MQTT client.";

    public static final String MQTT_TOPIC_PREFIX = "mqtt.topic.prefix";
    public static final String MQTT_TOPIC_DOC = "Prefix of MQTT topics.";

    public static final String MQTT_USERNAME = "mqtt.username";
    public static final String MQTT_USERNAME_DOC = "Username for MQTT authentication.";

    public static final String MQTT_PASSWORD = "mqtt.password";
    public static final String MQTT_PASSWORD_DOC = "Password for MQTT authentication.";

    public static final String KAFKA_TOPIC = "kafka.topic";
    public static final String KAFKA_TOPIC_DOC = "Kafka topic.";

    public MqttSourceConnectorConfig(Map<?, ?> originals) { super(configDef(), originals); }

    public static ConfigDef configDef() {

        return new ConfigDef()
                .define(MQTT_HOST,
                        Type.STRING,
                        Importance.HIGH,
                        MQTT_HOST_DOC)
                .define(MQTT_PORT,
                        Type.INT,
                        1883,
                        Importance.HIGH,
                        MQTT_PORT_DOC)
                .define(MQTT_CLIENT_ID,
                        Type.STRING,
                        UUID.randomUUID().toString(),
                        Importance.HIGH,
                        MQTT_CLIENT_ID_DOC)
                .define(MQTT_TOPIC_PREFIX,
                        Type.STRING,
                        Importance.HIGH,
                        MQTT_TOPIC_DOC)
                .define(KAFKA_TOPIC,
                        Type.STRING,
                        Importance.HIGH,
                        KAFKA_TOPIC_DOC)
                .define(MQTT_USERNAME,
                        Type.STRING,
                        Importance.MEDIUM,
                        MQTT_USERNAME_DOC)
                .define(MQTT_PASSWORD,
                        Type.STRING,
                        Importance.MEDIUM,
                        MQTT_PASSWORD_DOC);

    }

    public static class Version {

        private static final String VERSION = System.getProperty("source.connector.version", "sunki-mqtt-kafka-connect");

        public static String getVersion() { return VERSION; }

    }

}
