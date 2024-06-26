package com.rue.aurantium.mqtt;

import com.hivemq.client.internal.mqtt.lifecycle.MqttClientAutoReconnectImpl;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5DisconnectException;
import com.hivemq.client.util.TypeSwitch;
import com.rue.aurantium.data.DataPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Machine;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * @author Alex Kouremenos
 * */
public class AurantiumClient extends DataPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AurantiumClient.class);

    private static final String HOST = System.getenv("mqtt.host");

    public static final String MACHINE_TOPIC = "machine/" + System.getenv("machine.id");

    private static final int PORT = Integer.parseInt(System.getenv("mqtt.port"));

    private final Mqtt5AsyncClient client;

    public AurantiumClient(String filePath) {

        super(filePath);

        client = Mqtt5Client.builder()
                .serverHost(HOST)
                .serverPort(PORT)
                .identifier(UUID.randomUUID().toString())
                .automaticReconnect(MqttClientAutoReconnectImpl.DEFAULT)
                .addConnectedListener(context -> LOGGER.info("Client received a ConnAck."))
                .addDisconnectedListener(context -> {

                    InetSocketAddress serverAddress = context.getClientConfig().getServerAddress();
                    TypeSwitch.when(context.getCause())
                            .is(ConnectionFailedException.class,
                                    d -> LOGGER.error("Connection with: [{}] refused.",
                                            serverAddress))
                            .is(Mqtt5DisconnectException.class,
                                    d -> LOGGER.info("Connection with: [{}] successfully closed.",
                                            serverAddress));

                })
                .buildAsync();

        LOGGER.info("Client [{}] is built.", client);
        connectClient();

    }

    private void connectClient() {

        client.connectWith()
                .send()
                .whenComplete((mqtt5ConnAck, throwable) -> {
                    if (throwable != null) LOGGER.error("Unable to connect to broker.", throwable);
                    else {

                        LOGGER.info("Client [{}] is connected.", client);
                        super.start();

                    }

                });

    }


    public void publish(Machine machine) {

        client.publishWith()
                .topic(MACHINE_TOPIC)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(machine.toByteArray())
                .send();

    }

    @Override
    protected void shutdown() {

        LOGGER.info("Shutting down application...");
        shuttingDown = true;

        client.disconnect().whenComplete((ack, throwable) -> {

            if (throwable == null) LOGGER.info("Client disconnected.");
            else LOGGER.error("Shit.. ",  throwable);

        });
        
    }

}