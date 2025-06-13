package com.superbomberman.model;

/**
 * Résultat d'une partie de Super Bomberman.
 * <p>
 * Contient toutes les informations finales après une partie :
 * type de fin (victoire/défaite/draw), scores, noms des joueurs, durée, etc.
 * Supporte le mode solo et multijoueur.
 * </p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-12
 */
public class GameResult {
    private final GameEndType endType;
    private final int finalScore;
    private final String player1Name;
    private final String player2Name;
    private final int player1Score;
    private final int player2Score;
    private final long gameDuration;

    /**
     * Constructeur pour le mode solo.
     *
     * @param endType     Type de fin de partie (victoire ou défaite)
     * @param finalScore  Score final du joueur
     * @param gameDuration Durée de la partie en millisecondes
     */
    public GameResult(GameEndType endType, int finalScore, long gameDuration) {
        this.endType = endType;
        this.finalScore = finalScore;
        this.gameDuration = gameDuration;
        this.player1Name = null;
        this.player2Name = null;
        this.player1Score = 0;
        this.player2Score = 0;
    }

    /**
     * Constructeur pour le mode multijoueur.
     *
     * @param endType      Type de fin de partie
     * @param player1Name  Nom du joueur 1
     * @param player1Score Score du joueur 1
     * @param player2Name  Nom du joueur 2
     * @param player2Score Score du joueur 2
     * @param gameDuration Durée de la partie en millisecondes
     */
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

    /**
     * Retourne le type de fin de partie.
     * @return Type de fin (victoire, défaite, etc.)
     */
    public GameEndType getEndType() { return endType; }

    /**
     * Retourne le score final (mode solo ou meilleur score en multi).
     * @return Score final
     */
    public int getFinalScore() { return finalScore; }

    /**
     * Retourne le nom du joueur 1 (null en solo).
     * @return Nom du joueur 1 ou null
     */
    public String getPlayer1Name() { return player1Name; }

    /**
     * Retourne le nom du joueur 2 (null en solo).
     * @return Nom du joueur 2 ou null
     */
    public String getPlayer2Name() { return player2Name; }

    /**
     * Retourne le score du joueur 1 (0 en solo).
     * @return Score du joueur 1
     */
    public int getPlayer1Score() { return player1Score; }

    /**
     * Retourne le score du joueur 2 (0 en solo).
     * @return Score du joueur 2
     */
    public int getPlayer2Score() { return player2Score; }

    /**
     * Retourne la durée de la partie en millisecondes.
     * @return Durée de la partie
     */
    public long getGameDuration() { return gameDuration; }

    /**
     * Indique si le résultat concerne une partie solo.
     * @return true si solo, false sinon
     */
    public boolean isSoloMode() {
        return endType == GameEndType.SOLO_VICTORY || endType == GameEndType.SOLO_DEFEAT;
    }

    /**
     * Retourne le nom du gagnant en mode multijoueur.
     * @return Nom du gagnant ou null si égalité/solo
     */
    public String getWinnerName() {
        return switch (endType) {
            case MULTI_PLAYER1_WINS -> player1Name;
            case MULTI_PLAYER2_WINS -> player2Name;
            default -> null;
        };
    }

    /**
     * Retourne le nom du perdant en mode multijoueur.
     * @return Nom du perdant ou null si égalité/solo
     */
    public String getLoserName() {
        return switch (endType) {
            case MULTI_PLAYER1_WINS -> player2Name;
            case MULTI_PLAYER2_WINS -> player1Name;
            default -> null;
        };
    }
}