package mm19.tests;

import java.util.ArrayList;

import mm19.TestUtilities;
import mm19.game.board.Position;
import mm19.game.player.Player;
import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;
import mm19.game.ships.Ship;
import org.junit.Before;
import org.junit.Test;

import mm19.game.Action;
import mm19.game.Engine;
import mm19.server.API;
import mm19.server.ShipData;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

// Note: please run all tests individually, due to the nondeterministic 
// nature of junit, some may fail to to junit presets, not failure in the 
// function - the errors stem from ship ID wrangling

//TODO Please replace the magic numbers in this file with Constants or named variables.
public class EngineTests {
		Engine testGame;
		API testapi;
		@Before 
		public void method()
		{
			testapi = mock(API.class);
			testGame = new Engine();
		}
		
		@Test
		public void testConstructor(){
		    testGame = new Engine();
			assertTrue(-1 != testGame.playerSet(genPlayer(),null));
			assertFalse(-1 != testGame.playerSet(genBrokenPlayer(),null));
			assertFalse(-1 != testGame.playerSet(genNoMainShip(),null));
			assertTrue(-1 != testGame.playerSet(genPlayer(),null));
			assertTrue(-1 != testGame.playerSet(genPlayer(),null));
			
		}
		
		public static ArrayList<ShipData> genPlayer(){
			ArrayList<ShipData> shipData = new ArrayList<ShipData>();
			shipData.add(new ShipData(0, 0, MainShip.IDENTIFIER, 6, 6, Position.Orientation.HORIZONTAL));

			shipData.add(new ShipData(0, 1, DestroyerShip.IDENTIFIER, 4, 4, Position.Orientation.HORIZONTAL));
			shipData.add(new ShipData(0, 2, PilotShip.IDENTIFIER, 2, 2, Position.Orientation.HORIZONTAL));

			return shipData;
		}
		public static ArrayList<ShipData> genBrokenPlayer(){
			ArrayList<ShipData> shipData = new ArrayList<ShipData>();
			shipData.add(new ShipData(0, 0, MainShip.IDENTIFIER, 0, 0, Position.Orientation.HORIZONTAL));
			shipData.add(new ShipData(0, 0, DestroyerShip.IDENTIFIER, 4, 4, Position.Orientation.HORIZONTAL));
			return shipData;
		}
		public static ArrayList<ShipData> genNoMainShip(){
			ArrayList<ShipData> shipData = new ArrayList<ShipData>();
			shipData.add(new ShipData(0, 0, DestroyerShip.IDENTIFIER, 4, 4, Position.Orientation.HORIZONTAL));
			return shipData;
		}

    @Test
    public void testBreakTie() {
        Player player1 = TestUtilities.initializePlayer();
        Player player2 = TestUtilities.initializePlayer();

        //All things equal, player in second argument wins.
        assertEquals(player2, Engine.breakTie(player1, player2));
        assertEquals(player1, Engine.breakTie(player2, player1));

        //Resource count has precedence over equal conditions.
        player2.takeResources(1);
        assertEquals(player1, Engine.breakTie(player1, player2));
        assertEquals(player1, Engine.breakTie(player2, player1));

        //Health of resource generating ships has precedence over resource count.
        Ship damagedShip = null;
        ArrayList<Ship> ships = player1.getBoard().getShips();
        for( Ship ship : ships ){
            if(ship.canGenerateResources()){
                damagedShip = ship;
                damagedShip.applyDamage(1);
                break;
            }
        }
        assertNotNull("BrokenTest: Player1 has no resource generating ships", damagedShip);

        assertEquals(player2, Engine.breakTie(player2, player1));
        assertEquals(player2, Engine.breakTie(player1, player2));
    }

}
