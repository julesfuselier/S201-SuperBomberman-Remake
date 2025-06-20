package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

/**
 * Représente un power-up "RangeUp" dans le jeu SuperBomberman.
 *
 * <p>Ce power-up augmente la portée des explosions de bombes du joueur.</p>
 *
 * <p>Il hérite de la classe abstraite {@link PowerUp}.</p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-13
 */
public class RangeUp extends PowerUp {

    /**
     * Constructeur principal de RangeUp avec le type fixé à RANGE_UP.
     *
     * @param x la coordonnée X de la position du power-up sur la carte
     * @param y la coordonnée Y de la position du power-up sur la carte
     */
    public RangeUp(int x, int y) {
        super(x, y, PowerUpType.RANGE_UP);
    }

    /**
     * Applique l'effet du power-up au joueur donné.
     *
     * <p>Cette méthode augmente la portée des explosions des bombes du joueur.</p>
     *
     * @param player le joueur auquel appliquer le power-up
     */
    @Override
    public void applyTo(Player player) {
        player.increaseExplosionRange();
    }
}
