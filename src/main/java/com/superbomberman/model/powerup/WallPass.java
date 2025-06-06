package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

public class WallPass extends PowerUp {
    public WallPass(int x, int y) {
        super(x, y, PowerUpType.WALL_PASS);
    }

    @Override
    public void applyTo(Player player) {
        player.setCanPassThroughWalls(true);
    }
}

