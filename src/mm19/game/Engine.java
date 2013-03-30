package mm19.game;

import java.util.ArrayList;

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
	private Player p1;
	private Player p2;

	final public static String SHOOT = "F";
	final public static String BURST_SHOT = "BS";
	final public static String SONAR = "S";
	final public static String MOVE_Horizontal = "MH";
	final public static String MOVE_Vertical = "MV";
	
	final public static int DEFAULT_RESOURCES=100;
	private static final String DESTROYER = "D";
	private static final String MAINSHIP = "M";
	private static final String PILOT = "P";
	
	private API api;

	/**
	 * the constructor is called by the server (or API?) to start the game.
	 */
    public Engine(API api){
    	p1 = null;
    	p2 = null;
    	this.api = api;
    	
    }
	
    /**
	 * This function sets up the player's pieces on the board as specified
	 * And returns the playerID to the server so that it can refer back to it
	 */
	public int playerSet(ArrayList<ShipData> shipDatas, String playerName){//ArrayList<Ship> ships, ArrayList<Position> positions){
		
		ArrayList<Ship> ships = new ArrayList<Ship>();
		ArrayList<Position> positions = new ArrayList<Position>();
		Ship tempShip;
		Position tempPos;
		String tempType;
		for(int i = 0; i < shipDatas.size(); i++){
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
		Ability.setupBoard(player, ships, positions); //TODO: could fail to setup board
		if(p1 == null) p1 = player;
		else p2 = player;
		return player.getPlayerID();
	}
	
	/**
	 * At the start of their turn, they receive resources
	 * This function attempts all of the player's chosen actions for the turn
	 * Afterwards, it tells the API to send the data back
	 */
	public void playerTurn(int playerID, ArrayList<Action> actions){
		//Check for valid playerID
		Player p=null;
		Player otherP=null;
		if(p1.getPlayerID()==playerID){
			p=p1;
			otherP=p2;
		}
		else if(p2.getPlayerID()==playerID){
			p=p2;
			otherP=p1;
		}
		if(p==null){
			//TODO: just got an invalid player ID
		}
		
		Ability.gatherResources(p);
		
		ArrayList<ShipActionResult> results = new ArrayList<ShipActionResult>();
		ArrayList<HitReport> hits = new ArrayList<HitReport>();
		ArrayList<SonarReport> pings = new ArrayList<SonarReport>();
		for(Action a: actions){
			switch(a.actionID){
				case SHOOT:
					HitReport hitResponse = Ability.shoot(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
					if(hitResponse == null){ //TODO: should have some way of knowing what error ocurred
						results.add(new ShipActionResult(a.shipID, "R"));
					} else{
						results.add(new ShipActionResult(a.shipID, "S"));
						hits.add(hitResponse);
					}
					break;
				case BURST_SHOT:
					ArrayList<HitReport> burstResponse = Ability.burstShot(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
					if(burstResponse == null){
						results.add(new ShipActionResult(a.shipID, "R"));
					} else{
						results.add(new ShipActionResult(a.shipID, "S"));
						hits.addAll(burstResponse);
					}
					break;
				case SONAR: //TODO: Need a response for the other player as well?
					ArrayList<SonarReport> sonarResponse = Ability.sonar(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
					if(sonarResponse == null){
						results.add(new ShipActionResult(a.shipID, "R"));
					} else{
						results.add(new ShipActionResult(a.shipID, "S"));
						pings.addAll(sonarResponse);
					}
					break;
				case MOVE_Horizontal:
					boolean moveResponse = Ability.move(p, a.shipID, new Position(a.actionXVar, a.actionYVar, Position.Orientation.HORIZONTAL));
					if(moveResponse){
						results.add(new ShipActionResult(a.shipID, "S"));
					} else{
						results.add(new ShipActionResult(a.shipID, "R"));
						
					}
					break;
				case MOVE_Vertical:
					boolean moveResponse2 = Ability.move(p, a.shipID, new Position(a.actionXVar, a.actionYVar, Position.Orientation.VERTICAL));
					if(moveResponse2){
						results.add(new ShipActionResult(a.shipID, "S"));
					} else{
						results.add(new ShipActionResult(a.shipID, "R"));
						
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
		if(!p1.isAlive() && !p2.isAlive()){
			//Tie game (Is this even possible?)
			//TODO: send win message
		} else if(!p1.isAlive()){
			//Player 2 wins
			//TODO: send win message
			api.send(1, p1.getPlayerID(), p1.getPlayerName(), p1.getResources());
		} else if(!p2.isAlive()){
			//Player 1 wins
			//TODO: send win message
			api.send(1, p2.getPlayerID(), p2.getPlayerName(), p2.getResources());
		} else{
			//Send data to both players
			int player1, player2;
			if(p1.getPlayerID()==p.getPlayerID()){
				player1=0;
				player2=1;
			} else{
				player1=1;
				player2=0;
			}
			ArrayList<ShipData> data=new ArrayList<ShipData>();
			ArrayList<Ship> ships=p.getBoard().getShips();
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
				String temporient="";
				if(tempType != null){
					if(p.getBoard().getShipPosition(tempShip.getID()).orientation == Position.Orientation.HORIZONTAL){
						temporient="H";
					}else{
						temporient="V";
					}
					tempPos=p.getBoard().getShipPosition(tempShip.getID());
					data.add(new ShipData(tempShip.getHealth(), tempShip.getID(), tempType, tempPos.x, tempPos.y, temporient));
				}
			}
			
			api.writePlayerShips(player1, data);
			api.writePlayerResults(player1, results);
			api.writePlayerPings(player1, sonar);
			api.writePlayerHits(player1, hits);
			api.send(player1, p.getPlayerID(), p.getPlayerName(), p.getResources());
			//TODO: Should send some info to other player as well
			
			
		}
	}

	public int getP1ID() {
		if(p1 != null) return p1.getPlayerID();
		return -1;
	}

	public int getP2ID() {
		if(p2 != null) return p2.getPlayerID();
		return -1;
	}



}
