package com.superbomberman.model;

/**
 * Enumération des différents types de fin de partie dans le jeu Super Bomberman.
 * <p>
 * Permet de représenter les différents scénarios de victoire ou défaite possibles,
 * aussi bien en solo qu'en multijoueur.
 * </p>
 *
 * <ul>
 *     <li>{@link #SOLO_VICTORY} : Victoire du joueur en mode solo.</li>
 *     <li>{@link #SOLO_DEFEAT} : Défaite du joueur en mode solo.</li>
 *     <li>{@link #MULTI_PLAYER1_WINS} : Joueur 1 gagne en multijoueur.</li>
 *     <li>{@link #MULTI_PLAYER2_WINS} : Joueur 2 gagne en multijoueur.</li>
 *     <li>{@link #MULTI_DRAW} : Match nul en multijoueur.</li>
 * </ul>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-08
 */
public enum GameEndType {
    /** Victoire du joueur en mode solo. */
    SOLO_VICTORY,

    /** Défaite du joueur en mode solo. */
    SOLO_DEFEAT,

    /** Joueur 1 gagne en multijoueur. */
    MULTI_PLAYER1_WINS,

    /** Joueur 2 gagne en multijoueur. */
    MULTI_PLAYER2_WINS,

    /** Match nul en multijoueur. */
    MULTI_DRAW
}