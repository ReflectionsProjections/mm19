

var CANVAS_HEIGHT = 400;
var MARGIN = 20;
var GAME_HEIGHT = CANVAS_HEIGHT - MARGIN * 2;


var view = {

    render_state : {
        paper : Raphael("paper", 2 * CANVAS_HEIGHT, CANVAS_HEIGHT),
        shapes : []
    },

	/*
        Initializes user interface by showing and hiding what needs to be shown and hidden.
    */
    init_ui : function() {
        document.getElementById("info_bar").style.visibility = "visible";
        util.hide_class("play");
        document.getElementById("controls").style.visibility = "visible";
    },

    /*
        takes a render_state and game_state and renders game_state and returns new render_state
    */
    render_game : function (render_state, game_state) {
        var draw_ships = function(ships, x_offset) {
            util.map_array(function(ship) {
                var box_width = GAME_HEIGHT / controller.size;

                //width and height of ship in pixels
                var width = box_width * controller.ship_lib[ship.type].width;
                var height = box_width;
                //if vertical, swap width and height
                if (ship.orientation === "V") {
                    height = width;
                    width = box_width;
                }

                //width and height are changed to express radii of ellipse
                width = Math.floor(width/2);
                height = Math.floor(height/2);


                var center_x = ship.x * box_width + MARGIN + x_offset * box_width * controller.size;
                var center_y = ship.y * box_width + MARGIN;

                render_state.shapes.push(render_state.paper.ellipse(center_x, center_y, width, height).attr({"fill" : "#809080"}));
            },
            ships);
        };

        //set resources
        document.getElementById("p1_resources").innerHTML = game_state[0].resources;
        document.getElementById("p2_resources").innerHTML = game_state[1].resources;
        //clear shapes
        render_state.paper.clear();
        //draw new shapes
        draw_ships(game_state[0].ships, 0);
        draw_ships(game_state[1].ships, 1);

        return render_state;
    }
};



