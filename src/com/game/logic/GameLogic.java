package com.game.logic;

import com.game.state.GameState;
import com.game.state.Player;

import java.nio.channels.SocketChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameLogic {

    private final GameState gameState = GameState.getInstance();

    public String handleMessage(String message, SocketChannel client) {
        //System.out.println("Received message: " + message);

        Pattern movePattern = Pattern.compile("MOVE;id:([\\w-]+);x:(\\d+),y:(\\d+)");
        Pattern newPlayerPattern = Pattern.compile("NEW_PLAYER;id:([\\w-]+)");

        Matcher moveMatcher = movePattern.matcher(message.trim());
        Matcher newPlayerMatcher = newPlayerPattern.matcher(message.trim());

        // Обработка команды MOVE
        if (moveMatcher.matches()) {
            String playerId = moveMatcher.group(1);
            int x = Integer.parseInt(moveMatcher.group(2));
            int y = Integer.parseInt(moveMatcher.group(3));

            Player player = gameState.getPlayer(playerId);
            if (player != null) {
                player.setX(x);
                player.setY(y);
                gameState.updatePlayer(playerId, player);
                //System.out.println("Player " + playerId + " moved to x: " + x + ", y: " + y);
            } else {
                System.out.println("Player not found: " + playerId);
            }
        }
        // Обработка команды NEW_PLAYER
        else if (newPlayerMatcher.matches()) {
            String playerId = newPlayerMatcher.group(1);
            return gameState.addPlayer(playerId);
        }
        else {
            // Сообщение не соответствует ни MOVE, ни NEW_PLAYER
            System.out.println("No match found for the message: " + message);
        }

        return null;
    }

    public void removePlayer(String playerId) {
        gameState.removePlayer(playerId);
    }
}
