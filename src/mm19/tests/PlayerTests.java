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
		
		/* 
		 * this test is broken as it assumes that this tests runs first but junit has no
		 * method to ensure as tests are run asynchronously 
		 * 
		assertEquals("playerID does not match original nextPlayerID (should be 0)",
				0, testPlayer.getPlayerID());*/
	}
	
	/*
	 * TODO: Implement these Board Setup test cases.  
	 * Not entirely sure how to set up the two ArrayLists for this.
	 
	@Test
	public void testSetupBoard() {
		return;
	}*/
	
	@Test
	public void testChargePlayer() {
		Player testPlayer = new Player(10);
		boolean charged = testPlayer.chargePlayer(6);
		assertTrue("chargePlayer returned false",
				charged);
		assertEquals("Player wasn't correctly charged",
				testPlayer.getResources(), 4);
		boolean overCharge = testPlayer.chargePlayer(10);
		assertFalse("Player was charged with insufficient resources",
				overCharge);
		assertEquals("Player was charged more than the number of resources they have",
				testPlayer.getResources(), 4);
		
	}

}
