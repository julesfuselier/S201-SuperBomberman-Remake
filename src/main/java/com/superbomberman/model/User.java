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

    // Getters et Setters
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