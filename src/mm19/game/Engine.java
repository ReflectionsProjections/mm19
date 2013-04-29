package mm19.game;

import java.util.ArrayList;
import java.util.Timer;

import mm19.exceptions.InputException;
import mm19.exceptions.ResourceException;
import mm19.game.board.Position;
import mm19.game.player.Player;
import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;
import mm19.game.ships.Ship;
import mm19.server.API;
import mm19.server.ServerTimerTask;
import mm19.server.ShipData;


/**
 * @author mm19
 *
 * This will put all the pieces of the game together, and actually make things run.
 */
public class Engine{
	private Player[] players;
	private String[] playerTokens;
	private int turn = 0;
	private Timer time;
	public static final String SHOOT = "F";
	public static final String BURST_SHOT = "BS";
	public static final String SONAR = "S";
	public static final String MOVE_Horizontal = "MH";
	public static final String MOVE_Vertical = "MV";
	
	public static final int MAXSHIPS =5;
	public static final int DEFAULT_RESOURCES=100;
	
	public static final String DESTROYER = "D";
	public static final String MAINSHIP = "M";
	public static final String PILOT = "P";
	
	public static final int TURNLIMIT = 10000;
	public static final int TIMELIMIT = 10;
	
	/**
	 * the constructor is called by the API to start the game.
	 */
    public Engine(){
    	players = new Player[2];
    	players[0] = null;
    	players[1] = null;

    	playerTokens = new String[2];
    	playerTokens[0] = "";
    	playerTokens[1] = "";
    	turn = 0;
    }
	
    /**
	 * This function sets up the player's pieces on the board as specified
	 * And returns the playerID to the server so that it can refer back to it
	 * returns -1 on bad input
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
			} else{
				API.writePlayerError(turn%2, "Unable to initialize ship "+i+" to type "+shipDatas.get(i).type);
			}
		}
		if(ships.size() < shipDatas.size()) {
			API.writePlayerResponseCode(turn%2);
			return -1;
		}
		
		Player player=new Player(DEFAULT_RESOURCES);
		
		boolean setupShips = Ability.setupBoard(player, ships, positions);
		
		if (!(setupShips && player.isAlive())) {
			API.writePlayerError(turn%2, "Unable to setup ships due to bad positions");
			API.writePlayerResponseCode(turn%2);
			return -1;
			}
		if(players[0] == null) {
			players[0] = player;
		}
		else if (players[1] == null) {
			players[1] = player;
		}
		else throw new RuntimeException("too many players!");
		
		playerTokens[player.getPlayerID()] = playerToken;
		
		ArrayList<ShipData> data = getShipData(player);
		
		
		API.writePlayerShips(player.getPlayerID(), data);
		API.writePlayerResources(player.getPlayerID(), player.getResources());
		API.writePlayerResponseCode(player.getPlayerID());
		
		turn++;
		if(turn > 1){
			time = new Timer();
			time.schedule(new Timeout(this), TIMELIMIT*1000);
		}
		return player.getPlayerID();
	}
	
	/**
	 * At the start of their turn, they receive resources
	 * This function attempts all of the player's chosen actions for the turn
	 * Afterwards, it tells the API to send the data back
	 */
	public boolean playerTurn(String playerToken, ArrayList<Action> actions){
		//Cancel the timeout
		time.cancel();
		time.purge();
		
		//Check for valid playerID
		int playerID;
		if(playerTokens[0].equals(playerToken)) {
			playerID = 0;
		} else {
			playerID = 1;
		}
		if(playerID != turn %2 ){
			API.writePlayerError(playerID, "It is not your turn!");
			API.writePlayerResponseCode(playerID);
			return false;
		}
		Player p = null;
		Player otherP = null;
		if(players[0].getPlayerID() == playerID){
			p = players[0];
			otherP = players[1];
		}
		else if(players[1].getPlayerID() == playerID){
			p = players[1];
			otherP = players[0];
		}
//		if(p==null){
//			//just got an invalid player ID
//			return;
//		}
		
		Ability.gatherResources(p);
		
		ArrayList<ShipActionResult> results = new ArrayList<ShipActionResult>();
		ArrayList<HitReport> hits = new ArrayList<HitReport>();
		ArrayList<HitReport> opponentHits= new ArrayList<HitReport>();
		ArrayList<SonarReport> pings = new ArrayList<SonarReport>();
		
		for(Action a: actions){
			if(a.actionID.equals(SHOOT)) {
				try{
					HitReport hitResponse = Ability.shoot(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
					results.add(new ShipActionResult(a.shipID, "S"));
					hits.add(hitResponse);
					opponentHits.add(hitResponse);
				} catch(InputException e){
					results.add(new ShipActionResult(a.shipID, "I"));
					API.writePlayerError(playerID, e.getMessage());
				} catch(ResourceException e){
					results.add(new ShipActionResult(a.shipID, "R"));
					API.writePlayerError(playerID, e.getMessage());
				}
			} else if (a.actionID.equals(BURST_SHOT)) { 
				try{
					ArrayList<HitReport> burstResponse = 
			        Ability.burstShot(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
					results.add(new ShipActionResult(a.shipID, "S"));
					for(HitReport h : burstResponse){
						if(h.shotSuccessful){
							opponentHits.add(h);
						}
					}
					//hits.addAll(burstResponse);
				} catch(InputException e){
					results.add(new ShipActionResult(a.shipID, "I"));
					API.writePlayerError(playerID, e.getMessage());
				} catch(ResourceException e){
					results.add(new ShipActionResult(a.shipID, "R"));
					API.writePlayerError(playerID, e.getMessage());
				}
			} else if (a.actionID.equals(SONAR)) {
				try{
					ArrayList<SonarReport> sonarResponse = Ability.sonar(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
					results.add(new ShipActionResult(a.shipID, "S"));
					pings.addAll(sonarResponse);
				} catch(InputException e){
					results.add(new ShipActionResult(a.shipID, "I"));
					API.writePlayerError(playerID, e.getMessage());
				} catch(ResourceException e){
					results.add(new ShipActionResult(a.shipID, "R"));
					API.writePlayerError(playerID, e.getMessage());
				}
			} else if (a.actionID.equals(MOVE_Horizontal)) {
				try{
					boolean moveResponse = Ability.move(p, a.shipID, new Position(a.actionXVar, a.actionYVar, Position.Orientation.HORIZONTAL));
					results.add(new ShipActionResult(a.shipID, "S"));
				} catch(InputException e){
					results.add(new ShipActionResult(a.shipID, "I"));
					API.writePlayerError(playerID, e.getMessage());
				} catch(ResourceException e){
					results.add(new ShipActionResult(a.shipID, "R"));
					API.writePlayerError(playerID, e.getMessage());
				}
			} else if (a.actionID.equals(MOVE_Vertical)) {
				try{
					boolean moveResponse2 = Ability.move(p, a.shipID, new Position(a.actionXVar, a.actionYVar, Position.Orientation.VERTICAL));
					results.add(new ShipActionResult(a.shipID, "S"));
				} catch(InputException e){
					results.add(new ShipActionResult(a.shipID, "I"));
					API.writePlayerError(playerID, e.getMessage());
				} catch(ResourceException e){
					results.add(new ShipActionResult(a.shipID, "R"));
					API.writePlayerError(playerID, e.getMessage());
				}
			}
		}
		endofTurn(p, results, hits, opponentHits, pings);
		return true;
	}
	
	/**
	 * This function is called when the player fails to send their turn in a reasonable amount of time.
	 * It calls endofTurn as if a turn was taken, but without doing anything else
	 */
	public void timeout(){
		System.out.println("timeout!");
		int currPlayerID = turn%2;
		int opponentID = (turn+1)%2;
		Player p = players[turn % 2];
		endofTurn(p, new ArrayList<ShipActionResult>(), new ArrayList<HitReport>(), new ArrayList<HitReport>(), new ArrayList<SonarReport>());
		
		API.sendTurn(currPlayerID);
	}
	
	/**
	 * This function will check for victory conditions
	 * Then return to the player the results
	 * TODO This function seems very broken, and needs to be gone over again very carefully.
	 * @param results
	 * @param hits
	 * @param sonar
	 */
	public void endofTurn(Player p, ArrayList<ShipActionResult> results, ArrayList<HitReport> hits, ArrayList<HitReport> opponentHits, ArrayList<SonarReport> sonar){
		if(!players[0].isAlive() && !players[1].isAlive()){
			//Tie game (Is this even possible?)

			API.hasWon(Ability.tieBreaker(players[0], players[1]).getPlayerID());
		} else if(!players[0].isAlive()){
			//Player 2 wins
			System.out.println("P2 wins!");
			API.hasWon(players[0].getPlayerID());
		} else if(!players[1].isAlive()){
			//Player 1 wins
			System.out.println("P1 wins!");
			API.hasWon(players[1].getPlayerID());
		} else if(turn > TURNLIMIT){
			//Tie game, break the tie
			System.out.println("Tie!");
			API.hasWon(Ability.tieBreaker(players[0], players[1]).getPlayerID());
			
		} else{
			//Send data to both players
			int currPlayerID, opponentID;
			Player opponent;
			if(players[0].getPlayerID() == p.getPlayerID()){
				currPlayerID = 0;
				opponentID = 1;
				opponent = players[1];
			} else{
				currPlayerID = 1;
				opponentID = 0;
				opponent = players[0];
			}
			//reset player special
			Ability.resetAbilityStates(players[currPlayerID]);
			ArrayList<ShipData> data = new ArrayList<ShipData>();
			
            Position tempPos;
            String tempType;
            ArrayList<Ship> ships = opponent.getBoard().getShips();
			for(Ship ship : ships){
				tempType = null;
				if(ship instanceof DestroyerShip){
					tempType = DESTROYER;
				}else if(ship instanceof MainShip){
					tempType = MAINSHIP;
				}else if(ship instanceof PilotShip){
					tempType = PILOT;
				}
				String tempOrient = "";
				if(tempType != null){
					if(opponent.getBoard().getShipPosition(ship.getID()).orientation == Position.Orientation.HORIZONTAL){
						tempOrient = "H";
					}else{
						tempOrient = "V";
					}
					tempPos = opponent.getBoard().getShipPosition(ship.getID());
					data.add(new ShipData(ship.getHealth(), ship.getID(), tempType, tempPos.x, tempPos.y, tempOrient));
				}
			}
			
			ArrayList<SonarReport> opponentSonar = new ArrayList<SonarReport>();
			for(SonarReport sr : sonar) {
				opponentSonar.add(new SonarReport(-1, sr.ship));
			}
			
			// Formulate the server response for the current player's turn.
			API.writePlayerResponseCode(currPlayerID);
			API.writePlayerResources(currPlayerID, players[currPlayerID].getResources());
			API.writePlayerShips(currPlayerID, getShipData(players[currPlayerID]));
			API.writePlayerResults(currPlayerID, results);
			API.writePlayerPings(currPlayerID, sonar);
			API.writePlayerHits(currPlayerID, hits);
			
			// Send some info to the other player!
			API.writePlayerShips(opponentID, getShipData(players[opponentID]));
			API.writePlayerHits(opponentID, opponentHits);
			API.writePlayerPings(opponentID, opponentSonar);
			
			turn++;
			
			//Start the timer for the next turn
			time = new Timer();
			time.schedule(new Timeout(this), TIMELIMIT*1000);
		}
	}
	
	private ArrayList<ShipData> getShipData(Player p) {
		
		ArrayList<ShipData> data = new ArrayList<ShipData>();
		ArrayList<Ship> ships = p.getBoard().getShips();
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
			
			String temporient = "";
			
			if(tempType != null){
				
				if(p.getBoard().getShipPosition(tempShip.getID()).orientation == Position.Orientation.HORIZONTAL){
					temporient = "H";
				} else{
					temporient = "V";
				}
				tempPos=p.getBoard().getShipPosition(tempShip.getID());
				data.add(new ShipData(tempShip.getHealth(), tempShip.getID(), tempType, tempPos.x, tempPos.y, temporient));
			}
		}
		
		return data;
	}

	public int getP1ID() {
		if(players[0] != null) return players[0].getPlayerID();
		return -1;
	}

	public int getP2ID() {
		if(players[1] != null) return players[1].getPlayerID();
		return -1;
	}



}
