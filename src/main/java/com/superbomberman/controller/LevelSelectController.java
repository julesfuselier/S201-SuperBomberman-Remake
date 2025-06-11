package com.superbomberman.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class LevelSelectController {

    @FXML private ListView<String> levelListView;
    @FXML private Button backButton;
    @FXML private VBox mainContainer;

    private static final String LEVELS_DIRECTORY = "src/main/resources/maps";
    private String selectedLevel;

    @FXML
    public void initialize() {
        loadLevels();
        setupListView();
    }

    private void loadLevels() {
        try {
            // Crée le dossier levels s'il n'existe pas
            Path levelsPath = Paths.get(LEVELS_DIRECTORY);
            if (!Files.exists(levelsPath)) {
                Files.createDirectory(levelsPath);
            }

            // Charge tous les fichiers .txt du dossier
            List<String> levelFiles = Files.list(levelsPath)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());

            levelListView.getItems().clear();
            levelListView.getItems().addAll(levelFiles);

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des niveaux: " + e.getMessage());
        }
    }

    private void setupListView() {
        levelListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                selectedLevel = levelListView.getSelectionModel().getSelectedItem();
                if (selectedLevel != null) {
                    startGame(selectedLevel, (Stage) mainContainer.getScene().getWindow());
                }
            }
        });
    }

    @FXML
    private void handleBackToMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent menuRoot = loader.load();

            Scene menuScene = new Scene(menuRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(menuScene);
            stage.setTitle("Super Bomberman - Menu");

        } catch (IOException e) {
            System.err.println("Erreur lors du retour au menu: " + e.getMessage());
        }
    }

    private void startGame(String levelFile, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game-view.fxml"));
            Parent gameRoot = loader.load();

            GameViewController gameController = loader.getController();
            // Correction : Utiliser le chemin absolu du fichier
            String absolutePath = new File(LEVELS_DIRECTORY, levelFile).getAbsolutePath();
            gameController.setCustomLevel(new File(absolutePath));

            Scene gameScene = new Scene(gameRoot);
            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - " + levelFile);
            stage.sizeToScene();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du niveau: " + e.getMessage());
        }
    }
}