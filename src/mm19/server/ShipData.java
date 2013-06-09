package mm19.server;

import mm19.game.Constants;
import mm19.game.board.Position;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
 * This Ship Data class is a java representation  of the JSON the server will 
 * send detailing each ship.
 */
public class ShipData {
	final public int health;
	final public int ID;
	final public String type;
	final public int xCoord;
	final public int yCoord;
	final public Position.Orientation orientation;
	
	public ShipData(int health, int ID, String type,int xCoord, int yCoord,Position.Orientation orientation){
		this.health = health;
		this.ID = ID;
		this.type = type;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.orientation = orientation;
	}

    /**
     * TODO Description goes here
     *
     * @param obj TODO: This file needs more javadoc
     * @return A valid ShipData if the given JSONObject contains such, null otherwise
     */
    public static ShipData fromJSON(JSONObject obj) {
        int health;
        int ID;
        String type;
        int xCoord;
        int yCoord;
        String orientationIdentifier;

        try {
            if (obj.has("health") && obj.has("ID") && obj.has("type") && obj.has("xCoord")
                    && obj.has("yCoord") && obj.has("orientation")) {

                health = obj.getInt("health");
                ID = obj.getInt("ID");
                type = obj.getString("type");
                xCoord = obj.getInt("xCoord");
                yCoord = obj.getInt("yCoord");
                orientationIdentifier = obj.getString("orientation");
            } else {
                return null;
            }

            //TODO: Determine why ID must not be 0.  0 seems like a reasonable value. -Eric
            if (health != 0 && ID != 0 && !type.equals("") && xCoord > -1 && xCoord < Constants.BOARD_SIZE
                    && yCoord > -1 && yCoord < Constants.BOARD_SIZE && !orientationIdentifier.equals("")) {

                Position.Orientation orientation = Position.getOrientationByIdentifier(orientationIdentifier);
                return new ShipData(health, ID, type, xCoord, yCoord, orientation);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates ShipData objects using JSON data
     *
     * @param shipsJSONArray JSONArray containing the data need to create ShipData objects
     * @return An ArrayList of ShipData objects.
     */
    public static ArrayList<ShipData> fromJSONArray(JSONArray shipsJSONArray) {
        try {
            ArrayList<ShipData> ships = new ArrayList<ShipData>();
            for(int i = 0; i < shipsJSONArray.length(); i++) {
                ShipData shipData = fromJSON(shipsJSONArray.getJSONObject(i));
                if(shipData != null) {
                    ships.add(shipData);
                }

            }
            return ships;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
