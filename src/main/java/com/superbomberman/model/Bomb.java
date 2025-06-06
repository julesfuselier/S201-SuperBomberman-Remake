package com.superbomberman.model;



import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Bomb {
    private int x, y;
    private int previousX, previousY;
    private int damage;
    private Timeline timer;
    private int range;
    private boolean exploded;

    public Bomb(int x, int y, int damage, int range) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
        this.damage = damage;
        this.range = range;
    }

    public void startCountdown(Runnable onExplode) {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            exploded = true;
            onExplode.run();  // Cette ligne exécute le code qu'on lui a passé
        }));
        timer.setCycleCount(1);
        timer.play();
    }


    public int getX() { return x; }
    public int getY() { return y; }
    public int getDamage() { return damage; }
    public int getRange() { return range; }
}


