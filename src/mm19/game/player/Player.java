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
    private boolean canSpecial = true;
    private int resources;

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

    /*
     * TODO: Eric made an assumption that ship Positions could be passed in as a parallel ArrayList.
     * If you would prefer a different implementation, please change this method accordingly.
     * If this implementation is fine, then just remove this comment.
     */

    /**
     * Attempts to place the player's starting ships in their requested positions.
     *
     * @param ships     An ArrayList containing the ships to place
     * @param positions An ArrayList containing Positions indicating where the ships should be placed.
     * @return The player's Board if the placements were successful, null otherwise
     */
    public Board setupBoard(ArrayList<Ship> ships, ArrayList<Position> positions) {
        for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            Position position = positions.get(i);

            boolean shipPlaced = board.placeShip(ship, position);

            if (!shipPlaced) {
                board.reset();
                return null;
            }
        }
        return board;
    }

    /**
     * Reports whether or not the player still lives
     *
     * @return True if the player still has a MainShip, false otherwise.
     */
    private boolean isAlive() {
        ArrayList<Ship> ships = board.getShips();
        for (Ship ship : ships) {
            if (ship instanceof MainShip) {
                return ship.isAlive();
            }
        }
        return false;
    }
}
