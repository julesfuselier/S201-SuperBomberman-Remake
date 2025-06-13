package com.superbomberman.model.powerup;

import java.util.Random;

/**
 * Énumération des différents types de power-ups disponibles dans SuperBomberman.
 *
 * <p>Chaque type correspond à un power-up spécifique avec un effet particulier
 * appliqué au joueur.</p>
 *
 * <p>Cette énumération fournit également une méthode statique pour obtenir
 * un type de power-up aléatoire.</p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @date 2025-06-13
 */
public enum PowerUpType {
    /** Augmente le nombre maximum de bombes posables */
    BOMB_UP,

    /** Augmente la portée des bombes */
    RANGE_UP,

    /** Augmente la vitesse du joueur */
    SPEED_UP,

    /** Permet de donner des coups de pied aux bombes */
    KICK,

    /** Permet de lancer les bombes */
    GLOVE,

    /** Permet de contrôler des bombes à distance */
    REMOTE,

    /** Permet de traverser certains murs */
    WALL_PASS,

    /** Permet de traverser les bombes */
    BOMB_PASS,

    /** Permet d’utiliser des bombes qui explosent en ligne */
    LINE_BOMB,

    /** Malus lié à un effet négatif (exemple : tête de mort) */
    SKULL;

    private static final Random RANDOM = new Random();

    /**
     * Retourne un type de power-up choisi aléatoirement parmi tous les types possibles.
     *
     * @return un {@link PowerUpType} aléatoire
     */
    public static PowerUpType randomType() {
        PowerUpType[] values = values();
        return values[RANDOM.nextInt(values.length)];
    }
}
