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

    /** Indique si la bombe glisse suite à un coup de pied (Kick Power) */
    private boolean isMoving = false;

    /** Direction X du glissement par coup de pied (-1, 0, 1) */
    private int kickDirectionX = 0;

    /** Direction Y du glissement par coup de pied (-1, 0, 1) */
    private int kickDirectionY = 0;

    /** Timer pour le mouvement par coup de pied */
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

    /**
     * Démarre le compte à rebours avant l'explosion.
     *
     * @param onExplode Action à exécuter lorsque la bombe explose
     */
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
     * @param directionX   Direction X (-1, 0, 1)
     * @param directionY   Direction Y (-1, 0, 1)
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
     * Fait glisser la bombe suite à un coup de pied (Kick Power).
     *
     * @param directionX   Direction X (-1, 0, 1)
     * @param directionY   Direction Y (-1, 0, 1)
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
     * Calcule la prochaine position de la bombe qui glisse sans la déplacer.
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
     *  Déplace la bombe vers sa prochaine position de glissement (appelé par le GameViewController).
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
     * Arrête le glissement de la bombe (collision détectée).
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

    /**
     * Retourne la position X actuelle de la bombe sur la grille.
     * @return la coordonnée X de la bombe
     */
    public int getX() { return x; }

    /**
     * Retourne la position Y actuelle de la bombe sur la grille.
     * @return la coordonnée Y de la bombe
     */
    public int getY() { return y; }

    /**
     * Retourne la valeur de dégâts de la bombe.
     * @return dégâts infligés par la bombe
     */
    public int getDamage() { return damage; }

    /**
     * Retourne la portée d'explosion de la bombe.
     * @return portée de la bombe
     */
    public int getRange() { return range; }

    /**
     * Retourne la dernière position X avant le dernier déplacement de la bombe.
     * @return précédente coordonnée X
     */
    public int getPreviousX() { return previousX; }

    /**
     * Retourne la dernière position Y avant le dernier déplacement de la bombe.
     * @return précédente coordonnée Y
     */
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
     * Indique si la bombe est actuellement en vol (lancée avec Glove).
     * @return true si la bombe vole, false sinon
     */
    public boolean isFlying() {
        return isFlying;
    }

    /**
     * Indique si la bombe glisse actuellement suite à un coup de pied (Kick Power).
     * @return true si la bombe glisse, false sinon
     */
    public boolean isMoving() {
        return isMoving;
    }

    /**
     * Retourne la direction X du vol de la bombe (-1, 0, 1).
     * @return Direction X de vol
     */
    public int getFlyDirectionX() {
        return flyDirectionX;
    }

    /**
     * Retourne la direction Y du vol de la bombe (-1, 0, 1).
     * @return Direction Y de vol
     */
    public int getFlyDirectionY() {
        return flyDirectionY;
    }

    /**
     * Retourne la direction X du glissement de la bombe (-1, 0, 1).
     * @return Direction X de glissement
     */
    public int getKickDirectionX() {
        return kickDirectionX;
    }

    /**
     * Retourne la direction Y du glissement de la bombe (-1, 0, 1).
     * @return Direction Y de glissement
     */
    public int getKickDirectionY() {
        return kickDirectionY;
    }

    /**
     * Vérifie si la bombe a explosé
     *
     * @return true si la bombe a explosé, false sinon
     */
    public boolean hasExploded() {
        return exploded;
    }

    /**
     * Définit l'état d'explosion de la bombe
     *
     * @param exploded nouvel état d'explosion
     */
    public void setExploded(boolean exploded) {
        exploded = exploded;
    }

}