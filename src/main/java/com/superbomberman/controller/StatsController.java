package com.superbomberman.controller;

import com.superbomberman.model.GameStats;
import com.superbomberman.service.StatsService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour l'écran des statistiques
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-11
 */
public class StatsController implements Initializable {

    @FXML private Label usernameLabel;
    @FXML private Label totalGamesLabel;
    @FXML private Label winRateLabel;
    @FXML private Label bestScoreLabel;
    @FXML private Label averageScoreLabel;
    @FXML private Label totalPlayTimeLabel;
    @FXML private Label enemiesKilledLabel;
    @FXML private Label wallsDestroyedLabel;
    @FXML private Label powerUpsCollectedLabel;
    //@FXML private Label bestComboLabel;

    @FXML private TableView<GameStats> recentGamesTable;
    @FXML private TableColumn<GameStats, String> dateColumn;
    @FXML private TableColumn<GameStats, String> modeColumn;
    @FXML private TableColumn<GameStats, Boolean> resultColumn;
    @FXML private TableColumn<GameStats, Integer> scoreColumn;
    @FXML private TableColumn<GameStats, String> durationColumn;

    @FXML private TableView<StatsService.LeaderboardEntry> leaderboardTable;
    @FXML private TableColumn<StatsService.LeaderboardEntry, String> rankUsernameColumn;
    @FXML private TableColumn<StatsService.LeaderboardEntry, Integer> rankScoreColumn;
    @FXML private TableColumn<StatsService.LeaderboardEntry, Integer> rankGamesColumn;
    @FXML private TableColumn<StatsService.LeaderboardEntry, Double> rankWinRateColumn;

    private StatsService statsService;
    private String currentUsername;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statsService = new StatsService();
        setupTables();
    }

    /**
     * Initialise l'écran avec les données d'un utilisateur
     */
    public void initializeWithUser(String username) {
        this.currentUsername = username;
        loadUserStats();
        loadLeaderboard();
    }

    private void setupTables() {
        // Configuration de la table des parties récentes
        dateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getGameDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                )
        );
        modeColumn.setCellValueFactory(new PropertyValueFactory<>("gameMode"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("victory"));
        resultColumn.setCellFactory(column -> new TableCell<GameStats, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Victoire" : "Défaite");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
        durationColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        formatDuration(cellData.getValue().getGameDurationSeconds())
                )
        );

        // Configuration de la table du classement
        rankUsernameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().username)
        );
        rankScoreColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().bestScore).asObject()
        );
        rankGamesColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().totalGames).asObject()
        );
        rankWinRateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().winRate).asObject()
        );
        rankWinRateColumn.setCellFactory(column -> new TableCell<StatsService.LeaderboardEntry, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", item));
                }
            }
        });
    }

    private void loadUserStats() {
        // ✅ FIX : Correction du nom de la classe
        StatsService.UserStatsSummary summary = statsService.getUserStatsSummary(currentUsername);

        usernameLabel.setText(currentUsername);
        // ✅ FIX : Accès correct aux champs publics
        totalGamesLabel.setText(String.valueOf(summary.totalGames));
        winRateLabel.setText(String.format("%.1f%%", summary.winRate));
        bestScoreLabel.setText(String.valueOf(summary.bestScore));
        averageScoreLabel.setText(String.format("%.0f", summary.averageScore));
        totalPlayTimeLabel.setText(formatDuration(summary.totalPlayTime));
        enemiesKilledLabel.setText(String.valueOf(summary.totalEnemiesKilled));
        wallsDestroyedLabel.setText(String.valueOf(summary.totalWallsDestroyed));
        powerUpsCollectedLabel.setText(String.valueOf(summary.totalPowerUpsCollected));
        //bestComboLabel.setText(String.valueOf(summary.bestCombo));

        // Charger les parties récentes
        List<GameStats> recentGames = statsService.getRecentUserStats(currentUsername, 10);
        ObservableList<GameStats> recentGamesData = FXCollections.observableArrayList(recentGames);
        recentGamesTable.setItems(recentGamesData);
    }

    private void loadLeaderboard() {
        List<StatsService.LeaderboardEntry> leaderboard = statsService.getGlobalLeaderboard(10);
        ObservableList<StatsService.LeaderboardEntry> leaderboardData = FXCollections.observableArrayList(leaderboard);
        leaderboardTable.setItems(leaderboardData);
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }

    @FXML
    private void handleReturnToMenu() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/menu.fxml")
            );
            javafx.scene.Parent menuRoot = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) usernameLabel.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(menuRoot));
            stage.setTitle("Super Bomberman - Menu");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}