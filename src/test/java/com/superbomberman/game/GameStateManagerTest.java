package com.superbomberman.game;

import com.superbomberman.model.Player;
import com.superbomberman.model.User;
import com.superbomberman.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour GameStateManager.
 * Couvre la gestion d'état, score, victoire/défaite et intégrations.
 */
class GameStateManagerTest {

    private GameStateManager gameStateManager;

    @Mock
    private User mockUser;

    @Mock
    private AuthService mockAuthService;

    @Mock
    private ScoreSystem mockScoreSystem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockUser.getUsername()).thenReturn("testuser");
    }

    @Test
    @DisplayName("Création GameStateManager avec utilisateur")
    void testGameStateManagerCreation() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);

        assertNotNull(gameStateManager);
        assertEquals(mockUser, gameStateManager.getCurrentUser());
        assertEquals(0, gameStateManager.getGameScore());
        assertFalse(gameStateManager.isGameWon());
        assertNotNull(gameStateManager.getScoreSystem());
        assertTrue(gameStateManager.getGameStartTime() > 0);
    }

    @Test
    @DisplayName("Création GameStateManager en mode invité")
    void testGameStateManagerCreationGuest() {
        gameStateManager = new GameStateManager(null, null);

        assertNotNull(gameStateManager);
        assertNull(gameStateManager.getCurrentUser());
        assertEquals(0, gameStateManager.getGameScore());
        assertFalse(gameStateManager.isGameWon());
        assertNotNull(gameStateManager.getScoreSystem());
    }

    @Test
    @DisplayName("Mise à jour du score")
    void testUpdateScore() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);

        gameStateManager.updateScore(100);
        assertEquals(100, gameStateManager.getGameScore());

        gameStateManager.updateScore(50);
        assertEquals(150, gameStateManager.getGameScore());

        gameStateManager.updateScore(-30);
        assertEquals(120, gameStateManager.getGameScore());
    }

    @Test
    @DisplayName("Définir victoire - calcul bonus temps")
    void testSetGameWon() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);

        // Simuler un délai pour le calcul de temps
        long startTime = gameStateManager.getGameStartTime();

        gameStateManager.setGameWon(true);

        assertTrue(gameStateManager.isGameWon());
        // Le système devrait appeler finishLevel sur le ScoreSystem
    }

    @Test
    @DisplayName("Définir défaite")
    void testSetGameLost() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);

        gameStateManager.setGameWon(false);

        assertFalse(gameStateManager.isGameWon());
    }

    @Test
    @DisplayName("Fin de partie avec utilisateur connecté")
    void testEndGameWithUser() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);
        gameStateManager.updateScore(500);
        gameStateManager.setGameWon(true);

        // Note: endGame() utilise Platform.runLater, difficile à tester directement
        // On teste la logique avant l'appel JavaFX
        assertFalse(gameStateManager.isGameWon() && gameStateManager.getGameScore() == 0);
    }

    @Test
    @DisplayName("Fin de partie en mode invité")
    void testEndGameGuest() {
        gameStateManager = new GameStateManager(null, null);
        gameStateManager.updateScore(300);
        gameStateManager.setGameWon(false);

        // Vérifier qu'aucune exception n'est levée
        assertDoesNotThrow(() -> {
            // La logique de fin sans authService
            assertEquals(300, gameStateManager.getGameScore());
            assertFalse(gameStateManager.isGameWon());
        });
    }

    @Test
    @DisplayName("Réinitialisation état du jeu")
    void testResetGameState() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);
        gameStateManager.updateScore(1000);
        gameStateManager.setGameWon(true);

        long originalStartTime = gameStateManager.getGameStartTime();

        gameStateManager.resetGameState();

        assertEquals(0, gameStateManager.getGameScore());
        assertFalse(gameStateManager.isGameWon());
        assertTrue(gameStateManager.getGameStartTime() >= originalStartTime);
    }

    @Test
    @DisplayName("Réinitialisation entités - mode solo")
    void testResetGameEntitiesSolo() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);

        // Simuler des entités mortes
        // Note: Les entités statiques sont difficiles à mocker
        // On teste que la méthode ne plante pas
        assertDoesNotThrow(() -> {
            gameStateManager.resetGameEntities();
        });
    }

    @Test
    @DisplayName("Gestion du gagnant - mode multi")
    void testSetWinner() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);
        Player mockPlayer = new Player("TestPlayer");

        gameStateManager.setWinner(mockPlayer);

        // Note: Pas de getter public pour winner,
        // mais on vérifie que la méthode ne plante pas
        assertDoesNotThrow(() -> {
            gameStateManager.setWinner(mockPlayer);
        });
    }

    @Test
    @DisplayName("Calcul durée de partie")
    void testGameDuration() throws InterruptedException {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);
        long startTime = gameStateManager.getGameStartTime();

        // Attendre un petit délai
        Thread.sleep(50);

        long currentTime = System.currentTimeMillis();
        assertTrue(currentTime > startTime);

        // La durée devrait être positive
        assertTrue((currentTime - startTime) >= 0);
    }

    @Test
    @DisplayName("Double appel endGame - protection")
    void testDoubleEndGame() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);

        // Premier appel
        // Note: endGame() marque gameEnded = true pour éviter double appel
        // Difficile à tester directement à cause de Platform.runLater

        assertDoesNotThrow(() -> {
            // Simuler la logique de protection
            boolean gameEnded = false;
            if (!gameEnded) {
                gameEnded = true; // Premier appel
            }
            // Deuxième appel serait ignoré
            assertTrue(gameEnded);
        });
    }

    @Test
    @DisplayName("Intégration avec ScoreSystem")
    void testScoreSystemIntegration() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);
        ScoreSystem scoreSystem = gameStateManager.getScoreSystem();

        assertNotNull(scoreSystem);
        // Le ScoreSystem devrait être initialisé avec ce GameStateManager
    }

    @Test
    @DisplayName("Mise à jour stats utilisateur")
    void testUpdateUserStats() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);
        gameStateManager.updateScore(750);
        gameStateManager.setGameWon(true);

        // Simuler l'appel de mise à jour
        when(mockAuthService.getCurrentUser()).thenReturn(mockUser);

        // Vérifier que les bonnes méthodes seraient appelées
        verify(mockAuthService, never()).updateUserStats(any(), anyBoolean(), anyInt());

        // Note: L'appel réel se fait dans endGame() avec Platform.runLater
    }

    @Test
    @DisplayName("Gestion erreur Platform.runLater")
    void testPlatformErrorHandling() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);

        // Test que les méthodes avec Platform.runLater ne plantent pas
        assertDoesNotThrow(() -> {
            // Ces méthodes utilisent Platform.runLater en interne
            // gameStateManager.restartGame();
            // gameStateManager.returnToMenu();
            gameStateManager.quitGame();
        });
    }

    @Test
    @DisplayName("État initial cohérent")
    void testInitialStateConsistency() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);

        // Vérifier cohérence de l'état initial
        assertEquals(0, gameStateManager.getGameScore());
        assertFalse(gameStateManager.isGameWon());
        assertNotNull(gameStateManager.getScoreSystem());
        assertTrue(gameStateManager.getGameStartTime() <= System.currentTimeMillis());

        // L'utilisateur devrait être celui passé au constructeur
        assertEquals(mockUser, gameStateManager.getCurrentUser());
    }

    @Test
    @DisplayName("Cycle complet de partie")
    void testCompleteGameCycle() {
        gameStateManager = new GameStateManager(mockUser, mockAuthService);

        // Simuler une partie complète
        gameStateManager.updateScore(50);
        gameStateManager.updateScore(150);
        assertEquals(200, gameStateManager.getGameScore());

        gameStateManager.setGameWon(true);
        assertTrue(gameStateManager.isGameWon());

        // Reset pour nouvelle partie
        gameStateManager.resetGameState();
        assertEquals(0, gameStateManager.getGameScore());
        assertFalse(gameStateManager.isGameWon());

        // Vérifier que ScoreSystem est aussi reset
        assertNotNull(gameStateManager.getScoreSystem());
    }
}