package com.rue.pomelo.kafka;

import com.rue.pomelo.kafka.process.MachineProcessor;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.Topology.AutoOffsetReset;
import org.apache.kafka.streams.processor.RecordContext;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Machine;
import protobuf.SensorValue;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static com.rue.pomelo.Pomelo.KAFKA_TOPIC_PREFIX;
import static org.apache.kafka.common.serialization.Serdes.Bytes;
import static org.apache.kafka.common.serialization.Serdes.String;

/**
 * @author Alex Kouremenos
 * */
public class PomeloClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(PomeloClient.class);

    private KafkaStreams streams;

    private final Properties properties;

    public PomeloClient(Properties properties) { this.properties = properties; }

    public void start() {

        Topology topology = new Topology();

        String topicPrefix = properties.getProperty(KAFKA_TOPIC_PREFIX);
        Pattern pattern = Pattern.compile(topicPrefix + ".*");

        topology.addSource(AutoOffsetReset.LATEST,
                "Source",
                String().deserializer(),
                Bytes().deserializer(),
                pattern);

        topology.addProcessor("Machine-Processor",
                MachineProcessor::new,
                "Source");

//        topology.addProcessor("Mean-Value-Processor",
//                new MeanValueProcessorSupplier(),
//                "Machine-Processor");

        topology.addSink("Machine-Sink",
                new MachineTopicExtractor<>(),
                String().serializer(),
                MachineSerializer.MACHINE_SERIALIZER,
                "Machine-Processor");

//        topology.addSink("Sensor-Mean-Value-Sink",
//                new MachineTopicExtractor<>(), // publishing to <machine.id>-sensor-list
//                String().serializer(),
//                SensorListSerializer.SENSORS_SERIALIZER,
//                "Mean-Value-Processor");

        streams = new KafkaStreams(topology, properties);
        streams.start();

    }

    public void shutdown() {

        if (streams != null) {

            LOGGER.info("Closing Stream Consumer...");
            streams.close();

        }

    }

    private static class MachineTopicExtractor<K, V> implements TopicNameExtractor<K, V> {

        @Override
        public String extract(K key, V value, RecordContext recordContext) {

            if (value instanceof Machine) return onMachine((Machine) value);
            else if (value instanceof List<?>) return onList(key);
            return recordContext.topic();

        }

        private String onList(K key) {
            return ((String) key).replace("/", ".") + "-sensor-list";
        }

        private String onMachine(Machine machine) { return machine.getId(); }

    }

    public static class MachineSerializer implements Serializer<Machine> {

        private static final MachineSerializer MACHINE_SERIALIZER = new MachineSerializer();

        @Override
        public byte[] serialize(String topic, Machine data) { return data.toByteArray(); }

    }

    public static class SensorListSerializer implements Serializer<List<SensorValue>> {

        private static final SensorListSerializer SENSORS_SERIALIZER = new SensorListSerializer();

        @Override
        public byte[] serialize(String topic, List<SensorValue> data) {

            if (data == null) return null;

            return Machine.newBuilder()
                    .addAllSensorValues(data)
                    .setId("sensor-list-serializer")
                    .build()
                    .toByteArray();
        }

    }

}
