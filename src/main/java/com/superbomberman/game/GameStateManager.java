/**
 * Gestionnaire de l'état du jeu et des statistiques pour Super Bomberman.
 * <p>
 * Centralise la gestion de la progression de la partie, du score, de la victoire/défaite,
 * des écrans de fin, de la réinitialisation et de la persistance des statistiques utilisateur.
 * Interface avec le système d'authentification, les entités du jeu (joueurs, ennemi) et le ScoreSystem.
 * </p>
 *
 * <ul>
 *     <li>Gère la victoire/défaite en solo et multi</li>
 *     <li>Affiche l'écran de fin adaptatif avec résumé</li>
 *     <li>Réinitialise l'état du jeu et des entités pour restart</li>
 *     <li>Met à jour les statistiques utilisateur via AuthService</li>
 *     <li>Permet de retourner au menu ou quitter le jeu proprement</li>
 *     <li>Expose ScoreSystem et informations de partie</li>
 * </ul>
 *
 * @author Jules Fuselier
 * @version 2.0 - Intégration ScoreSystem
 * @since 2025-06-08
 */
package com.superbomberman.game;

import com.superbomberman.controller.EndGameController;
import com.superbomberman.model.GameEndType;
import com.superbomberman.model.GameResult;
import com.superbomberman.model.Player;
import com.superbomberman.model.User;
import com.superbomberman.service.AuthService;

import static com.superbomberman.model.MapLoader.enemy;
import static com.superbomberman.model.MapLoader.player1;
import static com.superbomberman.model.MapLoader.player2;
import static com.superbomberman.controller.MenuController.isOnePlayer;

/**
 * Gestionnaire principal de l'état de la partie.
 * <ul>
 *     <li>Gère la victoire, la défaite, le score et la réinitialisation de partie.</li>
 *     <li>Affiche l'écran de fin et met à jour les statistiques utilisateur.</li>
 *     <li>Permet le restart de partie ou de retourner au menu.</li>
 * </ul>
 */
public class GameStateManager {
    /** Utilisateur courant (peut être null en mode invité). */
    private User currentUser;
    /** Service d'authentification pour la persistance des stats utilisateur. */
    private AuthService authService;
    /** Score global de la partie. */
    private int gameScore = 0;
    /** Indique si la partie est gagnée. */
    private boolean gameWon = false;
    /** Timestamp de début de partie. */
    private long gameStartTime;
    /** Indique si la partie est terminée (pour éviter plusieurs appels fin de jeu). */
    private boolean gameEnded = false;

    /** Système avancé de score (par joueur). */
    private ScoreSystem scoreSystem;
    /** Joueur gagnant (mode multijoueur). */
    private Player winner;

    /**
     * Crée un gestionnaire d'état de partie.
     * @param currentUser utilisateur courant (null si invité)
     * @param authService service d'authentification pour MAJ stats
     */
    public GameStateManager(User currentUser, AuthService authService) {
        this.currentUser = currentUser;
        this.authService = authService;
        this.gameStartTime = System.currentTimeMillis();
        this.scoreSystem = new ScoreSystem(this);
    }

    /**
     * Incrémente le score global de la partie.
     * @param points points à ajouter
     */
    public void updateScore(int points) {
        gameScore += points;
        System.out.println("Score actuel: " + gameScore);
    }

    /**
     * Définit le statut victoire/défaite de la partie.
     * @param won true si victoire, false si défaite
     */
    public void setGameWon(boolean won) {
        this.gameWon = won;
        if (won) {
            // Calculer le bonus de temps si victoire
            long gameEndTime = System.currentTimeMillis();
            int usedTimeSeconds = (int) ((gameEndTime - gameStartTime) / 1000);
            int maxTimeSeconds = 120; // 2 minutes par défaut

            scoreSystem.finishLevel(maxTimeSeconds, usedTimeSeconds);

            System.out.println("🎉 Victoire ! Score final: " + gameScore);
        }
    }

    /**
     * Termine la partie et met à jour les statistiques utilisateur.
     * Affiche l'écran de fin de jeu.
     */
    public void endGame() {
        if (gameEnded) {
            return;
        }
        gameEnded = true;

        if (currentUser != null && authService != null) {
            authService.updateUserStats(currentUser, gameWon, gameScore);
            System.out.println("Statistiques mises à jour pour " + currentUser.getUsername());
            System.out.println("Score final: " + gameScore + " | Victoire: " + (gameWon ? "Oui" : "Non"));
            scoreSystem.displayScoreSummary();
        }

        javafx.application.Platform.runLater(this::showEndGameScreen);
    }

    /**
     * Affiche l'écran de fin adaptatif, avec résumé de partie.
     */
    private void showEndGameScreen() {
        try {
            GameResult result = createGameResult();

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/endgame.fxml")
            );
            javafx.scene.Parent root = loader.load();

            EndGameController controller = loader.getController();
            controller.initializeEndScreen(result);
            controller.setGameStateManager(this);

            javafx.stage.Stage stage = getCurrentStage();
            if (stage != null) {
                stage.setScene(new javafx.scene.Scene(root));
                stage.setTitle("Super Bomberman - Fin de Partie");
                stage.sizeToScene();
            } else {
                System.err.println("❌ Impossible de trouver la fenêtre principale");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'affichage de l'écran de fin:");
            e.printStackTrace();
            returnToMenu();
        }
    }

    /**
     * Récupère la fenêtre principale JavaFX courante.
     * @return la Stage courante ou null
     */
    private javafx.stage.Stage getCurrentStage() {
        try {
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window instanceof javafx.stage.Stage && window.isShowing()) {
                    return (javafx.stage.Stage) window;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la récupération de la fenêtre: " + e.getMessage());
        }
        return null;
    }

    /**
     * Construit l'objet GameResult selon le mode de jeu (solo/multi).
     * @return GameResult à transmettre à l'écran de fin
     */
    private GameResult createGameResult() {
        long gameDuration = System.currentTimeMillis() - gameStartTime;

        if (isOnePlayer) {
            GameEndType endType = gameWon ? GameEndType.SOLO_VICTORY : GameEndType.SOLO_DEFEAT;
            int finalScore = scoreSystem.getPlayerScore(player1) + gameScore;
            System.out.println("🎯 Score final transmis: " + finalScore);
            return new GameResult(endType, finalScore, gameDuration);
        } else {
            String player1Name = player1 != null ? player1.getName() : "Joueur 1";
            String player2Name = player2 != null ? player2.getName() : "Joueur 2";
            int player1Score = scoreSystem.getPlayerScore(player1);
            int player2Score = player2 != null ? scoreSystem.getPlayerScore(player2) : 0;

            GameEndType endType;
            if (player1 != null && player2 != null) {
                if (player1.isAlive() && !player2.isAlive()) {
                    endType = GameEndType.MULTI_PLAYER1_WINS;
                } else if (!player1.isAlive() && player2.isAlive()) {
                    endType = GameEndType.MULTI_PLAYER2_WINS;
                } else {
                    endType = GameEndType.MULTI_DRAW;
                }
            } else {
                endType = GameEndType.MULTI_DRAW;
            }

            return new GameResult(endType, player1Name, player1Score,
                    player2Name, player2Score, gameDuration);
        }
    }

    /**
     * Vérifie et met fin à la partie si les conditions de victoire/défaite sont remplies.
     */
    public void checkGameConditions() {
        if (gameEnded) {
            return;
        }

        if (isOnePlayer) {
            if (enemy != null && isEnemyDefeated()) {
                setGameWon(true);
                endGame();
            } else if (isPlayerDefeated()) {
                setGameWon(false);
                endGame();
            }
        } else {
            boolean player1Alive = player1 != null && player1.isAlive();
            boolean player2Alive = player2 != null && player2.isAlive();

            if (!player1Alive && !player2Alive) {
                setGameWon(false);
                endGame();
            } else if (player1Alive && !player2Alive) {
                setGameWon(true);
                endGame();
            } else if (!player1Alive && player2Alive) {
                setGameWon(false);
                endGame();
            }
        }
    }

    /**
     * Vérifie si l'ennemi est vaincu (mort).
     * @return true si l'ennemi est mort
     */
    private boolean isEnemyDefeated() {
        return enemy != null && enemy.isDead();
    }

    /**
     * Vérifie si le(s) joueur(s) sont vaincus.
     * @return true si joueur(s) mort(s)
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
     * Réinitialise l'état de toutes les entités du jeu (joueurs, ennemis).
     */
    public void resetGameEntities() {
        System.out.println("🔄 Réinitialisation des entités du jeu...");

        if (player1 != null) {
            player1.setAlive(true);
            System.out.println("✅ Joueur 1 réinitialisé");
        }
        if (player2 != null) {
            player2.setAlive(true);
            System.out.println("✅ Joueur 2 réinitialisé");
        }
        if (enemy != null) {
            enemy.setAlive(true);
            System.out.println("✅ Ennemi réinitialisé");
        }
        System.out.println("🎮 Toutes les entités ont été réinitialisées");
    }

    /**
     * Réinitialise complètement l'état de la partie (score, victoire, timer, etc).
     */
    public void resetGameState() {
        System.out.println("🔄 Réinitialisation de l'état du jeu...");

        this.gameEnded = false;
        this.gameWon = false;
        this.gameScore = 0;
        this.gameStartTime = System.currentTimeMillis();
        this.winner = null;

        if (scoreSystem != null) {
            scoreSystem.reset();
        }

        System.out.println("✅ État du jeu réinitialisé");
    }

    // === Méthodes pour les boutons (appelées depuis EndGameController) ===

    /**
     * Relance une nouvelle partie (restart) en réinitialisant tout et rechargeant la vue de jeu.
     */
    public void restartGame() {
        javafx.application.Platform.runLater(() -> {
            try {
                System.out.println("🔄 DÉBUT DU RESTART...");

                resetGameState();
                resetGameEntities();

                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/fxml/game-view.fxml")
                );
                javafx.scene.Parent gameRoot = loader.load();

                com.superbomberman.controller.GameViewController gameController = loader.getController();
                if (currentUser != null) {
                    gameController.setCurrentUser(currentUser);
                }

                javafx.stage.Stage stage = getCurrentStage();
                if (stage != null) {
                    javafx.scene.Scene newScene = new javafx.scene.Scene(gameRoot);
                    stage.setScene(newScene);
                    stage.setTitle("Super Bomberman - " + (isOnePlayer ? "1 Joueur" : "2 Joueurs"));
                    stage.sizeToScene();
                    stage.centerOnScreen();
                    System.out.println("✅ RESTART TERMINÉ AVEC SUCCÈS !");
                }
            } catch (Exception e) {
                System.err.println("❌ ERREUR DURANT LE RESTART:");
                e.printStackTrace();
            }
        });
    }

    /**
     * Retourne au menu principal.
     */
    public void returnToMenu() {
        javafx.application.Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/fxml/menu.fxml")
                );
                javafx.scene.Parent menuRoot = loader.load();

                javafx.stage.Stage stage = getCurrentStage();
                if (stage != null) {
                    stage.setScene(new javafx.scene.Scene(menuRoot));
                    stage.setTitle("Super Bomberman - Menu");
                    stage.sizeToScene();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Ferme proprement le jeu.
     */
    public void quitGame() {
        System.out.println("Quitter le jeu");
        javafx.application.Platform.exit();
    }

    // === Getters ===

    /** Définit le gagnant (mode multi). */
    public void setWinner(Player winner) {
        this.winner = winner;
    }
    /** @return score global */
    public int getGameScore() { return gameScore; }
    /** @return true si victoire */
    public boolean isGameWon() { return gameWon; }
    /** @return utilisateur courant */
    public User getCurrentUser() { return currentUser; }
    /** @return timestamp début de partie */
    public long getGameStartTime() { return gameStartTime; }
    /** @return ScoreSystem courant */
    public ScoreSystem getScoreSystem() { return scoreSystem; }
}