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
        // 🆕 Affichage de l'écran de fin adapté solo/multi
        javafx.application.Platform.runLater(() -> {
            try {
                if (isOnePlayer) {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/endgame-solo.fxml"));
                    javafx.scene.Parent root = loader.load();
                    com.superbomberman.controller.EndGameSoloController controller = loader.getController();
                    if (gameWon) {
                        controller.setVictory(gameScore);
                    } else {
                        controller.setDefeat(gameScore);
                    }
                    controller.getReplayButton().setOnAction(e -> restartGame());
                    controller.getMenuButton().setOnAction(e -> returnToMenu());
                    controller.getQuitButton().setOnAction(e -> quitGame());
                    javafx.stage.Stage stage = (javafx.stage.Stage) javafx.stage.Window.getWindows().filtered(javafx.stage.Window::isShowing).get(0);
                    stage.setScene(new javafx.scene.Scene(root));
                } else {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/endgame-multi.fxml"));
                    javafx.scene.Parent root = loader.load();
                    com.superbomberman.controller.EndGameMultiController controller = loader.getController();
                    // Récupérer les scores et noms des joueurs
                    String winnerName = player1 != null ? player1.getName() : "";
                    int winnerScore = scoreSystem.getPlayerScore(player1);
                    String loserName = (player2 != null) ? player2.getName() : "";
                    int loserScore = (player2 != null) ? scoreSystem.getPlayerScore(player2) : 0;
                    if (!gameWon) {
                        // Inverser si le joueur 2 a gagné
                        String tmpName = winnerName;
                        int tmpScore = winnerScore;
                        winnerName = loserName;
                        winnerScore = loserScore;
                        loserName = tmpName;
                        loserScore = tmpScore;
                    }
                    controller.setPodium(winnerName, winnerScore, loserName, loserScore);
                    controller.getReplayButton().setOnAction(e -> restartGame());
                    controller.getMenuButton().setOnAction(e -> returnToMenu());
                    controller.getQuitButton().setOnAction(e -> quitGame());
                    javafx.stage.Stage stage = (javafx.stage.Stage) javafx.stage.Window.getWindows().filtered(javafx.stage.Window::isShowing).get(0);
                    stage.setScene(new javafx.scene.Scene(root));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Méthodes pour les actions des boutons
    private void restartGame() {
        javafx.application.Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/game-view.fxml"));
                javafx.scene.Parent gameRoot = loader.load();
                com.superbomberman.controller.GameViewController gameController = loader.getController();
                if (currentUser != null) {
                    gameController.setCurrentUser(currentUser);
                }
                javafx.scene.Scene gameScene = new javafx.scene.Scene(gameRoot);
                javafx.stage.Stage stage = (javafx.stage.Stage) javafx.stage.Window.getWindows().filtered(javafx.stage.Window::isShowing).get(0);
                stage.setScene(gameScene);
                stage.setTitle("Super Bomberman - " + (isOnePlayer ? "1 Joueur" : "2 Joueurs"));
                stage.sizeToScene();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void returnToMenu() {
        javafx.application.Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
                javafx.scene.Parent menuRoot = loader.load();
                javafx.scene.Scene menuScene = new javafx.scene.Scene(menuRoot);
                javafx.stage.Stage stage = (javafx.stage.Stage) javafx.stage.Window.getWindows().filtered(javafx.stage.Window::isShowing).get(0);
                stage.setScene(menuScene);
                stage.setTitle("Super Bomberman - Menu");
                stage.sizeToScene();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void quitGame() {
        System.out.println("Quitter le jeu");
        javafx.application.Platform.exit();
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
        // Exemple de détection de mort du joueur principal (solo)
        if (isOnePlayer) {
            // Si le joueur n'est plus vivant (ex: a touché une explosion ou a 0 vie)
            return player1 == null || !player1.isAlive();
        } else {
            // En multi, on peut adapter selon la logique (ex: les deux joueurs morts)
            return (player1 == null || !player1.isAlive()) && (player2 == null || !player2.isAlive());
        }
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
