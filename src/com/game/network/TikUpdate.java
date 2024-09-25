package com.game.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class TikUpdate implements Runnable {

    private final MessageEncodeDecodeService messageEncodeDecodeService = new MessageEncodeDecodeService();
    private final Map<String, SocketChannel> clients; // Поле для клиентов

    public TikUpdate(Map<String, SocketChannel> clients) {
        this.clients = clients; // Передаем клиентов через конструктор
    }

    @Override
    public void run() {
        broadcast("qwe");
    }

    private void broadcast(String message) {
        ByteBuffer byteBuffer = messageEncodeDecodeService.encodeMessage(message);
        for (SocketChannel client : clients.values()) {
            if (client.isOpen()) {
                try {
                    // Отправляем данные в канал
                    while (byteBuffer.hasRemaining()) {
                        client.write(byteBuffer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
