package com.superbomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe Bomb
 *
 * Cette classe de test couvre :
 * - Construction et initialisation
 * - Gestion du timer d'explosion
 * - Fonctionnalités de mouvement (vol et glissement)
 * - États et propriétés de la bombe
 * - Interactions avec le propriétaire
 *
 * @author Jules Fuselier
 */

@DisplayName("Tests de la classe Bomb")
public class BombTest {

    private Bomb bomb;
    private Player mockPlayer;
    private Runnable mockCallback;

    @BeforeEach
    void setUp() {
        // Création des mocks
        mockPlayer = mock(Player.class);
        mockCallback = mock(Runnable.class);

        // Bombe standard pour les tests
        bomb = new Bomb(5, 3, 1, 2);
    }

    @Nested
    @DisplayName("Tests de construction et initialisation")
    class ConstructionTests {

        @Test
        @DisplayName("Doit créer une bombe avec les bonnes propriétés initiales")
        void shouldCreateBombWithCorrectInitialProperties() {
            // Given - nouvelle bombe
            Bomb testBomb = new Bomb(10, 8, 2, 3);

            // Then - vérifier les propriétés
            assertEquals(10, testBomb.getX());
            assertEquals(8, testBomb.getY());
            assertEquals(10, testBomb.getPreviousX());
            assertEquals(8, testBomb.getPreviousY());
            assertEquals(2, testBomb.getDamage());
            assertEquals(3, testBomb.getRange());
            assertNull(testBomb.getOwner());
            assertFalse(testBomb.hasExploded());
            assertFalse(testBomb.isFlying());
            assertFalse(testBomb.isMoving());
        }

        @Test
        @DisplayName("Doit accepter des coordonnées nulles ou négatives")
        void shouldAcceptNullOrNegativeCoordinates() {
            // Given & When
            Bomb testBomb = new Bomb(-1, 0, 1, 1);

            // Then
            assertEquals(-1, testBomb.getX());
            assertEquals(0, testBomb.getY());
        }

        @Test
        @DisplayName("Doit gérer les valeurs limites pour damage et range")
        void shouldHandleBoundaryValuesForDamageAndRange() {
            // Given & When
            Bomb minBomb = new Bomb(0, 0, 0, 0);
            Bomb maxBomb = new Bomb(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);

            // Then
            assertEquals(0, minBomb.getDamage());
            assertEquals(0, minBomb.getRange());
            assertEquals(Integer.MAX_VALUE, maxBomb.getDamage());
            assertEquals(Integer.MAX_VALUE, maxBomb.getRange());
        }
    }

    @Nested
    @DisplayName("Tests de gestion du propriétaire")
    class OwnerTests {

        @Test
        @DisplayName("Doit pouvoir définir et récupérer le propriétaire")
        void shouldSetAndGetOwner() {
            // Given
            when(mockPlayer.getName()).thenReturn("TestPlayer");

            // When
            bomb.setOwner(mockPlayer);

            // Then
            assertEquals(mockPlayer, bomb.getOwner());
        }

        @Test
        @DisplayName("Doit accepter un propriétaire null")
        void shouldAcceptNullOwner() {
            // When
            bomb.setOwner(null);

            // Then
            assertNull(bomb.getOwner());
        }

        @Test
        @DisplayName("Doit pouvoir changer de propriétaire")
        void shouldAllowOwnerChange() {
            // Given
            Player anotherPlayer = mock(Player.class);
            bomb.setOwner(mockPlayer);

            // When
            bomb.setOwner(anotherPlayer);

            // Then
            assertEquals(anotherPlayer, bomb.getOwner());
        }
    }

    @Nested
    @DisplayName("Tests de gestion de position")
    class PositionTests {

        @Test
        @DisplayName("Doit mettre à jour la position et garder l'ancienne")
        void shouldUpdatePositionAndKeepPrevious() {
            // Given
            int initialX = bomb.getX();
            int initialY = bomb.getY();

            // When
            bomb.setPosition(15, 20);

            // Then
            assertEquals(15, bomb.getX());
            assertEquals(20, bomb.getY());
            assertEquals(initialX, bomb.getPreviousX());
            assertEquals(initialY, bomb.getPreviousY());
        }

        @Test
        @DisplayName("Doit gérer plusieurs changements de position consécutifs")
        void shouldHandleMultiplePositionChanges() {
            // Given & When
            bomb.setPosition(1, 1);
            bomb.setPosition(2, 2);
            bomb.setPosition(3, 3);

            // Then
            assertEquals(3, bomb.getX());
            assertEquals(3, bomb.getY());
            assertEquals(2, bomb.getPreviousX());
            assertEquals(2, bomb.getPreviousY());
        }
    }

    @Nested
    @DisplayName("Tests du timer d'explosion")
    class ExplosionTimerTests {

        @Test
        @DisplayName("Doit gérer l'arrêt d'un timer déjà arrêté")
        void shouldHandleStoppingAlreadyStoppedTimer() {
            // When & Then
            assertDoesNotThrow(() -> bomb.stopCountdown());
        }

    }

    @Nested
    @DisplayName("Tests de fonctionnalité de vol (Glove Power)")
    class FlyingTests {

        @Test
        @DisplayName("Doit gérer l'arrêt d'une bombe qui ne vole pas")
        void shouldHandleStoppingNonFlyingBomb() {
            // Given
            assertFalse(bomb.isFlying());

            // When & Then
            assertDoesNotThrow(() -> bomb.stopFlying());
            assertFalse(bomb.isFlying());
        }
    }

    @Nested
    @DisplayName("Tests de fonctionnalité de glissement (Kick Power)")
    class KickingTests {


        @Test
        @DisplayName("Doit gérer l'arrêt d'une bombe qui ne glisse pas")
        void shouldHandleStoppingNonMovingBomb() {
            // Given
            assertFalse(bomb.isMoving());

            // When & Then
            assertDoesNotThrow(() -> bomb.stopMoving());
            assertFalse(bomb.isMoving());
        }
    }

    @Nested
    @DisplayName("Tests d'intégration avec Player")
    class PlayerIntegrationTests {

        @Test
        @DisplayName("Doit être associée correctement à un joueur réel")
        void shouldAssociateCorrectlyWithRealPlayer() {
            // Given
            Player realPlayer = new Player("TestPlayer");

            // When
            bomb.setOwner(realPlayer);

            // Then
            assertEquals(realPlayer, bomb.getOwner());
            assertEquals("TestPlayer", bomb.getOwner().getName());
        }
    }
}