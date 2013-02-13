package mm19.game;

public class Action {

	// Class variables
	public Ability.Type actionID;
	public int shipID;
	public int actionXVar;
	public int actionYVar;
	public int actionExtraVar;

	
	// Variable-initializing constructor
	public Action(int shipID, Ability.Type actionID, int x, int y, int extra)
	{
		// Initialize variables
		this.shipID = shipID;
		this.actionID = actionID;
		this.actionXVar = x;
		this.actionYVar = y;
		this.actionExtraVar = extra;
	}
}
