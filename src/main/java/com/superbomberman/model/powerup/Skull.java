package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

public class Skull extends PowerUp {
    public Skull(int x, int y) {
        super(x, y, PowerUpType.SKULL);
    }

    @Override
    public void applyTo(Player player) {
        player.applyRandomMalus();
    }
}
