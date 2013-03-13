package mm19.game;

public class Action {

	// Class variables
	final public int actionID;
	final public int shipID;
	final public int actionXVar;
	final public int actionYVar;
	final public int actionExtraVar;

	
	// Variable-initializing constructor
	public Action(int shipID, int i, int x, int y, int extra)
	{
		// Initialize variables
		this.shipID = shipID;
		this.actionID = i;
		this.actionXVar = x;
		this.actionYVar = y;
		this.actionExtraVar = extra;
	}
}
