package org.alexside.like.engine;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.alexside.like.engine.db.CqlConnector;
import org.alexside.like.engine.game.GameGenerator;
import org.alexside.like.engine.module.BasicModuleTest;
import org.alexside.like.engine.service.LikeServiceTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static java.util.Collections.reverseOrder;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertTrue;

public class AppTest {
    private static final Logger log = LoggerFactory.getLogger(AppTest.class);
    private static final AtomicBoolean expired = new AtomicBoolean(false);
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private CqlConnector connector;
    private LikeServiceTest likeService;
    private GameGenerator gameGenerator;


    @Before
    public void before() {
        log.info("init test app module..");
        Injector injector = Guice.createInjector(new BasicModuleTest());
        this.connector = injector.getInstance(CqlConnector.class);
        this.likeService = injector.getInstance(LikeServiceTest.class);
        this.likeService.deleteLikes();
        this.gameGenerator = injector.getInstance(GameGenerator.class);
        expired.getAndSet(false);
    }

    @Test
    public void baseTest() {
        log.info("start base test..");
        likeService.like("1");
        long likes = likeService.getLikes("1");
        log.info("Player: {}, Likes: {}", "1", likes);
        assertTrue(likes == 1);
    }

    @Test
    public void nullTest() {
        log.info("start nill test..");
        long likes = likeService.getLikes("1");
        log.info("Player: {}, Likes: {}", "1", likes);
        assertTrue(likes == 0);
    }

    @Test
    public void loadTest() throws InterruptedException {
        log.info("start load test..");
        try {
            int players = 100; long duration = 60000;
            log.info("Players: {}, Duration: {}ms", players, duration);
            CompletableFuture.runAsync(() -> gameGenerator.start(players, likeService, expired, executor));
            Thread.sleep(duration);
            expired.compareAndSet(false, true);
            log.info("Test is over. Await task termination..");
            executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
            log.info("Test statistic:");
            IntStream.rangeClosed(1, players)
                    .mapToObj(String::valueOf)
                    .collect(toMap(s -> s, likeService::getLikes))
                    .entrySet().stream()
                    .sorted(reverseOrder(Map.Entry.comparingByValue()))
                    .forEach(e -> log.info("PlayerId: {}, Likes: {}", e.getKey(), e.getValue()));
        } finally {
            log.info("close connection..");
            connector.close();
        }
    }
}