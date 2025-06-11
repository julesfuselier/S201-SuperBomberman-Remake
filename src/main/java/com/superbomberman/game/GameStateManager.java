package com.superbomberman.game;

import com.superbomberman.controller.EndGameController;
import com.superbomberman.model.GameEndType;
import com.superbomberman.model.GameResult;
import com.superbomberman.model.GameStats;
import com.superbomberman.model.Player;
import com.superbomberman.model.User;
import com.superbomberman.service.AuthService;
import com.superbomberman.service.StatsService;

import java.util.Timer;
import java.util.TimerTask;

import static com.superbomberman.model.MapLoader.enemy;
import static com.superbomberman.model.MapLoader.player1;
import static com.superbomberman.model.MapLoader.player2;
import static com.superbomberman.controller.MenuController.isOnePlayer;

/**
 * Gestionnaire de l'état du jeu et des statistiques avec sauvegarde en temps réel
 *
 * @author Jules Fuselier
 * @version 4.0 - Sauvegarde temps réel intégrée
 * @since 2025-06-11
 */
public class GameStateManager {
    private User currentUser;
    private AuthService authService;
    private int gameScore = 0;
    private boolean gameWon = false;
    private long gameStartTime;
    private boolean gameEnded = false;

    // Système de score et statistiques avancées
    private EnhancedScoreSystem enhancedScoreSystem;
    private StatsService statsService;
    private Player winner;

    // 🆕 SAUVEGARDE EN TEMPS RÉEL
    private Timer saveTimer;
    private int lastSavedEnemiesKilled = 0;
    private int lastSavedPowerUpsCollected = 0;
    private int lastSavedWallsDestroyed = 0;
    private int lastSavedBestCombo = 0;

    public GameStateManager(User currentUser, AuthService authService) {
        this.currentUser = currentUser;
        this.authService = authService;
        this.gameStartTime = System.currentTimeMillis();

        // Initialiser le système de score avancé et les statistiques
        this.enhancedScoreSystem = new EnhancedScoreSystem(this);
        this.statsService = new StatsService();

        // 🆕 GESTION DES PARTIES INTERROMPUES
        handlePreviousGameState();

        // 🆕 DÉMARRER LA SAUVEGARDE PÉRIODIQUE
        startPeriodicSave();

        // 🆕 AJOUTER SHUTDOWN HOOK
        addShutdownHook();

        System.out.println("🎮 GameStateManager initialisé avec sauvegarde temps réel");
    }

    /**
     * 🆕 Gère les parties précédemment interrompues
     */
    private void handlePreviousGameState() {
        if (currentUser != null && authService != null) {
            boolean hadInterruptedGame = authService.handleInterruptedGame(currentUser);
            if (hadInterruptedGame) {
                System.out.println("⚠️ Partie précédente restaurée et finalisée");
            }
        }
    }

    /**
     * 🆕 Démarre la sauvegarde périodique toutes les 30 secondes
     */
    private void startPeriodicSave() {
        if (currentUser == null || authService == null) return;

        saveTimer = new Timer(true); // daemon thread
        saveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                saveCurrentProgress();
            }
        }, 30000, 30000); // 30 secondes

        System.out.println("⏰ Sauvegarde périodique activée (30s)");
    }

    /**
     * 🆕 Sauvegarde les progrès actuels
     */
    private void saveCurrentProgress() {
        if (currentUser == null || authService == null || gameEnded) return;

        try {
            // Sauvegarder le progrès de base
            authService.saveCurrentGameProgress(currentUser, gameScore, gameStartTime);

            // Calculer les stats depuis la dernière sauvegarde
            int newEnemiesKilled = calculateNewEnemiesKilled();
            int newPowerUpsCollected = calculateNewPowerUpsCollected();
            int newWallsDestroyed = calculateNewWallsDestroyed();
            int currentBestCombo = getCurrentBestCombo();

            // Sauvegarder seulement les nouvelles stats
            if (newEnemiesKilled > 0 || newPowerUpsCollected > 0 ||
                    newWallsDestroyed > 0 || currentBestCombo > lastSavedBestCombo) {

                authService.updateDetailedStats(currentUser,
                        newEnemiesKilled, newPowerUpsCollected,
                        newWallsDestroyed, currentBestCombo);

                // Mettre à jour les compteurs
                lastSavedEnemiesKilled += newEnemiesKilled;
                lastSavedPowerUpsCollected += newPowerUpsCollected;
                lastSavedWallsDestroyed += newWallsDestroyed;
                lastSavedBestCombo = Math.max(lastSavedBestCombo, currentBestCombo);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur sauvegarde périodique: " + e.getMessage());
        }
    }

    /**
     * 🆕 Calcule le nombre d'ennemis tués depuis la dernière sauvegarde
     */
    private int calculateNewEnemiesKilled() {
        if (enhancedScoreSystem != null && player1 != null) {
            int currentTotal = enhancedScoreSystem.getEnemiesKilledByPlayer(player1);
            return Math.max(0, currentTotal - lastSavedEnemiesKilled);
        }
        return 0;
    }

    /**
     * 🆕 Calcule le nombre de power-ups collectés depuis la dernière sauvegarde
     */
    private int calculateNewPowerUpsCollected() {
        if (enhancedScoreSystem != null && player1 != null) {
            int currentTotal = enhancedScoreSystem.getPowerUpsCollectedByPlayer(player1);
            return Math.max(0, currentTotal - lastSavedPowerUpsCollected);
        }
        return 0;
    }

    /**
     * 🆕 Calcule le nombre de murs détruits depuis la dernière sauvegarde
     */
    private int calculateNewWallsDestroyed() {
        if (enhancedScoreSystem != null && player1 != null) {
            int currentTotal = enhancedScoreSystem.getWallsDestroyedByPlayer(player1);
            return Math.max(0, currentTotal - lastSavedWallsDestroyed);
        }
        return 0;
    }

    /**
     * 🆕 Obtient le meilleur combo actuel
     */
    private int getCurrentBestCombo() {
        if (enhancedScoreSystem != null && player1 != null) {
            return enhancedScoreSystem.getBestComboByPlayer(player1);
        }
        return 0;
    }

    /**
     * 🆕 Ajoute un shutdown hook pour sauvegarde d'urgence
     */
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🔄 Sauvegarde d'urgence avant fermeture...");
            if (currentUser != null && authService != null && !gameEnded) {
                authService.saveCurrentGameProgress(currentUser, gameScore, gameStartTime);
            }
            if (saveTimer != null) {
                saveTimer.cancel();
            }
        }));
    }

    /**
     * Met à jour le score du jeu avec sauvegarde conditionnelle
     */
    public void updateScore(int points) {
        gameScore += points;
        System.out.println("Score actuel: " + gameScore);

        // 🆕 Sauvegarde immédiate pour gros gains de points
        if (points >= 100 && currentUser != null && authService != null) {
            authService.saveCurrentGameProgress(currentUser, gameScore, gameStartTime);
            System.out.println("💾 Sauvegarde immédiate - Gros gain: " + points + " points");
        }
    }

    /**
     * Marque le jeu comme gagné ou perdu
     */
    public void setGameWon(boolean won) {
        this.gameWon = won;
        if (won) {
            // Calculer le bonus de temps quand le niveau est terminé
            long gameEndTime = System.currentTimeMillis();
            int usedTimeSeconds = (int) ((gameEndTime - gameStartTime) / 1000);
            int maxTimeSeconds = 120; // 2 minutes par défaut

            enhancedScoreSystem.finishLevel(maxTimeSeconds, usedTimeSeconds);

            System.out.println("🎉 Victoire ! Score final: " + gameScore);
        }
    }

    /**
     * 🆕 Enregistre qu'une bombe a été placée
     */
    public void recordBombPlaced(Player player) {
        if (enhancedScoreSystem != null) {
            enhancedScoreSystem.addBombPlaced(player);
        }
    }

    /**
     * 🆕 Enregistre qu'un ennemi a été tué (appelé depuis le gameplay)
     */
    public void recordEnemyKilled(Player killer) {
        if (enhancedScoreSystem != null) {
            enhancedScoreSystem.addEnemyKilled(killer);
        }
    }

    /**
     * 🆕 Enregistre qu'un power-up a été collecté (appelé depuis le gameplay)
     */
    public void recordPowerUpCollected(Player collector) {
        if (enhancedScoreSystem != null) {
            enhancedScoreSystem.addPowerUpCollected(collector);
        }
    }

    /**
     * 🆕 Enregistre qu'un mur a été détruit (appelé depuis le gameplay)
     */
    public void recordWallDestroyed(Player destroyer) {
        if (enhancedScoreSystem != null) {
            enhancedScoreSystem.addWallDestroyed(destroyer);
        }
    }

    /**
     * Termine le jeu et met à jour les statistiques utilisateur
     */
    public void endGame() {
        // Marquer le jeu comme terminé pour éviter les appels multiples
        if (gameEnded) {
            return;
        }
        gameEnded = true;

        // 🆕 Arrêter la sauvegarde périodique
        if (saveTimer != null) {
            saveTimer.cancel();
            System.out.println("⏰ Sauvegarde périodique arrêtée");
        }

        // 🆕 Sauvegarde finale des progrès
        saveCurrentProgress();

        // Créer et enregistrer les statistiques détaillées
        recordDetailedStats();

        // 🆕 UTILISER LA NOUVELLE MÉTHODE finalizeGame au lieu d'updateUserStats
        if (currentUser != null && authService != null) {
            long gameDuration = System.currentTimeMillis() - gameStartTime;

            // 🔥 FIX : Calculer le VRAI score final
            int realFinalScore = enhancedScoreSystem.getPlayerScore(player1) + gameScore;

            System.out.println("🎯 Score transmis à finalizeGame: " + realFinalScore);
            authService.finalizeGame(currentUser, gameWon, realFinalScore, gameDuration);

            System.out.println("✅ Statistiques finales pour " + currentUser.getUsername());
            System.out.println("Score final: " + realFinalScore + " | Victoire: " + (gameWon ? "Oui" : "Non"));
            enhancedScoreSystem.displayScoreSummary();
        }

        // AFFICHER L'ÉCRAN DE FIN ADAPTATIF
        javafx.application.Platform.runLater(() -> showEndGameScreen());
    }

    /**
     * Enregistre les statistiques détaillées de la partie
     */
    private void recordDetailedStats() {
        if (currentUser == null || statsService == null) {
            System.out.println("⚠️ Pas d'utilisateur ou de service stats - statistiques non enregistrées");
            return;
        }

        long gameDurationSeconds = (System.currentTimeMillis() - gameStartTime) / 1000;
        String gameMode = isOnePlayer ? "SOLO" : "MULTI";

        if (isOnePlayer) {
            // Mode solo
            GameStats stats = enhancedScoreSystem.createGameStats(
                    currentUser.getUsername(),
                    gameWon,
                    gameDurationSeconds,
                    gameMode,
                    player1
            );

            stats.setMapName("default"); // Tu peux améliorer ça plus tard
            statsService.recordGameStats(stats);

            System.out.println("📊 Statistiques solo enregistrées pour " + currentUser.getUsername());

        } else {
            // Mode multijoueur - Enregistrer pour le joueur principal
            GameStats stats = enhancedScoreSystem.createGameStats(
                    currentUser.getUsername(),
                    gameWon,
                    gameDurationSeconds,
                    gameMode,
                    player1
            );

            // Ajouter les infos de l'adversaire
            if (player2 != null) {
                stats.setOpponentName("Joueur 2"); // Tu peux améliorer ça si tu as le nom du J2
                stats.setOpponentScore(enhancedScoreSystem.getPlayerScore(player2));
            }

            stats.setMapName("default");
            statsService.recordGameStats(stats);

            System.out.println("📊 Statistiques multi enregistrées pour " + currentUser.getUsername());
        }
    }

    /**
     * Affiche l'écran de fin adaptatif
     */
    private void showEndGameScreen() {
        try {
            // Créer le résultat de jeu
            GameResult result = createGameResult();

            // Charger notre écran de fin universel
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/endgame.fxml")
            );
            javafx.scene.Parent root = loader.load();

            // Récupérer le contrôleur et l'initialiser
            EndGameController controller = loader.getController();
            controller.initializeEndScreen(result);

            // PASSER la référence du GameStateManager au contrôleur
            controller.setGameStateManager(this);

            // Obtenir la fenêtre actuelle de façon sécurisée
            javafx.stage.Stage stage = getCurrentStage();
            if (stage != null) {
                stage.setScene(new javafx.scene.Scene(root));
                stage.setTitle("Super Bomberman - Fin de Partie");
                stage.sizeToScene();
            } else {
                System.err.println("❌ Impossible de trouver la fenêtre principale");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'affichage de l'écran de fin:");
            e.printStackTrace();
            returnToMenu();
        }
    }

    /**
     * Obtient la fenêtre actuelle de façon sécurisée
     */
    private javafx.stage.Stage getCurrentStage() {
        try {
            // Méthode plus sécurisée pour obtenir la fenêtre
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window instanceof javafx.stage.Stage && window.isShowing()) {
                    return (javafx.stage.Stage) window;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la récupération de la fenêtre: " + e.getMessage());
        }
        return null;
    }

    /**
     * Crée le résultat de jeu selon le mode
     */
    private GameResult createGameResult() {
        long gameDuration = System.currentTimeMillis() - gameStartTime;

        if (isOnePlayer) {
            // Mode solo - Utiliser le score total du système
            GameEndType endType = gameWon ? GameEndType.SOLO_VICTORY : GameEndType.SOLO_DEFEAT;

            // FIX : Récupérer le VRAI score
            int finalScore = enhancedScoreSystem.getPlayerScore(player1) + gameScore;

            System.out.println("🎯 Score final transmis: " + finalScore);
            return new GameResult(endType, finalScore, gameDuration);
        } else {
            // Mode multijoueur
            String player1Name = player1 != null ? player1.getName() : "Joueur 1";
            String player2Name = player2 != null ? player2.getName() : "Joueur 2";
            int player1Score = enhancedScoreSystem.getPlayerScore(player1);
            int player2Score = player2 != null ? enhancedScoreSystem.getPlayerScore(player2) : 0;

            GameEndType endType;
            if (player1 != null && player2 != null) {
                if (player1.isAlive() && !player2.isAlive()) {
                    endType = GameEndType.MULTI_PLAYER1_WINS;
                } else if (!player1.isAlive() && player2.isAlive()) {
                    endType = GameEndType.MULTI_PLAYER2_WINS;
                } else {
                    endType = GameEndType.MULTI_DRAW;
                }
            } else {
                endType = GameEndType.MULTI_DRAW;
            }

            return new GameResult(endType, player1Name, player1Score,
                    player2Name, player2Score, gameDuration);
        }
    }

    /**
     * Vérifie les conditions de fin de jeu
     */
    public void checkGameConditions() {
        // Éviter les appels multiples
        if (gameEnded) {
            return;
        }

        if (isOnePlayer) {
            // Mode solo
            if (enemy != null && isEnemyDefeated()) {
                setGameWon(true);
                endGame();
            } else if (isPlayerDefeated()) {
                setGameWon(false);
                endGame();
            }
        } else {
            // Mode multijoueur
            boolean player1Alive = player1 != null && player1.isAlive();
            boolean player2Alive = player2 != null && player2.isAlive();

            if (!player1Alive && !player2Alive) {
                // Match nul
                setGameWon(false);
                endGame();
            } else if (player1Alive && !player2Alive) {
                // Joueur 1 gagne
                setGameWon(true);
                endGame();
            } else if (!player1Alive && player2Alive) {
                // Joueur 2 gagne
                setGameWon(false);
                endGame();
            }
        }
    }

    /**
     * Vérifie si l'ennemi est vaincu
     */
    private boolean isEnemyDefeated() {
        return enemy != null && enemy.isDead();
    }

    /**
     * Vérifie si le joueur est vaincu
     */
    private boolean isPlayerDefeated() {
        if (isOnePlayer) {
            return player1 == null || !player1.isAlive();
        } else {
            // En multi, tous morts = défaite
            boolean player1Alive = player1 != null && player1.isAlive();
            boolean player2Alive = player2 != null && player2.isAlive();
            return !player1Alive && !player2Alive;
        }
    }

    /**
     * Réinitialise l'état de toutes les entités du jeu
     */
    public void resetGameEntities() {
        System.out.println("🔄 Réinitialisation des entités du jeu...");

        // Réinitialiser les joueurs
        if (player1 != null) {
            player1.setAlive(true);
            System.out.println("✅ Joueur 1 réinitialisé");
        }

        if (player2 != null) {
            player2.setAlive(true);
            System.out.println("✅ Joueur 2 réinitialisé");
        }

        // Réinitialiser l'ennemi
        if (enemy != null) {
            enemy.setAlive(true);
            System.out.println("✅ Ennemi réinitialisé");
        }

        System.out.println("🎮 Toutes les entités ont été réinitialisées");
    }

    /**
     * Réinitialise complètement l'état du jeu pour une nouvelle partie
     */
    public void resetGameState() {
        System.out.println("🔄 Réinitialisation de l'état du jeu...");

        // 🆕 Arrêter l'ancien timer
        if (saveTimer != null) {
            saveTimer.cancel();
        }

        // Réinitialiser les variables d'état
        this.gameEnded = false;
        this.gameWon = false;
        this.gameScore = 0;
        this.gameStartTime = System.currentTimeMillis();
        this.winner = null;

        // 🆕 Réinitialiser les compteurs de sauvegarde
        this.lastSavedEnemiesKilled = 0;
        this.lastSavedPowerUpsCollected = 0;
        this.lastSavedWallsDestroyed = 0;
        this.lastSavedBestCombo = 0;

        // Réinitialiser le système de score
        if (enhancedScoreSystem != null) {
            enhancedScoreSystem.reset();
        }

        // 🆕 Redémarrer la sauvegarde périodique
        startPeriodicSave();

        System.out.println("✅ État du jeu réinitialisé avec sauvegarde temps réel");
    }

    // === MÉTHODES POUR LES BOUTONS (appelées depuis EndGameController) ===

    public void restartGame() {
        javafx.application.Platform.runLater(() -> {
            try {
                System.out.println("🔄 DÉBUT DU RESTART...");

                // 1️⃣ Réinitialiser l'état complet
                resetGameState();
                resetGameEntities();

                // 2️⃣ Charger la nouvelle vue de jeu
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/fxml/game-view.fxml")
                );
                javafx.scene.Parent gameRoot = loader.load();

                // 3️⃣ Configurer le contrôleur
                com.superbomberman.controller.GameViewController gameController = loader.getController();
                if (currentUser != null) {
                    gameController.setCurrentUser(currentUser);
                }

                // 4️⃣ Nettoyer et rafraîchir l'affichage
                javafx.stage.Stage stage = getCurrentStage();
                if (stage != null) {
                    javafx.scene.Scene newScene = new javafx.scene.Scene(gameRoot);
                    stage.setScene(newScene);
                    stage.setTitle("Super Bomberman - " + (isOnePlayer ? "1 Joueur" : "2 Joueurs"));

                    // FORCER le rafraîchissement complet
                    stage.sizeToScene();
                    stage.centerOnScreen();

                    System.out.println("✅ RESTART TERMINÉ AVEC SUCCÈS !");
                }
            } catch (Exception e) {
                System.err.println("❌ ERREUR DURANT LE RESTART:");
                e.printStackTrace();
            }
        });
    }

    public void returnToMenu() {
        // 🆕 Arrêter la sauvegarde avant de quitter
        if (saveTimer != null) {
            saveTimer.cancel();
        }

        javafx.application.Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/fxml/menu.fxml")
                );
                javafx.scene.Parent menuRoot = loader.load();

                javafx.stage.Stage stage = getCurrentStage();
                if (stage != null) {
                    stage.setScene(new javafx.scene.Scene(menuRoot));
                    stage.setTitle("Super Bomberman - Menu");
                    stage.sizeToScene();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void quitGame() {
        System.out.println("Quitter le jeu");
        // 🆕 Le shutdown hook se chargera de la sauvegarde
        javafx.application.Platform.exit();
    }

    // Getters
    public void setWinner(Player winner) {
        this.winner = winner;
    }
    public int getGameScore() { return gameScore; }
    public boolean isGameWon() { return gameWon; }
    public User getCurrentUser() { return currentUser; }
    public long getGameStartTime() { return gameStartTime; }

    // NOUVEAU : Getter pour le système de score avancé
    public EnhancedScoreSystem getEnhancedScoreSystem() { return enhancedScoreSystem; }

    // LEGACY : Compatibilité avec l'ancien système
    public ScoreSystem getScoreSystem() { return enhancedScoreSystem; }
}