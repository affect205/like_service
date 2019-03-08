package org.alexside.like.engine.game;

import org.alexside.like.engine.service.LikeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class GameTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(GameTask.class);
    private UUID gameId;
    private String[] teamOne;
    private String[] teamTwo;
    private LikeService likeService;
    private Consumer<GameResult> onResult;
    private long duration;
    private Map<String, Integer> statistic;

    public GameTask(UUID gameId, String[] teamOne, String[] teamTwo, LikeService likeService,
                    long duration, Consumer<GameResult> onResult) {
        this.gameId = gameId;
        this.teamOne = teamOne;
        this.teamTwo = teamTwo;
        this.likeService = likeService;
        this.onResult = onResult;
        this.duration = duration;
        this.statistic = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        log.info("Game started...");
        GameResult result = null;
        try {
            long elapsed = 0L;
            while (elapsed < duration) {// give a like for every frag
                for (String playerId : teamOne) {
                    int frags = getFrags(teamTwo.length);
                    IntStream.range(0, frags).forEach(i -> likeService.like(playerId));
                    statistic.merge(playerId, frags, Integer::sum);
                }
                for (String playerId : teamTwo) {
                    int frags = getFrags(teamOne.length);
                    IntStream.range(0, frags).forEach(i -> likeService.like(playerId));
                    statistic.merge(playerId, frags, Integer::sum);
                }
                elapsed += 500;
                Thread.sleep(500);
            }
            result = new GameResult(gameId, teamOne, teamTwo, getStatus(), statistic);
        } catch (Throwable ex) {
            log.error("Error occurred during the game ", ex);
            result = new GameResult(gameId, teamOne, teamTwo, GameStatus.ERROR, statistic);
        } finally {
            onResult.accept(result);
        }
    }

    public static class GameResult {
        private final UUID gameId;
        private final String[] teamOne;
        private final String[] teamTwo;
        private final GameStatus status;
        private final Map<String, Integer> statistic;
        public GameResult(UUID gameId, String[] teamOne, String[] teamTwo, GameStatus status,
                          Map<String, Integer> statistic) {
            this.gameId = gameId;
            this.teamOne = teamOne;
            this.teamTwo = teamTwo;
            this.status = status;
            this.statistic = statistic;
        }
        public UUID getGameId() { return gameId; }
        public String[] getTeamOne() { return teamOne; }
        public String[] getTeamTwo() { return teamTwo; }
        public GameStatus getStatus() { return status; }
        public Map<String, Integer> getStatistic() { return statistic; }
    }

    enum GameStatus {
        ONE, TWO, DRAW, ERROR;
    }

    private GameStatus getStatus() {
        return GameStatus.values()[ThreadLocalRandom.current().nextInt(0, 3)];
    }

    private int getFrags(int n) {
        return ThreadLocalRandom.current().nextInt(0, n+1);
    }
}