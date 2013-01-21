package mm19.game.ships;

/**
 * 
 * @author mm19
 * 
 * The ship that a player starts out with, can  fire, and move.
 * Type: 0
 * Length: 5
 *
 */
public class MainShip extends Ship{
	public MainShip(int x, int y, Orientation o){
		super(0,5);
		setX(x);
		setY(y);
		setOrientation(o);
	}
}
