package com.rue.pomelo.kafka.process;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowStore;
import protobuf.Machine;
import protobuf.SensorValue;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.rue.pomelo.kafka.serdes.HashMapSerde.HashMap;
import static org.apache.kafka.common.serialization.Serdes.String;

public class MeanValueProcessorSupplier implements ProcessorSupplier<String, Machine, String, List<SensorValue>> {

    private static final String MEAN_VALUE_STORE = "Mean-Value-Store";

    private static final int WINDOW_SIZE_MS = 5000;

    private static final int GRACE_PERIOD_MS = 500;

    @Override
    public Processor<String, Machine, String, List<SensorValue>> get() {
        return new MeanValueProcessor(MEAN_VALUE_STORE, WINDOW_SIZE_MS);
    }

    @Override
    public Set<StoreBuilder<?>> stores() {

        StoreBuilder<WindowStore<String, HashMap<String, Float>>> storeBuilder = Stores.windowStoreBuilder(
                Stores.persistentWindowStore(MEAN_VALUE_STORE,
                        Duration.ofMillis(WINDOW_SIZE_MS + GRACE_PERIOD_MS),
                        Duration.ofMillis(WINDOW_SIZE_MS),
                        false),
                String(),
                HashMap());

        return Collections.singleton(storeBuilder);

    }

}
