package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

public class RangeUp extends PowerUp {
    public RangeUp(int x, int y) {
        super(x, y, PowerUpType.RANGE_UP);
    }

    public RangeUp(int x, int y, PowerUpType type) {
        super(x, y, type);
    }

    @Override
    public void applyTo(Player player) {
        player.increaseExplosionRange();
    }
}