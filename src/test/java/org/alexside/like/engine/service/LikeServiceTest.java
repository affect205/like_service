package org.alexside.like.engine.service;

import java.util.Collection;

public interface LikeServiceTest extends LikeService {
    void deleteLikes();
    void deleteLikes(Collection<String> players);
}
