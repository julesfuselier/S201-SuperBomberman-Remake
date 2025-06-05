package com.superbomberman.model;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MapLoader {

    public static Player player1 = null;
    public static Enemy enemy = null;
    public static Tile[][] loadMap(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path)); // liste toutes les lignes du fichier
        int rows = lines.size(); // rows <- nombre de ligne du fichier
        int cols = lines.get(0).length(); // cols <- nombre de colonne du fichier

        Tile[][] map = new Tile[rows][cols]; // map vide faite selon les données recupérées avant

        for (int row = 0; row < rows; row++) {
            String line = lines.get(row); // prend ligne par ligne
            for (int col = 0; col < cols; col++) {
                char c = line.charAt(col); // parmi cette ligne, traite chaque caractère selon sa colonne
                switch (c) {
                    case '#' -> map[row][col] = new Tile(TileType.WALL);
                    case ' ' -> map[row][col] = new Tile(TileType.FLOOR);
                    case '0' -> map[row][col] = new Tile(TileType.WALL_BREAKABLE);
                    case '1' -> {

                        map[row][col] = new Tile(TileType.FLOOR);
                        if (player1 == null) {
                            player1 = new Player();
                        }

                        player1.setPosition(col, row);}

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


