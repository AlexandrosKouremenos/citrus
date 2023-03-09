package com.rue.sunki.connector.file;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSourceConnector extends SourceConnector {

    public static final String VERSION = "test-version";

    private static final String CONFIG_EXCEPTION = "'topic' in FileStreamSourceConnector configuration requires definition of a single topic";

    public static final String TOPIC_CONFIG = "test-topic";

    public static final String READ_DELAY_CONFIG = "delay";

    public static final String FILE_PATH = "file-path";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(FILE_PATH, Type.STRING, Importance.HIGH, "File path")
            .define(TOPIC_CONFIG, Type.LIST, Importance.HIGH, "Topic")
            .define(READ_DELAY_CONFIG, Type.INT, 1, Importance.MEDIUM, "Read delay");

    private String filePath;

    private String topic;

    private int readDelay;

    @Override
    public void start(Map<String, String> props) {

        AbstractConfig parsedConfig = new AbstractConfig(CONFIG_DEF, props);
        filePath = parsedConfig.getString(FILE_PATH);
        readDelay = parsedConfig.getInt(READ_DELAY_CONFIG);

        List<String> topics = parsedConfig.getList(TOPIC_CONFIG);
        if (topics.size() != 1) throw new ConfigException(CONFIG_EXCEPTION);

        topic = topics.get(0);

    }

    @Override
    public Class<? extends Task> taskClass() { return FileSourceTask.class; }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {

        ArrayList<Map<String, String>> configs = new ArrayList<>();
        Map<String, String> conf = new HashMap<>();

        if (filePath != null) conf.put(FILE_PATH, filePath);
        conf.put(TOPIC_CONFIG, topic);
        conf.put(READ_DELAY_CONFIG, String.valueOf(readDelay));

        configs.add(conf);
        return configs;

    }

    @Override
    public void stop() { /* Do nothing */ }

    @Override
    public ConfigDef config() { return CONFIG_DEF; }

    @Override
    public String version() { return VERSION; }
}
