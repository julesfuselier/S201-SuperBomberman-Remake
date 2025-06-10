package com.superbomberman.model;



public class GameResult {
    private final GameEndType endType;
    private final int finalScore;
    private final String player1Name;
    private final String player2Name;
    private final int player1Score;
    private final int player2Score;
    private final long gameDuration;

    // Constructeur pour mode solo
    public GameResult(GameEndType endType, int finalScore, long gameDuration) {
        this.endType = endType;
        this.finalScore = finalScore;
        this.gameDuration = gameDuration;
        this.player1Name = null;
        this.player2Name = null;
        this.player1Score = 0;
        this.player2Score = 0;
    }

    // Constructeur pour mode multijoueur
    public GameResult(GameEndType endType, String player1Name, int player1Score,
                      String player2Name, int player2Score, long gameDuration) {
        this.endType = endType;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.gameDuration = gameDuration;
        this.finalScore = Math.max(player1Score, player2Score);
    }

    // Getters
    public GameEndType getEndType() { return endType; }
    public int getFinalScore() { return finalScore; }
    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }
    public int getPlayer1Score() { return player1Score; }
    public int getPlayer2Score() { return player2Score; }
    public long getGameDuration() { return gameDuration; }

    public boolean isSoloMode() {
        return endType == GameEndType.SOLO_VICTORY || endType == GameEndType.SOLO_DEFEAT;
    }

    public String getWinnerName() {
        return switch (endType) {
            case MULTI_PLAYER1_WINS -> player1Name;
            case MULTI_PLAYER2_WINS -> player2Name;
            default -> null;
        };
    }

    public String getLoserName() {
        return switch (endType) {
            case MULTI_PLAYER1_WINS -> player2Name;
            case MULTI_PLAYER2_WINS -> player1Name;
            default -> null;
        };
    }
}
