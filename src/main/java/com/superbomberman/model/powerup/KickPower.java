package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

/**
 * Représente un power-up "Kick" dans le jeu SuperBomberman.
 *
 * <p>Ce power-up permet au joueur de donner un coup de pied
 * aux bombes pour les déplacer sur la carte.</p>
 *
 * <p>Il hérite de la classe abstraite {@link PowerUp}.</p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-13
 */
public class KickPower extends PowerUp {

    /**
     * Constructeur de KickPower.
     *
     * @param x la coordonnée X de la position du power-up sur la carte
     * @param y la coordonnée Y de la position du power-up sur la carte
     */
    public KickPower(int x, int y) {
        super(x, y, PowerUpType.KICK);
    }

    /**
     * Applique l'effet du power-up au joueur donné.
     *
     * <p>Cette méthode permet au joueur de donner un coup de pied aux bombes.</p>
     *
     * @param player le joueur auquel appliquer le power-up
     */
    @Override
    public void applyTo(Player player) {
        player.setCanKickBombs(true);
    }
}
