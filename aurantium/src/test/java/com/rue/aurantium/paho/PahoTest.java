package com.rue.aurantium.paho;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PahoTest {


    public static void main(String[] args) {
        try {

            MqttClient client = new MqttClient("tcp://test.mosquitto.org:1883", "my_subscriber", new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            /*
            * Set username and password from MqttConnectOptions method here.
            * */
            options.setAutomaticReconnect(true);

            client.connect(options);
            client.publish("machine/0", new MqttMessage("Hello!".getBytes()));
            client.subscribe("machine/0");

            client.setCallback(new MqttCallback() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("Received message: " + message.toString());
                }

                @Override
                public void connectionLost(Throwable cause) {
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            System.out.println("Waiting for messages...");

            // Keep the application running to receive messages
            while (true) {
                Thread.sleep(1000);
            }
        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }

    }


}
