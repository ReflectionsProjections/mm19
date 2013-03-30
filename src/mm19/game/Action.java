package mm19.game;

public class Action {

	// Class variables
	final public String actionID;
	final public int shipID;
	final public int actionXVar;
	final public int actionYVar;
	final public int actionExtraVar;

	
	// Variable-initializing constructor
	public Action(int shipID, String i, int x, int y, int extra)
	{
		// Initialize variables
		this.shipID = shipID;
		this.actionID = i;
		this.actionXVar = x;
		this.actionYVar = y;
		this.actionExtraVar = extra;
	}
}
