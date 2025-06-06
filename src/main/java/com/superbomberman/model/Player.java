package com.superbomberman.model;

/**
 * Représente un joueur dans le jeu Super Bomberman, avec sa position, ses capacités et ses bonus.
 * <p>
 * Cette classe gère la position courante et précédente du joueur, le nombre maximum de bombes
 * qu'il peut poser, la portée de ses explosions, sa vitesse de déplacement et sa capacité à
 * pousser les bombes. Elle fournit des méthodes pour manipuler et interroger ces propriétés,
 * notamment lors de l'obtention de bonus en jeu.
 * </p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-05
 */

public class Player {
    /** Position actuelle sur l'axe X du joueur. */
    private int x;
    /** Position actuelle sur l'axe Y du joueur. */
    private int y;
    /** Position précédente sur l'axe X du joueur. */
    private int previousX;
    /** Position précédente sur l'axe Y du joueur. */
    private int previousY;
    /** Nombre maximum de bombes que le joueur peut poser en même temps. */
    private int maxBombs = 1;
    /** Portée de l'explosion des bombes posées par le joueur. */
    private int explosionRange = 1;
    /** Vitesse de déplacement du joueur. */
    private double speed = 1.0;
    /** Indique si le joueur peut pousser les bombes. */
    private boolean canKickBombs = false;

    /**
     * Définit la position du joueur et met à jour la position précédente.
     *
     * @param x Nouvelle coordonnée X du joueur.
     * @param y Nouvelle coordonnée Y du joueur.
     */
    public void setPosition(int x, int y) {
        this.previousX = this.x;
        this.previousY = this.y;
        this.x = x;
        this.y = y;
    }

    /**
     * Retourne la position actuelle X du joueur.
     *
     * @return La coordonnée X actuelle.
     */
    public int getX() { return x; }

    /**
     * Retourne la position actuelle Y du joueur.
     *
     * @return La coordonnée Y actuelle.
     */
    public int getY() { return y; }

    /**
     * Retourne la dernière position X enregistrée avant le dernier déplacement.
     *
     * @return La coordonnée X précédente.
     */
    public int getPreviousX() { return previousX; }

    /**
     * Retourne la dernière position Y enregistrée avant le dernier déplacement.
     *
     * @return La coordonnée Y précédente.
     */
    public int getPreviousY() { return previousY; }

    /**
     * Retourne le nombre maximum de bombes que le joueur peut poser simultanément.
     *
     * @return Le nombre maximum de bombes.
     */
    public int getMaxBombs() { return maxBombs; }

    /**
     * Retourne la portée d'explosion des bombes posées par le joueur.
     *
     * @return La portée d'explosion.
     */
    public int getExplosionRange() { return explosionRange; }

    /**
     * Retourne la vitesse de déplacement du joueur.
     *
     * @return La vitesse du joueur.
     */
    public double getSpeed() { return speed; }

    /**
     * Augmente de 1 le nombre maximum de bombes que le joueur peut poser.
     */
    public void increaseMaxBombs() {
        maxBombs++;
    }

    /**
     * Augmente de 1 la portée d'explosion des bombes du joueur.
     */
    public void increaseExplosionRange() {
        explosionRange++;
    }

    /**
     * Augmente la vitesse de déplacement du joueur de 0,2.
     */
    public void increaseSpeed() {
        speed += 0.2;
    }

    /**
     * Définit la capacité du joueur à pousser les bombes.
     *
     * @param value true si le joueur peut pousser les bombes, sinon false.
     */
    public void setCanKickBombs(boolean value) {
        canKickBombs = value;
    }

    /**
     * Indique si le joueur peut actuellement pousser les bombes.
     *
     * @return true si le joueur peut pousser les bombes, sinon false.
     */
    public boolean canKickBombs() {
        return canKickBombs;
    }
}