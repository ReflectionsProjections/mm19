package mm19.tests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import mm19.api.HitReport;
import mm19.exceptions.EngineException;
import mm19.exceptions.InputException;
import mm19.exceptions.ResourceException;
import mm19.game.Ability;
import mm19.game.Constants;
import mm19.game.board.Position;
import mm19.game.player.Player;
import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;
import mm19.game.ships.Ship;

import org.junit.Test;

/**
 * @author mm19
 *         User: Eric
 *         Date: 5/25/13
 *         Time: 2:18 PM
 */
public class AbilityTests {

    @Test
    public void testResetAbilityStates() {
        //TODO: Use some abilities, call resetAbilityStates and check states
    }


    @Test
    public void testGatherResources() {
        Player attacker = TestUtilities.initializePlayer();
        Player defender = TestUtilities.initializePlayer();

        int resourcesWithAllShips = 1*MainShip.RESOURCES_GENERATED
                + 2*DestroyerShip.RESOURCES_GENERATED
                + 2*PilotShip.RESOURCES_GENERATED;
        Ability.gatherResources(attacker);
        assertEquals(Constants.STARTING_RESOURCES + resourcesWithAllShips, attacker.getResources());

        int resourcesWithAllButOnePilot = 1*MainShip.RESOURCES_GENERATED
                + 2*DestroyerShip.RESOURCES_GENERATED
                + 1*PilotShip.RESOURCES_GENERATED;

        defender.getBoard().removeShip(PilotShip.LENGTH-1,4);
        Ability.gatherResources(defender);
        assertEquals(Constants.STARTING_RESOURCES + resourcesWithAllButOnePilot, defender.getResources());
    }

    @Test
    public void testShoot() {
        Player attacker = TestUtilities.initializePlayer();
        Player defender = TestUtilities.initializePlayer();

        int width = defender.getBoard().getWidth();
        int height = defender.getBoard().getHeight();

        //Test invalid ship
        try {
            Ability.shoot(attacker, defender, Integer.MAX_VALUE, width-1, height-1);
            fail("No exception thrown despite an invalid ship id");
        } catch (EngineException ee) {
            assertTrue("Exception not of type InputException", ee instanceof InputException);
        }

        ArrayList<Ship> attackerShips = attacker.getBoard().getShips();

        Ship attackerShip = null;
        for (Ship attackerShip1 : attackerShips) {
            if (!attackerShip1.canShoot()) {
                attackerShip = attackerShip1;
                break;
            }
        }
        assertNotNull("BrokenTest: Test can not continue because attacker has no ships without shoot ability", attackerShip);

        //Test use of ship with no shoot ability
        try {
            Ability.shoot(attacker, defender, attackerShip.getID(), width-1, height-1);
            fail("No exception thrown despite a ship that cannot shoot");
        } catch (EngineException ee) {
            assertTrue("Exception not of type InputException", ee instanceof InputException);
        }

        attackerShip = null;
        for (Ship attackerShip1 : attackerShips) {
            if (attackerShip1.canShoot()) {
                attackerShip = attackerShip1;
                break;
            }
        }
        assertNotNull("BrokenTest: Test can not continue because attacker has no ships with shoot ability", attackerShip);

        //Test not enough resources
        attacker.takeResources(Constants.STARTING_RESOURCES);
        attacker.giveResources(Ability.MISSILE_COST - 1);

        try {
            Ability.shoot(attacker, defender, attackerShip.getID(), width-1, height-1);
            fail("No exception thrown despite attacker not having enough resources");
        } catch (EngineException ee) {
            assertTrue("Exception not of type ResourceException", ee instanceof ResourceException);
        }

        //Test use ability with miss of defender's ships.
        attacker.giveResources(1);

        HitReport hitReport = null;
        try {
            hitReport = Ability.shoot(attacker, defender, attackerShip.getID(), width-1, height-1);
        } catch (EngineException ee) {
            fail("Test was not expecting exception");
        }
        assertTrue("HitReport not setup for expected miss.",
                hitReport.x == width-1 && hitReport.y == height-1 && !hitReport.shotSuccessful);
        assertEquals("Attackers resources not deducted.", attacker.getResources(), 0);
        assertTrue("Ship ability should now be used.", attackerShip.hasUsedAbility());

        //Test ship ability used up already
        attacker.giveResources(Ability.MISSILE_COST);
        try {
            Ability.shoot(attacker, defender, attackerShip.getID(), 0, 0);
            fail("No exception thrown despite ship already used ability");
        } catch (EngineException ee) {
            assertTrue("Exception not of type InputException", ee instanceof InputException);
        }

        //Test ship damage
        attackerShip.resetAbility();
        Ship targetShip = defender.getBoard().getShip(0,0);
        assertNotNull("BrokenTest: Test can not continue because defender has no ship at position (0,0)", targetShip);
        int health = targetShip.getHealth();

        hitReport = null;
        try {
            hitReport = Ability.shoot(attacker, defender, attackerShip.getID(), 0, 0);
        } catch (EngineException ee) {
            fail("Test was not expecting exception");
        }
        assertTrue("HitReport not setup for expected hit.",
                hitReport.x == 0 && hitReport.y ==0 && hitReport.shotSuccessful);
        assertTrue("Defending player's ship's health not deducted properly",
                health - targetShip.getHealth() == Ability.MISSILE_DAMAGE);

        //Test ship destruction
        if(targetShip.getHealth() > Ability.MISSILE_DAMAGE) {
            targetShip.applyDamage(targetShip.getHealth() - Ability.MISSILE_DAMAGE);
        }
        attacker.giveResources(Ability.MISSILE_COST);
        attackerShip.resetAbility();

        try {
            Ability.shoot(attacker, defender, attackerShip.getID(), 0, 0);
        } catch (EngineException ee) {
            fail("Test was not expecting exception");
        }
        assertEquals("Target ship's health not zero", targetShip.getHealth(), 0);
        assertNull("Ship still exists on board but should have been destroyed", defender.getBoard().getShip(0,0));
    }

    @Test
    public void testMove() {
        Player player = TestUtilities.initializePlayer();

        int height = player.getBoard().getHeight();

        //Test invalid ship
        try {
            Ability.move(player, Integer.MAX_VALUE, new Position(0, height-1, Position.Orientation.HORIZONTAL));
            fail("No exception thrown despite an invalid ship id");
        } catch (EngineException ee) {
            assertTrue("Exception not of type InputException", ee instanceof InputException);
        }

        ArrayList<Ship> playerShips = player.getBoard().getShips();

        //Currently all ships have move ability.  Uncomment if a ship without move is ever created.

//        Ship playerShip = null;
//        for (Ship playerShip1 : playerShips) {
//            if (!playerShip1.canMove()) {
//                playerShip = playerShip1;
//                break;
//            }
//        }
//        assertNotNull("BrokenTest: Test can not continue because player has no ships without move ability", playerShip);
//
//        //Test use of ship with no move ability
//        try {
//            Ability.move(player, playerShip.getID(), new Position(0, height-1, Position.Orientation.HORIZONTAL));
//            fail("No exception thrown despite a ship that cannot Move");
//        } catch (EngineException ee) {
//            assertTrue("Exception not of type InputException", ee instanceof InputException);
//        }

        Ship playerShip = null;
        for (Ship playerShip1 : playerShips) {
            if (playerShip1.canMove()) {
                playerShip = playerShip1;
                break;
            }
        }
        assertNotNull("BrokenTest: Test can not continue because player has no ships with move ability", playerShip);

        Position shipOriginalPosition = player.getBoard().getShipPosition(playerShip.getID());

        //Test not enough resources
        player.takeResources(Constants.STARTING_RESOURCES);
        player.giveResources(playerShip.getMoveCost() - 1);

        try {
            Ability.move(player, playerShip.getID(), new Position(0, height-1, Position.Orientation.HORIZONTAL));
            fail("No exception thrown despite player not having enough resources");
        } catch (EngineException ee) {
            assertTrue("Exception not of type ResourceException", ee instanceof ResourceException);
        }

        //Test use ability.
        player.giveResources(1);

       boolean moveSuccessful = false;
        try {
            moveSuccessful = Ability.move(player, playerShip.getID(), new Position(0, height-1, Position.Orientation.HORIZONTAL));
        } catch (EngineException ee) {
            fail("Test was not expecting exception");
        }
        assertTrue("Ship did not move as expected.", moveSuccessful);
        assertEquals("Player's resources not deducted.", player.getResources(), 0);
        assertTrue("Ship ability should now be used.", playerShip.hasUsedAbility());
        assertTrue("Player special ability should now be used.", player.hasUsedSpecial());

        //Test player and ship ability used up already
        player.giveResources(playerShip.getMoveCost());
        player.getBoard().moveShip(playerShip.getID(), shipOriginalPosition);

        try {
            Ability.move(player, playerShip.getID(), new Position(0, height-1, Position.Orientation.HORIZONTAL));
            fail("No exception thrown despite ship already used ability");
        } catch (EngineException ee) {
            assertTrue("Exception not of type InputException", ee instanceof InputException);
        }

        player.resetSpecialAbility();

        try {
            Ability.move(player, playerShip.getID(), new Position(0, height-1, Position.Orientation.HORIZONTAL));
            fail("No exception thrown despite ship already used ability");
        } catch (EngineException ee) {
            assertTrue("Exception not of type InputException", ee instanceof InputException);
        }

        playerShip.resetAbility();

        //Test unsuccessful move
        moveSuccessful = false;
        try {
            moveSuccessful = Ability.move(player, playerShip.getID(), new Position(-1, -1, Position.Orientation.HORIZONTAL));
        } catch (EngineException ee) {
            fail("Test was not expecting exception");
        }
        assertFalse("Ship somehow moved to an invalid position.", moveSuccessful);
        assertEquals("Player's resources were not refunded for invalid move.",
                player.getResources(), playerShip.getMoveCost());
    }
}
