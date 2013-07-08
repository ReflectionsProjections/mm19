package mm19.api;

import org.json.JSONException;
import org.json.JSONObject;

import	mm19.game.ships.*;

public class SonarReport {
	final public int dist;
	final public Ship ship;
	
	public SonarReport(int dist, Ship ship){
		this.dist = dist;
		this.ship = ship;
	}
	
	public JSONObject toJSON() throws JSONException{
		JSONObject json = new JSONObject();
		json.put("shipID", ship.getID());
		json.put("distance", dist);
		return json;
	}
}
