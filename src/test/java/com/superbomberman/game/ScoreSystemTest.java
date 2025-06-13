package com.superbomberman.game;

import com.superbomberman.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe ScoreSystem.
 * Couvre les mécaniques de score, combos, vies supplémentaires et intégrations.
 */
class ScoreSystemTest {

    private ScoreSystem scoreSystem;

    @Mock
    private GameStateManager mockGameStateManager;

    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scoreSystem = new ScoreSystem(mockGameStateManager);
        player1 = new Player("Player1");
        player2 = new Player("Player2");

        // Enregistrer les joueurs
        scoreSystem.registerPlayer(player1);
        scoreSystem.registerPlayer(player2);
    }

    @Test
    @DisplayName("Enregistrement d'un joueur - initialisation des scores")
    void testRegisterPlayer() {
        Player newPlayer = new Player("NewPlayer");
        scoreSystem.registerPlayer(newPlayer);

        assertEquals(0, scoreSystem.getScore(newPlayer));
        assertEquals(0, scoreSystem.getPlayerScore(newPlayer));
    }

    @Test
    @DisplayName("Ajout de points pour mur détruit")
    void testAddWallDestroyed() {
        scoreSystem.addWallDestroyed(player1);

        assertEquals(10, scoreSystem.getScore(player1));
        verify(mockGameStateManager).updateScore(10);
    }

    @Test
    @DisplayName("Ajout de points pour power-up collecté")
    void testAddPowerUpCollected() {
        scoreSystem.addPowerUpCollected(player1);

        assertEquals(50, scoreSystem.getScore(player1));
        verify(mockGameStateManager).updateScore(50);
    }

    @Test
    @DisplayName("Ajout d'ennemi tué - combo en attente")
    void testAddEnemyKilled() {
        scoreSystem.addEnemyKilled(player1);

        // Le score ne doit pas encore être ajouté (combo en attente)
        assertEquals(0, scoreSystem.getScore(player1));
        verify(mockGameStateManager, never()).updateScore(anyInt());
    }

    @Test
    @DisplayName("Traitement combo simple - un ennemi")
    void testProcessExplosionComboSingle() {
        scoreSystem.addEnemyKilled(player1);
        scoreSystem.processExplosionCombo(player1);

        assertEquals(100, scoreSystem.getScore(player1)); // 100 * 1 (multiplicateur)
        verify(mockGameStateManager).updateScore(100);
    }

    @Test
    @DisplayName("Traitement combo multiple - plusieurs ennemis")
    void testProcessExplosionComboMultiple() {
        scoreSystem.addEnemyKilled(player1);
        scoreSystem.addEnemyKilled(player1);
        scoreSystem.addEnemyKilled(player1);
        scoreSystem.processExplosionCombo(player1);

        // 100*1 + 100*2 + 100*3 = 600 points
        assertEquals(600, scoreSystem.getScore(player1));
        verify(mockGameStateManager).updateScore(600);
    }

    @Test
    @DisplayName("Combo vide - aucun effet")
    void testProcessExplosionComboEmpty() {
        scoreSystem.processExplosionCombo(player1);

        assertEquals(0, scoreSystem.getScore(player1));
        verify(mockGameStateManager, never()).updateScore(anyInt());
    }

    @Test
    @DisplayName("Reset du système de score")
    void testReset() {
        scoreSystem.addWallDestroyed(player1);
        scoreSystem.addPowerUpCollected(player2);

        assertEquals(10, scoreSystem.getScore(player1));
        assertEquals(50, scoreSystem.getScore(player2));

        scoreSystem.reset();

        assertEquals(0, scoreSystem.getScore(player1));
        assertEquals(0, scoreSystem.getScore(player2));
    }

    @Test
    @DisplayName("Gestion de joueur non enregistré")
    void testUnregisteredPlayer() {
        Player unknownPlayer = new Player("Unknown");

        assertEquals(0, scoreSystem.getScore(unknownPlayer));
        assertEquals(0, scoreSystem.getPlayerScore(unknownPlayer));
    }

    @Test
    @DisplayName("Vies supplémentaires - seuil de 10000 points")
    void testExtraLivesThreshold() {
        // Simuler l'ajout de beaucoup de points
        for (int i = 0; i < 200; i++) { // 200 * 50 = 10000 points
            scoreSystem.addPowerUpCollected(player1);
        }

        assertEquals(10000, scoreSystem.getScore(player1));
        // Note: La logique des vies supplémentaires est dans addScore (méthode privée)
        // On teste indirectement via les messages console ou via une méthode publique si ajoutée
    }

    @Test
    @DisplayName("Accumulation de score pour plusieurs joueurs")
    void testMultiplePlayersScoring() {
        scoreSystem.addWallDestroyed(player1);
        scoreSystem.addPowerUpCollected(player1);
        scoreSystem.addWallDestroyed(player2);

        assertEquals(60, scoreSystem.getScore(player1)); // 10 + 50
        assertEquals(10, scoreSystem.getScore(player2));  // 10
    }

    @Test
    @DisplayName("Finir niveau - traitement des combos en cours")
    void testFinishLevel() {
        scoreSystem.addEnemyKilled(player1);
        scoreSystem.addEnemyKilled(player2);

        // Avant finish level, les combos ne sont pas traités
        assertEquals(0, scoreSystem.getScore(player1));
        assertEquals(0, scoreSystem.getScore(player2));

        scoreSystem.finishLevel(300, 250); // temps max, temps utilisé

        // Après finish level, les combos sont traités
        assertEquals(100, scoreSystem.getScore(player1));
        assertEquals(100, scoreSystem.getScore(player2));
    }

    @Test
    @DisplayName("ScoreSystem sans GameStateManager")
    void testScoreSystemWithoutGameStateManager() {
        ScoreSystem independentScoreSystem = new ScoreSystem(null);
        Player testPlayer = new Player("TestPlayer");
        independentScoreSystem.registerPlayer(testPlayer);

        // Ne doit pas planter même sans GameStateManager
        assertDoesNotThrow(() -> {
            independentScoreSystem.addWallDestroyed(testPlayer);
            independentScoreSystem.addPowerUpCollected(testPlayer);
        });

        assertEquals(60, independentScoreSystem.getScore(testPlayer));
    }

    @Test
    @DisplayName("Combo après reset - état propre")
    void testComboAfterReset() {
        scoreSystem.addEnemyKilled(player1);
        scoreSystem.reset();
        scoreSystem.registerPlayer(player1); // Re-enregistrer après reset

        scoreSystem.processExplosionCombo(player1);

        // Pas de combo après reset
        assertEquals(0, scoreSystem.getScore(player1));
    }
}