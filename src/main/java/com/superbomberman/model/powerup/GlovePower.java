package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

public class GlovePower extends PowerUp {
    public GlovePower(int x, int y) {
        super(x, y,  PowerUpType.GLOVE);
    }

    @Override
    public void applyTo(Player player) {
        player.setCanThrowBombs(true);
    }
}
