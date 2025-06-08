package com.superbomberman.controller;

import com.superbomberman.model.*;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.animation.AnimationTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    /** Compteur du nombre de bombes actuellement posées par le joueur. */
    private int currentBombCount = 0;

    /** Ensemble des touches actuellement pressées */
    private Set<javafx.scene.input.KeyCode> pressedKeys = new HashSet<>();

    /** Timer pour le mouvement continu */
    private AnimationTimer gameLoop;

    /** Timestamp du dernier mouvement du joueur */
    private long lastPlayerMoveTime = 0;

    /** Timestamp du dernier mouvement de l'ennemi */
    private long lastEnemyMoveTime = 0;

    /** Délai entre les mouvements en nanosecondes (basé sur la vitesse) */
    private static final long BASE_MOVE_DELAY = 200_000_000L; // 200ms de base

    /** Délai entre les mouvements de l'ennemi en nanosecondes */
    private static final long ENEMY_MOVE_DELAY = 500_000_000L; // 500ms entre chaque mouvement

    /** Dernière direction de mouvement du joueur pour le lancer */
    private int lastPlayerDirectionX = 0;
    private int lastPlayerDirectionY = 1; // Par défaut vers le bas

    /** Liste des bombes en vol */
    private List<Bomb> flyingBombs = new ArrayList<>();

    /** ⭐ NOUVEAU : Liste des bombes en mouvement par coup de pied */
    private List<Bomb> kickingBombs = new ArrayList<>();

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

        // Nouvelle gestion des événements clavier
        gameGrid.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());

            // Gestion immédiate pour la pose de bombes
            if (event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                placeBomb();
            }

            // Gestion du ramassage/lancer de bombes avec la touche SHIFT
            if (event.getCode() == javafx.scene.input.KeyCode.SHIFT) {
                handleBombPickupOrThrow();
            }

            // Gestion du LineBomb avec la touche ENTER
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                placeLineBombs();
            }

            // ⭐ NOUVEAU : Gestion du Remote Power avec la touche R
            if (event.getCode() == javafx.scene.input.KeyCode.R) {
                detonateRemoteBombs();
            }
        });

        gameGrid.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
        });

        // Démarrer la boucle de jeu
        startGameLoop();

        gameGrid.setFocusTraversable(true);
        gameGrid.requestFocus();
    }

    /**
     * Démarre la boucle de jeu principale pour le mouvement continu.
     */
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastAutoBombTime = 0;
            private final long AUTO_BOMB_INTERVAL = 2_000_000_000L; // 2 secondes

            @Override
            public void handle(long now) {
                handlePlayerMovement(now);
                handleEnemyMovement(now);
                handleFlyingBombs(); // Gérer les bombes en vol (Glove)
                handleKickingBombs(); // ⭐ NOUVEAU : Gérer les bombes qui glissent (Kick)

                // Gérer le malus AUTO_BOMB
                if (player1.hasMalus(MalusType.AUTO_BOMB) &&
                        now - lastAutoBombTime >= AUTO_BOMB_INTERVAL) {
                    placeBomb();
                    lastAutoBombTime = now;
                }
            }
        };
        gameLoop.start();
    }

    /**
     * Gère le mouvement du joueur basé sur la vitesse.
     */
    private void handlePlayerMovement(long currentTime) {
        // Mettre à jour les malus du joueur
        player1.updateMalus();

        // Calculer le délai basé sur la vitesse du joueur
        long moveDelay = (long) (BASE_MOVE_DELAY / player1.getSpeed());

        // Vérifier si assez de temps s'est écoulé depuis le dernier mouvement
        if (currentTime - lastPlayerMoveTime < moveDelay) {
            return;
        }

        int pNewX = player1.getX();
        int pNewY = player1.getY();
        boolean moved = false;

        // Gérer les contrôles inversés
        boolean reversed = player1.hasMalus(MalusType.REVERSED_CONTROLS);

        // Vérifier les touches pressées et calculer le nouveau mouvement
        if (pressedKeys.contains(javafx.scene.input.KeyCode.LEFT)) {
            pNewX += reversed ? 1 : -1; // Inverser si malus actif
            lastPlayerDirectionX = reversed ? 1 : -1;
            lastPlayerDirectionY = 0;
            moved = true;
        } else if (pressedKeys.contains(javafx.scene.input.KeyCode.RIGHT)) {
            pNewX += reversed ? -1 : 1; // Inverser si malus actif
            lastPlayerDirectionX = reversed ? -1 : 1;
            lastPlayerDirectionY = 0;
            moved = true;
        } else if (pressedKeys.contains(javafx.scene.input.KeyCode.UP)) {
            pNewY += reversed ? 1 : -1; // Inverser si malus actif
            lastPlayerDirectionX = 0;
            lastPlayerDirectionY = reversed ? 1 : -1;
            moved = true;
        } else if (pressedKeys.contains(javafx.scene.input.KeyCode.DOWN)) {
            pNewY += reversed ? -1 : 1; // Inverser si malus actif
            lastPlayerDirectionX = 0;
            lastPlayerDirectionY = reversed ? -1 : 1;
            moved = true;
        }

        // Effectuer le mouvement si possible
        if (moved && canMoveTo(pNewX, pNewY, player1)) {
            player1.setPosition(pNewX, pNewY);
            updatePlayerPosition(player1);
            lastPlayerMoveTime = currentTime;
        }
    }

    /**
     * Gère le mouvement de l'ennemi avec contrôle du timing.
     */
    private void handleEnemyMovement(long currentTime) {
        // Vérifier si assez de temps s'est écoulé depuis le dernier mouvement de l'ennemi
        if (currentTime - lastEnemyMoveTime < ENEMY_MOVE_DELAY) {
            return;
        }

        moveEnemy(enemy);
        lastEnemyMoveTime = currentTime;
    }

    /**
     * Gère le ramassage ou le lancer de bombes selon l'état du joueur.
     */
    private void handleBombPickupOrThrow() {
        if (!player1.canThrowBombs()) {
            System.out.println("Le joueur n'a pas le pouvoir Glove !");
            return;
        }

        if (player1.isHoldingBomb()) {
            // Le joueur tient une bombe, on la lance
            throwHeldBomb();
        } else {
            // Le joueur ne tient pas de bombe, on essaie d'en ramasser une
            tryPickupBomb();
        }
    }

    /**
     * Essaie de ramasser une bombe à la position du joueur.
     */
    private void tryPickupBomb() {
        Bomb bombToPickup = null;

        // Chercher une bombe à la position du joueur
        for (Bomb bomb : activeBombs) {
            if (bomb.getX() == player1.getX() && bomb.getY() == player1.getY()
                    && bomb.getOwner() == player1 && !bomb.isFlying() && !bomb.isMoving()) {
                bombToPickup = bomb;
                break;
            }
        }

        if (bombToPickup != null) {
            // ⭐ ARRÊTER LE TIMER D'EXPLOSION AVANT DE RAMASSER ⭐
            bombToPickup.stopCountdown();

            // Ramasser la bombe
            if (player1.pickUpBomb(bombToPickup)) {
                // Retirer la bombe de la liste des bombes actives
                activeBombs.remove(bombToPickup);
                kickingBombs.remove(bombToPickup); // Au cas où elle glissait
                currentBombCount--; // Réduire le compteur

                // Retirer visuellement la bombe
                removeBombVisual(bombToPickup);
                System.out.println("Bombe ramassée ! Bombes restantes: " + currentBombCount);
            }
        } else {
            System.out.println("Aucune bombe à ramasser ici !");
        }
    }

    /**
     * Lance la bombe tenue par le joueur.
     */
    private void throwHeldBomb() {
        Bomb thrownBomb = player1.throwHeldBomb(lastPlayerDirectionX, lastPlayerDirectionY);

        if (thrownBomb != null) {
            // Positionner la bombe à la position du joueur
            thrownBomb.setPosition(player1.getX(), player1.getY());

            // Ajouter la bombe aux listes
            activeBombs.add(thrownBomb);
            flyingBombs.add(thrownBomb);
            currentBombCount++; // Réaugmenter le compteur

            // Démarrer le vol de la bombe (le callback est géré automatiquement dans handleFlyingBombs)
            thrownBomb.throwBomb(lastPlayerDirectionX, lastPlayerDirectionY, () -> {
                // Ce callback n'est plus utilisé, la logique est dans handleFlyingBombs()
            });

            // Redémarrer le timer d'explosion après le lancer
            thrownBomb.startCountdown(() -> {
                handleExplosion(thrownBomb);
                activeBombs.remove(thrownBomb);
                flyingBombs.remove(thrownBomb); // Au cas où elle était encore en vol
                kickingBombs.remove(thrownBomb); // ⭐ NOUVEAU : Au cas où elle glissait
                currentBombCount--;
                System.out.println("Bombe lancée explosée ! Bombes restantes : " + currentBombCount + "/" + player1.getMaxBombs());
            });

            // Afficher la bombe
            placeBombVisual(thrownBomb);

            System.out.println("Bombe lancée ! Direction: (" + lastPlayerDirectionX + ", " + lastPlayerDirectionY + ")");
        }
    }

    /**
     * ⭐ NOUVEAU : Place des bombes en ligne droite (LineBomb Power).
     */
    private void placeLineBombs() {
        // Vérifier si le joueur a le pouvoir LineBomb
        if (!player1.hasLineBombs()) {
            System.out.println("Le joueur n'a pas le pouvoir LineBomb !");
            return;
        }

        // Vérifier le malus NO_BOMB
        if (player1.hasMalus(MalusType.NO_BOMB)) {
            System.out.println("Impossible de poser des bombes à cause du malus!");
            return;
        }

        // Calculer combien de bombes on peut poser
        int bombsToPlace = player1.getMaxBombs() - currentBombCount;
        if (bombsToPlace <= 0) {
            System.out.println("Limite de bombes atteinte ! Impossible d'utiliser LineBomb.");
            return;
        }

        System.out.println("LineBomb activé ! Pose de " + bombsToPlace + " bombes en ligne...");

        // Utiliser la dernière direction de mouvement du joueur
        int dirX = lastPlayerDirectionX;
        int dirY = lastPlayerDirectionY;

        // Si aucune direction définie, utiliser vers le bas par défaut
        if (dirX == 0 && dirY == 0) {
            dirX = 0;
            dirY = 1;
            System.out.println("Aucune direction détectée, LineBomb vers le bas par défaut");
        }

        int bombsPlaced = 0;
        int startX = player1.getX();
        int startY = player1.getY();

        // Poser les bombes en ligne
        for (int i = 1; i <= bombsToPlace; i++) {
            int bombX = startX + (dirX * i);
            int bombY = startY + (dirY * i);

            // Vérifier si la position est valide pour poser une bombe
            if (!canPlaceBombAt(bombX, bombY)) {
                System.out.println("LineBomb arrêté en (" + bombX + ", " + bombY + ") - obstacle détecté");
                break;
            }

            // Créer et poser la bombe
            Bomb bomb = new Bomb(bombX, bombY, 10, player1.getExplosionRange());
            bomb.setOwner(player1);

            placeBombVisual(bomb);
            activeBombs.add(bomb);
            currentBombCount++;
            bombsPlaced++;

            // ⭐ MODIFIÉ : Gérer le Remote Power pour LineBomb aussi
            if (player1.hasRemoteDetonation()) {
                System.out.println("Bombe LineBomb en attente de détonation manuelle (Remote Power).");
            } else {
                // Démarrer le timer d'explosion
                bomb.startCountdown(() -> {
                    handleExplosion(bomb);
                    activeBombs.remove(bomb);
                    flyingBombs.remove(bomb);
                    kickingBombs.remove(bomb);
                    currentBombCount--;
                    System.out.println("Bombe LineBomb explosée. Bombes restantes : " + currentBombCount + "/" + player1.getMaxBombs());
                });
            }

            System.out.println("Bombe LineBomb posée en (" + bombX + ", " + bombY + ")");
        }

        System.out.println("LineBomb terminé ! " + bombsPlaced + " bombes posées en direction (" + dirX + ", " + dirY + ")");
    }

    /**
     * ⭐ NOUVEAU : Fait exploser toutes les bombes en attente (Remote Power).
     */
    private void detonateRemoteBombs() {
        // Vérifier si le joueur a le pouvoir Remote
        if (!player1.hasRemoteDetonation()) {
            System.out.println("Le joueur n'a pas le pouvoir Remote !");
            return;
        }

        // Trouver toutes les bombes du joueur qui n'ont pas de timer actif
        List<Bomb> bombsToDetonate = new ArrayList<>();
        for (Bomb bomb : activeBombs) {
            if (bomb.getOwner() == player1 && !bomb.isFlying() && !bomb.isMoving()) {
                bombsToDetonate.add(bomb);
            }
        }

        if (bombsToDetonate.isEmpty()) {
            System.out.println("Aucune bombe à faire exploser !");
            return;
        }

        System.out.println("Remote Power activé ! Explosion de " + bombsToDetonate.size() + " bombe(s) !");

        // Faire exploser toutes les bombes du joueur
        for (Bomb bomb : bombsToDetonate) {
            handleExplosion(bomb);
            activeBombs.remove(bomb);
            flyingBombs.remove(bomb);
            kickingBombs.remove(bomb);
            currentBombCount--;
            System.out.println("Bombe Remote explosée ! Bombes restantes : " + currentBombCount + "/" + player1.getMaxBombs());
        }
    }

    /**
     * ⭐ MODIFIÉ : Vérifie si on peut poser une bombe à une position donnée.
     *
     * @param x Coordonnée X
     * @param y Coordonnée Y
     * @return true si on peut poser une bombe, false sinon
     */
    private boolean canPlaceBombAt(int x, int y) {
        // Vérifier les limites de la carte
        if (x < 0 || y < 0 || y >= map.length || x >= map[0].length) {
            return false;
        }

        // ⭐ MODIFIÉ : Vérifier les murs - on ne peut JAMAIS poser de bombes dans les murs
        // Même avec WallPass, on ne peut pas poser de bombes à l'intérieur des murs destructibles
        Tile tile = map[y][x];
        if (tile.getType() == TileType.WALL || tile.getType() == TileType.WALL_BREAKABLE) {
            System.out.println("Impossible de poser une bombe dans un mur (même avec WallPass)");
            return false;
        }

        // Vérifier s'il y a déjà une bombe à cette position
        for (Bomb existingBomb : activeBombs) {
            if (existingBomb.getX() == x && existingBomb.getY() == y) {
                return false;
            }
        }

        // Vérifier s'il y a des joueurs à cette position (optionnel)
        if ((player1.getX() == x && player1.getY() == y) ||
                (enemy.getX() == x && enemy.getY() == y)) {
            return false;
        }

        return true;
    }

    /**
     * Gère le mouvement des bombes en vol.
     */
    private void handleFlyingBombs() {
        List<Bomb> bombsToStop = new ArrayList<>();

        for (Bomb bomb : flyingBombs) {
            if (!bomb.isFlying()) continue;

            // Calculer la prochaine position
            int[] nextPos = bomb.getNextPosition();
            int newX = nextPos[0];
            int newY = nextPos[1];

            // Vérifier la collision avec les limites ou obstacles
            if (newX < 0 || newX >= map[0].length || newY < 0 || newY >= map.length ||
                    map[newY][newX].getType() == TileType.WALL ||
                    map[newY][newX].getType() == TileType.WALL_BREAKABLE) {

                // Collision détectée, arrêter la bombe
                bomb.stopFlying();
                bombsToStop.add(bomb);

                System.out.println("Bombe arrêtée par collision en (" + bomb.getX() + ", " + bomb.getY() + ")");
            } else {
                // Pas de collision, déplacer la bombe
                bomb.moveToNextPosition();

                // Mettre à jour l'affichage
                updateBombVisual(bomb);
            }
        }

        // Retirer les bombes arrêtées de la liste de vol
        flyingBombs.removeAll(bombsToStop);
    }

    /**
     * ⭐ NOUVEAU : Essaie de donner un coup de pied à une bombe.
     *
     * @param bomb La bombe à kicker
     * @param directionX Direction du coup de pied X
     * @param directionY Direction du coup de pied Y
     * @return true si le coup de pied a réussi, false sinon
     */
    private boolean tryKickBomb(Bomb bomb, int directionX, int directionY) {
        // Vérifier que la direction est valide (pas diagonale)
        if (Math.abs(directionX) + Math.abs(directionY) != 1) {
            return false;
        }

        // Calculer la position où la bombe va aller
        int newX = bomb.getX() + directionX;
        int newY = bomb.getY() + directionY;

        // Vérifier que la destination est libre
        if (!canBombMoveTo(newX, newY)) {
            System.out.println("Impossible de kicker la bombe : destination bloquée");
            return false;
        }

        // Démarrer le glissement de la bombe
        kickingBombs.add(bomb);
        bomb.kickBomb(directionX, directionY, () -> {
            // Ce callback n'est plus utilisé, la logique est dans handleKickingBombs()
        });

        return true;
    }

    /**
     * ⭐ NOUVEAU : Vérifie si une bombe peut se déplacer vers une position.
     *
     * @param x Coordonnée X
     * @param y Coordonnée Y
     * @return true si la bombe peut y aller, false sinon
     */
    private boolean canBombMoveTo(int x, int y) {
        // Vérifier les limites de la carte
        if (x < 0 || y < 0 || y >= map.length || x >= map[0].length) {
            return false;
        }

        // Vérifier les murs
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

        // Vérifier les joueurs (optionnel : la bombe s'arrête si elle touche un joueur)
        if ((player1.getX() == x && player1.getY() == y) ||
                (enemy.getX() == x && enemy.getY() == y)) {
            return false;
        }

        return true;
    }

    /**
     * ⭐ NOUVEAU : Gère le mouvement des bombes qui glissent suite à un coup de pied.
     */
    private void handleKickingBombs() {
        List<Bomb> bombsToStop = new ArrayList<>();

        for (Bomb bomb : kickingBombs) {
            if (!bomb.isMoving()) continue;

            // Calculer la prochaine position
            int[] nextPos = bomb.getNextKickPosition();
            int newX = nextPos[0];
            int newY = nextPos[1];

            // Vérifier la collision
            if (!canBombMoveTo(newX, newY)) {
                // Collision détectée, arrêter la bombe
                bomb.stopMoving();
                bombsToStop.add(bomb);

                System.out.println("Bombe arrêtée par collision en (" + bomb.getX() + ", " + bomb.getY() + ")");
            } else {
                // Pas de collision, déplacer la bombe
                bomb.moveToNextKickPosition();

                // Mettre à jour l'affichage
                updateBombVisual(bomb);
            }
        }

        // Retirer les bombes arrêtées de la liste de glissement
        kickingBombs.removeAll(bombsToStop);
    }

    /**
     * Met à jour l'affichage d'une bombe en mouvement.
     */
    private void updateBombVisual(Bomb bomb) {
        // Retirer la bombe de sa position précédente
        removeBombVisual(bomb.getPreviousX(), bomb.getPreviousY());

        // Afficher la bombe à sa nouvelle position
        placeBombVisual(bomb);
    }

    /**
     * Retire visuellement une bombe à une position spécifique.
     */
    private void removeBombVisual(int x, int y) {
        StackPane cell = (StackPane) getNodeFromGridPane(gameGrid, x, y);
        if (cell != null && cell.getChildren().size() > 1) {
            cell.getChildren().removeIf(node -> cell.getChildren().indexOf(node) > 0);
        }
    }

    /**
     * Retire visuellement une bombe.
     */
    private void removeBombVisual(Bomb bomb) {
        removeBombVisual(bomb.getX(), bomb.getY());
    }

    /**
     * Gère la pose d'une bombe.
     */
    private void placeBomb() {
        // Vérifier le malus NO_BOMB
        if (player1.hasMalus(MalusType.NO_BOMB)) {
            System.out.println("Impossible de poser une bombe à cause du malus!");
            return;
        }

        // ⭐ NOUVEAU : Vérifier que le joueur n'est pas dans un mur destructible
        if (map[player1.getY()][player1.getX()].getType() == TileType.WALL_BREAKABLE) {
            System.out.println("Impossible de poser une bombe à l'intérieur d'un mur destructible!");
            return;
        }

        if (currentBombCount < player1.getMaxBombs()) {
            System.out.println("Pose d'une bombe (" + (currentBombCount + 1) + "/" + player1.getMaxBombs() + ")");
            System.out.println("Range : " + player1.getExplosionRange());

            // Créer la bombe et lui assigner le joueur qui l'a posée
            Bomb bomb = new Bomb(player1.getX(), player1.getY(), 10, player1.getExplosionRange());
            bomb.setOwner(player1); // Assigner le propriétaire de la bombe
            System.out.println("Range Bomb : " + bomb.getRange());

            placeBombVisual(bomb);
            activeBombs.add(bomb);
            currentBombCount++;

            // ⭐ NOUVEAU : Si le joueur a Remote Power, ne pas démarrer le timer automatique
            if (player1.hasRemoteDetonation()) {
                System.out.println("Remote Power activé ! Bombe en attente de détonation manuelle.");
            } else {
                // Timer normal pour les bombes sans Remote Power
                bomb.startCountdown(() -> {
                    handleExplosion(bomb);
                    activeBombs.remove(bomb);
                    flyingBombs.remove(bomb); // Au cas où elle était en vol
                    kickingBombs.remove(bomb); // ⭐ NOUVEAU : Au cas où elle glissait
                    currentBombCount--;
                    System.out.println("Bombe explosée. Bombes restantes : " + currentBombCount + "/" + player1.getMaxBombs());
                });
            }
        } else {
            System.out.println("Limite de bombes atteinte (" + player1.getMaxBombs() + ")");
        }
    }

    /**
     * Arrête la boucle de jeu (à appeler lors de la fermeture).
     */
    public void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
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
     * ⭐ MODIFIÉ : Indique si une case est accessible et gère les coups de pied de bombes.
     *
     * @param x Coordonnée X.
     * @param y Coordonnée Y.
     * @param entity L'entité qui veut se déplacer (player1 ou enemy)
     * @return true si déplacement possible, false sinon.
     */
    private boolean canMoveTo(int x, int y, Object entity) {
        if (x < 0 || y < 0 || y >= map.length || x >= map[0].length) {
            return false;
        }

        Tile tile = map[y][x];

        // ⭐ MODIFIÉ : Gestion du WallPass pour les murs destructibles
        if (tile.getType() == TileType.WALL) {
            return false; // Murs indestructibles bloquent toujours
        }

        if (tile.getType() == TileType.WALL_BREAKABLE) {
            // Si c'est le joueur ET qu'il a WallPass, il peut traverser les murs destructibles
            if (entity == player1 && player1.canPassThroughWalls()) {
                System.out.println("WallPass activé ! Le joueur traverse le mur destructible.");
                return true;
            } else {
                return false; // Sinon (ennemi ou joueur sans WallPass), mur destructible bloque
            }
        }

        // Vérifier les collisions avec les bombes
        for (Bomb bomb : activeBombs) {
            if (bomb.getX() == x && bomb.getY() == y) {

                // ⭐ PRIORITÉ 1 : Si c'est le joueur avec Kick Power et que la bombe est immobile, donner un coup de pied
                if (entity == player1 && player1.canKickBombs() && !bomb.isFlying() && !bomb.isMoving()) {
                    int kickDirX = x - player1.getX();
                    int kickDirY = y - player1.getY();

                    if (tryKickBomb(bomb, kickDirX, kickDirY)) {
                        System.out.println("Coup de pied donné à la bombe !");
                        return true; // Le joueur peut se déplacer, la bombe a été kickée
                    }
                }

                // ⭐ PRIORITÉ 2 : Si c'est le joueur avec BombPass et que c'est sa propre bombe, il peut passer
                if (entity == player1 && player1.canPassThroughBombs() && bomb.getOwner() == player1) {
                    System.out.println("BombPass activé ! Le joueur traverse sa propre bombe.");
                    continue; // Pas de collision avec ses propres bombes
                }

                // ⭐ PRIORITÉ 3 : Sinon, collision normale
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
        System.out.println("Destruction de la tuile en (" + x + ", " + y + ") de type: " + tile.getType());

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
                System.out.println("Mur cassable détruit en (" + x + ", " + y + ")");
                map[y][x] = new Tile(TileType.FLOOR);

                double random = Math.random();
                System.out.println("Random généré: " + random);
                if (random < 0.25) { // 25% de chance
                    try {
                        PowerUpType type = PowerUpType.randomType();
                        System.out.println("Type de power-up généré: " + type);
                        PowerUp powerUp = PowerUpFactory.create(type, x, y);
                        System.out.println("Power-up créé: " + powerUp);

                        if (powerUp != null) {
                            activePowerUps.add(powerUp);
                            placePowerUpVisual(powerUp);
                            System.out.println("Power-up ajouté et affiché en (" + x + ", " + y + ")");
                            System.out.println("Nombre total de power-ups actifs: " + activePowerUps.size());
                        } else {
                            System.out.println("ERREUR: PowerUp créé est null!");
                        }
                    } catch (Exception e) {
                        System.out.println("ERREUR lors de la création du power-up: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Pas de power-up généré (probabilité)");
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
        return x >= 0 && y >= 0 && y < map.length && x < map[0].length;
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
            // Ne pas supprimer le power-up s'il y en a un
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

    /**
     * ⭐ MODIFIÉ : Déplace l'ennemi selon la direction courante ou choisit une nouvelle direction valide.
     *
     * @param enemy L'ennemi à déplacer.
     */
    public void moveEnemy(Enemy enemy) {
        int currentX = enemy.getX();
        int currentY = enemy.getY();

        int newX = currentX + enemyCurrDirection[0];
        int newY = currentY + enemyCurrDirection[1];

        if (canMoveTo(newX, newY, enemy)) {
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

            if (canMoveTo(newX, newY, enemy)) {
                enemy.setPosition(newX, newY);
                updateEnemyPosition(enemy);
            }
        }
    }

    private void placePowerUpVisual(PowerUp powerUp) {
        System.out.println("Tentative d'affichage du power-up en (" + powerUp.getX() + ", " + powerUp.getY() + ")");
        StackPane cell = (StackPane) getNodeFromGridPane(gameGrid, powerUp.getX(), powerUp.getY());
        if (cell != null) {
            Rectangle powerUpRect = new Rectangle(40, 40);
            powerUpRect.setFill(powerUpPattern);
            cell.getChildren().add(powerUpRect);
            System.out.println("Power-up affiché avec succès!");
        } else {
            System.out.println("ERREUR: Cellule introuvable pour afficher le power-up!");
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
            System.out.println("Power-up collecté: " + toCollect.getType());
            applyPowerUpEffect(player, toCollect);
            removePowerUpVisual(toCollect);
            activePowerUps.remove(toCollect);
            System.out.println("Power-ups restants: " + activePowerUps.size());
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
        // Utiliser la méthode applyTo du power-up pour appliquer l'effet
        powerUp.applyTo(player);

        // Messages de débogage selon le type de power-up
        switch (powerUp.getType()) {
            case RANGE_UP -> System.out.println("Range augmentée! Nouvelle range: " + player.getExplosionRange());
            case BOMB_UP -> System.out.println("Nombre de bombes augmenté! Nouvelles bombes max: " + player.getMaxBombs());
            case SPEED_UP -> {
                System.out.println("Vitesse augmentée! Nouvelle vitesse: " + player.getSpeed());
                System.out.println("Délai entre mouvements: " + (BASE_MOVE_DELAY / player.getSpeed() / 1_000_000) + "ms");
            }
            case GLOVE -> System.out.println("Pouvoir de lancer activé! Utilisez SHIFT pour ramasser/lancer des bombes!");
            case KICK -> System.out.println("Pouvoir de donner des coups de pied activé! Marchez contre une bombe pour la faire glisser!");
            case LINE_BOMB -> System.out.println("LineBomb activé! Utilisez ENTER pour poser des bombes en ligne!");
            case REMOTE -> System.out.println("Remote Power activé! Utilisez R pour faire exploser vos bombes à distance!");
            case SKULL -> System.out.println("MALUS SKULL ramassé! Un effet négatif a été appliqué!");
            case BOMB_PASS -> System.out.println("BOMB PASS activé! Le joueur peut maintenant traverser ses propres bombes!");
            case WALL_PASS -> System.out.println("WallPass activé! Le joueur peut maintenant traverser les murs destructibles!");
        }
    }
}