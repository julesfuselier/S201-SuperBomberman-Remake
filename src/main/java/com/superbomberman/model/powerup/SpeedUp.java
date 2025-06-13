package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

/**
 * Représente un power-up "SpeedUp" dans le jeu SuperBomberman.
 *
 * <p>Ce power-up augmente la vitesse de déplacement du joueur.</p>
 *
 * <p>Il hérite de la classe abstraite {@link PowerUp}.</p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @date 2025-06-13
 */
public class SpeedUp extends PowerUp {

    /**
     * Constructeur de SpeedUp.
     *
     * @param x la coordonnée X de la position du power-up sur la carte
     * @param y la coordonnée Y de la position du power-up sur la carte
     */
    public SpeedUp(int x, int y) {
        super(x, y, PowerUpType.SPEED_UP);
    }

    /**
     * Applique l'effet du power-up au joueur donné.
     *
     * <p>Cette méthode augmente la vitesse de déplacement du joueur.</p>
     *
     * @param player le joueur auquel appliquer le power-up
     */
    @Override
    public void applyTo(Player player) {
        player.increaseSpeed();
    }
}
