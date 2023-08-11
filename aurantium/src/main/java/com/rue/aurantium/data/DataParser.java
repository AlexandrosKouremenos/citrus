package com.rue.aurantium.data;

import protobuf.Machine;
import protobuf.SensorValue;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rue.protobuf.SensorTypes.SENSOR_TYPES;

/**
 * @author Alex Kouremenos
 * */
public class DataParser {

    private static final String COMMA = ",";

    private static final Pattern PATTERN = Pattern.compile("[LHM]");

    private final Machine.Builder machineBuilder;

    private final Map<Integer, SensorValue.Builder> sensors = new HashMap<>();

    private boolean startup = true;

    public DataParser(String machineId) {

        machineBuilder = Machine.newBuilder();
        machineBuilder.setId(machineId);

    }

    protected Machine parseData(String line) {

        String[] values = line.split(COMMA);

        if (startup) {

            initialize(values);
            return null;

        } else clearBuilder();

        for (int i = 0; i < values.length; i++) {

            String metric = values[i];
            SensorValue sensorValue = getSensorValue(i, metric);
            machineBuilder.addSensorValues(sensorValue);

        }

        return machineBuilder.build();

    }

    private SensorValue getSensorValue(int i, String metric) {

        Matcher matcher = PATTERN.matcher(metric);
        return matcher.matches() ?
                sensors.get(i)
                        .setMetrics(SENSOR_TYPES.get(matcher.group()))
                        .build() :
                sensors.get(i)
                        .setMetrics(Float.parseFloat(metric))
                        .build();
    }

    private void initialize(String[] values) {

        for (int i = 0; i < values.length; i++)
            sensors.put(i, SensorValue.newBuilder().setId(String.valueOf(i)));

        startup = false;

    }

    private void clearBuilder() { machineBuilder.clearSensorValues(); }

}
