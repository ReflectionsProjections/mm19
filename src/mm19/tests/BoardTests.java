package mm19.tests;

import static org.junit.Assert.*;


import mm19.game.board.Board;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;
import mm19.game.ships.Ship;

import org.junit.Test;

public class BoardTests {


	@Test
	public void testGetHeightAndWidth() { //sanity check
		Board testBoard= new Board(8,9); 
		assertEquals("How the hell did this fail?", 8, testBoard.getWidth());
		assertEquals("How the hell did this fail?", 9, testBoard.getHeight());
	}

	@Test
	public void testGetShip() { //test getting as setting ships
		Board testBoard= new Board(8,8); 
		Ship testShip = new MainShip(3, 3, Ship.Orientation.HORIZONTAL);
		assertTrue(testBoard.placeShip(testShip));
		assertTrue(testBoard.placeShip(new PilotShip(4,5, Ship.Orientation.HORIZONTAL)));
		assertEquals("did I get the correct ship?", testBoard.getShip(3,3), testShip);
	}
	@Test
	public void testSetOnlyVaidShips() { //test getting as setting ships
		Board testBoard= new Board(8,8); 
		Ship testShip = new MainShip(4, 5, Ship.Orientation.HORIZONTAL);
		assertTrue(testBoard.placeShip(testShip));
		assertFalse(testBoard.placeShip(new PilotShip(4,5, Ship.Orientation.HORIZONTAL))); //this one will fail as the spot is taken
		assertFalse(testBoard.placeShip(new PilotShip(4,5, Ship.Orientation.HORIZONTAL)));// out of bounds 		
		assertEquals("did I get the correct ship?", testBoard.getShip(4,5), testShip);
		}
	@Test
	public void testShipCount() { //test getting as setting ships
		Board testBoard= new Board(8,8); 
		Ship testShip = new MainShip(4, 5, Ship.Orientation.HORIZONTAL);
		assertTrue(testBoard.placeShip(testShip)); //this one will fail as the spot is taken
		assertFalse(testBoard.placeShip(new PilotShip(4,5, Ship.Orientation.HORIZONTAL)));// out of bounds
		assertTrue(testBoard.placeShip(new PilotShip(3,5, Ship.Orientation.HORIZONTAL)));
		assertTrue(testBoard.placeShip(new PilotShip(2,5, Ship.Orientation.HORIZONTAL)));
		assertTrue(testBoard.placeShip(new PilotShip(1,5, Ship.Orientation.HORIZONTAL)));
		assertTrue(testBoard.placeShip(new PilotShip(0,5, Ship.Orientation.HORIZONTAL)));
		assertTrue(testBoard.removeShip(0,5));
		assertTrue(testBoard.removeShip(1,5));
		assertEquals("did I get the correct ship?", testBoard.shipCount(), 5);
		}
	

}
