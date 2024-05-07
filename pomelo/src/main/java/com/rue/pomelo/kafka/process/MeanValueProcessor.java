package com.rue.pomelo.kafka.process;

import com.rue.protobuf.SensorTypes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Machine;
import protobuf.SensorValue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Kouremenos
 * */
public class MeanValueProcessor implements Processor<String, Machine, String, List<SensorValue>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeanValueProcessor.class);

    /**
     * Stores the mean value per sensor type for each machine.
     * */
    private WindowStore<String, HashMap<String, Float>> windowStore;

    /**
     * Stores the number of values in a window for each machine.
     * */
    private final Map<String, Long> windowCounter = new HashMap<>();

    private final String stateStoreName;

    private final int windowSize;

    public MeanValueProcessor(String stateStoreName, int windowSize) {

        this.stateStoreName = stateStoreName;
        this.windowSize = windowSize;

    }

    @Override
    public void init(ProcessorContext<String, List<SensorValue>> context) {

        context.schedule(Duration.ofMillis(windowSize),
                PunctuationType.STREAM_TIME,
                timestamp -> {

                    try (final KeyValueIterator<Windowed<String>, HashMap<String, Float>> iterator =
                                 windowStore.all()) {

                        while (iterator.hasNext()) {

                            KeyValue<Windowed<String>, HashMap<String, Float>> entry = iterator.next();

                            List<SensorValue> outputValues = new ArrayList<>();
                            for (Map.Entry<String, Float> val : entry.value.entrySet())
                                outputValues.add(newSensorValue(val.getKey(), val.getValue()));

                            windowCounter.put(entry.key.key(), (long) entry.value.size());

                            LOGGER.info("Forwarding Mean Values records.");
                            context.forward(new Record<>(entry.key.key(), outputValues, timestamp));

                        }

                    }

                });

        windowStore = context.getStateStore(stateStoreName);
        LOGGER.info("Initialization Successful.");

    }

    @Override
    public void close() {

        Processor.super.close();
        windowCounter.clear();

    }

    private static String listToString(List<SensorValue> sensorValues) {

        StringBuilder builder = new StringBuilder();
        builder.append("Sensors: " + "\n\t");
        for (SensorValue sensor : sensorValues) {

            builder.append("Type: ").append(sensor.getId())
                    .append(", Metric: ").append(sensor.getMetrics())
                    .append("\n\t");

        }
        return builder.toString();

    }

    @Override
    public void process(Record<String, Machine> record) {

        List<SensorValue> incoming = record.value().getSensorValuesList();

        long now = record.timestamp();
        long windowStart = now - windowSize;

        try (WindowStoreIterator<HashMap<String, Float>> iterator =
                     windowStore.fetch(record.key(), windowStart, now)) {

//            KeyValueIterator<Windowed<String>, HashMap<String, Float>> it = windowStore.all();
//
//            while (it.hasNext()) {
//
//                KeyValue<Windowed<String>, HashMap<String, Float>> next = it.next();
//                HashMap<String, Float> map = next.value;
//
//                for (String key : map.keySet()) {
//                    LOGGER.info("State store contains Windows: [{}], Map-Key [{}], Map-Value [{}]",
//                            next.key, key, map.get(key));
//                }
//
//            }
//            it.close();

            HashMap<String, Float> newEntry = new HashMap<>();
            if (!iterator.hasNext()) {

                LOGGER.info("Writing first entry in state store for key [{}]", record.key());

                for (SensorValue newValue : incoming) {

                    float metrics = newValue.getMetrics();
                    String id = newValue.getId();
                    if (!isExcludedSensorValue(newValue.getId())) newEntry.put(id, metrics);

                }

            } else {

                LOGGER.info("Found previous entries in state store.");
                while (iterator.hasNext()) {

                    HashMap<String, Float> prevValues = iterator.next().value;

                    for (SensorValue newValue : incoming) {

                        float metrics = newValue.getMetrics();
                        String id = newValue.getId();
                        if (!isExcludedSensorValue(id)) {

                            long windowedValuesCount = windowCounter.get(record.key());
                            float mean =
                                    computeMean(prevValues.get(id), metrics, windowedValuesCount);
                            newEntry.put(id, mean);

                        }

                    }

                }

            }

            windowStore.put(record.key(), newEntry, now);

        } catch (Exception e) { LOGGER.error("Exception while processing.", e); }

    }

    private static float computeMean(float prevValue, float newValue, long counter) {
        return (prevValue + newValue) / counter;
    }

    private static SensorValue newSensorValue(String id, Float metrics) {

        return SensorValue.newBuilder()
                .setId(id)
                .setMetrics(metrics)
                .build();

    }

    /**
     * Exclude the {@link SensorValue} that is a {@link SensorTypes}.
     * */
    private static boolean isExcludedSensorValue(String id) { return id.equals("0"); }

}
