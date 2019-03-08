package org.alexside.like.engine.game;

import org.alexside.like.engine.service.LikeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.Collectors.toConcurrentMap;

public class GameGenerator {
    private static final Logger log = LoggerFactory.getLogger(GameGenerator.class);
    private static final int TEAM_SIZE = 5;
    private Map<String, Boolean> playersTable = new ConcurrentHashMap<>();
    private Lock teamLock = new ReentrantLock();
    private Condition teamCond = teamLock.newCondition();
    private long duration = 10000L;
    public void start(int players, LikeService likeService, AtomicBoolean expired, ExecutorService executor) {
        playersTable = getPlayersTable(players);
        while (!expired.get()) {
            UUID gameId = randomUUID();
            try {
                log.info("Prepare for game {}..", gameId);
                String[] teamOne = searchTeam();
                if (teamOne.length < TEAM_SIZE) {
                    log.info("Could not search a teamOne. Game cancel..");
                    continue;
                }
                log.info("TeamOne is {}", Arrays.toString(teamOne));
                String[] teamTwo = searchTeam();
                if (teamTwo.length < TEAM_SIZE) {
                    log.info("Could not search a teamTwo. Game cancel..");
                    continue;
                }
                log.info("TeamTwo is {}", Arrays.toString(teamTwo));
                runAsync(new GameTask(gameId, teamOne, teamTwo, likeService, duration, this::onResult), executor);
                Thread.sleep(500L);
            } catch (InterruptedException ex) {
                log.error("Interruption occurred during game creating {}", ex);
            }
        }
    }

    private Map<String, Boolean> getPlayersTable(int players) {
        return IntStream.rangeClosed(1, players).boxed().collect(toConcurrentMap(String::valueOf, id -> true));
    }

    private String[] searchTeam() {
        log.info("Search team..");
        teamLock.lock();
        try {
            while (getAvailablePlayers() < TEAM_SIZE) {
                log.info("There are too few players. Await available..");
                teamCond.await(5, TimeUnit.SECONDS);
            }
            String[] team = shuffle(playersTable
                    .entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()))
                    .subList(0, TEAM_SIZE)
                    .toArray(new String[TEAM_SIZE]);
            refreshPlayersTable(team, false);
            return team;
        } catch (InterruptedException ex) {
            log.error("Interruption occurred during team searching {}", ex);
            return new String[]{};
        } finally {
            teamLock.unlock();
        }
    }

    private List<String> shuffle(List<String> list) {
        Collections.shuffle(list);
        return list;
    }

    private void onResult(GameTask.GameResult result) {
        log.info("Game finished..");
        log.info("GameId: {}, TeamOne: {}, TeamTwo: {}, Status: {}", result.getGameId(),
                Arrays.toString(result.getTeamOne()), Arrays.toString(result.getTeamTwo()), result.getStatus());
        log.info("Game statistic:");
        result.getStatistic().forEach((playerId, frags) -> log.info("PlayerId: {}, Frags: {}", playerId, frags));
        refreshPlayersTable(result.getTeamOne(), true);
        refreshPlayersTable(result.getTeamTwo(), true);
    }

    private long getAvailablePlayers() {
        long availablePlayers = playersTable.entrySet().stream().filter(Map.Entry::getValue).count();
        log.info("AvailablePlayers: {}", availablePlayers);
        return availablePlayers;
    }

    private void refreshPlayersTable(String[] team, boolean available) {
        log.info("Refresh players table. Team: {}, Available: {}", Arrays.toString(team), available);
        try {
            Stream.of(team).forEach(playerId -> playersTable.replace(playerId, !available, available));
        } finally {
            if (getAvailablePlayers() >= TEAM_SIZE) teamCond.signalAll();
        }
    }
}