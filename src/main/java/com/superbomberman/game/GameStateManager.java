package com.superbomberman.game;

import com.superbomberman.controller.GameOverController;
import com.superbomberman.controller.VictoryController;
import com.superbomberman.model.User;
import com.superbomberman.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static com.superbomberman.model.MapLoader.enemy;
import static com.superbomberman.model.MapLoader.player1;
import static com.superbomberman.model.MapLoader.player2;
import static com.superbomberman.controller.MenuController.isOnePlayer;

/**
 * Gestionnaire de l'état du jeu et des statistiques
 *
 * @author Jules Fuselier
 * @version 2.1 - Fix logique victoire mode 2 joueurs
 * @since 2025-06-08
 */
public class GameStateManager {
    private User currentUser;
    private AuthService authService;
    private int gameScore = 0;
    private boolean gameWon = false;
    private boolean gameEnded = false; //  : Éviter les fins multiples
    private String victoryMessage = ""; // Message de victoire personnalisé
    private long gameStartTime;
    private Stage gameStage; // Référence au stage du jeu

    private boolean player1Killed = false;
    private boolean player2Killed = false;
    private boolean enemyKilled = false;

    private long gameTime;

    private Runnable onGameEndCallback;

    public GameStateManager(User currentUser, AuthService authService) {
        this.currentUser = currentUser;
        this.authService = authService;
        this.gameStartTime = System.currentTimeMillis();
    }

    /**
     * Définit la référence au stage du jeu pour les redirections
     */
    public void setGameStage(Stage gameStage) {
        this.gameStage = gameStage;
    }

    /**
     *  Définit le callback à exécuter quand le jeu se termine
     */
    public void setOnGameEndCallback(Runnable callback) {
        this.onGameEndCallback = callback;
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
        if (gameEnded) return; // Éviter les doublons

        this.gameWon = won;
        this.gameEnded = true;

        if (won) {
            System.out.println("🎉 Victoire ! Score final: " + gameScore);
            showVictoryScreen();
        } else {
            System.out.println("💀 Défaite ! Score final: " + gameScore);
            showGameOverScreen();
        }

        endGame();

        // Arrêter le jeu après un court délai
        Platform.runLater(() -> {
            if (onGameEndCallback != null) {
                onGameEndCallback.run();
            }
        });
    }

    /**
     * Marque le jeu comme gagné ou perdu avec un message personnalisé
     */
    public void setGameWonWithMessage(boolean won, String message) {
        if (gameEnded) return; // Éviter les doublons

        this.gameWon = won;
        this.gameEnded = true;
        this.victoryMessage = message;

        System.out.println(message);
        System.out.println("Score final: " + gameScore);

        if (won) {
            showVictoryScreen();
        } else {
            showGameOverScreen();
        }

        endGame();

        // Arrêter le jeu après un court délai
        Platform.runLater(() -> {
            if (onGameEndCallback != null) {
                onGameEndCallback.run();
            }
        });
    }

    /**
     * Affiche l'écran de victoire
     */
    public void showVictoryScreen() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/victory.fxml"));
                Scene scene = new Scene(loader.load());

                // Passer les données au contrôleur si nécessaire
                VictoryController controller = loader.getController();
                if (controller != null) {
                    // Utiliser le message personnalisé si disponible
                    String messageToShow = victoryMessage.isEmpty() ? "Victoire !" : victoryMessage;
                    controller.initializeVictoryScreen(
                            currentUser,
                            gameScore,
                            gameTime,
                            isOnePlayer,
                            messageToShow
                    );
                }

                if (gameStage != null) {
                    gameStage.setScene(scene);
                    gameStage.setTitle("Victoire !");
                }
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de victory.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Affiche l'écran de défaite
     */
    public void showGameOverScreen() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gameOver.fxml"));
                Scene scene = new Scene(loader.load());

                // Passer les données au contrôleur si nécessaire
                GameOverController controller = loader.getController();
                if (controller != null) {
                    // Utiliser le message personnalisé si disponible
                    String messageToShow = victoryMessage.isEmpty() ? "Vous avez perdu !" : victoryMessage;
                    controller.initializeGameOverScreen(
                            currentUser,
                            gameScore,
                            gameTime,
                            isOnePlayer,
                            messageToShow
                    );
                }

                if (gameStage != null) {
                    gameStage.setScene(scene);
                    gameStage.setTitle("Game Over");
                }
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de gameOver.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     *  Appelée quand un joueur est tué
     */
    public void onPlayerKilled(int playerNumber) {
        if (gameEnded) return; // Éviter les doublons

        if (playerNumber == 1) {
            player1Killed = true;
            System.out.println("🚨 Joueur 1 éliminé !");
        } else if (playerNumber == 2) {
            player2Killed = true;
            System.out.println("🚨 Joueur 2 éliminé !");
        }

        // Vérifier immédiatement les conditions de fin
        checkGameConditions();
    }

    /**
     *  Appelée quand un ennemi est tué
     */
    public void onEnemyKilled() {
        if (gameEnded) return; // Éviter les doublons

        enemyKilled = true;
        updateScore(100); // Bonus pour éliminer un ennemi
        System.out.println("🎯 Ennemi éliminé ! Bonus +100 points");

        // Vérifier immédiatement les conditions de fin
        checkGameConditions();
    }

    /**
     * Termine le jeu et met à jour les statistiques utilisateur
     */
    public void endGame() {
        if (currentUser != null && authService != null) {
            authService.updateUserStats(currentUser, gameWon, gameScore);
            System.out.println("Statistiques mises à jour pour " + currentUser.getUsername());
            System.out.println("Score final: " + gameScore + " | Victoire: " + (gameWon ? "Oui" : "Non"));
        }

        gameTime = System.currentTimeMillis() - gameStartTime;
    }

    /**
     *  Vérifie les conditions de fin de jeu selon le mode
     */
    public void checkGameConditions() {
        if (gameEnded) return; // Éviter les vérifications multiples

        if (isOnePlayer) {
            // === MODE 1 JOUEUR ===
            if (isPlayerDefeated()) {
                System.out.println("💀 Mode 1 joueur : Joueur éliminé !");
                setGameWon(false);
            } else if (isEnemyDefeated()) {
                System.out.println("🎉 Mode 1 joueur : Tous les ennemis éliminés !");
                setGameWon(true);
            }
        } else {
            // === MODE 2 JOUEURS ===
            boolean player1Dead = isPlayer1Defeated();
            boolean player2Dead = isPlayer2Defeated();
            boolean allEnemiesDead = isEnemyDefeated();

            if (player1Dead && player2Dead) {
                System.out.println("⚖️ Mode 2 joueurs : Égalité ! Les deux joueurs sont morts.");
                setGameWonWithMessage(false, "💀 Match nul ! Les deux joueurs ont été éliminés !");
            } else if (player1Dead && !player2Dead) {
                System.out.println("🎉 Mode 2 joueurs : Joueur 2 gagne !");
                setGameWonWithMessage(true, "🎉 Victoire du Joueur 2 ! Le Joueur 1 a été éliminé !");
            } else if (player2Dead && !player1Dead) {
                System.out.println("🎉 Mode 2 joueurs : Joueur 1 gagne !");
                setGameWonWithMessage(true, "🎉 Victoire du Joueur 1 ! Le Joueur 2 a été éliminé !");
            } else if (allEnemiesDead && !player1Dead && !player2Dead) {
                System.out.println("🎉 Mode 2 joueurs : Les deux joueurs survivent ! Victoire partagée !");
                setGameWonWithMessage(true, "🎉 Victoire partagée ! Les deux joueurs ont survécu !");
            }
        }
    }

    /**
     *  Vérifie si le joueur principal est vaincu
     */
    private boolean isPlayerDefeated() {
        return player1 != null && !player1.isAlive();
    }

    /**
     *  Vérifie si le joueur 1 spécifiquement est vaincu
     */
    private boolean isPlayer1Defeated() {
        return player1 != null && !player1.isAlive();
    }

    /**
     * Vérifie si le joueur 2 spécifiquement est vaincu
     */
    private boolean isPlayer2Defeated() {
        return player2 != null && !player2.isAlive();
    }

    /**
     *   Vérifie si l'ennemi est vaincu
     */
    private boolean isEnemyDefeated() {
        // Si pas d'ennemi au départ, considérer comme "vaincu"
        if (enemy == null) return true;

        // Vérifier si l'ennemi est mort
        return !enemy.isAlive();
    }

    public int getGameScore() {
        return gameScore;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public String getVictoryMessage() {
        return victoryMessage;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public long getGameStartTime() {
        return gameStartTime;
    }

    public long getGameTime() {
        return gameTime;
    }

    // MÉTHODES DE DEBUG
    public void printGameState() {
        System.out.println("=== ÉTAT DU JEU ===");
        System.out.println("Mode: " + (isOnePlayer ? "1 joueur" : "2 joueurs"));
        System.out.println("Joueur 1 vivant: " + (player1 != null ? player1.isAlive() : "null"));
        if (!isOnePlayer) {
            System.out.println("Joueur 2 vivant: " + (player2 != null ? player2.isAlive() : "null"));
        }
        System.out.println("Ennemi vivant: " + (enemy != null ? enemy.isAlive() : "null"));
        System.out.println("Jeu terminé: " + gameEnded);
        System.out.println("Score: " + gameScore);
        System.out.println("Message: " + victoryMessage);
        System.out.println("==================");
    }
}