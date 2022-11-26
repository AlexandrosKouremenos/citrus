package com.rue.aurantium.data;

public class DataReaderTest {

    public static void main(String[] args) {

        String filePath = DataReader.class.getClassLoader().getResource("iot-data").getFile();

        DataReader dataReader = new DataReader(filePath + "/building0-short");
        dataReader.start();

    }

}
