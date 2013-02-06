package mm19.tests;

import static org.junit.Assert.*;

import mm19.game.board.Board;
import mm19.game.board.Position;
import mm19.game.player.Player;
import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;
import mm19.game.ships.Ship;

import org.junit.Test;

import java.util.ArrayList;

public class PlayerTests {
	
	@Test
	public void testPlayerContructor() {
		Player testPlayer = new Player(10);
		assertEquals("Initial resource amount does not match player's initial resources", 
				testPlayer.getResources(), 10);
	}
	
	@Test
	public void testTakeResources() {
		Player testPlayer = new Player(10);
		boolean charged = testPlayer.takeResources(6);
		assertTrue("chargePlayer returned false",
				charged);
		assertEquals("Player wasn't correctly charged",
				testPlayer.getResources(), 4);
		boolean overCharge = testPlayer.takeResources(10);
		assertFalse("Player was charged with insufficient resources",
				overCharge);
		assertEquals("Player was charged more than the number of resources they have",
				testPlayer.getResources(), 4);
		
	}
	
	@Test
	public void testIsAlive() {
		Player testPlayer = new Player(10);
		assertFalse("Player should be dead, returned as alive", 
				testPlayer.isAlive());
		MainShip testMainShip = new MainShip();
		Position testPosition1 = new Position(50, 50, Position.Orientation.HORIZONTAL);
		testPlayer.getBoard().placeShip(testMainShip, testPosition1);
		assertTrue("Player should be alive, returned dead",
				testPlayer.isAlive());
		DestroyerShip testDestroyerShip = new DestroyerShip();
		Position testPosition2 = new Position(20, 20, Position.Orientation.HORIZONTAL);
		testPlayer.getBoard().placeShip(testDestroyerShip, testPosition2);
		assertTrue("Player should be alive, returned dead",
				testPlayer.isAlive());
		testPlayer.getBoard().removeShip(50, 50);
		assertFalse("Player should be dead, returned as alive",
				testPlayer.isAlive());
	}
	
	@Test
	public void testGiveResources() {
		Player testPlayer = new Player(0);
		testPlayer.giveResources(10);
		int playerResources = testPlayer.getResources();
		assertEquals("Resources should equal 10, but they actually equal" + playerResources,
				playerResources, 10);
		testPlayer.giveResources(-10);
		playerResources = testPlayer.getResources();
		assertEquals("Resources should equal 10, but they equal" + playerResources,
				playerResources, 10);	
	}

}
