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
    private final Map<SocketChannel, ByteBuffer> clientBuffers = new ConcurrentHashMap<>(); // Буферы для чтения от каждого клиента
    private final GameLogic gameLogic;
    private final Set<SocketChannel> clients = ConcurrentHashMap.newKeySet(); // Потокобезопасное множество клиентов
    private final MessageEncodeDecodeService messageEncodeDecodeService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Map<String, SocketChannel> playerToClientMap = new ConcurrentHashMap<>();
    private final Map<SocketChannel, String> clientToPlayerMap = new ConcurrentHashMap<>();


    public GameServer(int port) throws IOException {
        bossSelector = Selector.open();
        workerSelector = Selector.open();
        gameLogic = new GameLogic();
        messageEncodeDecodeService = new MessageEncodeDecodeService();

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.register(bossSelector, SelectionKey.OP_ACCEPT);

        workerPool = Executors.newFixedThreadPool(10); // Количество потоков для обработки сообщений

        System.out.println("Game Server started on port " + port);
    }

    public void start() {
        new Thread(this::acceptConnections).start();
        new Thread(this::processWorkerEvents).start();
        startTikUpdate();
    }

    private void startTikUpdate() {
        scheduler.scheduleAtFixedRate(new TikUpdate(playerToClientMap), 0, 16, TimeUnit.MILLISECONDS);
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
                            SocketChannel client = serverSocketChannel.accept();
                            client.configureBlocking(false);

                            String request = readHttpRequest(client);
                            handleHandshake(client, request);

                            // Регистрируем клиента для чтения
                            client.register(workerSelector, SelectionKey.OP_READ);
                            workerSelector.wakeup();
                            clientBuffers.put(client, ByteBuffer.allocate(1024)); // Выделяем буфер для каждого клиента

                            System.out.println("Client connected: " + client.getRemoteAddress());
                        }
                        iterator.remove();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Поток для обработки чтения данных с клиентов
    private void processWorkerEvents() {
        while (true) {
            try {
                if (workerSelector.select() > 0) {

                    Set<SelectionKey> selectedKeys = workerSelector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        if (key.isReadable()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            ByteBuffer buffer = clientBuffers.get(client);
                            if (buffer != null) {
                                try {
                                    int bytesRead = client.read(buffer);
                                    if (bytesRead == -1) {
                                        clientBuffers.remove(client);
                                        String playerId = clientToPlayerMap.get(client);
                                        if (playerId != null) {
                                            SocketChannel removedClient = playerToClientMap.remove(playerId);
                                            removedClient.close();
                                            clientToPlayerMap.remove(client);
                                            client.close();
                                            gameLogic.removePlayer(playerId);
                                        }
                                        continue;
                                    }

                                    // Обработка сообщения от клиента
                                    buffer.flip();
                                    String message = messageEncodeDecodeService.decodeMessage(buffer);
                                    buffer.clear();

                                    //workerPool.submit(() -> gameLogic.handleMessage(message, client)); // Обработка в отдельном потоке
                                    String playerId = gameLogic.handleMessage(message, client);
                                    if (playerId != null) {
                                        playerToClientMap.put(playerId, client);
                                        clientToPlayerMap.put(client, playerId);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        iterator.remove();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleHandshake(SocketChannel client, String request) throws IOException {
        String webSocketKey = extractWebSocketKey(request);
        String acceptKey = generateAcceptKey(webSocketKey);

        String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";

        client.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
    }

    private String extractWebSocketKey(String request) {
        String[] headers = request.split("\r\n");
        for (String header : headers) {
            if (header.startsWith("Sec-WebSocket-Key:")) {
                return header.substring(19).trim();
            }
        }
        return null;
    }

    private String generateAcceptKey(String key) {
        String combined = key + MAGIC_NUMBER;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String readHttpRequest(SocketChannel client) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        StringBuilder request = new StringBuilder();
        int bytesRead;

        while ((bytesRead = client.read(buffer)) > 0) {
            request.append(new String(buffer.array(), 0, bytesRead));
            buffer.clear();
            if (request.toString().contains("\r\n\r\n")) {
                break;
            }
        }
        return request.toString();
    }
}
