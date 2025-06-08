package com.superbomberman.game;

import com.superbomberman.model.*;
import com.superbomberman.model.powerup.MalusType;
import com.superbomberman.model.powerup.PowerUp;
import javafx.scene.input.KeyCode;
import java.util.Set;

import static com.superbomberman.model.MapLoader.enemy;
import static com.superbomberman.model.MapLoader.player1;
import static com.superbomberman.model.MapLoader.player2;
import static com.superbomberman.controller.MenuController.isOnePlayer;

/**
 * Logique principale du jeu Super Bomberman
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-08
 */
public class GameLogic {
    private Tile[][] map;
    private BombManager bombManager;
    private PowerUpManager powerUpManager;
    private GameStateManager gameStateManager;

    // Gestion du mouvement
    private long lastPlayer1MoveTime = 0;
    private long lastPlayer2MoveTime = 0;
    private long lastEnemyMoveTime = 0;
    private int[] enemyCurrDirection = {1, 0};

    // Directions des joueurs pour le lancer de bombes
    private int lastPlayer1DirectionX = 0;
    private int lastPlayer1DirectionY = 1;
    private int lastPlayer2DirectionX = 0;
    private int lastPlayer2DirectionY = 1;

    // Délais de mouvement
    private static final long BASE_MOVE_DELAY = 200_000_000L; // 200ms de base
    private static final long ENEMY_MOVE_DELAY = 500_000_000L; // 500ms pour l'ennemi

    public GameLogic(Tile[][] map, BombManager bombManager, PowerUpManager powerUpManager, GameStateManager gameStateManager) {
        this.map = map;
        this.bombManager = bombManager;
        this.powerUpManager = powerUpManager;
        this.gameStateManager = gameStateManager;
    }

    /**
     * Gère le mouvement d'un joueur
     */
    public void handlePlayerMovement(Player player, int playerNumber, long currentTime, Set<KeyCode> pressedKeys, VisualRenderer visualRenderer) {
        // Mettre à jour les malus du joueur
        player.updateMalus();

        // Calculer le délai basé sur la vitesse du joueur
        long moveDelay = (long) (BASE_MOVE_DELAY / player.getSpeed());

        // Vérifier si assez de temps s'est écoulé depuis le dernier mouvement
        long lastMoveTime = (playerNumber == 1) ? lastPlayer1MoveTime : lastPlayer2MoveTime;
        if (currentTime - lastMoveTime < moveDelay) {
            return;
        }

        int newX = player.getX();
        int newY = player.getY();
        boolean moved = false;

        // Gérer les contrôles inversés
        boolean reversed = player.hasMalus(MalusType.REVERSED_CONTROLS);

        // Déterminer les touches selon le joueur
        KeyCode leftKey, rightKey, upKey, downKey;
        if (playerNumber == 1) {
            // Joueur 1: Flèches directionnelles
            leftKey = KeyCode.LEFT;
            rightKey = KeyCode.RIGHT;
            upKey = KeyCode.UP;
            downKey = KeyCode.DOWN;
        } else {
            // Joueur 2: ZQSD (AZERTY français)
            leftKey = KeyCode.Q;    // Q = gauche
            rightKey = KeyCode.D;   // D = droite
            upKey = KeyCode.Z;      // Z = haut
            downKey = KeyCode.S;    // S = bas
        }

        // Calculer le mouvement
        if (pressedKeys.contains(leftKey)) {
            newX += reversed ? 1 : -1;
            setLastDirection(playerNumber, reversed ? 1 : -1, 0);
            moved = true;
        } else if (pressedKeys.contains(rightKey)) {
            newX += reversed ? -1 : 1;
            setLastDirection(playerNumber, reversed ? -1 : 1, 0);
            moved = true;
        } else if (pressedKeys.contains(upKey)) {
            newY += reversed ? 1 : -1;
            setLastDirection(playerNumber, 0, reversed ? 1 : -1);
            moved = true;
        } else if (pressedKeys.contains(downKey)) {
            newY += reversed ? -1 : 1;
            setLastDirection(playerNumber, 0, reversed ? -1 : 1);
            moved = true;
        }

        // Effectuer le mouvement si possible
        if (moved && canMoveTo(newX, newY, player)) {
            player.setPosition(newX, newY);

            // Mettre à jour la position visuelle
            updatePlayerVisualPosition(player, playerNumber, visualRenderer);

            // Vérifier les power-ups
            powerUpManager.checkPlayerCollisions(player1, player2, gameStateManager, visualRenderer);

            // Mettre à jour le timestamp
            if (playerNumber == 1) {
                lastPlayer1MoveTime = currentTime;
            } else {
                lastPlayer2MoveTime = currentTime;
            }
        }
    }

    /**
     * Gère le mouvement de l'ennemi
     */
    public void handleEnemyMovement(long currentTime, VisualRenderer visualRenderer) {
        if (currentTime - lastEnemyMoveTime < ENEMY_MOVE_DELAY) {
            return;
        }

        if (enemy != null) {
            moveEnemy(enemy, visualRenderer);
            lastEnemyMoveTime = currentTime;
        }
    }

    /**
     * Déplace l'ennemi avec une IA simple
     */
    private void moveEnemy(Enemy enemy, VisualRenderer visualRenderer) {
        int currentX = enemy.getX();
        int currentY = enemy.getY();

        int newX = currentX + enemyCurrDirection[0];
        int newY = currentY + enemyCurrDirection[1];

        if (canMoveTo(newX, newY, enemy)) {
            enemy.setPosition(newX, newY);
            visualRenderer.updateEnemyPosition(enemy, bombManager.getActiveBombs());
        } else {
            // Changer de direction aléatoirement
            int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};
            int[][] possibleDirections = new int[3][2];
            int idx = 0;

            // Éviter de revenir en arrière
            for (int[] dir : directions) {
                if (!(dir[0] == enemyCurrDirection[0] && dir[1] == enemyCurrDirection[1])) {
                    possibleDirections[idx++] = dir;
                }
            }

            int randomIndex = (int) (Math.random() * possibleDirections.length);
            enemyCurrDirection = possibleDirections[randomIndex];

            newX = currentX + enemyCurrDirection[0];
            newY = currentY + enemyCurrDirection[1];

            if (canMoveTo(newX, newY, enemy)) {
                enemy.setPosition(newX, newY);
                visualRenderer.updateEnemyPosition(enemy, bombManager.getActiveBombs());
            }
        }
    }

    /**
     * Vérifie si une entité peut se déplacer vers une position
     */
    private boolean canMoveTo(int x, int y, Object entity) {
        if (x < 0 || y < 0 || y >= map.length || x >= map[0].length) {
            return false;
        }

        Tile tile = map[y][x];

        if (tile.getType() == TileType.WALL) {
            return false;
        }

        if (tile.getType() == TileType.WALL_BREAKABLE) {
            // Vérifier WallPass
            if (entity instanceof Player player) {
                return player.canPassThroughWalls();
            }
            return false;
        }

        // Vérifier les bombes
        for (Bomb bomb : bombManager.getActiveBombs()) {
            if (bomb.getX() == x && bomb.getY() == y) {
                if (entity instanceof Player player) {
                    // Vérifier Kick Power
                    if (player.canKickBombs() && !bomb.isFlying() && !bomb.isMoving()) {
                        int kickDirX = x - player.getX();
                        int kickDirY = y - player.getY();

                        if (bombManager.tryKickBomb(bomb, kickDirX, kickDirY)) {
                            return true;
                        }
                    }

                    // Vérifier BombPass
                    if (player.canPassThroughBombs() && bomb.getOwner() == player) {
                        return true;
                    }
                }
                return false;
            }
        }

        return true;
    }

    /**
     * Met à jour la position visuelle d'un joueur
     */
    private void updatePlayerVisualPosition(Player player, int playerNumber, VisualRenderer visualRenderer) {
        if (playerNumber == 1) {
            visualRenderer.updatePlayerPosition(player, visualRenderer.getPlayerPattern(), bombManager.getActiveBombs());
        } else {
            visualRenderer.updatePlayerPosition(player, visualRenderer.getPlayer2Pattern(), bombManager.getActiveBombs());
        }
    }

    /**
     * Définit la dernière direction d'un joueur
     */
    private void setLastDirection(int playerNumber, int dirX, int dirY) {
        if (playerNumber == 1) {
            lastPlayer1DirectionX = dirX;
            lastPlayer1DirectionY = dirY;
        } else {
            lastPlayer2DirectionX = dirX;
            lastPlayer2DirectionY = dirY;
        }
    }

    /**
     * Obtient la dernière direction d'un joueur
     */
    public int[] getLastDirection(int playerNumber) {
        if (playerNumber == 1) {
            return new int[]{lastPlayer1DirectionX, lastPlayer1DirectionY};
        } else {
            return new int[]{lastPlayer2DirectionX, lastPlayer2DirectionY};
        }
    }

    /**
     * Gère la logique d'explosion et la génération de power-ups
     */
    public void handleExplosion(int x, int y, VisualRenderer visualRenderer) {
        visualRenderer.showExplosion(x, y);

        // Délai pour l'animation d'explosion
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
        delay.setOnFinished(event -> {
            Tile tile = map[y][x];

            if (tile.getType() == TileType.WALL_BREAKABLE) {
                map[y][x] = new Tile(TileType.FLOOR);

                // Ajouter des points pour la destruction de murs
                gameStateManager.updateScore(10);

                // Générer un power-up aléatoire
                PowerUp powerUp = powerUpManager.generateRandomPowerUp(x, y);
                if (powerUp != null) {
                    visualRenderer.placePowerUpVisual(powerUp);
                }
            }

            visualRenderer.redrawTile(x, y, powerUpManager.getActivePowerUps());
        });
        delay.play();
    }

    /**
     * Vérifie les conditions de victoire/défaite
     */
    public void checkGameConditions() {
        // Vérifier si l'ennemi est vaincu
        if (enemy != null && isEnemyDefeated()) {
            gameStateManager.setGameWon(true);
        }

        // Vérifier si le joueur est vaincu
        if (isPlayerDefeated()) {
            gameStateManager.setGameWon(false);
        }
    }

    /**
     * Vérifie si l'ennemi est vaincu
     */
    private boolean isEnemyDefeated() {
        // Vérifier si l'ennemi est sur une case d'explosion
        // Cette logique sera à implémenter selon les besoins
        return false; // Placeholder
    }

    /**
     * Vérifie si le joueur est vaincu
     */
    private boolean isPlayerDefeated() {
        // Vérifier si le joueur est sur une case d'explosion
        // Cette logique sera à implémenter selon les besoins
        return false; // Placeholder
    }

    /**
     * Gère les malus automatiques (AUTO_BOMB)
     */
    public void handleAutoBombMalus(long currentTime) {
        final long AUTO_BOMB_INTERVAL = 2_000_000_000L; // 2 secondes

        // Variables statiques pour les timestamps (à améliorer)
        if (player1.hasMalus(MalusType.AUTO_BOMB)) {
            // Logic pour AUTO_BOMB du joueur 1
            bombManager.placeBomb(player1, 1);
        }

        if (!isOnePlayer && player2 != null && player2.hasMalus(MalusType.AUTO_BOMB)) {
            // Logic pour AUTO_BOMB du joueur 2
            bombManager.placeBomb(player2, 2);
        }
    }

    /**
     * Met à jour toutes les entités du jeu
     */
    public void updateEntities(VisualRenderer visualRenderer) {
        // Mettre à jour les bombes
        bombManager.updateBombs();

        // Vérifier les collisions avec les power-ups
        powerUpManager.checkPlayerCollisions(player1, player2, gameStateManager, visualRenderer);

        // Vérifier les conditions de jeu
        checkGameConditions();
    }

    /**
     * Calcule la distance entre deux points
     */
    public double calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Trouve le chemin le plus court pour l'ennemi (IA basique)
     */
    public int[] findBestMoveForEnemy(Enemy enemy) {
        // IA simple : se rapprocher du joueur le plus proche
        double minDistance = Double.MAX_VALUE;
        int[] bestMove = {0, 0};

        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};

        for (int[] dir : directions) {
            int newX = enemy.getX() + dir[0];
            int newY = enemy.getY() + dir[1];

            if (canMoveTo(newX, newY, enemy)) {
                double distanceToPlayer1 = calculateDistance(newX, newY, player1.getX(), player1.getY());
                double distance = distanceToPlayer1;

                if (!isOnePlayer && player2 != null) {
                    double distanceToPlayer2 = calculateDistance(newX, newY, player2.getX(), player2.getY());
                    distance = Math.min(distanceToPlayer1, distanceToPlayer2);
                }

                if (distance < minDistance) {
                    minDistance = distance;
                    bestMove = dir;
                }
            }
        }

        return bestMove;
    }

    // Getters pour les directions
    public int getLastPlayer1DirectionX() { return lastPlayer1DirectionX; }
    public int getLastPlayer1DirectionY() { return lastPlayer1DirectionY; }
    public int getLastPlayer2DirectionX() { return lastPlayer2DirectionX; }
    public int getLastPlayer2DirectionY() { return lastPlayer2DirectionY; }
}