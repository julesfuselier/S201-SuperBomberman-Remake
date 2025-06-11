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
        if (currentUser != null && authService != null) {
            authService.updateUserStats(currentUser, gameWon, gameScore);
            System.out.println("Statistiques mises à jour pour " + currentUser.getUsername());
            System.out.println("Score final: " + gameScore + " | Victoire: " + (gameWon ? "Oui" : "Non"));
            scoreSystem.displayScoreSummary();
        }

        // 🆕 AFFICHER L'ÉCRAN DE FIN ADAPTATIF
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

            // Fallback : retourner au menu
            returnToMenu();
        }
    }

    /**
     * 🆕 Obtient la fenêtre actuelle de façon sécurisée
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
     * 🆕 Crée le résultat de jeu selon le mode
     */
    private GameResult createGameResult() {
        long gameDuration = System.currentTimeMillis() - gameStartTime;

        if (isOnePlayer) {
            // Mode solo
            GameEndType endType = gameWon ? GameEndType.SOLO_VICTORY : GameEndType.SOLO_DEFEAT;
            return new GameResult(endType, gameScore, gameDuration);
        } else {
            // Mode multijoueur
            String player1Name = player1 != null ? player1.getName() : "Joueur 1";
            String player2Name = player2 != null ? player2.getName() : "Joueur 2";
            int player1Score = scoreSystem.getPlayerScore(player1);
            int player2Score = player2 != null ? scoreSystem.getPlayerScore(player2) : 0;

            GameEndType endType;
            if (player1.isAlive() && !player2.isAlive()) {
                endType = GameEndType.MULTI_PLAYER1_WINS;
            } else if (!player1.isAlive() && player2.isAlive()) {
                endType = GameEndType.MULTI_PLAYER2_WINS;
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
        // Exemple de conditions de victoire - à adapter selon votre logique
        if (enemy != null && isEnemyDefeated()) {
            setGameWon(true);
            endGame();
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

    // === MÉTHODES POUR LES BOUTONS (appelées depuis EndGameController) ===

    public void restartGame() {
        javafx.application.Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/fxml/game-view.fxml")
                );
                javafx.scene.Parent gameRoot = loader.load();

                com.superbomberman.controller.GameViewController gameController = loader.getController();
                if (currentUser != null) {
                    gameController.setCurrentUser(currentUser);
                }

                javafx.stage.Stage stage = getCurrentStage();
                if (stage != null) {
                    stage.setScene(new javafx.scene.Scene(gameRoot));
                    stage.setTitle("Super Bomberman - " + (isOnePlayer ? "1 Joueur" : "2 Joueurs"));
                    stage.sizeToScene();
                }
            } catch (Exception e) {
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