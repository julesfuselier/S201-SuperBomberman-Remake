package com.superbomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

/**
 * Point d'entrée principal de l'application Super Bomberman.
 * <p>
 * Cette classe initialise l'interface graphique JavaFX, charge la page d'accueil (welcome.fxml)
 * et lance la fenêtre principale de l'application.
 * </p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-08
 */
public class Main extends Application {
    /**
     * Démarre l'application JavaFX et affiche la page d'accueil.
     *
     * @param primaryStage la fenêtre principale de l'application
     * @throws Exception en cas d'erreur de chargement du FXML
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger la page d'accueil avec les 4 boutons
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/welcome.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Super Bomberman - Accueil");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Méthode main : point d'entrée standard Java.
     *
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }
}