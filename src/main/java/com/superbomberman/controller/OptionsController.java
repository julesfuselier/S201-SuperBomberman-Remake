package com.superbomberman.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hugo Brest Lestrade
 * @version 1.4
 **/
public class OptionsController {

    @FXML private ComboBox<String> difficultyComboBox;

    // Boutons de contrôle Joueur 1
    @FXML private Button upKeyButton1;
    @FXML private Button downKeyButton1;
    @FXML private Button leftKeyButton1;
    @FXML private Button rightKeyButton1;
    @FXML private Button bombKeyButton1;

    // Boutons de contrôle Joueur 2
    @FXML private Button upKeyButton2;
    @FXML private Button downKeyButton2;
    @FXML private Button leftKeyButton2;
    @FXML private Button rightKeyButton2;
    @FXML private Button bombKeyButton2;

    // Bouton pause commun
    @FXML private Button pauseKeyButton;

    // Boutons d'action
    @FXML private Button applyButton;
    @FXML private Button resetButton;
    @FXML private Button backButton;

    // Variables pour stocker les paramètres actuels
    private static String difficulty = "Normal";
    private static double gameSpeed = 3.0;

    // Touches par défaut Joueur 1
    private static String upKey1 = "UP";
    private static String downKey1 = "DOWN";
    private static String leftKey1 = "LEFT";
    private static String rightKey1 = "RIGHT";
    private static String bombKey1 = "SPACE";

    // Touches par défaut Joueur 2
    private static String upKey2 = "Z";
    private static String downKey2 = "S";
    private static String leftKey2 = "Q";
    private static String rightKey2 = "D";
    private static String bombKey2 = "ENTER";

    @FXML
    private ComboBox<String> imageTheme; // Lien avec le FXML

    private static String selectedImageTheme = "classique"; // Valeur globale accessible

    // --- Observer/Listener pour le changement de thème
    public interface ThemeChangeListener {
        void onThemeChanged(String newTheme);
    }
    private static final List<ThemeChangeListener> themeChangeListeners = new ArrayList<>();
    public static void addThemeChangeListener(ThemeChangeListener listener) {
        themeChangeListeners.add(listener);
    }
    public static void removeThemeChangeListener(ThemeChangeListener listener) {
        themeChangeListeners.remove(listener);
    }
    private static void notifyThemeChanged(String newTheme) {
        for (ThemeChangeListener listener : themeChangeListeners) {
            listener.onThemeChanged(newTheme);
        }
    }

    public static String getImageTheme() {
        return selectedImageTheme;
    }
    public static void setImageTheme(String theme) {
        selectedImageTheme = theme;
        notifyThemeChanged(theme); // Notifie tous les listeners
    }

    @FXML
    public void initialize() {
        updateButtonTexts();
        setupControlButtons();

        if (imageTheme != null) {
            // Ajoute les thèmes si besoin (à faire UNE fois)
            if (imageTheme.getItems().isEmpty()) {
                imageTheme.getItems().addAll("Bomberman", "Theme1", "Theme2");
            }
            // Ne change la valeur QUE si c'est nécessaire
            if (!selectedImageTheme.equals(imageTheme.getValue())) {
                imageTheme.setValue(selectedImageTheme);
            }
            imageTheme.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.equals(selectedImageTheme)) {
                    setImageTheme(newVal); // Appelle le setter qui notifie
                }
            });
        }
    }

    private void updateButtonTexts() {
        // Joueur 1
        upKeyButton1.setText(upKey1);
        downKeyButton1.setText(downKey1);
        leftKeyButton1.setText(leftKey1);
        rightKeyButton1.setText(rightKey1);
        bombKeyButton1.setText(bombKey1);

        // Joueur 2
        upKeyButton2.setText(upKey2);
        downKeyButton2.setText(downKey2);
        leftKeyButton2.setText(leftKey2);
        rightKeyButton2.setText(rightKey2);
        bombKeyButton2.setText(bombKey2);

        // Pause
    }

    private void setupControlButtons() {
        // Joueur 1
        upKeyButton1.setOnAction(e -> showKeyBindingDialog("Joueur 1 - Touche Haut", upKeyButton1, "upKey1"));
        downKeyButton1.setOnAction(e -> showKeyBindingDialog("Joueur 1 - Touche Bas", downKeyButton1, "downKey1"));
        leftKeyButton1.setOnAction(e -> showKeyBindingDialog("Joueur 1 - Touche Gauche", leftKeyButton1, "leftKey1"));
        rightKeyButton1.setOnAction(e -> showKeyBindingDialog("Joueur 1 - Touche Droite", rightKeyButton1, "rightKey1"));
        bombKeyButton1.setOnAction(e -> showKeyBindingDialog("Joueur 1 - Touche Bombe", bombKeyButton1, "bombKey1"));

        // Joueur 2
        upKeyButton2.setOnAction(e -> showKeyBindingDialog("Joueur 2 - Touche Haut", upKeyButton2, "upKey2"));
        downKeyButton2.setOnAction(e -> showKeyBindingDialog("Joueur 2 - Touche Bas", downKeyButton2, "downKey2"));
        leftKeyButton2.setOnAction(e -> showKeyBindingDialog("Joueur 2 - Touche Gauche", leftKeyButton2, "leftKey2"));
        rightKeyButton2.setOnAction(e -> showKeyBindingDialog("Joueur 2 - Touche Droite", rightKeyButton2, "rightKey2"));
        bombKeyButton2.setOnAction(e -> showKeyBindingDialog("Joueur 2 - Touche Bombe", bombKeyButton2, "bombKey2"));

    }

    private void showKeyBindingDialog(String keyName, Button button, String keyVariable) {
        TextInputDialog dialog = new TextInputDialog(button.getText());
        dialog.setTitle("Configuration des touches");
        dialog.setHeaderText("Personnalisation de: " + keyName);
        dialog.setContentText("Entrez la nouvelle touche:");

        dialog.showAndWait().ifPresent(newKey -> {
            if (!newKey.trim().isEmpty()) {
                String upperKey = newKey.toUpperCase().trim();
                button.setText(upperKey);

                // Mettre à jour la variable correspondante
                switch (keyVariable) {
                    case "upKey1": upKey1 = upperKey; break;
                    case "downKey1": downKey1 = upperKey; break;
                    case "leftKey1": leftKey1 = upperKey; break;
                    case "rightKey1": rightKey1 = upperKey; break;
                    case "bombKey1": bombKey1 = upperKey; break;
                    case "upKey2": upKey2 = upperKey; break;
                    case "downKey2": downKey2 = upperKey; break;
                    case "leftKey2": leftKey2 = upperKey; break;
                    case "rightKey2": rightKey2 = upperKey; break;
                    case "bombKey2": bombKey2 = upperKey; break;
                }
            }
        });
    }

    @FXML
    private void handleApply(ActionEvent event) {
        // Afficher une confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Paramètres appliqués");
        alert.setHeaderText(null);
        alert.setContentText("Les paramètres ont été sauvegardés avec succès !");
        alert.showAndWait();

        System.out.println("Paramètres appliqués:");
        System.out.println("Difficulté: " + difficulty);
        System.out.println("Vitesse de jeu: " + gameSpeed);
        System.out.println("Touches Joueur 1 - Haut: " + upKey1 + ", Bas: " + downKey1 + ", Gauche: " + leftKey1 + ", Droite: " + rightKey1 + ", Bombe: " + bombKey1);
        System.out.println("Touches Joueur 2 - Haut: " + upKey2 + ", Bas: " + downKey2 + ", Gauche: " + leftKey2 + ", Droite: " + rightKey2 + ", Bombe: " + bombKey2);
        System.out.println("Thème d'images: " + selectedImageTheme);
    }

    @FXML
    private void handleReset(ActionEvent event) {
        // Confirmer le reset
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Réinitialiser les paramètres");
        confirmAlert.setHeaderText("Êtes-vous sûr de vouloir réinitialiser tous les paramètres ?");
        confirmAlert.setContentText("Cette action remettra tous les paramètres à leurs valeurs par défaut.");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            // Remettre les valeurs par défaut
            if (difficultyComboBox != null) difficultyComboBox.setValue("Normal");

            // Touches Joueur 1
            upKey1 = "UP";
            downKey1 = "DOWN";
            leftKey1 = "LEFT";
            rightKey1 = "RIGHT";
            bombKey1 = "SPACE";
            // Touches Joueur 2
            upKey2 = "Z";
            downKey2 = "S";
            leftKey2 = "Q";
            rightKey2 = "D";
            bombKey2 = "ENTER";
            // Thème
            setImageTheme("Bomberman");
            if (imageTheme != null) imageTheme.setValue("Bomberman");
            // Mettre à jour les textes des boutons
            updateButtonTexts();

            // Afficher confirmation
            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Paramètres réinitialisés");
            infoAlert.setHeaderText(null);
            infoAlert.setContentText("Tous les paramètres ont été remis à leurs valeurs par défaut.");
            infoAlert.showAndWait();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Retourner au menu principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent menuRoot = loader.load();

            Scene menuScene = new Scene(menuRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(menuScene);
            stage.setTitle("Super Bomberman - Menu Principal");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du retour au menu");
        }
    }

    // Méthodes statiques pour accéder aux paramètres depuis d'autres classes
    public static String getDifficulty() { return difficulty; }
    public static double getGameSpeed() { return gameSpeed; }

    // Méthodes pour accéder aux touches du Joueur 1
    public static String getUpKey1() { return upKey1; }
    public static String getDownKey1() { return downKey1; }
    public static String getLeftKey1() { return leftKey1; }
    public static String getRightKey1() { return rightKey1; }
    public static String getBombKey1() { return bombKey1; }

    // Méthodes pour accéder aux touches du Joueur 2
    public static String getUpKey2() { return upKey2; }
    public static String getDownKey2() { return downKey2; }
    public static String getLeftKey2() { return leftKey2; }
    public static String getRightKey2() { return rightKey2; }
    public static String getBombKey2() { return bombKey2; }

}