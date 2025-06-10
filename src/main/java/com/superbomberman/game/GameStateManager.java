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
 * @version 2.0
 * @since 2025-06-10
 */
public class GameStateManager {
    private User currentUser;
    private AuthService authService;
    private int gameScore = 0;
    private boolean gameWon = false;
    private boolean gameEnded = false;
    private long gameStartTime;

    // PROPRIÉTÉS POUR LA GESTION DE FIN DE PARTIE
    private GameEndReason endReason = null;
    private int winnerPlayer = 0; // 0 = aucun, 1 = joueur 1, 2 = joueur 2
    private boolean isDraw = false;

    // Optionnel : Timer de partie (pour mode chrono)
    private long gameTimeLimit = 0; // 0 = pas de limite
    private boolean hasTimeLimit = false;

    public enum GameEndReason {
        PLAYER_VICTORY,     // Un joueur a gagné
        PLAYER_DEFEAT,      // Le joueur a perdu (solo)
        MUTUAL_DESTRUCTION, // Tous les joueurs morts en même temps
        TIME_UP,           // Temps écoulé
        ENEMY_DEFEATED     // Ennemi vaincu (mode solo)
    }

    public GameStateManager(User currentUser, AuthService authService) {
        this.currentUser = currentUser;
        this.authService = authService;
        this.gameStartTime = System.currentTimeMillis();
    }

    /**
     * Met à jour le score du jeu
     */
    public void updateScore(int points) {
        gameScore += points;
        System.out.println("Score actuel: " + gameScore);
    }

    /**
     *  Vérifie toutes les conditions de fin de jeu
     */
    public void checkGameConditions() {
        if (gameEnded) return; // Ne pas vérifier si le jeu est déjà terminé

        // 1. Vérifier le timer (si activé)
        if (hasTimeLimit && isTimeUp()) {
            endGameByTime();
            return;
        }

        // 2. Vérifier les conditions selon le mode de jeu
        if (isOnePlayer) {
            checkSoloGameConditions();
        } else {
            checkMultiplayerGameConditions();
        }
    }

    /**
     *  Vérifie les conditions de fin pour le mode solo
     */
    private void checkSoloGameConditions() {
        // Vérifier si le joueur 1 est mort
        if (player1 != null && player1.isDead()) {
            endGame(GameEndReason.PLAYER_DEFEAT, 0, false);
            return;
        }

        // Vérifier si l'ennemi est vaincu
        if (enemy != null && isEnemyDefeated()) {
            endGame(GameEndReason.ENEMY_DEFEATED, 1, true);
            return;
        }
    }

    /**
     * ✨ Vérifie les conditions de fin pour le mode 2 joueurs
     */
    private void checkMultiplayerGameConditions() {
        boolean player1Alive = player1 != null && player1.isAlive();
        boolean player2Alive = player2 != null && player2.isAlive();

        // Cas 1: Les deux joueurs sont morts → Égalité
        if (!player1Alive && !player2Alive) {
            endGame(GameEndReason.MUTUAL_DESTRUCTION, 0, false);
            return;
        }

        // Cas 2: Seul le joueur 1 survit → Joueur 1 gagne
        if (player1Alive && !player2Alive) {
            endGame(GameEndReason.PLAYER_VICTORY, 1, true);
            return;
        }

        // Cas 3: Seul le joueur 2 survit → Joueur 2 gagne
        if (!player1Alive && player2Alive) {
            endGame(GameEndReason.PLAYER_VICTORY, 2, true);
            return;
        }

        // Cas 4: Les deux sont vivants → Le jeu continue
    }

    /**
     * ✨ Termine le jeu par écoulement du temps
     */
    private void endGameByTime() {
        if (isOnePlayer) {
            // En solo, si le temps est écoulé → défaite
            endGame(GameEndReason.TIME_UP, 0, false);
        } else {
            // En multijoueur, déterminer le gagnant selon les critères
            // Option 1: Égalité
            endGame(GameEndReason.TIME_UP, 0, false);

            // Option 2: Gagnant selon le score, les vies, etc.
            // int winner = determineWinnerByScore();
            // endGame(GameEndReason.TIME_UP, winner, winner > 0);
        }
    }

    /**
     * ✨ MÉTHODE CENTRALE - Termine le jeu avec une raison spécifique
     */
    private void endGame(GameEndReason reason, int winner, boolean victory) {
        if (gameEnded) return; // Éviter les appels multiples

        this.gameEnded = true;
        this.endReason = reason;
        this.winnerPlayer = winner;
        this.gameWon = victory;
        this.isDraw = (winner == 0 && reason == GameEndReason.MUTUAL_DESTRUCTION);

        // Afficher le résultat
        displayGameResult();

        // Mettre à jour les statistiques
        updatePlayerStats();

        // Déclencher les effets de fin de partie
        triggerEndGameEffects();
    }

    /**
     * ✨ Affiche le résultat de la partie
     */
    private void displayGameResult() {
        System.out.println("========================================");
        System.out.println("🎮 FIN DE PARTIE 🎮");
        System.out.println("========================================");

        switch (endReason) {
            case PLAYER_VICTORY -> {
                System.out.println("🏆 VICTOIRE ! Joueur " + winnerPlayer + " a gagné !");
            }
            case PLAYER_DEFEAT -> {
                System.out.println("💀 DÉFAITE ! Vous avez été éliminé !");
            }
            case MUTUAL_DESTRUCTION -> {
                System.out.println("☠️ ÉGALITÉ ! Tous les joueurs ont été éliminés !");
            }
            case TIME_UP -> {
                if (winnerPlayer > 0) {
                    System.out.println("⏰ TEMPS ÉCOULÉ ! Joueur " + winnerPlayer + " gagne !");
                } else {
                    System.out.println("⏰ TEMPS ÉCOULÉ ! Aucun gagnant !");
                }
            }
            case ENEMY_DEFEATED -> {
                System.out.println("🎉 VICTOIRE ! Ennemi vaincu !");
            }
        }

        System.out.println("Score final: " + gameScore);
        System.out.println("Durée de la partie: " + getGameDurationSeconds() + " secondes");
        System.out.println("========================================");
    }

    /**
     * ✨ Met à jour les statistiques du joueur
     */
    private void updatePlayerStats() {
        if (currentUser != null && authService != null) {
            authService.updateUserStats(currentUser, gameWon, gameScore);
            System.out.println("📊 Statistiques mises à jour pour " + currentUser.getUsername());
        }
    }

    /**
     * ✨ Déclenche les effets visuels/sonores de fin de partie
     */
    private void triggerEndGameEffects() {
        // TODO: À implémenter dans l'étape suivante
        // - Animation de victoire/défaite
        // - Sons de fin de partie
        // - Transition vers l'écran de résultats
        System.out.println("🎬 Déclenchement des effets de fin de partie...");
    }

    // ✨ MÉTHODES UTILITAIRES

    /**
     * Active un timer pour la partie
     */
    public void setGameTimeLimit(long timeLimitInSeconds) {
        this.gameTimeLimit = timeLimitInSeconds * 1000; // Convertir en millisecondes
        this.hasTimeLimit = true;
        System.out.println("⏱️ Timer activé: " + timeLimitInSeconds + " secondes");
    }

    /**
     * Vérifie si le temps est écoulé
     */
    public boolean isTimeUp() {
        if (!hasTimeLimit) return false;
        return (System.currentTimeMillis() - gameStartTime) >= gameTimeLimit;
    }

    /**
     * Retourne le temps restant en secondes
     */
    public long getRemainingTimeSeconds() {
        if (!hasTimeLimit) return -1;
        long remaining = gameTimeLimit - (System.currentTimeMillis() - gameStartTime);
        return Math.max(0, remaining / 1000);
    }

    /**
     * Retourne la durée de la partie en secondes
     */
    public long getGameDurationSeconds() {
        return (System.currentTimeMillis() - gameStartTime) / 1000;
    }

    /**
     * Force la fin de partie (pour debug ou cas spéciaux)
     */
    public void forceEndGame(GameEndReason reason, int winner) {
        endGame(reason, winner, winner > 0);
    }

    // Méthodes existantes mises à jour
    public void setGameWon(boolean won) {
        // Cette méthode est conservée pour compatibilité
        if (!gameEnded) {
            endGame(won ? GameEndReason.PLAYER_VICTORY : GameEndReason.PLAYER_DEFEAT,
                    won ? 1 : 0, won);
        }
    }

    /**
     *  Vérifie si l'ennemi est vaincu
     */
    private boolean isEnemyDefeated() {
        // Vérifier si l'ennemi existe et s'il est mort
        if (enemy == null) {
            return false; // Pas d'ennemi = pas de victoire par ennemi vaincu
        }

        return enemy.isDead(); // Utilise la méthode isDead() déjà présente dans Enemy.java
    }

    /**
     *  Vérifie si le joueur est vaincu
     */
    private boolean isPlayerDefeated() {
        if (isOnePlayer) {
            // Mode solo : vérifier si le joueur 1 est mort
            return player1 != null && player1.isDead();
        } else {
            // Mode 2 joueurs : vérifier si les deux joueurs sont morts
            boolean player1Dead = player1 == null || player1.isDead();
            boolean player2Dead = player2 == null || player2.isDead();
            return player1Dead && player2Dead;
        }
    }

    public int getGameScore() { return gameScore; }
    public boolean isGameWon() { return gameWon; }
    public User getCurrentUser() { return currentUser; }
    public long getGameStartTime() { return gameStartTime; }

    public boolean isGameEnded() { return gameEnded; }
    public GameEndReason getEndReason() { return endReason; }
    public int getWinnerPlayer() { return winnerPlayer; }
    public boolean isDraw() { return isDraw; }
    public boolean hasTimeLimit() { return hasTimeLimit; }
}