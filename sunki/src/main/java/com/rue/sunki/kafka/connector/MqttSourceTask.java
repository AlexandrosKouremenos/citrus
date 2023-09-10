package com.rue.sunki.kafka.connector;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.rue.sunki.kafka.connector.util.MqttSourceConnectorConfig;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Machine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.rue.sunki.kafka.connector.MqttSourceConverter.convert;
import static com.rue.sunki.kafka.connector.util.MqttSourceConnectorConfig.*;
import static java.util.Collections.singletonList;

/**
 * @author Alex Kouremenos
 * */
public class MqttSourceTask extends SourceTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttSourceTask.class);

    private MqttSourceConnectorConfig sourceConnectorConfig;

    private Mqtt5AsyncClient mqttClient;

    private final BlockingQueue<SourceRecord> queue = new LinkedBlockingQueue<>();

    @Override
    public String version() { return MqttSourceConnectorConfig.Version.getVersion(); }

    @Override
    public void start(Map<String, String> props) {

        sourceConnectorConfig = new MqttSourceConnectorConfig(props);

        mqttClient = Mqtt5Client.builder()
                .serverHost(sourceConnectorConfig.getString(MQTT_HOST))
                .serverPort(sourceConnectorConfig.getInt(MQTT_PORT))
                .identifier(sourceConnectorConfig.getString(MQTT_CLIENT_ID))
                .automaticReconnect()
                .initialDelay(1, TimeUnit.SECONDS)
                .maxDelay(10, TimeUnit.SECONDS)
                .applyAutomaticReconnect()
                .addConnectedListener(context -> LOGGER.info("Client received a ConnAck."))
                .addDisconnectedListener(context ->
                        LOGGER.warn("Client is not connected yet or will disconnect."))
                .buildAsync();

        connect();

    }

    private void connect() {

        mqttClient.connectWith()
                .send()
                .whenComplete((mqtt5ConnAck, throwable) -> {
                    if (throwable != null) LOGGER.error("Unable to connect to broker.", throwable);
                    else {

                        LOGGER.info("Client [{}] is connected.", mqttClient);
                        subscribe();

                    }

                });
    }

    private void subscribe() {

        mqttClient.subscribeWith()
                .topicFilter(getTopicWildcard())
                .callback(this::accept)
                .send()
                .whenComplete((mqtt5SubAck, throwable) -> {
                    if (throwable != null) {

                        LOGGER.error("Failed to subscribe.", throwable);
                        stop();

                    } else LOGGER.info("Client [{}] subscribed.", mqttClient);

                });

    }

    private void convertToKafka(Mqtt5Publish mqttMessage) {

        SourceRecord sourceRecord = convert(mqttMessage);
        queue.add(sourceRecord);

    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        return singletonList(queue.take());

    }

    private void accept(Mqtt5Publish mqtt5Publish) {

        // Produce records
        convertToKafka(mqtt5Publish);

        try {

            LOGGER.info("Received \n\t" +
                    Machine.parseFrom(mqtt5Publish.getPayloadAsBytes()) +
                    " \n from " +
                    mqtt5Publish.getTopic());

        } catch (InvalidProtocolBufferException e) { throw new RuntimeException(e); }

    }

    private String getTopicWildcard() {

        // TODO: Drop the use of wildcard. Support multiple topics instead.
        return sourceConnectorConfig.getString(MQTT_TOPIC_PREFIX) + "/#";

    }

    @Override
    public synchronized void stop() {

        LOGGER.info("Shutting down application...");

        mqttClient.disconnect().whenComplete((ack, throwable) -> {

            if (throwable == null) LOGGER.info("Disconnecting client.");
            else LOGGER.error("Shit.. ",  throwable);

        });

    }

}
