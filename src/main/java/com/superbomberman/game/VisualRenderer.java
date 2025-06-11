package com.superbomberman.game;

import com.superbomberman.model.*;
import com.superbomberman.model.powerup.PowerUp;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;

/**
 * Gestionnaire du rendu visuel du jeu
 *
 * @author Jules Fuselier
 * @version 1.2 - Fix chemins d'images
 * @since 2025-06-08
 */
public class VisualRenderer {
    private GridPane gameGrid;
    private Tile[][] map;

    // Patterns pour les différents éléments
    private ImagePattern floorPattern;
    private ImagePattern wallPattern;
    private ImagePattern wallBreakablePattern;
    private ImagePattern playerPattern;
    private ImagePattern player2Pattern;
    private ImagePattern enemyPattern;
    private ImagePattern bombPattern;
    private ImagePattern explosionPattern;
    private ImagePattern powerUpPattern;

    public VisualRenderer(GridPane gameGrid, Tile[][] map) {
        this.gameGrid = gameGrid;
        this.map = map;
        loadPatterns();
    }

    /**
     * ✅ FIX: Charge tous les patterns d'images avec les bons chemins
     */
    private void loadPatterns() {
        try {
            // Utiliser les chemins existants dans /images/
            floorPattern = new ImagePattern(new Image(Objects.requireNonNull(getClass().getResource("/images/grass.png")).toExternalForm()));
            wallPattern = new ImagePattern(new Image(Objects.requireNonNull(getClass().getResource("/images/wall.png")).toExternalForm()));
            wallBreakablePattern = new ImagePattern(new Image(Objects.requireNonNull(getClass().getResource("/images/wall_breakable.png")).toExternalForm()));
            playerPattern = new ImagePattern(new Image(Objects.requireNonNull(getClass().getResource("/images/player.png")).toExternalForm()));
            player2Pattern = new ImagePattern(new Image(Objects.requireNonNull(getClass().getResource("/images/player2.png")).toExternalForm()));
            enemyPattern = new ImagePattern(new Image(Objects.requireNonNull(getClass().getResource("/images/enemy.png")).toExternalForm()));
            bombPattern = new ImagePattern(new Image(Objects.requireNonNull(getClass().getResource("/images/bomb.png")).toExternalForm()));
            explosionPattern = new ImagePattern(new Image(Objects.requireNonNull(getClass().getResource("/images/explosion.png")).toExternalForm()));
            powerUpPattern = new ImagePattern(new Image(Objects.requireNonNull(getClass().getResource("/images/powerup.png")).toExternalForm()));

            System.out.println("✅ Patterns chargés avec succès depuis /images/!");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des patterns: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure les contraintes de la grille
     */
    public void setupGridConstraints() {
        gameGrid.getChildren().clear();
        gameGrid.getColumnConstraints().clear();
        gameGrid.getRowConstraints().clear();

        int rows = map.length;
        int cols = map[0].length;

        // Ajouter les contraintes de colonnes
        for (int i = 0; i < cols; i++) {
            ColumnConstraints colConstraint = new ColumnConstraints();
            colConstraint.setPrefWidth(50);
            colConstraint.setMinWidth(50);
            colConstraint.setMaxWidth(50);
            gameGrid.getColumnConstraints().add(colConstraint);
        }

        // Ajouter les contraintes de lignes
        for (int i = 0; i < rows; i++) {
            RowConstraints rowConstraint = new RowConstraints();
            rowConstraint.setPrefHeight(50);
            rowConstraint.setMinHeight(50);
            rowConstraint.setMaxHeight(50);
            gameGrid.getRowConstraints().add(rowConstraint);
        }

        // Configurer le GridPane
        gameGrid.setHgap(0);
        gameGrid.setVgap(0);
        gameGrid.setStyle("-fx-background-color: black; -fx-grid-lines-visible: false;");

        System.out.println("✅ Contraintes de grille configurées: " + cols + "x" + rows);
    }

    /**
     * Dessine la carte initiale
     */
    public void drawMap() {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                createCell(col, row, map[row][col].getType());
            }
        }
        System.out.println("✅ Carte dessinée avec succès!");
    }

    /**
     * Crée une cellule de la grille
     */
    private void createCell(int x, int y, TileType tileType) {
        StackPane cell = new StackPane();
        cell.setAlignment(Pos.CENTER);

        Rectangle background = new Rectangle(50, 50);

        switch (tileType) {
            case FLOOR -> background.setFill(floorPattern);
            case WALL -> background.setFill(wallPattern);
            case WALL_BREAKABLE -> background.setFill(wallBreakablePattern);
            case PLAYER1, PLAYER2, ENEMY -> background.setFill(floorPattern); // Fond pour les entités
            default -> background.setFill(floorPattern);
        }

        cell.getChildren().add(background);
        gameGrid.add(cell, x, y);
    }

    /**
     * Ajoute une entité à la grille
     */
    public void addEntityToGrid(int x, int y, ImagePattern pattern) {
        StackPane cell = (StackPane) getNodeFromGridPane(x, y);
        if (cell != null) {
            // Supprimer les entités existantes (garder seulement le fond)
            if (cell.getChildren().size() > 1) {
                cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
            }

            Rectangle entityRect = new Rectangle(50, 50);
            entityRect.setFill(pattern);
            cell.getChildren().add(entityRect);
        }
    }

    /**
     * Met à jour la position d'un joueur
     */
    public void updatePlayerPosition(Player player, ImagePattern pattern, List<Bomb> activeBombs) {
        int prevX = player.getPreviousX();
        int prevY = player.getPreviousY();

        // Nettoyer la position précédente
        StackPane prevCell = (StackPane) getNodeFromGridPane(prevX, prevY);
        if (prevCell != null) {
            boolean hasBombAtPrevPos = activeBombs.stream()
                    .anyMatch(bomb -> bomb.getX() == prevX && bomb.getY() == prevY);

            if (hasBombAtPrevPos) {
                // Garder le fond + la bombe, supprimer le joueur
                if (prevCell.getChildren().size() > 2) {
                    prevCell.getChildren().removeIf(node -> prevCell.getChildren().indexOf(node) > 1);
                }
            } else {
                // Garder seulement le fond
                if (prevCell.getChildren().size() > 1) {
                    prevCell.getChildren().removeIf(node -> prevCell.getChildren().indexOf(node) > 0);
                }
            }
        }

        // Ajouter à la nouvelle position
        addEntityToGrid(player.getX(), player.getY(), pattern);
    }

    /**
     * Met à jour la position de l'ennemi
     */
    /**
     * Met à jour la position de l'ennemi avec nettoyage amélioré
     */
    public void updateEnemyPosition(Enemy enemy, List<Bomb> activeBombs) {
        int prevX = enemy.getPreviousX();
        int prevY = enemy.getPreviousY();

        // 🔥 FIX : Nettoyer la position précédente de manière plus agressive
        StackPane prevCell = (StackPane) getNodeFromGridPane(prevX, prevY);
        if (prevCell != null) {
            boolean hasBombAtPrevPos = activeBombs.stream()
                    .anyMatch(bomb -> bomb.getX() == prevX && bomb.getY() == prevY);

            if (hasBombAtPrevPos) {
                // Garder fond + bombe, supprimer tout le reste
                while (prevCell.getChildren().size() > 2) {
                    prevCell.getChildren().remove(prevCell.getChildren().size() - 1);
                }
            } else {
                // Garder seulement le fond, supprimer TOUT le reste
                while (prevCell.getChildren().size() > 1) {
                    prevCell.getChildren().remove(prevCell.getChildren().size() - 1);
                }
            }

            System.out.println("🧹 Position (" + prevX + ", " + prevY + ") nettoyée, reste " + prevCell.getChildren().size() + " éléments");
        }

        // Ajouter à la nouvelle position
        addEntityToGrid(enemy.getX(), enemy.getY(), enemyPattern);
        System.out.println("👾 Ennemi déplacé vers (" + enemy.getX() + ", " + enemy.getY() + ")");
    }

    /**
     * Affiche une bombe
     */
    public void placeBombVisual(Bomb bomb) {
        StackPane cell = (StackPane) getNodeFromGridPane(bomb.getX(), bomb.getY());
        if (cell != null) {
            // Supprimer les entités existantes sauf le fond
            if (cell.getChildren().size() > 1) {
                cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
            }

            Rectangle bombRect = new Rectangle(50, 50);
            bombRect.setFill(bombPattern);
            cell.getChildren().add(bombRect);
        }
    }

    /**
     * Supprime une bombe visuellement
     */
    public void removeBombVisual(Bomb bomb) {
        removeBombVisual(bomb.getX(), bomb.getY());
    }

    /**
     * Supprime une bombe à une position donnée
     */
    public void removeBombVisual(int x, int y) {
        StackPane cell = (StackPane) getNodeFromGridPane(x, y);
        if (cell != null && cell.getChildren().size() > 1) {
            // Supprimer tout sauf le fond
            cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
        }
    }

    /**
     * Met à jour la position visuelle d'une bombe
     */
    public void updateBombVisual(Bomb bomb) {
        removeBombVisual(bomb.getPreviousX(), bomb.getPreviousY());
        placeBombVisual(bomb);
    }

    /**
     *  Affiche une explosion qui se supprime automatiquement
     */
    public void showExplosion(int x, int y) {
        StackPane cell = (StackPane) getNodeFromGridPane(x, y);
        if (cell != null) {
            // Supprimer les entités existantes sauf le fond
            if (cell.getChildren().size() > 1) {
                cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
            }

            // Ajouter l'explosion avec un ID unique
            Rectangle explosionRect = new Rectangle(50, 50);
            explosionRect.setFill(explosionPattern);
            explosionRect.setId("explosion"); // ✅ MARQUER L'EXPLOSION
            cell.getChildren().add(explosionRect);

            PauseTransition explosionDuration = new PauseTransition(Duration.seconds(0.5));
            explosionDuration.setOnFinished(event -> {
                // ✅ SUPPRIMER SEULEMENT L'EXPLOSION PAR SON ID
                cell.getChildren().removeIf(node -> "explosion".equals(node.getId()));

                // Redessiner le fond
                Rectangle background = (Rectangle) cell.getChildren().get(0);
                switch (map[y][x].getType()) {
                    case FLOOR -> background.setFill(floorPattern);
                    case WALL -> background.setFill(wallPattern);
                    case WALL_BREAKABLE -> background.setFill(wallBreakablePattern);
                }

                System.out.println("💥 Explosion supprimée à (" + x + ", " + y + ") après 0.5s");
            });
            explosionDuration.play();

            System.out.println("💥 Explosion affichée à (" + x + ", " + y + ") - suppression dans 0.5s");
        }
    }

    /**
     * Affiche un power-up
     */
    public void placePowerUpVisual(PowerUp powerUp) {
        StackPane cell = (StackPane) getNodeFromGridPane(powerUp.getX(), powerUp.getY());
        if (cell != null) {
            Rectangle powerUpRect = new Rectangle(50, 50);
            powerUpRect.setFill(powerUpPattern);
            cell.getChildren().add(powerUpRect);
        }
    }

    /**
     * Supprime un power-up visuellement
     */
    public void removePowerUpVisual(PowerUp powerUp) {
        StackPane cell = (StackPane) getNodeFromGridPane(powerUp.getX(), powerUp.getY());
        if (cell != null) {
            // ✅ SUPPRIMER SEULEMENT LE POWER-UP (pas tout!)
            cell.getChildren().removeIf(node -> {
                if (node instanceof Rectangle) {
                    Rectangle rect = (Rectangle) node;
                    // Supprimer seulement si c'est le power-up
                    return rect.getFill() == powerUpPattern;
                }
                return false;
            });
        }
    }

    /**
     * Redessine une tuile à sa position
     */
    public void redrawTile(int x, int y, List<PowerUp> activePowerUps) {
        StackPane cell = (StackPane) getNodeFromGridPane(x, y);
        if (cell != null) {
            boolean hasPowerUp = activePowerUps.stream()
                    .anyMatch(powerUp -> powerUp.getX() == x && powerUp.getY() == y);

            // Si pas de power-up, nettoyer toutes les entités
            if (!hasPowerUp && cell.getChildren().size() > 1) {
                cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
            }

            // Redessiner le fond
            Rectangle background = (Rectangle) cell.getChildren().get(0);
            switch (map[y][x].getType()) {
                case FLOOR -> background.setFill(floorPattern);
                case WALL -> background.setFill(wallPattern);
                case WALL_BREAKABLE -> background.setFill(wallBreakablePattern);
            }
        }
    }

    /**
     * Obtient un noeud de la grille à une position donnée
     */
    private Node getNodeFromGridPane(int col, int row) {
        for (Node node : gameGrid.getChildren()) {
            Integer columnIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);
            if (columnIndex != null && rowIndex != null && columnIndex == col && rowIndex == row) {
                return node;
            }
        }
        return null;
    }


    /**
     * 🧹 NOUVEAU : Nettoie complètement la grille visuelle
     */
    public void clearAllVisuals() {
        for (Node node : gameGrid.getChildren()) {
            if (node instanceof StackPane cell) {
                // Garder seulement le background (premier enfant)
                if (cell.getChildren().size() > 1) {
                    cell.getChildren().removeIf(child -> cell.getChildren().indexOf(child) > 0);
                }
            }
        }
        System.out.println("🧹 Grille visuelle nettoyée complètement");
    }

    /**
     * 🔄 NOUVEAU : Recharge tous les patterns et redessine la carte
     */
    public void refreshDisplay() {
        loadPatterns();
        setupGridConstraints();
        drawMap();
        System.out.println("🔄 Affichage rafraîchi");
    }

    // Getters pour les patterns
    public ImagePattern getFloorPattern() { return floorPattern; }
    public ImagePattern getWallPattern() { return wallPattern; }
    public ImagePattern getWallBreakablePattern() { return wallBreakablePattern; }
    public ImagePattern getPlayerPattern() { return playerPattern; }
    public ImagePattern getPlayer2Pattern() { return player2Pattern; }
    public ImagePattern getEnemyPattern() { return enemyPattern; }
    public ImagePattern getBombPattern() { return bombPattern; }
    public ImagePattern getExplosionPattern() { return explosionPattern; }
    public ImagePattern getPowerUpPattern() { return powerUpPattern; }
}