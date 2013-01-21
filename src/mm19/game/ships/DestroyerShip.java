package mm19.game.ships;

/**
 * 
 * @author mm19
 * 
 * Destroyers can fire and move.
 * Type: 1
 * Length: 4
 * 
 *
 */
public class DestroyerShip extends Ship{
	public DestroyerShip(int x, int y, Orientation o){
		super(1,4);
		setX(x);
		setY(y);
		setOrientation(o);
	}
}
