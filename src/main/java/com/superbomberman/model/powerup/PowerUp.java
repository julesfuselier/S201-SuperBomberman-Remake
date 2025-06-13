package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

/**
 * Classe abstraite représentant un power-up dans le jeu SuperBomberman.
 *
 * <p>Chaque power-up possède une position (x, y) sur la carte ainsi qu'un type
 * défini par l'énumération {@link PowerUpType}.</p>
 *
 * <p>Cette classe définit la méthode abstraite {@link #applyTo(Player)}
 * qui doit être implémentée pour appliquer l'effet du power-up à un joueur.</p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-13
 */
public abstract class PowerUp {

    /** Coordonnée X de la position du power-up */
    protected int x;

    /** Coordonnée Y de la position du power-up */
    protected int y;

    /** Type du power-up */
    protected final PowerUpType type;

    /**
     * Constructeur de la classe PowerUp.
     *
     * @param x la coordonnée X de la position du power-up
     * @param y la coordonnée Y de la position du power-up
     * @param type le type du power-up
     */
    public PowerUp(int x, int y, PowerUpType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    /**
     * Retourne le type du power-up.
     *
     * @return le type du power-up
     */
    public PowerUpType getType() {
        return type;
    }

    /**
     * Applique l'effet du power-up au joueur spécifié.
     *
     * @param player le joueur auquel appliquer l'effet du power-up
     */
    public abstract void applyTo(Player player);

    /**
     * Retourne la coordonnée X du power-up.
     *
     * @return la coordonnée X
     */
    public int getX() {
        return x;
    }

    /**
     * Retourne la coordonnée Y du power-up.
     *
     * @return la coordonnée Y
     */
    public int getY() {
        return y;
    }
}
