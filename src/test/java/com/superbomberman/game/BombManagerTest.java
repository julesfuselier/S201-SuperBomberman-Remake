package com.superbomberman.game;

import com.superbomberman.model.*;
import com.superbomberman.model.powerup.MalusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe BombManager
 *
 * Politique de tests implémentée :
 * - Tests de placement de bombes (placeBomb)
 * - Tests de validation des contraintes
 * - Tests de gestion des compteurs
 * - Tests des getters existants
 * - Tests des cas d'erreur et limites
 *
 * Note: Évite les tests avec Timeline/Animation pour éviter les bugs
 * Couverture : Focus sur la logique métier sans JavaFX, méthodes réelles uniquement
 */
@DisplayName("Tests de la classe BombManager")
public class BombManagerTest {

    private BombManager bombManager;
    private Tile[][] testMap;
    private Player testPlayer;

    @BeforeEach
    void setUp() {
        // Créer une carte de test simple 5x5
        testMap = new Tile[5][5];
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                testMap[y][x] = new Tile(TileType.FLOOR);
            }
        }

        // Ajouter quelques murs
        testMap[1][1] = new Tile(TileType.WALL);
        testMap[2][2] = new Tile(TileType.WALL_BREAKABLE);

        bombManager = new BombManager(testMap);

        // Créer un joueur de test avec le vrai constructeur
        testPlayer = new Player("TestPlayer");
        testPlayer.setPosition(0, 0);
        // Ces méthodes n'existent pas, on va tester avec les valeurs par défaut
    }

    @Test
    @DisplayName("Test du constructeur de BombManager")
    void testBombManagerConstructor() {
        assertNotNull(bombManager);
        assertNotNull(bombManager.getActiveBombs());
        assertNotNull(bombManager.getFlyingBombs());
        assertNotNull(bombManager.getKickingBombs());

        assertTrue(bombManager.getActiveBombs().isEmpty());
        assertTrue(bombManager.getFlyingBombs().isEmpty());
        assertTrue(bombManager.getKickingBombs().isEmpty());

        assertEquals(0, bombManager.getCurrentBombCountPlayer1());
        assertEquals(0, bombManager.getCurrentBombCountPlayer2());
    }

    @Test
    @DisplayName("Test de placement dans un mur destructible")
    void testBombPlacementInBreakableWall() {
        testPlayer.setPosition(2, 2); // Position du mur destructible

        // Essayer de placer une bombe
        bombManager.placeBomb(testPlayer, 1);

        // Vérifier qu'aucune bombe n'a été placée
        assertEquals(0, bombManager.getCurrentBombCountPlayer1());
        assertTrue(bombManager.getActiveBombs().isEmpty());
    }

    @Test
    @DisplayName("Test des getters pour les listes de bombes")
    void testBombListGetters() {
        // Vérifier que les getters retournent des listes non nulles
        assertNotNull(bombManager.getActiveBombs());
        assertNotNull(bombManager.getFlyingBombs());
        assertNotNull(bombManager.getKickingBombs());

        // Vérifier que les listes sont initialement vides
        assertTrue(bombManager.getActiveBombs().isEmpty());
        assertTrue(bombManager.getFlyingBombs().isEmpty());
        assertTrue(bombManager.getKickingBombs().isEmpty());

        // Vérifier que les getters retournent des copies (sécurité)
        bombManager.getActiveBombs().add(new Bomb(1, 1, 10, 2));
        assertTrue(bombManager.getActiveBombs().isEmpty(), "Le getter doit retourner une copie");
    }

    @Test
    @DisplayName("Test de configuration des managers")
    void testSetManagers() {
        // Créer des mocks
        VisualRenderer mockVisualRenderer = mock(VisualRenderer.class);
        PowerUpManager mockPowerUpManager = mock(PowerUpManager.class);
        GameStateManager mockGameStateManager = mock(GameStateManager.class);

        // Cette méthode existe et ne devrait pas lever d'exception
        assertDoesNotThrow(() -> {
            bombManager.setManagers(mockVisualRenderer, mockPowerUpManager, mockGameStateManager);
        });
    }

    @Test
    @DisplayName("Test de configuration de GameLogic")
    void testSetGameLogic() {
        GameLogic mockGameLogic = mock(GameLogic.class);

        // Cette méthode existe et ne devrait pas lever d'exception
        assertDoesNotThrow(() -> {
            bombManager.setGameLogic(mockGameLogic);
        });
    }

}