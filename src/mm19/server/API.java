package mm19.server;

import java.util.ArrayList;

import mm19.game.Action;
import mm19.game.Engine;
import mm19.game.HitReport;
import mm19.game.ShipActionResult;
import mm19.game.SonarReport;
import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author mm19
 * 
 *         This will tie together the server and game logic. Functions defined
 *         here are called by the server to interface properly with the game.
 * 
 */
public class API {

	private static JSONObject[] playerTurnObj;
	private static String[] playerToken;
	private static String[] playerName;
	private static Engine game;
	private static int ID = 0;
	private static final int MAX_SIZE = 100; // temporary holder variable move to
										// constants

	public static boolean initAPI() {
		playerTurnObj = new JSONObject[2];
		playerTurnObj[0] = new JSONObject();
		playerTurnObj[1] = new JSONObject();
		
		playerToken = new String[2];
		playerToken[0] = "";
		playerToken[1] = "";
		
		playerName = new String[2];
		playerName[0] = "";
		playerName[1] = "";
		
		game = new Engine();
		return true;
	}

	public static boolean newData(JSONObject obj, String playerToken) {
		int playerID;
		game = new Engine();
		String playerName;
		JSONObject mainShipObj;
		ShipData mainShip;
		JSONArray shiparr;
		ArrayList<ShipData> ships;

		try {
			if (obj.has("playerName")
					&& ((playerName = obj.getString("playerName")) != null)) {
				
				if (obj.has("mainShip")
						&& ((mainShipObj = (JSONObject) obj.get("mainShip")) != null)) {
					
						mainShipObj.put("type", "M");
						mainShip = initShip(mainShipObj);
						
					if (obj.has("ships")
							&& ((shiparr = (JSONArray) obj.get("ships")) != null)) {
						
						ships = new ArrayList<ShipData>();
						
						for(int i = 0; i < 4; i++) {
							ShipData tempData = initShip(shiparr.getJSONObject(i));
							if(tempData != null) {
								ships.add(tempData);
							}
						}
	
						ships.add(mainShip);
						playerID = game.playerSet(ships, playerName);
						
						if(playerID == -1) {
							return false;
						}
						
						API.playerName[playerID] = playerName;
						API.playerToken[playerID] = playerToken;
						
						writePlayer(playerID, "playerToken", playerToken);
						writePlayer(playerID, "playerName", playerName);
						writePlayer(playerID, "shipActionResults", new JSONArray());
						writePlayer(playerID, "hitReport", new JSONArray());
						writePlayer(playerID, "pingReport", new JSONArray());
						send(playerID);
						
						return true;
					}
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return false;

	}

	public static boolean decodeTurn(JSONObject obj) {
		// sanity check
		if (playerToken[0].equals("") || playerToken[1].equals("")) {
			return false;
		}
		
		ArrayList<Action> actionList;
		
		try {
			if (obj.has("playerToken"))  {
				String playerToken = obj.getString("playerToken");
				
				if (obj.has("shipActions")
						&& ((actionList = getActionList((JSONArray) obj
								.get("shipActions"))) != null)) {
					game.playerTurn(playerToken, actionList);
					return true;
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static ShipData initShip(JSONObject obj) {
		int health;
		String type;
		int xCoord;
		int yCoord;
		String orientation;

		try {
			if (obj.has("type") 
					&& !(type = obj.getString("type")).equals("")) {
				
				if (obj.has("xCoord") && (xCoord = obj.getInt("xCoord")) > -1
						&& xCoord < MAX_SIZE) {
					
					if (obj.has("yCoord")
							&& (yCoord = obj.getInt("yCoord")) > -1
							&& yCoord < MAX_SIZE) {
						
						if (obj.has("orientation")
								&& !(orientation = obj.getString("orientation"))
										.equals("")) {
							// Success
							health = -1;
							if(type.equals("P")) {
								health = PilotShip.HEALTH;
							}
							else if(type.equals("D")) {
								health = DestroyerShip.HEALTH;
							}
							else if(type.equals("M")) {
								health = MainShip.HEALTH;
							}
							
							if(health == -1)
								return null;
							
							return new ShipData(health, ID++, type, xCoord,
									yCoord, orientation);
						}
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Failure
		return null;

	}

	/**
	 * @param obj
	 * @return returns a valid ship if the given JSONObject contains such, null
	 *         otherwise
	 */
	private static ShipData getShip(JSONObject obj) {
		int health;
		int ID;
		String type;
		int xCoord;
		int yCoord;
		String orientation;

		try {
			if (obj.has("health") && (health = obj.getInt("health")) != 0) {
				if (obj.has("ID") && (ID = obj.getInt("ID")) != 0) {
					if (obj.has("type")
							&& (type = obj.getString("type")).equals("")) {
						if (obj.has("xCoord")
								&& (xCoord = obj.getInt("xCoord")) > -1
								&& xCoord < MAX_SIZE) {
							if (obj.has("yCoord")
									&& (yCoord = obj.getInt("yCoord")) > -1
									&& yCoord < MAX_SIZE) {
								if (obj.has("orientation")
										&& !(orientation = obj
												.getString("orientation"))
												.equals("")) {
									// Success
									return new ShipData(health, ID, type,
											xCoord, yCoord, orientation);
								}
							}
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Failure
		return null;
	}

	/**
	 * @param jsonArray
	 * @return returns the full arrayList of 20 valid ships if the given
	 *         JSONArray contains such, any error will cause this function
	 *         return null
	 */
	private static ArrayList<ShipData> getShipList(JSONArray jsonArray) {

		if (jsonArray.length() != 19)
			return null;
		int length = jsonArray.length();
		ArrayList<ShipData> list = new ArrayList<ShipData>();
		ShipData tempShip;
		JSONObject tempJson;
		while (length > 0) {
			length--;
			try {
				tempJson = jsonArray.getJSONObject(length);
				if ((tempShip = getShip(tempJson)) != null)
					list.add(tempShip);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return list;

	}

	/**
	 * @param obj
	 * @return - returns the associated Action in the given JSON object contains
	 *         a valid Action, null otherwise
	 */
	private static Action getAction(JSONObject obj) {
		String actionID;
		int shipID;
		int actionXVar;
		int actionYVar;
		int actionExtraVar;

		try {
			if (obj.has("actionID")
					&& (actionID = obj.getString("actionID")).isEmpty()) {
				if (obj.has("shipID") && (shipID = obj.getInt("shipID")) != 0) {
					if (obj.has("actionXVar") && obj.has("actionYVar")) {
						actionXVar = obj.getInt("actionXVar");
						actionYVar = obj.getInt("actionYVar");
					} else
						actionXVar = actionYVar = -1;

					if (obj.has("actionExtraVar")) {
						actionExtraVar = obj.getInt("actionExtraVar");
					} else
						actionExtraVar = -1;

					return new Action(shipID, actionID, actionXVar, actionYVar,
							actionExtraVar);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Failure
		return null;
	}

	/**
	 * @param jsonArray
	 * @return returns the valid List of Actions if the given JSONarray contains
	 *         such, and null otherwise
	 */
	private static ArrayList<Action> getActionList(JSONArray jsonArray) {
		if (jsonArray.length() != 19)
			return null;
		int length = jsonArray.length();
		ArrayList<Action> list = new ArrayList<Action>();
		Action tempAction;
		JSONObject tempJson;
		while (length > 0) {
			length--;
			try {
				tempJson = jsonArray.getJSONObject(length);
				if ((tempAction = getAction(tempJson)) != null)
					list.add(tempAction);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	/**
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @param ships
	 *            - array list of current player ships/status
	 * @return - true if sucessful write
	 */
	public static boolean writePlayerShips(int status, ArrayList<ShipData> ships) {
		JSONArray shipsJson = new JSONArray();
		JSONObject tempShip;
		int length = ships.size();
		while (length > 0) {
			length--;
			if ((tempShip = makeShipJSON(ships.get(length))) != null)
				shipsJson.put(tempShip);
		}
		if (writePlayer(status, "ships", (Object) shipsJson))
			return true;
		return false;
	}

	/**
	 * @param data
	 *            - the data of a given ship
	 * @return - a jsonobject containing said data
	 */
	private static JSONObject makeShipJSON(ShipData data) {
		JSONObject tempShip = new JSONObject();

		try {
			tempShip.put("health", data.health);
			tempShip.put("ID", data.ID);
			tempShip.put("type", data.type);
			tempShip.put("xCoord", data.xCoord);
			tempShip.put("yCoord", data.yCoord);
			tempShip.put("orientation", data.orientation);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return tempShip;
	}
	
	public static boolean writePlayerError(int status, String message) {
		try {
			API.playerTurnObj[status].append("error", message);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static boolean writePlayerResponseCode(int status) {
		try {
			if(API.playerTurnObj[status].has("error")) {
				int length = API.playerTurnObj[status].getJSONArray("error").length();
				if(length > 0) {
					return writePlayer(status, "responseCode", 400);
				}
				return writePlayer(status, "responseCode", 200);
			}
			else {
				API.playerTurnObj[status].put("error", new JSONArray());
				return writePlayer(status, "responseCode", 200);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean writePlayerResources(int status, int resources) {
		return writePlayer(status, "resources", status);
	}

	/**
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @param hits
	 *            - array list of current player hit reports
	 * @return - true if sucessful write
	 */
	public static boolean writePlayerHits(int status, ArrayList<HitReport> hits) {
		JSONArray hitsJson = new JSONArray();
		JSONObject tempHit;
		int length = hits.size();
		while (length > 0) {
			length--;
			if ((tempHit = makeHitJSON(hits.get(length))) != null)
				hitsJson.put(tempHit);
		}
		if (writePlayer(status, "hitReport", (Object) hitsJson))
			return true;
		return false;
	}

	/**
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @param hits
	 *            - array list of current player hit reports
	 * @return - true if sucessful write
	 */
	public static boolean writePlayerEnemyHits(int status, ArrayList<HitReport> hits) {
		JSONArray hitsJson = new JSONArray();
		JSONObject tempHit;
		int length = hits.size();
		while (length > 0) {
			length--;
			if ((tempHit = makeHitJSON(hits.get(length))) != null)
				hitsJson.put(tempHit);
		}
		if (writePlayer(status, "enemyHitReport", (Object) hitsJson))
			return true;
		return false;
	}

	/**
	 * @param report
	 *            - the data of a given hitreport
	 * @return - a jsonobject containing said data
	 */
	private static JSONObject makeHitJSON(HitReport report) {
		JSONObject tempHit = new JSONObject();
		try {
			tempHit.put("xCoord", report.x);
			tempHit.put("yCoord", report.y);
			tempHit.put("hit", report.shotSuccessful);

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return tempHit;
	}

	/**
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @param pings
	 *            - array list of current player pings
	 * @return - true if sucessful write
	 */
	public static boolean writePlayerPings(int status, ArrayList<SonarReport> pings) {
		JSONArray pingsJson = new JSONArray();
		JSONObject tempPing;
		int length = pings.size();
		while (length > 0) {
			length--;
			if ((tempPing = makePingJSON(pings.get(length))) != null)
				pingsJson.put(tempPing);
		}
		if (writePlayer(status, "pingReport", (Object) pingsJson))
			return true;

		return false;
	}

	/**
	 * @param ping
	 *            - the data of a given ping
	 * @return - a jsonobject containing said data
	 */
	private static JSONObject makePingJSON(SonarReport ping) {
		JSONObject tempPing = new JSONObject();

		try {
			tempPing.append("distance", ping.dist);
			tempPing.append("shipID", ping.ship.getID());
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return tempPing;
	}

	/**
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @param results
	 *            - array list of current action results
	 * @return - true if sucessful write
	 */
	public static boolean writePlayerResults(int status,
			ArrayList<ShipActionResult> results) {
		JSONArray resultsJson = new JSONArray();
		JSONObject tempResult;
		int length = results.size();
		while (length > 0) {
			length--;
			if ((tempResult = makeResultJSON(results.get(length))) != null)
				resultsJson.put(tempResult);
		}
		if (writePlayer(status, "shipActionResults", (Object) resultsJson))
			return true;

		return false;
	}

	/**
	 * @param result
	 *            - the data of a given result
	 * @return - a jsonobject containing said data
	 */
	private static JSONObject makeResultJSON(ShipActionResult result) {
		JSONObject tempResult = new JSONObject();

		try {
			tempResult.append("ShipID", result.shipID);
			tempResult.append("result", result.result);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return tempResult;
	}
	
	public void notifyTurn(int status) {
		writePlayer(status, "error", new JSONArray());
		writePlayer(status, "responseCode", 100);
		writePlayer(status, "playerName", "");
		writePlayer(status, "playerToken", "");
		writePlayer(status, "ships", new JSONArray());
		writePlayer(status, "shipActionResults", new JSONArray());
		writePlayer(status, "hitReport", new JSONArray());
		
		send(status);
	}

	// TODO turn interuppts
	// TODO status enum creation
	/**
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @param string
	 *            - key to what we're writing
	 * @param obj
	 *            - object that we're writing
	 */
	private static boolean writePlayer(int status, String string, Object obj) {
		
		try {
			switch (status) {
			case 0: // append to player 1
				playerTurnObj[status].put(string, obj);
				break;
			case 1: // append to player 2
				playerTurnObj[status].put(string, obj);
				break;
			case 2: // append to both
				playerTurnObj[0].put(string, obj);
				playerTurnObj[1].put(string, obj);
				break;
			default:
				return false;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @param resources
	 *            - given Player's remaining resources
	 * @return - true if successful send
	 */
	
	public static boolean send(int status) {
		
		switch (status) {
		// send to player 1
		case 0: 
			Server.sendPlayer(playerTurnObj[status], playerToken[status]);
			playerTurnObj[status] = new JSONObject();
			break;
			
		// send to player 2	
		case 1: 
			Server.sendPlayer(playerTurnObj[status], playerToken[status]);
			playerTurnObj[status] = new JSONObject();
			break;
			
		// append to both
		case 2: 
			Server.sendPlayer(playerTurnObj[0], playerToken[0]);
			playerTurnObj[0] = new JSONObject();
			Server.sendPlayer(playerTurnObj[1], playerToken[1]);
			playerTurnObj[1] = new JSONObject();
			break;
			
		default:
			return false;
			
		}
		
		return true;
	}

	public static boolean hasWon(int PlayerID) {
		if (PlayerID == 0) {
			Server.winCondition(playerToken[0]);
			return true;
		} else if (PlayerID == 1) {
			Server.winCondition(playerToken[1]);
			return true;
		}
		return false;
	}

	public static void printTurnToLog(int playerID) {
		
		Server.printToVisualizerLog(playerTurnObj[playerID].toString());
	}

}
