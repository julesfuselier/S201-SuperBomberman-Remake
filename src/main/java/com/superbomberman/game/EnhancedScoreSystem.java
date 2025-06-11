package com.superbomberman.game;

import com.superbomberman.model.GameStats;
import com.superbomberman.model.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Version améliorée du ScoreSystem qui collecte des statistiques détaillées
 *
 * @author Jules Fuselier
 * @version 2.0
 * @since 2025-06-11
 */
public class EnhancedScoreSystem extends ScoreSystem {
    // Statistiques détaillées par joueur
    private Map<Player, Integer> playerEnemiesKilled = new HashMap<>();
    private Map<Player, Integer> playerWallsDestroyed = new HashMap<>();
    private Map<Player, Integer> playerPowerUpsCollected = new HashMap<>();
    private Map<Player, Integer> playerBombsPlaced = new HashMap<>();
    private Map<Player, Integer> playerMaxCombo = new HashMap<>();

    public EnhancedScoreSystem(GameStateManager gameStateManager) {
        super(gameStateManager);
    }

    @Override
    public void registerPlayer(Player player) {
        super.registerPlayer(player);
        // Initialiser les nouvelles stats
        playerEnemiesKilled.putIfAbsent(player, 0);
        playerWallsDestroyed.putIfAbsent(player, 0);
        playerPowerUpsCollected.putIfAbsent(player, 0);
        playerBombsPlaced.putIfAbsent(player, 0);
        playerMaxCombo.putIfAbsent(player, 0);
    }

    @Override
    public void addEnemyKilled(Player player) {
        super.addEnemyKilled(player);
        playerEnemiesKilled.put(player, playerEnemiesKilled.getOrDefault(player, 0) + 1);
    }

    @Override
    public void addWallDestroyed(Player player) {
        super.addWallDestroyed(player);
        playerWallsDestroyed.put(player, playerWallsDestroyed.getOrDefault(player, 0) + 1);
    }

    @Override
    public void addPowerUpCollected(Player player) {
        super.addPowerUpCollected(player);
        playerPowerUpsCollected.put(player, playerPowerUpsCollected.getOrDefault(player, 0) + 1);
    }

    /**
     * Nouvelle méthode pour enregistrer les bombes placées
     */
    public void addBombPlaced(Player player) {
        playerBombsPlaced.put(player, playerBombsPlaced.getOrDefault(player, 0) + 1);
    }

    @Override
    public void processExplosionCombo(Player player) {
        List<Integer> comboList = playerCombos.getOrDefault(player, new ArrayList<>());
        if (!comboList.isEmpty()) {
            int comboSize = comboList.size();
            int currentMax = playerMaxCombo.getOrDefault(player, 0);
            if (comboSize > currentMax) {
                playerMaxCombo.put(player, comboSize);
            }
        }
        super.processExplosionCombo(player);
    }

    /**
     * Crée un objet GameStats avec toutes les données collectées
     */
    public GameStats createGameStats(String username, boolean isVictory,
                                     long gameDurationSeconds, String gameMode, Player player) {
        GameStats stats = new GameStats(username, isVictory, getPlayerScore(player),
                gameDurationSeconds, gameMode);

        // Remplir les stats détaillées
        stats.setEnemiesKilled(playerEnemiesKilled.getOrDefault(player, 0));
        stats.setWallsDestroyed(playerWallsDestroyed.getOrDefault(player, 0));
        stats.setPowerUpsCollected(playerPowerUpsCollected.getOrDefault(player, 0));
        stats.setBombsPlaced(playerBombsPlaced.getOrDefault(player, 0));
        stats.setMaxCombo(playerMaxCombo.getOrDefault(player, 0));

        return stats;
    }

    @Override
    public void reset() {
        super.reset();
        playerEnemiesKilled.clear();
        playerWallsDestroyed.clear();
        playerPowerUpsCollected.clear();
        playerBombsPlaced.clear();
        playerMaxCombo.clear();
    }

    // Getters pour les nouvelles stats
    public int getPlayerEnemiesKilled(Player player) {
        return playerEnemiesKilled.getOrDefault(player, 0);
    }

    public int getPlayerWallsDestroyed(Player player) {
        return playerWallsDestroyed.getOrDefault(player, 0);
    }

    public int getPlayerPowerUpsCollected(Player player) {
        return playerPowerUpsCollected.getOrDefault(player, 0);
    }

    public int getPlayerBombsPlaced(Player player) {
        return playerBombsPlaced.getOrDefault(player, 0);
    }

    public int getPlayerMaxCombo(Player player) {
        return playerMaxCombo.getOrDefault(player, 0);
    }
}