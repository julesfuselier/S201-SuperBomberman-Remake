package com.superbomberman.model;

public enum TileType {
    WALL('#'),
    PLAYER1('1'),
    PLAYER2('2'),
    ENEMY('E'),
    BOMB('B'),
    POWER_UP('P'),
    EXIT('X'),
    WALL_BREAKABLE('0'),
    FLOOR(' ');

    private final char symbol;

    TileType(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return this.symbol;
    }

    public static TileType fromSymbol(char symbol) {
        for (TileType type : TileType.values()) {
            if (type.symbol == symbol) {
                return type;
            }
        }
        return FLOOR; // Valeur par défaut si le symbole n'est pas reconnu
    }
}