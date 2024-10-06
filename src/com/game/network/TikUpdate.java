package com.game.network;

import com.game.state.GameState;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TikUpdate implements Runnable {

    private final GameState gameState = GameState.getInstance();

    @Override
    public void run() {
        gameState.updateState();
        PlayersMessage playersMessage = new PlayersMessage(GameState.getInstance().getPlayers().values());

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(playersMessage);
            MessagingService.broadcast(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
