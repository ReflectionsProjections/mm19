package mm19.game.player;

import java.util.ArrayList;

import mm19.game.board.Board;
import mm19.game.ships.Ship;

/**
 * 
 * @author mm19
 * 
 * This will tie together the Board and Ship objects together, and keep track of 
 * the state of a player in the game.
 *
 */
public class Player {
	
	private boolean isAlive;
	private int resources;
	private Board board;
	private final int playerId;
	private boolean canSpecial;
	
	public Player(int res, int pId){
		playerId = pId;
		isAlive = true;
		resources = res;
		canSpecial = true;
		
	}
	
	public Board setMap(ArrayList<Ship> ships){
		for(int i = 0; i < ships.size(); i++){
			if(!board.placeShip(ships.get(i)))
			{
				board.reset();
				return null;
			}
		}
		return board;
	}
	
	private boolean isDead() {
		if(board.shipCount() == 0)
			return true;
		return false;
	}
	

}
