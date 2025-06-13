/**
 * Contrôleur du menu principal de Super Bomberman.
 * <p>
 * Gère la navigation entre les différentes vues du jeu (jeu, options, éditeur, accueil, authentification),
 * la gestion du mode un ou deux joueurs, et l'affichage de l'utilisateur courant.
 * Permet également la déconnexion, la sortie du jeu, et l'accès à l'éditeur de niveaux.
 * </p>
 *
 * @author HugoBrestLestrade
 * @see com.superbomberman.model.User
 * @see com.superbomberman.service.AuthService
 */
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

/**
 * Contrôleur principal du menu.
 * <ul>
 *     <li>Permet de lancer une partie en 1 ou 2 joueurs.</li>
 *     <li>Permet d'accéder aux options, à l'éditeur de niveaux, ou de se déconnecter.</li>
 *     <li>Affiche l'utilisateur connecté et gère la navigation entre les vues.</li>
 * </ul>
 */
public class MenuController {

    /** Indique si le mode un joueur est sélectionné (sinon, deux joueurs). */
    public static boolean isOnePlayer = false;
    /** Utilisateur actuellement connecté. */
    private User currentUser;
    /** Service d'authentification utilisé pour la gestion de session. */
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
    private Button logoutButton;  // Bouton de déconnexion
    @FXML
    private Label welcomeLabel;   // Label d'accueil

    /**
     * Initialise le contrôleur du menu (appelé après chargement du FXML).
     * Initialise le service d'authentification et rafraîchit l'affichage.
     */
    @FXML
    public void initialize() {
        authService = new AuthService();
        updateUI();
    }

    /**
     * Met à jour l'interface en fonction de l'état de connexion utilisateur.
     * Affiche le nom ou "Mode Invité" et affiche/cache le bouton de déconnexion.
     */
    private void updateUI() {
        if (currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getUsername() + " !");
            logoutButton.setVisible(true);
        } else if (authService.isLoggedIn()) {
            currentUser = authService.getCurrentUser();
            welcomeLabel.setText("Bienvenue, " + currentUser.getUsername() + " !");
            logoutButton.setVisible(true);
        } else {
            welcomeLabel.setText("Mode Invité");
            logoutButton.setVisible(false);
        }
    }

    /**
     * Définit l'utilisateur courant et met à jour l'interface.
     * (Méthode appelée depuis AuthController après connexion.)
     * @param user Utilisateur connecté
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUI();
    }

    /**
     * Déclenche le lancement du jeu en mode un joueur.
     * @param event événement ActionEvent du bouton
     */
    @FXML
    private void handleOnePlayer(ActionEvent event) {
        isOnePlayer = true;
        startGame(event);
    }

    /**
     * Déclenche le lancement du jeu en mode deux joueurs.
     * @param event événement ActionEvent du bouton
     */
    @FXML
    private void handleTwoPlayer(ActionEvent event) {
        isOnePlayer = false;
        startGame(event);
    }

    /**
     * Lance la vue du jeu (game-view.fxml) avec l'utilisateur courant.
     * Fixe la taille de la fenêtre et configure la scène.
     * @param event événement ActionEvent du bouton
     */
    private void startGame(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game-view.fxml"));
            Parent gameRoot = loader.load();

            // Passage de l'utilisateur au contrôleur du jeu
            GameViewController gameController = loader.getController();
            if (currentUser != null) {
                gameController.setCurrentUser(currentUser);
            }

            // Création de la scène avec une taille fixe
            Scene gameScene = new Scene(gameRoot, 1700, 1000);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - " + (isOnePlayer ? "1 Joueur" : "2 Joueurs"));
            stage.setResizable(false);
            stage.setWidth(1700);
            stage.setHeight(1000);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du jeu");
        }
    }

    /**
     * Affiche la vue des options (options.fxml).
     * @param event événement ActionEvent du bouton
     */
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

    /**
     * Déconnecte l'utilisateur courant et retourne à l'écran d'authentification.
     * @param event événement ActionEvent du bouton
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        authService.logout();
        navigateToAuth(event);
    }

    /**
     * Ferme l'application.
     * @param event événement ActionEvent du bouton
     */
    @FXML
    private void handleExit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Affiche l'éditeur de niveau (LevelEditor.fxml).
     * @param event événement ActionEvent du bouton
     */
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

    /**
     * Retourne à la vue d'accueil (welcome.fxml).
     * @param event événement ActionEvent du bouton
     */
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
     * Navigation vers l'écran d'authentification (auth.fxml).
     * @param event événement ActionEvent du bouton
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