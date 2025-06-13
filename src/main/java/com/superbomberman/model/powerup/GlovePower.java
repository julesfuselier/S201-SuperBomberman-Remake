package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

/**
 * Représente un power-up "Glove" dans le jeu SuperBomberman.
 *
 * <p>Ce power-up permet au joueur de lancer ses bombes
 * au lieu de les poser simplement au sol.</p>
 *
 * <p>Il hérite de la classe abstraite {@link PowerUp}.</p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since  2025-06-13
 */
public class GlovePower extends PowerUp {

    /**
     * Constructeur de GlovePower.
     *
     * @param x la coordonnée X de la position du power-up sur la carte
     * @param y la coordonnée Y de la position du power-up sur la carte
     */
    public GlovePower(int x, int y) {
        super(x, y, PowerUpType.GLOVE);
    }

    /**
     * Applique l'effet du power-up au joueur donné.
     *
     * <p>Cette méthode autorise le joueur à lancer ses bombes.</p>
     *
     * @param player le joueur auquel appliquer le power-up
     */
    @Override
    public void applyTo(Player player) {
        player.setCanThrowBombs(true);
    }
}
