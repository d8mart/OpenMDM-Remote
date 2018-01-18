function SpinnerController() {
    this.init();
}

SpinnerController.prototype = {
    API: void 0,

    init: function() {
        var self = this;
        return this;
    },

    spin: function() {
        var self = this;
        $("#webkeySpinner").show();
        return this;
    },

    stop: function() {
        $("#webkeySpinner").hide();
        return this;
    }
};

