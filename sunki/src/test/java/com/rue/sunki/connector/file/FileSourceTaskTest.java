package com.rue.sunki.connector.file;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.apache.kafka.connect.storage.OffsetStorageReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileSourceTaskTest {

    static Map<String, String> props = new HashMap<>();
    static FileSourceTask task;
    static OffsetStorageReader offsetStorageReader;
    static SourceTaskContext context;

    public static void main(String[] args) throws FileNotFoundException {

        String filePath = ClassLoader.getSystemResource("ai4i2020-sensors-only-short.csv").getFile();

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        props.put(FileSourceConnector.FILE_PATH, filePath);
        props.put(FileSourceConnector.TOPIC_CONFIG, "test");
        props.put(FileSourceConnector.READ_DELAY_CONFIG, "1");

        offsetStorageReader = mock(OffsetStorageReader.class);
        context = mock(SourceTaskContext.class);

        expectOffsetLookupReturnNone();

        task = new FileSourceTask();
        task.initialize(context);
        task.start(props);

        long numberOfLines = reader.lines().count();

        for (int i = 0; i < numberOfLines; i++) {

            List<SourceRecord> records = task.poll();
            System.out.println(records.get(0).toString());

        }


    }

    private static void expectOffsetLookupReturnNone() {
        when(context.offsetStorageReader()).thenReturn(offsetStorageReader);
        when(offsetStorageReader.offset(anyMap())).thenReturn(null);
    }

}
