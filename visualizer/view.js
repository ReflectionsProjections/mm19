

var CANVAS_HEIGHT = Math.min(window.innerWidth / 2, window.innerHeight - 80);
var MARGIN = 3;
var GAME_HEIGHT = CANVAS_HEIGHT - MARGIN * 2;

/*
    Holds the render state, and functions that render the game
*/
var view = {

    render_state : {
        paper1 : Raphael("paper1", CANVAS_HEIGHT, CANVAS_HEIGHT),
        paper2 : Raphael("paper2", CANVAS_HEIGHT, CANVAS_HEIGHT)
    },

    /*
        Initializes user interface by showing and hiding what needs to be shown and hidden.
    */
    init_ui : function() {
        util.hide_class("play");
        document.getElementById("controls").style.visibility = "visible";
        document.getElementById("board").style.visibility = "visible";
        view.render_state.paper1.canvas.style.backgroundColor = "#00CED1";
        view.render_state.paper2.canvas.style.backgroundColor = "#00CED1";
        var round = util.get_url_paramater("round");
        if (round !== null) {
            document.getElementById("round_info").innerHTML = "Round " + round;
        }
    },

    set_turn : function(turn_number) {
        document.getElementById("turn_number").value = "" + turn_number;
        document.getElementById("turn_info").innerHTML = "Turn " + turn_number;
    },

    /*
        takes a render_state and game_state and renders game_state and returns new render_state
    */
    render_game : function () {
        var draw_ships = function(ships, paper) {
            util.map_array(function(ship) {
                var box_width = GAME_HEIGHT / controller.size;

                //width and height of ship in pixels
                var width = box_width * model.ship_lib[ship.type].width;
                var height = box_width;

                var x = MARGIN + ship.x * box_width;
                var y = MARGIN + ship.y * box_width;

                if (ship.orientation === "V") {
                    //rotate
                }
                var ship_status = Math.min(Math.floor((ship.health / model.ship_lib[ship.type].health) * 3) + 1, 3);

                var src = "images/" + ship.type.toLocaleLowerCase() + ship_status + ".svg";
                console.log(src);

                var image = paper.image(src, x, y, width, height);
            },
            ships);
        };

        //set resources
        document.getElementById("p1_resources").innerHTML = model.game_state[0].resources;
        document.getElementById("p2_resources").innerHTML = model.game_state[1].resources;
        //clear shapes
        this.render_state.paper1.clear();
        this.render_state.paper2.clear();
        //draw new shapes
        draw_ships(model.game_state[0].ships, this.render_state.paper1);
        draw_ships(model.game_state[1].ships, this.render_state.paper2);
    }
};


