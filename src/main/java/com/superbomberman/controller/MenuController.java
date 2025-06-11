package com.superbomberman.controller;

import com.superbomberman.model.User;
import com.superbomberman.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.io.IOException;

public class MenuController {

    public static boolean isOnePlayer = false;
    private User currentUser;
    private AuthService authService;

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
    private Button logoutButton;
    @FXML
    private Button statsButton; // 🎯 NOUVEAU : Bouton statistiques
    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        authService = new AuthService();
        updateUI();
    }

    /**
     * Met à jour l'interface en fonction de l'état de connexion
     */
    private void updateUI() {
        if (currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getUsername() + " !");
            logoutButton.setVisible(true);
            // 🎯 NOUVEAU : Afficher le bouton stats seulement si connecté
            if (statsButton != null) {
                statsButton.setVisible(true);
            }
        } else if (authService.isLoggedIn()) {
            currentUser = authService.getCurrentUser();
            welcomeLabel.setText("Bienvenue, " + currentUser.getUsername() + " !");
            logoutButton.setVisible(true);
            if (statsButton != null) {
                statsButton.setVisible(true);
            }
        } else {
            welcomeLabel.setText("Mode Invité");
            logoutButton.setVisible(false);
            if (statsButton != null) {
                statsButton.setVisible(false);
            }
        }
    }

    /**
     * Définit l'utilisateur actuel (appelé depuis AuthController)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUI();
    }

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

            // Passer l'utilisateur au contrôleur de jeu
            GameViewController gameController = loader.getController();
            if (currentUser != null) {
                gameController.setCurrentUser(currentUser);
            }

            // MODIFICATION : Créer une scène avec une taille fixe plus grande que le jeu
            Scene gameScene = new Scene(gameRoot, 1700, 1000); // Taille fixe de la fenêtre
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - " + (isOnePlayer ? "1 Joueur" : "2 Joueurs"));

            // MODIFICATION : Configurer la fenêtre
            stage.setResizable(false); // Empêcher le redimensionnement
            stage.setWidth(1700);      // Largeur fixe
            stage.setHeight(1000);      // Hauteur fixe
            stage.centerOnScreen();    // Centrer la fenêtre

            // Ne plus utiliser sizeToScene() car on veut une taille fixe
            // stage.sizeToScene(); // À SUPPRIMER

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du jeu");
        }
    }

    // 🎯 NOUVEAU : Gestionnaire pour les statistiques
    @FXML
    private void handleStats(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/stats.fxml"));
            Parent statsRoot = loader.load();

            // Passer l'utilisateur actuel au contrôleur des stats
            StatsController statsController = loader.getController();
            if (currentUser != null) {
                statsController.initializeWithUser(currentUser.getUsername());
            }

            Scene statsScene = new Scene(statsRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(statsScene);
            stage.setTitle("Super Bomberman - Statistiques");
            stage.sizeToScene();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des statistiques");
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
    private void handleLogout(ActionEvent event) {
        authService.logout();
        navigateToAuth(event);
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleEditor(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LevelEditor.fxml"));
            Parent editorRoot = loader.load();

            Scene editorScene = new Scene(editorRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(editorScene);
            stage.setTitle("Super Bomberman - Éditeur de niveaux");
            stage.sizeToScene();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'éditeur");
        }
    }

    @FXML
    private void handleBackToMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/welcome.fxml"));
            Parent welcomeRoot = loader.load();

            Scene welcomeScene = new Scene(welcomeRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(welcomeScene);
            stage.setTitle("Super Bomberman - Accueil");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du retour à l'accueil");
        }
    }

    /**
     * Navigation vers l'écran d'authentification
     */
    private void navigateToAuth(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth.fxml"));
            Parent authRoot = loader.load();

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