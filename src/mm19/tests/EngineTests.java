package mm19.tests;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import mm19.game.Action;
import mm19.game.Engine;
import mm19.game.board.Position;
import mm19.server.API;
import mm19.server.ShipData;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

// Note: please run all tests individually, due to the nondeterministic 
// nature of junit, some may fail to to junit presets, not failure in the 
// function - the errors stem from ship ID wrangling

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
		
		@Test
		public void testFire(){
		    testGame = new Engine();
		    testGame.playerSet(genPlayer(), "A");
            testGame.playerSet(genPlayer(), "B");
            testGame.playerTurn("A", genFire()); // should hit 
            testGame.playerTurn("A", genFire()); // should 1 hit 1 not enough money
            testGame.playerTurn("A", genFire()); // should any hit not enough money
            testGame.playerTurn("A", genFire()); // should 1 hit kill 1 not enough money
            testGame.playerTurn("A", genFire()); // should 1 hit miss 1 not enough money
		}
		
		@Test
		public void testMove(){

		    testGame = new Engine();
			testGame.playerSet(genPlayer(), "A");
			testGame.playerSet(genPlayer(), "B");
			testGame.playerTurn("A", genMove()); //Cannot move Destroyer
			testGame.playerTurn("A", new ArrayList<Action>()); //wait
			testGame.playerTurn("A", new ArrayList<Action>()); //wait
			testGame.playerTurn("A", genMove()); //CAN move Destroyer

		}
		
		@Test
		public void testSonar(){
		    testGame = new Engine();
		    testGame.playerSet(genPlayer(), "A");
            testGame.playerSet(genPlayer(), "B");
            testGame.playerTurn("A", genSonar()); // should return positive
		}
		
		@Test
		public void testBurst(){
		    testGame = new Engine();
            testGame.playerSet(genPlayer(), "A");
            testGame.playerSet(genPlayer(), "B");
            testGame.playerTurn("A", genBurst()); // not enough money
            testGame.playerTurn("A", genBurst()); // should go boom
            testGame.playerTurn("A", genBurst()); // not enough money
            testGame.playerTurn("A", genBurst()); // not enough money
			
		}
		
		public static ArrayList<ShipData> genPlayer(){
			ArrayList<ShipData> shipData = new ArrayList<ShipData>();
			shipData.add(new ShipData(0, 0, Engine.MAINSHIP, 6, 6, "H"));

			shipData.add(new ShipData(0, 1, Engine.DESTROYER, 4, 4, "H"));
			shipData.add(new ShipData(0, 2, Engine.PILOT, 2, 2, "H"));

			return shipData;
		}
		public static ArrayList<ShipData> genBrokenPlayer(){
			ArrayList<ShipData> shipData = new ArrayList<ShipData>();
			shipData.add(new ShipData(0, 0, Engine.MAINSHIP, 0, 0, "H"));
			shipData.add(new ShipData(0, 0, Engine.DESTROYER, 4, 4, "H"));
			return shipData;
		}
		public static ArrayList<ShipData> genNoMainShip(){
			ArrayList<ShipData> shipData = new ArrayList<ShipData>();
			shipData.add(new ShipData(0, 0, Engine.DESTROYER, 4, 4, "H"));
			return shipData;
		}

		public static ArrayList<Action> genMove(){
			ArrayList<Action> move = new ArrayList<Action>();
			//the first number in the Action(s) here are magic dependent on the known placement w/n the board
			//and this one is wrong though move works
			move.add(new Action(1,"MH",4,5,0));
			                                    
			return move;
		}
		
		public static ArrayList<Action> genFire(){
            ArrayList<Action> move = new ArrayList<Action>();
            //the first number in the Action(s) here are magic dependent on the known placement w/n the board
            move.add(new Action(0,"F",4,4,0)); 
            move.add(new Action(1,"F",4,4,0));
            return move;
        }
		public static ArrayList<Action> genSonar(){
            ArrayList<Action> move = new ArrayList<Action>();
            //the first number in the Action(s) here are magic dependent on the known placement w/n the board
            move.add(new Action(23,"S",4,4,0));
            return move;
        }
		public static ArrayList<Action> genBurst(){
            ArrayList<Action> move = new ArrayList<Action>();
            //the first number in the Action(s) here are magic dependent on the known placement w/n the board
            move.add(new Action(19,"BS",4,4,0));
            return move;
        }

}
