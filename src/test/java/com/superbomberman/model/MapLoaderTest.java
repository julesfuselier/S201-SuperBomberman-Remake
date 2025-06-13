package com.superbomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe MapLoader.
 * Couvre le chargement des cartes, validation des entités et gestion des erreurs.
 */
class MapLoaderTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Réinitialiser les entités globales avant chaque test
        MapLoader.player1 = null;
        MapLoader.player2 = null;
        MapLoader.enemy = null;
    }

    @Test
    @DisplayName("Chargement carte basique - murs et sol")
    void testLoadBasicMap() throws IOException {
        String mapContent = """
                ###
                # #
                ###""";

        Path mapFile = createTempMapFile(mapContent);
        Tile[][] map = MapLoader.loadMap(mapFile.toString());

        assertEquals(3, map.length); // 3 lignes
        assertEquals(3, map[0].length); // 3 colonnes

        // Vérifier les murs du contour
        assertEquals(TileType.WALL, map[0][0].getType());
        assertEquals(TileType.WALL, map[0][1].getType());
        assertEquals(TileType.WALL, map[0][2].getType());
        assertEquals(TileType.WALL, map[2][0].getType());
        assertEquals(TileType.WALL, map[2][2].getType());

        // Vérifier l'espace du milieu
        assertEquals(TileType.FLOOR, map[1][1].getType());
    }

    @Test
    @DisplayName("Chargement carte avec murs destructibles")
    void testLoadMapWithBreakableWalls() throws IOException {
        String mapContent = """
                #0#
                0 0
                #0#""";

        Path mapFile = createTempMapFile(mapContent);
        Tile[][] map = MapLoader.loadMap(mapFile.toString());

        // Vérifier murs destructibles
        assertEquals(TileType.WALL_BREAKABLE, map[0][1].getType());
        assertEquals(TileType.WALL_BREAKABLE, map[1][0].getType());
        assertEquals(TileType.WALL_BREAKABLE, map[1][2].getType());
        assertEquals(TileType.WALL_BREAKABLE, map[2][1].getType());

        // Vérifier centre reste sol
        assertEquals(TileType.FLOOR, map[1][1].getType());
    }

    @Test
    @DisplayName("Chargement carte avec joueur 1")
    void testLoadMapWithPlayer1() throws IOException {
        String mapContent = """
                ###
                #1#
                ###""";

        Path mapFile = createTempMapFile(mapContent);
        Tile[][] map = MapLoader.loadMap(mapFile.toString());

        // Vérifier que la case du joueur 1 est un sol
        assertEquals(TileType.FLOOR, map[1][1].getType());

        // Vérifier que player1 est créé et positionné correctement
        assertNotNull(MapLoader.player1);
        assertEquals(1, MapLoader.player1.getX());
        assertEquals(1, MapLoader.player1.getY());
        assertEquals("Joueur 1", MapLoader.player1.getName());
    }

    @Test
    @DisplayName("Chargement carte avec joueur 2")
    void testLoadMapWithPlayer2() throws IOException {
        String mapContent = """
                ###
                #2#
                ###""";

        Path mapFile = createTempMapFile(mapContent);
        Tile[][] map = MapLoader.loadMap(mapFile.toString());

        // Vérifier que la case du joueur 2 est un sol
        assertEquals(TileType.FLOOR, map[1][1].getType());

        // Vérifier que player2 est créé et positionné correctement
        assertNotNull(MapLoader.player2);
        assertEquals(1, MapLoader.player2.getX());
        assertEquals(1, MapLoader.player2.getY());
        assertEquals("Joueur 2", MapLoader.player2.getName());
    }

    @Test
    @DisplayName("Chargement carte avec ennemi")
    void testLoadMapWithEnemy() throws IOException {
        String mapContent = """
                ###
                #E#
                ###""";

        Path mapFile = createTempMapFile(mapContent);
        Tile[][] map = MapLoader.loadMap(mapFile.toString());

        // Vérifier que la case de l'ennemi est un sol
        assertEquals(TileType.FLOOR, map[1][1].getType());

        // Vérifier que l'ennemi est créé et positionné correctement
        assertNotNull(MapLoader.enemy);
        assertEquals(1, MapLoader.enemy.getX());
        assertEquals(1, MapLoader.enemy.getY());
        assertTrue(MapLoader.enemy.isAlive());
    }

    @Test
    @DisplayName("Chargement carte complète - tous les éléments")
    void testLoadCompleteMap() throws IOException {
        String mapContent = """
                #####
                #1 2#
                # 0 #
                # E #
                #####""";

        Path mapFile = createTempMapFile(mapContent);
        Tile[][] map = MapLoader.loadMap(mapFile.toString());

        assertEquals(5, map.length);
        assertEquals(5, map[0].length);

        // Vérifier joueur 1
        assertNotNull(MapLoader.player1);
        assertEquals(1, MapLoader.player1.getX());
        assertEquals(1, MapLoader.player1.getY());

        // Vérifier joueur 2
        assertNotNull(MapLoader.player2);
        assertEquals(3, MapLoader.player2.getX());
        assertEquals(1, MapLoader.player2.getY());

        // Vérifier ennemi
        assertNotNull(MapLoader.enemy);
        assertEquals(2, MapLoader.enemy.getX());
        assertEquals(3, MapLoader.enemy.getY());

        // Vérifier mur destructible
        assertEquals(TileType.WALL_BREAKABLE, map[2][2].getType());

        // Vérifier sols
        assertEquals(TileType.FLOOR, map[1][1].getType()); // Player1
        assertEquals(TileType.FLOOR, map[1][3].getType()); // Player2
        assertEquals(TileType.FLOOR, map[3][2].getType()); // Enemy
    }

    @Test
    @DisplayName("Chargement multiple - réutilisation des entités existantes")
    void testLoadMultipleMapsReuseEntities() throws IOException {
        // Première carte avec player1
        String mapContent1 = """
                ###
                #1#
                ###""";

        Path mapFile1 = createTempMapFile(mapContent1);
        MapLoader.loadMap(mapFile1.toString());

        Player firstPlayer1 = MapLoader.player1;
        assertNotNull(firstPlayer1);
        assertEquals(1, firstPlayer1.getX());
        assertEquals(1, firstPlayer1.getY());

        // Deuxième carte avec player1 à une autre position
        String mapContent2 = """
                #####
                #   #
                # 1 #
                #   #
                #####""";

        Path mapFile2 = createTempMapFile(mapContent2);
        MapLoader.loadMap(mapFile2.toString());

        // Vérifier que c'est le même objet mais avec nouvelle position
        assertSame(firstPlayer1, MapLoader.player1);
        assertEquals(2, MapLoader.player1.getX());
        assertEquals(2, MapLoader.player1.getY());
    }

    @Test
    @DisplayName("Gestion erreur - fichier inexistant")
    void testLoadNonExistentFile() {
        String nonExistentPath = tempDir.resolve("inexistant.txt").toString();

        assertThrows(IOException.class, () -> {
            MapLoader.loadMap(nonExistentPath);
        });
    }


    @Test
    @DisplayName("Carte asymétrique - lignes de longueurs différentes")
    void testLoadAsymmetricMap() throws IOException {
        String mapContent = """
                ###
                #
                #####""";

        Path mapFile = createTempMapFile(mapContent);

        // Selon l'implémentation, peut planter ou tronquer
        // Test de robustesse
        assertThrows(Exception.class, () -> {
            MapLoader.loadMap(mapFile.toString());
        });
    }

    @Test
    @DisplayName("Caractères non reconnus - robustesse")
    void testLoadMapWithUnknownCharacters() throws IOException {
        String mapContent = """
                ###
                #X#
                ###""";

        Path mapFile = createTempMapFile(mapContent);
        Tile[][] map = MapLoader.loadMap(mapFile.toString());

        // Le caractère 'X' non reconnu devrait être ignoré (case null)
        // Ou traité selon l'implémentation
        assertNotNull(map);
        assertEquals(3, map.length);
    }

    @Test
    @DisplayName("Plusieurs joueurs du même type - dernière position gagne")
    void testLoadMapWithMultipleSameEntities() throws IOException {
        String mapContent = """
                #####
                #1 1#
                #   #
                #E E#
                #####""";

        Path mapFile = createTempMapFile(mapContent);
        MapLoader.loadMap(mapFile.toString());

        // Vérifier que player1 est à la dernière position trouvée
        assertNotNull(MapLoader.player1);
        assertEquals(3, MapLoader.player1.getX()); // Dernière position '1'
        assertEquals(1, MapLoader.player1.getY());

        // Vérifier que enemy est à la dernière position trouvée
        assertNotNull(MapLoader.enemy);
        assertEquals(3, MapLoader.enemy.getX()); // Dernière position 'E'
        assertEquals(3, MapLoader.enemy.getY());
    }

    @Test
    @DisplayName("État des entités statiques - persistance entre appels")
    void testStaticEntitiesPersistence() throws IOException {
        // Charger une carte
        String mapContent = """
                ###
                #1#
                ###""";

        Path mapFile = createTempMapFile(mapContent);
        MapLoader.loadMap(mapFile.toString());

        Player originalPlayer1 = MapLoader.player1;
        assertNotNull(originalPlayer1);

        // Modifier l'état du joueur
        originalPlayer1.increaseMaxBombs();
        int maxBombs = originalPlayer1.getMaxBombs();

        // Charger une autre carte
        String mapContent2 = """
                #####
                # 1 #
                #####""";

        Path mapFile2 = createTempMapFile(mapContent2);
        MapLoader.loadMap(mapFile2.toString());

        // Vérifier que l'objet est le même avec ses modifications
        assertSame(originalPlayer1, MapLoader.player1);
        assertEquals(maxBombs, MapLoader.player1.getMaxBombs());
    }

    /**
     * Méthode utilitaire pour créer un fichier de carte temporaire
     */
    private Path createTempMapFile(String content) throws IOException {
        Path mapFile = tempDir.resolve("test_map.txt");
        Files.write(mapFile, List.of(content.split("\n")));
        return mapFile;
    }
}