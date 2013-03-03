package mm19.server;
import java.util.ArrayList;

import mm19.game.Action;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author mm19
 * 
 * This will tie together the server and game logic.
 * Functions defined here will be called by the server to interface properly with the game.
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

	public void decode(JSONObject json){
		try {
			
			JSONArray Actions = (JSONArray)json.get("shipActions");
			JSONObject temp;
			ArrayList<Action> actionList = new ArrayList<Action>();
			for(int i = 0; i < Actions.length(); i++)	{
				temp = (JSONObject) Actions.get(i);
				actionList.add(new Action(temp.getInt("ID"), temp.getInt("actionID"),
						temp.getInt("actionX"), temp.getInt("actionY"), temp.getInt("extra")));
			}
			//playerTurn((int)json.get("PlayerID"), actionList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public void writeResultBoth(ServerResponse r){
		//writeResultP1(r);
		//writeResultP2(r);
	}

	public void writeP1(ServerResponse r){
		
		// Create new JSON object
		JSONObject jsonObj = new JSONObject();
		
		// Add values to JSON object
		jsonObj.put("error", r.error);
		jsonObj.put("playerId", r.playerID);
		jsonObj.put("playerName", r.playerName);
		jsonObj.put("resources", r.resources);
		jsonObj.put("ships", r.ships); // Does this work / Is this necessary?
		
		// Save JSON object
		player1results.add(jsonObj); // This will need changing
		
	}
	
	public void writeP2(ServerResponse r){
		
		// Create new JSON object
		JSONObject jsonObj = new JSONObject();
		
		// Add values to JSON object
		jsonObj.put("error", r.error);
		jsonObj.put("playerId", r.playerID);
		jsonObj.put("playerName", r.playerName);
		jsonObj.put("resources", r.resources);
		jsonObj.put("ships", r.ships); // Does this work / Is this necessary?
		
		// Save JSON object
		player1results.add(jsonObj); // This will need changing
		
	}
	
	public void send(int PlayerID){
		
	}
}
