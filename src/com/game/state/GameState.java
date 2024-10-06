package com.game.state;

import java.util.*;

import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    // Use ConcurrentHashMap for thread-safe access to players
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final List<Tree> trees = new ArrayList<>();

    private final Random random = new Random();

    // Private constructor to prevent external instantiation
    private GameState() {
        generateRandomTrees(50);
    }

    private void generateRandomTrees(int count) {
        for (int i = 0; i < count; i++) {
            double x = random.nextDouble() * 2000;
            double y = random.nextDouble() * 2000;
            trees.add(new Tree(x, y));
        }
    }

    public void addBullet(Player player, Bullet bullet) {
        player.getBullets().add(bullet);
    }

    public void updateState() {
        for (Player player : players.values()) {
            Iterator<Bullet> bulletIterator = player.getBullets().iterator();

            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                bullet.update();

                if (bullet.getX() < 0 || bullet.getX() > 2000 || bullet.getY() < 0 || bullet.getY() > 2000) {
                    System.out.println("Remove " + bullet);
                    bulletIterator.remove();
                }
            }
        }
    }


    // Static inner class to hold the singleton instance of GameState
    private static class GameStateHolder {
        private static final GameState INSTANCE = new GameState();
    }

    // Public method to access the single instance of GameState
    public static GameState getInstance() {
        return GameStateHolder.INSTANCE;
    }

    public void addPlayer(String playerId) {
        players.put(playerId, new Player(playerId));
    }

    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
    }

    public void updatePlayer(String playerId, Player newPlayerData) {
        players.put(playerId, newPlayerData);
    }

    public Map<String, Player> getPlayers() {
        return players;
    }

    public List<Tree> getTrees() {
        return trees;
    }
}
