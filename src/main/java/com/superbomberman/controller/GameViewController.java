package com.superbomberman.controller;

import com.superbomberman.model.*;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.Objects;

import static com.superbomberman.model.MapLoader.enemy;
import static com.superbomberman.model.MapLoader.player1;

public class GameViewController {

    @FXML
    private GridPane gameGrid;

    private Tile[][] map;
    private int[] enemyCurrDirection;

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
            map = MapLoader.loadMap("src/main/resources/maps/level1.txt");
            drawMap(map);
            enemyCurrDirection = new int[]{1, 0}; // [x, y] direction
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(map[player1.getY()][player1.getX()].getType() == TileType.FLOOR) {
            map[player1.getY()][player1.getX()] = new Tile(TileType.PLAYER1);
            Rectangle playerRect = new Rectangle(40, 40);
            playerRect.setFill(playerPattern);
            gameGrid.add(playerRect, player1.getX(), player1.getY());
        }

        if(map[enemy.getY()][enemy.getX()].getType() == TileType.FLOOR) {
            map[enemy.getY()][enemy.getX()] = new Tile(TileType.ENEMY);
            Rectangle enemyRect = new Rectangle(40, 40);
            enemyRect.setFill(enemyPattern);
            gameGrid.add(enemyRect, enemy.getX(), enemy.getY());
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
                    bomb.startCountdown(() -> {
                        System.out.println("BOOM !");
                        handleExplosion(bomb);
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
                Tile tile = map[row][col]; // recupération des type de Tile afin de construire la map
                Rectangle rect = new Rectangle(40, 40); // mise en place de rectangle -> simplifier l'affichage

                switch (tile.getType()) { // pour chaque TileType, on lui soumet une couleur
                    case WALL -> rect.setFill(wallPattern);
                    case FLOOR -> rect.setFill(floorPattern);
                    case WALL_BREAKABLE -> rect.setFill(wallBreakablePattern);
                    case PLAYER1 -> rect.setFill(playerPattern);
                    case ENEMY -> rect.setFill(enemyPattern);
                }

                gameGrid.add(rect, col, row);
            }
        }
    }

    private boolean canMoveTo(int x, int y) {
        //  coordonnées ==  limites de la grille ?
        if (x < 0 || y < 0 || y >= map.length || x >= map[0].length) {
            return false;
        }

        Tile tile = map[y][x];
        return tile.getType() != TileType.WALL && tile.getType() != TileType.WALL_BREAKABLE;
    }

    // TODO : ENLEVER LES COMMENTAIRES + EN REFAIRE
    private void updatePlayerPosition(Player player) {
        // 1. Remettre la case où était le joueur avant à son tile d'origine (sol, mur cassable...)
        int prevX = player.getPreviousX();
        int prevY = player.getPreviousY();


        // Supprimer le rectangle (joueur) à l'ancienne position
        gameGrid.getChildren().removeIf(node ->
                GridPane.getColumnIndex(node) == prevX
                        && GridPane.getRowIndex(node) == prevY
        );

        // Remettre la texture du sol ou autre tile à l'ancienne position
        Tile oldTile = map[prevY][prevX];
        Rectangle oldRect = new Rectangle(40, 40);
        switch (oldTile.getType()) {
            case FLOOR, WALL_BREAKABLE, PLAYER1, ENEMY -> oldRect.setFill(floorPattern);
        }
        gameGrid.add(oldRect, prevX, prevY);

        // 2. Ajouter le joueur à la nouvelle position
        Rectangle playerRect = new Rectangle(40, 40);
        playerRect.setFill(playerPattern);
        gameGrid.add(playerRect, player.getX(), player.getY());
    }

    private void updateEnemyPosition(Enemy enemy) {
        // 1. Remettre la case où était le joueur avant à son tile d'origine (sol, mur cassable...)
        int prevX = enemy.getPreviousX();
        int prevY = enemy.getPreviousY();

        // Supprimer le rectangle (joueur) à l'ancienne position
        gameGrid.getChildren().removeIf(node ->
                GridPane.getColumnIndex(node) == prevX
                        && GridPane.getRowIndex(node) == prevY
        );

        // Remettre la texture du sol ou autre tile à l'ancienne position
        Tile oldTile = map[prevY][prevX];
        Rectangle oldRect = new Rectangle(40, 40);
        switch (oldTile.getType()) {
            case FLOOR, WALL_BREAKABLE, ENEMY, PLAYER1 -> oldRect.setFill(floorPattern);
        }
        gameGrid.add(oldRect, prevX, prevY);

        // 2. Ajouter le joueur à la nouvelle position
        Rectangle enemyRect = new Rectangle(40, 40);
        enemyRect.setFill(enemyPattern);
        gameGrid.add(enemyRect, enemy.getX(), enemy.getY());
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
            for (int rangeStep = 1; rangeStep <= range; rangeStep++) { // TODO : EXPLIQUER AUX AUTRES
                int nx = x + direction[0] * rangeStep; // direction horizontale
                int ny = y + direction[1] * rangeStep; // direction verticale

                if (!isInBounds(nx, ny)) break; // si c'est en dehors de la map

                boolean continueExplosion = destroyTile(nx, ny);
                if (!continueExplosion) break; // stop si mur ou bloc destructible
            }
        }
    }

    private boolean destroyTile(int x, int y) {
        Tile tile = map[y][x];

        if (tile.getType() == TileType.WALL) {
            return false; // Ne rien afficher
        }

        // appliquée directement
        Rectangle explosionRect = new Rectangle(40, 40);
        explosionRect.setFill(explosionPattern);
        gameGrid.getChildren().removeIf(node ->
                GridPane.getColumnIndex(node) == x && GridPane.getRowIndex(node) == y
        );
        gameGrid.add(explosionRect, x, y);

        // 2s avant la bonne texture
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
        delay.setOnFinished(event -> {
            if (tile.getType() == TileType.WALL_BREAKABLE) {
                map[y][x] = new Tile(TileType.FLOOR);
            }
            redrawTile(x, y); // restaure le visuel du sol ou du mur
        });
        delay.play();

        return switch (tile.getType()) {
            case FLOOR -> true;
            case WALL -> false;
            case WALL_BREAKABLE -> false;
            default -> true;
        };
    }



    private boolean isInBounds(int x, int y) { // -> permet de verifier si les coordonnées sont dans la map
        return x >= 0 && y >= 0 && y < map.length && x < map[0].length;
    }

    private void redrawTile(int x, int y) {
        // Supprime le node à cette position
        gameGrid.getChildren().removeIf(node ->
                GridPane.getColumnIndex(node) == x &&
                        GridPane.getRowIndex(node) == y
        );

        // Redessine le bon type de case
        Rectangle rect = new Rectangle(40, 40);
        switch (map[y][x].getType()) {
            case FLOOR -> rect.setFill(floorPattern);
            case WALL -> rect.setFill(wallPattern);
            case WALL_BREAKABLE -> rect.setFill(wallBreakablePattern);
        }

        gameGrid.add(rect, x, y);
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
