package com.superbomberman.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {

    @FXML
    private Button guestButton;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Button exitButton;

    @FXML
    private void handleGuestMode(ActionEvent event) {
        navigateToMenu(event);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        navigateToAuth(event, "login");
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        navigateToAuth(event, "register");
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void navigateToMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent menuRoot = loader.load();
            Scene menuScene = new Scene(menuRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(menuScene);
            stage.setTitle("Super Bomberman - Menu Principal");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du menu principal");
        }
    }

    private void navigateToAuth(ActionEvent event, String mode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth.fxml"));
            Parent authRoot = loader.load();

            // Optionnel : configurer l'onglet actif selon le mode
            // AuthController authController = loader.getController();
            // authController.setActiveTab(mode);

            Scene authScene = new Scene(authRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(authScene);
            stage.setTitle("Super Bomberman - Authentification");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'authentification");
        }
    }
}