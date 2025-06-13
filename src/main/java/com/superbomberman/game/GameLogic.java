/**
 * Logique principale du jeu Super Bomberman.
 * <p>
 * Gère les mouvements des joueurs et de l'ennemi, la gestion des bombes, des power-ups, du score,
 * des collisions, des malus, et la détection des conditions de fin de partie (victoire/défaite).
 * Ce contrôleur centralise toute la logique du gameplay, indépendante de l'interface graphique.
 * </p>
 *
 * <ul>
 *     <li>Mouvements des joueurs et de l'ennemi (IA simple)</li>
 *     <li>Gestion des bombes et explosions</li>
 *     <li>Gestion des power-ups et malus</li>
 *     <li>Détection et gestion des collisions entre entités</li>
 *     <li>Détermination des conditions de victoire et défaite</li>
 *     <li>Gestion du score et des événements de jeu</li>
 * </ul>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-08
 */
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
 * Classe principale de logique du jeu.
 * Centralise la gestion des entités, du score, des conditions de victoire/défaite et des événements principaux.
 */
public class GameLogic {
    /** Carte actuelle du niveau. */
    private Tile[][] map;
    /** Gestionnaire des bombes. */
    private BombManager bombManager;
    /** Gestionnaire des power-ups. */
    private PowerUpManager powerUpManager;
    /** Gestionnaire d'état de partie. */
    private GameStateManager gameStateManager;
    /** Système de score. */
    private ScoreSystem scoreSystem;

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

    // États de mort
    private boolean player1Dead = false;
    private boolean player2Dead = false;
    private boolean enemyDead = false;
    private Player winner = null;

    // Délais de mouvement
    private static final long BASE_MOVE_DELAY = 200_000_000L; // 200ms de base
    private static final long ENEMY_MOVE_DELAY = 500_000_000L; // 500ms pour l'ennemi

    /**
     * Construit la logique du jeu à partir des gestionnaires et de la carte.
     * @param map Carte actuelle du niveau
     * @param bombManager Gestionnaire de bombes
     * @param powerUpManager Gestionnaire de power-ups
     * @param gameStateManager Gestionnaire d'état de partie
     */
    public GameLogic(Tile[][] map, BombManager bombManager, PowerUpManager powerUpManager, GameStateManager gameStateManager) {
        this.map = map;
        this.bombManager = bombManager;
        this.powerUpManager = powerUpManager;
        this.gameStateManager = gameStateManager;
        this.scoreSystem = gameStateManager.getScoreSystem();
        // Enregistrer les joueurs dans le système de score
        scoreSystem.registerPlayer(player1);
        if (!isOnePlayer && player2 != null) {
            scoreSystem.registerPlayer(player2);
        }
    }

    /**
     * Gère le mouvement d'un joueur (vitesse, contrôles, collisions, malus).
     * @param player Le joueur à déplacer
     * @param playerNumber Numéro du joueur (1 ou 2)
     * @param currentTime Horodatage courant (nanosecondes)
     * @param pressedKeys Ensemble des touches appuyées
     * @param visualRenderer Gestionnaire graphique pour MAJ visuelle
     */
    public void handlePlayerMovement(Player player, int playerNumber, long currentTime, Set<KeyCode> pressedKeys, VisualRenderer visualRenderer) {
        if ((playerNumber == 1 && player1Dead) || (playerNumber == 2 && player2Dead)) {
            return;
        }

        player.updateMalus();
        long moveDelay = (long) (BASE_MOVE_DELAY / player.getSpeed());
        long lastMoveTime = (playerNumber == 1) ? lastPlayer1MoveTime : lastPlayer2MoveTime;
        if (currentTime - lastMoveTime < moveDelay) {
            return;
        }

        int newX = player.getX();
        int newY = player.getY();
        boolean moved = false;
        boolean reversed = player.hasMalus(MalusType.REVERSED_CONTROLS);

        KeyCode leftKey, rightKey, upKey, downKey;
        if (playerNumber == 1) {
            leftKey = KeyCode.LEFT;
            rightKey = KeyCode.RIGHT;
            upKey = KeyCode.UP;
            downKey = KeyCode.DOWN;
        } else {
            leftKey = KeyCode.Q;
            rightKey = KeyCode.D;
            upKey = KeyCode.Z;
            downKey = KeyCode.S;
        }

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

        if (moved && canMoveTo(newX, newY, player)) {
            player.setPosition(newX, newY);
            updatePlayerVisualPosition(player, playerNumber, visualRenderer);
            powerUpManager.checkPlayerCollisions(player1, player2, gameStateManager, visualRenderer);
            checkPlayerEnemyCollisions();

            if (playerNumber == 1) {
                lastPlayer1MoveTime = currentTime;
            } else {
                lastPlayer2MoveTime = currentTime;
            }
        }
    }

    /**
     * Gère le mouvement de l'ennemi à intervalle régulier, puis vérifie les collisions.
     * @param currentTime Horodatage actuel
     * @param visualRenderer Gestionnaire graphique
     */
    public void handleEnemyMovement(long currentTime, VisualRenderer visualRenderer) {
        if (currentTime - lastEnemyMoveTime < ENEMY_MOVE_DELAY) {
            return;
        }
        if (enemy != null && enemy.isAlive()) {
            moveEnemy(enemy, visualRenderer);
            checkPlayerEnemyCollisions();
            lastEnemyMoveTime = currentTime;
        }
    }

    /**
     * IA simple pour déplacer l'ennemi (change de direction si bloqué).
     * @param enemy L'ennemi à déplacer
     * @param visualRenderer Gestionnaire graphique
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
            // Changer de direction aléatoirement (hors direction actuelle)
            int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};
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

            if (canMoveTo(newX, newY, enemy)) {
                enemy.setPosition(newX, newY);
                visualRenderer.updateEnemyPosition(enemy, bombManager.getActiveBombs());
            }
        }
    }

    /**
     * Vérifie si une entité peut se déplacer vers une position (en fonction des murs, bombes, etc.).
     * @param x abscisse cible
     * @param y ordonnée cible
     * @param entity entité qui veut se déplacer
     * @return true si le déplacement est possible, false sinon
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
            if (entity instanceof Player player) {
                return player.canPassThroughWalls();
            }
            return false;
        }

        // Vérifier les bombes sur la case
        for (Bomb bomb : bombManager.getActiveBombs()) {
            if (bomb.getX() == x && bomb.getY() == y) {
                if (entity instanceof Player player) {
                    // Power Kick
                    if (player.canKickBombs() && !bomb.isFlying() && !bomb.isMoving()) {
                        int kickDirX = x - player.getX();
                        int kickDirY = y - player.getY();
                        if (bombManager.tryKickBomb(bomb, kickDirX, kickDirY)) {
                            return true;
                        }
                    }
                    // BombPass
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
     * Met à jour la position visuelle d'un joueur.
     * @param player Joueur concerné
     * @param playerNumber Numéro du joueur
     * @param visualRenderer Gestionnaire graphique
     */
    private void updatePlayerVisualPosition(Player player, int playerNumber, VisualRenderer visualRenderer) {
        if (playerNumber == 1) {
            visualRenderer.updatePlayerPosition(player, visualRenderer.getPlayerPattern(), bombManager.getActiveBombs());
        } else {
            visualRenderer.updatePlayerPosition(player, visualRenderer.getPlayer2Pattern(), bombManager.getActiveBombs());
        }
    }

    /**
     * Définit la dernière direction d'un joueur (pour les actions directionnelles comme le lancer de bombe).
     * @param playerNumber Numéro du joueur
     * @param dirX Direction X
     * @param dirY Direction Y
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
     * Retourne la dernière direction utilisée par le joueur.
     * @param playerNumber Numéro du joueur
     * @return tableau [dirX, dirY]
     */
    public int[] getLastDirection(int playerNumber) {
        if (playerNumber == 1) {
            return new int[]{lastPlayer1DirectionX, lastPlayer1DirectionY};
        } else {
            return new int[]{lastPlayer2DirectionX, lastPlayer2DirectionY};
        }
    }

    /**
     * Gère l'explosion d'une case, destruction des murs, génération de power-ups, et effet graphique.
     * @param x abscisse cible
     * @param y ordonnée cible
     * @param visualRenderer gestionnaire graphique
     * @param player joueur à qui attribuer les points
     */
    public void handleExplosion(int x, int y, VisualRenderer visualRenderer, Player player) {
        visualRenderer.showExplosion(x, y);
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
        delay.setOnFinished(event -> {
            Tile tile = map[y][x];
            if (tile.getType() == TileType.WALL_BREAKABLE) {
                map[y][x] = new Tile(TileType.FLOOR);
                scoreSystem.addWallDestroyed(player);
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
     * Gère l'explosion sur une case et tue les entités concernées. Déclenche potentiellement la fin de partie.
     * @param x abscisse de la case
     * @param y ordonnée de la case
     */
    public void handleExplosionAt(int x, int y) {
        boolean gameEnded = false;

        if (player1 != null && player1.isAlive() && player1.getX() == x && player1.getY() == y) {
            player1.setAlive(false);
            player1Dead = true;
            System.out.println("💀 Joueur 1 éliminé par explosion à (" + x + ", " + y + ")");
            gameEnded = true;
        }
        if (!isOnePlayer && player2 != null && player2.isAlive() && player2.getX() == x && player2.getY() == y) {
            player2.setAlive(false);
            player2Dead = true;
            System.out.println("💀 Joueur 2 éliminé par explosion à (" + x + ", " + y + ")");
            gameEnded = true;
        }
        if (enemy != null && enemy.isAlive() && enemy.getX() == x && enemy.getY() == y) {
            enemy.kill();
            enemyDead = true;
            System.out.println("💀 Ennemi éliminé par explosion à (" + x + ", " + y + ")");
            if (isOnePlayer) {
                gameStateManager.setGameWon(true);
                gameEnded = true;
            }
        }
        if (gameEnded) {
            checkAndEndGame();
        }
    }

    /**
     * Gestion de la fin de partie (victoire/défaite) en fonction des états des entités.
     */
    public void checkAndEndGame() {
        if (isOnePlayer) {
            if (player1Dead) {
                gameStateManager.setGameWon(false);
                gameStateManager.endGame();
                return;
            }
            if (enemyDead || (enemy != null && enemy.isDead())) {
                gameStateManager.setGameWon(true);
                gameStateManager.endGame();
            }
        } else {
            if (player1Dead && player2Dead) {
                gameStateManager.setGameWon(false);
                gameStateManager.endGame();
            } else if (player1Dead) {
                winner = player2;
                gameStateManager.setGameWon(false);
                gameStateManager.endGame();
            } else if (player2Dead) {
                winner = player1;
                gameStateManager.setGameWon(true);
                gameStateManager.endGame();
            }
        }
    }

    /**
     * Vérifie les collisions entre joueur(s) et ennemi, et tue le joueur si collision.
     */
    private void checkPlayerEnemyCollisions() {
        if (enemy == null || enemy.isDead()) {
            return;
        }
        if (player1 != null && player1.isAlive() &&
                player1.getX() == enemy.getX() && player1.getY() == enemy.getY()) {
            player1.setAlive(false);
            player1Dead = true;
            System.out.println("💀 Joueur 1 tué par l'ennemi à (" + enemy.getX() + ", " + enemy.getY() + ")");
            checkAndEndGame();
        }
        if (!isOnePlayer && player2 != null && player2.isAlive() &&
                player2.getX() == enemy.getX() && player2.getY() == enemy.getY()) {
            player2.setAlive(false);
            player2Dead = true;
            System.out.println("💀 Joueur 2 tué par l'ennemi à (" + enemy.getX() + ", " + enemy.getY() + ")");
            checkAndEndGame();
        }
    }

    /**
     * Vérifie les conditions de victoire/défaite à chaque cycle d'update.
     */
    public void checkGameConditions() {
        if (isOnePlayer && enemy != null && enemy.isDead()) {
            gameStateManager.setGameWon(true);
            gameStateManager.endGame();
            return;
        }
        if (isPlayerDefeated()) {
            gameStateManager.setGameWon(false);
            gameStateManager.endGame();
        }
    }

    /**
     * Vérifie si l'ennemi est vaincu.
     * @return true si l'ennemi est mort
     */
    private boolean isEnemyDefeated() {
        return enemy != null && enemy.isDead();
    }

    /**
     * Vérifie si le joueur est vaincu (mode solo ou multi).
     * @return true si le(s) joueur(s) sont morts
     */
    private boolean isPlayerDefeated() {
        if (isOnePlayer) {
            return player1 == null || !player1.isAlive();
        } else {
            boolean player1Alive = player1 != null && player1.isAlive();
            boolean player2Alive = player2 != null && player2.isAlive();
            return !player1Alive && !player2Alive;
        }
    }

    /**
     * Gère les malus automatiques (ex : AUTO_BOMB).
     * @param currentTime Horodatage actuel
     */
    public void handleAutoBombMalus(long currentTime) {
        final long AUTO_BOMB_INTERVAL = 2_000_000_000L; // 2 secondes

        if (player1.hasMalus(MalusType.AUTO_BOMB)) {
            bombManager.placeBomb(player1, 1);
        }
        if (!isOnePlayer && player2 != null && player2.hasMalus(MalusType.AUTO_BOMB)) {
            bombManager.placeBomb(player2, 2);
        }
    }

    /**
     * Met à jour toutes les entités du jeu : bombes, power-ups, collisions.
     * @param visualRenderer gestionnaire graphique
     */
    public void updateEntities(VisualRenderer visualRenderer) {
        bombManager.updateBombs();
        powerUpManager.checkPlayerCollisions(player1, player2, gameStateManager, visualRenderer);
        checkPlayerEnemyCollisions();
        checkGameConditions();
    }

    /**
     * Calcule la distance euclidienne entre deux points.
     * @param x1 abscisse 1
     * @param y1 ordonnée 1
     * @param x2 abscisse 2
     * @param y2 ordonnée 2
     * @return distance
     */
    public double calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * IA basique : trouve le déplacement rapprochant l'ennemi du joueur le plus proche.
     * @param enemy L'ennemi
     * @return tableau directionnel [dx, dy]
     */
    public int[] findBestMoveForEnemy(Enemy enemy) {
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

    /**
     * Vérifie la validité d'une position pour une entité (murs, bombes, etc.).
     * @param x abscisse
     * @param y ordonnée
     * @param entity entité concernée
     * @return true si la position est valide
     */
    private boolean isValidPosition(int x, int y, Object entity) {
        if (x < 0 || y < 0 || y >= map.length || x >= map[0].length) {
            return false;
        }

        Tile tile = map[y][x];

        if (tile.getType() == TileType.WALL) {
            return false;
        }

        if (tile.getType() == TileType.WALL_BREAKABLE) {
            if (entity instanceof Player player) {
                return player.canPassThroughWalls();
            }
            return false;
        }

        for (Bomb bomb : bombManager.getActiveBombs()) {
            if (bomb.getX() == x && bomb.getY() == y) {
                if (entity instanceof Player player) {
                    return player.canPassThroughBombs();
                }
                return false;
            }
        }

        return true;
    }

    // === GETTERS pour états de mort et directions ===

    /** @return true si le joueur 1 est mort */
    public boolean isPlayer1Dead() { return player1Dead; }
    /** @return true si le joueur 2 est mort */
    public boolean isPlayer2Dead() { return player2Dead; }
    /** @return true si l'ennemi est mort */
    public boolean isEnemyDead() { return enemyDead; }
    /** @return le joueur gagnant (multijoueur) */
    public Player getWinner() { return winner; }

    // Getters pour les directions
    public int getLastPlayer1DirectionX() { return lastPlayer1DirectionX; }
    public int getLastPlayer1DirectionY() { return lastPlayer1DirectionY; }
    public int getLastPlayer2DirectionX() { return lastPlayer2DirectionX; }
    public int getLastPlayer2DirectionY() { return lastPlayer2DirectionY; }
}