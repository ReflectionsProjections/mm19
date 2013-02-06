package mm19.game.player;

import java.util.ArrayList;

import mm19.game.board.Board;
import mm19.game.board.Position;
import mm19.game.ships.MainShip;
import mm19.game.ships.Ship;

/**
 * @author mm19
 *
 * This will tie together the Board and Ship objects together, and keep track of
 * the state of a player in the game.
 */
public class Player {
    private static int nextPlayerID = 0;

    final private int playerID;

    private Board board = new Board();
    private int resources;
    private boolean specialUsed = false;

    /**
     * Constructor
     * Initializes the player's initial resources and gives them an empty board.
     *
     * @param resources The Player's initial resources
     */
    public Player(int resources) {
        playerID = nextPlayerID;
        nextPlayerID++;
        this.resources = resources;
    }

    /**
     * Reports whether or not the player still lives
     *
     * @return True if the player still has a MainShip, false otherwise.
     */
    public boolean isAlive() {
        ArrayList<Ship> ships = board.getShips();
        for (Ship ship : ships) {
            if (ship instanceof MainShip) {
                return ship.isAlive();
            }
        }
        return false;
    }

    /**
     * Indicates whether or not the player has used up their special ability this turn
     * @return True if they have used their special ability, false otherwise
     */
    public boolean hasUsedSpecial() {
        return specialUsed;
    }

    /**
     * Set state indicating that the player has used their special ability
     */
    public void useSpecialAbility() {
        specialUsed = true;
    }

    /**
     * Set state indicating that the player has their special ability available again
     */
    public void resetSpecialAbility() {
        specialUsed = false;
    }
    
    /**
     * Returns the ID of the player
     * 
     * @return playerID
     */
    
    public int getPlayerID(){
    	return this.playerID;
    }    
    /**
     * Sets the resources for the player
     * 
     * @param resources	An int as the number of resources to be added
     */
    public void setResources(int resources){
    	this.resources = resources;
    }
    
    /**
     * Returns the current resources the player has
     * 
     * @return number of resources the player has
     */    
    public int getResources(){
    	return this.resources;
    }
    
    /**
     * @return the player's board
     */
    public Board getBoard(){
    	return this.board;
    }
    
    /**
     * Subtracts resources from the player's total resources
     * @param resources	Number of resources to be subtracted
     * @return True of successful and false if not
     */
    public boolean takeResources(int resources){
    	if(this.resources >= resources){
    		this.resources -= resources;
    		return true;
    	}
    	return false;
    }

    /**
     * Adds resources to the player's total resources
     * @param resources	Number of resources to be subtracted
     * @return True of successful and false if not
     */
    public void giveResources(int resources){
        if(0 < resources){
            this.resources += resources;
        }
    }
}
