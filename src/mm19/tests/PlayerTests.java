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

        //TODO The value of playerIDs are dependant on the order of tests being run.  Commenting out this test.
        /* assertEquals("playerID does not match original nextPlayerID (should be 0)",
		 *		testPlayer.getPlayerID(), 0);
		 */
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

}
