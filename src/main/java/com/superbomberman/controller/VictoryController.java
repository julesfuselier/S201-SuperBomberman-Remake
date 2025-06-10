package com.superbomberman.controller;

import com.superbomberman.model.User;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Contrôleur pour l'écran de victoire avec affichage spécial mode 2 joueurs
 *
 * @author Jules Fuselier
 * @version 2.2 - Correction basée sur les vraies données du jeu
 * @since 2025-06-10
 */

public class VictoryController {

    // Labels et boutons communs
    @FXML private Label winnerLabel;
    @FXML private Label scoreLabel;
    @FXML private Label timeLabel;
    @FXML private Label modeLabel;

    @FXML private Button playAgainBtn;
    @FXML private Button menuBtn;
    @FXML private Button quitBtn;

    // Sections d'affichage
    @FXML private VBox onePlayerSection;
    @FXML private VBox twoPlayerSection;
    @FXML private HBox podiumContainer;

    // Statistiques comparatives
    @FXML private Label leftPlayerStats;
    @FXML private Label leftPlayerScore;
    @FXML private Label leftPlayerStatus;
    @FXML private Label rightPlayerStats;
    @FXML private Label rightPlayerScore;
    @FXML private Label rightPlayerStatus;

    private User currentUser;
    private int finalScore;
    private long gameTime;
    private boolean isOnePlayer;
    private String winner;
    private String victoryScenario;

    // Nouvelles variables pour les vraies données du jeu
    private boolean player1Alive;
    private boolean player2Alive;
    private int player1Score;
    private int player2Score;

    /**
     * ✅ NOUVELLE VERSION - Initialise l'écran de victoire avec les vraies données du jeu
     */
    public void initializeVictoryScreen(User user, int score, long gameTimeMs, boolean onePlayer,
                                        String winnerText, boolean p1Alive, boolean p2Alive,
                                        int p1Score, int p2Score) {
        this.currentUser = user;
        this.finalScore = score;
        this.gameTime = gameTimeMs;
        this.isOnePlayer = onePlayer;
        this.winner = winnerText;
        this.player1Alive = p1Alive;
        this.player2Alive = p2Alive;
        this.player1Score = p1Score;
        this.player2Score = p2Score;

        // ✅ Déterminer le vrai gagnant basé sur les données réelles du jeu
        determineRealWinner();

        System.out.println("🎯 Données reçues:");
        System.out.println("   - Joueur 1 vivant: " + player1Alive + " (Score: " + player1Score + ")");
        System.out.println("   - Joueur 2 vivant: " + player2Alive + " (Score: " + player2Score + ")");
        System.out.println("   - Scénario déterminé: " + victoryScenario);

        updateDisplay();
        playVictoryAnimations();
    }

    /**
     * ✅ ANCIENNE VERSION - Pour compatibilité avec les appels existants
     */
    public void initializeVictoryScreen(User user, int score, long gameTimeMs, boolean onePlayer, String winnerText) {
        // Version de compatibilité qui utilise l'analyse du texte
        this.currentUser = user;
        this.finalScore = score;
        this.gameTime = gameTimeMs;
        this.isOnePlayer = onePlayer;
        this.winner = winnerText;

        // Analyser le message de victoire pour déterminer le scenario (ancienne méthode)
        analyzeVictoryScenario(winnerText);

        System.out.println("⚠️ ATTENTION: Utilisation de l'ancienne méthode d'analyse de victoire");
        System.out.println("   - Message: " + winnerText);
        System.out.println("   - Scénario analysé: " + victoryScenario);

        updateDisplay();
        playVictoryAnimations();
    }

    /**
     * ✅ NOUVELLE MÉTHODE - Détermine le vrai gagnant basé sur les données réelles du jeu
     */
    private void determineRealWinner() {
        if (isOnePlayer) {
            victoryScenario = "SINGLE_PLAYER";
            return;
        }

        // Logique pour le mode 2 joueurs basée sur les VRAIES données
        if (player1Alive && !player2Alive) {
            // Joueur 1 survit, Joueur 2 mort -> Joueur 1 gagne
            victoryScenario = "PLAYER1_WINS";
            this.finalScore = player1Score;
        } else if (!player1Alive && player2Alive) {
            // ✅ CAS DE VOTRE PROBLÈME: Joueur 1 mort, Joueur 2 survit -> Joueur 2 gagne
            victoryScenario = "PLAYER2_WINS";
            this.finalScore = player2Score;
        } else if (player1Alive && player2Alive) {
            // Les deux survivent -> Victoire au score
            if (player1Score > player2Score) {
                victoryScenario = "PLAYER1_WINS";
                this.finalScore = player1Score;
            } else if (player2Score > player1Score) {
                victoryScenario = "PLAYER2_WINS";
                this.finalScore = player2Score;
            } else {
                victoryScenario = "BOTH_SURVIVE";
                this.finalScore = Math.max(player1Score, player2Score);
            }
        } else {
            // Les deux sont morts -> Victoire au score
            if (player1Score > player2Score) {
                victoryScenario = "PLAYER1_WINS";
                this.finalScore = player1Score;
            } else if (player2Score > player1Score) {
                victoryScenario = "PLAYER2_WINS";
                this.finalScore = player2Score;
            } else {
                victoryScenario = "BOTH_SURVIVE"; // Match nul
                this.finalScore = player1Score;
            }
        }
    }

    /**
     * ✅ ANCIENNE MÉTHODE - Analyse le message de victoire (gardée pour compatibilité)
     */
    private void analyzeVictoryScenario(String winnerText) {
        if (winnerText.contains("Joueur 1")) {
            victoryScenario = "PLAYER1_WINS";
        } else if (winnerText.contains("Joueur 2")) {
            victoryScenario = "PLAYER2_WINS";
        } else if (winnerText.contains("partagée") || winnerText.contains("survécu")) {
            victoryScenario = "BOTH_SURVIVE";
        } else {
            victoryScenario = "SINGLE_PLAYER";
        }
    }

    /**
     * Met à jour l'affichage selon le mode de jeu
     */
    private void updateDisplay() {
        if (isOnePlayer) {
            // Mode 1 joueur : affichage classique
            setupSinglePlayerDisplay();
        } else {
            // Mode 2 joueurs : affichage spécial
            setupTwoPlayerDisplay();
        }
    }

    /**
     * Configure l'affichage pour le mode 1 joueur
     */
    private void setupSinglePlayerDisplay() {
        onePlayerSection.setVisible(true);
        twoPlayerSection.setVisible(false);

        // Labels classiques
        if (winner != null && !winner.isEmpty()) {
            winnerLabel.setText(winner);
        }

        scoreLabel.setText("Score Final: " + finalScore);

        long minutes = gameTime / 60000;
        long seconds = (gameTime % 60000) / 1000;
        timeLabel.setText(String.format("Temps: %02d:%02d", minutes, seconds));

        modeLabel.setText("Mode: 1 Joueur");
    }

    /**
     * Configure l'affichage spécial pour le mode 2 joueurs
     */
    private void setupTwoPlayerDisplay() {
        onePlayerSection.setVisible(false);
        twoPlayerSection.setVisible(true);

        switch (victoryScenario) {
            case "PLAYER1_WINS":
                setupPlayer1Victory();
                break;
            case "PLAYER2_WINS":
                setupPlayer2Victory();
                break;
            case "BOTH_SURVIVE":
                setupBothSurviveVictory();
                break;
            default:
                setupSinglePlayerDisplay();
                break;
        }
    }

    /**
     * Crée un élément de podium pour un joueur
     */
    private VBox createPodiumElement(String playerName, String position, String positionText,
                                     double podiumWidth, double podiumHeight, javafx.scene.paint.Color podiumColor,
                                     String nameColor, String imagePath) {
        VBox podiumElement = new VBox();
        podiumElement.setAlignment(javafx.geometry.Pos.CENTER);
        podiumElement.setSpacing(10.0);

        // Rectangle du podium
        Rectangle podium = new Rectangle(podiumWidth, podiumHeight);
        podium.setFill(podiumColor);
        podium.setStroke(javafx.scene.paint.Color.BLACK);
        podium.setStrokeWidth(position.equals("1er") ? 3.0 : 2.0);

        // Label de position
        Label posLabel = new Label(positionText);
        posLabel.setStyle(position.equals("1er") ?
                "-fx-text-fill: gold; -fx-font-weight: bold; -fx-font-size: 18px;" :
                "-fx-text-fill: white; -fx-font-weight: bold;");

        // Avatar du joueur
        ImageView avatar = new ImageView();
        try {
            avatar.setImage(new Image(getClass().getResourceAsStream(imagePath)));
        } catch (Exception e) {
            System.err.println("Impossible de charger l'image: " + imagePath);
        }
        avatar.setFitHeight(position.equals("1er") ? 70.0 : 50.0);
        avatar.setFitWidth(position.equals("1er") ? 70.0 : 50.0);
        avatar.setPreserveRatio(true);

        // Nom du joueur
        Label nameLabel = new Label(playerName);
        nameLabel.setStyle(nameColor);

        podiumElement.getChildren().addAll(podium, posLabel, avatar, nameLabel);

        // Ajouter l'animation si c'est le gagnant
        if (position.equals("1er")) {
            animatePodium(podium);
        } else {
            fadeAnimation(podium, 0.7);
        }

        return podiumElement;
    }

    /**
     * ✅ CORRIGÉE - Configuration pour la victoire du Joueur 1
     */
    private void setupPlayer1Victory() {
        podiumContainer.getChildren().clear();

        // Joueur 2 (2ème place) à gauche
        VBox player2Podium = createPodiumElement(
                "Joueur 2", "2ème", "2ème",
                80.0, 60.0, javafx.scene.paint.Color.SILVER,
                "-fx-text-fill: silver; -fx-font-size: 14px;",
                "/images/player2.png"
        );

        // Joueur 1 (1ère place) à droite (centre visuel)
        VBox player1Podium = createPodiumElement(
                "Joueur 1", "1er", "1er",
                100.0, 100.0, javafx.scene.paint.Color.GOLD,
                "-fx-text-fill: gold; -fx-font-size: 16px; -fx-font-weight: bold;",
                "/images/player.png"
        );

        podiumContainer.getChildren().addAll(player2Podium, player1Podium);

        // ✅ Stats comparatives avec les VRAIS scores
        leftPlayerStats.setText("🎯 Joueur 1");
        leftPlayerScore.setText("Score: " + player1Score);
        leftPlayerStatus.setText(player1Alive ? "✅ VAINQUEUR" : "🏆 VAINQUEUR (au score)");
        leftPlayerStatus.setStyle("-fx-text-fill: lightgreen;");

        rightPlayerStats.setText("💀 Joueur 2");
        rightPlayerScore.setText("Score: " + player2Score);
        rightPlayerStatus.setText(player2Alive ? "😞 DÉFAITE" : "💀 ÉLIMINÉ");
        rightPlayerStatus.setStyle("-fx-text-fill: lightcoral;");
    }

    /**
     * ✅ CORRIGÉE - Configuration pour la victoire du Joueur 2
     */
    private void setupPlayer2Victory() {
        podiumContainer.getChildren().clear();

        // Joueur 1 (2ème place) à gauche
        VBox player1Podium = createPodiumElement(
                "Joueur 1", "2ème", "2ème",
                80.0, 60.0, javafx.scene.paint.Color.SILVER,
                "-fx-text-fill: silver; -fx-font-size: 14px;",
                "/images/player.png"
        );

        // Joueur 2 (1ère place) à droite (centre visuel)
        VBox player2Podium = createPodiumElement(
                "Joueur 2", "1er", "1er",
                100.0, 100.0, javafx.scene.paint.Color.GOLD,
                "-fx-text-fill: gold; -fx-font-size: 16px; -fx-font-weight: bold;",
                "/images/player2.png"
        );

        podiumContainer.getChildren().addAll(player1Podium, player2Podium);

        // ✅ Stats comparatives avec les VRAIS scores (CORRIGÉES)
        leftPlayerStats.setText("🎯 Joueur 2");
        leftPlayerScore.setText("Score: " + player2Score);
        leftPlayerStatus.setText(player2Alive ? "✅ VAINQUEUR" : "🏆 VAINQUEUR (au score)");
        leftPlayerStatus.setStyle("-fx-text-fill: lightgreen;");

        rightPlayerStats.setText("💀 Joueur 1");
        rightPlayerScore.setText("Score: " + player1Score);
        rightPlayerStatus.setText(player1Alive ? "😞 DÉFAITE" : "💀 ÉLIMINÉ");
        rightPlayerStatus.setStyle("-fx-text-fill: lightcoral;");
    }

    /**
     * ✅ CORRIGÉE - Configuration pour la victoire partagée (les deux survivent)
     */
    private void setupBothSurviveVictory() {
        podiumContainer.getChildren().clear();

        // Joueur 1 (co-vainqueur)
        VBox player1Podium = createPodiumElement(
                "Joueur 1", "1er", "1er",
                100.0, 100.0, javafx.scene.paint.Color.GOLD,
                "-fx-text-fill: gold; -fx-font-size: 16px; -fx-font-weight: bold;",
                "/images/player.png"
        );

        // Joueur 2 (co-vainqueur)
        VBox player2Podium = createPodiumElement(
                "Joueur 2", "1er", "1er",
                100.0, 100.0, javafx.scene.paint.Color.GOLD,
                "-fx-text-fill: gold; -fx-font-size: 16px; -fx-font-weight: bold;",
                "/images/player2.png"
        );

        podiumContainer.getChildren().addAll(player1Podium, player2Podium);

        // ✅ Stats pour victoire partagée avec les VRAIS scores
        leftPlayerStats.setText("🎯 Joueur 1");
        leftPlayerScore.setText("Score: " + player1Score);
        leftPlayerStatus.setText("🏆 CO-VAINQUEUR");
        leftPlayerStatus.setStyle("-fx-text-fill: gold;");

        rightPlayerStats.setText("🎯 Joueur 2");
        rightPlayerScore.setText("Score: " + player2Score);
        rightPlayerStatus.setText("🏆 CO-VAINQUEUR");
        rightPlayerStatus.setStyle("-fx-text-fill: gold;");
    }

    /**
     * Lance les animations de victoire
     */
    private void playVictoryAnimations() {
        // Les animations sont maintenant gérées dans createPodiumElement()
    }

    /**
     * Animation du podium gagnant
     */
    private void animatePodium(Rectangle podium) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1.5), podium);
        scaleTransition.setFromX(0.8);
        scaleTransition.setFromY(0.8);
        scaleTransition.setToX(1.2);
        scaleTransition.setToY(1.2);
        scaleTransition.setCycleCount(ScaleTransition.INDEFINITE);
        scaleTransition.setAutoReverse(true);
        scaleTransition.play();
    }

    /**
     * Animation de fade pour l'élément perdant
     */
    private void fadeAnimation(Rectangle element, double targetOpacity) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2.0), element);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(targetOpacity);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();
    }

    /**
     * Rejouer une partie
     */
    @FXML
    private void handlePlayAgain() {
        try {
            System.out.println("🔄 Démarrage d'une nouvelle partie...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game-view.fxml"));
            Parent gameRoot = loader.load();

            GameViewController gameController = loader.getController();
            if (currentUser != null) {
                gameController.setCurrentUser(currentUser);
            }

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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent menuRoot = loader.load();

            MenuController menuController = loader.getController();
            if (currentUser != null) {
                menuController.setCurrentUser(currentUser);
            }

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