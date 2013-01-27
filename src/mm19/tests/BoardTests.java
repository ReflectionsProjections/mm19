package mm19.tests;

import static org.junit.Assert.*;


import mm19.game.board.Board;
import mm19.game.board.Position;
import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;
import mm19.game.ships.Ship;

import org.junit.Test;

import java.util.ArrayList;

public class BoardTests {

    @Test
    public void testBoardConstructor() {
        Board testBoard = new Board();
        assertEquals("Width does not match initialization value",
                Board.DEFAULT_WIDTH, testBoard.getWidth());
        assertEquals("Height does not match initialization value.",
                Board.DEFAULT_HEIGHT, testBoard.getHeight());
    }

    @Test
    public void testPlaceShip() {
        int boardWidth = 8;
        int boardHeight = 8;
        Board board = new Board(boardWidth, boardHeight);
        Ship ship1 = new MainShip();
        Position position1 = new Position(6, 6, Position.Orientation.HORIZONTAL);
        boolean successfullyPlaced = board.placeShip(ship1, position1);

        assertTrue("Was unable to place a ship on a valid position", successfullyPlaced);
        assertEquals("Unable to retrieve ship by ID", ship1, board.getShip(ship1.getID()));

        assertFalse("Cannot place a boat which is already on the board",
                board.placeShip(ship1, new Position(5, 6, Position.Orientation.HORIZONTAL)));

        Ship ship2 = new PilotShip();
        assertFalse("Ship must be placed on the board",
                board.placeShip(ship2, new Position(-1, 0, Position.Orientation.VERTICAL)));
        assertFalse("Ship must be placed on the board",
                board.placeShip(ship2, new Position(0, -1, Position.Orientation.VERTICAL)));
        assertFalse("Ship must be placed on the board",
                board.placeShip(ship2, new Position(boardWidth, 0, Position.Orientation.VERTICAL)));
        assertFalse("Ship must be placed on the board",
                board.placeShip(ship2, new Position(0, boardHeight, Position.Orientation.VERTICAL)));

        assertFalse("Ship must not extend off the edge of the board",
                board.placeShip(ship2, new Position(0, 5, Position.Orientation.HORIZONTAL)));
        assertFalse("Ship must not extend off the edge of the board",
                board.placeShip(ship2, new Position(5, 0, Position.Orientation.VERTICAL)));

        assertFalse("Ship cannot be placed on top of another ship",
                board.placeShip(ship2, new Position(5, 7, Position.Orientation.VERTICAL)));

        for (int i = 0; i < MainShip.LENGTH; i++) {
            assertEquals("Boat is not located at the required position(" + (6 - i) + ", 6)",
                    ship1, board.getShip(6 - i, 6));
        }

        for (int x = 0; x < 6 - MainShip.LENGTH + 1; x++) {
            assertNull("No Ship should exist at (" + x + ", 6)", board.getShip(x, 6));
        }
        for (int x = 7; x < boardWidth; x++) {
            assertNull("No Ship should exist at (" + x + ", 6)", board.getShip(x, 6));
        }
        for (int x = 0; x < boardWidth; x++) {
            for (int y = 0; y < 6; y++) {
                assertNull("No Ship should exist at (" + x + ", " + y + ")", board.getShip(x, y));
            }
        }
        for (int x = 0; x < boardWidth; x++) {
            for (int y = 7; y < boardHeight; y++) {
                assertNull("No Ship should exist at (" + x + ", " + y + ")", board.getShip(x, y));
            }
        }

        board.placeShip(ship2, new Position(1, 1, Position.Orientation.VERTICAL));
        ArrayList<Ship> ships = board.getShips();

        assertEquals("Expected only two ships when getting all ships", ships.size(), 2);
        for (Ship ship : ships) {
            assertTrue("Ship IDs not valid",
                    ship.getID() == ship1.getID() || ship.getID() == ship2.getID());
        }
    }

    @Test
    public void testReset() {
        int boardWidth = 8;
        int boardHeight = 8;
        Board board = new Board(boardWidth, boardHeight);

        board.placeShip(new PilotShip(), new Position(4, 5, Position.Orientation.HORIZONTAL));
        board.placeShip(new PilotShip(), new Position(4, 4, Position.Orientation.VERTICAL));
        board.placeShip(new PilotShip(), new Position(3, 5, Position.Orientation.HORIZONTAL));
        board.placeShip(new PilotShip(), new Position(2, 5, Position.Orientation.HORIZONTAL));
        board.placeShip(new PilotShip(), new Position(7, 5, Position.Orientation.HORIZONTAL));
        board.placeShip(new PilotShip(), new Position(0, 1, Position.Orientation.VERTICAL));

        board.reset();
        assertEquals("Board size not reset to 0", board.shipCount(), 0);
        for (int x = 0; x < boardWidth; x++) {
            for (int y = 0; y < boardHeight; y++) {
                assertNull("Cell (" + x + ", " + y + ") not empty after reset", board.getShip(x, y));
            }
        }
    }

    @Test
    public void testGetShipPosition() {
        int boardWidth = 8;
        int boardHeight = 8;
        Board board = new Board(boardWidth, boardHeight);
        Ship ship1 = new MainShip();
        Position position1 = new Position(6, 6, Position.Orientation.HORIZONTAL);

        board.placeShip(ship1, position1);

        Position position2 = board.getShipPosition(ship1.getID());
        assertNotNull("Returned null despite valid shipID", position2);
        assertTrue("(x,y) coordinate did not match", 6 == position2.x && 6 == position2.y);
    }

    @Test
    public void testRemoveShip() {
        int boardWidth = 8;
        int boardHeight = 8;
        Board board = new Board(boardWidth, boardHeight);
        Ship ship1 = new MainShip();
        Position position1 = new Position(6, 6, Position.Orientation.HORIZONTAL);

        board.placeShip(ship1, position1);
        Ship removedShip = board.removeShip(ship1.getID());

        assertNotNull("No reference returned when removing by ID", removedShip);
        assertEquals("Reference to ship removed not returned properly", ship1, removedShip);

        assertNull("Ship should no longer exist", board.getShip(ship1.getID()));
        assertNull("Ship should no longer exist", board.getShip(6, 6));
        assertNull("Ship should no longer exist", board.getShip(ship1.getID()));

        board.placeShip(ship1, position1);
        Ship removedShip2 = board.removeShip(6 - ship1.getLength() + 1, 6);

        assertNotNull("Removing ship using other valid cell did not work", removedShip2);

        for (int x = 0; x < boardWidth; x++) {
            for (int y = 0; y < boardHeight; y++) {
                assertNull("Cell (" + x + ", " + y + ") not empty",
                        board.getShip(x, y));
            }
        }
    }

    @Test
    public void testMoveShip() {
        int boardWidth = 8;
        int boardHeight = 8;
        Board board = new Board(boardWidth, boardHeight);
        Ship ship = new DestroyerShip();
        Position position = new Position(4, 5, Position.Orientation.HORIZONTAL);

        board.placeShip(ship, position);
        board.moveShip(ship.getID(), new Position(3, 6, Position.Orientation.VERTICAL));

        assertNull("Ship not removed from prior position", board.getShip(4, 5));
        assertNull("Ship not removed from prior position", board.getShip(2, 5));
        assertNull("Ship not removed from prior position", board.getShip(1, 5));

        assertNotNull("Ship not in new position", board.getShip(3, 6));
        assertNotNull("Ship not in new position", board.getShip(3, 5));
        assertNotNull("Ship not in new position", board.getShip(3, 4));
        assertNotNull("Ship not in new position", board.getShip(3, 3));
    }

    @Test
    public void testShipCount() {
        Board board = new Board(8, 8);
        board.placeShip(new PilotShip(), new Position(4, 5, Position.Orientation.VERTICAL));
        assertEquals("Ship count not correct after placing a ship", board.shipCount(), 1);

        board.removeShip(4, 5);
        assertEquals("Ship count not 0 after removing only ship", board.shipCount(), 0);

        board.placeShip(new PilotShip(), new Position(4, 5, Position.Orientation.HORIZONTAL));
        board.placeShip(new PilotShip(), new Position(4, 4, Position.Orientation.VERTICAL));
        board.removeShip(4, 5);
        board.placeShip(new PilotShip(), new Position(3, 5, Position.Orientation.HORIZONTAL));
        board.placeShip(new PilotShip(), new Position(2, 5, Position.Orientation.HORIZONTAL));
        board.removeShip(2, 5);
        board.placeShip(new PilotShip(), new Position(7, 5, Position.Orientation.HORIZONTAL));
        board.placeShip(new PilotShip(), new Position(0, 1, Position.Orientation.VERTICAL));
        board.removeShip(3, 5);
        board.removeShip(0, 1);

        assertEquals("Ship count not correct after a sequence of adds and removals",
                board.shipCount(), 2);
    }
}
