package com.superbomberman.controller;

import com.superbomberman.model.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.animation.AnimationTimer;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.superbomberman.model.powerup.*;

import static com.superbomberman.model.MapLoader.enemy;
import static com.superbomberman.model.MapLoader.player1;
import static com.superbomberman.model.MapLoader.player2;
import static com.superbomberman.controller.MenuController.isOnePlayer;

/**
 * Contrôleur principal de la vue de jeu pour Super Bomberman.
 * Gère l'initialisation de la grille de jeu, le rendu de la carte,
 * la gestion des entités (joueurs, ennemi, bombes), le déplacement des personnages,
 * la pose et la détonation des bombes, ainsi que les interactions clavier.
 * Support complet du mode 1 et 2 joueurs avec toutes les fonctionnalités de power-ups.
 *
 * Contrôles:
 * - Joueur 1: Flèches directionnelles + SPACE (bombe) + SHIFT (glove) + L (line) + R (remote)
 * - Joueur 2: ZQSD + ENTER (bombe) + CTRL (glove) + K (line) + O (remote)
 *
 * @author Jules Fuselier
 * @version 2.1
 * @since 2025-06-08
 */
public class GameViewController extends OptionsController {

    @FXML
    private GridPane gameGrid;

    private Tile[][] map;
    private int[] enemyCurrDirection;
    private List<Bomb> activeBombs = new ArrayList<>();

    // Compteurs de bombes séparés pour chaque joueur
    private int currentBombCountPlayer1 = 0;
    private int currentBombCountPlayer2 = 0;

    // Gestion des touches pressées
    private Set<javafx.scene.input.KeyCode> pressedKeys = new HashSet<>();

    // Timer pour le mouvement continu
    private AnimationTimer gameLoop;

    // Timestamps pour le mouvement
    private long lastPlayer1MoveTime = 0;
    private long lastPlayer2MoveTime = 0;
    private long lastEnemyMoveTime = 0;

    // Délais de mouvement
    private static final long BASE_MOVE_DELAY = 200_000_000L; // 200ms de base
    private static final long ENEMY_MOVE_DELAY = 500_000_000L; // 500ms pour l'ennemi

    // Directions des joueurs pour le lancer de bombes
    private int lastPlayer1DirectionX = 0;
    private int lastPlayer1DirectionY = 1;
    private int lastPlayer2DirectionX = 0;
    private int lastPlayer2DirectionY = 1;

    // Listes pour les bombes spéciales
    private List<Bomb> flyingBombs = new ArrayList<>();
    private List<Bomb> kickingBombs = new ArrayList<>();
    private List<PowerUp> activePowerUps = new ArrayList<>();

    // Images et patterns
    private Image powerUpImg = new Image(Objects.requireNonNull(getClass().getResource("/images/powerup.png")).toExternalForm());
    private ImagePattern powerUpPattern = new ImagePattern(powerUpImg);

    private Image playerImg = new Image(Objects.requireNonNull(getClass().getResource("/images/player.png")).toExternalForm());
    private ImagePattern playerPattern = new ImagePattern(playerImg);

    private Image player2Img = new Image(Objects.requireNonNull(getClass().getResource("/images/player2.png")).toExternalForm());
    private ImagePattern player2Pattern = new ImagePattern(player2Img);

    private Image wallImg = new Image(Objects.requireNonNull(getClass().getResource("/images/wall.png")).toExternalForm());
    private ImagePattern wallPattern = new ImagePattern(wallImg);

    private Image wallBreakableImg = new Image(Objects.requireNonNull(getClass().getResource("/images/wall_breakable.png")).toExternalForm());
    private ImagePattern wallBreakablePattern = new ImagePattern(wallBreakableImg);

    private Image floorImg = new Image(Objects.requireNonNull(getClass().getResource("/images/grass.png")).toExternalForm());
    private ImagePattern floorPattern = new ImagePattern(floorImg);

    private Image explosionImg = new Image(Objects.requireNonNull(getClass().getResource("/images/explosion.png")).toExternalForm());
    private ImagePattern explosionPattern = new ImagePattern(explosionImg);

    private Image enemyImg = new Image(Objects.requireNonNull(getClass().getResource("/images/enemy.png")).toExternalForm());
    private ImagePattern enemyPattern = new ImagePattern(enemyImg);

    private Image bombImg = new Image(Objects.requireNonNull(getClass().getResource("/images/bomb.png")).toExternalForm());
    private ImagePattern bombPattern = new ImagePattern(bombImg);

    public void initialize() {
        try {
            System.out.println("Mode un joueur: " + isOnePlayer);
            if (isOnePlayer) {
                System.out.println("Chargement de la carte niveau 1 (1 joueur)");
                map = MapLoader.loadMap("src/main/resources/maps/level1.txt");
            } else {
                System.out.println("Chargement de la carte niveau 2 (2 joueurs)");
                map = MapLoader.loadMap("src/main/resources/maps/level2.txt");
            }

            drawMap(map);
            enemyCurrDirection = new int[]{1, 0};
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la carte");
        }

        // Initialiser le joueur 1
        if (map[player1.getY()][player1.getX()].getType() == TileType.FLOOR) {
            map[player1.getY()][player1.getX()] = new Tile(TileType.PLAYER1);
            addEntityToGrid(player1.getX(), player1.getY(), playerPattern);
        }

        // Initialiser le joueur 2 (mode 2 joueurs uniquement)
        if (!isOnePlayer && player2 != null && map[player2.getY()][player2.getX()].getType() == TileType.FLOOR) {
            map[player2.getY()][player2.getX()] = new Tile(TileType.PLAYER2);
            addEntityToGrid(player2.getX(), player2.getY(), player2Pattern);
        }

        // Initialiser l'ennemi
        if (enemy != null && map[enemy.getY()][enemy.getX()].getType() == TileType.FLOOR) {
            map[enemy.getY()][enemy.getX()] = new Tile(TileType.ENEMY);
            addEntityToGrid(enemy.getX(), enemy.getY(), enemyPattern);
        }

        // Gestion simple des événements clavier (actions immédiates uniquement)
        gameGrid.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());
        });

        gameGrid.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
        });

        // Démarrer la boucle de jeu
        startGameLoop();

        gameGrid.setFocusTraversable(true);
        gameGrid.requestFocus();

        // Afficher les contrôles
        displayControls();
    }

    /**
     * Affiche les contrôles dans la console pour information.
     */
    private void displayControls() {
        System.out.println("=== CONTRÔLES DU JEU ===");
        System.out.println("Joueur 1 (Bleu):");
        System.out.println("  - Déplacement: Flèches directionnelles");
        System.out.println("  - Bombe: ESPACE");
        System.out.println("  - Ramasser/Lancer: SHIFT");
        System.out.println("  - LineBomb: L");
        System.out.println("  - Remote: R");

        if (!isOnePlayer) {
            System.out.println("\nJoueur 2 (Rouge):");
            System.out.println("  - Déplacement: Z-Q-S-D (AZERTY)");
            System.out.println("  - Bombe: ENTRÉE");
            System.out.println("  - Ramasser/Lancer: CTRL");
            System.out.println("  - LineBomb: K");
            System.out.println("  - Remote: O");
        }
        System.out.println("========================");
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastAutoBombTimePlayer1 = 0;
            private long lastAutoBombTimePlayer2 = 0;
            private final long AUTO_BOMB_INTERVAL = 2_000_000_000L; // 2 secondes

            @Override
            public void handle(long now) {
                // Gestion des actions immédiates (bombes, powers)
                handleImmediateActions();

                // Gestion du mouvement des joueurs
                handlePlayerMovement(player1, 1, now);
                if (!isOnePlayer && player2 != null) {
                    handlePlayerMovement(player2, 2, now);
                }

                // Gestion du mouvement de l'ennemi
                handleEnemyMovement(now);

                // Gestion des bombes spéciales
                handleFlyingBombs();
                handleKickingBombs();

                // Gestion du malus AUTO_BOMB
                if (player1.hasMalus(MalusType.AUTO_BOMB) && now - lastAutoBombTimePlayer1 >= AUTO_BOMB_INTERVAL) {
                    placeBomb(player1, 1);
                    lastAutoBombTimePlayer1 = now;
                }

                if (!isOnePlayer && player2 != null && player2.hasMalus(MalusType.AUTO_BOMB) &&
                        now - lastAutoBombTimePlayer2 >= AUTO_BOMB_INTERVAL) {
                    placeBomb(player2, 2);
                    lastAutoBombTimePlayer2 = now;
                }
            }
        };
        gameLoop.start();
    }

    private void handleImmediateActions() {
        // Actions du joueur 1 (Flèches + SPACE + SHIFT + L + R)
        if (pressedKeys.contains(javafx.scene.input.KeyCode.SPACE)) {
            placeBomb(player1, 1);
            pressedKeys.remove(javafx.scene.input.KeyCode.SPACE); // Éviter la répétition
        }
        if (pressedKeys.contains(javafx.scene.input.KeyCode.SHIFT)) {
            handleBombPickupOrThrow(player1, 1);
            pressedKeys.remove(javafx.scene.input.KeyCode.SHIFT);
        }
        if (pressedKeys.contains(javafx.scene.input.KeyCode.L)) {
            placeLineBombs(player1, 1);
            pressedKeys.remove(javafx.scene.input.KeyCode.L);
        }
        if (pressedKeys.contains(javafx.scene.input.KeyCode.R)) {
            detonateRemoteBombs(player1, 1);
            pressedKeys.remove(javafx.scene.input.KeyCode.R);
        }

        // Actions du joueur 2 (ZQSD + ENTER + CTRL + K + O)
        if (!isOnePlayer && player2 != null) {
            if (pressedKeys.contains(javafx.scene.input.KeyCode.ENTER)) {
                placeBomb(player2, 2);
                pressedKeys.remove(javafx.scene.input.KeyCode.ENTER);
            }
            if (pressedKeys.contains(javafx.scene.input.KeyCode.CONTROL)) {
                handleBombPickupOrThrow(player2, 2);
                pressedKeys.remove(javafx.scene.input.KeyCode.CONTROL);
            }
            if (pressedKeys.contains(javafx.scene.input.KeyCode.K)) {
                placeLineBombs(player2, 2);
                pressedKeys.remove(javafx.scene.input.KeyCode.K);
            }
            if (pressedKeys.contains(javafx.scene.input.KeyCode.O)) {
                detonateRemoteBombs(player2, 2);
                pressedKeys.remove(javafx.scene.input.KeyCode.O);
            }
        }
    }

    private void handlePlayerMovement(Player player, int playerNumber, long currentTime) {
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
        javafx.scene.input.KeyCode leftKey, rightKey, upKey, downKey;
        if (playerNumber == 1) {
            // Joueur 1: Flèches directionnelles
            leftKey = javafx.scene.input.KeyCode.LEFT;
            rightKey = javafx.scene.input.KeyCode.RIGHT;
            upKey = javafx.scene.input.KeyCode.UP;
            downKey = javafx.scene.input.KeyCode.DOWN;
        } else {
            // Joueur 2: ZQSD (AZERTY français)
            leftKey = javafx.scene.input.KeyCode.Q;    // Q = gauche
            rightKey = javafx.scene.input.KeyCode.D;   // D = droite
            upKey = javafx.scene.input.KeyCode.Z;      // Z = haut
            downKey = javafx.scene.input.KeyCode.S;    // S = bas
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
            ImagePattern pattern = (playerNumber == 1) ? playerPattern : player2Pattern;
            updatePlayerPosition(player, pattern);

            // Mettre à jour le timestamp
            if (playerNumber == 1) {
                lastPlayer1MoveTime = currentTime;
            } else {
                lastPlayer2MoveTime = currentTime;
            }
        }
    }

    private void setLastDirection(int playerNumber, int dirX, int dirY) {
        if (playerNumber == 1) {
            lastPlayer1DirectionX = dirX;
            lastPlayer1DirectionY = dirY;
        } else {
            lastPlayer2DirectionX = dirX;
            lastPlayer2DirectionY = dirY;
        }
    }

    private int[] getLastDirection(int playerNumber) {
        if (playerNumber == 1) {
            return new int[]{lastPlayer1DirectionX, lastPlayer1DirectionY};
        } else {
            return new int[]{lastPlayer2DirectionX, lastPlayer2DirectionY};
        }
    }

    private void handleEnemyMovement(long currentTime) {
        if (currentTime - lastEnemyMoveTime < ENEMY_MOVE_DELAY) {
            return;
        }

        if (enemy != null) {
            moveEnemy(enemy);
            lastEnemyMoveTime = currentTime;
        }
    }

    private void placeBomb(Player player, int playerNumber) {
        // Vérifier le malus NO_BOMB
        if (player.hasMalus(MalusType.NO_BOMB)) {
            System.out.println("Joueur " + playerNumber + ": Impossible de poser une bombe à cause du malus!");
            return;
        }

        // Vérifier qu'on n'est pas dans un mur destructible
        if (map[player.getY()][player.getX()].getType() == TileType.WALL_BREAKABLE) {
            System.out.println("Joueur " + playerNumber + ": Impossible de poser une bombe à l'intérieur d'un mur destructible!");
            return;
        }

        int currentBombCount = (playerNumber == 1) ? currentBombCountPlayer1 : currentBombCountPlayer2;

        if (currentBombCount < player.getMaxBombs()) {
            System.out.println("Joueur " + playerNumber + ": Pose d'une bombe (" + (currentBombCount + 1) + "/" + player.getMaxBombs() + ")");

            Bomb bomb = new Bomb(player.getX(), player.getY(), 10, player.getExplosionRange());
            bomb.setOwner(player);

            placeBombVisual(bomb);
            activeBombs.add(bomb);

            // Incrémenter le bon compteur
            if (playerNumber == 1) {
                currentBombCountPlayer1++;
            } else {
                currentBombCountPlayer2++;
            }

            // Gérer Remote Power
            if (player.hasRemoteDetonation()) {
                System.out.println("Joueur " + playerNumber + ": Remote Power activé ! Bombe en attente de détonation manuelle.");
            } else {
                bomb.startCountdown(() -> {
                    handleExplosion(bomb);
                    activeBombs.remove(bomb);
                    flyingBombs.remove(bomb);
                    kickingBombs.remove(bomb);

                    // Décrémenter le bon compteur
                    if (playerNumber == 1) {
                        currentBombCountPlayer1--;
                    } else {
                        currentBombCountPlayer2--;
                    }

                    System.out.println("Joueur " + playerNumber + ": Bombe explosée. Bombes restantes : " +
                            ((playerNumber == 1) ? currentBombCountPlayer1 : currentBombCountPlayer2) + "/" + player.getMaxBombs());
                });
            }
        } else {
            System.out.println("Joueur " + playerNumber + ": Limite de bombes atteinte (" + player.getMaxBombs() + ")");
        }
    }

    private void handleBombPickupOrThrow(Player player, int playerNumber) {
        if (!player.canThrowBombs()) {
            System.out.println("Joueur " + playerNumber + ": Pas le pouvoir Glove !");
            return;
        }

        if (player.isHoldingBomb()) {
            throwHeldBomb(player, playerNumber);
        } else {
            tryPickupBomb(player, playerNumber);
        }
    }

    private void tryPickupBomb(Player player, int playerNumber) {
        Bomb bombToPickup = null;

        // Chercher une bombe à ramasser
        for (Bomb bomb : activeBombs) {
            if (bomb.getX() == player.getX() && bomb.getY() == player.getY() &&
                    bomb.getOwner() == player && !bomb.isFlying() && !bomb.isMoving()) {
                bombToPickup = bomb;
                break;
            }
        }

        if (bombToPickup != null) {
            bombToPickup.stopCountdown();

            if (player.pickUpBomb(bombToPickup)) {
                activeBombs.remove(bombToPickup);
                kickingBombs.remove(bombToPickup);

                // Décrémenter le bon compteur
                if (playerNumber == 1) {
                    currentBombCountPlayer1--;
                } else {
                    currentBombCountPlayer2--;
                }

                removeBombVisual(bombToPickup);
                System.out.println("Joueur " + playerNumber + ": Bombe ramassée !");
            }
        } else {
            System.out.println("Joueur " + playerNumber + ": Aucune bombe à ramasser !");
        }
    }

    private void throwHeldBomb(Player player, int playerNumber) {
        int[] direction = getLastDirection(playerNumber);
        Bomb thrownBomb = player.throwHeldBomb(direction[0], direction[1]);

        if (thrownBomb != null) {
            thrownBomb.setPosition(player.getX(), player.getY());
            activeBombs.add(thrownBomb);
            flyingBombs.add(thrownBomb);

            // Incrémenter le bon compteur
            if (playerNumber == 1) {
                currentBombCountPlayer1++;
            } else {
                currentBombCountPlayer2++;
            }

            thrownBomb.throwBomb(direction[0], direction[1], () -> {
                // Logique gérée dans handleFlyingBombs()
            });

            thrownBomb.startCountdown(() -> {
                handleExplosion(thrownBomb);
                activeBombs.remove(thrownBomb);
                flyingBombs.remove(thrownBomb);
                kickingBombs.remove(thrownBomb);

                // Décrémenter le bon compteur
                if (playerNumber == 1) {
                    currentBombCountPlayer1--;
                } else {
                    currentBombCountPlayer2--;
                }

                System.out.println("Joueur " + playerNumber + ": Bombe lancée explosée !");
            });

            placeBombVisual(thrownBomb);
            System.out.println("Joueur " + playerNumber + ": Bombe lancée !");
        }
    }

    private void placeLineBombs(Player player, int playerNumber) {
        if (!player.hasLineBombs()) {
            System.out.println("Joueur " + playerNumber + ": Pas le pouvoir LineBomb !");
            return;
        }

        if (player.hasMalus(MalusType.NO_BOMB)) {
            System.out.println("Joueur " + playerNumber + ": Impossible de poser des bombes à cause du malus !");
            return;
        }

        int currentBombCount = (playerNumber == 1) ? currentBombCountPlayer1 : currentBombCountPlayer2;
        int bombsToPlace = player.getMaxBombs() - currentBombCount;

        if (bombsToPlace <= 0) {
            System.out.println("Joueur " + playerNumber + ": Limite de bombes atteinte !");
            return;
        }

        int[] direction = getLastDirection(playerNumber);
        int dirX = direction[0];
        int dirY = direction[1];

        // Direction par défaut si aucune détectée
        if (dirX == 0 && dirY == 0) {
            dirX = 0;
            dirY = 1;
        }

        System.out.println("Joueur " + playerNumber + ": LineBomb activé ! Pose de " + bombsToPlace + " bombes...");

        int bombsPlaced = 0;
        for (int i = 1; i <= bombsToPlace; i++) {
            int bombX = player.getX() + (dirX * i);
            int bombY = player.getY() + (dirY * i);

            if (!canPlaceBombAt(bombX, bombY)) {
                break;
            }

            Bomb bomb = new Bomb(bombX, bombY, 10, player.getExplosionRange());
            bomb.setOwner(player);

            placeBombVisual(bomb);
            activeBombs.add(bomb);

            // Incrémenter le bon compteur
            if (playerNumber == 1) {
                currentBombCountPlayer1++;
            } else {
                currentBombCountPlayer2++;
            }
            bombsPlaced++;

            if (player.hasRemoteDetonation()) {
                System.out.println("Joueur " + playerNumber + ": Bombe LineBomb en attente de détonation manuelle.");
            } else {
                bomb.startCountdown(() -> {
                    handleExplosion(bomb);
                    activeBombs.remove(bomb);
                    flyingBombs.remove(bomb);
                    kickingBombs.remove(bomb);

                    // Décrémenter le bon compteur
                    if (playerNumber == 1) {
                        currentBombCountPlayer1--;
                    } else {
                        currentBombCountPlayer2--;
                    }
                });
            }
        }

        System.out.println("Joueur " + playerNumber + ": LineBomb terminé ! " + bombsPlaced + " bombes posées.");
    }

    private void detonateRemoteBombs(Player player, int playerNumber) {
        if (!player.hasRemoteDetonation()) {
            System.out.println("Joueur " + playerNumber + ": Pas le pouvoir Remote !");
            return;
        }

        List<Bomb> bombsToDetonate = new ArrayList<>();
        for (Bomb bomb : activeBombs) {
            if (bomb.getOwner() == player && !bomb.isFlying() && !bomb.isMoving()) {
                bombsToDetonate.add(bomb);
            }
        }

        if (bombsToDetonate.isEmpty()) {
            System.out.println("Joueur " + playerNumber + ": Aucune bombe à faire exploser !");
            return;
        }

        System.out.println("Joueur " + playerNumber + ": Remote Power activé ! Explosion de " + bombsToDetonate.size() + " bombe(s) !");

        for (Bomb bomb : bombsToDetonate) {
            handleExplosion(bomb);
            activeBombs.remove(bomb);
            flyingBombs.remove(bomb);
            kickingBombs.remove(bomb);

            // Décrémenter le bon compteur
            if (playerNumber == 1) {
                currentBombCountPlayer1--;
            } else {
                currentBombCountPlayer2--;
            }
        }
    }

    private boolean canPlaceBombAt(int x, int y) {
        if (x < 0 || y < 0 || y >= map.length || x >= map[0].length) {
            return false;
        }

        Tile tile = map[y][x];
        if (tile.getType() == TileType.WALL || tile.getType() == TileType.WALL_BREAKABLE) {
            return false;
        }

        // Vérifier s'il y a déjà une bombe
        for (Bomb existingBomb : activeBombs) {
            if (existingBomb.getX() == x && existingBomb.getY() == y) {
                return false;
            }
        }

        // Vérifier s'il y a des entités
        if ((player1.getX() == x && player1.getY() == y) ||
                (!isOnePlayer && player2 != null && player2.getX() == x && player2.getY() == y) ||
                (enemy != null && enemy.getX() == x && enemy.getY() == y)) {
            return false;
        }

        return true;
    }

    private void handleFlyingBombs() {
        List<Bomb> bombsToStop = new ArrayList<>();

        for (Bomb bomb : flyingBombs) {
            if (!bomb.isFlying()) continue;

            int[] nextPos = bomb.getNextPosition();
            int newX = nextPos[0];
            int newY = nextPos[1];

            // Vérifier collision
            if (newX < 0 || newX >= map[0].length || newY < 0 || newY >= map.length ||
                    map[newY][newX].getType() == TileType.WALL ||
                    map[newY][newX].getType() == TileType.WALL_BREAKABLE) {

                bomb.stopFlying();
                bombsToStop.add(bomb);
            } else {
                bomb.moveToNextPosition();
                updateBombVisual(bomb);
            }
        }

        flyingBombs.removeAll(bombsToStop);
    }

    private void handleKickingBombs() {
        List<Bomb> bombsToStop = new ArrayList<>();

        for (Bomb bomb : kickingBombs) {
            if (!bomb.isMoving()) continue;

            int[] nextPos = bomb.getNextKickPosition();
            int newX = nextPos[0];
            int newY = nextPos[1];

            if (!canBombMoveTo(newX, newY)) {
                bomb.stopMoving();
                bombsToStop.add(bomb);
            } else {
                bomb.moveToNextKickPosition();
                updateBombVisual(bomb);
            }
        }

        kickingBombs.removeAll(bombsToStop);
    }

    private boolean canBombMoveTo(int x, int y) {
        if (x < 0 || y < 0 || y >= map.length || x >= map[0].length) {
            return false;
        }

        Tile tile = map[y][x];
        if (tile.getType() == TileType.WALL || tile.getType() == TileType.WALL_BREAKABLE) {
            return false;
        }

        // Vérifier les autres bombes immobiles
        for (Bomb otherBomb : activeBombs) {
            if (otherBomb.getX() == x && otherBomb.getY() == y &&
                    !otherBomb.isFlying() && !otherBomb.isMoving()) {
                return false;
            }
        }

        // Vérifier les joueurs
        if ((player1.getX() == x && player1.getY() == y) ||
                (!isOnePlayer && player2 != null && player2.getX() == x && player2.getY() == y) ||
                (enemy != null && enemy.getX() == x && enemy.getY() == y)) {
            return false;
        }

        return true;
    }

    private boolean tryKickBomb(Bomb bomb, int directionX, int directionY) {
        if (Math.abs(directionX) + Math.abs(directionY) != 1) {
            return false;
        }

        int newX = bomb.getX() + directionX;
        int newY = bomb.getY() + directionY;

        if (!canBombMoveTo(newX, newY)) {
            return false;
        }

        kickingBombs.add(bomb);
        bomb.kickBomb(directionX, directionY, () -> {
            // Logique gérée dans handleKickingBombs()
        });

        return true;
    }

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
            if ((entity == player1 && player1.canPassThroughWalls()) ||
                    (!isOnePlayer && entity == player2 && player2 != null && player2.canPassThroughWalls())) {
                return true;
            } else {
                return false;
            }
        }

        // Vérifier les bombes
        for (Bomb bomb : activeBombs) {
            if (bomb.getX() == x && bomb.getY() == y) {
                // Vérifier Kick Power
                if (((entity == player1 && player1.canKickBombs()) ||
                        (!isOnePlayer && entity == player2 && player2 != null && player2.canKickBombs()))
                        && !bomb.isFlying() && !bomb.isMoving()) {

                    Player currentPlayer = (Player) entity;
                    int kickDirX = x - currentPlayer.getX();
                    int kickDirY = y - currentPlayer.getY();

                    if (tryKickBomb(bomb, kickDirX, kickDirY)) {
                        return true;
                    }
                }

                // Vérifier BombPass
                if (((entity == player1 && player1.canPassThroughBombs() && bomb.getOwner() == player1) ||
                        (!isOnePlayer && entity == player2 && player2 != null && player2.canPassThroughBombs() && bomb.getOwner() == player2))) {
                    continue;
                }

                return false;
            }
        }

        return true;
    }

    private void updateBombVisual(Bomb bomb) {
        removeBombVisual(bomb.getPreviousX(), bomb.getPreviousY());
        placeBombVisual(bomb);
    }

    private void removeBombVisual(int x, int y) {
        StackPane cell = (StackPane) getNodeFromGridPane(gameGrid, x, y);
        if (cell != null && cell.getChildren().size() > 1) {
            cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
        }
    }

    private void removeBombVisual(Bomb bomb) {
        removeBombVisual(bomb.getX(), bomb.getY());
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
        checkPlayerOnPowerUp(player);
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

    public void moveEnemy(Enemy enemy) {
        int currentX = enemy.getX();
        int currentY = enemy.getY();

        int newX = currentX + enemyCurrDirection[0];
        int newY = currentY + enemyCurrDirection[1];

        if (canMoveTo(newX, newY, enemy)) {
            enemy.setPosition(newX, newY);
            updateEnemyPosition(enemy);
        } else {
            // Changer de direction
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
                updateEnemyPosition(enemy);
            }
        }
    }

    private void handleExplosion(Bomb bomb) {
        int x = bomb.getX();
        int y = bomb.getY();
        int range = bomb.getRange();

        destroyTile(x, y);

        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};

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

                // 25% de chance de générer un power-up
                if (Math.random() < 0.25) {
                    try {
                        PowerUpType type = PowerUpType.randomType();
                        PowerUp powerUp = PowerUpFactory.create(type, x, y);

                        if (powerUp != null) {
                            activePowerUps.add(powerUp);
                            placePowerUpVisual(powerUp);
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur lors de la création du power-up: " + e.getMessage());
                    }
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

    private boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && y < map.length && x < map[0].length;
    }

    private void redrawTile(int x, int y) {
        StackPane cell = (StackPane) getNodeFromGridPane(gameGrid, x, y);
        if (cell != null) {
            boolean hasPowerUp = activePowerUps.stream()
                    .anyMatch(powerUp -> powerUp.getX() == x && powerUp.getY() == y);

            if (!hasPowerUp && cell.getChildren().size() > 1) {
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
            int playerNumber = (player == player1) ? 1 : 2;
            System.out.println("Joueur " + playerNumber + ": Power-up collecté: " + toCollect.getType());

            applyPowerUpEffect(player, toCollect, playerNumber);
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

    private void applyPowerUpEffect(Player player, PowerUp powerUp, int playerNumber) {
        powerUp.applyTo(player);

        switch (powerUp.getType()) {
            case RANGE_UP -> System.out.println("Joueur " + playerNumber + ": Range augmentée! (" + player.getExplosionRange() + ")");
            case BOMB_UP -> System.out.println("Joueur " + playerNumber + ": Bombes max augmentées! (" + player.getMaxBombs() + ")");
            case SPEED_UP -> System.out.println("Joueur " + playerNumber + ": Vitesse augmentée! (" + player.getSpeed() + ")");
            case GLOVE -> System.out.println("Joueur " + playerNumber + ": Glove activé! (" +
                    (playerNumber == 1 ? "SHIFT" : "CTRL") + " pour ramasser/lancer)");
            case KICK -> System.out.println("Joueur " + playerNumber + ": Kick activé! (marcher contre une bombe)");
            case LINE_BOMB -> System.out.println("Joueur " + playerNumber + ": LineBomb activé! (" +
                    (playerNumber == 1 ? "L" : "K") + ")");
            case REMOTE -> System.out.println("Joueur " + playerNumber + ": Remote activé! (" +
                    (playerNumber == 1 ? "R" : "O") + ")");
            case SKULL -> System.out.println("Joueur " + playerNumber + ": MALUS SKULL!");
            case BOMB_PASS -> System.out.println("Joueur " + playerNumber + ": BombPass activé!");
            case WALL_PASS -> System.out.println("Joueur " + playerNumber + ": WallPass activé!");
        }
    }

    public void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    @FXML
    private void handleBackToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent menuRoot = loader.load();
            Scene menuScene = new Scene(menuRoot);
            Stage stage = (Stage) gameGrid.getScene().getWindow();
            stage.setScene(menuScene);
            stage.setTitle("Super Bomberman - Menu");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}