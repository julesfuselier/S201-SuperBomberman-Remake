package com.superbomberman.controller;

import com.superbomberman.model.*;
import com.superbomberman.game.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.animation.AnimationTimer;
import javafx.stage.Stage;

import java.io.IOException;

import static com.superbomberman.model.MapLoader.enemy;
import static com.superbomberman.model.MapLoader.player1;
import static com.superbomberman.model.MapLoader.player2;
import static com.superbomberman.controller.MenuController.isOnePlayer;

/**
 * Contrôleur principal de la vue de jeu pour Super Bomberman.
 * Version refactorisée avec séparation des responsabilités et affichage des scores.
 *
 * Chaque aspect du jeu est maintenant géré par une classe spécialisée :
 * - GameStateManager : État du jeu et statistiques
 * - VisualRenderer : Rendu visuel
 * - InputHandler : Gestion des entrées clavier
 * - BombManager : Logique des bombes
 * - PowerUpManager : Gestion des power-ups
 * - GameLogic : Logique principale (mouvement, collisions, IA)
 *
 * @author Jules Fuselier
 * @version 3.3 - Ajout affichage scores à gauche
 * @since 2025-06-11
 */
public class GameViewController extends OptionsController {

    @FXML
    private GridPane gameGrid;

    // Éléments FXML pour l'affichage des scores
    @FXML
    private Label scorePlayer1;
    @FXML
    private Label scorePlayer2;
    @FXML
    private Label bombsPlayer1;
    @FXML
    private Label bombsPlayer2;
    @FXML
    private Label livesPlayer1;
    @FXML
    private Label livesPlayer2;
    @FXML
    private Label timeLabel;
    @FXML
    private Label powerupsLabel;
    @FXML
    private Label totalScoreLabel;
    @FXML
    private VBox player2Panel;
    @FXML
    private Button pauseButton;

    // Gestionnaires délégués - Chacun a sa responsabilité
    private GameStateManager gameStateManager;
    private VisualRenderer visualRenderer;
    private InputHandler inputHandler;
    private BombManager bombManager;
    private PowerUpManager powerUpManager;
    private GameLogic gameLogic;

    // Données de base
    private Tile[][] map;
    private User currentUser;

    // Timer pour la boucle de jeu et gestion du temps
    private AnimationTimer gameLoop;
    private long gameStartTime;
    private boolean gamePaused = false;

    /**
     * Initialise tous les composants du jeu
     */
    public void initialize() {
        try {
            System.out.println("=== INITIALISATION DU JEU ===");

            // Enregistrer le temps de début
            gameStartTime = System.currentTimeMillis();

            // Masquer le panneau joueur 2 si mode 1 joueur
            if (isOnePlayer && player2Panel != null) {
                player2Panel.setVisible(false);
                player2Panel.setManaged(false);
            }

            // Étape 1 : Charger la carte
            initializeMap();

            // Étape 2 : Créer tous les gestionnaires
            initializeManagers();

            // Étape 3 : Placer les entités sur la carte
            initializeEntities();

            // Étape 4 : Initialiser l'affichage des scores
            initializeScoreDisplay();

            // Étape 5 : Démarrer la boucle de jeu
            startGameLoop();

            // Étape 6 : Afficher les contrôles
            inputHandler.displayControls();

            // Étape 7 : Forcer le focus et l'affichage
            Platform.runLater(() -> {
                forceFocus();
                System.out.println("=== JEU INITIALISÉ AVEC SUCCÈS ===");
            });

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERREUR CRITIQUE : Impossible d'initialiser le jeu");
        }
    }

    /**
     * Initialise l'affichage des scores
     */
    private void initializeScoreDisplay() {
        Platform.runLater(() -> {
            updateScoreDisplay();
            System.out.println("Affichage des scores initialisé");
        });
    }

    /**
     * Met à jour l'affichage des scores dans le panneau latéral
     */
    private void updateScoreDisplay() {
        if (gameStateManager == null) return;

        Platform.runLater(() -> {
            try {
                // Mise à jour du score du joueur 1
                if (scorePlayer1 != null && player1 != null) {
                    int score1 = gameStateManager.getScoreSystem().getPlayerScore(player1);
                    scorePlayer1.setText("Score: " + score1);
                }

                if (bombsPlayer1 != null && player1 != null && bombManager != null) {
                    int currentBombs = bombManager.getCurrentBombCountPlayer1();
                    int maxBombs = player1.getMaxBombs();
                    bombsPlayer1.setText("💣 Bombes: " + currentBombs + "/" + maxBombs);
                }

                if (livesPlayer1 != null && player1 != null) {
                    // Assumons que le joueur commence avec 3 vies (à adapter selon votre logique)
                    int lives1 = player1.isAlive() ? 3 : 0; // Vous pouvez ajuster selon votre système de vies
                    livesPlayer1.setText("❤️ Vies: " + lives1);
                }

                // Mise à jour du score du joueur 2 (si mode 2 joueurs)
                if (!isOnePlayer && player2 != null) {
                    if (scorePlayer2 != null) {
                        int score2 = gameStateManager.getScoreSystem().getPlayerScore(player2);
                        scorePlayer2.setText("Score: " + score2);
                    }

                    if (bombsPlayer2 != null && bombManager != null) {
                        int currentBombs2 = bombManager.getCurrentBombCountPlayer2();
                        int maxBombs2 = player2.getMaxBombs();
                        bombsPlayer2.setText("💣 Bombes: " + currentBombs2 + "/" + maxBombs2);
                    }

                    if (livesPlayer2 != null) {
                        int lives2 = player2.isAlive() ? 3 : 0;
                        livesPlayer2.setText("❤️ Vies: " + lives2);
                    }
                }

                // Mise à jour du temps de jeu
                if (timeLabel != null) {
                    long elapsedTime = (System.currentTimeMillis() - gameStartTime) / 1000;
                    int minutes = (int) (elapsedTime / 60);
                    int seconds = (int) (elapsedTime % 60);
                    timeLabel.setText(String.format("⏱️ Temps: %02d:%02d", minutes, seconds));
                }

                // Mise à jour des power-ups actifs
                if (powerupsLabel != null && powerUpManager != null) {
                    int powerUpCount = powerUpManager.getActivePowerUpCount();
                    powerupsLabel.setText("⚡ Power-ups: " + powerUpCount);
                }

                // Mise à jour du score total
                if (totalScoreLabel != null) {
                    int totalScore = gameStateManager.getGameScore();
                    totalScoreLabel.setText("🎯 Total: " + totalScore);
                }

            } catch (Exception e) {
                System.err.println("Erreur lors de la mise à jour des scores: " + e.getMessage());
            }
        });
    }

    /**
     * Charge la carte selon le mode de jeu
     */
    private void initializeMap() throws IOException {
        System.out.println("Mode un joueur: " + isOnePlayer);
        String mapName = OptionsController.getSelectedMap();
        if (mapName == null || mapName.isEmpty())
            mapName = isOnePlayer ? "level1.txt" : "level2.txt";
        String mapPath = "src/main/resources/maps/" + mapName;
        map = MapLoader.loadMap(mapPath);

        // Toujours appliquer le filtrage, même si ce n'est pas passé par le bouton map
        filterMapForGameMode();

        if (isOnePlayer) player2 = null;
        if (!isOnePlayer) enemy = null;

        System.out.println("Carte chargée: " + map.length + "x" + map[0].length);
    }

    /**
     * Retire dynamiquement les 'E' (ennemi) ou '2' (joueur 2) de la map selon le mode.
     * On remplace simplement par du sol (FLOOR) pour la logique.
     */
    private void filterMapForGameMode() {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                TileType type = map[y][x].getType();
                if (isOnePlayer && type == TileType.ENEMY) {
                    map[y][x] = new Tile(TileType.FLOOR); // enlève le bot en 1 joueur
                } else if (!isOnePlayer && type == TileType.PLAYER2) {
                    map[y][x] = new Tile(TileType.FLOOR); // enlève le joueur 2 en solo bot
                }
            }
        }
    }

    /**
     * Initialise tous les gestionnaires dans le bon ordre
     */
    private void initializeManagers() {
        System.out.println("Initialisation des gestionnaires...");

        // 1. Configurer la grille de jeu AVANT tout le reste
        configureGameGrid();

        // 2. GameStateManager - Gère l'état du jeu
        gameStateManager = new GameStateManager(currentUser, null);

        // 3. VisualRenderer - Gère l'affichage
        visualRenderer = new VisualRenderer(gameGrid, map);

        // 4. Initialiser l'affichage visuel
        Platform.runLater(() -> {
            visualRenderer.setupGridConstraints();
            visualRenderer.drawMap();
            System.out.println("Rendu visuel initialisé");
        });

        // 5. InputHandler - Gère les entrées
        inputHandler = new InputHandler();

        // 6. BombManager - Gère les bombes
        bombManager = new BombManager(map);

        // 7. PowerUpManager - Gère les power-ups
        powerUpManager = new PowerUpManager();

        // 8. GameLogic - Logique principale (dépend de tous les autres)
        gameLogic = new GameLogic(map, bombManager, powerUpManager, gameStateManager);

        // 9. Configurer les références croisées

        bombManager.setManagers(visualRenderer, powerUpManager, gameStateManager);

        // 🆕 AJOUTER LA RÉFÉRENCE GAMELOGIC MANQUANTE
        bombManager.setGameLogic(gameLogic);

        System.out.println("Tous les gestionnaires initialisés!");
    }

    /**
     * Configure la grille de jeu avec des tailles appropriées
     */
    private void configureGameGrid() {
        if (gameGrid != null) {
            System.out.println("Configuration de la grille de jeu...");

            // Calculer les dimensions basées sur la carte
            int mapWidth = map[0].length;
            int mapHeight = map.length;

            // Taille de cellule optimale pour la lisibilité
            double cellSize = 40.0;

            // Dimensions de la grille
            double gridWidth = mapWidth * cellSize;
            double gridHeight = mapHeight * cellSize;

            // Configurer la grille
            gameGrid.setPrefSize(gridWidth, gridHeight);
            gameGrid.setMaxSize(gridWidth, gridHeight);
            gameGrid.setMinSize(gridWidth, gridHeight);

            // Styles pour éviter les problèmes d'affichage
            gameGrid.setStyle(
                    "-fx-background-color: #34495e;" +
                            "-fx-border-color: #ecf0f1;" +
                            "-fx-border-width: 2px;" +
                            "-fx-grid-lines-visible: false;"
            );

            // S'assurer que la grille peut recevoir le focus
            gameGrid.setFocusTraversable(true);

            System.out.println("Grille configurée : " + gridWidth + "x" + gridHeight +
                    " (" + mapWidth + "x" + mapHeight + " cellules de " + cellSize + "px)");
        }
    }

    /**
     * Place toutes les entités sur la carte
     */
    private void initializeEntities() {
        System.out.println("Placement des entités...");

        // Attendre que l'interface soit prête
        Platform.runLater(() -> {
            try {
                // Initialiser le joueur 1
                if (map[player1.getY()][player1.getX()].getType() == TileType.FLOOR) {
                    map[player1.getY()][player1.getX()] = new Tile(TileType.PLAYER1);
                    visualRenderer.addEntityToGrid(player1.getX(), player1.getY(), visualRenderer.getPlayerPattern());
                    System.out.println("Joueur 1 placé à (" + player1.getX() + ", " + player1.getY() + ")");
                }

                // Initialiser le joueur 2 (mode 2 joueurs uniquement)
                if (!isOnePlayer && player2 != null && map[player2.getY()][player2.getX()].getType() == TileType.FLOOR) {
                    map[player2.getY()][player2.getX()] = new Tile(TileType.PLAYER2);
                    visualRenderer.addEntityToGrid(player2.getX(), player2.getY(), visualRenderer.getPlayer2Pattern());
                    System.out.println("Joueur 2 placé à (" + player2.getX() + ", " + player2.getY() + ")");
                }

                // Initialiser l'ennemi
                if (enemy != null && map[enemy.getY()][enemy.getX()].getType() == TileType.FLOOR) {
                    map[enemy.getY()][enemy.getX()] = new Tile(TileType.ENEMY);
                    visualRenderer.addEntityToGrid(enemy.getX(), enemy.getY(), visualRenderer.getEnemyPattern());
                    System.out.println("Ennemi placé à (" + enemy.getX() + ", " + enemy.getY() + ")");
                }

                // Configurer les entrées clavier
                inputHandler.setupKeyboardHandling(gameGrid);

                System.out.println("Entités placées avec succès!");

            } catch (Exception e) {
                System.err.println("Erreur lors du placement des entités: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Démarre la boucle principale du jeu
     */
    private void startGameLoop() {
        System.out.println("Démarrage de la boucle de jeu...");

        gameLoop = new AnimationTimer() {
            private long lastAutoBombTimePlayer1 = 0;
            private long lastAutoBombTimePlayer2 = 0;
            private long lastScoreUpdate = 0;
            private final long AUTO_BOMB_INTERVAL = 2_000_000_000L; // 2 secondes
            private final long SCORE_UPDATE_INTERVAL = 100_000_000L; // 0.1 seconde

            @Override
            public void handle(long now) {
                try {

                    // Ignorer si le jeu est en pause
                    if (gamePaused) return;


                    // === PHASE 1 : ACTIONS IMMÉDIATES ===
                    // Traiter les actions instantanées (bombes, pouvoirs)
                    inputHandler.processImmediateActions(player1, player2, bombManager, gameLogic);

                    // === PHASE 2 : MOUVEMENT DES ENTITÉS ===
                    // Mouvement du joueur 1
                    gameLogic.handlePlayerMovement(player1, 1, now, inputHandler.getPressedKeys(), visualRenderer);

                    // Mouvement du joueur 2 (si mode 2 joueurs)
                    if (!isOnePlayer && player2 != null) {
                        gameLogic.handlePlayerMovement(player2, 2, now, inputHandler.getPressedKeys(), visualRenderer);
                    }

                    // Mouvement de l'ennemi
                    gameLogic.handleEnemyMovement(now, visualRenderer);

                    // === PHASE 3 : GESTION DES BOMBES ===
                    // Mettre à jour les bombes volantes et qui roulent
                    bombManager.updateBombs();

                    // === PHASE 4 : GESTION DES MALUS AUTO_BOMB ===
                    if (player1.hasMalus(com.superbomberman.model.powerup.MalusType.AUTO_BOMB) &&
                            now - lastAutoBombTimePlayer1 >= AUTO_BOMB_INTERVAL) {
                        bombManager.placeBomb(player1, 1);
                        lastAutoBombTimePlayer1 = now;
                    }

                    if (!isOnePlayer && player2 != null &&
                            player2.hasMalus(com.superbomberman.model.powerup.MalusType.AUTO_BOMB) &&
                            now - lastAutoBombTimePlayer2 >= AUTO_BOMB_INTERVAL) {
                        bombManager.placeBomb(player2, 2);
                        lastAutoBombTimePlayer2 = now;
                    }

                    // === PHASE 5 : MISE À JOUR DE L'AFFICHAGE DES SCORES ===
                    if (now - lastScoreUpdate >= SCORE_UPDATE_INTERVAL) {
                        updateScoreDisplay();
                        lastScoreUpdate = now;
                    }

                    // === PHASE 6 : VÉRIFICATIONS FINALES ===
                    // Mettre à jour toutes les entités
                    gameLogic.updateEntities(visualRenderer);

                    // Vérifier les conditions de victoire/défaite
                    gameStateManager.checkGameConditions();

                } catch (Exception e) {
                    System.err.println("Erreur dans la boucle de jeu: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        gameLoop.start();
        System.out.println("Boucle de jeu démarrée!");
    }

    /**
     * Gère la mise en pause du jeu
     */
    @FXML
    private void handlePause() {
        if (gamePaused) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    /**
     * Définit l'utilisateur actuel pour le suivi des statistiques
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("Utilisateur défini: " + (user != null ? user.getUsername() : "Invité"));
    }

    /**
     * Arrête la boucle de jeu
     */
    public void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
            System.out.println("Boucle de jeu arrêtée");
        }
    }

    /**
     * Retourne au menu principal
     */
    @FXML
    private void handleBackToMenu() {
        System.out.println("Retour au menu demandé...");

        // Arrêter la boucle de jeu
        stopGameLoop();

        // Sauvegarder les statistiques
        gameStateManager.endGame();

        try {
            // Charger la vue du menu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent menuRoot = loader.load();

            // Passer l'utilisateur au contrôleur du menu
            MenuController menuController = loader.getController();
            if (gameStateManager.getCurrentUser() != null) {
                menuController.setCurrentUser(gameStateManager.getCurrentUser());
            }

            // Récupérer la fenêtre actuelle
            Stage stage = (Stage) gameGrid.getScene().getWindow();

            // Créer la nouvelle scène
            Scene menuScene = new Scene(menuRoot);

            // Changer de scène
            stage.setScene(menuScene);
            stage.setTitle("Super Bomberman - Menu");

            // IMPORTANT : Redimensionner la fenêtre pour s'adapter au menu
            stage.sizeToScene(); // Ajuste automatiquement à la taille du contenu

            // Ou vous pouvez définir des dimensions spécifiques pour le menu :
            // stage.setWidth(800);  // Largeur du menu
            // stage.setHeight(600); // Hauteur du menu

            // Centrer la fenêtre sur l'écran
            stage.centerOnScreen();

            System.out.println("Retour au menu réussi!");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du retour au menu");
        }
    }

    /**
     * Gère la mise en pause du jeu
     */
    public void pauseGame() {
        gamePaused = true;
        if (inputHandler != null) {
            inputHandler.clearPressedKeys();
        }
        if (pauseButton != null) {
            pauseButton.setText("▶️ Reprendre");
        }
        System.out.println("Jeu mis en pause");
    }

    /**
     * Reprend le jeu après une pause
     */
    public void resumeGame() {
        gamePaused = false;
        if (pauseButton != null) {
            pauseButton.setText("⏸️ Pause");
        }
        System.out.println("Jeu repris");
    }

    /**
     * Nettoie toutes les ressources (utile pour les tests)
     */
    public void cleanup() {
        stopGameLoop();
        if (bombManager != null) bombManager.clearAllBombs();
        if (powerUpManager != null) powerUpManager.clearAllPowerUps();
        System.out.println("Nettoyage terminé");
    }

    // === GETTERS POUR LES TESTS ET LE DEBUG ===

    public GameStateManager getGameStateManager() { return gameStateManager; }
    public VisualRenderer getVisualRenderer() { return visualRenderer; }
    public InputHandler getInputHandler() { return inputHandler; }
    public BombManager getBombManager() { return bombManager; }
    public PowerUpManager getPowerUpManager() { return powerUpManager; }
    public GameLogic getGameLogic() { return gameLogic; }
    public Tile[][] getMap() { return map; }

    /**
     * Affiche des statistiques de debug
     */
    public void printDebugStats() {
        System.out.println("=== STATISTIQUES DE DEBUG ===");
        System.out.println("Score actuel: " + gameStateManager.getGameScore());
        System.out.println("Bombes actives: " + bombManager.getActiveBombs().size());
        System.out.println("Power-ups actifs: " + powerUpManager.getActivePowerUpCount());
        System.out.println("Joueur 1 - Position: (" + player1.getX() + ", " + player1.getY() + ")");
        if (!isOnePlayer && player2 != null) {
            System.out.println("Joueur 2 - Position: (" + player2.getX() + ", " + player2.getY() + ")");
        }
        if (enemy != null) {
            System.out.println("Ennemi - Position: (" + enemy.getX() + ", " + enemy.getY() + ")");
        }

        // Stats des bombes par joueur
        System.out.println("Bombes J1: " + bombManager.getCurrentBombCountPlayer1() + "/" + player1.getMaxBombs());
        if (!isOnePlayer && player2 != null) {
            System.out.println("Bombes J2: " + bombManager.getCurrentBombCountPlayer2() + "/" + player2.getMaxBombs());
        }

        // Stats des malus actifs
        if (player1.hasActiveMalus()) {
            System.out.println("Joueur 1 - Malus actif: " + player1.getCurrentMalus() +
                    " (reste " + (player1.getMalusTimeRemaining()/1000) + "s)");
        }
        if (!isOnePlayer && player2 != null && player2.hasActiveMalus()) {
            System.out.println("Joueur 2 - Malus actif: " + player2.getCurrentMalus() +
                    " (reste " + (player2.getMalusTimeRemaining()/1000) + "s)");
        }

        System.out.println("==============================");
    }

    /**
     * Force l'affichage des contrôles dans la console (utile pour debug)
     */
    public void showControls() {
        if (inputHandler != null) {
            inputHandler.displayControls();
        }
    }

    /**
     * Force le focus sur la grille de jeu (utile si les contrôles ne répondent pas)
     */
    public void forceFocus() {
        if (gameGrid != null) {
            Platform.runLater(() -> {
                gameGrid.requestFocus();
                System.out.println("Focus forcé sur la grille de jeu");
            });
        }
    }

    /**
     * Méthode utilitaire pour tester la pose de bombes (debug)
     */
    public void testPlaceBomb(int playerNumber) {
        if (playerNumber == 1) {
            bombManager.placeBomb(player1, 1);
        } else if (playerNumber == 2 && !isOnePlayer && player2 != null) {
            bombManager.placeBomb(player2, 2);
        }
    }

    /**
     * Méthode utilitaire pour tester l'explosion manuelle (debug)
     */
    public void testRemoteExplosion(int playerNumber) {
        if (playerNumber == 1) {
            bombManager.detonateRemoteBombs(player1, 1);
        } else if (playerNumber == 2 && !isOnePlayer && player2 != null) {
            bombManager.detonateRemoteBombs(player2, 2);
        }
    }

    /**
     * Active les power-ups de debug pour tester les fonctionnalités
     */
    public void enableDebugPowers(int playerNumber) {
        Player player = (playerNumber == 1) ? player1 : player2;
        if (player == null) return;

        // Activer tous les pouvoirs pour les tests
        player.setCanKickBombs(true);
        player.setCanThrowBombs(true);
        player.setRemoteDetonation(true);
        player.setCanPassThroughWalls(true);
        player.setCanPassThroughBombs(true);
        player.setHasLineBombs(true);

        // Augmenter les stats
        for (int i = 0; i < 3; i++) {
            player.increaseMaxBombs();
            player.increaseExplosionRange();
            player.increaseSpeed();
        }

        System.out.println("Joueur " + playerNumber + ": Tous les pouvoirs de debug activés!");
    }

    /**
     * Affiche l'état détaillé d'un joueur (debug)
     */
    public void printPlayerState(int playerNumber) {
        Player player = (playerNumber == 1) ? player1 : player2;
        if (player == null) {
            System.out.println("Joueur " + playerNumber + " non disponible");
            return;
        }

        System.out.println("=== ÉTAT JOUEUR " + playerNumber + " ===");
        System.out.println("Position: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Vitesse: " + player.getSpeed());
        System.out.println("Bombes max: " + player.getMaxBombs());
        System.out.println("Portée explosion: " + player.getExplosionRange());
        System.out.println("Kick: " + player.canKickBombs());
        System.out.println("Glove: " + player.canThrowBombs());
        System.out.println("Remote: " + player.hasRemoteDetonation());
        System.out.println("WallPass: " + player.canPassThroughWalls());
        System.out.println("BombPass: " + player.canPassThroughBombs());
        System.out.println("LineBomb: " + player.hasLineBombs());
        System.out.println("Tient une bombe: " + player.isHoldingBomb());
        System.out.println("Malus actif: " + (player.hasActiveMalus() ? player.getCurrentMalus() : "Aucun"));
        System.out.println("========================");
    }
}