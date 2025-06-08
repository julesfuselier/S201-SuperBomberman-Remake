package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

/**
 * Représente le power-up BombPass dans le jeu Super Bomberman.
 * <p>
 * Le BombPass permet au joueur de traverser ses propres bombes sans collision.
 * Le joueur peut ainsi sortir d'une bombe qu'il vient de poser ou passer à travers
 * ses bombes déjà posées sur le terrain.
 * </p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-07
 */
public class BombPass extends PowerUp {

    /**
     * Constructeur du power-up BombPass.
     *
     * @param x Coordonnée X du power-up sur la grille
     * @param y Coordonnée Y du power-up sur la grille
     */
    public BombPass(int x, int y) {
        super(x, y, PowerUpType.BOMB_PASS);
    }

    /**
     * Applique l'effet du power-up BombPass au joueur.
     * <p>
     * Active la capacité du joueur à passer à travers ses propres bombes.
     * Cette capacité reste active en permanence une fois obtenue.
     * </p>
     *
     * @param player Le joueur qui ramasse le power-up
     */
    @Override
    public void applyTo(Player player) {
        player.setCanPassThroughBombs(true);
    }
}