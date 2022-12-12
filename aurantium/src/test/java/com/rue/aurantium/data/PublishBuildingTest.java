package com.rue.aurantium.data;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import protobuf.Building;
import protobuf.Outlet;

import java.util.Scanner;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PublishBuildingTest {

    private static final String HOST = System.getProperty("mqtt.host");

    private static final String USERNAME = System.getProperty("mqtt.username");

    private static final String PASSWORD = System.getProperty("mqtt.password");

    private static Mqtt5AsyncClient client;

    public static void main(String[] args) {

        /*
        * Example took from
        * https://github.com/hivemq/hivemq-mqtt-client/blob/master/examples/src/main/java/com/hivemq/client/mqtt/examples/AsyncDemo.java
        * */

        Outlet outlet = Outlet
                .newBuilder()
                .setId("0")
                .setConsumption("max")
                .build();

        Building building = Building
                .newBuilder()
                .setId("0")
                .setTimestamp(String.valueOf(System.currentTimeMillis()))
                .addOutlets(outlet)
                .build();

        client = Mqtt5Client.builder()
                .serverHost(HOST)
                .serverPort(1883)
                .identifier(UUID.randomUUID().toString())
                .buildAsync();

        Scanner scanner = new Scanner(System.in);

        boolean r = true;

        while (r) {

            if (scanner.hasNext()) {

                String in = scanner.next();

                if (in.equals("b")) pub(building);
                else if (in.equals("s")) r = false;
            }

        }

    }

    public static void pub(Building building){
        client.connectWith()
            .simpleAuth()
            .username(USERNAME)
            .password(UTF_8.encode(PASSWORD))
            .applySimpleAuth()
            .send()
            .thenAccept(connAck -> System.out.println("connected " + connAck))
            .thenCompose(v -> client.publishWith().topic("demo/topic/b").qos(MqttQos.EXACTLY_ONCE).payload(building.toByteArray()).send().whenComplete(((mqtt5PublishResult, throwable) -> {
                System.out.println("Published: " + building.toString());
            } )))
            .thenAccept(publishResult -> System.out.println("published " + publishResult))
            .thenCompose(v -> client.disconnect())
            .thenAccept(v -> System.out.println("disconnected"));
    }

}
