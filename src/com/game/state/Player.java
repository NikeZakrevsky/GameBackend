package com.game.state;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String playerId;
    private double x;
    private double y;
    private double lookDirection;
    private List<Bullet> bullets = new ArrayList<>();

    public Player(String playerId) {
        this.playerId = playerId;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public double getLookDirection() {
        return lookDirection;
    }

    public void setLookDirection(double lookDirection) {
        this.lookDirection = lookDirection;
    }
}
