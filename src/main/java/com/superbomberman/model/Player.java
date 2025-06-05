package com.superbomberman.model;

public class Player {
    private int x, y;
    private int previousX, previousY;

    public void setPosition(int x, int y) {
        this.previousX = this.x;
        this.previousY = this.y;
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public int getPreviousX() { return previousX; }
    public int getPreviousY() { return previousY; }

}
