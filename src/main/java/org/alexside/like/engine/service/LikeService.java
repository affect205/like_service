package org.alexside.like.engine.service;

public interface LikeService {
    void like(String playerId);
    long getLikes(String playerId);
}
