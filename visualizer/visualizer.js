
// Check for the various File API support.
if (window.File && window.FileReader && window.FileList && window.Blob) {
  // Great success! All the File APIs are supported.
} else {
  alert('The File APIs are not fully supported in this browser.');
}



//global object for currentLog being simulated
var currentLog = {};

//global object holding turns to be processed
var globalTurns = [];

//constant for length between animation frames in milliseconds
var TICK_LENGTH = 1000;

//global raphael canvas object
var canvas;

/**
* setups a game to be visualized and sets initial timer interrupt for displaying turns
* 
* This is called when you hit the Run Game button.
*/
function visualizeGame() {
    //make local so doesnt get overwritten
    var log = currentLog;

    globalTurns = copyTurns(log.turns);

    if (canvas) {
        canvas.clear();
        canvas.remove();
    }
    
    /*
    This is what display looks like
    var display = {
        "browser" : {
            //browser width
            "width" : 0,
            //horizontal margin for canvas
            "horizontalMargin" : 0,
            //vertical margin for canvas
            "verticalMargin" : 0,
            //width of a single team's board
            "boardWidth" : 0,
            //height of a single team's board
            "boardHeight" : 0,
            //width of a tile
            "boxWidth" : 0
        },
        "canvasElements" : {
            //keys are dynamically assigned by player name
            "Roger" : {
                //xOffset for board
                "x" : 0,
                //paper element displaying resources string
                "resources" : 0,
                "ships" : [
                    {
                        //paper element displaying ship
                        "ship" : 0, 
                        //paper element displaying health
                        "health" : 0, 
                        //paper element displaying lost health
                        "neg" : 0
                    },
                    {},
                    ...
                ]
            },
            "Paul" : {
                "x" : 0,
                "resources" : 0,
                "ships" : [
                    {},
                    {},
                    ...
                ]
            }
        }
    };
    */

    var display = setupCanvas(log);

    window.setTimeout(drawFrame, TICK_LENGTH, display);
};

/**
* Make a shallow copy of the turns array so popping from the queue doesn't pop it from the global log.
* This way you can hit run button multiple times on the same json and start at the beginning of the game
* @param turns Array of turns to be copied
* @return Copied array of turns
*/
function copyTurns(turns) {
    var turnsClone = [];
    var ind;
    for (ind = 0; ind < turns.length; ind++) {
        turnsClone[ind] = turns[ind];
    }
    return turnsClone;
};

/**
* Draws a single turn, or frame in the animation of a game.
* @param display object defined in visualizeGame and initialized in setupCanvas
*/
function drawFrame(display) {
    if (globalTurns.length === 0) {
        return;
    }
    var turn = globalTurns.shift();
    var player = turn.playerName;

    //update their resources
    display.canvasElements[player].resources.attr({"text" : String(turn.resources)});

    turn.ships.sort(shipComparator);

    var ind;
    var ship;
    for (ind = 0; ind < turn.ships.length; ind++) {
        //remove old shapes from paper
        shipShapes = display.canvasElements[player].ships[ind];
        shipShapes.ship.remove();
        shipShapes.health.remove();
        shipShapes.neg.remove();

        display.canvasElements[player].ships[ind] =
            drawShip(
                turn.ships[ind],
                display.browser.boxWidth,
                display.canvasElements[player].x);
    }

    //ask for callback
    window.setTimeout(drawFrame, TICK_LENGTH, display);
};

/**
* Returns a ship object of raphael elements which looks like:
* {
*       "ship" : an ellipse representing the ship
*       "health" : a green rectangle representing the remaining health
*       "neg" : a red rectangle representing lost health
* }
*
* @param ship     ship object given in the log
* @param boxWidth width of a tile in pixels
* @param xOffset  horizontal offset of players board
* @return ship object as defined above
*/
function drawShip(ship, boxWidth, xOffset) {
    var shipLib = {
        "M" : {
            "width" : 5,
            "health" : 60
        },
        "D" : {
            "width" : 4,
            "health" : 40
        },
        "P" : {
            "width" : 2,
            "health" : 20
        }
    };

    var elements = {};
    var width = boxWidth * shipLib[ship.type].width;
    var height = boxWidth;
    //if vertical, swap width and height
    if (ship.orientation == "V") {
        height = width;
        width = boxWidth;
    }

    //width and height are changed to express radii of ellipse
    width = Math.floor(width/2);
    height = Math.floor(height/2);

    var centerX = ship.xCoord * boxWidth + width + xOffset;
    var centerY = ship.yCoord * boxWidth + height;

    //adds ship
    elements.ship = canvas.ellipse(centerX, centerY, width, height);
    //adds red bar, underneath green bar
    elements.neg = canvas.rect(centerX - width,
                              centerY - height - 8,
                              boxWidth * Math.floor(shipLib[ship.type].health / 10),
                              5);
    elements.neg.attr({"fill" : "#FF0000"});
    //adds green bar for shp health
    elements.health = canvas.rect(centerX - width,
                                 centerY - height - 8,
                                 boxWidth * Math.floor(ship.health/10),
                                 5);
    elements.health.attr({"fill" : "#00FF00"});

    return elements;
}

/**
* Compare function used to sort ships by ID.
* Only used as a functor for sort function.
*/
function shipComparator(a, b) {
    return a.ID - b.ID;
}

/**
* Setup canvas and display object for drawing a game.  Draws the initial state of game as well.
* @return display object defined in visualizeGame
*/
function setupCanvas() {
    var display = {};

    display.browser = setupBrowserSettings();

    canvas = Raphael(display.browser.horizontalMargin,
                     display.browser.verticalMargin,
                     display.browser.boardWidth * 2,
                     display.browser.boardHeight + 100);

    display.canvasElements = {};

    var turn1 = globalTurns.shift();
    display.canvasElements[turn1.playerName] = initializePlayer(display, turn1, 0);

    var turn2 = globalTurns.shift();
    display.canvasElements[turn2.playerName] = initializePlayer(display, turn2, display.browser.boardWidth);


    return display;
};

/**
* Setups various browser settings based on the width of the window.
* @return An object representing browser settings, defined in display object in visualizeGame
*/
function setupBrowserSettings() {
    var browser = {};
    browser.width = window.innerWidth;
    browser.height = window.innerHeight;
    browser.horizontalMargin = 60;
    browser.verticalMargin = 60;
    browser.boardWidth = Math.min(Math.floor((browser.width - 4 * browser.horizontalMargin)/2),
                                  browser.height - 4 * browser.verticalMargin);
    var config = currentLog.boardConfiguration;
    browser.boardHeight = browser.boardWidth;
    browser.boxWidth = Math.floor(browser.boardWidth / config.width);

    return browser;

};

/**
* Initializes a singe player's data structures and draws their initial board setup
* @param display display object defined in visualizeGame
* @param turn object given by log representing player's first turn
* @param xOffset horizontal offset for player
*/
function initializePlayer(display, turn, xOffset) {
    //sort ships by ID
    turn.ships.sort(shipComparator);

    //draw player name
    canvas.text(50 + xOffset, 5, turn.playerName);

    //draw rectangle
    canvas.rect(xOffset, 15, display.browser.boardWidth, display.browser.boardHeight + 10);

    //intitialize canvasElements
    player = {"ships" : []};
    player.x = xOffset;

    //draw resource amounts
    player.resources = canvas.text(50 + xOffset, display.browser.boardHeight + 40, String(turn.resources));

    //draw ships
    var ind;
    for (ind = 0; ind < turn.ships.length; ind += 1) {
        player.ships.push(drawShip(turn.ships[ind], display.browser.boxWidth, xOffset));
    }

    return player;
};

/**
* File loading handler for loading JSON
*/
function handleFileSelect(evt) {
    var file = evt.target.files[0]; // File object

    var reader = new FileReader();

    // Closure to capture the file information.
    reader.onload = (function(theFile) {
        return function(e) {
            try {
                currentLog = json_parse(e.target.result);
                //enable run visualization button
                document.getElementById("runLogButton").removeAttribute("disabled");
            } catch (error) {
                alert("Invalid JSON");
            }

        };
    })(file);

    // Read in the json file as a string.
    reader.readAsText(file);

};

//cannot interact with anything on the page until this statement
document.getElementById('jsonInput').addEventListener('change', handleFileSelect, false);




