package com.superbomberman.game;

import com.superbomberman.model.*;
import com.superbomberman.model.powerup.*;
import java.util.ArrayList;
import java.util.List;

import static com.superbomberman.model.MapLoader.player1;
import static com.superbomberman.model.MapLoader.player2;

/**
 * Gestionnaire des power-ups du jeu
 *
 * @author Jules Fuselier
 * @version 1.1 - Correction des malus manquants
 * @since 2025-06-08
 */
public class PowerUpManager {
    private List<PowerUp> activePowerUps = new ArrayList<>();

    /**
     * Ajoute un power-up à la liste des power-ups actifs
     */
    public void addPowerUp(PowerUp powerUp) {
        if (powerUp != null) {
            activePowerUps.add(powerUp);
            System.out.println("Power-up ajouté à la position (" + powerUp.getX() + ", " + powerUp.getY() + "): " + powerUp.getType());
        }
    }

    /**
     * Génère un power-up aléatoire à une position donnée (25% de chance)
     */
    public PowerUp generateRandomPowerUp(int x, int y) {
        if (Math.random() < 0.25) { // 25% de chance
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
     * Vérifie les collisions entre les joueurs et les power-ups
     */
    public void checkPlayerCollisions(Player player1, Player player2, GameStateManager gameStateManager, VisualRenderer visualRenderer) {
        checkPlayerOnPowerUp(player1, 1, gameStateManager, visualRenderer);

        if (player2 != null) {
            checkPlayerOnPowerUp(player2, 2, gameStateManager, visualRenderer);
        }
    }

    /**
     * Vérifie si un joueur est sur un power-up et l'applique
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
     * Applique l'effet d'un power-up sur un joueur
     */
    private void applyPowerUpEffect(Player player, PowerUp powerUp, int playerNumber, GameStateManager gameStateManager) {
        // Appliquer l'effet du power-up
        powerUp.applyTo(player);

        // Ajouter des points pour les power-ups collectés
        gameStateManager.updateScore(50);

        // Afficher les informations selon le type de power-up
        switch (powerUp.getType()) {
            case RANGE_UP -> {
                System.out.println("Joueur " + playerNumber + ": Range augmentée! (" + player.getExplosionRange() + ")");
            }
            case BOMB_UP -> {
                System.out.println("Joueur " + playerNumber + ": Bombes max augmentées! (" + player.getMaxBombs() + ")");
            }
            case SPEED_UP -> {
                System.out.println("Joueur " + playerNumber + ": Vitesse augmentée! (" + player.getSpeed() + ")");
            }
            case GLOVE -> {
                System.out.println("Joueur " + playerNumber + ": Glove activé! (" +
                        (playerNumber == 1 ? "SHIFT" : "CTRL") + " pour ramasser/lancer)");
            }
            case KICK -> {
                System.out.println("Joueur " + playerNumber + ": Kick activé! (marcher contre une bombe)");
            }
            case LINE_BOMB -> {
                System.out.println("Joueur " + playerNumber + ": LineBomb activé! (" +
                        (playerNumber == 1 ? "L" : "K") + ")");
            }
            case REMOTE -> {
                System.out.println("Joueur " + playerNumber + ": Remote activé! (" +
                        (playerNumber == 1 ? "R" : "O") + ")");
            }
            case SKULL -> {
                System.out.println("Joueur " + playerNumber + ": MALUS SKULL! Un effet négatif aléatoire a été appliqué!");
                // Le SkullMalus applique automatiquement un malus aléatoire via player.applyRandomMalus()
                displayCurrentMalus(player, playerNumber);
            }
            case BOMB_PASS -> {
                System.out.println("Joueur " + playerNumber + ": BombPass activé! (traverser ses propres bombes)");
            }
            case WALL_PASS -> {
                System.out.println("Joueur " + playerNumber + ": WallPass activé! (traverser les murs destructibles)");
            }
        }
    }

    /**
     * Affiche le malus actuel d'un joueur après avoir ramassé un SKULL
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

            long timeRemaining = player.getMalusTimeRemaining() / 1000; // Convertir en secondes
            System.out.println("Joueur " + playerNumber + ": Malus actif - " + malusDescription +
                    " (encore " + timeRemaining + " secondes)");
        }
    }

    /**
     * Supprime un power-up spécifique
     */
    public void removePowerUp(PowerUp powerUp) {
        activePowerUps.remove(powerUp);
    }

    /**
     * Supprime tous les power-ups d'une position donnée
     */
    public void removePowerUpsAt(int x, int y) {
        activePowerUps.removeIf(powerUp -> powerUp.getX() == x && powerUp.getY() == y);
    }

    /**
     * Vérifie s'il y a un power-up à une position donnée
     */
    public boolean hasPowerUpAt(int x, int y) {
        return activePowerUps.stream()
                .anyMatch(powerUp -> powerUp.getX() == x && powerUp.getY() == y);
    }

    /**
     * Obtient le power-up à une position donnée
     */
    public PowerUp getPowerUpAt(int x, int y) {
        return activePowerUps.stream()
                .filter(powerUp -> powerUp.getX() == x && powerUp.getY() == y)
                .findFirst()
                .orElse(null);
    }

    /**
     * Nettoie tous les power-ups (utile pour reset du jeu)
     */
    public void clearAllPowerUps() {
        activePowerUps.clear();
        System.out.println("Tous les power-ups ont été supprimés");
    }

    /**
     * Obtient des statistiques sur les power-ups actifs
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

    // Getters
    public List<PowerUp> getActivePowerUps() {
        return new ArrayList<>(activePowerUps); // Retourne une copie pour éviter les modifications externes
    }

    public int getActivePowerUpCount() {
        return activePowerUps.size();
    }

    /**
     * Vérifie si un joueur peut collecter des power-ups (pas en malus bloquant par exemple)
     */
    public boolean canPlayerCollectPowerUps(Player player) {
        // Logique pour déterminer si le joueur peut collecter des power-ups
        // Pour l'instant, tous les joueurs peuvent collecter (même avec des malus)
        return true;
    }
}