package com.superbomberman.model;

public class Tile {
    private final TileType type;

    public Tile(TileType type) {
        this.type = type;
    }

    public TileType getType() {
        return type;
    }

    public boolean isWalkable() {
        return type == TileType.FLOOR || type == TileType.POWER_UP || type == TileType.EXIT;
    }

    // TODO : FAIRE D'AUTRES METHODES
}
