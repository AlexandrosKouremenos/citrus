package com.rue.aurantium.data;

import protobuf.Machine;

import java.util.Objects;

public class DataReaderTest {

    public static void main(String[] args) {

        String filePath = Objects.requireNonNull(DataParser.class
                        .getClassLoader()
                        .getResource("iot-data"))
                .getFile();

        Parser parser = new Parser(filePath + "/machine0-short");
        parser.start();

    }

    private static final class Parser extends DataPublisher {

        public Parser(String filePath) { super(filePath); }

        @Override
        protected void start() { super.start(); }

        @Override
        protected void publish(Machine machine) { System.out.println(machine.toString()); }

        @Override
        protected void shutdown() { System.out.println("Exit."); }

    }

}
