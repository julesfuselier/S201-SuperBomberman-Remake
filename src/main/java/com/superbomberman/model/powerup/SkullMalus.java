package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

/**
 * Représente un malus SKULL dans le jeu Super Bomberman.
 * <p>
 * Le SKULL est un power-up négatif qui applique un effet handicapant aléatoire
 * au joueur qui le ramasse. L'effet est temporaire et varie à chaque fois.
 * </p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-06
 */
public class SkullMalus extends PowerUp {

    /**
     * Constructeur du malus SKULL.
     *
     * @param x Coordonnée X du malus sur la grille
     * @param y Coordonnée Y du malus sur la grille
     */
    public SkullMalus(int x, int y) {
        super(x, y, PowerUpType.SKULL);
    }

    /**
     * Applique l'effet du malus SKULL au joueur.
     * <p>
     * Cette méthode déclenche l'application d'un malus aléatoire au joueur
     * via la méthode applyRandomMalus() de la classe Player.
     * </p>
     *
     * @param player Le joueur qui ramasse le malus
     */
    @Override
    public void applyTo(Player player) {
        player.applyRandomMalus();
    }
}