package com.rue.sunki.kafka.connector;


import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.rue.sunki.kafka.connector.util.MqttSourceConnectorConfig.Version;
import static com.rue.sunki.kafka.connector.util.MqttSourceConnectorConfig.configDef;

/**
 * @author Alex Kouremenos
 * */
public class MqttSourceConnector extends SourceConnector {

    public static final Logger LOGGER = LoggerFactory.getLogger(MqttSourceConnector.class);

    private Map<String, String> configProps;

    @Override
    public void start(Map<String, String> props) {

        LOGGER.info("Starting the MQTT Source Connector.");
        configProps = Collections.unmodifiableMap(props);

    }

    @Override
    public Class<? extends Task> taskClass() { return MqttSourceTask.class; }

    // TODO: Find how to actually utilize multiple tasks.
    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {

        List<Map<String, String>> configs = new ArrayList<>();
        for (int i = 0; i < maxTasks; i++) {

            Map<String, String> taskProps = new HashMap<>(configProps);
            configs.add(taskProps);

        }

        return configs;

    }

    @Override
    public void stop() { LOGGER.info("Stopping the MQTT Source Connector."); }

    @Override
    public ConfigDef config() { return configDef(); }

    @Override
    public String version() { return Version.getVersion(); }

}
