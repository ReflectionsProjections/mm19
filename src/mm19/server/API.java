package mm19.server;
import java.util.ArrayList;

import mm19.game.Action;
import mm19.game.HitReport;
import mm19.game.SonarReport;
import mm19.game.ships.Ship;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author mm19
 * 
 * This will tie together the server and game logic.
 * Functions defined here are called by the server to interface properly with the game.
 *
 */
public class API {

	JSONObject player1;
	JSONObject player2;
	int p1ID;
	int p2ID;
	JSONArray player1results;
	JSONArray player2results;
	
	JSONArray player1ships;
	JSONArray player2ships;
	private final int MAX_SIZE = 100; // temporary holder variable move to constants
	
	
	public boolean newData(JSONObject obj)
	{
		String playerName;
		ShipData MainShip;
		JSONArray shiparr;
		ArrayList<ShipData> ships;
		
		try {
			if(obj.has("PlayerName") && 
					((playerName = obj.getString("PlayerName")) != null) )
			{
				if(obj.has("mainShip") && 
						((MainShip = getShip((JSONObject)obj.get("MainShip"))) != null))
				{
					if(obj.has("Ships") && 
							((shiparr = (JSONArray)obj.get("Ships")) != null))
					{
						if((ships = getShipList(shiparr)) != null){
							//TODO send data to engine
							return true; 						
						}
					}
				}
				
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return false;
		
	}
	
	/**
	 * @param ship
	 * @return returns a valid ship if the given JSONObject contains 
	 * 			such, null otherwise
	 */
	private ShipData getShip(JSONObject ship){		
		int health;
		int ID;
		String type;
		int xCoord;
		int yCoord;
		String orientation;
		
		// TODO Auto-generated method stub
		try {
			if(ship.has("health") && (health = ship.getInt("health")) != 0)
			{
				if(ship.has("ID") && (ID = ship.getInt("ID")) != 0)
				{
					if(ship.has("type") && (type = ship.getString("type")).equals(""))
					{
						if(ship.has("xCoord") && (xCoord = ship.getInt("xCoord")) > -1 && xCoord < MAX_SIZE)
						{
							if(ship.has("yCoord") && (yCoord = ship.getInt("yCoord")) > -1 && yCoord < MAX_SIZE)
							{
								if(ship.has("orientation") && !(orientation = ship.getString("orientation")).equals(""))
								{
									// Success
									return new ShipData(health, ID, type, xCoord, yCoord, orientation);
								}
							}
						}
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Failure
		return null;
	}
	
	/**
	 * @param shiparr
	 * @return returns the full arrayList of 20 valid ships if the given
	 * 			JSONArray contains such, any error will cause this function 
	 * 			return null
	 */
	private ArrayList<ShipData> getShipList(JSONArray shipArr) {
		
		if(shipArr.length() != 19) return null;
		int length = shipArr.length();
		ArrayList<ShipData> list = new ArrayList<ShipData>();
		ShipData tempShip;
		JSONObject tempJson;
		while(length > 0){
			length--;
			try {
				tempJson = shipArr.getJSONObject(length);
				if((tempShip = getShip(tempJson)) != null) list.add(tempShip);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
		
	}

	
	
	public boolean decodeTurn(JSONObject obj){
		int playerID;
		ArrayList<Action> actionList;
		try {
			if(obj.has("PlayerID") && 
					((playerID = obj.getInt("PlayerID")) > 0)) //we assume playerID's are nonzero and nonnegative
			{
				if(obj.has("shipActions") 
						&& ((actionList = getActionList((JSONArray)obj.get("shipActions"))) != null))
				{
					//TODO send data to engine
					return true;
				}
			}
			
		} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * @param obj
	 * @return - returns the associated Action in the given 
	 * 				JSON object contains a valid Action, null otherwise
	 */
	private Action getAction(JSONObject obj) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * @param jsonArray
	 * @return returns the valid List of Actions if the given 
	 * 				JSONarray contains such, and null otherwise
	 */
	private ArrayList<Action> getActionList(JSONArray jsonArray) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param status - enum that tells us to write to player1, player2 or both
	 * @param ships - array list of current player ships/status
	 * @return - true if sucessful write
	 */
	public boolean writePlayerShips(int status, ArrayList<ShipData> ships){
		JSONArray shipsJson = new JSONArray();
		JSONObject tempShip;
		int length = ships.size();
		while(length > 0){
			length --;
			if((tempShip = makeShipJSON(ships.get(length)))!=null)
				shipsJson.put(tempShip);
		}
		if(status == 1)
		{
			//TODO write conditions
			return true;
			
		}
		return false;
	}
	
	/**
	 * @param data - the data of a given ship
	 * @return - a jsonobject containing said data
	 */
	private JSONObject makeShipJSON(ShipData data)
	{
		JSONObject tempShip = new JSONObject();
		
		
		try {
			tempShip.append("health", data.health);
			tempShip.append("ID", data.ID); 
			tempShip.append("type", data.type); 
			tempShip.append("xCoord", data.xCoord);
			tempShip.append("yCoord", data.yCoord);
			tempShip.append("orientation", data.orientation);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return tempShip;
	}
	
	/* may be redundant
	public boolean writePlayerShipActions(int status, ArrayList<ActionReport> acts){
		//TODO write action results to json
	}
	
	private JSONObject makeShipActionJSON(ActionReport act)
	{
		return null;
	}
	*/
	
	/**
	 * @param status - enum that tells us to write to player1, player2 or both
	 * @param hits - array list of current player hit reports
	 * @return - true if sucessful write
	 */
	public boolean writePlayerHits(int status, ArrayList<HitReport> hits){
		//TODO write hitdata to json
		return false;
	}
	
	/**
	 * @param report - the data of a given hitreport
	 * @return - a jsonobject containing said data
	 */
	private JSONObject makeHitJSON(HitReport report)
	{
		return null;
	}
	
	/**
	 * @param status - enum that tells us to write to player1, player2 or both
	 * @param pings - array list of current player pings
	 * @return - true if sucessful write
	 */
	public boolean writePlayerPings(int status, ArrayList<SonarReport> pings){
		//TODO write sonar reports to json
		return false;
	}
	
	/**
	 * @param ping - the data of a given ping
	 * @return - a jsonobject containing said data
	 */
	private JSONObject makePingJSON(SonarReport ping)
	{
		return null;
	}
	
	//TODO error gen
	/*
	public boolean writePlayerErrors(int status, ArrayList<ErrorReport> errs){
		//TODO write errors reports to json
		return false;
	}

	private JSONObject makeErrJSON(SonarReport ping)
	{
		return null;
	}
	*/
		
	/**
	 * @param status  - enum that tells us to write to player1, player2 or both
	 * @param PlayerID - given Player's ID
	 * @param PlayerName - given Player's Name
	 * @param resources - given Player's remaining resources
	 * @return - true if successful send
	 */
	public boolean send(int status, int PlayerID, String PlayerName, int resources){
		return false;
	}
}
