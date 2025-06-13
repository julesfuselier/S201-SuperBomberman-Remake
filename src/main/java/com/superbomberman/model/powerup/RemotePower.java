package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;

/**
 * Représente un power-up "RemotePower" dans le jeu SuperBomberman.
 *
 * <p>Ce power-up permet au joueur de déclencher ses bombes à distance.</p>
 *
 * <p>Il hérite de la classe abstraite {@link PowerUp}.</p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-13
 */
public class RemotePower extends PowerUp {

    /**
     * Constructeur de RemotePower.
     *
     * @param x la coordonnée X de la position du power-up sur la carte
     * @param y la coordonnée Y de la position du power-up sur la carte
     */
    public RemotePower(int x, int y) {
        super(x, y, PowerUpType.REMOTE);
    }

    /**
     * Applique l'effet du power-up au joueur donné.
     *
     * <p>Cette méthode active la capacité du joueur à déclencher ses bombes à distance.</p>
     *
     * @param player le joueur auquel appliquer le power-up
     */
    @Override
    public void applyTo(Player player) {
        player.setRemoteDetonation(true);
    }
}
