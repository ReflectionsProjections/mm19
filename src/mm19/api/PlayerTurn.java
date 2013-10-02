package mm19.api;

import java.util.ArrayList;

import mm19.exceptions.ShipDataException;
import mm19.game.board.Board;
import mm19.game.board.Position;
import mm19.game.player.Player;
import mm19.game.ships.Ship;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author mm19 
 * 
 * PlayerTurn is a java representation of the JSON the server will send upon a
 * successful, or partially successful submission. It provides an interface to
 * add other API objects and easily form all types of server responses.
 */
public class PlayerTurn {

	private final int NOTIFY_RESPONSE_CODE = 100;
	private final int SUCCESS_RESPONSE_CODE = 200;
	private final int WARNING_RESPONSE_CODE = 201;
	private final int ERROR_RESPONSE_CODE = 400;
	private final int INTERRUPT_RESPONSE_CODE = 418;
	private final int WIN_RESPONSE_CODE = 9001;
	private final int LOSS_RESPONSE_CODE = -1;

	private boolean initialized;
	private boolean interrupt;
	private boolean won;
	private boolean lost;
	private boolean notify;
	private boolean error;

	private Player player;
	private String playerToken;

	private ArrayList<String> errors;
	private ArrayList<ShipActionResult> shipActionResults;
	private ArrayList<Action> sucessfulActions;
	private ArrayList<HitReport> hitReport;
	private ArrayList<SonarReport> sonarReport;

	/**
	 * Constructor
	 * 
	 * Creates an uninitialized PlayerTurn object, see setPlayer on how to
	 * initialize PlayerTurn
	 */
	public PlayerTurn() {
		player = null;
		playerToken = null;
		initialized = false;
		won = false;
		lost = false;
		resetTurn();
	}

	/**
	 * Constructor
	 * 
	 * Creates an initialized PlayerTurn object
	 * 
	 * @param p
	 *            The player that this PlayerTurn object will represent
	 * @param token
	 *            The player's authentication token
	 */
	public PlayerTurn(Player p, String token) {
		setPlayer(p, token);
	}

	/**
	 * Initializes the PlayerTurn object, initializing allows the user to send
	 * all types of server responses (interrupts, notifications, end of turn
	 * responses, etc.) This function will assume you're passing in non-null
	 * parameters, and can potentially crash the server if you pass in null for
	 * either and try to call the toJSON method.
	 * 
	 * @param p
	 * @param token
	 */
	public void setPlayer(Player p, String token) {
		player = p;
		playerToken = token;
		initialized = true;
		resetTurn();
	}

	/**
	 * Resets all objects within the PlayerTurn, should be called after sending
	 * responses.
	 */
	public void resetTurn() {
		errors = new ArrayList<String>();
		shipActionResults = new ArrayList<ShipActionResult>();
		sucessfulActions = new ArrayList<Action>();
		hitReport = new ArrayList<HitReport>();
		sonarReport = new ArrayList<SonarReport>();

		interrupt = false;
		notify = false;
		won = false;
		lost = false;
		error = false;
	}

	/**
	 * Add error to PlayerTurn. If sending normal end of turn responses, this
	 * will automatically change the response code from 200 to 201 (warning)
	 * 
	 * @param e
	 *            The error to add
	 */
	public void addError(String e) {
		errors.add(e);
	}

	/**
	 * Add a hit report to PlayerTurn
	 * 
	 * @param hr
	 *            The hit report to add
	 */
	public void addHitReport(HitReport hr) {
		hitReport.add(hr);
	}

	/**
	 * Add a ship action result to PlayerTurn
	 * 
	 * @param sar
	 *            The ship action result to add
	 */
	public void addShipActionResult(ShipActionResult sar) {
		shipActionResults.add(sar);
	}

	/**
	 * Add a sonar report to PlayerTurn
	 * 
	 * @param sr
	 *            The sonar report to add
	 */
	public void addSonarReport(SonarReport sr) {
		sonarReport.add(sr);
	}
	/**
     * Add action to PlayerTurn
     * 
     * @param a
     *            The action to add
     */
    public void addAction(Action a) {
        sucessfulActions.add(a);
    }
	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * @return the player token
	 */
	public String getPlayerToken() {
		return playerToken;
	}
	
	/**
	 * Tells the PlayterTurn object to send an error response next time toJSON is called.
	 */
	public void setError() {
		error = true;
	}
	
	/**
	 * Tells the PlayerTurn object to form an interrupt response next time toJSON is called.
	 */
	public void setInterrupt() {
		interrupt = true;
	}
	
	/**
	 * Tells the PlayerTurn object to form a notification response next time toJSON is called.
	 */
	public void setNotify() {
		notify = true;
	}
	
	/**
	 * Tells the PlayerTurn object to notify the player that he has won next turn
	 */
	public void setWon() {
		won = true;
	}
	
	/**
	 * Tells the PlayerTurn object to notify the player that he has lost next turn
	 */
	public void setLost() {
		lost = true;
	}
	
	/**
     * checks whether the given player has won: 
     *  if it returns false that does not mean the player has lost, it just means they have not lost yet
     */
	public boolean hasWon(){
	    return won;
	}
	
	/**
     * checks whether the given player has lost: 
     *  if it returns false that does not mean the player has won, it just means they have not won yet
     */
	public boolean hasLost(){
	    return lost;
	}
	
	/**
	 * Returns a Json containing "winner", playerName
	 * @throws JSONException 
	 */
	public JSONObject winnerJSON(){
        try {
            JSONObject json = new JSONObject();
            json.put("winner", player.getName());
            return json;
        } catch (JSONException e) {
            return null;
        }
	}

	/**
	 * Get the PlayerTurn in a format that the visualizer can read it
	 * 
	 * @return The JSON properly formatted for logging
	 */
	public JSONObject toLoggingJSON() {
		try {
			return getLoggingJSON();//writeJSONBody(new JSONObject());
		} catch (JSONException e) {
			return null;
		}
	}

	/**
	 * This method will properly format a JSON object to send back to the
	 * player. Depending on the flags set, this function can generate an
	 * interrupt, notification, end of turn response, or invalid initial data
	 * @return
	 */
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			// Determine the right JSON to respond with
			if (!initialized) {
				json = getUninitializedJSON();
			} else if (won) {
				json = getWinJSON();
			} else if (lost) {
				json = getLossJSON();
			} else if (interrupt) {
				json = getInterruptJSON();
			} else if (notify) {
				json = getNotifyJSON();
			} else {
				json = getResponseJSON();
			}
		}

		// This should never happen, and if it does will probably result in
		// crashing the server with a NullPointerException.
		catch (JSONException e) {
			e.printStackTrace();
			json = null;
		}
		return json;
	}
	
	
	/**
	 * Generates an invalid initial data JSON object. This only gets called if
	 * the user calls the default constructor and doesn't call setPlayer before
	 * toJSON
	 * 
	 * @return The JSON object
	 * @throws JSONException
	 */
	private JSONObject getUninitializedJSON() throws JSONException {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		json.put("responseCode", ERROR_RESPONSE_CODE);

		for (int i = 0; i < errors.size(); i++) {
			array.put(errors.get(i));
		}
		json.put("error", array);
		return json;
	}

	/**
	 * Generates an interrupt response.
	 * @return The JSON object
	 * @throws JSONException
	 */
	private JSONObject getInterruptJSON() throws JSONException {
		errors.add("You are taking too long and have lost your turn.");
		JSONObject json = new JSONObject();
		json.put("responseCode", INTERRUPT_RESPONSE_CODE);

		return writeJSONBody(json);
	}

	/**
	 * Generates a notification that it's the player's turn
	 * @return The JSON object
	 * @throws JSONException
	 */
	private JSONObject getNotifyJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("responseCode", NOTIFY_RESPONSE_CODE);
		return writeJSONBody(json);
	}

	/**
	 * Generates an end of turn response
	 * @return The JSON object
	 * @throws JSONException
	 */
	private JSONObject getResponseJSON() throws JSONException {
		JSONObject json = new JSONObject();
		
		if(error) {
			json.put("responseCode", ERROR_RESPONSE_CODE);
		} else if (errors.size() == 0) {
			json.put("responseCode", SUCCESS_RESPONSE_CODE);
		} else {
			json.put("responseCode", WARNING_RESPONSE_CODE);
		}
		return writeJSONBody(json);
	}
	
	/**
	 * Generates a win response
	 * @return The JSON object
	 * @throws JSONException
	 */
	private JSONObject getWinJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("responseCode", WIN_RESPONSE_CODE);
		return writeJSONBody(json);
	}
	
	/** 
	 * Generates a loss response
	 * @return The JSON object
	 * @throws JSONException
	 */
	private JSONObject getLossJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("responseCode", LOSS_RESPONSE_CODE);
		return writeJSONBody(json);
	}

	
	
	/**
	 * Writes the generic JSON body for most server responses
	 * @param json
	 * 			The JSON to build upon
	 * @return The JSON that was built upon
	 * @throws JSONException
	 */
	private JSONObject writeJSONBody(JSONObject json) throws JSONException {

		ArrayList<Ship> ships = player.getBoard().getShips();
		Board board = player.getBoard();

		JSONArray errorArray = new JSONArray();
		JSONArray shipDataArray = new JSONArray();
		JSONArray shipActionArray = new JSONArray();
		JSONArray sonarReportArray = new JSONArray();
		JSONArray hitReportArray = new JSONArray();

		// Form the ship data array
		for (Ship ship : ships) {
			Position pos = board.getShipPosition(ship.getID());
			ShipData tempShipData = new ShipData(ship.getHealth(),
					ship.getID(), ship.getIdentifier(), pos.x, pos.y,
					pos.orientation);

			try {
				shipDataArray.put(tempShipData.toJSON());
			} catch (ShipDataException e) {
				throw new JSONException(e.getMessage());
			}
		}

		// Form the errors array
		for (String e : errors) {
			errorArray.put(e);
		}

		// Form the ship action results array
		for (ShipActionResult result : shipActionResults) {
			shipActionArray.put(result.toJSON());
		}

		// Form the sonar report array
		for (SonarReport report : sonarReport) {
			sonarReportArray.put(report.toJSON());
		}

		// Form the hit report array
		for (HitReport report : hitReport) {
			hitReportArray.put(report.toJSON());
		}

		json.put("error", errorArray);
		json.put("playerName", player.getName());
		json.put("playerToken", playerToken);
		json.put("resources", player.getResources());
		json.put("ships", shipDataArray);
		json.put("shipActionResults", shipActionArray);
		json.put("hitReport", hitReportArray);
		json.put("pingReport", sonarReportArray);

		return json;
	}
	
	public JSONObject getLoggingJSON() throws JSONException{
        ArrayList<Ship> ships = player.getBoard().getShips();
        Board board = player.getBoard();
        JSONObject json = new JSONObject();
        JSONArray shipDataArray = new JSONArray();
        JSONArray actionArray = new JSONArray();
        
        if (won) {
            json.put("responseCode", WIN_RESPONSE_CODE);
        } else if (lost) {
            json.put("responseCode", LOSS_RESPONSE_CODE);
        } else json.put("responseCode", SUCCESS_RESPONSE_CODE);

        // Form the ship data array
        for (Ship ship : ships) {
            Position pos = board.getShipPosition(ship.getID());
            ShipData tempShipData = new ShipData(ship.getHealth(),
                    ship.getID(), ship.getIdentifier(), pos.x, pos.y,
                    pos.orientation);

            try {
                shipDataArray.put(tempShipData.toJSON());
            } catch (ShipDataException e) {
                throw new JSONException(e.getMessage());
            }
        }
        
        for (Action action : sucessfulActions) {
            actionArray.put(action.toJSON());
        }
        json.put("playerName", player.getName());
        json.put("playerToken", playerToken);
        json.put("resources", player.getResources());
        json.put("ships", shipDataArray);
        json.put("actions", actionArray);
        
        return json;
    }
}