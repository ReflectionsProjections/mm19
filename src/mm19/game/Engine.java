package mm19.game;

import java.util.ArrayList;

import mm19.game.board.Board;
import mm19.game.board.Position;
import mm19.game.player.Player;
import mm19.game.ships.Ship;

/**
 * @author mm19
 *
 * This will put all the pieces of the game together, and actually make things run.
 */
public class Engine{
	private Player p1;
	private Player p2;
	final public static int DEFAULT_RESOURCES=100;

	/**
	 * the constructor is called by the server (or API?) to start the game.
	 */
    public Engine(){
    }
	
	/**
	 * This function sets up the player's pieces on the board as specified
	 * And returns the playerID to the server so that it can refer back to it
	 */
	public int playerSet(ArrayList<Ship> ships, ArrayList<Position> positions){
		Player player=new Player(DEFAULT_RESOURCES);
		Ability.setupBoard(player, ships, positions); //TODO: could fail to setup board
		return player.getPlayerID();
	}
	
	/**
	 * At the start of their turn, they receive resources
	 * This function attempts all of the player's chosen actions for the turn
	 * Afterwards, it tells the API to send the data back
	 */
	private void playerTurn(int playerID, ArrayList<Action> actions){
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
		for(Action a: actions){ //TODO: check the return value of these functions, save to some type of array list
			switch(a.actionID){
				case SHOOT:
					Ability.shoot(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
					break;
				case BURST_SHOT:
					Ability.burstShot(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
					break;
				case SONAR:
					Ability.sonar(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
					break;
				case MOVE:
					Ability.move(p, a.shipID, new Position(a.actionXVar, a.actionYVar, p.getBoard().getShipPosition(a.shipID).orientation));
					break;
				default:
					break;
			}
			
		}
	}

}
