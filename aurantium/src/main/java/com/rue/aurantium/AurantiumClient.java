package com.rue.aurantium;

import com.rue.aurantium.data.DataReader;
import org.springframework.stereotype.Service;

@Service("aurantiumClient")
public class AurantiumClient {

    public AurantiumClient() {

        String filePath = System.getProperty("file.path");
        DataReader dataReader = new DataReader(filePath);
        dataReader.start();

    }

}
