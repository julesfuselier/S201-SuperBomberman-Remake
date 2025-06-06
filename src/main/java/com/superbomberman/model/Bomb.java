package com.superbomberman.model;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Représente une bombe dans le jeu Bomberman.
 * Une bombe peut être placée par un joueur et explose après un délai défini.
 * L'explosion peut détruire des murs destructibles et affecter les entités dans sa portée.
 *
 * @author Votre nom
 * @version 1.0
 * @since 1.0
 */
public class Bomb {

    /** Position X de la bombe sur la grille */
    private int x, y;

    /** Position précédente de la bombe (pour le suivi des déplacements si nécessaire) */
    private int previousX, previousY;

    /** Dégâts infligés par l'explosion de la bombe */
    private int damage;

    /** Timer gérant le délai avant l'explosion */
    private Timeline timer;

    /** Portée de l'explosion en nombre de cases */
    private int range;

    /** Indique si la bombe a déjà explosé */
    private boolean exploded;

    /**
     * Constructeur pour créer une nouvelle bombe.
     *
     * @param x      Position X de la bombe sur la grille
     * @param y      Position Y de la bombe sur la grille
     * @param damage Dégâts infligés par l'explosion
     * @param range  Portée de l'explosion en nombre de cases
     */
    public Bomb(int x, int y, int damage, int range) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
        this.damage = damage;
        this.range = range;
        this.exploded = false;
    }

    /**
     * Démarre le compte à rebours avant l'explosion de la bombe.
     * Après 1 seconde, la bombe explose et exécute le code fourni en paramètre.
     *
     * @param onExplode Action à exécuter lors de l'explosion (callback)
     * @throws IllegalStateException si la bombe a déjà explosé
     */
    public void startCountdown(Runnable onExplode) {
        if (exploded) {
            throw new IllegalStateException("La bombe a déjà explosé");
        }

        timer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            exploded = true;
            onExplode.run();
        }));
        timer.setCycleCount(1);
        timer.play();
    }

    /**
     * Retourne la position X de la bombe sur la grille.
     *
     * @return Position X de la bombe
     */
    public int getX() {
        return x;
    }

    /**
     * Retourne la position Y de la bombe sur la grille.
     *
     * @return Position Y de la bombe
     */
    public int getY() {
        return y;
    }

    /**
     * Retourne les dégâts infligés par l'explosion de la bombe.
     *
     * @return Dégâts de la bombe
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Retourne la portée de l'explosion en nombre de cases.
     *
     * @return Portée de l'explosion
     */
    public int getRange() {
        return range;
    }

    /**
     * Indique si la bombe a déjà explosé.
     *
     * @return true si la bombe a explosé, false sinon
     */
    public boolean hasExploded() {
        return exploded;
    }

    /**
     * Arrête le timer de la bombe si elle n'a pas encore explosé.
     * Utile pour annuler une bombe avant son explosion.
     */
    public void cancel() {
        if (timer != null && !exploded) {
            timer.stop();
        }
    }
}