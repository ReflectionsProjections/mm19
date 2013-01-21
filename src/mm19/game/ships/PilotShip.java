package mm19.game.ships;

/**
 * 
 * @author mm19
 * 
 * Pilot ships can only move, but gather resources.
 * Type: 2
 * Length: 2
 *
 */
public class PilotShip extends Ship{
	public PilotShip(int x, int y, Orientation o){
		super(2,2);
		setX(x);
		setY(y);
		setOrientation(o);
	}
}
