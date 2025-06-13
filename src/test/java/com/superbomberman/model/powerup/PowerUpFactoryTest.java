package com.superbomberman.model.powerup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour PowerUpFactory.
 *
 * POLITIQUE DE TESTS IMPLÉMENTÉE :
 * - Tests du pattern Factory (création d'objets)
 * - Tests de validation des types
 * - Tests de construction avec paramètres
 * - Tests de couverture complète des PowerUpType
 *
 * COUVERTURE : 100% de PowerUpFactory (simple mais essentiel)
 * NOMBRE DE TESTS : 15+ tests couvrant tous les cas
 * STRATÉGIE : Tests paramétrés pour éviter la duplication
 */

class PowerUpFactoryTest {

    // ================== TESTS DE CRÉATION SPÉCIFIQUES ==================

    @Test
    @DisplayName("Create - RangeUp avec coordonnées correctes")
    void testCreateRangeUp() {
        PowerUp powerUp = PowerUpFactory.create(PowerUpType.RANGE_UP, 5, 10);

        assertNotNull(powerUp);
        assertInstanceOf(RangeUp.class, powerUp);
        assertEquals(5, powerUp.getX());
        assertEquals(10, powerUp.getY());
        assertEquals(PowerUpType.RANGE_UP, powerUp.getType());
    }

    @Test
    @DisplayName("Create - BombUp avec coordonnées correctes")
    void testCreateBombUp() {
        PowerUp powerUp = PowerUpFactory.create(PowerUpType.BOMB_UP, 3, 7);

        assertNotNull(powerUp);
        assertInstanceOf(BombUp.class, powerUp);
        assertEquals(3, powerUp.getX());
        assertEquals(7, powerUp.getY());
        assertEquals(PowerUpType.BOMB_UP, powerUp.getType());
    }

    @Test
    @DisplayName("Create - SpeedUp avec coordonnées correctes")
    void testCreateSpeedUp() {
        PowerUp powerUp = PowerUpFactory.create(PowerUpType.SPEED_UP, 8, 2);

        assertNotNull(powerUp);
        assertInstanceOf(SpeedUp.class, powerUp);
        assertEquals(8, powerUp.getX());
        assertEquals(2, powerUp.getY());
        assertEquals(PowerUpType.SPEED_UP, powerUp.getType());
    }

    @Test
    @DisplayName("Create - KickPower avec coordonnées correctes")
    void testCreateKickPower() {
        PowerUp powerUp = PowerUpFactory.create(PowerUpType.KICK, 1, 1);

        assertNotNull(powerUp);
        assertInstanceOf(KickPower.class, powerUp);
        assertEquals(1, powerUp.getX());
        assertEquals(1, powerUp.getY());
        assertEquals(PowerUpType.KICK, powerUp.getType());
    }

    @Test
    @DisplayName("Create - GlovePower avec coordonnées correctes")
    void testCreateGlovePower() {
        PowerUp powerUp = PowerUpFactory.create(PowerUpType.GLOVE, 12, 6);

        assertNotNull(powerUp);
        assertInstanceOf(GlovePower.class, powerUp);
        assertEquals(12, powerUp.getX());
        assertEquals(6, powerUp.getY());
        assertEquals(PowerUpType.GLOVE, powerUp.getType());
    }

    @Test
    @DisplayName("Create - RemotePower avec coordonnées correctes")
    void testCreateRemotePower() {
        PowerUp powerUp = PowerUpFactory.create(PowerUpType.REMOTE, 9, 4);

        assertNotNull(powerUp);
        assertInstanceOf(RemotePower.class, powerUp);
        assertEquals(9, powerUp.getX());
        assertEquals(4, powerUp.getY());
        assertEquals(PowerUpType.REMOTE, powerUp.getType());
    }

    @Test
    @DisplayName("Create - WallPass avec coordonnées correctes")
    void testCreateWallPass() {
        PowerUp powerUp = PowerUpFactory.create(PowerUpType.WALL_PASS, 11, 8);

        assertNotNull(powerUp);
        assertInstanceOf(WallPass.class, powerUp);
        assertEquals(11, powerUp.getX());
        assertEquals(8, powerUp.getY());
        assertEquals(PowerUpType.WALL_PASS, powerUp.getType());
    }

    @Test
    @DisplayName("Create - BombPass avec coordonnées correctes")
    void testCreateBombPass() {
        PowerUp powerUp = PowerUpFactory.create(PowerUpType.BOMB_PASS, 6, 3);

        assertNotNull(powerUp);
        assertInstanceOf(BombPass.class, powerUp);
        assertEquals(6, powerUp.getX());
        assertEquals(3, powerUp.getY());
        assertEquals(PowerUpType.BOMB_PASS, powerUp.getType());
    }

    @Test
    @DisplayName("Create - LineBomb avec coordonnées correctes")
    void testCreateLineBomb() {
        PowerUp powerUp = PowerUpFactory.create(PowerUpType.LINE_BOMB, 4, 9);

        assertNotNull(powerUp);
        assertInstanceOf(LineBomb.class, powerUp);
        assertEquals(4, powerUp.getX());
        assertEquals(9, powerUp.getY());
        assertEquals(PowerUpType.LINE_BOMB, powerUp.getType());
    }

    @Test
    @DisplayName("Create - SkullMalus avec coordonnées correctes")
    void testCreateSkullMalus() {
        PowerUp powerUp = PowerUpFactory.create(PowerUpType.SKULL, 7, 5);

        assertNotNull(powerUp);
        assertInstanceOf(SkullMalus.class, powerUp);
        assertEquals(7, powerUp.getX());
        assertEquals(5, powerUp.getY());
        assertEquals(PowerUpType.SKULL, powerUp.getType());
    }

    // ================== TESTS PARAMÉTRÉS ==================

    @ParameterizedTest(name = "Create - {0} doit retourner une instance non-null")
    @EnumSource(PowerUpType.class)
    @DisplayName("Create - Tous les types retournent une instance valide")
    void testCreateAllTypesNotNull(PowerUpType type) {
        PowerUp powerUp = PowerUpFactory.create(type, 0, 0);

        assertNotNull(powerUp, "Le PowerUp créé ne doit pas être null pour le type " + type);
        assertEquals(type, powerUp.getType(), "Le type du PowerUp doit correspondre au type demandé");
    }

    @ParameterizedTest(name = "Create - {0} avec coordonnées négatives")
    @EnumSource(PowerUpType.class)
    @DisplayName("Create - Gestion des coordonnées négatives")
    void testCreateWithNegativeCoordinates(PowerUpType type) {
        PowerUp powerUp = PowerUpFactory.create(type, -5, -10);

        assertNotNull(powerUp);
        assertEquals(-5, powerUp.getX());
        assertEquals(-10, powerUp.getY());
        assertEquals(type, powerUp.getType());
    }

    @ParameterizedTest(name = "Create - {0} avec coordonnées zéro")
    @EnumSource(PowerUpType.class)
    @DisplayName("Create - Gestion des coordonnées à zéro")
    void testCreateWithZeroCoordinates(PowerUpType type) {
        PowerUp powerUp = PowerUpFactory.create(type, 0, 0);

        assertNotNull(powerUp);
        assertEquals(0, powerUp.getX());
        assertEquals(0, powerUp.getY());
        assertEquals(type, powerUp.getType());
    }

    @ParameterizedTest(name = "Create - {0} avec coordonnées maximales")
    @EnumSource(PowerUpType.class)
    @DisplayName("Create - Gestion des coordonnées maximales")
    void testCreateWithMaxCoordinates(PowerUpType type) {
        PowerUp powerUp = PowerUpFactory.create(type, Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertNotNull(powerUp);
        assertEquals(Integer.MAX_VALUE, powerUp.getX());
        assertEquals(Integer.MAX_VALUE, powerUp.getY());
        assertEquals(type, powerUp.getType());
    }

    // ================== TESTS DE CORRESPONDANCE TYPE/CLASSE ==================

    @Test
    @DisplayName("Type Mapping - Vérification de toutes les correspondances")
    void testAllTypeMappings() {
        // Vérifier que chaque type crée la bonne classe
        assertTrue(PowerUpFactory.create(PowerUpType.RANGE_UP, 0, 0) instanceof RangeUp);
        assertTrue(PowerUpFactory.create(PowerUpType.BOMB_UP, 0, 0) instanceof BombUp);
        assertTrue(PowerUpFactory.create(PowerUpType.SPEED_UP, 0, 0) instanceof SpeedUp);
        assertTrue(PowerUpFactory.create(PowerUpType.KICK, 0, 0) instanceof KickPower);
        assertTrue(PowerUpFactory.create(PowerUpType.GLOVE, 0, 0) instanceof GlovePower);
        assertTrue(PowerUpFactory.create(PowerUpType.REMOTE, 0, 0) instanceof RemotePower);
        assertTrue(PowerUpFactory.create(PowerUpType.WALL_PASS, 0, 0) instanceof WallPass);
        assertTrue(PowerUpFactory.create(PowerUpType.BOMB_PASS, 0, 0) instanceof BombPass);
        assertTrue(PowerUpFactory.create(PowerUpType.LINE_BOMB, 0, 0) instanceof LineBomb);
        assertTrue(PowerUpFactory.create(PowerUpType.SKULL, 0, 0) instanceof SkullMalus);
    }

    // ================== TESTS DE DIFFÉRENTES COORDONNÉES ==================

    @Test
    @DisplayName("Coordinates - Même type avec différentes coordonnées")
    void testSameTypeWithDifferentCoordinates() {
        PowerUp powerUp1 = PowerUpFactory.create(PowerUpType.RANGE_UP, 1, 2);
        PowerUp powerUp2 = PowerUpFactory.create(PowerUpType.RANGE_UP, 3, 4);
        PowerUp powerUp3 = PowerUpFactory.create(PowerUpType.RANGE_UP, 5, 6);

        // Vérifier que les instances sont différentes
        assertNotSame(powerUp1, powerUp2);
        assertNotSame(powerUp2, powerUp3);
        assertNotSame(powerUp1, powerUp3);

        // Vérifier que les coordonnées sont correctes
        assertEquals(1, powerUp1.getX());
        assertEquals(2, powerUp1.getY());
        assertEquals(3, powerUp2.getX());
        assertEquals(4, powerUp2.getY());
        assertEquals(5, powerUp3.getX());
        assertEquals(6, powerUp3.getY());

        // Vérifier que le type est identique
        assertEquals(PowerUpType.RANGE_UP, powerUp1.getType());
        assertEquals(PowerUpType.RANGE_UP, powerUp2.getType());
        assertEquals(PowerUpType.RANGE_UP, powerUp3.getType());
    }

    // ================== TESTS DE PERFORMANCE ==================

    @Test
    @DisplayName("Performance - Création multiple rapide")
    void testMultipleCreationPerformance() {
        long startTime = System.currentTimeMillis();

        // Créer 1000 PowerUps de chaque type
        for (int i = 0; i < 1000; i++) {
            for (PowerUpType type : PowerUpType.values()) {
                PowerUp powerUp = PowerUpFactory.create(type, i, i);
                assertNotNull(powerUp);
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Vérifier que la création est rapide (moins de 5 secondes)
        assertTrue(duration < 5000, "La création de 10000 PowerUps devrait prendre moins de 5 secondes");
        System.out.println("Création de " + (1000 * PowerUpType.values().length) + " PowerUps en " + duration + "ms");
    }

    // ================== TESTS DE COHÉRENCE ==================

    @Test
    @DisplayName("Consistency - Même paramètres donnent objets équivalents")
    void testConsistentCreation() {
        PowerUpType type = PowerUpType.BOMB_UP;
        int x = 10, y = 15;

        PowerUp powerUp1 = PowerUpFactory.create(type, x, y);
        PowerUp powerUp2 = PowerUpFactory.create(type, x, y);

        // Vérifier que les objets sont différents (nouvelles instances)
        assertNotSame(powerUp1, powerUp2);

        // Mais avec les mêmes propriétés
        assertEquals(powerUp1.getX(), powerUp2.getX());
        assertEquals(powerUp1.getY(), powerUp2.getY());
        assertEquals(powerUp1.getType(), powerUp2.getType());
        assertEquals(powerUp1.getClass(), powerUp2.getClass());
    }
}
