package mm19.game.board;

import mm19.game.ships.Ship;

import java.util.Iterator;
import java.util.LinkedList;


/**
 * 
 * @author mm19
 * 
 * The entity which keeps track of positions of ships.
 *
 */
public class Board {

    private Tile[][] tiles;
    private int width;
    private int height;

    private LinkedList<Ship> ships = new LinkedList<Ship>();

    /**
     * Constructor.  Takes a width and a height and initializes the double Tile array.
     * @param boardWidth
     * @param boardHeight
     */
    public Board(int boardWidth, int boardHeight){
        width = boardWidth;
        height = boardHeight;
        tiles = new Tile[width][height];
        for(int x = 0; x < width; x++) {
            for( int y = 0; y < height; y++){
                tiles[x][y] = new Tile();
            }
        }
    }

    /**
     * Returns the width of the board
     * @return int Board width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the board
     * @return int Board height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns a reference to a ship if it occupies coordinate (x,y) on the board.
     * @param x
     * @param y
     * @return A Ship if one exists at the position, null otherwise
     */
    public Ship getShip(int x, int y){
        if(inBounds(x,y) && isOccupied(x,y)){
            return tiles[x][y].getShip();
        } else {
            return null;
        }
    }

    /**
     * Searches the ships storage for the given shipID
     * @param shipID
     * @return
     */
    public Ship getShip(int shipID){
        Iterator it = ships.descendingIterator();
        while(it.hasNext()){
            Ship ship = (Ship)it.next();
            if(ship.getID() == shipID)
                return ship;
        }
        return null;
    }

    /**
     * Attempts to place a ship on the board.
     *
     * @param ship The ship to place
     * @param shipX
     * @param shipY
     * @param orientation Orientation of the ship
     * @return If ship was successfully place, returns true, otherwise false
     */
    public boolean placeShip(Ship ship, int shipX, int shipY, Ship.Orientation orientation){
        if(!canPlaceShip(ship, shipX, shipY, orientation))
            return false;

        ships.add(ship);
        ship.setX(shipX);
        ship.setY(shipY);
        ship.setOrientation(orientation);

        int x = shipX;
        int y = shipY;
        for(int i = 0; i < ship.getLength(); i++){
            if(orientation == Ship.Orientation.VERTICAL){
                y = shipY - i;
            }else{
                x = shipX - i;
            }
            tiles[x][y].setShip(ship);
        }
        return true;
    }

    /**
     * Removes the ship occupying the given x,y coordinate and returns a reference to the ship.
     * @param x
     * @param y
     * @return The Ship removed, null if no ship could be removed.
     */
    public Ship removeShip(int x, int y){
        if(!inBounds(x,y) || !isOccupied(x,y))
            return null;

        Ship ship = tiles[x][y].getShip();
        int shipX = ship.getX();
        int shipY = ship.getY();
        Ship.Orientation orientation = ship.getOrientation();

        for(int i = 0; i < ship.getLength(); i++){
            if(orientation == Ship.Orientation.VERTICAL){
                y = shipY - i;
            }else{
                x = shipX - i;
            }
            tiles[x][y].removeShip();
        }

        ships.remove(ship);

        return ship;
    }

    /**
     * Resets the board to it's post-constructor state by removing all ships.
     */
    public void reset(){
        while(ships.size() > 0){
            Ship ship = ships.getFirst();
            removeShip(ship.getX(), ship.getY());
        }
    }

    /**
     * Reports the number of ships that exist on the board.
     * @return int Number of Ships on the board.
     */
    public int shipCount(){
        return ships.size();
    }

    /**
     * Reports if a ship can be placed on the board
     *
     * @param ship The ship to place
     * @param x
     * @param y
     * @param orientation Orientation of the ship
     * @return Returns true if the ship can be placed, false otherwise
     */
    private boolean canPlaceShip(Ship ship, int x, int y, Ship.Orientation orientation){
        int xCoord = x;
        int yCoord = y;
        for(int i = 0; i < ship.getLength(); i++){
            if(orientation == Ship.Orientation.VERTICAL){
                yCoord = y - i;
            }else{
                xCoord = x - i;
            }
            if(!inBounds(xCoord, yCoord) || isOccupied(xCoord, yCoord)){
                return false;
            }
        }
        return true;
    }

    /**
     * Given an (x,y) coordinate, determines if there is a corresponding board location.
     * @param x
     * @param y
     * @return returns true if cell is within bounds of double array, false if not.
     */
    private boolean inBounds(int x, int y){
        return (x >= 0) && (x < width) && (y >= 0) && (y < height);
    }

    /**
     * Given an (x,y) coordinate, reports if the board location is occupied
     * @param x
     * @param y
     * @return true is an object occupies Tile, false otherwise
     */
    private boolean isOccupied(int x, int y) {
        return tiles[x][y].isOccupied();
    }

    /**
     *
     * @author mm19
     *
     * The entities which make up the board
     *
     */
    public static class Tile {

        private boolean occupied = false;

        /**
         * Reference to the ship occupying this Tile.
         */
        private Ship currentShip = null;



        /**
         * Default Tile constructor
         */
        public Tile(){

        }

        /**
         * @return true if Tile is occupied, false if not.
         */
        public boolean isOccupied(){
            return occupied;
        }

        /**
         * Sets a reference to the ship currently occupying this Tile
         * @param ship
         */
        public void setShip(Ship ship){
            currentShip = ship;
            occupied = true;
        }

        /**
         * @return Returns a reference to the current ship.
         */
        public Ship getShip(){
            return currentShip;
        }

        /**
         * Removes a reference to the ship currently occupying this Tile
         */
        public void removeShip(){
            currentShip = null;
            occupied = false;
        }  
    }
}





