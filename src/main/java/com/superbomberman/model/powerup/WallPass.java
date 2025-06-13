package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

/**
 * Représente un power-up "WallPass" dans le jeu SuperBomberman.
 *
 * <p>Ce power-up permet au joueur de traverser certains murs.</p>
 *
 * <p>Il hérite de la classe abstraite {@link PowerUp}.</p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @date 2025-06-13
 */
public class WallPass extends PowerUp {

    /**
     * Constructeur de WallPass.
     *
     * @param x la coordonnée X de la position du power-up sur la carte
     * @param y la coordonnée Y de la position du power-up sur la carte
     */
    public WallPass(int x, int y) {
        super(x, y, PowerUpType.WALL_PASS);
    }

    /**
     * Applique l'effet du power-up au joueur donné.
     *
     * <p>Cette méthode permet au joueur de traverser certains murs.</p>
     *
     * @param player le joueur auquel appliquer le power-up
     */
    @Override
    public void applyTo(Player player) {
        player.setCanPassThroughWalls(true);
    }
}
