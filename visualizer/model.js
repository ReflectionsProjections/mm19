
/*
	Holds game state and functions that mutate game state
*/
var model = {
	game_state : [],

    /*
        Takes board setup and returns new game state.
    */
    new_game_state : function(board_setup) {
        controller.size = board_setup[0].size;

        this.perform_turn(board_setup[1], 0);
        this.perform_turn(board_setup[2], 1);
    },

    /*
        Takes a game_state and turn and returns a new game_state with turn applied to it.
    */
	perform_turn : function(turn, ind) {
		if (ind === undefined) {
            var ind = this.game_state[0].player_name === turn.playerName ? 0 : 1;
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

        this.game_state[ind] = {
            player_name : turn.playerName,
            resources : turn.resources,
            ships : util.map_array(convert_ship, turn.ships),
            actions : []
        };
	}
};

