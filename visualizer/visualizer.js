

var controller = {

    /*
        Inititilizes UI.
    */
    init : function () {
        // Check for the various File API support.
        if (!(window.File && window.FileReader && window.FileList && window.Blob)) {
            alert('The File APIs are not fully supported in this browser.');
            return;
        }

        /**
        * File loading handler for loading JSON
        */
        var file_select_handler = function(evt) {
            var file = evt.target.files[0]; // File object

            var reader = new FileReader();

            // Closure to capture the file information.
            reader.onload = function(e) {
                controller.log = e.target.result.split("\n");
                controller.frame = 1;
                controller.pause = true;

                try {
                    var setup = util.map_array(json_parse, controller.log.slice(0, 3));
                } catch (error) {
                    console.log(error.message);
                    console.log("One of the first three lines are invalid JSON.  Did you upload the right file?");
                    return;
                }

                document.getElementById("p1_name").innerHTML = setup[1].playerName;
                document.getElementById("p2_name").innerHTML = setup[2].playerName;

                controller.game_state = controller.new_game_state(setup);
                view.render_game(view.render_state, controller.game_state);
                view.init_ui();
            };

            // Read in the json file as a string.
            reader.readAsText(file);
        };

        //cannot interact with anything on the page until this statement
        document.getElementById('file_select').addEventListener('change', file_select_handler, false);
    },

    /*
        Takes board setup and returns new game state.
    */
    new_game_state : function(board_setup) {
        controller.size = board_setup[0].size;

        return controller.run_tick(controller.run_tick([], board_setup[1], 0), board_setup[2], 1);
    },

    /*
        Takes a game_state and turn and returns a new game_state with turn applied to it.
    */
    run_tick : function (game_state, turn, ind) {
        if (ind === undefined) {
            var ind = game_state[0].player_name === turn.playerName ? 0 : 1;
        }

        var convert_ship = function(ship) {
            return {
                health : ship.health,
                type : ship.type,
                x : ship.xCoord,
                y : ship.yCoord,
                orientation : ship.orientation
            };
        };

        game_state[ind] = {
            player_name : turn.playerName,
            resources : turn.resources,
            ships : util.map_array(convert_ship, turn.ships),
            actions : []
        }

        return game_state;
    },

    game_state : [],

    pause : true,

    frame : 1,

    log : [],

    size: 0,

    ship_lib : {
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
    },

    buttons : {
        play : function() {
            if (controller.frame >= controller.log.length) {
                controller.buttons.pause();
                console.log("Reached end of game");
                return;
            }

            document.getElementById("turn_number").innerHTML = "Turn " + controller.frame;

            controller.pause = false;

            var turn;
            var complete = false;
            while (!complete) {
                try {
                    turn = json_parse(controller.log[controller.frame]);
                    complete = true;
                } catch (error) {
                    console.log("Turn " + controller.frame + " inproperly formatted.  Skipping to next turn...");
                }
                controller.frame += 1;
            }

            // apply tick to game state
            controller.game_state = controller.run_tick(controller.game_state, turn);
            // apply that to render state
            view.render_state = view.render_game(view.render_state, controller.game_state);
            var timeout = parseInt(document.getElementById("tick_rate").value);

            if (timeout < 1) {
                timeout = 1;
            } else if (timeout > 99999) {
                timeout = 99999;
            }

            //timeout
            window.setTimeout(controller.buttons.play, timeout);
        },

        pause : function() {
            controller.pause = true;
        },

        increase_sleep : function() {
            document.getElementById("tick_rate").value = parseInt(document.getElementById("tick_rate").value) + 10;
        },

        decrease_sleep : function() {
            document.getElementById("tick_rate").value = parseInt(document.getElementById("tick_rate").value) - 10;
        },

        step_forward : function() {

        },

        step_backward : function() {

        }
    }
};

window.onload = controller.init;


