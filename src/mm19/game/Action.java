package mm19.game;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Action {

	// Class variables
	final public Type actionID;
	final public int shipID;
	final public int actionXVar;
	final public int actionYVar;
	final public int actionExtraVar;

    public static final String SHOOT_IDENTIFIER = "F";
    public static final String BURST_SHOT_IDENTIFIER = "BS";
    public static final String SONAR_IDENTIFIER = "S";
    public static final String MOVE_HORIZONTAL_IDENTIFIER = "MH";
    public static final String MOVE_VERTICAL_IDENTIFIER = "MV";
    public static final String NOTHING_IDENTIFIER = "N";

    public enum Type {BURST_SHOT, MOVE_HORIZONTAL, MOVE_VERTICAL, SHOOT, SONAR, NOTHING, INVALID}

	// Variable-initializing constructor
	public Action(int shipID, Type actionID, int x, int y, int extra)
	{
		// Initialize variables
		this.shipID = shipID;
		this.actionID = actionID;
		this.actionXVar = x;
		this.actionYVar = y;
		this.actionExtraVar = extra;
	}

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

    /**
     * TODO Description goes here
     *
     * @param obj TODO: This file needs more javadoc
     * @return - returns the associated Action if the given JSON object contains
     *         a valid Action, null otherwise
     */
    public static Action fromJSON(JSONObject obj) {
        try {
            if (obj.has("actionID") && obj.has("ID") && obj.has("actionX") && obj.has("actionY") && obj.has("actionExtra")) {
                String actionID = obj.getString("actionID");
                int shipID = obj.getInt("ID");
                int actionX = obj.getInt("actionX");
                int actionY = obj.getInt("actionY");
                //TODO Determine what goes in actionExtra and why it is an int instead of an enum
                int actionExtra = obj.getInt("actionExtra");

                Type actionType = getTypeByIdentifier(actionID);
                return new Action(shipID, actionType, actionX, actionY, actionExtra);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * TODO Description goes here
     *
     * @param actionsJSONArray TODO: This file needs more javadoc
     * @return An ArrayList of Actions if the given JSONArray contains such, null otherwise
     */
    public static ArrayList<Action> fromJSONArray(JSONArray actionsJSONArray) {
        ArrayList<Action> actions = new ArrayList<Action>();

        //TODO Determine if iterating in reverse order causes actions to occur in order player intended -Eric
        for (int i = actionsJSONArray.length()-1; i >= 0; i--) {
            try {
                JSONObject actionJSON = actionsJSONArray.getJSONObject(i);
                Action action = fromJSON(actionJSON);
                if (action != null) {
                    actions.add(action);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return actions;
    }
}
