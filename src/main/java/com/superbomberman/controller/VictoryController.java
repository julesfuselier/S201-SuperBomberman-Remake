package com.superbomberman.controller;

import com.superbomberman.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Contrôleur pour l'écran de victoire
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-10
 */

public class VictoryController {

    @FXML private Label winnerLabel;
    @FXML private Label scoreLabel;
    @FXML private Label timeLabel;
    @FXML private Label modeLabel;

    @FXML private Button playAgainBtn;
    @FXML private Button menuBtn;
    @FXML private Button quitBtn;

    private User currentUser;
    private int finalScore;
    private long gameTime;
    private boolean isOnePlayer;
    private String winner;

    /**
     * Initialise l'écran de victoire avec les données du jeu
     */
    public void initializeVictoryScreen(User user, int score, long gameTimeMs, boolean onePlayer, String winnerText) {
        this.currentUser = user;
        this.finalScore = score;
        this.gameTime = gameTimeMs;
        this.isOnePlayer = onePlayer;
        this.winner = winnerText;

        updateLabels();
    }

    /**
     * Met à jour les labels avec les informations du jeu
     */
    private void updateLabels() {
        // Texte du gagnant
        if (winner != null && !winner.isEmpty()) {
            winnerLabel.setText(winner);
        }

        // Score
        scoreLabel.setText("Score Final: " + finalScore);

        // Temps de jeu
        long minutes = gameTime / 60000;
        long seconds = (gameTime % 60000) / 1000;
        timeLabel.setText(String.format("Temps: %02d:%02d", minutes, seconds));

        // Mode de jeu
        modeLabel.setText("Mode: " + (isOnePlayer ? "1 Joueur" : "2 Joueurs"));
    }

    /**
     * Rejouer une partie
     */
    @FXML
    private void handlePlayAgain() {
        try {
            System.out.println("🔄 Démarrage d'une nouvelle partie...");

            // Charger la vue de jeu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
            Parent gameRoot = loader.load();

            // Passer l'utilisateur au contrôleur de jeu
            GameViewController gameController = loader.getController();
            if (currentUser != null) {
                gameController.setCurrentUser(currentUser);
            }

            // Changer de scène
            Scene gameScene = new Scene(gameRoot);
            Stage stage = (Stage) playAgainBtn.getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - Jeu");

            System.out.println("✅ Nouvelle partie démarrée !");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors du redémarrage du jeu");
        }
    }

    /**
     * Retour au menu principal
     */
    @FXML
    private void handleBackToMenu() {
        try {
            System.out.println("🏠 Retour au menu principal...");

            // Charger la vue du menu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent menuRoot = loader.load();

            // Passer l'utilisateur au contrôleur du menu
            MenuController menuController = loader.getController();
            if (currentUser != null) {
                menuController.setCurrentUser(currentUser);
            }

            // Changer de scène
            Scene menuScene = new Scene(menuRoot);
            Stage stage = (Stage) menuBtn.getScene().getWindow();
            stage.setScene(menuScene);
            stage.setTitle("Super Bomberman - Menu");

            System.out.println("✅ Retour au menu réussi !");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors du retour au menu");
        }
    }

    /**
     * Quitter l'application
     */
    @FXML
    private void handleQuit() {
        System.out.println("👋 Fermeture de l'application...");
        Platform.exit();
    }
}