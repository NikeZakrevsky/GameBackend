package com.game.logic;

import com.game.state.GameState;
import com.game.state.Player;

import java.nio.channels.SocketChannel;

public class GameLogic {

    private final GameState gameState = GameState.getInstance();

    public String handleMessage(String message, SocketChannel client) {
        System.out.println(message);
        if (message.startsWith("MOVE")) {
            String playerId = message.substring(8,21);

            Player player = gameState.getPlayer(playerId);
            player.setX(Integer.parseInt(message.substring(24, 27)));
            player.setY(Integer.parseInt(message.substring(30, 33)));

            gameState.updatePlayer(playerId, player);
        }

        if (message.startsWith("NEW_PLAYER")) {
            return gameState.addPlayer(message.substring(14));
        }

        return null;
    }
}
