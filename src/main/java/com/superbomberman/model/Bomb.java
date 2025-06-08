package com.superbomberman.model;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Représente une bombe dans le jeu Super Bomberman.
 * <p>
 * Une bombe possède une position, un délai d'explosion, une portée d'explosion,
 * et un propriétaire (le joueur qui l'a posée). Elle peut déclencher une explosion
 * après un certain délai et exécuter une action définie lors de cette explosion.
 * Elle peut également être lancée et se déplacer en ligne droite.
 * </p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-07
 */
public class Bomb {
    private int x, y;
    private int previousX, previousY;
    private int damage;
    private Timeline timer;
    private int range;
    private boolean exploded;

    /** Joueur qui a posé cette bombe */
    private Player owner;

    /** Indique si la bombe est en train de voler (lancée avec Glove) */
    private boolean isFlying = false;

    /** Direction X du mouvement de la bombe en vol (-1, 0, 1) */
    private int flyDirectionX = 0;

    /** Direction Y du mouvement de la bombe en vol (-1, 0, 1) */
    private int flyDirectionY = 0;

    /** Timer pour le mouvement de la bombe en vol */
    private Timeline flyTimer;

    /** Callback pour notifier le mouvement */
    private Runnable moveCallback;

    /** ⭐ NOUVEAU : Indique si la bombe glisse suite à un coup de pied (Kick Power) */
    private boolean isMoving = false;

    /** ⭐ NOUVEAU : Direction X du glissement par coup de pied (-1, 0, 1) */
    private int kickDirectionX = 0;

    /** ⭐ NOUVEAU : Direction Y du glissement par coup de pied (-1, 0, 1) */
    private int kickDirectionY = 0;

    /** ⭐ NOUVEAU : Timer pour le mouvement par coup de pied */
    private Timeline kickTimer;

    public Bomb(int x, int y, int damage, int range) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
        this.damage = damage;
        this.range = range;
        this.owner = null; // Sera défini lors de la pose
    }

    public void startCountdown(Runnable onExplode) {
        timer = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
            exploded = true;
            onExplode.run();  // Cette ligne exécute le code qu'on lui a passé
        }));
        timer.setCycleCount(1);
        timer.play();
    }

    /**
     * Arrête le timer d'explosion de la bombe.
     */
    public void stopCountdown() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    /**
     * Lance la bombe dans une direction donnée (Glove Power).
     *
     * @param directionX Direction X (-1, 0, 1)
     * @param directionY Direction Y (-1, 0, 1)
     * @param moveCallback Callback appelé à chaque tick de mouvement
     */
    public void throwBomb(int directionX, int directionY, Runnable moveCallback) {
        this.flyDirectionX = directionX;
        this.flyDirectionY = directionY;
        this.isFlying = true;
        this.moveCallback = moveCallback;

        // Timer pour le mouvement de la bombe (toutes les 200ms)
        flyTimer = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            if (moveCallback != null) {
                moveCallback.run(); // Le GameViewController gère le mouvement
            }
        }));
        flyTimer.setCycleCount(Timeline.INDEFINITE);
        flyTimer.play();

        System.out.println("Bombe lancée ! Direction: (" + directionX + ", " + directionY + ")");
    }

    /**
     * ⭐ NOUVEAU : Fait glisser la bombe suite à un coup de pied (Kick Power).
     *
     * @param directionX Direction X (-1, 0, 1)
     * @param directionY Direction Y (-1, 0, 1)
     * @param moveCallback Callback appelé à chaque tick de mouvement
     */
    public void kickBomb(int directionX, int directionY, Runnable moveCallback) {
        this.kickDirectionX = directionX;
        this.kickDirectionY = directionY;
        this.isMoving = true;
        this.moveCallback = moveCallback;

        // Timer pour le glissement de la bombe (toutes les 300ms, un peu plus lent que le vol)
        kickTimer = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            if (moveCallback != null) {
                moveCallback.run(); // Le GameViewController gère le mouvement
            }
        }));
        kickTimer.setCycleCount(Timeline.INDEFINITE);
        kickTimer.play();

        System.out.println("Bombe donnée un coup de pied ! Direction: (" + directionX + ", " + directionY + ")");
    }

    /**
     * Calcule la prochaine position de la bombe en vol sans la déplacer.
     *
     * @return Tableau [newX, newY] de la prochaine position
     */
    public int[] getNextPosition() {
        return new int[]{x + flyDirectionX, y + flyDirectionY};
    }

    /**
     * ⭐ NOUVEAU : Calcule la prochaine position de la bombe qui glisse sans la déplacer.
     *
     * @return Tableau [newX, newY] de la prochaine position
     */
    public int[] getNextKickPosition() {
        return new int[]{x + kickDirectionX, y + kickDirectionY};
    }

    /**
     * Déplace la bombe vers sa prochaine position en vol (appelé par le GameViewController).
     */
    public void moveToNextPosition() {
        if (!isFlying) return;

        previousX = x;
        previousY = y;
        x += flyDirectionX;
        y += flyDirectionY;

        System.out.println("Bombe en vol se déplace vers (" + x + ", " + y + ")");
    }

    /**
     * ⭐ NOUVEAU : Déplace la bombe vers sa prochaine position de glissement (appelé par le GameViewController).
     */
    public void moveToNextKickPosition() {
        if (!isMoving) return;

        previousX = x;
        previousY = y;
        x += kickDirectionX;
        y += kickDirectionY;

        System.out.println("Bombe glisse vers (" + x + ", " + y + ")");
    }

    /**
     * Arrête le vol de la bombe (collision détectée).
     */
    public void stopFlying() {
        if (flyTimer != null) {
            flyTimer.stop();
            flyTimer = null;
        }
        this.isFlying = false;
        this.flyDirectionX = 0;
        this.flyDirectionY = 0;
        this.moveCallback = null;
        System.out.println("Bombe arrêtée en (" + x + ", " + y + ")");
    }

    /**
     * ⭐ NOUVEAU : Arrête le glissement de la bombe (collision détectée).
     */
    public void stopMoving() {
        if (kickTimer != null) {
            kickTimer.stop();
            kickTimer = null;
        }
        this.isMoving = false;
        this.kickDirectionX = 0;
        this.kickDirectionY = 0;
        this.moveCallback = null;
        System.out.println("Bombe arrête de glisser en (" + x + ", " + y + ")");
    }

    /**
     * Force la position de la bombe (utilisé pour la repositionner après collision).
     */
    public void setPosition(int x, int y) {
        this.previousX = this.x;
        this.previousY = this.y;
        this.x = x;
        this.y = y;
    }

    // Getters existants
    public int getX() { return x; }
    public int getY() { return y; }
    public int getDamage() { return damage; }
    public int getRange() { return range; }
    public int getPreviousX() { return previousX; }
    public int getPreviousY() { return previousY; }

    /**
     * Retourne le joueur propriétaire de cette bombe.
     *
     * @return Le joueur qui a posé cette bombe, ou null si aucun propriétaire
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * Définit le joueur propriétaire de cette bombe.
     *
     * @param owner Le joueur qui pose cette bombe
     */
    public void setOwner(Player owner) {
        this.owner = owner;
    }

    /**
     * Vérifie si la bombe est en train de voler.
     *
     * @return true si la bombe vole, false sinon
     */
    public boolean isFlying() {
        return isFlying;
    }

    /**
     * ⭐ NOUVEAU : Vérifie si la bombe est en train de glisser (coup de pied).
     *
     * @return true si la bombe glisse, false sinon
     */
    public boolean isMoving() {
        return isMoving;
    }

    /**
     * Retourne la direction X de vol de la bombe.
     *
     * @return Direction X (-1, 0, 1)
     */
    public int getFlyDirectionX() {
        return flyDirectionX;
    }

    /**
     * Retourne la direction Y de vol de la bombe.
     *
     * @return Direction Y (-1, 0, 1)
     */
    public int getFlyDirectionY() {
        return flyDirectionY;
    }

    /**
     * ⭐ NOUVEAU : Retourne la direction X de glissement de la bombe.
     *
     * @return Direction X (-1, 0, 1)
     */
    public int getKickDirectionX() {
        return kickDirectionX;
    }

    /**
     * ⭐ NOUVEAU : Retourne la direction Y de glissement de la bombe.
     *
     * @return Direction Y (-1, 0, 1)
     */
    public int getKickDirectionY() {
        return kickDirectionY;
    }
}