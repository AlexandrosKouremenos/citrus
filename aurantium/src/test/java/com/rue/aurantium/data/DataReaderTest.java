package com.rue.aurantium.data;

public class DataReaderTest {

    public static void main(String[] args) {

        String filePath = DataPublisher.class.getClassLoader().getResource("iot-data").getFile();

        DataPublisher dataPublisher = new DataPublisher(filePath + "/building0-short");
        dataPublisher.start();

    }

}
