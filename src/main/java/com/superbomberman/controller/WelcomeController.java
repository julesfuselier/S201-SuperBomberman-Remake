/**
 * Contrôleur pour l'écran d'accueil de Super Bomberman.
 * <p>
 * Permet à l'utilisateur de choisir entre jouer en tant qu'invité, se connecter, s'inscrire ou quitter l'application.
 * Gère la navigation vers le menu principal ou les écrans d'authentification/inscription.
 * </p>
 *
 * <ul>
 *     <li>Bouton Invité : accès direct au menu principal sans authentification</li>
 *     <li>Bouton Connexion : accès à la page de connexion</li>
 *     <li>Bouton Inscription : accès à la page d'inscription</li>
 *     <li>Bouton Quitter : ferme l'application</li>
 * </ul>
 *
 * @author Hugo Brest Lestrade
 * @version 1.3
 **/
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

/**
 * Contrôleur principal pour la page d'accueil (welcome).
 * Gère la navigation vers les autres vues selon le choix de l'utilisateur.
 */
public class WelcomeController {

    /** Bouton pour jouer en mode invité. */
    @FXML
    private Button guestButton;
    /** Bouton pour accéder à la connexion. */
    @FXML
    private Button loginButton;
    /** Bouton pour accéder à l'inscription. */
    @FXML
    private Button registerButton;
    /** Bouton pour quitter l'application. */
    @FXML
    private Button exitButton;

    /**
     * Gère le clic sur "Invité" et lance le menu principal sans authentification.
     * @param event événement de clic
     */
    @FXML
    private void handleGuestMode(ActionEvent event) {
        navigateToMenu(event);
    }

    /**
     * Gère le clic sur "Connexion" et affiche la page d'authentification.
     * @param event événement de clic
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        navigateToAuth(event, "auth");
    }

    /**
     * Gère le clic sur "Inscription" et affiche la page d'inscription.
     * @param event événement de clic
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        navigateToAuth(event, "register");
    }

    /**
     * Gère le clic sur "Quitter" et ferme l'application.
     * @param event événement de clic
     */
    @FXML
    private void handleExit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Navigation vers le menu principal.
     * @param event événement ayant déclenché la navigation
     */
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

    /**
     * Navigation vers l'écran de connexion ou d'inscription selon le mode ("auth" ou "register").
     * @param event événement ayant déclenché la navigation
     * @param mode nom du fichier FXML à charger ("auth" ou "register")
     */
    private void navigateToAuth(ActionEvent event, String mode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + mode + ".fxml"));
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