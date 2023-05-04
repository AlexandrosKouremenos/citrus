package com.rue.pomelo.kafka.process;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Machine;
import protobuf.SensorValue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MeanValueProcessor implements Processor<String, Machine, String, List<SensorValue>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeanValueProcessor.class);

    private ProcessorContext<String, List<SensorValue>> context;

    private WindowStore<String, HashMap<String, Float>> windowStore;

    private final AtomicLong counter = new AtomicLong(0);

    private final String stateStoreName;

    private final int windowSize;

    public MeanValueProcessor(String stateStoreName, int windowSize) {

        this.stateStoreName = stateStoreName;
        this.windowSize = windowSize;

    }

    @Override
    public void init(ProcessorContext<String, List<SensorValue>> context) {

        this.context = context;
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

                            LOGGER.info("Forwarding Mean Values records.");
                            context.forward(new Record<>(entry.key.key(), outputValues, timestamp));

                        }

                    }

                });

        windowStore = context.getStateStore(stateStoreName);
        LOGGER.info("Initialization Successful.");

    }

    @Override
    public void process(Record<String, Machine> record) {

        List<SensorValue> incoming = record.value().getSensorValuesList();

        long now = record.timestamp();
        long windowStart = now - windowSize;

        try (KeyValueIterator<Windowed<String>, HashMap<String, Float>> iterator =
                     windowStore.all()) {

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

            if (!iterator.hasNext()) {

                LOGGER.info("Writing first entry in state store for key [{}]", record.key());

                HashMap<String, Float> initEntry = incoming.stream()
                        .collect(Collectors.toMap(SensorValue::getId,
                                SensorValue::getMetrics,
                                (a, b) -> b,
                                HashMap::new));

                windowStore.put(record.key(), initEntry, windowStart);
                counter.incrementAndGet();
                return;

            }

            LOGGER.info("Found previous entries in state store.");
            HashMap<String, Float> newEntry = new HashMap<>();
            while (iterator.hasNext()) {

                HashMap<String, Float> inWindowValues = iterator.next().value;

                for (SensorValue newValue : incoming) {

                    String id = newValue.getId();
                    newEntry.put(id, computeMean(inWindowValues.get(id), newValue.getMetrics()));

                }

            }

            windowStore.put(record.key(), newEntry, windowStart);

        } catch (Exception e) { LOGGER.error("Exception while processing.", e); }

    }

    private float computeMean(float prevValue, float newValue) {
        return (prevValue + newValue) / counter.incrementAndGet();
    }

    private static SensorValue newSensorValue(String id, Float metrics) {

        return SensorValue.newBuilder()
                .setId(id)
                .setMetrics(metrics)
                .build();

    }

}
