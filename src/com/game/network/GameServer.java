package com.game.network;

import com.game.logic.GameLogic;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.Base64;

public class GameServer {
    private static final String MAGIC_NUMBER = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private final Selector bossSelector;
    private final Selector workerSelector;
    private final ServerSocketChannel serverSocketChannel;
    private final ExecutorService workerPool;
    private final GameLogic gameLogic;
    private final MessageEncodeDecodeService messageEncodeDecodeService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<SocketChannel, ClientInfo> clientInfoMap = new ConcurrentHashMap<>();

    public GameServer(int port) throws IOException {
        bossSelector = Selector.open();
        workerSelector = Selector.open();
        gameLogic = new GameLogic();
        messageEncodeDecodeService = new MessageEncodeDecodeService();

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.register(bossSelector, SelectionKey.OP_ACCEPT);

        workerPool = Executors.newFixedThreadPool(10);

        System.out.println("Game Server started on port " + port);
    }

    public void start() {
        new Thread(this::acceptConnections).start();
        new Thread(this::processWorkerEvents).start();
        startTikUpdate();
    }

    private void startTikUpdate() {
        scheduler.scheduleAtFixedRate(new TikUpdate(clientInfoMap), 0, 16, TimeUnit.MILLISECONDS);
    }

    // Поток для принятия соединений
    private void acceptConnections() {
        while (true) {
            try {
                if (bossSelector.select() > 0) {
                    Set<SelectionKey> selectedKeys = bossSelector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        if (key.isAcceptable()) {
                            SocketChannel channel = serverSocketChannel.accept();
                            if (channel != null) {
                                channel.configureBlocking(false);
                                channel.register(workerSelector, SelectionKey.OP_READ);
                                workerSelector.wakeup();
                            }
                        }
                        iterator.remove();
                    }
                }
            } catch (IOException e) {
                System.out.println("Error in accepting connections:");
                e.printStackTrace();
            }
        }
    }

    private void processWorkerEvents() {
        while (true) {
            try {
                if (workerSelector.select() > 0) {
                    Set<SelectionKey> selectedKeys = workerSelector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(2048);

                            int count = channel.read(buffer);
                            buffer.flip();

                            String string = new String(buffer.array());
                            if (string.startsWith("GET / HTTP/1.1")) {
                                int swkIndex = string.indexOf("Sec-WebSocket-Key:");
                                int endIndex = string.indexOf("\r\n", swkIndex);
                                String swk = string.substring(swkIndex + 19, endIndex);

                                String acceptKey = generateAcceptKey(swk);

                                String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                                        "Upgrade: websocket\r\n" +
                                        "Connection: Upgrade\r\n" +
                                        "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
                                channel.write(ByteBuffer.wrap(response.getBytes()));

                                ClientInfo clientInfo = new ClientInfo(channel);
                                clientInfoMap.put(channel, clientInfo);
                            } else {
                                if (count == -1) {
                                    handleClientDisconnect(channel);
                                } else {
                                    try {
                                        String message = messageEncodeDecodeService.decodeMessage(buffer);
                                        System.out.println(message);
                                        ClientInfo clientInfo = clientInfoMap.get(channel);
                                        gameLogic.handleMessage(message, channel, clientInfo);
                                    } catch (Exception e) {
                                        System.out.println("Can not decode");
                                    }
                                }
                            }
                            iterator.remove();
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error in processing worker events:");
                e.printStackTrace();
            }
        }
    }

    private void handleClientDisconnect(SocketChannel client) {
        try {
            if (client != null && client.isOpen()) {
                System.out.println("Client disconnect: " + client.getRemoteAddress());
                ClientInfo clientInfo = clientInfoMap.remove(client);
                if (clientInfo != null && clientInfo.getPlayerId() != null) {
                    gameLogic.removePlayer(clientInfo.getPlayerId());
                }
                client.close(); // Закрываем канал
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateAcceptKey(String key) {
        String combined = key + MAGIC_NUMBER;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error generating WebSocket accept key", e);
        }
    }
}