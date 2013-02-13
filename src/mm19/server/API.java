package mm19.server;
import java.util.ArrayList;

import mm19.game.Action;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

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
	public void decode(JSONObject json){
		try {
			JSONArray Actions = (JSONArray)json.get("shipActions");
			JSONObject temp;
			ArrayList<Action> actionList = new ArrayList<Action>();
			for(int i = 0; i < Actions.size(); i++)	{
				temp = (JSONObject) Actions.get(i);
				actionList.add(new Action((int)temp.get("ID"), (int)temp.get("actionID"), 
						(int)temp.get("actionX"), (int)temp.get("actionY"), (int)temp.get("extra")));
			}
			//playerTurn((int)json.get("PlayerID"), actionList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void writeP1(Result r){
		
	}
	
	public void writeP2(Result r){
		
	}
	
	public void send(int PlayerID){
		
	}
}
