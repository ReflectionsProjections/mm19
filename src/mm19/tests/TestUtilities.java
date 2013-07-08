package mm19.tests;

import mm19.game.Ability;
import mm19.game.Constants;
import mm19.game.board.Position;
import mm19.game.player.Player;
import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;
import mm19.game.ships.Ship;

import java.util.ArrayList;

/**
 * @author mm19
 *         User: Eric
 *         Date: 5/29/13
 *         Time: 10:33 AM
 */
public class TestUtilities {


    public static Player initializePlayer() {
        ArrayList<Ship> ships = new ArrayList<Ship>();
        ArrayList<Position> positions = new ArrayList<Position>();

        //player SHIP SETUP
        //#1 MainShip
        ships.add(new MainShip());
        positions.add(new Position(MainShip.LENGTH-1, 0, Position.Orientation.HORIZONTAL));

        //#2 DestroyerShip
        ships.add(new DestroyerShip());
        positions.add(new Position(DestroyerShip.LENGTH-1, 1, Position.Orientation.HORIZONTAL));

        //#3 DestroyerShip
        ships.add(new DestroyerShip());
        positions.add(new Position(DestroyerShip.LENGTH-1, 2, Position.Orientation.HORIZONTAL));

        //#4 PilotShip
        ships.add(new PilotShip());
        positions.add(new Position(PilotShip.LENGTH-1, 3, Position.Orientation.HORIZONTAL));

        //#5 PilotShip
        ships.add(new PilotShip());
        positions.add(new Position(PilotShip.LENGTH-1, 4, Position.Orientation.HORIZONTAL));

        return initializePlayer(ships, positions);
    }

    public static Player initializePlayer(ArrayList<Ship> ships, ArrayList<Position> positions) {
        Player player = new Player(Constants.STARTING_RESOURCES);

        Ability.setupBoard(player, ships, positions);

        return player;
    }
}
