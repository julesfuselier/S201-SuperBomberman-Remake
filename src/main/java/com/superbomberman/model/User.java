package com.superbomberman.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe représentant un utilisateur du jeu Super Bomberman.
 * Contient les informations d'authentification et les statistiques de jeu.
 */
public class User {
    private String username;
    private String password; // En production, utiliser un hash sécurisé
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private int gamesPlayed;
    private int gamesWon;
    private int highScore;
    private String favoriteCharacter;

    // 🆕 NOUVELLES PROPRIÉTÉS POUR LA SAUVEGARDE EN TEMPS RÉEL
    private long totalPlayTime = 0;           // temps de jeu total en secondes
    private int averageScore = 0;             // score moyen
    private int bestCombo = 0;                // meilleure série
    private int powerUpsCollected = 0;        // power-ups collectés au total
    private int enemiesKilled = 0;            // ennemis tués au total
    private int wallsDestroyed = 0;           // murs détruits au total
    private boolean currentGameInProgress = false;  // partie en cours
    private long currentGameStartTime = 0;    // début partie actuelle
    private int currentGameScore = 0;         // score partie en cours

    public User() {
        this.createdAt = LocalDateTime.now();
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.highScore = 0;
        this.favoriteCharacter = "Bomberman";
    }

    public User(String username, String password, String email) {
        this();
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getters et Setters existants
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public int getGamesWon() { return gamesWon; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }

    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }

    public String getFavoriteCharacter() { return favoriteCharacter; }
    public void setFavoriteCharacter(String favoriteCharacter) { this.favoriteCharacter = favoriteCharacter; }

    // 🆕 NOUVEAUX GETTERS/SETTERS
    public long getTotalPlayTime() { return totalPlayTime; }
    public void setTotalPlayTime(long totalPlayTime) { this.totalPlayTime = totalPlayTime; }

    public int getAverageScore() { return averageScore; }
    public void setAverageScore(int averageScore) { this.averageScore = averageScore; }

    public int getBestCombo() { return bestCombo; }
    public void setBestCombo(int bestCombo) { this.bestCombo = bestCombo; }

    public int getPowerUpsCollected() { return powerUpsCollected; }
    public void setPowerUpsCollected(int powerUpsCollected) { this.powerUpsCollected = powerUpsCollected; }

    public int getEnemiesKilled() { return enemiesKilled; }
    public void setEnemiesKilled(int enemiesKilled) { this.enemiesKilled = enemiesKilled; }

    public int getWallsDestroyed() { return wallsDestroyed; }
    public void setWallsDestroyed(int wallsDestroyed) { this.wallsDestroyed = wallsDestroyed; }

    public boolean isCurrentGameInProgress() { return currentGameInProgress; }
    public void setCurrentGameInProgress(boolean currentGameInProgress) { this.currentGameInProgress = currentGameInProgress; }

    public long getCurrentGameStartTime() { return currentGameStartTime; }
    public void setCurrentGameStartTime(long currentGameStartTime) { this.currentGameStartTime = currentGameStartTime; }

    public int getCurrentGameScore() { return currentGameScore; }
    public void setCurrentGameScore(int currentGameScore) { this.currentGameScore = currentGameScore; }

    public double getWinRate() {
        return gamesPlayed > 0 ? (double) gamesWon / gamesPlayed * 100 : 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", gamesPlayed=" + gamesPlayed +
                ", winRate=" + String.format("%.1f", getWinRate()) + "%" +
                '}';
    }
}