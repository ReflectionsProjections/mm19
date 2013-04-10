package mm19.game;

import java.util.ArrayList;

import mm19.exceptions.EngineException;
import mm19.exceptions.InputException;
import mm19.exceptions.ResourceException;
import mm19.game.board.Board;
import mm19.game.board.Position;
import mm19.game.player.Player;
import mm19.game.ships.Ship;

/**
 * @author mm19
 *         User: Eric
 *         Date: 1/29/13
 *         Time: 9:05 PM
 *
 *         Static methods to interact with the game elements
 */
public class Ability {
    final public static int MISSILE_DAMAGE = 10;
    final public static int MISSILE_COST = 50;

    final private static double BURST_SHOT_EFFECTIVENESS = 1.0;
    final public static int BURST_SHOT_DAMAGE = (int)(MISSILE_DAMAGE * BURST_SHOT_EFFECTIVENESS);
    final public static int BURST_SHOT_COST = 250;
    final public static int BURST_SHOT_RADIUS = 2; //3x3 area

    final public static int SONAR_RADIUS = 3; //5x5 area
    final public static int SONAR_COST = 110;

    final public static int MOVE_COST_PER_UNIT_LENGTH = 50;

    public enum Type {SHOOT, BURST_SHOT, MOVE, SONAR}

    /**
     * Resets the abilities of the player and their ships.
     * @param player The player to reset abilities on.
     */
    public static void resetAbilityStates(Player player){
        player.resetSpecialAbility();
        Board board = player.getBoard();
        ArrayList<Ship> ships = board.getShips();
        for(Ship ship : ships) {
            ship.resetAbility();
        }
    }


    /**
     * Updates the player's resources by polling the boats they own for resources generated.
     *
     * @param player The player to update the resources of
     */
    public static void gatherResources(Player player) {
        Board board = player.getBoard();
        ArrayList<Ship> ships = board.getShips();
        for (Ship ship : ships) {
            if(ship.canGenerateResources()) {
                player.giveResources(ship.getResources());
            }
        }
    }

    /**
     * Attempts to place the player's starting ships in their requested positions.
     *
     * @param player    The player whose board to setup
     * @param ships     An ArrayList containing the ships to place
     * @param positions An ArrayList containing Positions indicating where the ships should be placed.
     * @return True if the ships could be placed, false otherwise.
     */
    public static boolean setupBoard(Player player, ArrayList<Ship> ships, ArrayList<Position> positions) {
        Board board = player.getBoard();
        for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            Position position = positions.get(i);

            boolean shipPlaced = board.placeShip(ship, position);

            if (!shipPlaced) {
                board.reset();
                return false;
            }
        }
        return true;
    }

    /**
     * Fires a shot at the opponent's board
     *
     * @param attackingPlayer The player taking the shot
     * @param targetPlayer    The player who is being shot at
     * @param shipID          ID of the ship attacking
     * @param targetX         The x coordinate to attack
     * @param targetY         The y coordinate to attack
     * @return Null if the attackingPlayer did not have enough resources, a HitReport otherwise.
     */
    public static HitReport shoot(Player attackingPlayer, Player targetPlayer, int shipID, int targetX, int targetY) {
        Ship attackingShip = attackingPlayer.getBoard().getShip(shipID);
        if(!attackingShip.canShoot() || attackingShip.hasUsedAbility()) {
        	throw new InputException();
        }

        boolean hadResources = attackingPlayer.takeResources(MISSILE_COST);
        if (!hadResources) {
        	throw new ResourceException();
        }

        attackingShip.useAbility();

        Board board = targetPlayer.getBoard();
        Ship targetShip = board.getShip(targetX, targetY);
        boolean shipHit = false;
        if (targetShip != null) {
            shipHit = true;
            targetShip.applyDamage(MISSILE_DAMAGE);
            if (!targetShip.isAlive()) {
                board.removeShip(targetShip.getID());
            }
        }
        return new HitReport(targetX, targetY, shipHit, targetShip);
    }

    /**
     * Attempt to move a ship
     *
     * @param player      The player moving a ship
     * @param shipId      The id of a ship to move
     * @param newPosition A position object indicating the new position of the ship
     * @return False if the move could not be made or player did not have enough resources, true otherwise.
     */
    public static boolean move(Player player, int shipId, Position newPosition) {
        Board board = player.getBoard();
        Ship ship = board.getShip(shipId);

        if(player.hasUsedSpecial() || !ship.canMove() || ship.hasUsedAbility()) {
        	throw new InputException();
        }

        boolean hadResources = player.takeResources(ship.getMoveCost());
        if (!hadResources) {
        	throw new ResourceException();
        }

        boolean moveSuccessful = board.moveShip(shipId, newPosition);
        if (moveSuccessful) {
            player.useSpecialAbility();
            ship.useAbility();
        } else {
            //Refund the player as the move was not possible
            player.giveResources(ship.getMoveCost());
        }
        return moveSuccessful;
    }

    /**
     * Fires a burst shot at the opponent's board
     *
     * @param attackingPlayer The player taking a shot
     * @param targetPlayer    The player being shot at
     * @param shipID          The ID of the ship using burst shot
     * @param targetX         The x coordinate to shoot at
     * @param targetY         The y coordinate to shoot at
     * @return Null if the attackingPlayer did not have enough resources, an ArrayList of hitReports otherwise
     */
    public static ArrayList<HitReport>
    burstShot(Player attackingPlayer, Player targetPlayer, int shipID, int targetX, int targetY) {
        Board attackersBoard = attackingPlayer.getBoard();
        Ship attackingShip = attackersBoard.getShip(shipID);

        if(attackingPlayer.hasUsedSpecial() || !attackingShip.canBurstShot() || attackingShip.hasUsedAbility()) {
        	throw new InputException();
        }

        boolean hadResources = attackingPlayer.takeResources(BURST_SHOT_COST);
        if (!hadResources) {
            throw new ResourceException();
        }

        attackingShip.useAbility();
        attackingPlayer.useSpecialAbility();

        ArrayList<HitReport> hitReports = new ArrayList<HitReport>();
        Board board = targetPlayer.getBoard();
        int NECornerX = targetX - BURST_SHOT_RADIUS + 1;
        int NECornerY = targetY - BURST_SHOT_RADIUS + 1;

        for (int x = NECornerX; x < NECornerX + BURST_SHOT_RADIUS; x++) {
            for (int y = NECornerY; y < NECornerY + BURST_SHOT_RADIUS; y++) {
                Ship ship = board.getShip(x, y);
                boolean hitSuccessful;
                if (ship != null) {
                    ship.applyDamage(BURST_SHOT_DAMAGE);
                    if (!ship.isAlive()) {
                        board.removeShip(ship.getID());
                    }
                    hitSuccessful = true;
                } else {
                    hitSuccessful = false;
                }
                hitReports.add(new HitReport(x, y, hitSuccessful, ship));
            }
        }

        return hitReports;
    }

    /**
     * Pings an area on the opponent's map
     *
     * @param attackingPlayer The player using sonar
     * @param targetPlayer    The player being pinged by sonar
     * @param shipID          The ID of the ship using sonar
     * @param targetX         The x coordinate to use sonar on
     * @param targetY         The y coordinate to use sonar on
     * @return Null if attackingPlayer didn't have enough resources, a list of ship distances otherwise
     */
    public static ArrayList<SonarReport>
    sonar(Player attackingPlayer, Player targetPlayer, int shipID, int targetX, int targetY) {
        Board attackersBoard = attackingPlayer.getBoard();
        Ship attackingShip = attackersBoard.getShip(shipID);
        if(attackingPlayer.hasUsedSpecial() || !attackingShip.canSonar() || attackingShip.hasUsedAbility()) {
            throw new InputException();
        }

        boolean hadResources = attackingPlayer.takeResources(SONAR_COST);
        if (!hadResources) {
            throw new ResourceException();
        }

        attackingPlayer.useSpecialAbility();
        attackingShip.useAbility();

        ArrayList<SonarReport> sonarReports = new ArrayList<SonarReport>();
        Board board = targetPlayer.getBoard();
        int NECornerX = targetX - BURST_SHOT_RADIUS + 1;
        int NECornerY = targetY - BURST_SHOT_RADIUS + 1;

        for (int x = NECornerX; x < NECornerX + SONAR_RADIUS; x++) {
            for (int y = NECornerY; y < NECornerY + SONAR_RADIUS; y++) {
                pingCoordinate(board, sonarReports, targetX, targetY, x, y);
            }
        }
        return sonarReports;
    }

    /**
     * Pings an individual Tile on the board with sonar.
     * Removes prior reports if this coordinate is better.
     *
     * @param board        Board to ping
     * @param sonarReports ArrayList of current sonarReports
     * @param targetX      The x coordinate of the sonar ping
     * @param targetY      The y coordinate of the sonar ping
     * @param x            The x coordinate being checked
     * @param y            The y coordinate being checked
     */
    private static void
    pingCoordinate(Board board, ArrayList<SonarReport> sonarReports, int targetX, int targetY, int x, int y) {
        Ship ship = board.getShip(x, y);
        if (ship != null) {
            int shipDistance = sonarDistance(targetX, targetY, x, y);
            boolean oldReportFound = false;
            boolean oldReportRemoved = false;
            for (SonarReport priorReport : sonarReports) {
                if (priorReport.ship == ship) {
                    oldReportFound = true;
                    if (shipDistance < priorReport.dist) {
                        sonarReports.remove(priorReport);
                        oldReportRemoved = true;
                    }
                }
            }
            if (!oldReportFound || oldReportRemoved) {
                sonarReports.add(new SonarReport(shipDistance, ship));
            }
        }
    }

    /**
     * Returns a distance for sonar
     *
     * @param targetX The x coordinate of the sonar ping
     * @param targetY The y coordinate of the sonar ping
     * @param x       The current x coordinate
     * @param y       The current y coordinate
     * @return The distance from the sonar coordinate to the current coordinate
     */
    private static int sonarDistance(int targetX, int targetY, int x, int y) {
        return Math.max(Math.abs(targetX - x), Math.abs(targetY - y));
    }
}
