

Rudimentary visualizer for mm19.

Log files are loaded by the browser.

An example log can be found in test_log.json.
The main idea is it contatins the board configuration and an array of turns, the same turns the server sends to players.
There must be at least two turns to setup the initial configuration of the board.


LIBRARIES:
    Uses Raphael library, raphael.js, for drawing shapes and interacting with HTML 5 canvas.  May also be used for animations and other cool features.
    Uses json_parse.js to parse the log.

TODO:
    add effects for sonar, shots, and burst shots
    *gracefully* deal with valid JSON this is an invalid log

BUGS:
    if Run Game button is pressed while a game is already running, there are problems with animation






