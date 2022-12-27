package com.rue.sunki.mqtt;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Machine;

import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SunkiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SunkiClient.class);

    private static final String HOST = System.getenv("mqtt.host");

    private static final String USERNAME = System.getenv("mqtt.username");

    private static final String PASSWORD = System.getenv("mqtt.password");

    private final Mqtt5AsyncClient client;

    public SunkiClient() {

        client = Mqtt5Client.builder()
                .serverHost(HOST)
                .serverPort(1883)
                .identifier(UUID.randomUUID().toString())
                .buildAsync();

        LOGGER.info("Client [{}] is built.", client);
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
                .topicFilter(getMachineTopic())
                .callback(mqtt5Publish -> {
                    try {

                        LOGGER.info("Received \n\t" +
                                Machine.parseFrom(mqtt5Publish.getPayloadAsBytes()) +
                                " \n from " +
                                mqtt5Publish.getTopic());

                    } catch (InvalidProtocolBufferException e) { throw new RuntimeException(e); }

                })
                .send()
                .whenComplete((mqtt5SubAck, throwable) -> {
                    if (throwable != null) {

                        LOGGER.error("Failed to subscribe.", throwable);
                        shutdown();

                    } else LOGGER.info("Client [{}] subscribed.", client);

                });

    }

    protected void shutdown() {

        LOGGER.info("Shutting down application...");

        client.disconnect().whenComplete((ack, throwable) -> {

            if (throwable == null) LOGGER.info("Disconnecting client.");
            else LOGGER.error("Shit.. ",  throwable);

        });

    }

    private static String getMachineTopic() { return "machine/#"; }

}
