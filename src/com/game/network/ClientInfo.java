package com.game.network;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientInfo {
    private final SocketChannel channel;
    private final ByteBuffer buffer;
    private final AtomicBoolean isActive;
    private String playerId;

    public ClientInfo(SocketChannel channel) {
        this.channel = channel;
        this.buffer = ByteBuffer.allocate(1024);
        this.isActive = new AtomicBoolean(true);
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public boolean isActive() {
        return isActive.get();
    }

    public void setActive(boolean active) {
        this.isActive.set(active);
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    @Override
    public String toString() {
        return "ClientInfo{" +
                "channel=" + channel +
                ", buffer=" + buffer +
                ", isActive=" + isActive +
                ", playerId='" + playerId + '\'' +
                '}';
    }
}

