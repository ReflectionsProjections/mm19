package mm19.game;

import java.util.ArrayList;
import java.util.HashMap;

import mm19.exceptions.EngineException;
import mm19.game.board.Position;
import mm19.game.player.Player;
import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;
import mm19.game.ships.Ship;
import mm19.server.API;
import mm19.server.ShipData;


/**
 * @author mm19
 *
 * This will put all the pieces of the game together, and actually make things run.
 */
public class Engine{
	private Player[] players;

	final public static String SHOOT = "F";
	final public static String BURST_SHOT = "BS";
	final public static String SONAR = "S";
	final public static String MOVE_Horizontal = "MH";
	final public static String MOVE_Vertical = "MV";
	
	final public static int MAXSHIPS =5;
	final public static int DEFAULT_RESOURCES=100;
	
	public static final String DESTROYER = "D";
	public static final String MAINSHIP = "M";
	public static final String PILOT = "P";
	
	private static HashMap<String, Integer> tokenMap;
	
	private API api;

	/**
	 * the constructor is called by the server (or API?) to start the game.
	 */
    public Engine(API api){
    	players=new Player[2];
    	players[1]=null;
    	players[2]=null;
    	this.api = api;
    	tokenMap = new HashMap<String, Integer>();
    }
	
    /**
	 * This function sets up the player's pieces on the board as specified
	 * And returns the playerID to the server so that it can refer back to it
	 * returns -1 on bad imput
	 */
	public int playerSet(ArrayList<ShipData> shipDatas, String playerToken){
		
		ArrayList<Ship> ships = new ArrayList<Ship>();
		ArrayList<Position> positions = new ArrayList<Position>();
		
		Ship tempShip;
		Position tempPos;
		String tempType;
		
		for(int i = 0; i < Math.min(shipDatas.size(), MAXSHIPS); i++){
			tempType = shipDatas.get(i).type;
			tempShip = null;
			if(tempType.equals(DESTROYER)){
				tempShip = new DestroyerShip();
			}else if(tempType.equals(MAINSHIP)){
				tempShip = new MainShip();
			}else if(tempType.equals(PILOT)){
				tempShip = new PilotShip();
			}
			if(tempShip != null){
				if(shipDatas.get(i).orientation.equals("H")){
					tempPos = new Position(shipDatas.get(i).xCoord, 
							shipDatas.get(i).yCoord, 
							Position.Orientation.HORIZONTAL);
				}else{
					tempPos = new Position(shipDatas.get(i).xCoord, 
							shipDatas.get(i).yCoord, 
							Position.Orientation.VERTICAL);
				}
				ships.add(tempShip);
				positions.add(tempPos);
			}
		}
		
		Player player=new Player(DEFAULT_RESOURCES);
		
		boolean setupShips = Ability.setupBoard(player, ships, positions);
		
		if (!(setupShips && player.isAlive())) {
			return -1;
			}
		if(players[1] == null) {
			players[1] = player;
		}
		else if (players[2] == null) {
			players[2] = player;
		}
		else throw new RuntimeException("too many players!");
		
		tokenMap.put(playerToken, player.getPlayerID());
		
		return player.getPlayerID();
	}
	
	/**
	 * At the start of their turn, they receive resources
	 * This function attempts all of the player's chosen actions for the turn
	 * Afterwards, it tells the API to send the data back
	 */
	public void playerTurn(String playerToken, ArrayList<Action> actions){
		//Check for valid playerID
		
		int playerID = tokenMap.get(playerToken);
		
		Player p=null;
		Player otherP=null;
		if(players[1].getPlayerID()==playerID){
			p=players[1];
			otherP=players[2];
		}
		else if(players[2].getPlayerID()==playerID){
			p=players[2];
			otherP=players[1];
		}
		if(p==null){
			//TODO: just got an invalid player ID
			return;
		}
		
		Ability.gatherResources(p);
		
		ArrayList<ShipActionResult> results = new ArrayList<ShipActionResult>();
		ArrayList<HitReport> hits = new ArrayList<HitReport>();
		ArrayList<SonarReport> pings = new ArrayList<SonarReport>();
		for(Action a: actions){
			switch(a.actionID){
				case SHOOT:
					try{
						HitReport hitResponse = Ability.shoot(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
						results.add(new ShipActionResult(a.shipID, "S"));
						hits.add(hitResponse);
					} catch(EngineException e){
						results.add(new ShipActionResult(a.shipID, e.getMessage()));
					} 
					break;
				case BURST_SHOT:
					try{
						ArrayList<HitReport> burstResponse = Ability.burstShot(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
						results.add(new ShipActionResult(a.shipID, "S"));
						hits.addAll(burstResponse);
					} catch(EngineException e){
						results.add(new ShipActionResult(a.shipID, e.getMessage()));
					} 
					break;
				case SONAR: 
					//TODO: Need a response for the other player as well?
					try{
						ArrayList<SonarReport> sonarResponse = Ability.sonar(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
						results.add(new ShipActionResult(a.shipID, "S"));
						pings.addAll(sonarResponse);
					} catch(EngineException e){
						results.add(new ShipActionResult(a.shipID, e.getMessage()));
					} 
					break;
				case MOVE_Horizontal:
					try{
						boolean moveResponse = Ability.move(p, a.shipID, new Position(a.actionXVar, a.actionYVar, Position.Orientation.HORIZONTAL));
						results.add(new ShipActionResult(a.shipID, "S"));
					} catch(EngineException e){
						results.add(new ShipActionResult(a.shipID, e.getMessage()));
					} 
					break;
				case MOVE_Vertical:
					try{
						boolean moveResponse2 = Ability.move(p, a.shipID, new Position(a.actionXVar, a.actionYVar, Position.Orientation.VERTICAL));
						results.add(new ShipActionResult(a.shipID, "S"));
					} catch(EngineException e){
						results.add(new ShipActionResult(a.shipID, e.getMessage()));
					} 
					break;
				default:
					break;
			}
		}
		endofTurn(p, results, hits, pings);
	}
	
	/**
	 * This function will check for victory conditions
	 * Then return to the player the results
	 * @param results
	 * @param hits
	 * @param sonar
	 */
	public void endofTurn(Player p, ArrayList<ShipActionResult> results, ArrayList<HitReport> hits, ArrayList<SonarReport> sonar){
		if(!players[1].isAlive() && !players[2].isAlive()){
			//Tie game (Is this even possible?)
			api.hasWon(players[1].getPlayerID());
		} else if(!players[1].isAlive()){
			//Player 2 wins
			api.hasWon(players[1].getPlayerID());
		} else if(!players[2].isAlive()){
			//Player 1 wins
			api.hasWon(players[2].getPlayerID());
		} else{
			//Send data to both players
			int currPlayerID, opponentID;
			Player opponent;
			if(players[1].getPlayerID()==p.getPlayerID()){
				currPlayerID=0;
				opponentID=1;
				opponent=players[2];
			} else{
				currPlayerID=1;
				opponentID=0;
				opponent=players[1];
			}
			ArrayList<ShipData> data=new ArrayList<ShipData>();
			ArrayList<Ship> ships=opponent.getBoard().getShips();
			Ship tempShip;
			Position tempPos;
			String tempType;
			for(int i = 0; i < ships.size(); i++){
				tempShip = ships.get(i);
				tempType = null;
				if(tempShip instanceof DestroyerShip){
					tempType = DESTROYER;
				}else if(tempShip instanceof MainShip){
					tempType = MAINSHIP;
				}else if(tempShip instanceof PilotShip){
					tempType = PILOT;
				}
				String tempOrient="";
				if(tempType != null){
					if(opponent.getBoard().getShipPosition(tempShip.getID()).orientation == Position.Orientation.HORIZONTAL){
						tempOrient="H";
					}else{
						tempOrient="V";
					}
					tempPos=opponent.getBoard().getShipPosition(tempShip.getID());
					data.add(new ShipData(tempShip.getHealth(), tempShip.getID(), tempType, tempPos.x, tempPos.y, tempOrient));
				}
			}
			
			api.writePlayerResults(currPlayerID, results);
			api.writePlayerPings(currPlayerID, sonar);
			api.writePlayerHits(currPlayerID, hits);
			//Send some info to the other player!
			
			api.writePlayerShips(opponentID, data);
			api.writePlayerEnemyHits(opponentID, hits);
			api.send(currPlayerID, opponent.getPlayerID(), opponent.getPlayerName(), opponent.getResources());
		}
	}

	public int getP1ID() {
		if(players[1] != null) return players[1].getPlayerID();
		return -1;
	}

	public int getP2ID() {
		if(players[2] != null) return players[2].getPlayerID();
		return -1;
	}



}
