package com.superbomberman.model.powerup;


import static com.superbomberman.model.powerup.PowerUpType.RANGE_UP;

public class PowerUpFactory {

    public static PowerUp create(PowerUpType type, int x, int y) {
        return switch (type) {
            case RANGE_UP     -> new RangeUp(x, y);
            case BOMB_UP     -> new BombUp(x, y);
            case SPEED_UP    -> new SpeedUp(x, y);
            case KICK        -> new KickPower(x, y);
            case GLOVE       -> new GlovePower(x, y);
            case REMOTE      -> new RemotePower(x, y);
            case WALL_PASS   -> new WallPass(x, y);
            case BOMB_PASS   -> new BombPass(x, y);
            case LINE_BOMB   -> new LineBomb(x, y);
            case SKULL       -> new SkullMalus(x, y);
        };
    }

}

