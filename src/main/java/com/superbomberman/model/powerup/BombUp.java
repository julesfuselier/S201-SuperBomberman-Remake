package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

/**
 * Représente un power-up "BombUp" dans le jeu SuperBomberman.
 *
 * <p>Ce power-up permet d'augmenter le nombre maximum de bombes
 * qu'un joueur peut poser simultanément.</p>
 *
 * <p>Il hérite de la classe abstraite {@link PowerUp}.</p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-13
 */
public class BombUp extends PowerUp {

    /**
     * Constructeur de BombUp.
     *
     * @param x la coordonnée X de la position du power-up sur la carte
     * @param y la coordonnée Y de la position du power-up sur la carte
     */
    public BombUp(int x, int y) {
        super(x, y, PowerUpType.BOMB_UP);
    }

    /**
     * Applique l'effet du power-up au joueur donné.
     *
     * <p>Cette méthode augmente la capacité maximale de bombes que
     * le joueur peut poser.</p>
     *
     * @param player le joueur auquel appliquer le power-up
     */
    @Override
    public void applyTo(Player player) {
        player.increaseMaxBombs();
    }
}


