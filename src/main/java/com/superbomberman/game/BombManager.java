package com.superbomberman.game;

import com.superbomberman.model.*;
import com.superbomberman.model.powerup.MalusType;
import com.superbomberman.model.powerup.PowerUp;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.superbomberman.model.MapLoader.player1;
import static com.superbomberman.model.MapLoader.player2;
import static com.superbomberman.model.MapLoader.enemy;
import static com.superbomberman.controller.MenuController.isOnePlayer;

/**
 * Gestionnaire des bombes du jeu Super Bomberman.
 * <p>
 * Cette classe gère la pose, l'explosion, le ramassage, le lancer, le kick et la gestion visuelle des bombes
 * pour les joueurs et l'ennemi. Elle intègre aussi l'interaction avec les autres managers du jeu (rendu visuel, power-ups,
 * gestion d'état, score, et logique de jeu).
 * </p>
 *
 * @author Jules Fuselier
 * @version 1.1 - Fix affichage et explosion
 * @since 2025-06-08
 */
public class BombManager {
    /** Carte du jeu sous forme de matrice de tuiles. */
    private Tile[][] map;
    /** Liste des bombes actives posées sur la carte. */
    private List<Bomb> activeBombs = new ArrayList<>();
    /** Liste des bombes en vol (lancées). */
    private List<Bomb> flyingBombs = new ArrayList<>();
    /** Liste des bombes qui roulent (kick). */
    private List<Bomb> kickingBombs = new ArrayList<>();

    // Références vers les autres managers pour l'intégration
    private VisualRenderer visualRenderer;
    private PowerUpManager powerUpManager;
    private GameStateManager gameStateManager;
    private ScoreSystem scoreSystem;

    /** Référence vers la logique principale du jeu (pour notifier morts et explosions). */
    private GameLogic gameLogic;

    /** Nombre de bombes actuellement posées par le joueur 1. */
    private int currentBombCountPlayer1 = 0;
    /** Nombre de bombes actuellement posées par le joueur 2. */
    private int currentBombCountPlayer2 = 0;

    /**
     * Constructeur du BombManager.
     * @param map La carte du jeu
     */
    public BombManager(Tile[][] map) {
        this.map = map;
    }

    /**
     * Configure les références vers les autres managers.
     * @param visualRenderer Gestionnaire de rendu visuel.
     * @param powerUpManager Gestionnaire de power-ups.
     * @param gameStateManager Gestionnaire d'état de partie.
     */
    public void setManagers(VisualRenderer visualRenderer, PowerUpManager powerUpManager, GameStateManager gameStateManager) {
        this.visualRenderer = visualRenderer;
        this.powerUpManager = powerUpManager;
        this.gameStateManager = gameStateManager;
        if (gameStateManager != null) {
            this.scoreSystem = gameStateManager.getScoreSystem();
        }
    }

    /**
     * Configure la référence vers la logique de jeu.
     * @param gameLogic Logique principale du jeu
     */
    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    /**
     * Place une bombe pour un joueur (si autorisé).
     * @param player Le joueur qui pose la bombe
     * @param playerNumber Numéro du joueur (1 ou 2)
     */
    public void placeBomb(Player player, int playerNumber) {
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

            if (visualRenderer != null) {
                visualRenderer.placeBombVisual(bomb);
            }

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
                    explodeBomb(bomb, playerNumber);
                });
            }
        } else {
            System.out.println("Joueur " + playerNumber + ": Limite de bombes atteinte (" + player.getMaxBombs() + ")");
        }
    }

    /**
     * Gère l'explosion complète d'une bombe.
     * @param bomb La bombe à faire exploser
     * @param playerNumber Le numéro du joueur propriétaire
     */
    private void explodeBomb(Bomb bomb, int playerNumber) {
        // Supprimer la bombe visuellement
        if (visualRenderer != null) {
            visualRenderer.removeBombVisual(bomb);
        }

        // Gérer l'explosion avec le GameLogic si disponible
        handleExplosion(bomb);

        // Nettoyer les listes
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
                ((playerNumber == 1) ? currentBombCountPlayer1 : currentBombCountPlayer2) + "/" + bomb.getOwner().getMaxBombs());
    }

    /**
     * Gère le ramassage ou le lancer de bombes (Glove Power).
     * @param player Le joueur
     * @param playerNumber Le numéro du joueur
     */
    public void handleBombPickupOrThrow(Player player, int playerNumber) {
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

    /**
     * Essaie de ramasser une bombe.
     * @param player Le joueur
     * @param playerNumber Le numéro du joueur
     */
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
                // Supprimer visuellement
                if (visualRenderer != null) {
                    visualRenderer.removeBombVisual(bombToPickup);
                }

                activeBombs.remove(bombToPickup);
                kickingBombs.remove(bombToPickup);

                // Décrémenter le bon compteur
                if (playerNumber == 1) {
                    currentBombCountPlayer1--;
                } else {
                    currentBombCountPlayer2--;
                }

                System.out.println("Joueur " + playerNumber + ": Bombe ramassée !");
            }
        } else {
            System.out.println("Joueur " + playerNumber + ": Aucune bombe à ramasser !");
        }
    }

    /**
     * Lance la bombe tenue par le joueur.
     * @param player Le joueur
     * @param playerNumber Le numéro du joueur
     */
    private void throwHeldBomb(Player player, int playerNumber) {
        // Direction par défaut - à améliorer avec GameLogic
        int[] direction = {0, 1}; // Direction par défaut vers le bas

        Bomb thrownBomb = player.throwHeldBomb(direction[0], direction[1]);

        if (thrownBomb != null) {
            thrownBomb.setPosition(player.getX(), player.getY());

            // Afficher visuellement
            if (visualRenderer != null) {
                visualRenderer.placeBombVisual(thrownBomb);
            }

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
                explodeBomb(thrownBomb, playerNumber);
            });

            System.out.println("Joueur " + playerNumber + ": Bombe lancée !");
        }
    }

    /**
     * Place des bombes en ligne droite (LineBomb Power).
     * @param player Le joueur
     * @param playerNumber Le numéro du joueur
     */
    public void placeLineBombs(Player player, int playerNumber) {
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

        // Direction par défaut pour LineBomb (on devra l'améliorer plus tard)
        int dirX = 0;
        int dirY = 1; // Vers le bas par défaut

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

            // Afficher visuellement
            if (visualRenderer != null) {
                visualRenderer.placeBombVisual(bomb);
            }

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
                    explodeBomb(bomb, playerNumber);
                });
            }
        }

        System.out.println("Joueur " + playerNumber + ": LineBomb terminé ! " + bombsPlaced + " bombes posées.");
    }

    /**
     * Fait exploser toutes les bombes Remote du joueur.
     * @param player Le joueur
     * @param playerNumber Le numéro du joueur
     */
    public void detonateRemoteBombs(Player player, int playerNumber) {
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
            explodeBomb(bomb, playerNumber);
        }
    }

    /**
     * Gère l'explosion d'une bombe (centre + propagation dans les 4 directions).
     * @param bomb La bombe à faire exploser
     */
    private void handleExplosion(Bomb bomb) {
        int x = bomb.getX();
        int y = bomb.getY();
        int range = bomb.getRange();
        Player owner = bomb.getOwner();

        // Afficher l'explosion au centre (se supprime auto en 0.5s)
        if (visualRenderer != null) {
            visualRenderer.showExplosion(x, y);
        }

        // Gérer l'explosion au centre via gameLogic
        if (gameLogic != null) {
            gameLogic.handleExplosionAt(x, y); // Centre
        }

        // Explosion dans les 4 directions
        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        for (int[] direction : directions) {
            for (int rangeStep = 1; rangeStep <= range; rangeStep++) {
                int nx = x + direction[0] * rangeStep;
                int ny = y + direction[1] * rangeStep;
                if (!isInBounds(nx, ny)) break;
                boolean continueExplosion = destroyTile(nx, ny, owner);
                if (gameLogic != null) {
                    gameLogic.handleExplosionAt(nx, ny);
                }
                if (!continueExplosion) break;
            }
        }

        // Tuer l'ennemi si touché par l'explosion au centre
        if (enemy != null && enemy.isAlive() && enemy.getX() == x && enemy.getY() == y) {
            enemy.kill();
            if (scoreSystem != null && owner != null) {
                scoreSystem.addEnemyKilled(owner);
                scoreSystem.processExplosionCombo(owner);
            }
            if (gameLogic != null) {
                gameLogic.checkAndEndGame();
            }
        }
    }

    /**
     * Détruit une tuile lors d'une explosion.
     * @param x Abscisse de la tuile
     * @param y Ordonnée de la tuile
     * @param owner Propriétaire de la bombe
     * @return true si l'explosion doit continuer, false sinon
     */
    private boolean destroyTile(int x, int y, Player owner) {
        Tile tile = map[y][x];

        if (tile.getType() == TileType.WALL) {
            return false; // Arrêter l'explosion
        }

        if (visualRenderer != null) {
            visualRenderer.showExplosion(x, y);

            if (tile.getType() == TileType.WALL_BREAKABLE) {
                map[y][x] = new Tile(TileType.FLOOR);

                if (scoreSystem != null && owner != null) {
                    scoreSystem.addWallDestroyed(owner);
                }

                PauseTransition delay = new PauseTransition(Duration.seconds(0.6));
                delay.setOnFinished(event -> {
                    if (powerUpManager != null) {
                        PowerUp powerUp = powerUpManager.generateRandomPowerUp(x, y);
                        if (powerUp != null) {
                            visualRenderer.placePowerUpVisual(powerUp);
                        }
                    }
                    visualRenderer.redrawTile(x, y, powerUpManager != null ? powerUpManager.getActivePowerUps() : new ArrayList<>());
                });
                delay.play();
                return false; // Arrêter l'explosion
            }
        }

        // Gérer la mort de l'ennemi si touché par l'explosion
        if (enemy != null && enemy.isAlive() && enemy.getX() == x && enemy.getY() == y) {
            enemy.kill();
            if (scoreSystem != null && owner != null) {
                scoreSystem.addEnemyKilled(owner);
                scoreSystem.processExplosionCombo(owner);
            }
            if (gameLogic != null) {
                gameLogic.checkAndEndGame();
            }
        }

        return true; // Continuer l'explosion
    }

    /**
     * Met à jour les bombes volantes et qui roulent.
     */
    public void updateBombs() {
        handleFlyingBombs();
        handleKickingBombs();
    }

    /**
     * Gère les bombes volantes (Glove Power).
     */
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
                // Mettre à jour la position visuelle
                if (visualRenderer != null) {
                    visualRenderer.updateBombVisual(bomb);
                }
            }
        }

        flyingBombs.removeAll(bombsToStop);
    }

    /**
     * Gère les bombes qui roulent (Kick Power).
     */
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
                // Mettre à jour la position visuelle
                if (visualRenderer != null) {
                    visualRenderer.updateBombVisual(bomb);
                }
            }
        }

        kickingBombs.removeAll(bombsToStop);
    }

    /**
     * Vérifie si une bombe peut se déplacer vers une position.
     * @param x Abscisse
     * @param y Ordonnée
     * @return true si le déplacement est possible, false sinon
     */
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

        // Vérifier les joueurs et l'ennemi
        if ((player1.getX() == x && player1.getY() == y) ||
                (!isOnePlayer && player2 != null && player2.getX() == x && player2.getY() == y) ||
                (enemy != null && enemy.getX() == x && enemy.getY() == y)) {
            return false;
        }

        return true;
    }

    /**
     * Vérifie si on peut placer une bombe à une position.
     * @param x Abscisse
     * @param y Ordonnée
     * @return true si possible, false sinon
     */
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

    /**
     * Vérifie si les coordonnées sont dans les limites de la carte.
     * @param x Abscisse
     * @param y Ordonnée
     * @return true si dans les limites, false sinon
     */
    private boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && y < map.length && x < map[0].length;
    }

    /**
     * Essaie de faire rouler une bombe (Kick Power).
     * @param bomb La bombe à kicker
     * @param directionX Direction X (-1, 0, 1)
     * @param directionY Direction Y (-1, 0, 1)
     * @return true si le kick a réussi, false sinon
     */
    public boolean tryKickBomb(Bomb bomb, int directionX, int directionY) {
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

    // --- Getters ---

    /** @return Liste des bombes actives */
    public List<Bomb> getActiveBombs() {
        return new ArrayList<>(activeBombs);
    }

    /** @return Liste des bombes volantes */
    public List<Bomb> getFlyingBombs() {
        return new ArrayList<>(flyingBombs);
    }

    /** @return Liste des bombes qui roulent */
    public List<Bomb> getKickingBombs() {
        return new ArrayList<>(kickingBombs);
    }

    /** @return Nombre de bombes du joueur 1 */
    public int getCurrentBombCountPlayer1() {
        return currentBombCountPlayer1;
    }

    /** @return Nombre de bombes du joueur 2 */
    public int getCurrentBombCountPlayer2() {
        return currentBombCountPlayer2;
    }

    /**
     * Nettoie toutes les bombes (utile pour reset).
     */
    public void clearAllBombs() {
        activeBombs.clear();
        flyingBombs.clear();
        kickingBombs.clear();
        currentBombCountPlayer1 = 0;
        currentBombCountPlayer2 = 0;
        System.out.println("Toutes les bombes ont été supprimées");
    }
}