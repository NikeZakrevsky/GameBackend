package com.game.state;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    // Use ConcurrentHashMap for thread-safe access to players
    private Map<String, Player> players = new ConcurrentHashMap<>();

    // Private constructor to prevent external instantiation
    private GameState() {}

    // Static inner class to hold the singleton instance of GameState
    private static class GameStateHolder {
        private static final GameState INSTANCE = new GameState();
    }

    // Public method to access the single instance of GameState
    public static GameState getInstance() {
        return GameStateHolder.INSTANCE;
    }

    // Add a player to the game state
    public String addPlayer(String playerId) {
        players.put(playerId, new Player(playerId));

        return playerId;
    }

    // Get a player from the game state
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    // Remove a player from the game state
    public void removePlayer(String playerId) {
        players.remove(playerId);
    }

    // Modify player (e.g. update position)
    public void updatePlayer(String playerId, Player newPlayerData) {
        players.put(playerId, newPlayerData);
    }

    // Get the current state of all players
    public Map<String, Player> getPlayers() {
        return players;
    }
}
