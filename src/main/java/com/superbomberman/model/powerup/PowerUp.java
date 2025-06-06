package com.superbomberman.model.powerup;

// Classe mère des PowerUp

import com.superbomberman.model.Player;

public abstract class PowerUp {
    protected int x, y;
    protected final PowerUpType type;

    public PowerUp(int x, int y, PowerUpType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public PowerUpType getType() {
        return type;
    }

    public abstract void applyTo(Player player);
}

