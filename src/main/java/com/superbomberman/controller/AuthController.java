package com.superbomberman.controller;

import com.superbomberman.model.User;
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
 * Contrôleur pour l'écran d'authentification (connexion/inscription).
 * Gère les interactions utilisateur et la navigation vers le menu principal.
 */
public class AuthController {

    @FXML private VBox loginForm;
    @FXML private VBox registerForm;
    @FXML private TabPane authTabPane;

    // Éléments de connexion
    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Button loginButton;
    @FXML private Label loginMessage;
    @FXML private CheckBox rememberMe;

    // Éléments d'inscription
    @FXML private TextField registerUsername;
    @FXML private TextField registerEmail;
    @FXML private PasswordField registerPassword;
    @FXML private PasswordField confirmPassword;
    @FXML private Button registerButton;
    @FXML private Label registerMessage;

    // Boutons de navigation
    @FXML private Button guestButton;
    @FXML private Button exitButton;

    private AuthService authService;

    @FXML
    public void initialize() {
        authService = new AuthService();
        setupKeyboardShortcuts();
        setupAnimations();

        // Tentative de restauration de session
        if (authService.restoreSession()) {
            showWelcomeMessage();
        }
    }

    /**
     * Configure les raccourcis clavier
     */
    private void setupKeyboardShortcuts() {
        // Entrée pour se connecter
        loginPassword.setOnKeyPressed(this::handleKeyPressed);
        loginUsername.setOnKeyPressed(this::handleKeyPressed);

        // Entrée pour s'inscrire
        confirmPassword.setOnKeyPressed(this::handleRegisterKeyPressed);
    }

    /**
     * Configure les animations des boutons
     */
    private void setupAnimations() {
        setupButtonAnimation(loginButton);
        setupButtonAnimation(registerButton);
        setupButtonAnimation(guestButton);
        setupButtonAnimation(exitButton);
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
     * Gère les touches pressées sur les champs de connexion
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin(null);
        }
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
     * Gère la tentative de connexion
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText();

        // Validation des champs
        if (username.isEmpty() || password.isEmpty()) {
            showLoginMessage("Veuillez remplir tous les champs.", false);
            return;
        }

        // Tentative de connexion
        if (authService.login(username, password)) {
            showLoginMessage("Connexion réussie ! Bienvenue " + username + " !", true);

            // Animation de succès puis navigation
            FadeTransition fade = new FadeTransition(Duration.millis(1000), loginMessage);
            fade.setFromValue(1.0);
            fade.setToValue(0.3);
            fade.setOnFinished(e -> navigateToMainMenu(event));
            fade.play();

        } else {
            showLoginMessage("Nom d'utilisateur ou mot de passe incorrect.", false);
            // Animation de shake pour les champs
            shakeNode(loginUsername);
            shakeNode(loginPassword);
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

        // Validation des champs
        if (username.isEmpty() || password.isEmpty() || confirmPwd.isEmpty()) {
            showRegisterMessage("Veuillez remplir tous les champs obligatoires.", false);
            return;
        }

        // Validation du nom d'utilisateur
        if (username.length() < 3) {
            showRegisterMessage("Le nom d'utilisateur doit contenir au moins 3 caractères.", false);
            return;
        }

        if (!username.matches("^[a-zA-Z0-9_-]+$")) {
            showRegisterMessage("Le nom d'utilisateur ne peut contenir que des lettres, chiffres, tirets et underscores.", false);
            return;
        }

        // Validation du mot de passe
        if (password.length() < 4) {
            showRegisterMessage("Le mot de passe doit contenir au moins 4 caractères.", false);
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
            return;
        }

        // Tentative d'inscription
        if (authService.register(username, password, email)) {
            showRegisterMessage("Inscription réussie ! Bienvenue " + username + " !", true);

            // Animation de succès puis navigation
            FadeTransition fade = new FadeTransition(Duration.millis(1000), registerMessage);
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

            // Passer les informations utilisateur au contrôleur du menu si nécessaire
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
     * Affiche un message de connexion
     */
    private void showLoginMessage(String message, boolean isSuccess) {
        loginMessage.setText(message);
        loginMessage.setStyle(isSuccess ?
                "-fx-text-fill: #27ae60; -fx-font-weight: bold;" :
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        // Animation d'apparition
        FadeTransition fade = new FadeTransition(Duration.millis(300), loginMessage);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    /**
     * Affiche un message d'inscription
     */
    private void showRegisterMessage(String message, boolean isSuccess) {
        registerMessage.setText(message);
        registerMessage.setStyle(isSuccess ?
                "-fx-text-fill: #27ae60; -fx-font-weight: bold;" :
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

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

    /**
     * Affiche un message de bienvenue pour les sessions restaurées
     */
    private void showWelcomeMessage() {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            Alert welcomeAlert = new Alert(Alert.AlertType.INFORMATION);
            welcomeAlert.setTitle("Session restaurée");
            welcomeAlert.setHeaderText("Bon retour, " + currentUser.getUsername() + " !");
            welcomeAlert.setContentText("Votre session précédente a été restaurée.\n" +
                    "Parties jouées : " + currentUser.getGamesPlayed() + "\n" +
                    "Taux de victoire : " + String.format("%.1f", currentUser.getWinRate()) + "%");

            welcomeAlert.showAndWait();
        }
    }
}
