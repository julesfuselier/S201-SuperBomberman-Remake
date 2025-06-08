package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

public class KickPower extends PowerUp {
    public KickPower(int x, int y) {
        super(x, y, PowerUpType.KICK);
    }

    @Override
    public void applyTo(Player player) {
        player.setCanKickBombs(true);
    }
}

