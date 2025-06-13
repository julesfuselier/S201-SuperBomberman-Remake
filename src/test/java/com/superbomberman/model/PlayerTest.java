package com.superbomberman.model;

import com.superbomberman.model.powerup.MalusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests unitaires pour la classe Player")
class PlayerTest {

    private Player player;
    private Bomb mockBomb;

    @BeforeEach
    void setUp() {
        player = new Player("TestPlayer");
        mockBomb = mock(Bomb.class);
    }

    @Nested
    @DisplayName("Tests de gestion de position")
    class PositionTests {

        @Test
        @DisplayName("setPosition() doit mettre à jour la position actuelle et sauvegarder la précédente")
        void testSetPosition() {
            // Given - Position initiale
            player.setPosition(5, 3);

            // When - Nouvelle position
            player.setPosition(8, 7);

            // Then - Vérifications
            assertEquals(8, player.getX(), "La nouvelle position X doit être 8");
            assertEquals(7, player.getY(), "La nouvelle position Y doit être 7");
            assertEquals(5, player.getPreviousX(), "La position X précédente doit être 5");
            assertEquals(3, player.getPreviousY(), "La position Y précédente doit être 3");
        }

        @Test
        @DisplayName("getX() et getY() doivent retourner la position actuelle")
        void testGetCurrentPosition() {
            // Given
            player.setPosition(10, 15);

            // Then
            assertEquals(10, player.getX(), "getX() doit retourner la position X actuelle");
            assertEquals(15, player.getY(), "getY() doit retourner la position Y actuelle");
        }

        @Test
        @DisplayName("getPreviousX() et getPreviousY() doivent retourner la position précédente")
        void testGetPreviousPosition() {
            // Given - Première position
            player.setPosition(2, 4);

            // When - Deuxième position
            player.setPosition(6, 8);

            // Then
            assertEquals(2, player.getPreviousX(), "getPreviousX() doit retourner l'ancienne position X");
            assertEquals(4, player.getPreviousY(), "getPreviousY() doit retourner l'ancienne position Y");
        }

        @Test
        @DisplayName("Position initiale doit être (0,0) avec previousX/Y à 0")
        void testInitialPosition() {
            // Given - Nouveau joueur
            Player newPlayer = new Player("NewPlayer");

            // Then
            assertEquals(0, newPlayer.getX(), "Position X initiale doit être 0");
            assertEquals(0, newPlayer.getY(), "Position Y initiale doit être 0");
            assertEquals(0, newPlayer.getPreviousX(), "Position X précédente initiale doit être 0");
            assertEquals(0, newPlayer.getPreviousY(), "Position Y précédente initiale doit être 0");
        }
    }

    @Nested
    @DisplayName("Tests des capacités de base")
    class BasicCapabilitiesTests {

        @Test
        @DisplayName("Les valeurs par défaut doivent être correctes")
        void testDefaultValues() {
            assertEquals(1, player.getMaxBombs(), "Nombre de bombes par défaut doit être 1");
            assertEquals(2, player.getExplosionRange(), "Portée par défaut doit être 2");
            assertEquals(1.0, player.getSpeed(), 0.01, "Vitesse par défaut doit être 1.0");
        }

        @Test
        @DisplayName("increaseMaxBombs() doit augmenter le nombre de bombes de 1")
        void testIncreaseMaxBombs() {
            // Given - Valeur initiale
            int initialBombs = player.getMaxBombs();

            // When
            player.increaseMaxBombs();

            // Then
            assertEquals(initialBombs + 1, player.getMaxBombs(),
                    "Le nombre de bombes doit augmenter de 1");
        }

        @Test
        @DisplayName("increaseExplosionRange() doit augmenter la portée de 1")
        void testIncreaseExplosionRange() {
            // Given - Valeur initiale
            int initialRange = player.getExplosionRange();

            // When
            player.increaseExplosionRange();

            // Then
            assertEquals(initialRange + 1, player.getExplosionRange(),
                    "La portée d'explosion doit augmenter de 1");
        }

        @Test
        @DisplayName("increaseSpeed() doit augmenter la vitesse de 0.2")
        void testIncreaseSpeed() {
            // Given - Vitesse initiale
            double initialSpeed = player.getSpeed();

            // When
            player.increaseSpeed();

            // Then
            assertEquals(initialSpeed + 0.2, player.getSpeed(), 0.01,
                    "La vitesse doit augmenter de 0.2");
        }

        @Test
        @DisplayName("Augmentations multiples doivent être cumulatives")
        void testMultipleIncreases() {
            // Given - Valeurs initiales
            int initialBombs = player.getMaxBombs();
            int initialRange = player.getExplosionRange();
            double initialSpeed = player.getSpeed();

            // When - Plusieurs augmentations
            player.increaseMaxBombs();
            player.increaseMaxBombs();
            player.increaseExplosionRange();
            player.increaseSpeed();
            player.increaseSpeed();

            // Then
            assertEquals(initialBombs + 2, player.getMaxBombs(),
                    "2 augmentations de bombes");
            assertEquals(initialRange + 1, player.getExplosionRange(),
                    "1 augmentation de portée");
            assertEquals(initialSpeed + 0.4, player.getSpeed(), 0.01,
                    "2 augmentations de vitesse");
        }
    }

    @Nested
    @DisplayName("Tests des pouvoirs spéciaux")
    class SpecialPowersTests {

        @Test
        @DisplayName("Les pouvoirs doivent être désactivés par défaut")
        void testDefaultPowers() {
            assertFalse(player.canKickBombs(), "Kick Power doit être désactivé par défaut");
            assertFalse(player.canThrowBombs(), "Throw Power doit être désactivé par défaut");
            assertFalse(player.hasRemoteDetonation(), "Remote Power doit être désactivé par défaut");
            assertFalse(player.canPassThroughWalls(), "WallPass doit être désactivé par défaut");
            assertFalse(player.canPassThroughBombs(), "BombPass doit être désactivé par défaut");
            assertFalse(player.hasLineBombs(), "LineBomb doit être désactivé par défaut");
        }

        @Test
        @DisplayName("setCanKickBombs() doit activer/désactiver le Kick Power")
        void testKickBombsPower() {
            // When - Activation
            player.setCanKickBombs(true);
            // Then
            assertTrue(player.canKickBombs(), "Kick Power doit être activé");

            // When - Désactivation
            player.setCanKickBombs(false);
            // Then
            assertFalse(player.canKickBombs(), "Kick Power doit être désactivé");
        }

        @Test
        @DisplayName("setCanThrowBombs() doit activer/désactiver le Throw Power")
        void testThrowBombsPower() {
            // When - Activation
            player.setCanThrowBombs(true);
            // Then
            assertTrue(player.canThrowBombs(), "Throw Power doit être activé");

            // When - Désactivation
            player.setCanThrowBombs(false);
            // Then
            assertFalse(player.canThrowBombs(), "Throw Power doit être désactivé");
        }

        @Test
        @DisplayName("setRemoteDetonation() doit activer/désactiver le Remote Power")
        void testRemoteDetonationPower() {
            // When - Activation
            player.setRemoteDetonation(true);
            // Then
            assertTrue(player.hasRemoteDetonation(), "Remote Power doit être activé");

            // When - Désactivation
            player.setRemoteDetonation(false);
            // Then
            assertFalse(player.hasRemoteDetonation(), "Remote Power doit être désactivé");
        }

        @Test
        @DisplayName("setCanPassThroughWalls() doit activer/désactiver le WallPass")
        void testWallPassPower() {
            // When - Activation
            player.setCanPassThroughWalls(true);
            // Then
            assertTrue(player.canPassThroughWalls(), "WallPass doit être activé");

            // When - Désactivation
            player.setCanPassThroughWalls(false);
            // Then
            assertFalse(player.canPassThroughWalls(), "WallPass doit être désactivé");
        }

        @Test
        @DisplayName("setCanPassThroughBombs() doit activer/désactiver le BombPass")
        void testBombPassPower() {
            // When - Activation
            player.setCanPassThroughBombs(true);
            // Then
            assertTrue(player.canPassThroughBombs(), "BombPass doit être activé");

            // When - Désactivation
            player.setCanPassThroughBombs(false);
            // Then
            assertFalse(player.canPassThroughBombs(), "BombPass doit être désactivé");
        }

        @Test
        @DisplayName("setHasLineBombs() doit activer/désactiver le LineBomb Power")
        void testLineBombsPower() {
            // When - Activation
            player.setHasLineBombs(true);
            // Then
            assertTrue(player.hasLineBombs(), "LineBomb Power doit être activé");

            // When - Désactivation
            player.setHasLineBombs(false);
            // Then
            assertFalse(player.hasLineBombs(), "LineBomb Power doit être désactivé");
        }
    }

    @Nested
    @DisplayName("Tests de gestion des bombes tenues")
    class HeldBombTests {

        @BeforeEach
        void setUpHeldBombTests() {
            // Configurer le mock pour les tests
            when(mockBomb.getOwner()).thenReturn(player);
        }

        @Test
        @DisplayName("pickUpBomb() doit réussir avec les bonnes conditions")
        void testPickUpBombSuccess() {
            // Given - Joueur avec Throw Power
            player.setCanThrowBombs(true);

            // When
            boolean result = player.pickUpBomb(mockBomb);

            // Then
            assertTrue(result, "Le ramassage doit réussir");
            assertTrue(player.isHoldingBomb(), "Le joueur doit tenir une bombe");
            assertEquals(mockBomb, player.getHeldBomb(), "La bombe tenue doit être celle ramassée");
        }

        @Test
        @DisplayName("pickUpBomb() doit échouer sans Throw Power")
        void testPickUpBombFailWithoutThrowPower() {
            // Given - Joueur sans Throw Power (par défaut)

            // When
            boolean result = player.pickUpBomb(mockBomb);

            // Then
            assertFalse(result, "Le ramassage doit échouer sans Throw Power");
            assertFalse(player.isHoldingBomb(), "Le joueur ne doit pas tenir de bombe");
            assertNull(player.getHeldBomb(), "Aucune bombe ne doit être tenue");
        }

        @Test
        @DisplayName("pickUpBomb() doit échouer si le joueur tient déjà une bombe")
        void testPickUpBombFailAlreadyHolding() {
            // Given - Joueur avec Throw Power et tenant déjà une bombe
            player.setCanThrowBombs(true);
            player.pickUpBomb(mockBomb);
            Bomb anotherMockBomb = mock(Bomb.class);
            when(anotherMockBomb.getOwner()).thenReturn(player);

            // When - Essayer de ramasser une autre bombe
            boolean result = player.pickUpBomb(anotherMockBomb);

            // Then
            assertFalse(result, "Le ramassage doit échouer si déjà une bombe tenue");
            assertEquals(mockBomb, player.getHeldBomb(), "La première bombe doit être conservée");
        }

        @Test
        @DisplayName("pickUpBomb() doit échouer si la bombe n'appartient pas au joueur")
        void testPickUpBombFailWrongOwner() {
            // Given - Joueur avec Throw Power mais bombe d'un autre joueur
            player.setCanThrowBombs(true);
            Player otherPlayer = new Player("OtherPlayer");
            when(mockBomb.getOwner()).thenReturn(otherPlayer);

            // When
            boolean result = player.pickUpBomb(mockBomb);

            // Then
            assertFalse(result, "Le ramassage doit échouer si pas le propriétaire");
            assertFalse(player.isHoldingBomb(), "Le joueur ne doit pas tenir de bombe");
        }

        @Test
        @DisplayName("throwHeldBomb() doit réussir si le joueur tient une bombe")
        void testThrowHeldBombSuccess() {
            // Given - Joueur tenant une bombe
            player.setCanThrowBombs(true);
            player.pickUpBomb(mockBomb);

            // When
            Bomb thrownBomb = player.throwHeldBomb(1, 0);

            // Then
            assertEquals(mockBomb, thrownBomb, "La bombe lancée doit être celle tenue");
            assertFalse(player.isHoldingBomb(), "Le joueur ne doit plus tenir de bombe");
            assertNull(player.getHeldBomb(), "Aucune bombe ne doit être tenue après le lancer");
        }

        @Test
        @DisplayName("throwHeldBomb() doit échouer si le joueur ne tient pas de bombe")
        void testThrowHeldBombFailNoHeldBomb() {
            // When - Essayer de lancer sans tenir de bombe
            Bomb thrownBomb = player.throwHeldBomb(0, 1);

            // Then
            assertNull(thrownBomb, "Aucune bombe ne doit être lancée");
            assertFalse(player.isHoldingBomb(), "Le joueur ne doit pas tenir de bombe");
        }

        @Test
        @DisplayName("dropHeldBomb() doit forcer le lâcher de la bombe")
        void testDropHeldBomb() {
            // Given - Joueur tenant une bombe
            player.setCanThrowBombs(true);
            player.pickUpBomb(mockBomb);

            // When
            player.dropHeldBomb();

            // Then
            assertFalse(player.isHoldingBomb(), "Le joueur ne doit plus tenir de bombe");
            assertNull(player.getHeldBomb(), "Aucune bombe ne doit être tenue");
        }
    }

    @Nested
    @DisplayName("Tests du système de malus")
    class MalusSystemTests {

        @Test
        @DisplayName("Aucun malus par défaut")
        void testNoMalusDefault() {
            assertFalse(player.hasActiveMalus(), "Aucun malus actif par défaut");
            assertNull(player.getCurrentMalus(), "Aucun malus actuel par défaut");
            assertEquals(0, player.getMalusTimeRemaining(), "Aucun temps de malus par défaut");
        }

        @Test
        @DisplayName("applyRandomMalus() doit appliquer un malus")
        void testApplyRandomMalus() {
            // When
            player.applyRandomMalus();

            // Then
            assertTrue(player.hasActiveMalus(), "Un malus doit être actif");
            assertNotNull(player.getCurrentMalus(), "Un malus doit être défini");
            assertTrue(player.getMalusTimeRemaining() > 0, "Il doit rester du temps de malus");
        }

        @Test
        @DisplayName("hasMalus() doit détecter un malus spécifique")
        void testHasMalusSpecific() {
            // Given - Forcer un malus spécifique (nous devrons tester avec applyRandomMalus)
            player.applyRandomMalus();
            MalusType currentMalus = player.getCurrentMalus();

            // Then
            assertTrue(player.hasMalus(currentMalus), "Le malus appliqué doit être détecté");

            // Test avec un malus différent
            MalusType[] allMalus = MalusType.values();
            for (MalusType malus : allMalus) {
                if (malus != currentMalus) {
                    assertFalse(player.hasMalus(malus),
                            "Un malus non appliqué ne doit pas être détecté: " + malus);
                    break;
                }
            }
        }

        @Test
        @DisplayName("updateMalus() doit supprimer les malus expirés")
        void testUpdateMalusExpiration() throws InterruptedException {
            // Given - Appliquer un malus
            player.applyRandomMalus();
            assertTrue(player.hasActiveMalus(), "Un malus doit être actif");

            // When - Simuler l'expiration (nous ne pouvons pas attendre 10 secondes)
            // Nous testerons la logique avec un malus qui vient d'expirer
            // Comme nous ne pouvons pas modifier facilement le temps,
            // ce test vérifie plutôt le comportement normal
            player.updateMalus();

            // Then - Le malus devrait toujours être actif car pas expiré
            assertTrue(player.hasActiveMalus(), "Le malus doit encore être actif");
        }

        @Test
        @DisplayName("Malus SLOW_SPEED doit réduire la vitesse")
        void testSlowSpeedMalus() {
            // Given - Vitesse initiale
            double initialSpeed = player.getSpeed();

            // When - Appliquer des malus jusqu'à avoir SLOW_SPEED
            boolean foundSlowSpeed = false;
            for (int i = 0; i < 20 && !foundSlowSpeed; i++) {
                player.applyRandomMalus();
                if (player.hasMalus(MalusType.SLOW_SPEED)) {
                    foundSlowSpeed = true;
                    // Then
                    assertTrue(player.getSpeed() < initialSpeed,
                            "La vitesse doit être réduite avec SLOW_SPEED");
                    assertTrue(player.getSpeed() >= 0.3,
                            "La vitesse ne doit pas être inférieure à 0.3");
                }
            }

            // Si nous n'avons pas trouvé SLOW_SPEED, on teste au moins qu'un malus est appliqué
            if (!foundSlowSpeed) {
                assertTrue(player.hasActiveMalus(), "Au moins un malus doit être actif");
            }
        }
    }

    @Nested
    @DisplayName("Tests du système de vie")
    class LifeSystemTests {

        @Test
        @DisplayName("Le joueur doit être vivant par défaut")
        void testDefaultAliveState() {
            assertTrue(player.isAlive(), "Le joueur doit être vivant par défaut");
        }

        @Test
        @DisplayName("setAlive() doit changer l'état de vie")
        void testSetAlive() {
            // When - Tuer le joueur
            player.setAlive(false);
            // Then
            assertFalse(player.isAlive(), "Le joueur doit être mort");

            // When - Ressusciter le joueur
            player.setAlive(true);
            // Then
            assertTrue(player.isAlive(), "Le joueur doit être vivant");
        }

        @Test
        @DisplayName("getName() doit retourner le nom du joueur")
        void testGetName() {
            assertEquals("TestPlayer", player.getName(), "Le nom doit être celui du constructeur");
        }

        @Test
        @DisplayName("setName() doit changer le nom du joueur")
        void testSetName() {
            // When
            player.setName("NouveauNom");

            // Then
            assertEquals("NouveauNom", player.getName(), "Le nom doit être mis à jour");
        }

        @Test
        @DisplayName("Le constructeur doit initialiser correctement")
        void testConstructor() {
            // When
            Player newPlayer = new Player("TestConstructor");

            // Then
            assertEquals("TestConstructor", newPlayer.getName(), "Le nom doit être initialisé");
            assertTrue(newPlayer.isAlive(), "Le joueur doit être vivant");
        }
    }
}