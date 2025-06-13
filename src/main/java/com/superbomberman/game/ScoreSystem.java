package com.superbomberman.game;

import com.superbomberman.model.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Système de score complet pour Super Bomberman.
 * <p>
 * Cette classe gère les points, les combos, les bonus de temps et les vies supplémentaires
 * pour chaque joueur. Elle intègre la gestion des événements de score lors des actions du jeu
 * (ennemi tué, mur détruit, power-up ramassé, combos, etc.) et fournit des utilitaires pour
 * l'affichage et la remise à zéro des scores.
 * </p>
 *
 * <ul>
 *     <li>Points attribués pour chaque action (ennemis, power-ups, murs...)</li>
 *     <li>Gestion des combos d'explosion avec multiplicateur</li>
 *     <li>Gestion des vies supplémentaires à chaque palier de score</li>
 *     <li>Affichage et accès aux scores individuels</li>
 * </ul>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-10
 */
public class ScoreSystem {

    // --- Configuration des points ---
    private static final int POINTS_WALL_DESTROYED = 10;
    private static final int POINTS_POWERUP_COLLECTED = 50;
    private static final int POINTS_ENEMY_KILLED = 100;

    // --- Configuration des bonus de temps ---
    private static final int TIME_BONUS_MULTIPLIER = 10;

    // --- Configuration des vies supplémentaires ---
    private static final int EXTRA_LIFE_THRESHOLD = 10000; // Tous les 10 000 points

    // --- État du système ---
    /** Scores par joueur */
    private Map<Player, Integer> playerScores = new HashMap<>();
    /** Liste des points des combos en attente par joueur */
    private Map<Player, List<Integer>> playerCombos = new HashMap<>();
    /** Nombre de vies supplémentaires gagnées par joueur (calculé sur la base du score) */
    private Map<Player, Integer> playerLivesEarned = new HashMap<>();
    /** Référence vers le GameStateManager pour notification */
    private GameStateManager gameStateManager;

    /**
     * Crée un système de score lié à un GameStateManager.
     * @param gameStateManager Gestionnaire d'état de partie
     */
    public ScoreSystem(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }

    /**
     * Ajoute des points pour un ennemi tué (points en attente de combo).
     * @param player Joueur ayant tué l'ennemi
     */
    public void addEnemyKilled(Player player) {
        playerCombos.computeIfAbsent(player, k -> new ArrayList<>()).add(POINTS_ENEMY_KILLED);
        System.out.println("💀 Ennemi tué par " + player + " : +" + POINTS_ENEMY_KILLED + " points");
    }

    /**
     * Traite le combo d'une explosion (appelé après toutes les morts).
     * Applique un multiplicateur en fonction du nombre d'ennemis tués dans la même explosion.
     * @param player Joueur concerné
     */
    public void processExplosionCombo(Player player) {
        List<Integer> comboList = playerCombos.getOrDefault(player, new ArrayList<>());
        if (comboList.isEmpty()) {
            return;
        }

        int totalPoints = 0;
        int enemyCount = comboList.size();

        // Calcul des points avec multiplicateur de combo
        for (int i = 0; i < comboList.size(); i++) {
            int basePoints = comboList.get(i);
            int comboMultiplier = i + 1; // 1er ennemi = x1, 2e = x2, etc.
            int points = basePoints * comboMultiplier;
            totalPoints += points;

            System.out.println("🔥 Combo x" + comboMultiplier + " : " + basePoints + " -> " + points + " points");
        }

        // Affichage spécial si plusieurs ennemis touchés
        if (enemyCount > 1) {
            System.out.println("🎊 COMBO " + enemyCount + " ENNEMIS pour " + player + " ! Total : +" + totalPoints + " points");
        }

        addScore(player, totalPoints);
        comboList.clear();
    }

    /**
     * Ajoute des points pour un power-up collecté.
     * @param player Joueur concerné
     */
    public void addPowerUpCollected(Player player) {
        addScore(player, POINTS_POWERUP_COLLECTED);
        System.out.println("✨ Power-up collecté par " + player + " : +" + POINTS_POWERUP_COLLECTED + " points");
    }

    /**
     * Ajoute des points pour un mur détruit.
     * @param player Joueur concerné
     */
    public void addWallDestroyed(Player player) {
        addScore(player, POINTS_WALL_DESTROYED);
        System.out.println("🧱 Mur détruit par " + player + " : +" + POINTS_WALL_DESTROYED + " points");
    }

    /**
     * Termine le niveau et calcule tous les bonus (combos, temps...).
     * @param maxTimeSeconds Temps maximal du niveau
     * @param usedTimeSeconds Temps utilisé par le joueur
     */
    public void finishLevel(int maxTimeSeconds, int usedTimeSeconds) {
        // Finaliser les combos en cours
        for (Player player : playerCombos.keySet()) {
            processExplosionCombo(player);
        }

        // Calculer le bonus de temps pour chaque joueur (décommenter si besoin)
//        for (Player player : playerScores.keySet()) {
//            calculateTimeBonus(player, maxTimeSeconds, usedTimeSeconds);
//        }

        System.out.println("🎉 Niveau terminé !");
    }

    /**
     * Méthode privée pour ajouter du score et vérifier les vies supplémentaires.
     * @param player Joueur concerné
     * @param points Points à ajouter
     */
    private void addScore(Player player, int points) {
        int newScore = playerScores.getOrDefault(player, 0) + points;
        playerScores.put(player, newScore);

        // Gestion des vies supplémentaires
        int lives = newScore / EXTRA_LIFE_THRESHOLD;
        if (lives > playerLivesEarned.getOrDefault(player, 0)) {
            playerLivesEarned.put(player, lives);
            System.out.println("❤️ Vie supplémentaire gagnée par " + player + " !");
        }

        // Notifier le GameStateManager
        if (gameStateManager != null) {
            gameStateManager.updateScore(points);
        }

        System.out.println("Score actuel de " + player + " : " + newScore);
    }

    /**
     * Vérifie si le joueur a gagné des vies supplémentaires (obsolète, voir playerLivesEarned).
     */
    @Deprecated
    private void checkExtraLives(int oldScore, int newScore) {
        int oldLives = oldScore / EXTRA_LIFE_THRESHOLD;
        int newLives = newScore / EXTRA_LIFE_THRESHOLD;
        // Cette méthode n'est plus utilisée, car la gestion des vies se fait par joueur dans playerLivesEarned
    }

    /**
     * Remet à zéro le système de score.
     */
    public void reset() {
        playerScores.clear();
        playerCombos.clear();
        playerLivesEarned.clear();
        System.out.println("🔄 Système de score remis à zéro");
    }

    // --- Getters et utilitaires ---

    /**
     * Récupère le score d'un joueur.
     * @param player Joueur concerné
     * @return Score du joueur
     */
    public int getScore(Player player) {
        return playerScores.getOrDefault(player, 0);
    }

    /**
     * Alias pour récupérer le score d'un joueur.
     * @param player Joueur concerné
     * @return Score du joueur
     */
    public int getPlayerScore(Player player) {
        return playerScores.getOrDefault(player, 0);
    }

    /**
     * 📋 Affiche un résumé du score de chaque joueur.
     */
    public void displayScoreSummary() {
        System.out.println("=== 📊 RÉSUMÉ DU SCORE ===");
        for (Map.Entry<Player, Integer> entry : playerScores.entrySet()) {
            System.out.println("Joueur " + entry.getKey() + " : " + entry.getValue() + " points");
        }
        System.out.println("========================");
    }

    /**
     * Initialise un joueur dans le système de score (à appeler lors de la création).
     * @param player Joueur à enregistrer
     */
    public void registerPlayer(Player player) {
        playerScores.putIfAbsent(player, 0);
        playerCombos.putIfAbsent(player, new ArrayList<>());
        playerLivesEarned.putIfAbsent(player, 0);
    }
}