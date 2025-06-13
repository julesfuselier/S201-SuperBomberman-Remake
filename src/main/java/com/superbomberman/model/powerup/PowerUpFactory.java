package com.superbomberman.model.powerup;

/**
 * Usine (Factory) pour créer des instances de {@link PowerUp} en fonction de leur type.
 *
 * <p>Cette classe fournit une méthode statique pour instancier le power-up correspondant
 * au type demandé, avec une position donnée.</p>
 *
 * <p>Elle utilise un switch sur l'énumération {@link PowerUpType} pour retourner la bonne instance.</p>
 *
 * @author Jules Fuselier
 * @version 1.0
 * @since 2025-06-13
 */
public class PowerUpFactory {

    /**
     * Crée une instance de {@link PowerUp} correspondant au type donné,
     * positionnée aux coordonnées spécifiées.
     *
     * @param type le type de power-up à créer
     * @param x la coordonnée X de la position du power-up
     * @param y la coordonnée Y de la position du power-up
     * @return une instance de {@link PowerUp} correspondant au type
     */
    public static PowerUp create(PowerUpType type, int x, int y) {
        return switch (type) {
            case RANGE_UP   -> new RangeUp(x, y);
            case BOMB_UP    -> new BombUp(x, y);
            case SPEED_UP   -> new SpeedUp(x, y);
            case KICK       -> new KickPower(x, y);
            case GLOVE      -> new GlovePower(x, y);
            case REMOTE     -> new RemotePower(x, y);
            case WALL_PASS  -> new WallPass(x, y);
            case BOMB_PASS  -> new BombPass(x, y);
            case LINE_BOMB  -> new LineBomb(x, y);
            case SKULL      -> new SkullMalus(x, y);
        };
    }

}
