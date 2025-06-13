package com.superbomberman.model.powerup;

import com.superbomberman.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour tous les PowerUps
 *
 * Politique de tests :
 * - Test de chaque type de power-up individuellement
 * - Vérification des effets sur le joueur
 * - Tests de positions et propriétés
 * - Tests du système de factory
 *
 * Couverture : 100% des classes PowerUp
 */
class PowerUpTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("TestPlayer");
    }

    // ================ TESTS BOMBUP ================

    @Test
    @DisplayName("BombUp - Création et propriétés")
    void testBombUpCreation() {
        BombUp bombUp = new BombUp(5, 7);

        assertEquals(5, bombUp.getX());
        assertEquals(7, bombUp.getY());
        assertEquals(PowerUpType.BOMB_UP, bombUp.getType());
    }

    @Test
    @DisplayName("BombUp - Application de l'effet")
    void testBombUpEffect() {
        BombUp bombUp = new BombUp(5, 7);
        int initialMaxBombs = player.getMaxBombs();

        bombUp.applyTo(player);

        assertEquals(initialMaxBombs + 1, player.getMaxBombs());
    }

    // ================ TESTS RANGEUP ================

    @Test
    @DisplayName("RangeUp - Création et propriétés")
    void testRangeUpCreation() {
        RangeUp rangeUp = new RangeUp(3, 4);

        assertEquals(3, rangeUp.getX());
        assertEquals(4, rangeUp.getY());
        assertEquals(PowerUpType.RANGE_UP, rangeUp.getType());
    }

    @Test
    @DisplayName("RangeUp - Application de l'effet")
    void testRangeUpEffect() {
        RangeUp rangeUp = new RangeUp(3, 4);
        int initialRange = player.getExplosionRange();

        rangeUp.applyTo(player);

        assertEquals(initialRange + 1, player.getExplosionRange());
    }

    // ================ TESTS SPEEDUP ================

    @Test
    @DisplayName("SpeedUp - Création et propriétés")
    void testSpeedUpCreation() {
        SpeedUp speedUp = new SpeedUp(8, 9);

        assertEquals(8, speedUp.getX());
        assertEquals(9, speedUp.getY());
        assertEquals(PowerUpType.SPEED_UP, speedUp.getType());
    }

    @Test
    @DisplayName("SpeedUp - Application de l'effet")
    void testSpeedUpEffect() {
        SpeedUp speedUp = new SpeedUp(8, 9);
        double initialSpeed = player.getSpeed();

        speedUp.applyTo(player);

        assertEquals(initialSpeed + 0.2, player.getSpeed(), 0.01);
    }

    // ================ TESTS KICKPOWER ================

    @Test
    @DisplayName("KickPower - Création et propriétés")
    void testKickPowerCreation() {
        KickPower kickPower = new KickPower(2, 3);

        assertEquals(2, kickPower.getX());
        assertEquals(3, kickPower.getY());
        assertEquals(PowerUpType.KICK, kickPower.getType());
    }

    @Test
    @DisplayName("KickPower - Application de l'effet")
    void testKickPowerEffect() {
        KickPower kickPower = new KickPower(2, 3);

        assertFalse(player.canKickBombs());

        kickPower.applyTo(player);

        assertTrue(player.canKickBombs());
    }

    // ================ TESTS GLOVEPOWER ================

    @Test
    @DisplayName("GlovePower - Création et propriétés")
    void testGlovePowerCreation() {
        GlovePower glovePower = new GlovePower(6, 1);

        assertEquals(6, glovePower.getX());
        assertEquals(1, glovePower.getY());
        assertEquals(PowerUpType.GLOVE, glovePower.getType());
    }

    @Test
    @DisplayName("GlovePower - Application de l'effet")
    void testGlovePowerEffect() {
        GlovePower glovePower = new GlovePower(6, 1);

        assertFalse(player.canThrowBombs());

        glovePower.applyTo(player);

        assertTrue(player.canThrowBombs());
    }

    // ================ TESTS BOMBPASS ================

    @Test
    @DisplayName("BombPass - Création et propriétés")
    void testBombPassCreation() {
        BombPass bombPass = new BombPass(4, 5);

        assertEquals(4, bombPass.getX());
        assertEquals(5, bombPass.getY());
        assertEquals(PowerUpType.BOMB_PASS, bombPass.getType());
    }

    @Test
    @DisplayName("BombPass - Application de l'effet")
    void testBombPassEffect() {
        BombPass bombPass = new BombPass(4, 5);

        assertFalse(player.canPassThroughBombs());

        bombPass.applyTo(player);

        assertTrue(player.canPassThroughBombs());
    }

    // ================ TESTS WALLPASS ================

    @Test
    @DisplayName("WallPass - Création et propriétés")
    void testWallPassCreation() {
        WallPass wallPass = new WallPass(7, 8);

        assertEquals(7, wallPass.getX());
        assertEquals(8, wallPass.getY());
        assertEquals(PowerUpType.WALL_PASS, wallPass.getType());
    }

    @Test
    @DisplayName("WallPass - Application de l'effet")
    void testWallPassEffect() {
        WallPass wallPass = new WallPass(7, 8);

        assertFalse(player.canPassThroughWalls());

        wallPass.applyTo(player);

        assertTrue(player.canPassThroughWalls());
    }

    // ================ TESTS REMOTEPOWER ================

    @Test
    @DisplayName("RemotePower - Création et propriétés")
    void testRemotePowerCreation() {
        RemotePower remotePower = new RemotePower(1, 2);

        assertEquals(1, remotePower.getX());
        assertEquals(2, remotePower.getY());
        assertEquals(PowerUpType.REMOTE, remotePower.getType());
    }

    @Test
    @DisplayName("RemotePower - Application de l'effet")
    void testRemotePowerEffect() {
        RemotePower remotePower = new RemotePower(1, 2);

        assertFalse(player.hasRemoteDetonation());

        remotePower.applyTo(player);

        assertTrue(player.hasRemoteDetonation());
    }

    // ================ TESTS LINEBOMB ================

    @Test
    @DisplayName("LineBomb - Création et propriétés")
    void testLineBombCreation() {
        LineBomb lineBomb = new LineBomb(9, 0);

        assertEquals(9, lineBomb.getX());
        assertEquals(0, lineBomb.getY());
        assertEquals(PowerUpType.LINE_BOMB, lineBomb.getType());
    }

    @Test
    @DisplayName("LineBomb - Application de l'effet")
    void testLineBombEffect() {
        LineBomb lineBomb = new LineBomb(9, 0);

        assertFalse(player.hasLineBombs());

        lineBomb.applyTo(player);

        assertTrue(player.hasLineBombs());
    }

    // ================ TESTS DE FACTORY ET TYPES ================

    @Test
    @DisplayName("PowerUpFactory - Création de tous les types")
    void testPowerUpFactoryAllTypes() {
        for (PowerUpType type : PowerUpType.values()) {
            if (type == PowerUpType.SKULL) continue; // Skip malus

            PowerUp powerUp = PowerUpFactory.create(type, 5, 5);

            assertNotNull(powerUp, "Factory should create " + type);
            assertEquals(type, powerUp.getType());
            assertEquals(5, powerUp.getX());
            assertEquals(5, powerUp.getY());
        }
    }

    @Test
    @DisplayName("PowerUpType - Génération aléatoire")
    void testRandomPowerUpType() {
        Set<PowerUpType> generatedTypes = new HashSet<>();

        // Générer 100 types aléatoires
        for (int i = 0; i < 100; i++) {
            PowerUpType type = PowerUpType.randomType();
            assertNotNull(type);
            generatedTypes.add(type);
        }

        // Vérifier qu'on a généré plusieurs types différents
        assertTrue(generatedTypes.size() > 1, "Should generate variety of types");
    }

    // ================ TESTS DE CAS LIMITES ================

    @Test
    @DisplayName("Application multiple du même power-up")
    void testMultipleApplicationSamePowerUp() {
        BombUp bombUp = new BombUp(5, 5);
        int initialMaxBombs = player.getMaxBombs();

        bombUp.applyTo(player);
        bombUp.applyTo(player);
        bombUp.applyTo(player);

        // Chaque application doit ajouter +1
        assertEquals(initialMaxBombs + 3, player.getMaxBombs());
    }

    @Test
    @DisplayName("Positions négatives pour power-ups")
    void testNegativePositions() {
        BombUp bombUp = new BombUp(-10, -20);

        assertEquals(-10, bombUp.getX());
        assertEquals(-20, bombUp.getY());
        assertEquals(PowerUpType.BOMB_UP, bombUp.getType());

        // L'effet doit toujours fonctionner
        int initialMaxBombs = player.getMaxBombs();
        bombUp.applyTo(player);
        assertEquals(initialMaxBombs + 1, player.getMaxBombs());
    }
}