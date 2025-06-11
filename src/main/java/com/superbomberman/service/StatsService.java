package com.superbomberman.service;

import com.superbomberman.model.GameStats;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour gérer les statistiques détaillées des joueurs
 * Version utilisant la sérialisation Java native
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-11
 */
public class StatsService {
    private static final String STATS_FILE = "data/game_stats.dat";
    private List<GameStats> allGameStats;

    public StatsService() {
        this.allGameStats = new ArrayList<>();
        loadStats();
    }

    /**
     * Enregistre une nouvelle partie terminée
     */
    public void recordGameStats(GameStats gameStats) {
        allGameStats.add(gameStats);
        saveStats();
        System.out.println("📊 Statistiques enregistrées pour " + gameStats.getUsername());
    }

    /**
     * Récupère toutes les stats d'un utilisateur
     */
    public List<GameStats> getUserStats(String username) {
        return allGameStats.stream()
                .filter(stats -> stats.getUsername().equals(username))
                .sorted((a, b) -> b.getGameDate().compareTo(a.getGameDate())) // Plus récent en premier
                .collect(Collectors.toList());
    }

    /**
     * Récupère les N dernières parties d'un utilisateur
     */
    public List<GameStats> getRecentUserStats(String username, int limit) {
        return getUserStats(username).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Calcule les statistiques globales d'un utilisateur
     */
    public UserStatsSummary getUserStatsSummary(String username) {
        List<GameStats> userStats = getUserStats(username);

        if (userStats.isEmpty()) {
            return new UserStatsSummary(); // Stats vides
        }

        UserStatsSummary summary = new UserStatsSummary();
        summary.username = username;
        summary.totalGames = userStats.size();
        summary.totalVictories = (int) userStats.stream().mapToLong(s -> s.isVictory() ? 1 : 0).sum();
        summary.winRate = (double) summary.totalVictories / summary.totalGames * 100;
        summary.bestScore = userStats.stream().mapToInt(GameStats::getFinalScore).max().orElse(0);
        summary.averageScore = userStats.stream().mapToInt(GameStats::getFinalScore).average().orElse(0);
        summary.totalPlayTime = userStats.stream().mapToLong(GameStats::getGameDurationSeconds).sum();
        summary.averageGameDuration = summary.totalPlayTime / summary.totalGames;

        // Stats détaillées
        summary.totalEnemiesKilled = userStats.stream().mapToInt(GameStats::getEnemiesKilled).sum();
        summary.totalWallsDestroyed = userStats.stream().mapToInt(GameStats::getWallsDestroyed).sum();
        summary.totalPowerUpsCollected = userStats.stream().mapToInt(GameStats::getPowerUpsCollected).sum();
        summary.bestCombo = userStats.stream().mapToInt(GameStats::getMaxCombo).max().orElse(0);

        // Modes de jeu favoris
        Map<String, Long> modeCount = userStats.stream()
                .collect(Collectors.groupingBy(GameStats::getGameMode, Collectors.counting()));
        summary.favoriteGameMode = modeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("SOLO");

        return summary;
    }

    /**
     * Obtient le classement global des joueurs
     */
    public List<LeaderboardEntry> getGlobalLeaderboard(int limit) {
        Map<String, List<GameStats>> statsByUser = allGameStats.stream()
                .collect(Collectors.groupingBy(GameStats::getUsername));

        return statsByUser.entrySet().stream()
                .map(entry -> {
                    String username = entry.getKey();
                    List<GameStats> stats = entry.getValue();
                    int bestScore = stats.stream().mapToInt(GameStats::getFinalScore).max().orElse(0);
                    int totalGames = stats.size();
                    int victories = (int) stats.stream().mapToLong(s -> s.isVictory() ? 1 : 0).sum();
                    double winRate = totalGames > 0 ? (double) victories / totalGames * 100 : 0;

                    return new LeaderboardEntry(username, bestScore, totalGames, victories, winRate);
                })
                .sorted((a, b) -> Integer.compare(b.bestScore, a.bestScore)) // Tri par meilleur score
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Sauvegarde les statistiques en format texte simple
     */
    private void saveStats() {
        try {
            File file = new File(STATS_FILE);
            file.getParentFile().mkdirs();

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (GameStats stats : allGameStats) {
                    writer.println(statsToString(stats));
                }
            }
            System.out.println("💾 " + allGameStats.size() + " statistiques sauvegardées");
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la sauvegarde des stats: " + e.getMessage());
        }
    }

    /**
     * Charge les statistiques depuis le fichier texte
     */
    private void loadStats() {
        try {
            File file = new File(STATS_FILE);
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        GameStats stats = statsFromString(line);
                        if (stats != null) {
                            allGameStats.add(stats);
                        }
                    }
                }
                System.out.println("📊 " + allGameStats.size() + " statistiques chargées");
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement des stats: " + e.getMessage());
            allGameStats = new ArrayList<>();
        }
    }

    /**
     * Convertit un GameStats en string pour la sauvegarde
     */
    private String statsToString(GameStats stats) {
        return String.join("|",
                stats.getUsername(),
                stats.getGameDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                String.valueOf(stats.isVictory()),
                String.valueOf(stats.getFinalScore()),
                String.valueOf(stats.getGameDurationSeconds()),
                stats.getGameMode() != null ? stats.getGameMode() : "SOLO",
                stats.getMapName() != null ? stats.getMapName() : "default",
                String.valueOf(stats.getEnemiesKilled()),
                String.valueOf(stats.getWallsDestroyed()),
                String.valueOf(stats.getPowerUpsCollected()),
                String.valueOf(stats.getBombsPlaced()),
                String.valueOf(stats.getMaxCombo()),
                String.valueOf(stats.getLivesEarned()),
                stats.getOpponentName() != null ? stats.getOpponentName() : "",
                String.valueOf(stats.getOpponentScore())
        );
    }

    /**
     * Reconstruit un GameStats depuis une string
     */
    private GameStats statsFromString(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length >= 7) {
                GameStats stats = new GameStats();
                stats.setUsername(parts[0]);
                stats.setGameDate(LocalDateTime.parse(parts[1], DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                stats.setVictory(Boolean.parseBoolean(parts[2]));
                stats.setFinalScore(Integer.parseInt(parts[3]));
                stats.setGameDurationSeconds(Long.parseLong(parts[4]));
                stats.setGameMode(parts[5]);
                stats.setMapName(parts[6]);

                // Stats détaillées (optionnelles pour compatibilité)
                if (parts.length > 7) stats.setEnemiesKilled(Integer.parseInt(parts[7]));
                if (parts.length > 8) stats.setWallsDestroyed(Integer.parseInt(parts[8]));
                if (parts.length > 9) stats.setPowerUpsCollected(Integer.parseInt(parts[9]));
                if (parts.length > 10) stats.setBombsPlaced(Integer.parseInt(parts[10]));
                if (parts.length > 11) stats.setMaxCombo(Integer.parseInt(parts[11]));
                if (parts.length > 12) stats.setLivesEarned(Integer.parseInt(parts[12]));
                if (parts.length > 13 && !parts[13].isEmpty()) stats.setOpponentName(parts[13]);
                if (parts.length > 14) stats.setOpponentScore(Integer.parseInt(parts[14]));

                return stats;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du parsing d'une ligne: " + line);
        }
        return null;
    }

    // Classes internes pour les résumés
    public static class UserStatsSummary {
        public String username = "";
        public int totalGames = 0;
        public int totalVictories = 0;
        public double winRate = 0.0;
        public int bestScore = 0;
        public double averageScore = 0.0;
        public long totalPlayTime = 0;
        public double averageGameDuration = 0.0;
        public int totalEnemiesKilled = 0;
        public int totalWallsDestroyed = 0;
        public int totalPowerUpsCollected = 0;
        public int bestCombo = 0;
        public String favoriteGameMode = "SOLO";

        public UserStatsSummary() {
            // Constructeur par défaut
        }
    }

    public static class LeaderboardEntry {
        public final String username;
        public final int bestScore;
        public final int totalGames;
        public final int victories;
        public final double winRate;

        public LeaderboardEntry(String username, int bestScore, int totalGames,
                                int victories, double winRate) {
            this.username = username;
            this.bestScore = bestScore;
            this.totalGames = totalGames;
            this.victories = victories;
            this.winRate = winRate;
        }
    }
}