package com.game.network;

import com.game.state.GameState;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class TikUpdate implements Runnable {

    private final MessageEncodeDecodeService messageEncodeDecodeService = new MessageEncodeDecodeService();
    private final Map<SocketChannel, ClientInfo> clients; // Поле для клиентов

    public TikUpdate(Map<SocketChannel, ClientInfo> clients) {
        this.clients = clients;
    }

    @Override
    public void run() {
        PlayersMessage playersMessage = new PlayersMessage(GameState.getInstance().getPlayers().values());

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(playersMessage);
            broadcast(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String message) {
        for (SocketChannel client : clients.keySet()) {
            ByteBuffer byteBuffer = messageEncodeDecodeService.encodeMessage(message);
            if (client.isOpen()) {
                try {
                    while (byteBuffer.hasRemaining() && client.isConnected() && client.isOpen()) {
                        client.write(byteBuffer);
                    }
                } catch (IOException e) {
                    clients.remove(client);
                    e.printStackTrace();
                }
            }
        }
    }
}
