package com.superbomberman.controller;

import com.superbomberman.model.GameMode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.io.IOException;

public class MenuController {

    @FXML
    private Button startGameButton;

    @FXML
    private Button optionsButton;

    @FXML
    private Button onePlayerBtn;

    @FXML
    private Button twoPlayerBtn;

    @FXML
    private Button exitButton;

    @FXML
    public void initialize() {
        // Initialisation du menu si nécessaire
    }

    @FXML
    private void handleStartGame(ActionEvent event) {
        try {
            // Charger la scène du jeu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game-view.fxml"));
            Parent gameRoot = loader.load();

            // Créer une nouvelle scène
            Scene gameScene = new Scene(gameRoot);

            // Obtenir la stage actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();


            // Changer la scène
            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - Jeu");

            // Optionnel : ajuster la taille de la fenêtre
            stage.sizeToScene();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du jeu");
        }
    }

    @FXML
    private void handleOptions(ActionEvent event) {
        try {
            // Charger la scène des options
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/options.fxml"));
            Parent optionsRoot = loader.load();

            // Créer une nouvelle scène
            Scene optionsScene = new Scene(optionsRoot);

            // Obtenir la stage actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Changer la scène
            stage.setScene(optionsScene);
            stage.setTitle("Bomberman JavaFX - Options");

            // Ajuster la taille de la fenêtre
            stage.sizeToScene();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des options");
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        // Fermer l'application
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void startOnePlayerGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game-view.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur du jeu et définir le mode 1 joueur
            GameViewController gameController = loader.getController();
            gameController.setGameMode(GameMode.ONE_PLAYER);

            Stage stage = (Stage) onePlayerBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Bomberman - 1 Joueur");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}