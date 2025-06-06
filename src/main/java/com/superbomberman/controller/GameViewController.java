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
import static com.superbomberman.model.MapLoader.player2;

public class GameViewController {

    @FXML
    private GridPane gameGrid;

    private Tile[][] map;
    private int[] enemyCurrDirection;

    // Liste pour stocker les bombes actives
    private List<Bomb> activeBombs = new ArrayList<>();

    // Variables pour contrôler si chaque joueur peut placer une bombe
    private boolean canPlaceBombPlayer1 = true;
    private boolean canPlaceBombPlayer2 = true;

    // Images pour les joueurs
    Image playerImg = new Image(
            Objects.requireNonNull(getClass().getResource("/images/player.png")).toExternalForm()
    );
    private ImagePattern playerPattern = new ImagePattern(playerImg);

    Image player2Img = new Image(
            Objects.requireNonNull(getClass().getResource("/images/player2.png")).toExternalForm()
    );
    private ImagePattern player2Pattern = new ImagePattern(player2Img);

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

        // Initialiser le joueur 1
        if(map[player1.getY()][player1.getX()].getType() == TileType.FLOOR) {
            map[player1.getY()][player1.getX()] = new Tile(TileType.PLAYER1);
            addEntityToGrid(player1.getX(), player1.getY(), playerPattern);
        }

        // Initialiser le joueur 2
        if(player2 != null && map[player2.getY()][player2.getX()].getType() == TileType.FLOOR) {
            map[player2.getY()][player2.getX()] = new Tile(TileType.PLAYER2);
            addEntityToGrid(player2.getX(), player2.getY(), player2Pattern);
        }

        // Initialiser l'ennemi
        if(enemy != null && map[enemy.getY()][enemy.getX()].getType() == TileType.FLOOR) {
            map[enemy.getY()][enemy.getX()] = new Tile(TileType.ENEMY);
            addEntityToGrid(enemy.getX(), enemy.getY(), enemyPattern);
        }

        gameGrid.setOnKeyPressed((KeyEvent event) -> {
            System.out.println("Touche appuyée : " + event.getCode()); // debug

            // Variables pour le joueur 1
            int p1NewX = player1.getX();
            int p1NewY = player1.getY();

            // Variables pour le joueur 2
            int p2NewX = player2 != null ? player2.getX() : -1;
            int p2NewY = player2 != null ? player2.getY() : -1;

            // Variables pour l'ennemi
            int eNewX = enemy != null ? enemy.getX() : -1;
            int eNewY = enemy != null ? enemy.getY() : -1;

            switch (event.getCode()) {
                // Contrôles Joueur 1 (flèches directionnelles)
                case LEFT -> p1NewX -= 1;
                case RIGHT -> p1NewX += 1;
                case UP -> p1NewY -= 1;
                case DOWN -> p1NewY += 1;
                case SPACE -> {
                    // Bombe du joueur 1
                    if (canPlaceBombPlayer1) {
                        System.out.println("Joueur 1 pose une bombe !");
                        Bomb bomb = new Bomb(player1.getX(), player1.getY(), 10, 1);
                        placeBombVisual(bomb);
                        activeBombs.add(bomb);
                        canPlaceBombPlayer1 = false;

                        bomb.startCountdown(() -> {
                            System.out.println("BOOM ! (Joueur 1)");
                            handleExplosion(bomb);
                            activeBombs.remove(bomb);
                            canPlaceBombPlayer1 = true;
                        });
                    }
                }

                // Contrôles Joueur 2 (WASD)
                case Q -> {
                    if (player2 != null) p2NewX -= 1;
                }
                case D -> {
                    if (player2 != null) p2NewX += 1;
                }
                case Z -> {
                    if (player2 != null) p2NewY -= 1;
                }
                case S -> {
                    if (player2 != null) p2NewY += 1;
                }
                case A -> {
                    // Bombe du joueur 2
                    if (player2 != null && canPlaceBombPlayer2) {
                        System.out.println("Joueur 2 pose une bombe !");
                        Bomb bomb = new Bomb(player2.getX(), player2.getY(), 10, 1);
                        placeBombVisual(bomb);
                        activeBombs.add(bomb);
                        canPlaceBombPlayer2 = false;

                        bomb.startCountdown(() -> {
                            System.out.println("BOOM ! (Joueur 2)");
                            handleExplosion(bomb);
                            activeBombs.remove(bomb);
                            canPlaceBombPlayer2 = true;
                        });
                    }
                }
            }

            // Déplacer le joueur 1
            if (canMoveTo(p1NewX, p1NewY)) {
                player1.setPosition(p1NewX, p1NewY);
                updatePlayerPosition(player1, playerPattern);
            }

            // Déplacer le joueur 2
            if (player2 != null && canMoveTo(p2NewX, p2NewY)) {
                player2.setPosition(p2NewX, p2NewY);
                updatePlayerPosition(player2, player2Pattern);
            }

            // Déplacer l'ennemi
            if (enemy != null) {
                moveEnemy(enemy);
                updateEnemyPosition(enemy);
            }
        });

        gameGrid.setFocusTraversable(true);
        gameGrid.requestFocus();
    }

    private void drawMap(Tile[][] map) {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                Tile tile = map[row][col];

                StackPane cell = new StackPane();
                Rectangle background = new Rectangle(40, 40);

                switch (tile.getType()) {
                    case WALL -> background.setFill(wallPattern);
                    case FLOOR -> background.setFill(floorPattern);
                    case WALL_BREAKABLE -> background.setFill(wallBreakablePattern);
                    case PLAYER1, PLAYER2, ENEMY -> background.setFill(floorPattern);
                }

                cell.getChildren().add(background);
                gameGrid.add(cell, col, row);
            }
        }
    }

    private void addEntityToGrid(int x, int y, ImagePattern entityPattern) {
        StackPane cell = (StackPane) getNodeFromGridPane(gameGrid, x, y);
        if (cell != null) {
            Rectangle entity = new Rectangle(40, 40);
            entity.setFill(entityPattern);
            cell.getChildren().add(entity);
        }
    }

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

    private void placeBombVisual(Bomb bomb) {
        StackPane cell = (StackPane) getNodeFromGridPane(gameGrid, bomb.getX(), bomb.getY());
        if (cell != null) {
            if (cell.getChildren().size() > 1) {
                cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
            }

            Rectangle bombRect = new Rectangle(40, 40);
            bombRect.setFill(bombPattern);
            cell.getChildren().add(bombRect);
        }
    }

    private boolean canMoveTo(int x, int y) {
        if (x < 0 || y < 0 || y >= map.length || x >= map[0].length) {
            return false;
        }

        Tile tile = map[y][x];

        if (tile.getType() == TileType.WALL || tile.getType() == TileType.WALL_BREAKABLE) {
            return false;
        }

        // Vérifier collisions avec les autres joueurs
        if (player1.getX() == x && player1.getY() == y) {
            return false;
        }
        if (player2 != null && player2.getX() == x && player2.getY() == y) {
            return false;
        }
        if (enemy != null && enemy.getX() == x && enemy.getY() == y) {
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

    private void updatePlayerPosition(Player player, ImagePattern pattern) {
        int prevX = player.getPreviousX();
        int prevY = player.getPreviousY();

        StackPane prevCell = (StackPane) getNodeFromGridPane(gameGrid, prevX, prevY);
        if (prevCell != null) {
            boolean hasBombAtPrevPos = activeBombs.stream()
                    .anyMatch(bomb -> bomb.getX() == prevX && bomb.getY() == prevY);

            if (hasBombAtPrevPos) {
                if (prevCell.getChildren().size() > 2) {
                    prevCell.getChildren().removeIf(node -> prevCell.getChildren().indexOf(node) > 1);
                }
            } else {
                if (prevCell.getChildren().size() > 1) {
                    prevCell.getChildren().removeIf(node -> prevCell.getChildren().indexOf(node) > 0);
                }
            }
        }

        addEntityToGrid(player.getX(), player.getY(), pattern);
    }

    private void updateEnemyPosition(Enemy enemy) {
        int prevX = enemy.getPreviousX();
        int prevY = enemy.getPreviousY();

        StackPane prevCell = (StackPane) getNodeFromGridPane(gameGrid, prevX, prevY);
        if (prevCell != null) {
            boolean hasBombAtPrevPos = activeBombs.stream()
                    .anyMatch(bomb -> bomb.getX() == prevX && bomb.getY() == prevY);

            if (hasBombAtPrevPos) {
                if (prevCell.getChildren().size() > 2) {
                    prevCell.getChildren().removeIf(node -> prevCell.getChildren().indexOf(node) > 1);
                }
            } else {
                if (prevCell.getChildren().size() > 1) {
                    prevCell.getChildren().removeIf(node -> prevCell.getChildren().indexOf(node) > 0);
                }
            }
        }

        addEntityToGrid(enemy.getX(), enemy.getY(), enemyPattern);
    }

    private void handleExplosion(Bomb bomb) {
        int x = bomb.getX();
        int y = bomb.getY();
        int range = bomb.getRange();

        destroyTile(x, y);

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
            if (cell.getChildren().size() > 1) {
                cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
            }

            Rectangle explosionRect = new Rectangle(40, 40);
            explosionRect.setFill(explosionPattern);
            cell.getChildren().add(explosionRect);
        }

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
            if (cell.getChildren().size() > 1) {
                cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
            }

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