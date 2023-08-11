package com.rue.aurantium.data;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import protobuf.Machine;
import protobuf.SensorValue;

import java.util.Scanner;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Alex Kouremenos
 * */
public class PublishMachineTest {

    private static final String HOST = System.getenv("mqtt.host");

    private static final String USERNAME = System.getenv("mqtt.username");

    private static final String PASSWORD = System.getenv("mqtt.password");

    private static Mqtt5AsyncClient client;

    public static void main(String[] args) {

        /*
        * Example took from
        * https://github.com/hivemq/hivemq-mqtt-client/blob/master/examples/src/main/java/com/hivemq/client/mqtt/examples/AsyncDemo.java
        * */

        SensorValue typeSensor = SensorValue.newBuilder()
                .setId("Type")
                .setMetrics(1F)
                .build();

        SensorValue airSensor = SensorValue.newBuilder()
                .setId("Air")
                .setMetrics(12)
                .build();

        SensorValue processSensor = SensorValue.newBuilder()
                .setId("Process")
                .setMetrics(112)
                .build();

        SensorValue rotationalSensor = SensorValue.newBuilder()
                .setId("Rotational")
                .setMetrics(1122)
                .build();

        SensorValue torqueSensor = SensorValue.newBuilder()
                .setId("Torque")
                .setMetrics(14)
                .build();

        Machine machine = Machine.newBuilder()
                .setId("0")
                .addSensorValues(typeSensor)
                .addSensorValues(airSensor)
                .addSensorValues(processSensor)
                .addSensorValues(rotationalSensor)
                .addSensorValues(torqueSensor)
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

                if (in.equals("b")) pub(machine);
                else if (in.equals("s")) r = false;
            }

        }

    }

    public static void pub(Machine machine) {

        client.connectWith()
                .simpleAuth()
                .username(USERNAME)
                .password(UTF_8.encode(PASSWORD))
                .applySimpleAuth()
                .send()
                .thenAccept(connAck -> System.out.println("connected " + connAck))
                .thenCompose(v -> client
                        .publishWith()
                        .topic("machine/0")
                        .qos(MqttQos.EXACTLY_ONCE)
                        .payload(machine.toByteArray())
                        .send()
                        .whenComplete(((mqtt5PublishResult, throwable) -> System.out.println("Published: " + machine))))
                .thenAccept(publishResult -> System.out.println("published " + publishResult))
                .thenCompose(v -> client.disconnect())
                .thenAccept(v -> System.out.println("disconnected"));

    }

}
