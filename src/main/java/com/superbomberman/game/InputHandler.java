package com.superbomberman.game;

import com.superbomberman.model.Player;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;

import java.util.HashSet;
import java.util.Set;

import static com.superbomberman.controller.MenuController.isOnePlayer;

/**
 * Gestionnaire des entrées clavier du jeu
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-08
 */
public class InputHandler {
    private Set<KeyCode> pressedKeys = new HashSet<>();

    /**
     * Configure la gestion des événements clavier
     */
    public void setupKeyboardHandling(GridPane gameGrid) {
        // Configuration sur le gameGrid
        gameGrid.setFocusTraversable(true);

        gameGrid.setOnKeyPressed(event -> {
            System.out.println("DEBUG: Touche pressée sur gameGrid: " + event.getCode());
            pressedKeys.add(event.getCode());
            event.consume(); // Empêcher la propagation
        });

        gameGrid.setOnKeyReleased(event -> {
            System.out.println("DEBUG: Touche relâchée sur gameGrid: " + event.getCode());
            pressedKeys.remove(event.getCode());
            event.consume(); // Empêcher la propagation
        });

        // Clic pour forcer le focus
        gameGrid.setOnMouseClicked(event -> {
            gameGrid.requestFocus();
            System.out.println("DEBUG: Focus demandé via clic");
        });

        // Debug du focus
        gameGrid.focusedProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("DEBUG: Focus du gameGrid: " + newVal);
        });

        // Configuration sur la scene une fois qu'elle est disponible
        Platform.runLater(() -> {
            Scene scene = gameGrid.getScene();
            if (scene != null) {
                System.out.println("DEBUG: Configuration des événements sur la scene");

                scene.setOnKeyPressed(event -> {
                    System.out.println("DEBUG: Touche pressée sur scene: " + event.getCode());
                    pressedKeys.add(event.getCode());
                    // Ne pas consommer ici pour permettre au gameGrid de recevoir aussi
                });

                scene.setOnKeyReleased(event -> {
                    System.out.println("DEBUG: Touche relâchée sur scene: " + event.getCode());
                    pressedKeys.remove(event.getCode());
                    // Ne pas consommer ici pour permettre au gameGrid de recevoir aussi
                });

                // Forcer le focus initial
                gameGrid.requestFocus();
                System.out.println("DEBUG: Focus initial demandé");
            } else {
                System.err.println("DEBUG: Scene non disponible!");
            }
        });
    }

    /**
     * Traite les actions immédiates (bombes, powers spéciaux)
     */
    public void processImmediateActions(Player player1, Player player2, BombManager bombManager, GameLogic gameLogic) {
        // Actions du joueur 1 (Flèches + SPACE + SHIFT + L + R)
        if (pressedKeys.contains(KeyCode.SPACE)) {
            bombManager.placeBomb(player1, 1);
            pressedKeys.remove(KeyCode.SPACE); // Éviter la répétition
        }
        if (pressedKeys.contains(KeyCode.SHIFT)) {
            bombManager.handleBombPickupOrThrow(player1, 1);
            pressedKeys.remove(KeyCode.SHIFT);
        }
        if (pressedKeys.contains(KeyCode.L)) {
            bombManager.placeLineBombs(player1, 1);
            pressedKeys.remove(KeyCode.L);
        }
        if (pressedKeys.contains(KeyCode.R)) {
            bombManager.detonateRemoteBombs(player1, 1);
            pressedKeys.remove(KeyCode.R);
        }

        // Actions du joueur 2 (ZQSD + ENTER + CTRL + K + O)
        if (!isOnePlayer && player2 != null) {
            if (pressedKeys.contains(KeyCode.ENTER)) {
                bombManager.placeBomb(player2, 2);
                pressedKeys.remove(KeyCode.ENTER);
            }
            if (pressedKeys.contains(KeyCode.CONTROL)) {
                bombManager.handleBombPickupOrThrow(player2, 2);
                pressedKeys.remove(KeyCode.CONTROL);
            }
            if (pressedKeys.contains(KeyCode.K)) {
                bombManager.placeLineBombs(player2, 2);
                pressedKeys.remove(KeyCode.K);
            }
            if (pressedKeys.contains(KeyCode.O)) {
                bombManager.detonateRemoteBombs(player2, 2);
                pressedKeys.remove(KeyCode.O);
            }
        }
    }

    /**
     * Vérifie si une touche de mouvement du joueur 1 est pressée
     */
    public boolean isPlayer1Moving() {
        return pressedKeys.contains(KeyCode.LEFT) ||
                pressedKeys.contains(KeyCode.RIGHT) ||
                pressedKeys.contains(KeyCode.UP) ||
                pressedKeys.contains(KeyCode.DOWN);
    }

    /**
     * Vérifie si une touche de mouvement du joueur 2 est pressée
     */
    public boolean isPlayer2Moving() {
        return pressedKeys.contains(KeyCode.Q) ||
                pressedKeys.contains(KeyCode.D) ||
                pressedKeys.contains(KeyCode.Z) ||
                pressedKeys.contains(KeyCode.S);
    }

    /**
     * Obtient la direction de mouvement du joueur 1
     * @return int[] {directionX, directionY}
     */
    public int[] getPlayer1Movement() {
        if (pressedKeys.contains(KeyCode.LEFT)) {
            return new int[]{-1, 0};
        } else if (pressedKeys.contains(KeyCode.RIGHT)) {
            return new int[]{1, 0};
        } else if (pressedKeys.contains(KeyCode.UP)) {
            return new int[]{0, -1};
        } else if (pressedKeys.contains(KeyCode.DOWN)) {
            return new int[]{0, 1};
        }
        return new int[]{0, 0};
    }

    /**
     * Obtient la direction de mouvement du joueur 2
     * @return int[] {directionX, directionY}
     */
    public int[] getPlayer2Movement() {
        if (pressedKeys.contains(KeyCode.Q)) {        // Q = gauche
            return new int[]{-1, 0};
        } else if (pressedKeys.contains(KeyCode.D)) { // D = droite
            return new int[]{1, 0};
        } else if (pressedKeys.contains(KeyCode.Z)) { // Z = haut
            return new int[]{0, -1};
        } else if (pressedKeys.contains(KeyCode.S)) { // S = bas
            return new int[]{0, 1};
        }
        return new int[]{0, 0};
    }

    /**
     * Affiche les contrôles dans la console
     */
    public void displayControls() {
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
        System.out.println("CLIQUEZ SUR LA GRILLE POUR ACTIVER LES CONTRÔLES !");
    }

    /**
     * Nettoie les touches pressées (utile pour les pauses)
     */
    public void clearPressedKeys() {
        pressedKeys.clear();
    }

    /**
     * Vérifie si une touche spécifique est pressée
     */
    public boolean isKeyPressed(KeyCode key) {
        return pressedKeys.contains(key);
    }

    /**
     * Retourne une copie de l'ensemble des touches actuellement pressées.
     */
    public Set<KeyCode> getPressedKeys() {
        return new HashSet<>(pressedKeys); // Retourne une copie pour éviter les modifications externes
    }
}