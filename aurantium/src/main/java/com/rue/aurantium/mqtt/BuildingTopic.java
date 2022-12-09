package com.rue.aurantium.mqtt;

public class BuildingTopic {

    private static final String BUILDING_TOPIC = "building/" + System.getProperty("building.id");

    public BuildingTopic() { }

    public static String getBuildingTopic() { return BUILDING_TOPIC; }
}
