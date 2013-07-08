package mm19;

/**
 * @author mm19
 * 
 * Coding Conventions for MechMania 19
 */
public class CodingConventions {

    /*
     * Use spaces instead of tabs.  Please adjust your IDE to conform to this.
     */

    /*
     * Upgrade to JavaSE-1.7.  Please do not downgrade the project target
     * to 1.6 or older versions.
     */

    /*
     * Do write JavaDocs for classes and functions.  We plan to generate
     * documentation when we figure out a secure way to share it.
     */

    /*
     * Do write unit tests.  You do not need to follow a test driven development
     * methodology, but you should have at least 90% test coverage before you
     * declare a class as "done".
     */

    /*
     * Use a singular, capitalized CamelCase noun when choosing class names.
     * Use camelCase for variable and method names.
     * Method names should convey some sort of action or answer to a question.
     * Constants should be written in all caps with underscores instead of spaces.
     */

    /*
     * The name of an enumeration should be treated as a class and an enumeration's
     * values treated as constants.
     *
     * For example:
     */
    enum Direction {NORTH, EAST, SOUTH, WEST}

    /*
     * Constants should be defined in the most relevant classes.  Only if no class fits
     * may you add it to the Constants class.  If a new class is created, re-evaluate
     * whether or not that the constants defined in the Constants class still belong there.
     */

    /*
     * Avoid god classes (classes that try to do everything) Always ask yourself,
     * "Should this class *really* be the one responsible for this functionality?"
     */

    /*
     * Stick to the project's verbiage, even when just writing comments.
     * i.e. Use "move" instead of "jump" and "ship" instead of "boat"
     */

    /*
     * Keep your functions short.  If they get long, try to make a private helper
     * function and move some of the function's code to the helper.
     */

    /*
     * Avoid commenting the obvious.  Additionally, comments have a tendency to become
     * "out of sync" with your code which can mislead other developers.  To combat this,
     * make "executable comments" by choosing your variable and function names wisely.
     * (a.k.a self-documenting code)
     */

    /*
     * Do not append inline comments to the end of a statement
     */
    public void inlineAppendedCommentsExample() {

        x++; //This is an inline appended comment, avoid this

        //Instead inline comments should be placed before the line being commented on
        x++;
    }

    /*
     * Choose concise, meaningful variable names, but avoid abbreviations.
     *
     * Same goes for function names, HOWEVER:
     * With functions, meaningful, longer names trump conciseness if
     * conciseness is too difficult of a realizable goal.
     */
    public void abbreviationExample() {
        //Do not do this
        resGen = 30;

        //Instead type out the full words
        resourcesGenerated = 30;

        //Do not go overboard
        resourcesGeneratedByThisUnitEachTurn = 30;
    }

    /*
     * Never pass primitive data types or magic numbers to a function
     */
    public void namelessVariableExample() {
        //Do not do this
        ship = new Ship(40);

        //This is better, but it is still using a magic number
        int health = 40;
        ship = new Ship(health);

        //Instead of a magic number, use a constant or a generated value
        ship = new Ship(Ship.DEFAULT_HEALTH);
    }

    /*
     * Please use the K&R style of writing code blocks rather than the Allman style.
     * Either version has its merits and demerits and can be debated,
     * but this is what we are going with for this project.
     */
    public void controlBlockExample() {
        //Avoid this
        if (ship.canShoot())
        {
            doSomething();
        }

        //Instead do this
        if (ship.canShoot()) {
            doSomething();
        }
    }

    /*
     * Extract complex math or boolean expressions into functions.
     * This helps to reduce code duplication and increase readability
     */
    public void expressionExample(){
        //Avoid doing this
        if (x >= 0 && x < board.width && y >= 0 && y < board.height ) {
            doSomething();
        }

        //Instead do this
        if (isInBounds(x, y)) {
            doSomething();
        }
    }

    /*
     * Encapsulate all code following a control statement with { and }
     */
    public void blockExample(){
        //Definitely don't do this
        if ( ship.canShoot() )
            return;
        else
            for (int i = 0; i < shipCount; i++)
                doSomething();

        //But don't even do this...
        if (ship.canShoot())
            doSomething();


        //Instead do this
        if ( ship.canShoot() ) {
            return;
        } else {
            for (int i = 0; i < shipCount; i++) {
                doSomething();
            }
        }

        //and this
        if (ship.canShoot()) {
            doSomething();
        }
    }

    /*
     * Do not embed functions in conditionals unless their name asks a question.
     */
    public void embeddingExample() {

        //This is acceptable because the functions both ask about something
        if (isInBounds(x, y) && ship.canShoot()) {
            doSomething();
        }

        //Avoid this
        if ( placeShip(ship, position) ) {
            doSomething();
        }

        //Instead do this
        boolean placementSuccessful = placeShip(ship, position);
        if (placementSuccessful) {
            doSomething();
        }
    }

    /*
     * Use foreach syntax when it is possible to do so
     */
    public void forEachExample() {
        //Don't do this
        for (int i = 0; i < ships.length; i++) {
            ships[i].destroy();
        }

        //Instead do this.
        for (Ship ship : ships) {
            ship.destroy();
        }
    }

















    //####################################################################
    //####################################################################
    //###                                                              ###
    //### The following code exists only to make the code above "work" ###
    //###         without your IDE complaining about syntax            ###
    //###                                                              ###
    //####################################################################
    //####################################################################
    
    @SuppressWarnings("unused")
    private class Ship {
        final static public int DEFAULT_HEALTH = 30;
        public int health;
        Direction direction = Direction.NORTH;

        public boolean canShoot() {
            direction = Direction.EAST;
            return x > 0;
        }

        public void destroy() {
            direction = Direction.SOUTH;
            health = 0;
        }

        public Ship(int health) {
            direction = Direction.WEST;
            this.health = health;
        }

        public Ship() {
        }
    }

    private class Board {
        public int width = 20;
        public int height = 20;
    }

    public int x = 1;
    public int y = 1;
    public int position = 1;
    public Ship ship = new Ship();
    public Ship[] ships = new Ship[30];
    public int resGen = 30;
    public Board board = new Board();
    public int resourcesGenerated = 30;
    public int shipCount = 10;
    public int resourcesGeneratedByThisUnitEachTurn = 30;
    public boolean isInBounds(int a, int b) { return a == b; }
    public boolean placeShip(Ship a, int b) { return (a == null && b == 0); }
    public void doSomething(){x++;}

    public static void main(String[] args) {
        CodingConventions conventions = new CodingConventions();
        conventions.embeddingExample();
        conventions.inlineAppendedCommentsExample();
        conventions.abbreviationExample();
        conventions.namelessVariableExample();
        conventions.forEachExample();
        conventions.blockExample();
        conventions.controlBlockExample();
        conventions.expressionExample();
    }
}
