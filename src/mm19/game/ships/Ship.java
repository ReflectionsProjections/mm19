package mm19.game.ships;

/**
 * 
 * @author mm19
 * 
 * An abstract base class for all ships in the game
 *
 */
public abstract class Ship {

	// x,y passed to cstructor - cstructor sets other vars on super class
	
	private final int uId; 	    // [Presumption] Type of ship
	private int x;       			// X coordinate of ship on board
	private int y;					// Y coordinate of ship on board
	private final int boatLength;   // Length of the boat (boats are always of width 1)
	private int health;			    // Health of the boat
	private boolean isAlive = true; // Whether or not the ship is alive
	private Orientation orientation;
	
	// Enum for orientation
	public static enum Orientation { HORIZONTAL, VERTICAL }
	
	// Constructor
	public Ship(int uID, int boatLength) // x, y, orientation - g/s
	{
		this.uId = uID;
		this.boatLength = boatLength;
	}
	
	// ---- Getters ----
	public int getX()
	{
		return this.x;
	}
	public int getY()
	{
		return this.y;
	}
	
	public Orientation getOrientation()
	{
		return this.orientation;
	}
	
	public int getLength()
	{
		return this.boatLength;
	}
	
	public int getID()
	{
		return this.uId;
	}
	
	// ---- Setters ----
	public void setX(int x)
	{
		this.x = x;
	}
	
	public void setY(int y)
	{
		this.y = y;
	}
	
	public void setOrientation(Orientation o)
	{
		this.orientation = o;
	}
	
	// ---- Damage ----
	public int applyDamage(int damage)
	{
		// If damage is <= 0, nothing happens
		this.health -= Math.max(damage, 0);
		
		// Update isAlive
		this.isAlive = (this.health > 0);
		
		// Return health
		return this.health;
	}
}
