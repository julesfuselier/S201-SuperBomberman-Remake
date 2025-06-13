package com.superbomberman.model;

/**
 * Enumération des différents modes de jeu disponibles dans Super Bomberman.
 * <p>
 * Permet de distinguer entre le mode solo (un joueur) et le mode multijoueur (deux joueurs).
 * </p>
 *
 * <ul>
 *     <li>{@link #ONE_PLAYER} : Mode un joueur (solo).</li>
 *     <li>{@link #TWO_PLAYER} : Mode deux joueurs (multijoueur).</li>
 * </ul>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-08
 */
public enum GameMode {
    /** Mode un joueur (solo). */
    ONE_PLAYER,

    /** Mode deux joueurs (multijoueur). */
    TWO_PLAYER
}