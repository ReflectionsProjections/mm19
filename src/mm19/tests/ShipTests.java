package mm19.tests;

import static org.junit.Assert.*;

import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ShipTests {

	@Test
	public void testMainShip() {
		
		// Test constructor
		MainShip ms = new MainShip();
		assertFalse(ms == null);
		assertEquals(MainShip.HEALTH, ms.getHealth());
		
		// Test slow death
		ms.applyDamage(5);
		assertTrue(ms.isAlive());
		assertEquals(ms.getHealth(),MainShip.HEALTH-5);
		ms.applyDamage(ms.getHealth());
		assertFalse(ms.isAlive());
		assertEquals(ms.getHealth(),0);
		
		// Test exact death
		ms = new MainShip();
		assertTrue(ms.isAlive());
		ms.applyDamage(MainShip.HEALTH);
		assertEquals(ms.getHealth(),0);
		assertFalse(ms.isAlive());
		
		// Test overkill
		ms = new MainShip();
		assertTrue(ms.isAlive());
		ms.applyDamage(MainShip.HEALTH*2);
		assertFalse(ms.isAlive());
		
		// Test negative damage
		ms = new MainShip();
		ms.applyDamage(-MainShip.HEALTH);
		assertEquals(ms.getHealth(),MainShip.HEALTH*2);
		assertTrue(ms.isAlive());
		
		// - Kill half the ship -
		ms.applyDamage(MainShip.HEALTH);
		assertEquals(ms.getHealth(),MainShip.HEALTH);
		assertTrue(ms.isAlive());
		
		// Kill the other half
		assertEquals(ms.getHealth(),0);
		assertFalse(ms.isAlive());
		
		// - Test off-by-one death -
		ms = new MainShip();
		ms.applyDamage(MainShip.HEALTH-1);
		assertTrue(ms.isAlive());
		assertEquals(ms.getHealth(),1);
		
		// Finish the ship off
		ms.applyDamage(1);
		assertFalse(ms.isAlive());
		assertEquals(ms.getHealth(),0);
	}

	@Test
	public void testDestroyerShip() {
		
		// Test constructor
		DestroyerShip ms = new DestroyerShip();
		assertFalse(ms == null);
		assertEquals(ms.HEALTH, ms.getHealth());
		
		// Test slow death
		ms.applyDamage(5);
		assertTrue(ms.isAlive());
		assertEquals(ms.getHealth(),DestroyerShip.HEALTH-5);
		ms.applyDamage(ms.getHealth());
		assertFalse(ms.isAlive());
		assertEquals(ms.getHealth(),0);
		
		// Test exact death
		ms = new DestroyerShip();
		assertTrue(ms.isAlive());
		ms.applyDamage(DestroyerShip.HEALTH);
		assertEquals(ms.getHealth(),0);
		assertFalse(ms.isAlive());
		
		// Test overkill
		ms = new DestroyerShip();
		assertTrue(ms.isAlive());
		ms.applyDamage(DestroyerShip.HEALTH*2);
		assertFalse(ms.isAlive());
		
		// Test negative damage
		ms = new DestroyerShip();
		ms.applyDamage(-MainShip.HEALTH);
		assertEquals(ms.getHealth(),DestroyerShip.HEALTH*2);
		assertTrue(ms.isAlive());
		
		// - Kill half the ship -
		ms.applyDamage(DestroyerShip.HEALTH);
		assertEquals(ms.getHealth(),DestroyerShip.HEALTH);
		assertTrue(ms.isAlive());
		
		// Kill the other half
		assertEquals(ms.getHealth(),0);
		assertFalse(ms.isAlive());
		
		// - Test off-by-one death -
		ms = new DestroyerShip();
		ms.applyDamage(DestroyerShip.HEALTH-1);
		assertTrue(ms.isAlive());
		assertEquals(ms.getHealth(),1);
		
		// Finish the ship off
		ms.applyDamage(1);
		assertFalse(ms.isAlive());
		assertEquals(ms.getHealth(),0);
		
	}

	@Test
	public void testPilotShip() {
		
		// Test constructor
		PilotShip ms = new PilotShip();
		assertFalse(ms == null);
		assertEquals(PilotShip.HEALTH, ms.getHealth());
		
		// Test slow death
		ms.applyDamage(5);
		assertTrue(ms.isAlive());
		assertEquals(ms.getHealth(),PilotShip.HEALTH-5);
		ms.applyDamage(ms.getHealth());
		assertFalse(ms.isAlive());
		assertEquals(ms.getHealth(),0);
		
		// Test exact death
		ms = new PilotShip();
		assertTrue(ms.isAlive());
		ms.applyDamage(PilotShip.HEALTH);
		assertEquals(ms.getHealth(),0);
		assertFalse(ms.isAlive());
		
		// Test overkill
		ms = new PilotShip();
		assertTrue(ms.isAlive());
		ms.applyDamage(PilotShip.HEALTH*2);
		assertFalse(ms.isAlive());
		
		// Test negative damage
		ms = new PilotShip();
		ms.applyDamage(-PilotShip.HEALTH);
		assertEquals(ms.getHealth(),PilotShip.HEALTH*2);
		assertTrue(ms.isAlive());
		
		// - Kill half the ship -
		ms.applyDamage(PilotShip.HEALTH);
		assertEquals(ms.getHealth(),PilotShip.HEALTH);
		assertTrue(ms.isAlive());
		
		// Kill the other half
		assertEquals(ms.getHealth(),0);
		assertFalse(ms.isAlive());
		
		// - Test off-by-one death -
		ms = new PilotShip();
		ms.applyDamage(PilotShip.HEALTH-1);
		assertTrue(ms.isAlive());
		assertEquals(ms.getHealth(),1);
		
		// Finish the ship off
		ms.applyDamage(1);
		assertFalse(ms.isAlive());
		assertEquals(ms.getHealth(),0);
	}
}
