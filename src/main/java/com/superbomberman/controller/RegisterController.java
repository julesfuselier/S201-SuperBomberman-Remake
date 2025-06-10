package com.superbomberman.controller;

import com.superbomberman.service.AuthService;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Contrôleur pour la page d'inscription dédiée.
 * Gère la création de nouveaux comptes utilisateur.
 *
 * @author Hugo Brest Lestrade
 * @version 1.3
 */
public class RegisterController {

    @FXML private VBox registerForm;

    // Éléments d'inscription
    @FXML private TextField registerUsername;
    @FXML private TextField registerEmail;
    @FXML private PasswordField registerPassword;
    @FXML private PasswordField confirmPassword;
    @FXML private ComboBox<String> favoriteCharacterComboBox;
    @FXML private Button registerButton;
    @FXML private Label registerMessage;

    // Boutons de navigation
    @FXML private Button backToLoginButton;
    @FXML private Button guestButton;
    @FXML private Button exitButton;

    private AuthService authService;

    @FXML
    public void initialize() {
        authService = new AuthService();
        setupKeyboardShortcuts();
        setupAnimations();
        setupFormValidation();

        // Initialiser le ComboBox avec une valeur par défaut
        if (favoriteCharacterComboBox.getValue() == null) {
            favoriteCharacterComboBox.setValue("Bomberman");
        }
    }

    /**
     * Configure les raccourcis clavier
     */
    private void setupKeyboardShortcuts() {
        // Entrée pour s'inscrire
        confirmPassword.setOnKeyPressed(this::handleRegisterKeyPressed);
        registerPassword.setOnKeyPressed(this::handleRegisterKeyPressed);
        registerUsername.setOnKeyPressed(this::handleRegisterKeyPressed);
        registerEmail.setOnKeyPressed(this::handleRegisterKeyPressed);
    }

    /**
     * Configure les animations des boutons
     */
    private void setupAnimations() {
        setupButtonAnimation(registerButton);
        setupButtonAnimation(backToLoginButton);
        setupButtonAnimation(exitButton);
    }

    /**
     * Configure la validation en temps réel
     */
    private void setupFormValidation() {
        // Validation en temps réel du nom d'utilisateur
        registerUsername.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.isEmpty()) {
                if (newText.length() < 3) {
                    registerUsername.setStyle("-fx-border-color: #e74c3c; -fx-background-color: #2c3e50; -fx-text-fill: #ecf0f1; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                } else if (!newText.matches("^[a-zA-Z0-9_-]+$")) {
                    registerUsername.setStyle("-fx-border-color: #f39c12; -fx-background-color: #2c3e50; -fx-text-fill: #ecf0f1; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                } else {
                    registerUsername.setStyle("-fx-border-color: #27ae60; -fx-background-color: #2c3e50; -fx-text-fill: #ecf0f1; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                }
            } else {
                registerUsername.setStyle("-fx-border-color: #34495e; -fx-background-color: #2c3e50; -fx-text-fill: #ecf0f1; -fx-border-radius: 8px; -fx-background-radius: 8px;");
            }
        });

        // Validation des mots de passe
        confirmPassword.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.isEmpty() && !registerPassword.getText().isEmpty()) {
                if (newText.equals(registerPassword.getText())) {
                    confirmPassword.setStyle("-fx-border-color: #27ae60; -fx-background-color: #2c3e50; -fx-text-fill: #ecf0f1; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                } else {
                    confirmPassword.setStyle("-fx-border-color: #e74c3c; -fx-background-color: #2c3e50; -fx-text-fill: #ecf0f1; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                }
            }
        });
    }

    /**
     * Ajoute une animation hover à un bouton
     */
    private void setupButtonAnimation(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    /**
     * Gère les touches pressées sur les champs d'inscription
     */
    private void handleRegisterKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleRegister(null);
        }
    }

    /**
     * Gère la tentative d'inscription
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        String username = registerUsername.getText().trim();
        String email = registerEmail.getText().trim();
        String password = registerPassword.getText();
        String confirmPwd = confirmPassword.getText();
        String favoriteCharacter = favoriteCharacterComboBox.getValue();

        // Validation des champs
        if (username.isEmpty() || password.isEmpty() || confirmPwd.isEmpty()) {
            showRegisterMessage("Veuillez remplir tous les champs obligatoires.", false);
            return;
        }

        // Validation du nom d'utilisateur
        if (username.length() < 3) {
            showRegisterMessage("Le nom d'utilisateur doit contenir au moins 3 caractères.", false);
            shakeNode(registerUsername);
            return;
        }

        if (!username.matches("^[a-zA-Z0-9_-]+$")) {
            showRegisterMessage("Le nom d'utilisateur ne peut contenir que des lettres, chiffres, tirets et underscores.", false);
            shakeNode(registerUsername);
            return;
        }

        // Validation du mot de passe
        if (password.length() < 4) {
            showRegisterMessage("Le mot de passe doit contenir au moins 4 caractères.", false);
            shakeNode(registerPassword);
            return;
        }

        if (!password.equals(confirmPwd)) {
            showRegisterMessage("Les mots de passe ne correspondent pas.", false);
            shakeNode(confirmPassword);
            return;
        }

        // Validation de l'email (optionnel mais format)
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showRegisterMessage("Format d'email invalide.", false);
            shakeNode(registerEmail);
            return;
        }

        // Tentative d'inscription (utiliser la méthode existante sans personnage favori pour l'instant)
        if (authService.register(username, password, email)) {
            showRegisterMessage("Inscription réussie ! Bienvenue " + username + " !", true);

            // Animation de succès puis navigation
            FadeTransition fade = new FadeTransition(Duration.millis(1500), registerMessage);
            fade.setFromValue(1.0);
            fade.setToValue(0.3);
            fade.setOnFinished(e -> navigateToMainMenu(event));
            fade.play();

        } else {
            showRegisterMessage("Ce nom d'utilisateur existe déjà.", false);
            shakeNode(registerUsername);
        }
    }

    /**
     * Retour à la page de connexion
     */
    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth.fxml"));
            Parent authRoot = loader.load();

            Scene authScene = new Scene(authRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(authScene);
            stage.setTitle("Super Bomberman - Authentification");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la page de connexion");
        }
    }

    @FXML
    private void handleBackToMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/welcome.fxml"));
            Parent welcomeRoot = loader.load();

            // Pas besoin de cast vers MenuController, c'est un WelcomeController
            // WelcomeController welcomeController = loader.getController();
            // Vous pouvez ajouter des informations si nécessaire dans WelcomeController

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
     * Permet de continuer en tant qu'invité
     */
    @FXML
    private void handleGuestMode(ActionEvent event) {
        navigateToMainMenu(event);
    }

    /**
     * Ferme l'application
     */
    @FXML
    private void handleExit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Navigation vers le menu principal
     */
    private void navigateToMainMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent menuRoot = loader.load();

            // Passer les informations utilisateur au contrôleur du menu
            MenuController menuController = loader.getController();
            if (authService.isLoggedIn()) {
                menuController.setCurrentUser(authService.getCurrentUser());
            }

            Scene menuScene = new Scene(menuRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(menuScene);
            stage.setTitle("Super Bomberman - Menu Principal");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du menu principal");
        }
    }

    /**
     * Affiche un message d'inscription
     */
    private void showRegisterMessage(String message, boolean isSuccess) {
        registerMessage.setText(message);
        registerMessage.setStyle(isSuccess ?
                "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 16px;" :
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 16px;");

        // Animation d'apparition
        FadeTransition fade = new FadeTransition(Duration.millis(300), registerMessage);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    /**
     * Animation de shake pour les erreurs
     */
    private void shakeNode(Node node) {
        ScaleTransition shake = new ScaleTransition(Duration.millis(100), node);
        shake.setFromX(1.0);
        shake.setToX(1.05);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }
}