package com.superbomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le menu principal au démarrage au lieu du jeu directement
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Bomberman JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Optionnel pour éviter le redimensionnement
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}