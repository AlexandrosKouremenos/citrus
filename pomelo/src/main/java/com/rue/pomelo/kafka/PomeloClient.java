package com.rue.pomelo.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static com.rue.pomelo.Pomelo.KAFKA_TOPIC;

public class PomeloClient implements Runnable {

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final Logger logger = LoggerFactory.getLogger(PomeloClient.class);

    private final KafkaConsumer<String, byte[]> consumer;

    private final Properties properties;

    public PomeloClient(Properties properties) {

        this.properties = properties;
        this.consumer = new KafkaConsumer<>(properties);

    }

    @Override
    public void run() {

        subscribe();

        try {

            while (!closed.get()) {

                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(10));
                for (ConsumerRecord<String, byte[]> record : records) {

                    logger.info("Received machine data with: " +
                                    "topic: [{}], " +
                                    "with partition: [{}], " +
                                    "at offset: [{}] " +
                                    "key: [{}] " +
                                    "value: [{}]",
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            record.key(),
//                            Machine.parseFrom(record.value()));
                            record.value());

                }

            }

        } catch (WakeupException e) {

            // Ignore exception if closing
            if (!closed.get()) throw e;

        } finally { consumer.close(); }

    }

    private void subscribe() {

        String kafkaTopic = properties.getProperty(KAFKA_TOPIC);
        Pattern pattern = Pattern.compile(kafkaTopic + ".*");
        consumer.subscribe(pattern);

    }

    public void shutdown() {

        logger.info("Closing consumer..");
        closed.set(true);
        consumer.wakeup();

    }

}
