package mm19.api;

import mm19.exceptions.ShipDataException;
import mm19.game.board.Position;
import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * This Ship Data class is a java representation of the JSON the server will 
 * send detailing each ship.
 * 
 * Note: This class does not need to do error checking. Any logic for correct positioning,
 * resources, etc. Will be handled by the engine.
 */
public class ShipData {
	final public int health;
	final public int ID;
	final public String type;
	final public int xCoord;
	final public int yCoord;
	final public Position.Orientation orientation;

	public ShipData(int health, int ID, String type, int xCoord, int yCoord,
			Position.Orientation orientation) {
		this.health = health;
		this.ID = ID;
		this.type = type;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.orientation = orientation;
	}

	public ShipData(JSONObject json) throws ShipDataException {
		try {

			// Health and ID is not necessary for initializing ShipData through
			// a JSONObject, the Engine logic will have the data within the game
			health = json.optInt("health");
			ID = json.optInt("ID");
			type = json.getString("type");
			xCoord = json.getInt("xCoord");
			yCoord = json.getInt("yCoord");
			String orientationIdentifier = json.getString("orientation");
			orientation = Position
					.getOrientationByIdentifier(orientationIdentifier);
		} catch (JSONException e) {
			throw new ShipDataException("ShipDataException: " + e.getMessage());
		}
		if(!validType(type)) throw new ShipDataException("ShipDataException: Invalid Ship Type: " + type);
	}
	
	private boolean validType(String type){
	    if(!type.equalsIgnoreCase(PilotShip.IDENTIFIER) && 
	            !type.equalsIgnoreCase(MainShip.IDENTIFIER) && 
	            !type.equalsIgnoreCase(DestroyerShip.IDENTIFIER))
	        return false;
	    else return true;
	}

	public JSONObject toJSON() throws ShipDataException {
		JSONObject json = new JSONObject();
		try {
			json.put("health", health);
			json.put("ID", ID);
			json.put("type", type);
			json.put("xCoord", xCoord);
			json.put("yCoord", yCoord);
			json.put("orientation",
					Position.getIdentifierByOrientation(orientation));
		} catch (JSONException e) {
			// This should never happen.
			throw new ShipDataException("ShipDataException: " + e.getMessage());
		}
		return json;
	}
}
