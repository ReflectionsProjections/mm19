package mm19.tests;

import org.junit.Before;
import org.junit.Test;
import mm19.game.Engine;
import mm19.server.API;
import static org.mockito.Mockito.*;

public class EngineTests {
		Engine testGame;
		API testapi;
		@Before 
		public void method()
		{
			testapi = mock(API.class);
			testGame = new Engine(null);
		}
		
		@Test
		public void testConstructor(){
			
		}
		
		@Test
		public void testFire(){
			
		}
		
		@Test
		public void testMove(){
			
		}
		
		@Test
		public void testSonar(){
			
		}
		
		@Test
		public void testBurst(){
			
		}
		
		@Test
		public void testTurn(){
			
		}
}
