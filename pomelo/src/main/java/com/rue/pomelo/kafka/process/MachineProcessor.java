package com.rue.pomelo.kafka.process;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Machine;
import protobuf.SensorValue;

import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Float.*;

public class MachineProcessor implements Processor<String, Bytes, String, Machine> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MachineProcessor.class);

    private static final Throwable ERROR_VALUE = new Throwable("Sensor value was out of range");

    private ProcessorContext<String, Machine> context;

    @Override
    public void init(ProcessorContext<String, Machine> context) {

        Processor.super.init(context);
        this.context = context;

    }

    @Override
    public void process(Record<String, Bytes> record) {

        Machine machine;
        try {

            machine = Machine.parseFrom(record.value().get());
            LOGGER.info("Successfully parsed incoming machine data for [{}].", machine.getId());

        }
        catch (InvalidProtocolBufferException e) {

            LOGGER.error("Error while parsing machine.", e);
            throw new RuntimeException(e);

        }

        try {

            Optional<Machine> outputValue = Stream.of(machine)
                    .filter(m -> m.getSensorValuesList()
                            .stream()
                            .noneMatch(MachineProcessor::outOfRange))
                    .findFirst();

            if (outputValue.isEmpty()) throw ERROR_VALUE;

            Record<String, Machine> outRecord = new Record<>(record.key(),
                    outputValue.get(),
                    record.timestamp());

            context.forward(outRecord);

        } catch (Throwable e) {

            LOGGER.error("Record with key: [{}] has an out of range sensor value.", record.key());
            throw new RuntimeException(e);

        }

    }

    private static boolean outOfRange(SensorValue sensor) {

        return sensor == null ||
                isNaN(sensor.getMetrics()) ||
                sensor.getMetrics() == MAX_VALUE ||
                isInfinite(sensor.getMetrics());

    }

}
