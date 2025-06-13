package com.superbomberman.controller;

import com.superbomberman.game.GameStateManager;
import com.superbomberman.model.GameResult;
import com.superbomberman.model.GameEndType;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Contrôleur de l'écran de fin de partie (victoire, défaite, égalité, podium, etc.).
 * <p>
 * Gère l'affichage des scores, du temps, des messages et des animations pour
 * le mode solo et multijoueur, ainsi que l'action du bouton quitter.
 * </p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-12
 */
public class EndGameController {

    // === ÉLÉMENTS FXML ===
    @FXML private VBox rootContainer;
    @FXML private Label titleLabel;
    @FXML private VBox soloContainer;
    @FXML private VBox multiContainer;

    // Solo mode elements
    @FXML private Label soloScoreLabel;
    @FXML private Label soloTimeLabel;
    @FXML private Label soloMessageLabel;

    // Multi mode elements
    @FXML private Label winnerNameLabel;
    @FXML private Label winnerScoreLabel;
    @FXML private Label loserNameLabel;
    @FXML private Label loserScoreLabel;
    @FXML private VBox podiumContainer;

    // Common button
    @FXML private Button quitButton;

    private GameResult gameResult;
    private GameStateManager gameStateManager;

    /**
     * Initialise l'écran de fin avec les résultats du jeu.
     * Affiche soit la vue solo, soit la vue multi selon le résultat.
     *
     * @param result Résultat de la partie
     */
    public void initializeEndScreen(GameResult result) {
        this.gameResult = result;

        // Masquer toutes les sections d'abord
        soloContainer.setVisible(false);
        multiContainer.setVisible(false);

        if (result.isSoloMode()) {
            setupSoloEndScreen(result);
        } else {
            setupMultiEndScreen(result);
        }

        // Configurer les boutons
        setupButtons();

        // Animation d'entrée
        playEntryAnimation();
    }

    /**
     * Spécifie le gestionnaire d'état du jeu pour permettre interaction (restart, menu, etc.).
     * @param gameStateManager Instance du gestionnaire d'état
     */
    public void setGameStateManager(com.superbomberman.game.GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }

    /**
     * Configure l'affichage pour le mode solo.
     * @param result Résultat du mode solo
     */
    private void setupSoloEndScreen(GameResult result) {
        soloContainer.setVisible(true);

        // Titre principal
        if (result.getEndType() == GameEndType.SOLO_VICTORY) {
            titleLabel.setText("🎉 VICTOIRE ! 🎉");
            titleLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 32px; -fx-font-weight: bold;");
            soloMessageLabel.setText("Félicitations ! Vous avez vaincu tous les ennemis !");
        } else {
            titleLabel.setText("💀 DÉFAITE 💀");
            titleLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 32px; -fx-font-weight: bold;");
            soloMessageLabel.setText("Vous avez été vaincu... Tentez votre chance à nouveau !");
        }

        // Affichage du score et du temps
        soloScoreLabel.setText("Score final: " + result.getFinalScore()/2); // Divisé par 2 pour correspondre à la logique du jeu
        soloTimeLabel.setText("Temps de jeu: " + formatDuration(result.getGameDuration()));
    }

    /**
     * Configure l'affichage pour le mode multijoueur.
     * @param result Résultat du mode multi
     */
    private void setupMultiEndScreen(GameResult result) {
        multiContainer.setVisible(true);

        switch (result.getEndType()) {
            case MULTI_PLAYER1_WINS -> {
                titleLabel.setText("🏆 " + result.getPlayer1Name() + " GAGNE ! 🏆");
                titleLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 28px; -fx-font-weight: bold;");
                setupPodium(result.getPlayer1Name(), result.getPlayer1Score(),
                        result.getPlayer2Name(), result.getPlayer2Score());
            }
            case MULTI_PLAYER2_WINS -> {
                titleLabel.setText("🏆 " + result.getPlayer2Name() + " GAGNE ! 🏆");
                titleLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 28px; -fx-font-weight: bold;");
                setupPodium(result.getPlayer2Name(), result.getPlayer2Score(),
                        result.getPlayer1Name(), result.getPlayer1Score());
            }
            case MULTI_DRAW -> {
                titleLabel.setText("🤝 ÉGALITÉ ! 🤝");
                titleLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 28px; -fx-font-weight: bold;");
                setupDrawDisplay(result);
            }
        }
    }

    /**
     * Configure l'affichage du podium (gagnant et perdant).
     * @param winnerName Nom du gagnant
     * @param winnerScore Score du gagnant
     * @param loserName Nom du perdant
     * @param loserScore Score du perdant
     */
    private void setupPodium(String winnerName, int winnerScore, String loserName, int loserScore) {
        winnerNameLabel.setText("🥇 " + winnerName);
        winnerNameLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 24px; -fx-font-weight: bold;");
        winnerScoreLabel.setText("Score: " + winnerScore);

        loserNameLabel.setText("🥈 " + loserName);
        loserNameLabel.setStyle("-fx-text-fill: #C0C0C0; -fx-font-size: 18px;");
        loserScoreLabel.setText("Score: " + loserScore);
    }

    /**
     * Configure l'affichage en cas d'égalité.
     * @param result Résultat de la partie
     */
    private void setupDrawDisplay(GameResult result) {
        winnerNameLabel.setText("🥇 " + result.getPlayer1Name() + " & " + result.getPlayer2Name());
        winnerNameLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 20px; -fx-font-weight: bold;");
        winnerScoreLabel.setText("Score identique: " + result.getPlayer1Score());

        loserNameLabel.setText("Une partie serrée !");
        loserScoreLabel.setText("Temps: " + formatDuration(result.getGameDuration()));
    }

    /**
     * Configure les actions des boutons (bouton quitter).
     */
    private void setupButtons() {
        quitButton.setOnAction(e -> handleQuit());
    }

    /**
     * Joue une animation d'entrée lors de l'affichage de l'écran de fin.
     */
    private void playEntryAnimation() {
        // Animation de fade-in
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.8), rootContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // Animation de scaling
        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(0.6), titleLabel);
        scaleIn.setFromX(0.5);
        scaleIn.setFromY(0.5);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        // Animation des boutons avec délai
        TranslateTransition buttonsSlide = new TranslateTransition(Duration.seconds(0.5),
                rootContainer.getChildren().get(rootContainer.getChildren().size() - 1));
        buttonsSlide.setFromY(50);
        buttonsSlide.setToY(0);
        buttonsSlide.setDelay(Duration.seconds(0.3));

        // Jouer toutes les animations
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleIn);
        SequentialTransition sequence = new SequentialTransition(parallelTransition, buttonsSlide);
        sequence.play();
    }

    /**
     * Formate la durée (en ms) en chaîne lisible mm:ss.
     * @param durationMs Durée en millisecondes
     * @return String formatée mm:ss
     */
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Action du bouton quitter : ferme l'application.
     */
    @FXML
    private void handleQuit() {
        javafx.application.Platform.exit();
    }
}