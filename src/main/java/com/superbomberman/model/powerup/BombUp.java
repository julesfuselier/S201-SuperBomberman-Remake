package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

public class BombUp extends PowerUp {
    public BombUp(int x, int y) {
        super(x, y, PowerUpType.BOMB_UP);
    }

    @Override
    public void applyTo(Player player) {
        player.increaseMaxBombs();
    }
}

