package mm19.game.board;

/**
 * @author mm19
 *
 * An immutable data object for positions
 */
public class Position {
    // Enum for orientation
    public static enum Orientation {
        HORIZONTAL, VERTICAL
    }

    final public int x;
    final public int y;
    final public Orientation orientation;

    /**
     * Constructor for positions.
     *
     * @param x           X Coordinate
     * @param y           Y Coordinate
     * @param orientation See Orientation enum for possible values
     */
    public Position(int x, int y, Orientation orientation) {
        this.x = x;
        this.y = y;
        this.orientation = orientation;
    }
}
