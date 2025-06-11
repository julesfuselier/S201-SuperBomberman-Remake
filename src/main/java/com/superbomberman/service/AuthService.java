package com.superbomberman.service;

import com.superbomberman.model.User;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Service de gestion de l'authentification et des utilisateurs.
 * Utilise un système de fichiers simple pour stocker les données utilisateurs.
 */
public class AuthService {
    private static final String USERS_DIR = "data/users/";
    private static final String CURRENT_USER_FILE = "data/current_user.properties";
    private User currentUser;
    private Map<String, User> userCache;

    public AuthService() {
        this.userCache = new HashMap<>();
        createDataDirectories();
        loadAllUsers();
    }

    private void createDataDirectories() {
        try {
            Path usersPath = Paths.get(USERS_DIR);
            Path dataPath = Paths.get("data");

            if (!Files.exists(dataPath)) {
                Files.createDirectory(dataPath);
            }
            if (!Files.exists(usersPath)) {
                Files.createDirectory(usersPath);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la création des répertoires : " + e.getMessage());
        }
    }


    /**
     * Authentifie un utilisateur avec son nom d'utilisateur et mot de passe
     */
// Modifiez la méthode login pour accepter le paramètre rememberMe
    public boolean login(String username, String password, boolean rememberMe) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }

        User user = loadUser(username);
        if (user != null && user.getPassword().equals(password)) {
            user.setLastLoginAt(LocalDateTime.now());
            saveUser(user);
            this.currentUser = user;

            // Sauvegarder la session seulement si "Se souvenir de moi" est coché
            if (rememberMe) {
                saveCurrentUserSession();
            }

            return true;
        }
        return false;
    }

    // Gardez aussi l'ancienne méthode pour la compatibilité (inscription)
    public boolean login(String username, String password) {
        return login(username, password, false);
    }

    /**
     * Inscrit un nouvel utilisateur
     */
    public boolean register(String username, String password, String email) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }

        username = username.trim();

        // Vérifier si l'utilisateur existe déjà
        if (userExists(username)) {
            return false;
        }

        // Créer le nouvel utilisateur
        User newUser = new User(username, password, email);
        saveUser(newUser);
        userCache.put(username, newUser);

        // Connexion automatique après inscription
        this.currentUser = newUser;
        saveCurrentUserSession();

        return true;
    }

    public boolean register(String username, String password, String email, String favoriteCharacter) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }

        username = username.trim();

        // Vérifier si l'utilisateur existe déjà
        if (userExists(username)) {
            return false;
        }

        // Créer le nouvel utilisateur avec personnage favori
        User newUser = new User(username, password, email);
        if (favoriteCharacter != null && !favoriteCharacter.isEmpty()) {
            newUser.setFavoriteCharacter(favoriteCharacter);
        }

        saveUser(newUser);
        userCache.put(username, newUser);

        // Connexion automatique après inscription
        this.currentUser = newUser;
        saveCurrentUserSession();

        return true;
    }

    /**
     * Déconnecte l'utilisateur actuel
     */
    public void logout() {
        this.currentUser = null;
        deleteCurrentUserSession();
    }

    /**
     * Vérifie si un utilisateur existe
     */
    public boolean userExists(String username) {
        return loadUser(username) != null;
    }

    /**
     * Récupère l'utilisateur actuellement connecté
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Charge un utilisateur depuis le fichier
     */
    public User loadUser(String username) {
        if (userCache.containsKey(username)) {
            return userCache.get(username);
        }

        Path userFile = Paths.get(USERS_DIR + username + ".properties");
        if (!Files.exists(userFile)) {
            return null;
        }

        try (InputStream input = Files.newInputStream(userFile)) {
            Properties props = new Properties();
            props.load(input);

            User user = new User();
            user.setUsername(props.getProperty("username"));
            user.setPassword(props.getProperty("password"));
            user.setEmail(props.getProperty("email", ""));
            user.setGamesPlayed(Integer.parseInt(props.getProperty("gamesPlayed", "0")));
            user.setGamesWon(Integer.parseInt(props.getProperty("gamesWon", "0")));
            user.setHighScore(Integer.parseInt(props.getProperty("highScore", "0")));
            user.setFavoriteCharacter(props.getProperty("favoriteCharacter", "Bomberman"));

            // 🆕 CHARGER LES NOUVELLES PROPRIÉTÉS
            user.setTotalPlayTime(Long.parseLong(props.getProperty("totalPlayTime", "0")));
            user.setAverageScore(Integer.parseInt(props.getProperty("averageScore", "0")));
            user.setBestCombo(Integer.parseInt(props.getProperty("bestCombo", "0")));
            user.setPowerUpsCollected(Integer.parseInt(props.getProperty("powerUpsCollected", "0")));
            user.setEnemiesKilled(Integer.parseInt(props.getProperty("enemiesKilled", "0")));
            user.setWallsDestroyed(Integer.parseInt(props.getProperty("wallsDestroyed", "0")));
            user.setCurrentGameInProgress(Boolean.parseBoolean(props.getProperty("currentGameInProgress", "false")));
            user.setCurrentGameStartTime(Long.parseLong(props.getProperty("currentGameStartTime", "0")));
            user.setCurrentGameScore(Integer.parseInt(props.getProperty("currentGameScore", "0")));

            if (props.getProperty("createdAt") != null) {
                user.setCreatedAt(LocalDateTime.parse(props.getProperty("createdAt")));
            }
            if (props.getProperty("lastLoginAt") != null) {
                user.setLastLoginAt(LocalDateTime.parse(props.getProperty("lastLoginAt")));
            }

            userCache.put(username, user);
            return user;

        } catch (IOException | NumberFormatException e) {
            System.err.println("Erreur lors du chargement de l'utilisateur " + username + " : " + e.getMessage());
            return null;
        }
    }

    /**
     * Sauvegarde un utilisateur dans un fichier
     */
    private void saveUser(User user) {
        Path userFile = Paths.get(USERS_DIR + user.getUsername() + ".properties");

        System.out.println("💾 SAUVEGARDE USER: " + user.getUsername());
        System.out.println("💾 Fichier: " + userFile.toAbsolutePath());

        try (OutputStream output = Files.newOutputStream(userFile)) {
            Properties props = new Properties();

            // Propriétés existantes
            props.setProperty("username", user.getUsername());
            props.setProperty("password", user.getPassword());
            props.setProperty("email", user.getEmail() != null ? user.getEmail() : "");
            props.setProperty("gamesPlayed", String.valueOf(user.getGamesPlayed()));
            props.setProperty("gamesWon", String.valueOf(user.getGamesWon()));
            props.setProperty("highScore", String.valueOf(user.getHighScore()));
            props.setProperty("favoriteCharacter", user.getFavoriteCharacter());
            props.setProperty("createdAt", user.getCreatedAt().toString());

            if (user.getLastLoginAt() != null) {
                props.setProperty("lastLoginAt", user.getLastLoginAt().toString());
            }

            // 🆕 NOUVELLES PROPRIÉTÉS
            props.setProperty("totalPlayTime", String.valueOf(user.getTotalPlayTime()));
            props.setProperty("averageScore", String.valueOf(user.getAverageScore()));
            props.setProperty("bestCombo", String.valueOf(user.getBestCombo()));
            props.setProperty("powerUpsCollected", String.valueOf(user.getPowerUpsCollected()));
            props.setProperty("enemiesKilled", String.valueOf(user.getEnemiesKilled()));
            props.setProperty("wallsDestroyed", String.valueOf(user.getWallsDestroyed()));
            props.setProperty("currentGameInProgress", String.valueOf(user.isCurrentGameInProgress()));
            props.setProperty("currentGameStartTime", String.valueOf(user.getCurrentGameStartTime()));
            props.setProperty("currentGameScore", String.valueOf(user.getCurrentGameScore()));

            props.store(output, "User data for " + user.getUsername());
            userCache.put(user.getUsername(), user);

            props.store(output, "User data for " + user.getUsername());
            userCache.put(user.getUsername(), user);

            System.out.println("💾 Sauvegarde réussie !");

        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde de l'utilisateur " + user.getUsername() + " : " + e.getMessage());
        }
    }

    /**
     * Charge tous les utilisateurs en cache
     */
    private void loadAllUsers() {
        try {
            Path usersPath = Paths.get(USERS_DIR);
            if (Files.exists(usersPath)) {
                Files.list(usersPath)
                        .filter(path -> path.toString().endsWith(".properties"))
                        .forEach(path -> {
                            String filename = path.getFileName().toString();
                            String username = filename.substring(0, filename.lastIndexOf("."));
                            loadUser(username);
                        });
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des utilisateurs : " + e.getMessage());
        }
    }

    /**
     * Sauvegarde la session utilisateur actuelle
     */
    private void saveCurrentUserSession() {
        if (currentUser == null) return;

        try (OutputStream output = Files.newOutputStream(Paths.get(CURRENT_USER_FILE))) {
            Properties props = new Properties();
            props.setProperty("currentUser", currentUser.getUsername());
            props.store(output, "Current user session");
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde de la session : " + e.getMessage());
        }
    }

    /**
     * Supprime la session utilisateur actuelle
     */
    private void deleteCurrentUserSession() {
        try {
            Path sessionFile = Paths.get(CURRENT_USER_FILE);
            if (Files.exists(sessionFile)) {
                Files.delete(sessionFile);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression de la session : " + e.getMessage());
        }
    }

    /**
     * Tente de restaurer une session utilisateur précédente
     */
    public boolean restoreSession() {
        Path sessionFile = Paths.get(CURRENT_USER_FILE);
        if (!Files.exists(sessionFile)) {
            return false;
        }

        try (InputStream input = Files.newInputStream(sessionFile)) {
            Properties props = new Properties();
            props.load(input);

            String username = props.getProperty("currentUser");
            if (username != null) {
                User user = loadUser(username);
                if (user != null) {
                    this.currentUser = user;
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la restauration de la session : " + e.getMessage());
        }

        return false;
    }

    /**
     * Met à jour les statistiques d'un utilisateur
     */
    public void updateUserStats(User user, boolean won, int score) {
        user.setGamesPlayed(user.getGamesPlayed() + 1);
        if (won) {
            user.setGamesWon(user.getGamesWon() + 1);
        }
        if (score > user.getHighScore()) {
            user.setHighScore(score);
        }
        saveUser(user);
    }

    /**
     * Sauvegarde les progrès de la partie en cours
     */
    public void saveCurrentGameProgress(User user, int currentScore, long gameStartTime) {
        if (user == null) return;

        user.setCurrentGameInProgress(true);
        user.setCurrentGameStartTime(gameStartTime);
        user.setCurrentGameScore(currentScore);

        saveUser(user);
        System.out.println("💾 Progrès sauvegardé - Score: " + currentScore);
    }

    public void updateDetailedStats(User user, int enemiesKilled, int powerUpsCollected, int wallsDestroyed, int bestCombo) {
        if (user == null) return;

        user.setEnemiesKilled(user.getEnemiesKilled() + enemiesKilled);
        user.setPowerUpsCollected(user.getPowerUpsCollected() + powerUpsCollected);
        user.setWallsDestroyed(user.getWallsDestroyed() + wallsDestroyed);

        if (bestCombo > user.getBestCombo()) {
            user.setBestCombo(bestCombo);
        }

        saveUser(user);
    }

    /**
     * Finalise une partie et met à jour les statistiques de l'utilisateur.
     */
    public void finalizeGame(User user, boolean won, int finalScore, long gameDuration) {
        if (user == null) {
            System.err.println("❌ USER NULL !");
            return;
        }

        System.out.println("🎯 DEBUT finalizeGame");
        System.out.println("  - User: " + user.getUsername());
        System.out.println("  - Score: " + finalScore + " | Won: " + won);
        System.out.println("  - Durée: " + (gameDuration/1000) + "s");

        // Stats AVANT modification
        System.out.println("  - AVANT - gamesPlayed: " + user.getGamesPlayed());
        System.out.println("  - AVANT - enemiesKilled: " + user.getEnemiesKilled());
        System.out.println("  - AVANT - powerUpsCollected: " + user.getPowerUpsCollected());

        // Mettre à jour les stats traditionnelles
        user.setGamesPlayed(user.getGamesPlayed() + 1);
        if (won) {
            user.setGamesWon(user.getGamesWon() + 1);
        }
        if (finalScore > user.getHighScore()) {
            user.setHighScore(finalScore);
        }

        // Mettre à jour les nouvelles stats
        user.setTotalPlayTime(user.getTotalPlayTime() + (gameDuration / 1000));
        user.setCurrentGameInProgress(false);
        user.setCurrentGameScore(0);

        // Recalculer score moyen
        if (user.getGamesPlayed() > 0) {
            int newAverage = ((user.getAverageScore() * (user.getGamesPlayed() - 1)) + finalScore) / user.getGamesPlayed();
            user.setAverageScore(newAverage);
        }

        // Stats APRES modification
        System.out.println("  - APRES - gamesPlayed: " + user.getGamesPlayed());
        System.out.println("  - APRES - enemiesKilled: " + user.getEnemiesKilled());
        System.out.println("  - APRES - powerUpsCollected: " + user.getPowerUpsCollected());

        saveUser(user);
        System.out.println("🎯 FIN finalizeGame - Sauvegarde terminée");
    }

    /**
     * Vérifie et gère une partie interrompue
     */
    public boolean handleInterruptedGame(User user) {
        if (user == null || !user.isCurrentGameInProgress()) {
            return false;
        }

        System.out.println("⚠️ Partie interrompue détectée pour " + user.getUsername());

        // Compter comme défaite et finaliser
        finalizeGame(user, false, user.getCurrentGameScore(),
                System.currentTimeMillis() - user.getCurrentGameStartTime());

        return true;
    }


}