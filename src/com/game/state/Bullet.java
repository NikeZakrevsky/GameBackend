package com.game.state;

public class Bullet {
    private double x;
    private double y;
    private double angle;

    public Bullet(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
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

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void update() {
        // Вычисляем смещение по осям X и Y
        double dx = Math.cos(angle) * 50;
        double dy = Math.sin(angle) * 50;

        // Обновляем координаты
        this.x += dx;
        this.y += dy;
    }
}
