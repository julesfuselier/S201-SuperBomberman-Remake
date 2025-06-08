package com.superbomberman.model.powerup;

import java.util.Random;

public enum PowerUpType {
    BOMB_UP,
    RANGE_UP,
    SPEED_UP,
    KICK,
    GLOVE,
    REMOTE,
    WALL_PASS,
    BOMB_PASS,
    LINE_BOMB,
    SKULL;

    private static final Random RANDOM = new Random();

    public static PowerUpType randomType() {
        PowerUpType[] values = values();
        return values[RANDOM.nextInt(values.length)];
    }
}


