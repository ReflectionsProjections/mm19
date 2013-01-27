package mm19.game;

/**
 * @author mm19
 *
 * Constants for the game.  Only place Constants here if it doesn't make sense to add
 * them to any other class
 */
public class Constants {
    final public static int MISSILE_DAMAGE = 10;

    final private static double BURST_SHOT_EFFECTIVENESS = 0.7;
    final public static int BURST_SHOT_DAMAGE = (int)(MISSILE_DAMAGE * BURST_SHOT_EFFECTIVENESS);
    final public static int BURST_SHOT_COST = 250;
    final public static int BURST_SHOT_RADIUS = 2; //3x3 area

    final public static int MOVE_COST_PER_UNIT_LENGTH = 50;

    final public static int SONAR_COST = 110;
    final public static int SONAR_RADIUS = 3; //5x5 area
}
