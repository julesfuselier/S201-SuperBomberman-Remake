package com.superbomberman.model;

/**
 * Représente une case (tuile) de la carte dans Super Bomberman.
 * <p>
 * Une tuile a un type {@link TileType} qui définit sa nature (mur, sol, power-up, sortie, etc).
 * Elle peut être testée pour sa franchissabilité (walkable) et d'autres comportements selon son type.
 * </p>
 *
 * @author Hugo Brest Lestrade
 * @version 1.0
 * @since 2025-06-12
 */
public class Tile {
    private final TileType type;

    /**
     * Construit une tuile du type spécifié.
     * @param type Le type de la tuile (mur, sol, etc)
     */
    public Tile(TileType type) {
        this.type = type;
    }

    /**
     * Retourne le type de cette tuile.
     * @return le type {@link TileType} de la tuile
     */
    public TileType getType() {
        return type;
    }

    /**
     * Indique si la tuile est franchissable (le joueur peut marcher dessus).
     * @return true si la tuile est franchissable, false sinon
     */
    public boolean isWalkable() {
        return type == TileType.FLOOR || type == TileType.POWER_UP || type == TileType.EXIT;
    }

}