package mm19.game;

import	mm19.game.ships.*;

public class SonarReport {
	final public int dist;
	final public Ship ship;
	
	public SonarReport(int dist, Ship ship){
		this.dist = dist;
		this.ship = ship;
	}
}
