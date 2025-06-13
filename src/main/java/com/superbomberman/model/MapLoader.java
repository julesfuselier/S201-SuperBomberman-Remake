package com.superbomberman.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Utilitaire pour charger une carte du jeu Super Bomberman à partir d'un fichier texte.
 * <p>
 * Cette classe permet de générer une matrice de {@link Tile} à partir d'un fichier de carte,
 * tout en positionnant les joueurs {@link Player} et l'{@link Enemy} selon les caractères du fichier.
 * </p>
 * <ul>
 *     <li>'#' : Mur indestructible ({@link TileType#WALL})</li>
 *     <li>' ' : Sol ({@link TileType#FLOOR})</li>
 *     <li>'0' : Mur destructible ({@link TileType#WALL_BREAKABLE})</li>
 *     <li>'1' : Joueur 1 (positionné sur une case sol)</li>
 *     <li>'2' : Joueur 2 (positionné sur une case sol)</li>
 *     <li>'E' : Ennemi (positionné sur une case sol)</li>
 * </ul>
 *
 * Les attributs {@link #player1}, {@link #player2} et {@link #enemy} sont mis à jour avec leurs positions respectives.
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-08
 */
public class MapLoader {

    /** Référence globale vers le joueur 1 (définie lors du chargement de la carte). */
    public static Player player1 = null;

    /** Référence globale vers le joueur 2 (définie lors du chargement de la carte). */
    public static Player player2 = null;

    /** Référence globale vers l'ennemi (définie lors du chargement de la carte). */
    public static Enemy enemy = null;

    /**
     * Charge une carte à partir d'un fichier texte et retourne la matrice de {@link Tile} correspondante.
     * <p>
     * Met à jour les positions des joueurs et de l'ennemi selon les caractères de la carte.
     * </p>
     *
     * @param path Chemin du fichier de carte à charger.
     * @return Matrice de {@link Tile} représentant la carte du jeu.
     * @throws IOException Si le fichier ne peut pas être lu.
     */
    public static Tile[][] loadMap(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));
        int rows = lines.size();
        int cols = lines.get(0).length();

        Tile[][] map = new Tile[rows][cols];

        for (int row = 0; row < rows; row++) {
            String line = lines.get(row);
            for (int col = 0; col < cols; col++) {
                char c = line.charAt(col);
                switch (c) {
                    case '#' -> map[row][col] = new Tile(TileType.WALL);
                    case ' ' -> map[row][col] = new Tile(TileType.FLOOR);
                    case '0' -> map[row][col] = new Tile(TileType.WALL_BREAKABLE);
                    case '1' -> {
                        map[row][col] = new Tile(TileType.FLOOR);
                        if (player1 == null) {
                            player1 = new Player("Joueur 1");
                        }
                        player1.setPosition(col, row);
                    }
                    case '2' -> {
                        map[row][col] = new Tile(TileType.FLOOR);
                        if (player2 == null) {
                            player2 = new Player("Joueur 2");
                        }
                        player2.setPosition(col, row);
                    }
                    case 'E' -> {
                        map[row][col] = new Tile(TileType.FLOOR);
                        if (enemy == null) {
                            enemy = new Enemy();
                        }
                        enemy.setPosition(col, row);
                    }
                }
            }
        }

        return map;
    }
}