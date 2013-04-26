

var util = {

    show_class : function(class_name) {
        var show = document.getElementsByName(class_name);

        for (var ind = 0; ind < show.length; ind += 1) {
            show[ind].style.visibility = "visible";
        }
    },

    hide_class : function(class_name) {
        var hidden = document.getElementsByName(class_name);

        for (var ind = 0; ind < hidden.length; ind += 1) {
            hidden[ind].style.visibility = "hidden";
        }
    },

    /*
        Maps func over arr to get some functional programming going :P
    */
    map_array : function(func, arr) {
        var ret_val = [];

        for (var ind = 0; ind < arr.length; ind++) {
            ret_val[ind] = func(arr[ind]);
        }

        return ret_val;
    }

};


