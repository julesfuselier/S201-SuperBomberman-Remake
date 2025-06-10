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
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

import java.io.IOException;

/**
 * Contrôleur pour l'écran de connexion.
 * Gère l'authentification utilisateur et la navigation vers le menu principal.
 */
public class AuthController {

    @FXML private VBox loginForm;

    // Éléments de connexion
    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Button loginButton;
    @FXML private Label loginMessage;
    @FXML private CheckBox rememberMe;
    @FXML private Button registerPageButton;

    // Bouton de navigation
    @FXML private Button exitButton;

    private AuthService authService;

    @FXML
    public void initialize() {
        authService = new AuthService();
        setupKeyboardShortcuts();
        setupAnimations();

        // Tentative de restauration de session avec connexion automatique
        if (authService.restoreSession()) {
            // Connexion automatique si l'utilisateur a coché "Se souvenir de moi"
            showAutoLoginMessage();

            // Redirection automatique vers le menu après 2 secondes
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                try {
                    navigateToMainMenuDirect();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }));
            timeline.play();
        }
    }
    /**
     * Affiche un message de connexion automatique
     */
    private void showAutoLoginMessage() {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            showLoginMessage("Connexion automatique... Bienvenue " + currentUser.getUsername() + " !", true);
        }
    }

    @FXML
    private void handleGoToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent registerRoot = loader.load();

            Scene registerScene = new Scene(registerRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(registerScene);
            stage.setTitle("Super Bomberman - Inscription");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la page d'inscription");
        }
    }

    /**
     * Navigation directe vers le menu principal (sans événement)
     */
    private void navigateToMainMenuDirect() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
        Parent menuRoot = loader.load();

        // Passer les informations utilisateur au contrôleur du menu
        MenuController menuController = loader.getController();
        if (authService.isLoggedIn()) {
            menuController.setCurrentUser(authService.getCurrentUser());
        }

        Scene menuScene = new Scene(menuRoot);

        // Récupérer le stage depuis n'importe quel élément de la scène actuelle
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(menuScene);
        stage.setTitle("Super Bomberman - Menu Principal");
    }

    /**
     * Configure les raccourcis clavier
     */
    private void setupKeyboardShortcuts() {
        // Entrée pour se connecter
        loginPassword.setOnKeyPressed(this::handleKeyPressed);
        loginUsername.setOnKeyPressed(this::handleKeyPressed);
    }

    /**
     * Configure les animations des boutons
     */
    private void setupAnimations() {
        setupButtonAnimation(loginButton);
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

        // Tentative de connexion AVEC l'état de "Se souvenir de moi"
        if (authService.login(username, password, rememberMe.isSelected())) {
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
     * Ferme l'application
     */
    @FXML
    private void handleExit(ActionEvent event) {
        // Confirmation de fermeture
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Quitter l'application");
        confirmAlert.setHeaderText("Êtes-vous sûr de vouloir quitter ?");
        confirmAlert.setContentText("Toute progression non sauvegardée sera perdue.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            }
        });
    }

    /**
     * Navigation vers le menu principal
     * MÉTHODE CORRIGÉE : gère le cas où event peut être null
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

            // CORRECTION : obtenir le Stage de manière sécurisée
            Stage stage = getCurrentStage(event);
            if (stage == null) {
                System.err.println("Impossible de récupérer la fenêtre actuelle");
                return;
            }

            // Animation de transition
            FadeTransition sceneTransition = new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
            sceneTransition.setFromValue(1.0);
            sceneTransition.setToValue(0.0);
            sceneTransition.setOnFinished(e -> {
                stage.setScene(menuScene);
                stage.setTitle("Super Bomberman - Menu Principal");

                // Animation d'entrée pour la nouvelle scène
                FadeTransition enterTransition = new FadeTransition(Duration.millis(300), menuRoot);
                enterTransition.setFromValue(0.0);
                enterTransition.setToValue(1.0);
                enterTransition.play();
            });
            sceneTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showLoginMessage("Erreur lors du chargement du menu principal.", false);
            resetLoginButton();
        }
    }

    /**
     * NOUVELLE MÉTHODE : Obtient le Stage actuel de manière sécurisée
     */
    private Stage getCurrentStage(ActionEvent event) {
        // Méthode 1 : Si event n'est pas null, utiliser la source
        if (event != null && event.getSource() instanceof Node) {
            return (Stage) ((Node) event.getSource()).getScene().getWindow();
        }

        // Méthode 2 : Utiliser un des éléments FXML du contrôleur
        if (loginButton != null && loginButton.getScene() != null) {
            return (Stage) loginButton.getScene().getWindow();
        }

        // Méthode 3 : Utiliser loginForm comme fallback
        if (loginForm != null && loginForm.getScene() != null) {
            return (Stage) loginForm.getScene().getWindow();
        }

        // Méthode 4 : Utiliser exitButton comme dernière option
        if (exitButton != null && exitButton.getScene() != null) {
            return (Stage) exitButton.getScene().getWindow();
        }

        return null;
    }

    /**
     * Affiche un message de connexion
     */
    private void showLoginMessage(String message, boolean isSuccess) {
        loginMessage.setText(message);
        loginMessage.setStyle(isSuccess ?
                "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;" :
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Animation d'apparition
        FadeTransition fade = new FadeTransition(Duration.millis(300), loginMessage);
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
     * Remet le bouton de connexion dans son état normal
     */
    private void resetLoginButton() {
        loginButton.setDisable(false);
        loginButton.setText("SE CONNECTER");
    }

    /**
     * MÉTHODE CORRIGÉE : Affiche un message de bienvenue pour les sessions restaurées
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

            // Créer des boutons personnalisés
            ButtonType goToMenuButton = new ButtonType("Continuer");
            ButtonType stayHereButton = new ButtonType("Changer de Compte", ButtonBar.ButtonData.CANCEL_CLOSE);

            welcomeAlert.getButtonTypes().clear();
            welcomeAlert.getButtonTypes().addAll(goToMenuButton, stayHereButton);

            welcomeAlert.showAndWait().ifPresent(response -> {
                if (response == goToMenuButton) {
                    // CORRECTION : Passer null comme event est maintenant géré
                    navigateToMainMenu(null);
                }
            });
        }
    }

    /**
     * Vide les champs de connexion
     */
    public void clearFields() {
        loginUsername.clear();
        loginPassword.clear();
        rememberMe.setSelected(false);
        loginMessage.setText("");
    }

    /**
     * Met le focus sur le champ nom d'utilisateur
     */
    public void focusUsernameField() {
        loginUsername.requestFocus();
    }
}