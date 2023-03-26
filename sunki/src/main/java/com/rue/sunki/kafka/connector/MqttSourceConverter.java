package com.rue.sunki.kafka.connector;


import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.rue.sunki.kafka.connector.util.MqttSourceConnectorConfig;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class MqttSourceConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttSourceConverter.class);

    public static SourceRecord convert(Mqtt5Publish mqttMessage) {

        LOGGER.info("Converting message type [{}] to type [{}].", Mqtt5Publish.class, SourceRecord.class);

        return new SourceRecord(new HashMap<>(),
                new HashMap<>(),
                mqttMessage.getTopic().toString().replace("/", "."),
                null,
                Schema.STRING_SCHEMA,
                mqttMessage.getTopic(),
                Schema.BYTES_SCHEMA,
                mqttMessage.getPayloadAsBytes(),
                System.currentTimeMillis()
        );

    }

    public static void main(String[] args) {
        System.out.println(MqttSourceConnectorConfig.Version.getVersion());
    }
}
