package com.superbomberman.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe représentant un utilisateur du jeu Super Bomberman.
 * <p>
 * Contient les informations d'authentification, les statistiques de jeu et des préférences utilisateur.
 * <ul>
 *     <li>Identifiants (nom d'utilisateur, mot de passe, email)</li>
 *     <li>Dates de création et de dernière connexion</li>
 *     <li>Statistiques : parties jouées, parties gagnées, meilleur score</li>
 *     <li>Personnage favori et calcul du taux de victoire</li>
 * </ul>
 * <b>Remarque :</b> En production, le mot de passe doit être stocké de manière sécurisée (hashé).
 * </p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-08
 */
public class User {
    /** Nom d'utilisateur unique. */
    private String username;

    /** Mot de passe du compte utilisateur (à hasher en production). */
    private String password; // En production, utiliser un hash sécurisé

    /** Adresse email de l'utilisateur. */
    private String email;

    /** Date de création du compte utilisateur. */
    private LocalDateTime createdAt;

    /** Date de dernière connexion de l'utilisateur. */
    private LocalDateTime lastLoginAt;

    /** Nombre total de parties jouées. */
    private int gamesPlayed;

    /** Nombre total de parties gagnées. */
    private int gamesWon;

    /** Meilleur score atteint par l'utilisateur. */
    private int highScore;

    /** Personnage favori choisi par l'utilisateur. */
    private String favoriteCharacter;

    /**
     * Constructeur par défaut.
     * Initialise les dates et statistiques à des valeurs par défaut.
     */
    public User() {
        this.createdAt = LocalDateTime.now();
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.highScore = 0;
        this.favoriteCharacter = "Bomberman";
    }

    /**
     * Constructeur principal.
     *
     * @param username Nom d'utilisateur.
     * @param password Mot de passe (en clair, à hasher en production).
     * @param email Adresse email.
     */
    public User(String username, String password, String email) {
        this();
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // --- Getters et Setters ---

    /** @return le nom d'utilisateur */
    public String getUsername() { return username; }

    /** @param username le nom d'utilisateur à définir */
    public void setUsername(String username) { this.username = username; }

    /** @return le mot de passe (non hashé) */
    public String getPassword() { return password; }

    /** @param password le mot de passe à définir */
    public void setPassword(String password) { this.password = password; }

    /** @return l'email de l'utilisateur */
    public String getEmail() { return email; }

    /** @param email l'email à définir */
    public void setEmail(String email) { this.email = email; }

    /** @return la date de création du compte */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @param createdAt la date de création à définir */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** @return la date de dernière connexion */
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }

    /** @param lastLoginAt la date de dernière connexion à définir */
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    /** @return le nombre de parties jouées */
    public int getGamesPlayed() { return gamesPlayed; }

    /** @param gamesPlayed le nombre de parties jouées à définir */
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    /** @return le nombre de parties gagnées */
    public int getGamesWon() { return gamesWon; }

    /** @param gamesWon le nombre de victoires à définir */
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }

    /** @return le meilleur score */
    public int getHighScore() { return highScore; }

    /** @param highScore le meilleur score à définir */
    public void setHighScore(int highScore) { this.highScore = highScore; }

    /** @return le personnage favori */
    public String getFavoriteCharacter() { return favoriteCharacter; }

    /** @param favoriteCharacter le personnage favori à définir */
    public void setFavoriteCharacter(String favoriteCharacter) { this.favoriteCharacter = favoriteCharacter; }

    /**
     * Calcule le taux de victoire de l'utilisateur en pourcentage.
     * @return le pourcentage de victoire (0 si aucune partie jouée)
     */
    public double getWinRate() {
        return gamesPlayed > 0 ? (double) gamesWon / gamesPlayed * 100 : 0.0;
    }

    // --- Méthodes utilitaires ---

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    /** {@inheritDoc} */
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