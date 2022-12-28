package com.rue.protobuf;


import protobuf.Machine;
import protobuf.SensorValue;

public class Demo {
    public static void main(String[] args) {

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

        System.out.println(machine);


    }
}
