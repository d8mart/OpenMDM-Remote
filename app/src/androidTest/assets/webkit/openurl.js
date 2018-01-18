function OpenURLController(webkey, webkeyApi) {
    this.API = webkeyApi;
    this.APP = webkey;
    this.init();
}

OpenURLController.prototype = {
    API: void 0,

    transparent: true,

    viewEnabled: false,

    url: "http://webkey.cc",

    init: function() {
        var self = this;
    },

    showView: function() {
        var self = this;
        $("#openurlContainer").show();
        $("#inputUrl").val(this.url);

        this.viewEnabled = true;
        $("#urlForm").on("submit", function(e) {
            e.preventDefault();
            this.url = $("#inputUrl").val();
            self.API.openURL(this.url);
            self.hideView();
        });
    },

    hideView: function() {
        $("#urlForm").off("submit");
        $("#openurlContainer").hide();

        // reenable phone buttons
        if(this.viewEnabled) {
            this.viewEnabled = false;
        }
    }
};
