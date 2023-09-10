package com.rue.sunki.connector;

import com.rue.sunki.kafka.connector.MqttSourceTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static com.rue.sunki.kafka.connector.util.MqttSourceConnectorConfig.*;

/**
 * @author Alex Kouremenos
 * */
public class MqttSourceTaskTest {

    private static final Map<String, String> props = new HashMap<>();

    static {

        props.put(MQTT_HOST, System.getenv("mqtt.host"));
        props.put(MQTT_PORT, "1883");
        props.put(MQTT_TOPIC_PREFIX, "mqtt-test");
        props.put(KAFKA_TOPIC, "test-kafka");

    }

    public static void main(String[] args) {

        MqttSourceTask sourceTask = new MqttSourceTask();

        sourceTask.start(props);

        Scanner scanner = new Scanner(System.in);

        boolean r = true;
        while (r) {

            if (scanner.hasNext()) {

                String in = scanner.next();

                if (in.equals("p")) {

                    try {
                        System.out.println(sourceTask.poll());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                } else if (in.equals("q")) r = false;
            }

        }

    }

}
