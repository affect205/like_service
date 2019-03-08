package org.alexside.like.engine.service.impl;

import com.datastax.driver.core.ResultSet;
import org.alexside.like.engine.config.AppConfig;
import org.alexside.like.engine.db.CqlConnector;
import org.alexside.like.engine.service.LikeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

public class LikeServiceImpl implements LikeService {
    private static final Logger log = LoggerFactory.getLogger(LikeServiceImpl.class);
    private AppConfig config;
    private CqlConnector connector;

    @Inject
    public LikeServiceImpl(AppConfig config, CqlConnector connector) {
        this.config = config;
        this.connector = connector;
        connector.connect(config.getCassandraHost(), config.getCassandraPort(), config.getCassandraKeyspace());
    }

    @Override
    public void like(String playerId) {
        String query = "update store.likes set total += 1 where player_id = ?";
        connector.getSession().execute(query, playerId);
    }

    @Override
    public long getLikes(String playerId) {
        String query = "select * from store.likes where player_id = ?";
        ResultSet result = connector.getSession().execute(query, playerId);
        return Optional.ofNullable(result.one()).map(row -> row.getLong("total")).orElse(0L);
    }
}
