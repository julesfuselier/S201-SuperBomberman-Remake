package com.superbomberman.controller;

import com.superbomberman.model.TileType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Objects;

public class LevelEditorController {

    @FXML private GridPane editorGrid;
    @FXML private HBox toolbox;
    @FXML private ScrollPane scrollPane;
    @FXML private Button saveButton;
    @FXML private Button loadButton;
    @FXML private Button clearButton;

    private TileType selectedTile = TileType.FLOOR;
    private TileType[][] map;
    private static final int DEFAULT_WIDTH = 15;
    private static final int DEFAULT_HEIGHT = 13;
    private static final int CELL_SIZE = 50;
    private Point player1Position;
    private Point player2Position;

    private static class Point {
        int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @FXML
    public void initialize() {
        initializeMap();
        setupToolbox();
        setupGrid();
        setupButtons();
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
            e.printStackTrace();
            System.err.println("Erreur lors du retour au menu");
        }
    }

    private void initializeMap() {
        map = new TileType[DEFAULT_HEIGHT][DEFAULT_WIDTH];
        // Remplir d'abord toute la carte avec du sol
        for (int y = 0; y < DEFAULT_HEIGHT; y++) {
            for (int x = 0; x < DEFAULT_WIDTH; x++) {
                map[y][x] = TileType.FLOOR;
            }
        }

        // Ajouter le contour de murs incassables
        for (int x = 0; x < DEFAULT_WIDTH; x++) {
            map[0][x] = TileType.WALL;              // Mur du haut
            map[DEFAULT_HEIGHT-1][x] = TileType.WALL; // Mur du bas
        }
        for (int y = 0; y < DEFAULT_HEIGHT; y++) {
            map[y][0] = TileType.WALL;              // Mur de gauche
            map[y][DEFAULT_WIDTH-1] = TileType.WALL; // Mur de droite
        }

        player1Position = null;
        player2Position = null;
    }

    private void setupToolbox() {
        toolbox.setPadding(new Insets(10));
        toolbox.setSpacing(10);

        addToolButton("grass.png", TileType.FLOOR);
        addToolButton("wall.png", TileType.WALL);
        addToolButton("wall_breakable.png", TileType.WALL_BREAKABLE);
        addToolButton("player.png", TileType.PLAYER1);
        addToolButton("player2.png", TileType.PLAYER2);
        addToolButton("enemy.png", TileType.ENEMY);
    }

    private void addToolButton(String imagePath, TileType type) {
        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResource("/images/classique/" + imagePath)).toExternalForm());
            ImageView imageView = new ImageView(img);
            imageView.setFitWidth(40);
            imageView.setFitHeight(40);

            Button btn = new Button();
            btn.setGraphic(imageView);
            btn.setOnAction(e -> selectedTile = type);

            toolbox.getChildren().add(btn);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + imagePath);
        }
    }

    private void setupGrid() {
        editorGrid.getChildren().clear();

        for (int y = 0; y < DEFAULT_HEIGHT; y++) {
            for (int x = 0; x < DEFAULT_WIDTH; x++) {
                Button cell = createGridCell(x, y);
                editorGrid.add(cell, x, y);
                // Désactiver les cellules du contour
                if (y == 0 || y == DEFAULT_HEIGHT-1 || x == 0 || x == DEFAULT_WIDTH-1) {
                    cell.setDisable(true);
                    updateCellAppearance(cell, TileType.WALL);
                }
            }
        }
    }

    private Button createGridCell(int x, int y) {
        Button cell = new Button();
        cell.setPrefSize(CELL_SIZE, CELL_SIZE);
        cell.setMinSize(CELL_SIZE, CELL_SIZE);
        cell.setMaxSize(CELL_SIZE, CELL_SIZE);

        updateCellAppearance(cell, map[y][x]);

        // N'ajouter l'événement que si la cellule n'est pas sur le contour
        if (x > 0 && x < DEFAULT_WIDTH-1 && y > 0 && y < DEFAULT_HEIGHT-1) {
            cell.setOnMousePressed(e -> {
                if (selectedTile == TileType.PLAYER1) {
                    if (player1Position != null) {
                        map[player1Position.y][player1Position.x] = TileType.FLOOR;
                        Button oldCell = (Button) editorGrid.getChildren().get(player1Position.y * DEFAULT_WIDTH + player1Position.x);
                        updateCellAppearance(oldCell, TileType.FLOOR);
                    }
                    player1Position = new Point(x, y);
                    map[y][x] = selectedTile;
                    updateCellAppearance(cell, selectedTile);
                }
                else if (selectedTile == TileType.PLAYER2) {
                    if (player2Position != null) {
                        map[player2Position.y][player2Position.x] = TileType.FLOOR;
                        Button oldCell = (Button) editorGrid.getChildren().get(player2Position.y * DEFAULT_WIDTH + player2Position.x);
                        updateCellAppearance(oldCell, TileType.FLOOR);
                    }
                    player2Position = new Point(x, y);
                    map[y][x] = selectedTile;
                    updateCellAppearance(cell, selectedTile);
                }
                else {
                    if (player1Position != null && player1Position.x == x && player1Position.y == y) {
                        player1Position = null;
                    }
                    if (player2Position != null && player2Position.x == x && player2Position.y == y) {
                        player2Position = null;
                    }
                    map[y][x] = selectedTile;
                    updateCellAppearance(cell, selectedTile);
                }
            });
        }

        return cell;
    }

    private void updateCellAppearance(Button cell, TileType type) {
        String imagePath = switch (type) {
            case FLOOR -> "/images/classique/grass.png";
            case WALL -> "/images/classique/wall.png";
            case WALL_BREAKABLE -> "/images/classique/wall_breakable.png";
            case PLAYER1 -> "/images/classique/player.png";
            case PLAYER2 -> "/images/classique/player2.png";
            case ENEMY -> "/images/classique/enemy.png";
            default -> "/images/classique/grass.png";
        };

        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm());
            ImageView imageView = new ImageView(img);
            imageView.setFitWidth(CELL_SIZE);
            imageView.setFitHeight(CELL_SIZE);
            cell.setGraphic(imageView);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + imagePath);
        }
    }

    private void setupButtons() {
        saveButton.setOnAction(e -> saveMap());
        loadButton.setOnAction(e -> loadMap());
        clearButton.setOnAction(e -> clearMap());
    }

    private void saveMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder la carte");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers texte", "*.txt")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (int y = 0; y < DEFAULT_HEIGHT; y++) {
                    for (int x = 0; x < DEFAULT_WIDTH; x++) {
                        writer.write(map[y][x].getSymbol());
                    }
                    writer.newLine();
                }
                System.out.println("Carte sauvegardée avec succès!");
            } catch (IOException ex) {
                System.err.println("Erreur lors de la sauvegarde: " + ex.getMessage());
            }
        }
    }

    private void loadMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger une carte");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers texte", "*.txt")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                player1Position = null;
                player2Position = null;

                for (int y = 0; y < DEFAULT_HEIGHT; y++) {
                    String line = reader.readLine();
                    if (line != null) {
                        for (int x = 0; x < DEFAULT_WIDTH; x++) {
                            char symbol = line.charAt(x);
                            TileType type = TileType.fromSymbol(symbol);

                            // Conserver les murs du contour
                            if (y == 0 || y == DEFAULT_HEIGHT-1 || x == 0 || x == DEFAULT_WIDTH-1) {
                                type = TileType.WALL;
                            }

                            map[y][x] = type;

                            if (type == TileType.PLAYER1) {
                                player1Position = new Point(x, y);
                            } else if (type == TileType.PLAYER2) {
                                player2Position = new Point(x, y);
                            }

                            Button cell = (Button) editorGrid.getChildren().get(y * DEFAULT_WIDTH + x);
                            updateCellAppearance(cell, type);

                            // Désactiver les cellules du contour
                            if (y == 0 || y == DEFAULT_HEIGHT-1 || x == 0 || x == DEFAULT_WIDTH-1) {
                                cell.setDisable(true);
                            }
                        }
                    }
                }
                System.out.println("Carte chargée avec succès!");
            } catch (IOException ex) {
                System.err.println("Erreur lors du chargement: " + ex.getMessage());
            }
        }
    }

    private void clearMap() {
        player1Position = null;
        player2Position = null;
        initializeMap();  // Réinitialise la carte avec le contour de murs
        setupGrid();      // Réinitialise la grille avec les cellules désactivées
    }
}