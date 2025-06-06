package com.superbomberman.controller;

import com.superbomberman.model.GameMode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.application.Platform;

public class MenuController {

    @FXML
    private Button onePlayerBtn;

    @FXML
    private Button twoPlayerBtn;

    @FXML
    private Button quitBtn;

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

    @FXML
    private void startTwoPlayerGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game-view.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur du jeu et définir le mode 2 joueurs
            GameViewController gameController = loader.getController();
            gameController.setGameMode(GameMode.TWO_PLAYER);

            Stage stage = (Stage) twoPlayerBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Bomberman - 2 Joueurs");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void quitGame() {
        Platform.exit();
    }
}