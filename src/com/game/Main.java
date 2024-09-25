package com.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.network.GameServer;
import com.game.network.TikUpdate;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {
        new GameServer(8080).start();
    }
}
