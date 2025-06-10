package com.superbomberman.game;

import com.superbomberman.model.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Système de score complet pour Super Bomberman
 * Gère les points, combos, bonus de temps et vies supplémentaires
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-10
 */
public class ScoreSystem {

    // Configuration des points
    private static final int POINTS_WALL_DESTROYED = 10;
    private static final int POINTS_POWERUP_COLLECTED = 50;
    private static final int POINTS_ENEMY_KILLED = 100;

    // Configuration des bonus de temps
    private static final int TIME_BONUS_MULTIPLIER = 10;

    // Configuration des vies supplémentaires
    private static final int EXTRA_LIFE_THRESHOLD = 10000; // Tous les 10 000 points

    // État du système
    // Remplace le score global par un score par joueur
    private Map<Player, Integer> playerScores = new HashMap<>();
    private Map<Player, List<Integer>> playerCombos = new HashMap<>();
    private Map<Player, Integer> playerLivesEarned = new HashMap<>();
    private GameStateManager gameStateManager;

    public ScoreSystem(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }

    /**
     * 🎯 Ajoute des points pour un ennemi tué
     */
    public void addEnemyKilled(Player player) {
        playerCombos.computeIfAbsent(player, k -> new ArrayList<>()).add(POINTS_ENEMY_KILLED);
        System.out.println("💀 Ennemi tué par " + player + " : +" + POINTS_ENEMY_KILLED + " points");
    }

    /**
     * 💥 Traite le combo d'une explosion (appelé après toutes les morts)
     */
    public void processExplosionCombo(Player player) {
        List<Integer> comboList = playerCombos.getOrDefault(player, new ArrayList<>());
        if (comboList.isEmpty()) {
            return;
        }

        int totalPoints = 0;
        int enemyCount = comboList.size();

        // Calculer les points avec bonus de combo
        for (int i = 0; i < comboList.size(); i++) {
            int basePoints = comboList.get(i);
            int comboMultiplier = i + 1; // 1er ennemi = x1, 2e = x2, etc.
            int points = basePoints * comboMultiplier;
            totalPoints += points;

            System.out.println("🔥 Combo x" + comboMultiplier + " : " + basePoints + " -> " + points + " points");
        }

        // Afficher le combo si multiple
        if (enemyCount > 1) {
            System.out.println("🎊 COMBO " + enemyCount + " ENNEMIS pour " + player + " ! Total : +" + totalPoints + " points");
        }

        addScore(player, totalPoints);
        comboList.clear();
    }

    /**
     * 🏆 Calcule et ajoute le bonus de temps
     */
    public void calculateTimeBonus(Player player, int maxTimeSeconds, int usedTimeSeconds) {
        int remainingTime = Math.max(0, maxTimeSeconds - usedTimeSeconds);
        int timeBonus = remainingTime * TIME_BONUS_MULTIPLIER;

        if (timeBonus > 0) {
            System.out.println("⏱️ Bonus de temps pour " + player + " : " + remainingTime + "s × " + TIME_BONUS_MULTIPLIER + " = +" + timeBonus + " points");
            addScore(player, timeBonus);
        }
    }

    /**
     * 🎁 Ajoute des points pour un power-up collecté
     */
    public void addPowerUpCollected(Player player) {
        addScore(player, POINTS_POWERUP_COLLECTED);
        System.out.println("✨ Power-up collecté par " + player + " : +" + POINTS_POWERUP_COLLECTED + " points");
    }

    /**
     * 🧱 Ajoute des points pour un mur détruit
     */
    public void addWallDestroyed(Player player) {
        addScore(player, POINTS_WALL_DESTROYED);
        System.out.println("🧱 Mur détruit par " + player + " : +" + POINTS_WALL_DESTROYED + " points");
    }

    /**
     * 🏁 Termine le niveau et calcule tous les bonus
     */
    public void finishLevel(int maxTimeSeconds, int usedTimeSeconds) {
        // Finaliser les combos en cours
        for (Player player : playerCombos.keySet()) {
            processExplosionCombo(player);
        }

        // Calculer le bonus de temps
        for (Player player : playerScores.keySet()) {
            calculateTimeBonus(player, maxTimeSeconds, usedTimeSeconds);
        }

        System.out.println("🎉 Niveau terminé !");
    }

    /**
     * 📊 Méthode privée pour ajouter du score et vérifier les vies
     */
    private void addScore(Player player, int points) {
        int newScore = playerScores.getOrDefault(player, 0) + points;
        playerScores.put(player, newScore);

        // Gestion des vies supplémentaires (optionnel)
        int lives = newScore / EXTRA_LIFE_THRESHOLD;
        if (lives > playerLivesEarned.getOrDefault(player, 0)) {
            playerLivesEarned.put(player, lives);
            System.out.println("❤️ Vie supplémentaire gagnée par " + player + " !");
        }

        // Mettre à jour le GameStateManager
        if (gameStateManager != null) {
            gameStateManager.updateScore(points);
        }

        System.out.println("Score actuel de " + player + " : " + newScore);
    }

    /**
     * 💚 Vérifie si le joueur a gagné des vies supplémentaires
     */
    private void checkExtraLives(int oldScore, int newScore) {
        int oldLives = oldScore / EXTRA_LIFE_THRESHOLD;
        int newLives = newScore / EXTRA_LIFE_THRESHOLD;
        // Cette méthode n'est plus utilisée, car la gestion des vies se fait par joueur dans playerLivesEarned
        // Elle peut être supprimée si non utilisée ailleurs
    }

    /**
     * 🎮 Remet à zéro le système de score
     */
    public void reset() {
        playerScores.clear();
        playerCombos.clear();
        playerLivesEarned.clear();
        System.out.println("🔄 Système de score remis à zéro");
    }

    // Getters
    public int getScore(Player player) {
        return playerScores.getOrDefault(player, 0);
    }

    /**
     * 📋 Affiche un résumé du score
     */
    public void displayScoreSummary() {
        System.out.println("=== 📊 RÉSUMÉ DU SCORE ===");
        for (Map.Entry<Player, Integer> entry : playerScores.entrySet()) {
            System.out.println("Joueur " + entry.getKey() + " : " + entry.getValue() + " points");
        }
        System.out.println("========================");
    }

    /**
     * 📋 Méthodes utilitaires pour initialiser les joueurs
     */
    public void registerPlayer(Player player) {
        playerScores.putIfAbsent(player, 0);
        playerCombos.putIfAbsent(player, new ArrayList<>());
        playerLivesEarned.putIfAbsent(player, 0);
    }
}