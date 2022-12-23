package com.rue.aurantium.mqtt;

public class MachineTopic {

    private static final String MACHINE_TOPIC = "machine/" + System.getenv("machine.id");

    public MachineTopic() { }

    public static String getMachineTopic() { return MACHINE_TOPIC; }
}
