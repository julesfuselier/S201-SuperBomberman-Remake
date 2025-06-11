package com.superbomberman.game;

import com.superbomberman.model.GameStats;
import com.superbomberman.model.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map;

/**
 * Version améliorée du ScoreSystem qui collecte des statistiques détaillées
 *
 * @author Jules Fuselier
 * @version 2.0
 * @since 2025-06-11
 */
public class EnhancedScoreSystem extends ScoreSystem {
    /**
     * Classe interne pour stocker les statistiques des joueurs
     */
    private static class PlayerStats {
        private int enemiesKilled = 0;
        private int wallsDestroyed = 0;
        private int powerUpsCollected = 0;
        private int bombsPlaced = 0;
        private int bestCombo = 0;

        public int getEnemiesKilled() { return enemiesKilled; }
        public void setEnemiesKilled(int enemiesKilled) { this.enemiesKilled = enemiesKilled; }

        public int getWallsDestroyed() { return wallsDestroyed; }
        public void setWallsDestroyed(int wallsDestroyed) { this.wallsDestroyed = wallsDestroyed; }

        public int getPowerUpsCollected() { return powerUpsCollected; }
        public void setPowerUpsCollected(int powerUpsCollected) { this.powerUpsCollected = powerUpsCollected; }

        public int getBombsPlaced() { return bombsPlaced; }
        public void setBombsPlaced(int bombsPlaced) { this.bombsPlaced = bombsPlaced; }

        public int getBestCombo() { return bestCombo; }
        public void setBestCombo(int bestCombo) { this.bestCombo = bestCombo; }
    }

    // Statistiques détaillées par joueur
    // Statistiques détaillées par joueur
    private Map<Player, Integer> playerEnemiesKilled = new HashMap<>();
    private Map<Player, Integer> playerWallsDestroyed = new HashMap<>();
    private Map<Player, Integer> playerPowerUpsCollected = new HashMap<>();
    private Map<Player, Integer> playerBombsPlaced = new HashMap<>();
    private Map<Player, Integer> playerMaxCombo = new HashMap<>();
    private Map<Player, PlayerStats> playerStats = new HashMap<>();
    public EnhancedScoreSystem(GameStateManager gameStateManager) {super(gameStateManager);}

    @Override
    public void registerPlayer(Player player) {
        super.registerPlayer(player);
        // Initialiser les nouvelles stats
        playerEnemiesKilled.putIfAbsent(player, 0);
        playerWallsDestroyed.putIfAbsent(player, 0);
        playerPowerUpsCollected.putIfAbsent(player, 0);
        playerBombsPlaced.putIfAbsent(player, 0);
        playerMaxCombo.putIfAbsent(player, 0);
        playerStats.putIfAbsent(player, new PlayerStats());
    }

    @Override
    public void addEnemyKilled(Player player) {
        super.addEnemyKilled(player);
        playerEnemiesKilled.put(player, playerEnemiesKilled.getOrDefault(player, 0) + 1);
        playerStats.computeIfAbsent(player, k -> new PlayerStats()).setEnemiesKilled(playerEnemiesKilled.get(player));
    }

    @Override
    public void addWallDestroyed(Player player) {
        super.addWallDestroyed(player);
        playerWallsDestroyed.put(player, playerWallsDestroyed.getOrDefault(player, 0) + 1);
        playerStats.computeIfAbsent(player, k -> new PlayerStats()).setWallsDestroyed(playerWallsDestroyed.get(player));
    }

    @Override
    public void addPowerUpCollected(Player player) {
        super.addPowerUpCollected(player);
        playerPowerUpsCollected.put(player, playerPowerUpsCollected.getOrDefault(player, 0) + 1);
        playerStats.computeIfAbsent(player, k -> new PlayerStats()).setPowerUpsCollected(playerPowerUpsCollected.get(player));
    }

    /**
     * Nouvelle méthode pour enregistrer les bombes placées
     */
    public void addBombPlaced(Player player) {
        playerBombsPlaced.put(player, playerBombsPlaced.getOrDefault(player, 0) + 1);
        playerStats.computeIfAbsent(player, k -> new PlayerStats()).setBombsPlaced(playerBombsPlaced.get(player));
    }

    @Override
    public void processExplosionCombo(Player player) {
        List<Integer> comboList = playerCombos.getOrDefault(player, new ArrayList<>());
        if (!comboList.isEmpty()) {
            int comboSize = comboList.size();
            int currentMax = playerMaxCombo.getOrDefault(player, 0);
            if (comboSize > currentMax) {
                playerMaxCombo.put(player, comboSize);
                playerStats.computeIfAbsent(player, k -> new PlayerStats()).setBestCombo(comboSize);
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


    /**
     * Récupère le nombre d'ennemis tués par un joueur spécifique
     *
     * @param player Le joueur dont on veut connaître les statistiques
     * @return Le nombre d'ennemis tués par ce joueur
     */
    public int getEnemiesKilledByPlayer(Player player) {
        if (player == null) return 0;

        // Si on utilise une map pour stocker les statistiques par joueur
        // On peut supposer que la classe EnhancedScoreSystem a un champ comme:
        // private Map<Player, PlayerStats> playerStats;
        return playerStats.getOrDefault(player, new PlayerStats()).getEnemiesKilled();
    }

    /**
     * Récupère le nombre de power-ups collectés par un joueur spécifique
     *
     * @param player Le joueur dont on veut connaître les statistiques
     * @return Le nombre de power-ups collectés par ce joueur
     */
    public int getPowerUpsCollectedByPlayer(Player player) {
        if (player == null) return 0;
        return playerStats.getOrDefault(player, new PlayerStats()).getPowerUpsCollected();
    }

    /**
     * Récupère le nombre de murs détruits par un joueur spécifique
     *
     * @param player Le joueur dont on veut connaître les statistiques
     * @return Le nombre de murs détruits par ce joueur
     */
    public int getWallsDestroyedByPlayer(Player player) {
        if (player == null) return 0;
        return playerStats.getOrDefault(player, new PlayerStats()).getWallsDestroyed();
    }

    /**
     * Récupère le meilleur combo réalisé par un joueur spécifique
     *
     * @param player Le joueur dont on veut connaître les statistiques
     * @return La valeur du meilleur combo de ce joueur
     */
    public int getBestComboByPlayer(Player player) {
        if (player == null) return 0;
        return playerStats.getOrDefault(player, new PlayerStats()).getBestCombo();
    }


    @Override
    public void reset() {
        super.reset();
        playerEnemiesKilled.clear();
        playerWallsDestroyed.clear();
        playerPowerUpsCollected.clear();
        playerBombsPlaced.clear();
        playerMaxCombo.clear();
        playerStats.clear();
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