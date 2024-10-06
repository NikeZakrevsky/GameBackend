package com.game.network;

import com.game.state.GameState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class MessagingService {

    private static final MessageEncodeDecodeService messageEncodeDecodeService = new MessageEncodeDecodeService();

    public static void sendMessage(SocketChannel client, String message) {
        ByteBuffer byteBuffer = messageEncodeDecodeService.encodeMessage(message);
        if (client.isOpen()) {
            try {
                while (byteBuffer.hasRemaining() && client.isConnected() && client.isOpen()) {
                    client.write(byteBuffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void broadcast(String message) {
        Map<SocketChannel, ClientInfo> clients = GameServer.clientInfoMap;
        for (SocketChannel client : clients.keySet()) {
            sendMessage(client, message);
        }
    }
}
