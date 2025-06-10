package com.superbomberman.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MapLoader {

    public static Player player1 = null;
    public static Player player2 = null;
    public static Enemy enemy = null;

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