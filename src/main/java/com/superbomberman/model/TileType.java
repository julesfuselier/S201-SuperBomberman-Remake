package com.superbomberman.model;

/**
 * Enumération représentant les différents types de tuiles (cases) dans Super Bomberman.
 * Chaque type possède un symbole associé pour l'import/export des cartes.
 *
 * <ul>
 *     <li>WALL : Mur indestructible</li>
 *     <li>PLAYER1 : Position initiale du joueur 1</li>
 *     <li>PLAYER2 : Position initiale du joueur 2</li>
 *     <li>ENEMY : Position initiale de l'ennemi</li>
 *     <li>BOMB : Bombe posée</li>
 *     <li>POWER_UP : Case contenant un power-up</li>
 *     <li>EXIT : Sortie/niveau terminé</li>
 *     <li>WALL_BREAKABLE : Mur destructible</li>
 *     <li>FLOOR : Sol libre</li>
 * </ul>
 *
 * @author Hugo Brest Lestrade
 * @version 1.0
 * @since 2025-06-12
 */
public enum TileType {
    WALL('#'),
    PLAYER1('1'),
    PLAYER2('2'),
    ENEMY('E'),
    BOMB('B'),
    POWER_UP('P'),
    EXIT('X'),
    WALL_BREAKABLE('0'),
    FLOOR(' ');

    private final char symbol;

    /**
     * Construit un type de tuile avec le symbole associé.
     * @param symbol Caractère représentant ce type dans les fichiers de carte
     */
    TileType(char symbol) {
        this.symbol = symbol;
    }

    /**
     * Retourne le symbole associé à ce type de tuile.
     * @return le caractère symbole
     */
    public char getSymbol() {
        return this.symbol;
    }

    /**
     * Retourne le type de tuile correspondant à un symbole donné.
     * @param symbol caractère à tester
     * @return le {@link TileType} correspondant, ou FLOOR si inconnu
     */
    public static TileType fromSymbol(char symbol) {
        for (TileType type : TileType.values()) {
            if (type.symbol == symbol) {
                return type;
            }
        }
        return FLOOR; // Valeur par défaut si le symbole n'est pas reconnu
    }
}