package com.rue.aurantium.data;

import protobuf.Building;
import protobuf.Outlet;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.currentTimeMillis;

public class DataParser {

    private static final String COMMA = ",";

    // We need the offset to skip the timestamp in the data.
    private static final int OFFSET = 1;

    public static final String UNPLUGGED = "unplugged";

    private final Building.Builder builder;

    private final Map<Integer, Outlet.Builder> outlets = new HashMap<>();

    private Integer numberOfOutlets;

    private boolean startup = true;

    public DataParser(int buildingId) {

        builder = Building.newBuilder();
        builder.setId(String.valueOf(buildingId));

    }

    public Building parseData(String line) {

        String[] consumptions = line.split(COMMA);

        if (startup) {

            initialize(consumptions);
            return null;

        } else clearBuilder();


        Timestamp now = new Timestamp(currentTimeMillis());
        builder.setTimestamp(String.valueOf(now));

        for (int i = 0; i < numberOfOutlets; i++) {

            String consumption = consumptions[i + OFFSET];

            Outlet.Builder outlet = outlets.get(i);
            outlet.setConsumption(
                    consumption.equalsIgnoreCase("null") ? UNPLUGGED : consumption);

            builder.addOutlets(outlet.build());

        }

        return builder.build();

    }

    private void initialize(String[] fields) {

        numberOfOutlets = fields.length - 1;

        for (int i = 0; i < numberOfOutlets; i++)
            outlets.put(i, Outlet.newBuilder().setId(String.valueOf(i)));

        startup = false;

    }

    private void clearBuilder() {

        builder.clearOutlets();
        builder.clearTimestamp();

    }

}
