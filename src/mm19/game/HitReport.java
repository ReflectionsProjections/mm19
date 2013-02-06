package mm19.game;

import mm19.game.ships.Ship;

/**
 * @author mm19
 *
 * Immutable object for reporting attacks.
 */
public class HitReport {
    final public int x;
    final public int y;
    final public boolean shotSuccessful;
    final public Ship shipHit;

    public HitReport(int x, int y, boolean hitSuccessful, Ship ship){
        this.x = x;
        this.y = y;
        this.shotSuccessful = hitSuccessful;
        this.shipHit = ship;
    }
}
