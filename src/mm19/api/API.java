package mm19.api;

import java.util.ArrayList;

import mm19.exceptions.APIException;
import mm19.exceptions.ActionException;
import mm19.exceptions.EngineException;
import mm19.exceptions.ShipDataException;
import mm19.game.Constants;
import mm19.game.Engine;
import mm19.game.player.Player;
import mm19.game.ships.MainShip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author mm19
 * 
 *         This will tie together the server and game logic. Functions defined
 *         here are called by the server to interface properly with the game.
 * 
 */
public class API {
	private PlayerTurn[] playerTurns;
	private Engine game;
	private static boolean initialized = false;
	private static API m_API = null;

	/**
	 * Constructor
	 */
	private API() {
		playerTurns = new PlayerTurn[Constants.PLAYER_COUNT];
		game = new Engine();
	}

	/**
	 * Retrieve the singleton API
	 * 
	 * @return the API
	 */
	public static API getAPI() {
		if (m_API == null) {
			if(!initialized) {
				initialized = true;
				m_API = new API();
			}
			
		}

		return m_API;
	}

	/**
	 * Adds player to the game
	 * 
	 * @param s
	 *            The String (hopefully valid JSON) representing the player to
	 *            be initialized
	 * @param playerToken
	 *            The playerToken for identifying the player in future turns
	 */
	public void addPlayer(String s, String playerToken) throws APIException {
		try {
			JSONObject json = new JSONObject(s);
			ShipData mainShip = null;
			// Get the main ship from JSON
			JSONObject mainShipJSON = json.getJSONObject("mainShip");
			mainShipJSON.put("type", MainShip.IDENTIFIER);
			try {
				mainShip = new ShipData(mainShipJSON);
			} catch (ShipDataException e) {
				throw new APIException("Error initializing main ship: "
						+ e.getMessage());
			}

			// Get the array of other ships from JSON and verify it's the right
			// length.
			JSONArray shipsJSONArray = json.getJSONArray("ships");
			if (shipsJSONArray.length() != Constants.MAX_SHIPS - 1) {
				throw new APIException("You are trying to place "
						+ (shipsJSONArray.length() + 1)
						+ " ships but the game requires " + Constants.MAX_SHIPS);
			}

			// Get the array of other ships, we can safely assume the main ship
			// will not be null because an exception will be thrown if there's
			// an error initializing it
			ArrayList<ShipData> shipDatas = new ArrayList<ShipData>();
			shipDatas.add(mainShip);
			for (int i = 0; i < shipsJSONArray.length(); i++) {
				try {
					ShipData tempShipData = new ShipData(
							shipsJSONArray.getJSONObject(i));
					shipDatas.add(tempShipData);
				} catch (ShipDataException e) {
					throw new APIException("Error initializing ship " + i
							+ ": " + e.getMessage());
				}
			}

			// Add the player to the engine
			Player player;
			try {
				player = game.setPlayer(shipDatas, playerToken);
				player.setName(json.getString("playerName"));
			} catch (EngineException e) {
				throw new APIException(e.getMessage());
			}

			// We can assume the player isn't null, there would've been an
			// exception thrown if there was an issue initializing
			playerTurns[player.getPlayerID()] = new PlayerTurn(player,
					playerToken);
		} catch (JSONException e) {
			throw new APIException(e.getMessage());
		}
	}

	/**
	 * Takes a JSON Object from one of the clients, parses the actions, and
	 * gives it to the Engine to process
	 * 
	 * @param obj
	 *            The JSON object to parse
	 */
	public void processTurn(JSONObject obj, int playerID) throws APIException {
		if (!game.getStarted()) {
			throw new APIException(
					"The game has not started, so we can not decode a turn");
		}

		try {
			JSONArray actionListObj = obj.getJSONArray("shipActions");

			ArrayList<Action> actions = new ArrayList<Action>();
			for (int i = 0; i < actionListObj.length(); i++) {
				try {
					Action tempAction = new Action(
							actionListObj.getJSONObject(i));
					actions.add(tempAction);
				} catch (ActionException e) {
					playerTurns[playerID]
							.addError("Error initializing action " + i + ": "
									+ e.getMessage());
				}
			}

			boolean success = game.playerTurn(playerID, actions);
			
			if(!success) {
				throw new APIException("It is not your turn!");
			}

		} catch (JSONException e) {
			if (playerID != -1) {
				playerTurns[playerID].setError();
			}
			throw new APIException("JSONException while decoding turn: "
					+ e.getMessage());
		}
	}

	/**
	 * Add an error to a player's turn
	 * 
	 * @param playerID
	 *            The playerID to add to
	 * @param message
	 *            The error message
	 */
	public void addPlayerError(int playerID, String message) {
		playerTurns[playerID].addError(message);
	}

	/**
	 * Add a hit report to a player's turn
	 * 
	 * @param playerID
	 *            The playerID to add to
	 * @param hr
	 *            The hit report
	 */
	public void addHitReport(int playerID, HitReport hr) {
		playerTurns[playerID].addHitReport(hr);
	}

	/**
	 * Add a sonar report to a player's turn
	 * 
	 * @param playerID
	 *            The playerID to add to
	 * @param sr
	 *            The sonar report
	 */
	public void addSonarReport(int playerID, SonarReport sr) {
		playerTurns[playerID].addSonarReport(sr);
	}

	/**
	 * Add a ship's action result to a player's turn
	 * 
	 * @param playerID
	 *            The playerID to add to
	 * @param sar
	 *            The ship's action result
	 */
	public void addShipActionResult(int playerID, ShipActionResult sar) {
		playerTurns[playerID].addShipActionResult(sar);
	}

	/**
	 * Gets the player's turn object by the playerID
	 * 
	 * @param playerID
	 *            The playerID
	 * @return The player's turn
	 */
	public PlayerTurn getPlayerTurn(int playerID) {
		return playerTurns[playerID];
	}
	
	/**
	 * Gets the ID of the player of the current turn
	 * @return
	 */
	public int getCurrPlayerID() {
		return game.getCurrPlayerID();
	}
	
	/**
	 * Gets the ID of the opponent of the current turn
	 * @return
	 */
	public int getCurrOpponentID() {
		return game.getCurrOpponentID();
	}
	
	/**
	 * Notifies the API (and engine) that a player was interrupted
	 */
	public void notifyInterrupt() {
		game.notifyInterrupt();
	}
	
	/**
	 * Gets whether the game is ready to be started
	 */
	public boolean getStarted() {
		return game.getStarted();
	}
	
	/**
	 * Gets whether the game has a winner
	 */
	public boolean getWinner() {
		return game.getWinner();
	}
}
