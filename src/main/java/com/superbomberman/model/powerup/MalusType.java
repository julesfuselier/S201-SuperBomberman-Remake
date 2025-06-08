package com.superbomberman.model.powerup;

/**
 * Énumération des différents types de malus disponibles dans le jeu Super Bomberman.
 * <p>
 * Les malus sont des effets négatifs temporaires appliqués au joueur lorsqu'il ramasse
 * un power-up SKULL. Chaque malus a un effet spécifique qui handicape le joueur pendant
 * une durée déterminée.
 * </p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-06
 */
public enum MalusType {
    /** Les contrôles directionnels sont inversés (gauche/droite, haut/bas) */
    REVERSED_CONTROLS,

    /** La vitesse de déplacement est considérablement réduite */
    SLOW_SPEED,

    /** La vitesse de déplacement devient incontrôlablement rapide */
    SUPER_FAST,

    /** Le joueur pose automatiquement des bombes à intervalles réguliers */
    AUTO_BOMB,

    /** Le joueur ne peut plus poser de bombes manuellement */
    NO_BOMB,

    /** La portée d'explosion des bombes est réduite */
    REDUCED_RANGE
}