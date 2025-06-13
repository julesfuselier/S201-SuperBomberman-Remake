package com.superbomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

/**
 * Tests unitaires pour la classe User
 *
 * Politique de tests implémentée :
 * - Tests de construction et initialisation
 * - Tests des getters/setters
 * - Tests des méthodes de calcul (winRate)
 * - Tests des méthodes equals/hashCode
 * - Tests de validation des données
 * - Tests des cas limites
 *
 * Couverture : Vise 100% des méthodes publiques
 */
@DisplayName("Tests de la classe User")
public class UserTest {

    private User user;
    private User userWithParams;

    @BeforeEach
    void setUp() {
        user = new User();
        userWithParams = new User("testUser", "password123", "test@example.com");
    }

    @Test
    @DisplayName("Test du constructeur par défaut")
    void testDefaultConstructor() {
        assertNotNull(user.getCreatedAt(), "La date de création doit être définie");
        assertEquals(0, user.getGamesPlayed(), "Nombre de parties jouées initial doit être 0");
        assertEquals(0, user.getGamesWon(), "Nombre de victoires initial doit être 0");
        assertEquals(0, user.getHighScore(), "Score max initial doit être 0");
        assertEquals("Bomberman", user.getFavoriteCharacter(), "Personnage favori par défaut doit être Bomberman");
        assertNull(user.getLastLoginAt(), "Dernière connexion doit être null initialement");
    }

    @Test
    @DisplayName("Test du constructeur avec paramètres")
    void testParameterConstructor() {
        assertEquals("testUser", userWithParams.getUsername());
        assertEquals("password123", userWithParams.getPassword());
        assertEquals("test@example.com", userWithParams.getEmail());
        assertNotNull(userWithParams.getCreatedAt());
        assertEquals(0, userWithParams.getGamesPlayed());
        assertEquals("Bomberman", userWithParams.getFavoriteCharacter());
    }

    @Test
    @DisplayName("Test des setters et getters")
    void testSettersAndGetters() {
        // Test username
        user.setUsername("nouveauNom");
        assertEquals("nouveauNom", user.getUsername());

        // Test password
        user.setPassword("nouveauMdp");
        assertEquals("nouveauMdp", user.getPassword());

        // Test email
        user.setEmail("nouveau@email.com");
        assertEquals("nouveau@email.com", user.getEmail());

        // Test favorite character
        user.setFavoriteCharacter("Luigi");
        assertEquals("Luigi", user.getFavoriteCharacter());

        // Test dates
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginAt(now);
        assertEquals(now, user.getLastLoginAt());

        LocalDateTime creation = LocalDateTime.now().minusDays(10);
        user.setCreatedAt(creation);
        assertEquals(creation, user.getCreatedAt());
    }

    @Test
    @DisplayName("Test des statistiques de jeu")
    void testGameStatistics() {
        // Test initial
        assertEquals(0, user.getGamesPlayed());
        assertEquals(0, user.getGamesWon());
        assertEquals(0, user.getHighScore());

        // Mise à jour des stats
        user.setGamesPlayed(10);
        user.setGamesWon(7);
        user.setHighScore(15000);

        assertEquals(10, user.getGamesPlayed());
        assertEquals(7, user.getGamesWon());
        assertEquals(15000, user.getHighScore());
    }

    @Test
    @DisplayName("Test du calcul du taux de victoire")
    void testWinRateCalculation() {
        // Cas initial : 0 parties jouées
        assertEquals(0.0, user.getWinRate(), 0.01, "Taux de victoire initial doit être 0");

        // Cas normal : 7 victoires sur 10 parties
        user.setGamesPlayed(10);
        user.setGamesWon(7);
        assertEquals(70.0, user.getWinRate(), 0.01, "Taux de victoire doit être 70%");

        // Cas parfait : 100% de victoires
        user.setGamesPlayed(5);
        user.setGamesWon(5);
        assertEquals(100.0, user.getWinRate(), 0.01, "Taux de victoire doit être 100%");

        // Cas zéro victoire
        user.setGamesPlayed(3);
        user.setGamesWon(0);
        assertEquals(0.0, user.getWinRate(), 0.01, "Taux de victoire doit être 0%");

        // Cas avec nombres décimaux
        user.setGamesPlayed(3);
        user.setGamesWon(1);
        assertEquals(33.33, user.getWinRate(), 0.01, "Taux de victoire doit être ~33.33%");
    }

    @Test
    @DisplayName("Test de la méthode equals")
    void testEquals() {
        User user1 = new User("player1", "pass1", "email1@test.com");
        User user2 = new User("player1", "pass2", "email2@test.com"); // Même username
        User user3 = new User("player2", "pass1", "email1@test.com"); // Username différent

        // Test égalité basée sur username
        assertTrue(user1.equals(user2), "Users avec même username doivent être égaux");
        assertFalse(user1.equals(user3), "Users avec username différents ne doivent pas être égaux");

        // Test réflexivité
        assertTrue(user1.equals(user1), "Un user doit être égal à lui-même");

        // Test avec null
        assertFalse(user1.equals(null), "Un user ne doit pas être égal à null");

        // Test avec objet différent
        assertFalse(user1.equals("string"), "Un user ne doit pas être égal à un string");
    }

    @Test
    @DisplayName("Test de la méthode hashCode")
    void testHashCode() {
        User user1 = new User("player1", "pass1", "email1@test.com");
        User user2 = new User("player1", "pass2", "email2@test.com");
        User user3 = new User("player2", "pass1", "email1@test.com");

        // Users égaux doivent avoir même hashCode
        assertEquals(user1.hashCode(), user2.hashCode(),
                "Users égaux doivent avoir le même hashCode");

        // Users différents peuvent avoir hashCode différents
        assertNotEquals(user1.hashCode(), user3.hashCode(),
                "Users différents devraient avoir des hashCodes différents");
    }



    @Test
    @DisplayName("Test avec valeurs nulles")
    void testNullValues() {
        // Test avec username null
        user.setUsername(null);
        assertNull(user.getUsername());

        // Test avec email null
        user.setEmail(null);
        assertNull(user.getEmail());

        // Test avec favorite character null
        user.setFavoriteCharacter(null);
        assertNull(user.getFavoriteCharacter());
    }
}