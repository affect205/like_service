package org.alexside.like.engine.service.impl;

import org.alexside.like.engine.service.LikeServiceTest;
import org.alexside.like.engine.db.CqlConnector;
import org.alexside.like.engine.service.LikeService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;

public class LikeServiceTestImpl implements LikeServiceTest {
    @Inject
    private CqlConnector connector;

    private LikeService likeService;


    @Inject
    public LikeServiceTestImpl(LikeService likeService) {
        this.likeService = likeService;
    }

    @Override
    public void deleteLikes(Collection<String> players) {
        if (players.isEmpty()) {
            connector.getSession().execute("truncate store.likes;");
            return;
        }
        String query = "delete from store.likes where player_id in(?);";
        connector.getSession().execute(query, players);
    }

    @Override
    public void deleteLikes() {
        deleteLikes(Collections.emptyList());
    }

    @Override
    public void like(String playerId) {
        likeService.like(playerId);
    }

    @Override
    public long getLikes(String playerId) {
        return likeService.getLikes(playerId);
    }
}