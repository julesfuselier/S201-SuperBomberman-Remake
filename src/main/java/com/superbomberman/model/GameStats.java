package com.superbomberman.model;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * Modèle pour stocker les statistiques détaillées d'une partie
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-11
 */
public class GameStats implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private LocalDateTime gameDate;
    private boolean isVictory;
    private int finalScore;
    private long gameDurationSeconds;
    private String gameMode; // "SOLO" ou "MULTI"
    private String mapName;

    // Stats détaillées du gameplay
    private int enemiesKilled;
    private int wallsDestroyed;
    private int powerUpsCollected;
    private int bombsPlaced;
    private int maxCombo;
    private int livesEarned;

    // Pour le mode multijoueur
    private String opponentName;
    private int opponentScore;

    public GameStats() {
        this.gameDate = LocalDateTime.now();
    }

    public GameStats(String username, boolean isVictory, int finalScore,
                     long gameDurationSeconds, String gameMode) {
        this();
        this.username = username;
        this.isVictory = isVictory;
        this.finalScore = finalScore;
        this.gameDurationSeconds = gameDurationSeconds;
        this.gameMode = gameMode;
    }

    // Getters et Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getGameDate() { return gameDate; }
    public void setGameDate(LocalDateTime gameDate) { this.gameDate = gameDate; }

    public boolean isVictory() { return isVictory; }
    public void setVictory(boolean victory) { isVictory = victory; }

    public int getFinalScore() { return finalScore; }
    public void setFinalScore(int finalScore) { this.finalScore = finalScore; }

    public long getGameDurationSeconds() { return gameDurationSeconds; }
    public void setGameDurationSeconds(long gameDurationSeconds) {
        this.gameDurationSeconds = gameDurationSeconds;
    }

    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }

    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }

    public int getEnemiesKilled() { return enemiesKilled; }
    public void setEnemiesKilled(int enemiesKilled) { this.enemiesKilled = enemiesKilled; }

    public int getWallsDestroyed() { return wallsDestroyed; }
    public void setWallsDestroyed(int wallsDestroyed) { this.wallsDestroyed = wallsDestroyed; }

    public int getPowerUpsCollected() { return powerUpsCollected; }
    public void setPowerUpsCollected(int powerUpsCollected) {
        this.powerUpsCollected = powerUpsCollected;
    }

    public int getBombsPlaced() { return bombsPlaced; }
    public void setBombsPlaced(int bombsPlaced) { this.bombsPlaced = bombsPlaced; }

    public int getMaxCombo() { return maxCombo; }
    public void setMaxCombo(int maxCombo) { this.maxCombo = maxCombo; }

    public int getLivesEarned() { return livesEarned; }
    public void setLivesEarned(int livesEarned) { this.livesEarned = livesEarned; }

    public String getOpponentName() { return opponentName; }
    public void setOpponentName(String opponentName) { this.opponentName = opponentName; }

    public int getOpponentScore() { return opponentScore; }
    public void setOpponentScore(int opponentScore) { this.opponentScore = opponentScore; }

    @Override
    public String toString() {
        return "GameStats{" +
                "username='" + username + '\'' +
                ", gameDate=" + gameDate +
                ", isVictory=" + isVictory +
                ", finalScore=" + finalScore +
                ", gameMode='" + gameMode + '\'' +
                '}';
    }
}