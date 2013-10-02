package mm19.api;

import mm19.exceptions.ActionException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author mm19
 * 
 * Immutable object for player actions
 */
public class Action {

	// Class variables
	final public Type actionID;
	final public int shipID;
	final public int actionXVar;
	final public int actionYVar;
	final public int actionExtraVar;

    public static final String SHOOT_IDENTIFIER = "F";
    public static final String BURST_SHOT_IDENTIFIER = "B";
    public static final String SONAR_IDENTIFIER = "S";
    public static final String MOVE_HORIZONTAL_IDENTIFIER = "MH";
    public static final String MOVE_VERTICAL_IDENTIFIER = "MV";
    public static final String NOTHING_IDENTIFIER = "N";

    public enum Type {BURST_SHOT, MOVE_HORIZONTAL, MOVE_VERTICAL, SHOOT, SONAR, NOTHING, INVALID}

	/**
	 * Constructor
	 * 
	 * @param shipID
	 * 			The ships's unique identifier
	 * @param actionID
	 * 			The action to take
	 * @param x
	 * 			The x coordinate that action takes place
	 * @param y
	 * 			The y coordinate that action takes place
	 * @param extra
	 */
	public Action(int shipID, Type actionID, int x, int y, int extra)
	{
		// Initialize variables
		this.shipID = shipID;
		this.actionID = actionID;
		this.actionXVar = x;
		this.actionYVar = y;
		this.actionExtraVar = extra;
	}
	
	/**
	 * Constructor 
	 * @param json
	 * 			The JSON to be parsed
	 * @throws ActionException
	 */
	public Action(JSONObject json) throws ActionException{
		try {
			shipID = json.getInt("ID");
			actionID = getTypeByIdentifier(json.getString("actionID"));
			actionXVar = json.getInt("actionX");
			actionYVar = json.getInt("actionY");
			actionExtraVar = json.getInt("actionExtra");
		} catch(JSONException e) {
			throw new ActionException(e.getMessage());
		}
	}

	/**
	 * Get the Type enumerated variable from a String.
	 * @param identifier
	 * 			The string identifier
	 * @return The enumerated Type
	 */
    public static Type getTypeByIdentifier(String identifier) {
        if (identifier.equals(BURST_SHOT_IDENTIFIER)) {
            return Type.BURST_SHOT;
        } else if (identifier.equals(MOVE_HORIZONTAL_IDENTIFIER)) {
            return Type.MOVE_HORIZONTAL;
        } else if (identifier.equals(MOVE_VERTICAL_IDENTIFIER)) {
            return Type.MOVE_VERTICAL;
        } else if (identifier.equals(NOTHING_IDENTIFIER)) {
            return Type.NOTHING;
        } else if (identifier.equals(SHOOT_IDENTIFIER)) {
            return Type.SHOOT;
        } else if (identifier.equals(SONAR_IDENTIFIER)){
            return Type.SONAR;
        } else {
            return Type.INVALID;
        }
    }

    /**
     * Get the string identifier from the enumerated Type
     * @param actionType
     * 			The enumerated Type
     * @return The string identifier
     */
    public static String getIdentifierByType(Type actionType) {
        if (actionType == Type.BURST_SHOT) {
            return BURST_SHOT_IDENTIFIER;
        } else if (actionType == Type.MOVE_HORIZONTAL) {
            return MOVE_HORIZONTAL_IDENTIFIER;
        } else if (actionType == Type.MOVE_VERTICAL) {
            return MOVE_VERTICAL_IDENTIFIER;
        } else if (actionType == Type.NOTHING) {
            return NOTHING_IDENTIFIER;
        } else if (actionType == Type.SHOOT) {
            return SHOOT_IDENTIFIER;
        } else if (actionType == Type.SONAR){
            return SONAR_IDENTIFIER;
        } else {
            return "";
        }
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("ID", shipID);
        json.put("actionID", getIdentifierByType(actionID));
        json.put("actionX", actionXVar);
        json.put("actionY", actionYVar);
        json.put("actionExtra", actionExtraVar);
        return json;
    }
}
