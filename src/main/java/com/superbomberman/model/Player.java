package com.superbomberman.model;

import java.util.Random;

import com.superbomberman.model.powerup.MalusType;

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
    private int explosionRange = 2;

    /** Vitesse de déplacement du joueur. */
    private double speed = 1.0;

    /** Indique si le joueur peut pousser les bombes. */
    private boolean canKickBombs = false;

    private boolean canThrowBombs = false;

    private boolean hasRemoteDetonation = false;

    private boolean canPassThroughWalls = false;

    private boolean canPassThroughBombs = false;

    private boolean hasLineBombs = false;

    /** Bombe actuellement tenue par le joueur (null si aucune) */
    private Bomb heldBomb = null;

    /** Indique si le joueur tient actuellement une bombe */
    private boolean isHoldingBomb = false;

    /** Malus actuel du joueur (null si aucun) */
    private MalusType currentMalus = null;

    /** Timestamp de fin du malus actuel */
    private long malusEndTime = 0;

    /** Durée d'un malus en millisecondes */
    private static final long MALUS_DURATION = 10000; // 10 secondes

    /** Vitesse sauvegardée avant le malus de vitesse */
    private double savedSpeed = 0;

    /** Range sauvegardée avant le malus de range */
    private int savedRange = 0;

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

    public void setCanThrowBombs(boolean value) {
        canThrowBombs = value;
    }

    public boolean canThrowBombs() {
        return canThrowBombs;
    }

    public void setRemoteDetonation(boolean value) {
        hasRemoteDetonation = value;
    }

    public boolean hasRemoteDetonation() {
        return hasRemoteDetonation;
    }

    public void setCanPassThroughWalls(boolean value) {
        canPassThroughWalls = value;
    }

    public boolean canPassThroughWalls() {
        return canPassThroughWalls;
    }

    public void setCanPassThroughBombs(boolean value) {
        canPassThroughBombs = value;
    }

    public boolean canPassThroughBombs() {
        return canPassThroughBombs;
    }

    public void setHasLineBombs(boolean value) {
        hasLineBombs = value;
    }

    public boolean hasLineBombs() {
        return hasLineBombs;
    }

    /**
     * Fait ramasser une bombe au joueur.
     *
     * @param bomb La bombe à ramasser
     * @return true si le ramassage a réussi, false sinon
     */
    public boolean pickUpBomb(Bomb bomb) {
        // Vérifier que le joueur peut ramasser (a le Glove Power, pas déjà une bombe en main, etc.)
        if (!canThrowBombs() || isHoldingBomb || bomb.getOwner() != this) {
            return false;
        }

        this.heldBomb = bomb;
        this.isHoldingBomb = true;
        System.out.println("Bombe ramassée !");
        return true;
    }

    /**
     * Fait lancer la bombe tenue par le joueur dans une direction.
     *
     * @param directionX Direction X du lancer (-1, 0, 1)
     * @param directionY Direction Y du lancer (-1, 0, 1)
     * @return La bombe lancée, ou null si aucune bombe tenue
     */
    public Bomb throwHeldBomb(int directionX, int directionY) {
        if (!isHoldingBomb || heldBomb == null) {
            return null;
        }

        Bomb thrownBomb = heldBomb;
        this.heldBomb = null;
        this.isHoldingBomb = false;

        System.out.println("Bombe lancée dans la direction (" + directionX + ", " + directionY + ")");
        return thrownBomb;
    }

    /**
     * Vérifie si le joueur tient actuellement une bombe.
     *
     * @return true si le joueur tient une bombe, false sinon
     */
    public boolean isHoldingBomb() {
        return isHoldingBomb;
    }

    /**
     * Retourne la bombe actuellement tenue par le joueur.
     *
     * @return La bombe tenue, ou null si aucune
     */
    public Bomb getHeldBomb() {
        return heldBomb;
    }

    /**
     * Force le joueur à lâcher sa bombe (en cas d'urgence).
     */
    public void dropHeldBomb() {
        this.heldBomb = null;
        this.isHoldingBomb = false;
    }

    /**
     * Applique un malus aléatoire au joueur.
     */
    public void applyRandomMalus() {
        // Nettoyer l'ancien malus s'il y en a un
        clearCurrentMalus();

        // Choisir un malus aléatoire
        MalusType[] malusTypes = MalusType.values();
        currentMalus = malusTypes[new Random().nextInt(malusTypes.length)];
        malusEndTime = System.currentTimeMillis() + MALUS_DURATION;

        // Appliquer l'effet du malus
        switch (currentMalus) {
            case SLOW_SPEED -> {
                savedSpeed = speed;
                speed = Math.max(0.3, speed - 0.7);
                System.out.println("MALUS: Vitesse réduite!");
            }
            case SUPER_FAST -> {
                savedSpeed = speed;
                speed += 2.0;
                System.out.println("MALUS: Vitesse incontrôlable!");
            }
            case REDUCED_RANGE -> {
                savedRange = explosionRange;
                explosionRange = Math.max(1, explosionRange - 2);
                System.out.println("MALUS: Portée d'explosion réduite!");
            }
            case REVERSED_CONTROLS -> {
                System.out.println("MALUS: Contrôles inversés!");
            }
            case AUTO_BOMB -> {
                System.out.println("MALUS: Pose de bombes automatique!");
            }
            case NO_BOMB -> {
                System.out.println("MALUS: Impossible de poser des bombes!");
            }
        }

        System.out.println("Malus actif pendant " + (MALUS_DURATION / 1000) + " secondes: " + currentMalus);
    }

    /**
     * Nettoie le malus actuel et restaure les valeurs normales.
     */
    private void clearCurrentMalus() {
        if (currentMalus != null) {
            switch (currentMalus) {
                case SLOW_SPEED, SUPER_FAST -> {
                    if (savedSpeed > 0) {
                        speed = savedSpeed;
                        savedSpeed = 0;
                    }
                }
                case REDUCED_RANGE -> {
                    if (savedRange > 0) {
                        explosionRange = savedRange;
                        savedRange = 0;
                    }
                }
            }
        }
        currentMalus = null;
        malusEndTime = 0;
    }

    /**
     * Met à jour l'état du malus (à appeler régulièrement).
     */
    public void updateMalus() {
        if (currentMalus != null && System.currentTimeMillis() >= malusEndTime) {
            System.out.println("Fin du malus: " + currentMalus);
            clearCurrentMalus();
        }
    }

    /**
     * Vérifie si le joueur a un malus spécifique.
     */
    public boolean hasMalus(MalusType malusType) {
        return currentMalus == malusType;
    }

    /**
     * Retourne le malus actuel du joueur.
     */
    public MalusType getCurrentMalus() {
        return currentMalus;
    }

    /**
     * Vérifie si le joueur a un malus actif.
     */
    public boolean hasActiveMalus() {
        return currentMalus != null;
    }

    /**
     * Retourne le temps restant du malus en millisecondes.
     */
    public long getMalusTimeRemaining() {
        if (currentMalus == null) return 0;
        return Math.max(0, malusEndTime - System.currentTimeMillis());
    }
}