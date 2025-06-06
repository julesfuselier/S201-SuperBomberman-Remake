package com.superbomberman.controller;

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

    public static boolean isOnePlayer = false;

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
    private void handleOnePlayer(ActionEvent event) {
        isOnePlayer = true;
        startGame(event);
    }

    @FXML
    private void handleTwoPlayer(ActionEvent event) {
        isOnePlayer = false;
        startGame(event);
    }

    private void startGame(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game-view.fxml"));
            Parent gameRoot = loader.load();

            Scene gameScene = new Scene(gameRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - " + (isOnePlayer ? "1 Joueur" : "2 Joueurs"));
            stage.sizeToScene();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du jeu");
        }
    }

    @FXML
    private void handleOptions(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/options.fxml"));
            Parent optionsRoot = loader.load();

            Scene optionsScene = new Scene(optionsRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(optionsScene);
            stage.setTitle("Bomberman JavaFX - Options");
            stage.sizeToScene();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des options");
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}