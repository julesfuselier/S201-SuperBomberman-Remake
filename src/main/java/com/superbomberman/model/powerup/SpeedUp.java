package com.superbomberman.model.powerup;
import com.superbomberman.model.Player;

public class SpeedUp extends PowerUp {
    public SpeedUp(int x, int y) {
        super(x, y, PowerUpType.SPEED_UP);
    }

    @Override
    public void applyTo(Player player) {
        player.increaseSpeed();
    }
}

