package mm19.game;

import java.util.ArrayList;

import mm19.game.player.Player;

/**
 * @author mm19
 *
 * This will put all the pieces of the game together, and actually make things run.
 */
public class Engine{
	private Player p1;
	private Player p2;

	/**
	 * the constructor is called by the server (or API?) to start the game.
	 */
    public Engine(){
        createBoard();
    }
	
	/**
	 * createBoard sets up the variables needed to start the game
	 */
	private void createBoard(){
		
	}
	
	/**
	 * This function sets up the player's pieces on the board as specified
	 */
	public void playerSet(){
		
	}
	
	/**
	 * At the start of their turn, they receive resources
	 * This function attempts all of the player's chosen actions for the turn
	 * Afterwards, it tells the API to send the data back
	 */
	private void playerTurn(){
	}
	
	/**
	 * This function does the requested actions by the player, then writes the output to the API
	 */
	private void playerAction(){
		
	}

}
