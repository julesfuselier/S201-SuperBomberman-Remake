package com.superbomberman.controller;

import com.superbomberman.model.*;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.superbomberman.model.MapLoader.enemy;
import static com.superbomberman.model.MapLoader.player1;

public class GameViewController {

    @FXML
    private GridPane gameGrid;

    private Tile[][] map;
    private int[] enemyCurrDirection;

    // Liste pour stocker les bombes actives
    private List<Bomb> activeBombs = new ArrayList<>();

    Image playerImg = new Image(
            Objects.requireNonNull(getClass().getResource("/images/player.png")).toExternalForm()
    );
    private ImagePattern playerPattern = new ImagePattern(playerImg);

    Image wallImg = new Image(
            Objects.requireNonNull(getClass().getResource("/images/wall.png")).toExternalForm()
    );
    private ImagePattern wallPattern = new ImagePattern(wallImg);

    Image wallBreakableImg = new Image(
            Objects.requireNonNull(getClass().getResource("/images/wall_breakable.png")).toExternalForm()
    );
    private ImagePattern wallBreakablePattern = new ImagePattern(wallBreakableImg);

    Image floorImg = new Image(
            Objects.requireNonNull(getClass().getResource("/images/grass.png")).toExternalForm()
    );
    private ImagePattern floorPattern = new ImagePattern(floorImg);

    Image explosionImg = new Image(
            Objects.requireNonNull(getClass().getResource("/images/explosion.png")).toExternalForm()
    );
    private ImagePattern explosionPattern = new ImagePattern(explosionImg);

    Image enemyImg = new Image(
            Objects.requireNonNull(getClass().getResource("/images/enemy.png")).toExternalForm()
    );
    private ImagePattern enemyPattern = new ImagePattern(enemyImg);

    Image bombImg = new Image(
            Objects.requireNonNull(getClass().getResource("/images/bomb.png")).toExternalForm()
    );
    private ImagePattern bombPattern = new ImagePattern(bombImg);


    public void initialize() {
        try {
            map = MapLoader.loadMap("src/main/resources/maps/level2.txt");
            drawMap(map);
            enemyCurrDirection = new int[]{1, 0}; // [x, y] direction
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(map[player1.getY()][player1.getX()].getType() == TileType.FLOOR) {
            map[player1.getY()][player1.getX()] = new Tile(TileType.PLAYER1);
            addEntityToGrid(player1.getX(), player1.getY(), playerPattern);
        }

        if(map[enemy.getY()][enemy.getX()].getType() == TileType.FLOOR) {
            map[enemy.getY()][enemy.getX()] = new Tile(TileType.ENEMY);
            addEntityToGrid(enemy.getX(), enemy.getY(), enemyPattern);
        }

        gameGrid.setOnKeyPressed((KeyEvent event) -> {
            System.out.println("Touche appuyée : " + event.getCode()); // debug
            int pNewX = player1.getX();
            int pNewY = player1.getY();

            int eNewX = enemy.getX();
            int eNewY = enemy.getY();
            System.out.println("New position : (" + pNewX + ", " + pNewY + ")");
            switch (event.getCode()) {
                case LEFT -> {
                    pNewX -= 1;
                    break;
                }
                case RIGHT -> {
                    pNewX += 1;
                    break;
                }
                case UP -> {
                    pNewY -= 1;
                    break;
                }
                case DOWN -> {
                    pNewY += 1;
                    break;
                }
                case SPACE -> {
                    System.out.println("Bombe posée !");
                    Bomb bomb = new Bomb(player1.getX(), player1.getY(), 10, 1);

                    // Afficher visuellement la bombe avec l'herbe en arrière-plan
                    placeBombVisual(bomb);

                    // Ajouter à la liste des bombes actives
                    activeBombs.add(bomb);

                    bomb.startCountdown(() -> {
                        System.out.println("BOOM !");
                        handleExplosion(bomb);

                        // Retirer la bombe de la liste après explosion
                        activeBombs.remove(bomb);
                    });
                }

                default -> {
                    break;
                }
            }

            if (canMoveTo(pNewX, pNewY)) {
                player1.setPosition(pNewX, pNewY);
                updatePlayerPosition(player1);
            }

            System.out.println("New enemy position : (" + eNewX + ", " + eNewY + ")");
            moveEnemy(enemy);
            updateEnemyPosition(enemy);
        });

        gameGrid.setFocusTraversable(true);
        gameGrid.requestFocus();
    }

    // ***************************************************
    // drawmap() -> METHODE : DESSINER LA MAP
    // ***************************************************

    private void drawMap(Tile[][] map) {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                Tile tile = map[row][col];

                // Créer un StackPane pour permettre la superposition
                StackPane cell = new StackPane();

                // Fond de base (herbe pour les sols, texture appropriée pour les autres)
                Rectangle background = new Rectangle(40, 40);

                switch (tile.getType()) {
                    case WALL -> background.setFill(wallPattern);
                    case FLOOR -> background.setFill(floorPattern);
                    case WALL_BREAKABLE -> background.setFill(wallBreakablePattern);
                    case PLAYER1, ENEMY -> background.setFill(floorPattern); // Herbe en arrière-plan
                }

                cell.getChildren().add(background);
                gameGrid.add(cell, col, row);
            }
        }
    }

    // Nouvelle méthode pour ajouter une entité sur la grille avec l'herbe en arrière-plan
    private void addEntityToGrid(int x, int y, ImagePattern entityPattern) {
        StackPane cell = (StackPane) getNodeFromGridPane(gameGrid, x, y);
        if (cell != null) {
            // Ajouter l'entité par-dessus l'arrière-plan
            Rectangle entity = new Rectangle(40, 40);
            entity.setFill(entityPattern);
            cell.getChildren().add(entity);
        }
    }

    // Méthode pour récupérer un nœud spécifique de la GridPane
    private javafx.scene.Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            Integer columnIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);
            if (columnIndex != null && rowIndex != null && columnIndex == col && rowIndex == row) {
                return node;
            }
        }
        return null;
    }

    // Méthode pour afficher la bombe visuellement avec l'herbe en arrière-plan
    private void placeBombVisual(Bomb bomb) {
        StackPane cell = (StackPane) getNodeFromGridPane(gameGrid, bomb.getX(), bomb.getY());
        if (cell != null) {
            // Garder seulement l'arrière-plan (herbe)
            if (cell.getChildren().size() > 1) {
                // Supprimer les entités (joueur, ennemi) mais garder l'arrière-plan
                cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
            }

            // Ajouter la texture de la bombe par-dessus l'herbe
            Rectangle bombRect = new Rectangle(40, 40);
            bombRect.setFill(bombPattern);
            cell.getChildren().add(bombRect);
        }
    }

    // Méthode canMoveTo mise à jour
    private boolean canMoveTo(int x, int y) {
        // Vérifier les limites de la grille
        if (x < 0 || y < 0 || y >= map.length || x >= map[0].length) {
            return false;
        }

        Tile tile = map[y][x];

        // Empêcher de marcher sur les murs
        if (tile.getType() == TileType.WALL || tile.getType() == TileType.WALL_BREAKABLE) {
            return false;
        }

        // Empêcher de marcher sur les bombes actives
        for (Bomb bomb : activeBombs) {
            if (bomb.getX() == x && bomb.getY() == y) {
                return false;
            }
        }

        return true;
    }

    private void updatePlayerPosition(Player player) {
        // Remettre la case où était le joueur avant à son état d'origine
        int prevX = player.getPreviousX();
        int prevY = player.getPreviousY();

        StackPane prevCell = (StackPane) getNodeFromGridPane(gameGrid, prevX, prevY);
        if (prevCell != null) {
            // Vérifier s'il y a une bombe à l'ancienne position
            boolean hasBombAtPrevPos = activeBombs.stream()
                    .anyMatch(bomb -> bomb.getX() == prevX && bomb.getY() == prevY);

            if (hasBombAtPrevPos) {
                // S'il y a une bombe, garder seulement l'herbe et la bombe
                if (prevCell.getChildren().size() > 2) {
                    // Supprimer le joueur mais garder l'herbe (index 0) et la bombe (index 1)
                    prevCell.getChildren().removeIf(node -> prevCell.getChildren().indexOf(node) > 1);
                }
            } else {
                // Sinon, garder seulement l'herbe
                if (prevCell.getChildren().size() > 1) {
                    prevCell.getChildren().removeIf(node -> prevCell.getChildren().indexOf(node) > 0);
                }
            }
        }

        // Ajouter le joueur à la nouvelle position
        addEntityToGrid(player.getX(), player.getY(), playerPattern);
    }

    private void updateEnemyPosition(Enemy enemy) {
        // Remettre la case où était l'ennemi avant à son état d'origine
        int prevX = enemy.getPreviousX();
        int prevY = enemy.getPreviousY();

        StackPane prevCell = (StackPane) getNodeFromGridPane(gameGrid, prevX, prevY);
        if (prevCell != null) {
            // Vérifier s'il y a une bombe à l'ancienne position
            boolean hasBombAtPrevPos = activeBombs.stream()
                    .anyMatch(bomb -> bomb.getX() == prevX && bomb.getY() == prevY);

            if (hasBombAtPrevPos) {
                // S'il y a une bombe, garder seulement l'herbe et la bombe
                if (prevCell.getChildren().size() > 2) {
                    // Supprimer l'ennemi mais garder l'herbe (index 0) et la bombe (index 1)
                    prevCell.getChildren().removeIf(node -> prevCell.getChildren().indexOf(node) > 1);
                }
            } else {
                // Sinon, garder seulement l'herbe
                if (prevCell.getChildren().size() > 1) {
                    prevCell.getChildren().removeIf(node -> prevCell.getChildren().indexOf(node) > 0);
                }
            }
        }

        // Ajouter l'ennemi à la nouvelle position
        addEntityToGrid(enemy.getX(), enemy.getY(), enemyPattern);
    }

    private void handleExplosion(Bomb bomb) {
        int x = bomb.getX();
        int y = bomb.getY();
        int range = bomb.getRange();

        // Centre de l'explosion
        destroyTile(x, y);

        // Directions : droite, gauche, bas, haut
        int[][] directions = { {1,0}, {-1,0}, {0,1}, {0,-1} };

        for (int[] direction : directions) {
            for (int rangeStep = 1; rangeStep <= range; rangeStep++) {
                int nx = x + direction[0] * rangeStep;
                int ny = y + direction[1] * rangeStep;

                if (!isInBounds(nx, ny)) break;

                boolean continueExplosion = destroyTile(nx, ny);
                if (!continueExplosion) break;
            }
        }
    }

    private boolean destroyTile(int x, int y) {
        Tile tile = map[y][x];

        if (tile.getType() == TileType.WALL) {
            return false;
        }

        StackPane cell = (StackPane) getNodeFromGridPane(gameGrid, x, y);
        if (cell != null) {
            // Garder seulement l'arrière-plan et ajouter l'explosion
            if (cell.getChildren().size() > 1) {
                cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
            }

            Rectangle explosionRect = new Rectangle(40, 40);
            explosionRect.setFill(explosionPattern);
            cell.getChildren().add(explosionRect);
        }

        // 2s avant de restaurer la texture appropriée
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
        delay.setOnFinished(event -> {
            if (tile.getType() == TileType.WALL_BREAKABLE) {
                map[y][x] = new Tile(TileType.FLOOR);
            }
            redrawTile(x, y);
        });
        delay.play();

        return switch (tile.getType()) {
            case FLOOR -> true;
            case WALL -> false;
            case WALL_BREAKABLE -> false;
            default -> true;
        };
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && y < map.length && x < map[0].length;
    }

    private void redrawTile(int x, int y) {
        StackPane cell = (StackPane) getNodeFromGridPane(gameGrid, x, y);
        if (cell != null) {
            // Supprimer tout sauf l'arrière-plan
            if (cell.getChildren().size() > 1) {
                cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
            }

            // Mettre à jour l'arrière-plan si nécessaire
            Rectangle background = (Rectangle) cell.getChildren().get(0);
            switch (map[y][x].getType()) {
                case FLOOR -> background.setFill(floorPattern);
                case WALL -> background.setFill(wallPattern);
                case WALL_BREAKABLE -> background.setFill(wallBreakablePattern);
            }
        }
    }

    public void moveEnemy(Enemy enemy) {
        int currentX = enemy.getX();
        int currentY = enemy.getY();

        int newX = currentX + enemyCurrDirection[0];
        int newY = currentY + enemyCurrDirection[1];

        if (canMoveTo(newX, newY)) {
            enemy.setPosition(newX, newY);
            updateEnemyPosition(enemy);
        } else {
            int[][] directions = { {1,0}, {-1,0}, {0,1}, {0,-1} };
            int[][] possibleDirections = new int[3][2];
            int idx = 0;
            for (int[] dir : directions) {
                if (!(dir[0] == enemyCurrDirection[0] && dir[1] == enemyCurrDirection[1])) {
                    possibleDirections[idx++] = dir;
                }
            }
            int randomIndex = (int) (Math.random() * possibleDirections.length);
            enemyCurrDirection = possibleDirections[randomIndex];

            newX = currentX + enemyCurrDirection[0];
            newY = currentY + enemyCurrDirection[1];

            if (canMoveTo(newX, newY)) {
                enemy.setPosition(newX, newY);
                updateEnemyPosition(enemy);
            }
        }
    }
}