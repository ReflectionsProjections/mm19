package mm19.server;
/*
 * This Ship Data class is a java representation  of the JSON the server will 
 * send detailing each ship.
 */
public class ShipData {
	final public int health;
	final public int ID;
	final public String type;
	final public int xCoord;
	final public int yCoord;
	final public String orientation;
	
	public ShipData(int health, int ID, String type,int xCoord, int yCoord,String orientation){
		this.health = health;
		this.ID = ID;
		this.type = type;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.orientation = orientation;
	}
}
