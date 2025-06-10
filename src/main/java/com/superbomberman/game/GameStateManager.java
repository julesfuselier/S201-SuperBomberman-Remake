package com.superbomberman.game;

import com.superbomberman.model.User;
import com.superbomberman.service.AuthService;

import static com.superbomberman.model.MapLoader.enemy;
import static com.superbomberman.model.MapLoader.player1;
import static com.superbomberman.model.MapLoader.player2;
import static com.superbomberman.controller.MenuController.isOnePlayer;

/**
 * Gestionnaire de l'état du jeu et des statistiques
 *
 * @author Jules Fuselier
 * @version 2.0 - Intégration ScoreSystem
 * @since 2025-06-08
 */
public class GameStateManager {
    private User currentUser;
    private AuthService authService;
    private int gameScore = 0;
    private boolean gameWon = false;
    private long gameStartTime;

    // 🆕 NOUVEAU : Système de score avancé
    private ScoreSystem scoreSystem;

    public GameStateManager(User currentUser, AuthService authService) {
        this.currentUser = currentUser;
        this.authService = authService;
        this.gameStartTime = System.currentTimeMillis();

        // 🆕 Initialiser le système de score
        this.scoreSystem = new ScoreSystem(this);
    }

    /**
     * Met à jour le score du jeu
     */
    public void updateScore(int points) {
        gameScore += points;
        System.out.println("Score actuel: " + gameScore);
    }

    /**
     * Marque le jeu comme gagné ou perdu
     */
    public void setGameWon(boolean won) {
        this.gameWon = won;
        if (won) {
            // 🆕 Calculer le bonus de temps quand le niveau est terminé
            long gameEndTime = System.currentTimeMillis();
            int usedTimeSeconds = (int) ((gameEndTime - gameStartTime) / 1000);
            int maxTimeSeconds = 120; // 2 minutes par défaut

            scoreSystem.finishLevel(maxTimeSeconds, usedTimeSeconds);

            System.out.println("🎉 Victoire ! Score final: " + gameScore);
            endGame();
        }
    }

    /**
     * Termine le jeu et met à jour les statistiques utilisateur
     */
    public void endGame() {
        if (currentUser != null && authService != null) {
            authService.updateUserStats(currentUser, gameWon, gameScore);
            System.out.println("Statistiques mises à jour pour " + currentUser.getUsername());
            System.out.println("Score final: " + gameScore + " | Victoire: " + (gameWon ? "Oui" : "Non"));

            // 🆕 Afficher le résumé du score
            scoreSystem.displayScoreSummary();
        }
    }

    /**
     * Vérifie les conditions de fin de jeu
     */
    public void checkGameConditions() {
        // Exemple de conditions de victoire - à adapter selon votre logique
        if (enemy != null && isEnemyDefeated()) {
            setGameWon(true);
        }

        // Vérifier si le joueur est toujours en vie
        if (isPlayerDefeated()) {
            setGameWon(false);
            endGame();
        }
    }

    /**
     * Vérifie si l'ennemi est vaincu
     */
    private boolean isEnemyDefeated() {
        // 🆕 Vérifier si l'ennemi est mort
        return enemy != null && enemy.isDead();
    }

    /**
     * Vérifie si le joueur est vaincu
     */
    private boolean isPlayerDefeated() {
        // Logique pour déterminer si le joueur est vaincu
        // Par exemple, si le joueur est touché par une explosion
        return false; // Placeholder - à implémenter selon votre logique
    }

    // Getters
    public int getGameScore() {
        return gameScore;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public long getGameStartTime() {
        return gameStartTime;
    }

    //  Getter pour le système de score
    public ScoreSystem getScoreSystem() {
        return scoreSystem;
    }
}