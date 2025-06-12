/**
 * Gestionnaire des power-ups du jeu Super Bomberman.
 * <p>
 * Gère la génération, l'application, la gestion et la suppression des power-ups sur la carte.
 * Vérifie les collisions entre joueurs et power-ups, applique les effets (bonus ou malus), et
 * interagit avec le système de score pour comptabiliser les collectes.
 * </p>
 *
 * <ul>
 *     <li>Génération aléatoire de power-ups lors de la destruction de murs</li>
 *     <li>Vérification des collisions joueurs/power-ups</li>
 *     <li>Application des effets selon le type de power-up</li>
 *     <li>Gestion des malus (SKULL)</li>
 *     <li>Statistiques et nettoyage des power-ups</li>
 * </ul>
 *
 * @author Jules Fuselier
 * @version 1.1 - Correction des malus manquants
 * @since 2025-06-08
 */
package com.superbomberman.game;

import com.superbomberman.model.*;
import com.superbomberman.model.powerup.*;
import java.util.ArrayList;
import java.util.List;

import static com.superbomberman.model.MapLoader.player1;
import static com.superbomberman.model.MapLoader.player2;

/**
 * Gestionnaire central des power-ups : ajout, détection collision, application effet, suppression.
 */
public class PowerUpManager {
    /** Liste des power-ups actifs sur la carte. */
    private List<PowerUp> activePowerUps = new ArrayList<>();
    /** Système de score associé (pour la collecte de power-ups). */
    private ScoreSystem scoreSystem;

    /**
     * Ajoute un power-up à la liste des power-ups actifs.
     * @param powerUp Le power-up à ajouter
     */
    public void addPowerUp(PowerUp powerUp) {
        if (powerUp != null) {
            activePowerUps.add(powerUp);
            System.out.println("Power-up ajouté à la position (" + powerUp.getX() + ", " + powerUp.getY() + "): " + powerUp.getType());
        }
    }

    /**
     * Génère un power-up aléatoire à une position donnée (25% de chance).
     * @param x abscisse
     * @param y ordonnée
     * @return Le power-up généré, ou null si aucun
     */
    public PowerUp generateRandomPowerUp(int x, int y) {
        if (Math.random() < 0.25) {
            try {
                PowerUpType type = PowerUpType.randomType();
                PowerUp powerUp = PowerUpFactory.create(type, x, y);

                if (powerUp != null) {
                    addPowerUp(powerUp);
                    System.out.println("Power-up généré: " + type + " à (" + x + ", " + y + ")");
                }
                return powerUp;
            } catch (Exception e) {
                System.err.println("Erreur lors de la création du power-up: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Vérifie les collisions entre les joueurs et les power-ups.
     * Applique l'effet au joueur concerné et l'enlève de la carte.
     * @param player1 Joueur 1
     * @param player2 Joueur 2
     * @param gameStateManager Gestionnaire état de partie (pour score)
     * @param visualRenderer Pour affichage/suppression visuelle des power-ups
     */
    public void checkPlayerCollisions(Player player1, Player player2, GameStateManager gameStateManager, VisualRenderer visualRenderer) {
        if (gameStateManager != null && scoreSystem == null) {
            scoreSystem = gameStateManager.getScoreSystem();
        }
        checkPlayerOnPowerUp(player1, 1, gameStateManager, visualRenderer);
        if (player2 != null) {
            checkPlayerOnPowerUp(player2, 2, gameStateManager, visualRenderer);
        }
    }

    /**
     * Vérifie si un joueur est sur un power-up et applique son effet.
     * @param player Joueur à vérifier
     * @param playerNumber Numéro du joueur (1 ou 2)
     * @param gameStateManager Gestionnaire état du jeu
     * @param visualRenderer Pour affichage/suppression visuelle
     */
    private void checkPlayerOnPowerUp(Player player, int playerNumber, GameStateManager gameStateManager, VisualRenderer visualRenderer) {
        PowerUp toCollect = null;

        for (PowerUp powerUp : activePowerUps) {
            if (powerUp.getX() == player.getX() && powerUp.getY() == player.getY()) {
                toCollect = powerUp;
                break;
            }
        }

        if (toCollect != null) {
            System.out.println("Joueur " + playerNumber + ": Power-up collecté: " + toCollect.getType());
            applyPowerUpEffect(player, toCollect, playerNumber, gameStateManager);
            visualRenderer.removePowerUpVisual(toCollect);
            activePowerUps.remove(toCollect);
        }
    }

    /**
     * Applique l'effet d'un power-up sur un joueur (bonus ou malus).
     * @param player Joueur concerné
     * @param powerUp Power-up collecté
     * @param playerNumber Numéro du joueur (1 ou 2)
     * @param gameStateManager Gestionnaire état
     */
    private void applyPowerUpEffect(Player player, PowerUp powerUp, int playerNumber, GameStateManager gameStateManager) {
        powerUp.applyTo(player);

        if (scoreSystem != null) {
            scoreSystem.addPowerUpCollected(player);
        }

        // Affiche l'effet selon le type
        switch (powerUp.getType()) {
            case RANGE_UP -> System.out.println("Joueur " + playerNumber + ": Range augmentée! (" + player.getExplosionRange() + ")");
            case BOMB_UP -> System.out.println("Joueur " + playerNumber + ": Bombes max augmentées! (" + player.getMaxBombs() + ")");
            case SPEED_UP -> System.out.println("Joueur " + playerNumber + ": Vitesse augmentée! (" + player.getSpeed() + ")");
            case GLOVE -> System.out.println("Joueur " + playerNumber + ": Glove activé! (" +
                    (playerNumber == 1 ? "SHIFT" : "CTRL") + " pour ramasser/lancer)");
            case KICK -> System.out.println("Joueur " + playerNumber + ": Kick activé! (marcher contre une bombe)");
            case LINE_BOMB -> System.out.println("Joueur " + playerNumber + ": LineBomb activé! (" +
                    (playerNumber == 1 ? "L" : "K") + ")");
            case REMOTE -> System.out.println("Joueur " + playerNumber + ": Remote activé! (" +
                    (playerNumber == 1 ? "R" : "O") + ")");
            case SKULL -> {
                System.out.println("Joueur " + playerNumber + ": MALUS SKULL! Un effet négatif aléatoire a été appliqué!");
                displayCurrentMalus(player, playerNumber);
            }
            case BOMB_PASS -> System.out.println("Joueur " + playerNumber + ": BombPass activé! (traverser ses propres bombes)");
            case WALL_PASS -> System.out.println("Joueur " + playerNumber + ": WallPass activé! (traverser les murs destructibles)");
        }
    }

    /**
     * Affiche le malus actuel d'un joueur après avoir ramassé un SKULL.
     * @param player Joueur concerné
     * @param playerNumber Numéro
     */
    private void displayCurrentMalus(Player player, int playerNumber) {
        MalusType currentMalus = player.getCurrentMalus();
        if (currentMalus != null) {
            String malusDescription = switch (currentMalus) {
                case NO_BOMB -> "Impossible de poser des bombes";
                case REVERSED_CONTROLS -> "Contrôles inversés (gauche/droite, haut/bas)";
                case AUTO_BOMB -> "Bombes posées automatiquement à intervalles réguliers";
                case SLOW_SPEED -> "Vitesse de déplacement considérablement réduite";
                case SUPER_FAST -> "Vitesse de déplacement incontrôlablement rapide";
                case REDUCED_RANGE -> "Portée d'explosion des bombes réduite";
            };

            long timeRemaining = player.getMalusTimeRemaining() / 1000;
            System.out.println("Joueur " + playerNumber + ": Malus actif - " + malusDescription +
                    " (encore " + timeRemaining + " secondes)");
        }
    }

    /**
     * Supprime un power-up spécifique.
     * @param powerUp à supprimer
     */
    public void removePowerUp(PowerUp powerUp) {
        activePowerUps.remove(powerUp);
    }

    /**
     * Supprime tous les power-ups à une position donnée.
     * @param x abscisse
     * @param y ordonnée
     */
    public void removePowerUpsAt(int x, int y) {
        activePowerUps.removeIf(powerUp -> powerUp.getX() == x && powerUp.getY() == y);
    }

    /**
     * Vérifie la présence d'un power-up sur une case.
     * @param x abscisse
     * @param y ordonnée
     * @return true si power-up présent
     */
    public boolean hasPowerUpAt(int x, int y) {
        return activePowerUps.stream()
                .anyMatch(powerUp -> powerUp.getX() == x && powerUp.getY() == y);
    }

    /**
     * Retourne le power-up à une position donnée (ou null).
     * @param x abscisse
     * @param y ordonnée
     * @return power-up ou null
     */
    public PowerUp getPowerUpAt(int x, int y) {
        return activePowerUps.stream()
                .filter(powerUp -> powerUp.getX() == x && powerUp.getY() == y)
                .findFirst()
                .orElse(null);
    }

    /**
     * Supprime tous les power-ups (pour reset).
     */
    public void clearAllPowerUps() {
        activePowerUps.clear();
        System.out.println("Tous les power-ups ont été supprimés");
    }

    /**
     * Affiche des statistiques sur les power-ups actifs.
     */
    public void printPowerUpStats() {
        System.out.println("=== STATISTIQUES POWER-UPS ===");
        System.out.println("Nombre total de power-ups actifs: " + activePowerUps.size());

        for (PowerUpType type : PowerUpType.values()) {
            long count = activePowerUps.stream()
                    .filter(powerUp -> powerUp.getType() == type)
                    .count();
            if (count > 0) {
                System.out.println(type + ": " + count);
            }
        }
        System.out.println("===============================");
    }

    // === GETTERS ===

    /** @return copie de la liste des power-ups actifs */
    public List<PowerUp> getActivePowerUps() {
        return new ArrayList<>(activePowerUps);
    }

    /** @return nombre de power-ups actifs */
    public int getActivePowerUpCount() {
        return activePowerUps.size();
    }

    /**
     * Détermine si un joueur peut collecter des power-ups (logique extensible).
     * @param player Joueur concerné
     * @return true si collectable
     */
    public boolean canPlayerCollectPowerUps(Player player) {
        // Par défaut : tous les joueurs peuvent collecter (même avec malus)
        return true;
    }
}