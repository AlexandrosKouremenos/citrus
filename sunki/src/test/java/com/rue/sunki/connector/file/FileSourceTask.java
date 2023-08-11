package com.rue.sunki.connector.file;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Kouremenos
 * */
public class FileSourceTask extends SourceTask {

    private static final String FILENAME_FIELD = "filename";

    private static final String POSITION_FIELD = "position";

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSourceTask.class);

    private static final Schema VALUE_SCHEMA = Schema.STRING_SCHEMA;

    private BufferedReader reader = null;

    private String filePath;

    private String topic;

    private int readDelay;

    private Long fileOffset;

    public FileSourceTask() { }

    @Override
    public String version() { return  FileSourceConnector.VERSION; }

    @Override
    public void start(Map<String, String> props) {

        filePath = props.get(FileSourceConnector.FILE_PATH);
        topic = props.get(FileSourceConnector.TOPIC_CONFIG);
        readDelay = Integer.parseInt(props.get(FileSourceConnector.READ_DELAY_CONFIG));

    }

    @Override
    public List<SourceRecord> poll() {

        // TODO: Test offset handling.

        boolean eof = false;
        List<SourceRecord> records;

        if (reader == null) {

            try {

                reader = new BufferedReader(new FileReader(filePath));

                Map<String, Object> offset = context
                        .offsetStorageReader()
                        .offset(Collections.singletonMap(FILENAME_FIELD, filePath));

                if (offset != null) {

                    Object lastRecordedOffset = offset.get(POSITION_FIELD);
                    if (lastRecordedOffset != null && !(lastRecordedOffset instanceof Long))
                        throw new ConnectException("Offset position is the incorrect type");

                    if (lastRecordedOffset != null) {

                        long skipping = (Long) lastRecordedOffset;
                        while (skipping > 0) {

                            try {
                                skipping -= reader.skip(skipping);
                            } catch (IOException e) {

                                LOGGER.error("Error while trying to seek to previous offset in file {}: ", filePath, e);
                                throw new ConnectException(e);

                            }

                        }

                        LOGGER.debug("Skipped to offset [{}]", lastRecordedOffset);

                    }

                    fileOffset = lastRecordedOffset != null ? (Long) lastRecordedOffset : 0L;

                } else fileOffset = 0L;

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        try {

            String line;
//            while (!eof && reader.ready()) {

            line = reader.readLine();
            if (line == null) eof = true;
            else {

                records = new ArrayList<>();
                records.add(new SourceRecord(offsetKey(filePath),
                        offsetValue(fileOffset),
                        topic,
                        null,
                        null,
                        null,
                        VALUE_SCHEMA,
                        line,
                        System.currentTimeMillis()));
//                    Thread.sleep(readDelay);
                return records;

            }

//            }

        } catch (IOException e) {
            LOGGER.error("Failed to read.", e);
        }

        return null;
    }

    @Override
    public void stop() {

        LOGGER.info("Stopping File Source Task.");
        synchronized (this) {
            try { reader.close(); }
            catch (IOException e) { LOGGER.error("Failed to close reader.", e); }

        }

        this.notify();

    }

    private Map<String, String> offsetKey(String filename) {
        return Collections.singletonMap(FILENAME_FIELD, filename);
    }

    private Map<String, Long> offsetValue(Long pos) {
        return Collections.singletonMap(POSITION_FIELD, pos);
    }

}
