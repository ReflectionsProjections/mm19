package mm19.server;

import java.util.ArrayList;

import mm19.game.Ability;
import mm19.game.Action;
import mm19.game.Engine;
import mm19.game.HitReport;
import mm19.game.SonarReport;
import mm19.game.ShipActionResult;

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

    private JSONObject player1;
    private JSONObject player2;
    private JSONObject err;
    private int p1ID;
    private int p2ID;
    private String authP1;
    private String authP2;
    private Engine game;
    private int ID = 0;
    private final int MAX_SIZE = 100; // temporary holder variable move to
                                        // constants

    public API() {
        err = new JSONObject();
        p1ID = -1;
        p2ID = -1;
        player1 = new JSONObject();
        player2 = new JSONObject();
        game = new Engine(this);
    }

    public boolean newData(JSONObject obj, String authToken) {
        int temp;
        game = new Engine(this);
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
                        if((temp = game.playerSet(ships, playerName)) == -1) return false;
                        if (p1ID == -1) {
                            p1ID = temp;
                            authP1 = authToken;
                        } else {
                            p2ID = temp;
                            authP2 = authToken;
                        }
                        return true;

                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean decodeTurn(JSONObject obj) {
        // sanity check
        if (p1ID == -1 || p2ID == -1) {
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

    private ShipData initShip(JSONObject obj) {
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
                                health = Ability.MISSILE_DAMAGE * 2;
                            }
                            else if(type.equals("D")) {
                                health = Ability.MISSILE_DAMAGE * 4;
                            }
                            else if(type.equals("M")) {
                                health = Ability.MISSILE_DAMAGE * 6;
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
    private ShipData getShip(JSONObject obj) {
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
    private ArrayList<ShipData> getShipList(JSONArray jsonArray) {

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
    private Action getAction(JSONObject obj) {
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
    private ArrayList<Action> getActionList(JSONArray jsonArray) {
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
    public boolean writePlayerShips(int status, ArrayList<ShipData> ships) {
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
    private JSONObject makeShipJSON(ShipData data) {
        JSONObject tempShip = new JSONObject();

        try {
            tempShip.append("health", data.health);
            tempShip.append("ID", data.ID);
            tempShip.append("type", data.type);
            tempShip.append("xCoord", data.xCoord);
            tempShip.append("yCoord", data.yCoord);
            tempShip.append("orientation", data.orientation);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return tempShip;
    }

    /**
     * @param status
     *            - enum that tells us to write to player1, player2 or both
     * @param hits
     *            - array list of current player hit reports
     * @return - true if sucessful write
     */
    public boolean writePlayerHits(int status, ArrayList<HitReport> hits) {
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
    public boolean writePlayerEnemyHits(int status, ArrayList<HitReport> hits) {
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
    private JSONObject makeHitJSON(HitReport report) {
        JSONObject tempHit = new JSONObject();
        try {
            tempHit.append("xCoord", report.x);
            tempHit.append("yCoord", report.y);
            tempHit.append("hit", report.shotSuccessful);

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
    public boolean writePlayerPings(int status, ArrayList<SonarReport> pings) {
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
    private JSONObject makePingJSON(SonarReport ping) {
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
    public boolean writePlayerResults(int status,
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
    private JSONObject makeResultJSON(ShipActionResult result) {
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

    /**
     * @param errString - string of the error that has happened
     */
    private void addError(String errString)
    {
        try {
            err.append("error", errString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @param status 
     *          - enum that tells us to write to player1, player2 or both
     */
    private void writePlayerErr(int status) {
        try {
                
            switch (status) {
            case 0: // append to player 1  
                if(err.has("error")){
                    player1.put("responsecode", 400);
                    player1.put("error", err.getJSONObject("error"));
                }
                else {
                    player1.put("responsecode", 200);
                    player1.put("error", "");
                }
                break;
            case 1: // append to player 2
                if(err.has("error")){
                    player2.put("responsecode", 400);
                    player2.put("error", err.getJSONObject("error"));
                }
                else {
                    player2.put("responsecode", 200);
                    player2.put("error", "");
                }
                break;
            case 2: // append to both
                if(err.has("error")){
                    player1.put("responsecode", 400);
                    player2.put("responsecode", 400);
                    player1.put("error", err.getJSONObject("error"));
                    player2.put("error", err.getJSONObject("error"));
                }
                else {
                    player1.put("responsecode", 200);
                    player2.put("responsecode", 200);
                    player1.put("error", "");
                    player2.put("error", "");
                }
                break;
            default:
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        err = new JSONObject();
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
    private boolean writePlayer(int status, String string, Object obj) {
        try {
            switch (status) {
            case 0: // append to player 1
                player1.put(string, obj);
                break;
            case 1: // append to player 2
                player2.put(string, obj);
                break;
            case 2: // append to both
                player1.put(string, obj);
                player2.put(string, obj);
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
     * @param PlayerID
     *            - given Player's ID
     * @param PlayerName
     *            - given Player's Name
     * @param resources
     *            - given Player's remaining resources
     * @return - true if successful send
     */
    public boolean send(int status, int PlayerID, String PlayerName,
            int resources) {
        // TODO send to server and clear local Json
        writePlayer(status, "PlayerID", PlayerID);
        writePlayer(status, "PlayerName", PlayerName);
        writePlayer(status, "resources", resources);
        writePlayerErr(status);
        
        switch (status) {
            case 0: // send to player 1
                Server.sendPlayer(player1, authP1);
                player1 = new JSONObject();
                break;
            case 1: // send to player 2
                Server.sendPlayer(player2, authP2);
                player2 = new JSONObject();
                break;
            case 2: // append to both
                Server.sendPlayer(player1, authP1);
                player1 = new JSONObject();
                Server.sendPlayer(player2, authP2);    
                player2 = new JSONObject();
                break;
            default:
                return false;
        }
        return false;
    }

    public boolean hasWon(int PlayerID) {
        if (PlayerID == p1ID) {
            Server.winCondition(authP1);
            return true;
        } else if (PlayerID == p2ID) {
            Server.winCondition(authP2);
            return true;
        }
        return false;
    }
}
