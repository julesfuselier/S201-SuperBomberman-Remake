package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

public class LineBomb extends PowerUp {
    public LineBomb(int x, int y) {
        super(x, y, PowerUpType.LINE_BOMB);
    }

    @Override
    public void applyTo(Player player) {
        player.setHasLineBombs(true);
    }
}
