package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

public class BombPass extends PowerUp {
    public BombPass(int x, int y) {
        super(x, y, PowerUpType.BOMB_PASS);
    }

    @Override
    public void applyTo(Player player) {
        player.setCanPassThroughBombs(true);
    }
}

