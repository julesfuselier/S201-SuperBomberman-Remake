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

import com.superbomberman.model.powerup.*;


import static com.superbomberman.model.MapLoader.enemy;
import static com.superbomberman.model.MapLoader.player1;
import static javafx.scene.CacheHint.SPEED;

/**
 * Contrôleur principal de la vue de jeu pour Super Bomberman.
 * <p>
 * Cette classe gère l'initialisation de la grille de jeu, le rendu de la carte,
 * la gestion des entités (joueur, ennemi, bombes), le déplacement des personnages,
 * la pose et la détonation des bombes, ainsi que les interactions clavier.
 * Elle orchestre l'affichage graphique et la logique de jeu côté client.
 * </p>
 *
 * <h2>Fonctionnalités principales :</h2>
 * <ul>
 *   <li>Initialise la grille de jeu et place les entités sur celle-ci.</li>
 *   <li>Gère les déplacements du joueur et de l'ennemi en fonction des entrées clavier ou de l'IA.</li>
 *   <li>Permet la pose, le rendu et l'explosion des bombes avec gestion des collisions.</li>
 *   <li>Met à jour dynamiquement l'interface graphique (GridPane) selon l'état du jeu.</li>
 * </ul>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-05
 */
public class GameViewController {

    /** Grille de jeu affichée à l'écran. */
    @FXML
    private GridPane gameGrid;

    /** Carte du niveau courant, sous forme de matrice de tuiles. */
    private Tile[][] map;

    /** Direction actuelle de déplacement de l'ennemi ([x, y]). */
    private int[] enemyCurrDirection;

    /** Liste des bombes actuellement actives sur la grille. */
    private List<Bomb> activeBombs = new ArrayList<>();

    /** Indique si le joueur peut actuellement poser une nouvelle bombe. */
    private boolean canPlaceBomb = true;

    private List<PowerUp> activePowerUps = new ArrayList<>();
    private Image powerUpImg = new Image(Objects.requireNonNull(getClass().getResource("/images/powerup.png")).toExternalForm());
    private ImagePattern powerUpPattern = new ImagePattern(powerUpImg);

    // Images et patterns pour le rendu des entités et tuiles
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

    /**
     * Initialise la scène de jeu : charge la carte, place les entités et configure les contrôles clavier.
     * Appelée automatiquement par JavaFX à l'affichage de la vue.
     */
    public void initialize() {
        try {
            map = MapLoader.loadMap("src/main/resources/maps/level3.txt");
            drawMap(map);
            enemyCurrDirection = new int[]{1, 0}; // [x, y] direction
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (map[player1.getY()][player1.getX()].getType() == TileType.FLOOR) {
            map[player1.getY()][player1.getX()] = new Tile(TileType.PLAYER1);
            addEntityToGrid(player1.getX(), player1.getY(), playerPattern);
        }

        if (map[enemy.getY()][enemy.getX()].getType() == TileType.FLOOR) {
            map[enemy.getY()][enemy.getX()] = new Tile(TileType.ENEMY);
            addEntityToGrid(enemy.getX(), enemy.getY(), enemyPattern);
        }

        gameGrid.setOnKeyPressed((KeyEvent event) -> {
            // Gestion des touches de déplacement et d'action
            int pNewX = player1.getX();
            int pNewY = player1.getY();

            int eNewX = enemy.getX();
            int eNewY = enemy.getY();

            switch (event.getCode()) {
                case LEFT -> pNewX -= 1;
                case RIGHT -> pNewX += 1;
                case UP -> pNewY -= 1;
                case DOWN -> pNewY += 1;
                case SPACE -> {
                    // Pose d'une bombe
                    if (canPlaceBomb) {
                        Bomb bomb = new Bomb(player1.getX(), player1.getY(), 10, 1);
                        placeBombVisual(bomb);
                        activeBombs.add(bomb);
                        canPlaceBomb = false;

                        bomb.startCountdown(() -> {
                            handleExplosion(bomb);
                            activeBombs.remove(bomb);
                            canPlaceBomb = true;
                        });
                    }
                }
                default -> { /* Pas d'action */ }
            }

            if (canMoveTo(pNewX, pNewY)) {
                player1.setPosition(pNewX, pNewY);
                updatePlayerPosition(player1);
            }

            moveEnemy(enemy);
            updateEnemyPosition(enemy);
        });

        gameGrid.setFocusTraversable(true);
        gameGrid.requestFocus();
    }

    /**
     * Dessine la carte du niveau sur la grille graphique.
     *
     * @param map Tableau de tuiles à afficher.
     */
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
                    case PLAYER1, ENEMY -> background.setFill(floorPattern);
                }

                cell.getChildren().add(background);
                gameGrid.add(cell, col, row);
            }
        }
    }

    /**
     * Ajoute une entité graphique (joueur, ennemi, bombe) sur la grille à la position spécifiée.
     *
     * @param x             Coordonnée X dans la grille.
     * @param y             Coordonnée Y dans la grille.
     * @param entityPattern Pattern graphique pour l'entité.
     */
    private void addEntityToGrid(int x, int y, ImagePattern entityPattern) {
        StackPane cell = (StackPane) getNodeFromGridPane(gameGrid, x, y);
        if (cell != null) {
            Rectangle entity = new Rectangle(40, 40);
            entity.setFill(entityPattern);
            cell.getChildren().add(entity);
        }
    }

    /**
     * Récupère un nœud spécifique dans la GridPane selon ses coordonnées.
     *
     * @param gridPane Grille cible.
     * @param col      Colonne du nœud.
     * @param row      Ligne du nœud.
     * @return Le nœud correspondant ou null si absent.
     */
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

    /**
     * Affiche une bombe sur la grille à sa position, au-dessus de l'herbe.
     *
     * @param bomb Bombe à afficher.
     */
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

    /**
     * Indique si une case est accessible (pas de mur ni de bombe).
     *
     * @param x Coordonnée X.
     * @param y Coordonnée Y.
     * @return true si déplacement possible, false sinon.
     */
    private boolean canMoveTo(int x, int y) {
        if (x < 0 || y < 0 || y >= map.length || x >= map[0].length) {
            return false;
        }

        Tile tile = map[y][x];

        if (tile.getType() == TileType.WALL || tile.getType() == TileType.WALL_BREAKABLE) {
            return false;
        }

        for (Bomb bomb : activeBombs) {
            if (bomb.getX() == x && bomb.getY() == y) {
                return false;
            }
        }

        return true;
    }

    /**
     * Met à jour la position graphique du joueur sur la grille.
     *
     * @param player Le joueur à afficher.
     */
    private void updatePlayerPosition(Player player) {
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

        addEntityToGrid(player.getX(), player.getY(), playerPattern);
        checkPlayerOnPowerUp(player);
    }

    /**
     * Met à jour la position graphique de l'ennemi sur la grille.
     *
     * @param enemy L'ennemi à afficher.
     */
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

    /**
     * Gère l'explosion d'une bombe et applique les effets sur la grille.
     *
     * @param bomb Bombe qui explose.
     */
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

    /**
     * Détruit une tuile à la position spécifiée (si destructible) et affiche l'explosion.
     *
     * @param x Coordonnée X de la tuile.
     * @param y Coordonnée Y de la tuile.
     * @return true si l'explosion peut continuer au-delà, false sinon.
     */
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
                if (Math.random() < 0.3) { // 30% de chance, adapte le taux si besoin
                    PowerUp powerUp = new PowerUp(x, y, PowerUpType.randomType());
                    activePowerUps.add(powerUp);
                    placePowerUpVisual(powerUp);
                }
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

    /**
     * Vérifie si les coordonnées spécifiées se trouvent dans les limites de la carte.
     *
     * @param x Coordonnée X.
     * @param y Coordonnée Y.
     * @return true si dans les limites, false sinon.
     */
    private boolean isInBounds(int x, int y) {
        return x >= 0 && y >= map.length && y < map.length && x < map[0].length;
    }

    /**
     * Redessine une tuile particulière de la grille (arrière-plan uniquement).
     *
     * @param x Coordonnée X.
     * @param y Coordonnée Y.
     */
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

    /**
     * Déplace l'ennemi selon la direction courante ou choisit une nouvelle direction valide.
     *
     * @param enemy L'ennemi à déplacer.
     */
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

    private void placePowerUpVisual(PowerUp powerUp) {
        StackPane cell = (StackPane) getNodeFromGridPane(gameGrid, powerUp.getX(), powerUp.getY());
        if (cell != null) {
            Rectangle powerUpRect = new Rectangle(40, 40);
            powerUpRect.setFill(powerUpPattern);
            cell.getChildren().add(powerUpRect);
        }
    }

    private void checkPlayerOnPowerUp(Player player) {
        PowerUp toCollect = null;
        for (PowerUp powerUp : activePowerUps) {
            if (powerUp.getX() == player.getX() && powerUp.getY() == player.getY()) {
                toCollect = powerUp;
                break;
            }
        }
        if (toCollect != null) {
            applyPowerUpEffect(player, toCollect);
            removePowerUpVisual(toCollect);
            activePowerUps.remove(toCollect);
        }
    }

    private void removePowerUpVisual(PowerUp powerUp) {
        StackPane cell = (StackPane) getNodeFromGridPane(gameGrid, powerUp.getX(), powerUp.getY());
        if (cell != null) {
            if (cell.getChildren().size() > 1) {
                cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
            }
        }
    }
    private void applyPowerUpEffect(Player player, PowerUp powerUp) {
        switch (powerUp.getType()) {
            case BOMB_RANGE -> player.increaseExplosionRange();
            case BOMB_COUNT -> player.increaseMaxBombs();
            case SPEED -> player.increaseSpeed();

        }
    }
}