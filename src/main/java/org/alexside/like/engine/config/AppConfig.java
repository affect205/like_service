package org.alexside.like.engine.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private Configuration config;
    public AppConfig() {
        Configurations configs = new Configurations();
        try {
            config = configs.properties(new File("application.properties"));
        } catch (ConfigurationException ex) {
            log.error("Error occurred during application.properties reading: ", ex);
        }
    }
    public Integer getCassandraPort() {
        return config.getInteger("cassandra.port", null);
    }
    public String getCassandraHost() { return config.getString("cassandra.host", null); }
    public String getCassandraKeyspace() { return config.getString("cassandra.keyspace", null); }
}
