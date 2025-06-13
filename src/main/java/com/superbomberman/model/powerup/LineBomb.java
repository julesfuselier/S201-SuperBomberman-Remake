package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

/**
 * Représente un power-up "LineBomb" dans le jeu SuperBomberman.
 *
 * <p>Ce power-up permet au joueur d'avoir des bombes qui explosent en ligne,
 * augmentant ainsi la portée et l'effet de l'explosion.</p>
 *
 * <p>Il hérite de la classe abstraite {@link PowerUp}.</p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @date 2025-06-13
 */
public class LineBomb extends PowerUp {

    /**
     * Constructeur de LineBomb.
     *
     * @param x la coordonnée X de la position du power-up sur la carte
     * @param y la coordonnée Y de la position du power-up sur la carte
     */
    public LineBomb(int x, int y) {
        super(x, y, PowerUpType.LINE_BOMB);
    }

    /**
     * Applique l'effet du power-up au joueur donné.
     *
     * <p>Cette méthode active la capacité du joueur à utiliser des bombes en ligne.</p>
     *
     * @param player le joueur auquel appliquer le power-up
     */
    @Override
    public void applyTo(Player player) {
        player.setHasLineBombs(true);
    }
}
