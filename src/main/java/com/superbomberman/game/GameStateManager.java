package com.superbomberman.game;

import com.superbomberman.controller.EndGameController;
import com.superbomberman.model.GameEndType;
import com.superbomberman.model.GameResult;
import com.superbomberman.model.Player;
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
    private boolean gameEnded = false;

    // 🆕 NOUVEAU : Système de score avancé
    private ScoreSystem scoreSystem;
    private Player winner;

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

        if (currentUser != null && authService != null) {
            authService.updateUserStats(currentUser, gameWon, gameScore);
            System.out.println("Statistiques mises à jour pour " + currentUser.getUsername());
            System.out.println("Score final: " + gameScore + " | Victoire: " + (gameWon ? "Oui" : "Non"));
            scoreSystem.displayScoreSummary();
        }

        //  AFFICHER L'ÉCRAN DE FIN ADAPTATIF
        javafx.application.Platform.runLater(() -> showEndGameScreen());
    }

    /**
     * 🆕 Affiche l'écran de fin adaptatif
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

            // 🆕 PASSER la référence du GameStateManager au contrôleur
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
     *  Crée le résultat de jeu selon le mode
     */
    private GameResult createGameResult() {
        long gameDuration = System.currentTimeMillis() - gameStartTime;

        if (isOnePlayer) {
            // Mode solo - Utiliser le score total du système
            GameEndType endType = gameWon ? GameEndType.SOLO_VICTORY : GameEndType.SOLO_DEFEAT;

            // 🔥 FIX : Récupérer le VRAI score
            int finalScore = scoreSystem.getPlayerScore(player1) + gameScore;

            System.out.println("🎯 Score final transmis: " + finalScore);
            return new GameResult(endType, finalScore, gameDuration);
        } else {
            // Mode multijoueur
            String player1Name = player1 != null ? player1.getName() : "Joueur 1";
            String player2Name = player2 != null ? player2.getName() : "Joueur 2";
            int player1Score = scoreSystem.getPlayerScore(player1);
            int player2Score = player2 != null ? scoreSystem.getPlayerScore(player2) : 0;

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

        // Réinitialiser les variables d'état
        this.gameEnded = false;
        this.gameWon = false;
        this.gameScore = 0;
        this.gameStartTime = System.currentTimeMillis();
        this.winner = null;

        // Réinitialiser le système de score
        if (scoreSystem != null) {
            scoreSystem.reset();
        }

        System.out.println("✅ État du jeu réinitialisé");
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

                // 4️⃣ ✅ FIX : Nettoyer et rafraîchir l'affichage
                javafx.stage.Stage stage = getCurrentStage();
                if (stage != null) {
                    javafx.scene.Scene newScene = new javafx.scene.Scene(gameRoot);
                    stage.setScene(newScene);
                    stage.setTitle("Super Bomberman - " + (isOnePlayer ? "1 Joueur" : "2 Joueurs"));

                    // 🔥 FORCER le rafraîchissement complet
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
    public ScoreSystem getScoreSystem() { return scoreSystem; }
}