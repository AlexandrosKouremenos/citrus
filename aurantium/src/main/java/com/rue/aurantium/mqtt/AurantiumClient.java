package com.rue.aurantium.mqtt;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.rue.aurantium.data.DataPublisher;
import com.rue.aurantium.util.EventPublisher;
import com.rue.aurantium.util.StartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Building;

import java.util.UUID;

import static com.rue.aurantium.mqtt.BuildingTopic.getBuildingTopic;
import static java.nio.charset.StandardCharsets.UTF_8;

public class AurantiumClient extends DataPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AurantiumClient.class);

    private static final String HOST = System.getProperty("mqtt.host");

    private static final String USERNAME = System.getProperty("mqtt.username");

    private static final String PASSWORD = System.getProperty("mqtt.password");

    private final Mqtt5AsyncClient client;

    private final EventPublisher publisher;

    public AurantiumClient(String filePath, EventPublisher eventPublisher) {

        super(filePath);

        client = Mqtt5Client.builder()
                .serverHost(HOST)
                .serverPort(8883)
                .sslWithDefaultConfig()
                .identifier(UUID.randomUUID().toString())
                .buildAsync();

        LOGGER.info("Built client: " + client);
        this.publisher = eventPublisher;
        connectClient();

    }

    public void connectClient() {

        client.connectWith()
                .simpleAuth()
                .username(USERNAME)
                .password(UTF_8.encode(PASSWORD))
                .applySimpleAuth()
                .send()
                .whenComplete((mqtt5ConnAck, throwable) -> {
                    if (throwable != null) LOGGER.error("Unable to connect to broker.", throwable);
                    else {

                        LOGGER.info("Client [{}] is connected.", client);
                        subscribe();

                    }

                });

    }

    private void subscribe() {

        client.subscribeWith()
                .topicFilter(getBuildingTopic())
//                .callback(mqtt5Publish -> {
//                    LOGGER.info("Received " + new String(mqtt5Publish.getPayloadAsBytes()) + " from " + mqtt5Publish.getTopic());
//                })
                .send()
                .whenComplete((mqtt5SubAck, throwable) -> {
                    if (throwable != null) {

                        LOGGER.error("Failed to subscribe.", throwable);
                        shutdown();

                    } else {

                        LOGGER.info("Client [{}] subscribed.", client);
                        this.publisher.publishEvent(new StartEvent(this));

                    }
                });


    }


    public void publish(Building building) {

        client.publishWith()
                .topic(getBuildingTopic())
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(building.toByteArray())
                .send();

    }

    @Override
    protected void shutdown() {

        LOGGER.info("Shutting down application...");
        shuttingDown = true;

        client.disconnect().whenComplete((ack, throwable) -> {

            if (throwable == null) LOGGER.info("Disconnecting client.");
            else LOGGER.error("Shit.. ",  throwable);

        });
        
    }

}