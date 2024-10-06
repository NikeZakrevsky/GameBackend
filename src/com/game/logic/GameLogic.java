package com.game.logic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.network.ClientInfo;
import com.game.network.GameServer;
import com.game.network.MapMessage;
import com.game.network.MessagingService;
import com.game.state.Bullet;
import com.game.state.GameState;
import com.game.state.Player;

import java.util.Optional;

public class GameLogic {
    private final GameState gameState = GameState.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void handleMessage(String message, ClientInfo clientInfo) {
        JsonNode jsonMessage = parseMessage(message);

        switch (jsonMessage.get("event").asText()) {
            case "SHOOT":
                processPlayerShoot(jsonMessage);
                break;
            case "NEW_PLAYER":
                processNewPlayer(jsonMessage, clientInfo);
                break;
            case "MOVE":
                processPlayerMovement(jsonMessage);
                break;
            default:
                System.out.println("Can not parse the message");
        }
    }

    public void removePlayer(String playerId) {
        gameState.removePlayer(playerId);
    }

    private JsonNode parseMessage(String message) {
        try {
            return objectMapper.readTree(message);
        } catch (JsonProcessingException e) {
            return objectMapper.createObjectNode();
        }
    }

    private void processPlayerMovement(JsonNode message) {
        String playerId = message.get("playerId").asText();
        double x = message.get("position").get("x").asDouble();
        double y = message.get("position").get("y").asDouble();
        double lookDirection = message.get("lookDirection").asDouble();

        Player player = gameState.getPlayer(playerId);
        player.setX(x);
        player.setY(y);
        player.setLookDirection(lookDirection);

        gameState.updatePlayer(playerId, player);
    }

    private void processPlayerShoot(JsonNode message) {
        String playerId = message.get("playerId").asText();
        double x = message.get("position").get("x").asDouble();
        double y = message.get("position").get("y").asDouble();
        double angle = message.get("angle").asDouble();

        Bullet bullet = new Bullet(x, y, angle);
        Player player = gameState.getPlayer(playerId);

        gameState.addBullet(player, bullet);
    }

    private void processNewPlayer(JsonNode message, ClientInfo clientInfo) {
        String playerId = message.get("playerId").asText();

        gameState.addPlayer(playerId);

        sendMap(clientInfo);
    }

    private void sendMap(ClientInfo clientInfo) {
        MapMessage mapMessage = new MapMessage(gameState.getTrees());
        try {
            String message = objectMapper.writeValueAsString(mapMessage);

            MessagingService.sendMessage(clientInfo.getChannel(), message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
