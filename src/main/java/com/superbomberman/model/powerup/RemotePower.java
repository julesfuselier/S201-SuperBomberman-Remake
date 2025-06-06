package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

public class RemotePower extends PowerUp {
    public RemotePower(int x, int y) {
        super(x, y,  PowerUpType.REMOTE);
    }

    @Override
    public void applyTo(Player player) {
        player.setRemoteDetonation(true);
    }
}
