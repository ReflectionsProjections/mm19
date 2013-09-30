

var CANVAS_HEIGHT = 440;
var MARGIN = 20;
var GAME_HEIGHT = CANVAS_HEIGHT - MARGIN * 2;

/*
	Holds the render state, and functions that render the game
*/
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
        document.getElementById("paper").style.visibility = "visible";
    },

    /*
        takes a render_state and game_state and renders game_state and returns new render_state
    */
    render_game : function () {
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


                var center_x = MARGIN + (x_offset + 1) * box_width * controller.size - (ship.x * box_width);
                var center_y = MARGIN + ship.y * box_width;

                var image = view.render_state.paper.ellipse(center_x, center_y, width, height).attr({"fill" : "#809080"});

                view.render_state.shapes.push(image);
            },
            ships);
        };

        //set resources
        document.getElementById("p1_resources").innerHTML = model.game_state[0].resources;
        document.getElementById("p2_resources").innerHTML = model.game_state[1].resources;
        //clear shapes
        this.render_state.paper.clear();
        this.render_state.shapes = [];
        //draw new shapes
        draw_ships(model.game_state[0].ships, 0);
        draw_ships(model.game_state[1].ships, 1);
    }
};



