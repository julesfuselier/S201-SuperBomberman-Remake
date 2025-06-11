package com.superbomberman.model;

/**
 * Représente un ennemi dans le jeu Super Bomberman.
 * <p>
 * Cette classe gère la position courante et précédente de l'ennemi,
 * ainsi que son état de vie/mort pour le système de victoire.
 * </p>
 *
 * @author Jules Fuselier
 * @version 2.0
 * @since 2025-06-08
 */
public class Enemy {

    /** Position actuelle sur l'axe X de l'ennemi. */
    private int x;

    /** Position actuelle sur l'axe Y de l'ennemi. */
    private int y;

    /** Position précédente sur l'axe X de l'ennemi. */
    private int previousX;

    /** Position précédente sur l'axe Y de l'ennemi. */
    private int previousY;

    /** Indique si l'ennemi est vivant */
    private boolean isAlive = true;

    /**
     * Définit la position de l'ennemi et met à jour la position précédente.
     *
     * @param x Nouvelle coordonnée X de l'ennemi.
     * @param y Nouvelle coordonnée Y de l'ennemi.
     */
    public void setPosition(int x, int y) {
        this.previousX = this.x;
        this.previousY = this.y;
        this.x = x;
        this.y = y;
    }

    /**
     * Retourne la position actuelle X de l'ennemi.
     *
     * @return La coordonnée X actuelle.
     */
    public int getX() {
        return x;
    }

    /**
     * Retourne la position actuelle Y de l'ennemi.
     *
     * @return La coordonnée Y actuelle.
     */
    public int getY() {
        return y;
    }

    /**
     * Retourne la dernière position X enregistrée avant le dernier déplacement.
     *
     * @return La coordonnée X précédente.
     */
    public int getPreviousX() {
        return previousX;
    }

    /**
     * Retourne la dernière position Y enregistrée avant le dernier déplacement.
     *
     * @return La coordonnée Y précédente.
     */
    public int getPreviousY() {
        return previousY;
    }
    
    /**
     * Tue l'ennemi (le marque comme mort)
     */
    public void kill() {
        this.isAlive = false;
        System.out.println("💀 Ennemi éliminé à la position (" + x + ", " + y + ")");
    }

    /**
     * Ressuscite l'ennemi (pour les cas spéciaux ou reset)
     */
    public void revive() {
        this.isAlive = true;
        System.out.println("✨ Ennemi ressuscité");
    }

    /**
     * Vérifie si l'ennemi est vivant
     * @return true si l'ennemi est vivant, false sinon
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Vérifie si l'ennemi est mort
     * @return true si l'ennemi est mort, false sinon
     */
    public boolean isDead() {
        return !isAlive;
    }

    /**
     * Tue l'ennemi (pour compatibilité)
     */
    public void setDead(boolean dead) {
        this.isAlive = !dead;
    }

    /**
     * Définit si l'ennemi est vivant ou mort
     */
    public void setAlive(boolean b) {
        this.isAlive = b;
    }
}