function WIPCController(webkey, webkeyApi) {
    this.API = webkeyApi;
    this.APP = webkey;
    this.init();
}

WIPCController.prototype = {
    API: void 0,

    transparent: true,

    viewEnabled: false,

    sessionKey: void 0,

    hidden: false,

    init: function() {
        var self = this;
        self.hideView();
        $("#wipcFix").on("click", function($event) {
            if(!$($event.currentTarget).hasClass('unclickable')) {
                //force the download
                var link = document.createElement('a');
                link.href = PATH_STARTSH;
                link.download = 'start.sh';
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                self.hideView();
            }
        });

        $("#wipcCancel").on("click", function($event) {
            if(!$($event.currentTarget).hasClass('unclickable')) {
                self.hideView();
            }
        });

        $("#connectedStatusIndicator").on("click", function() {
            if(self.APP.isPanelStateLoggedIn()) {
                self.toggleView();
            }
        });
    },

    onMessage: function(msg) {
        switch (msg.type) {
            case "BACKENDHALTED":
                this.backendHalted();
                break;
            case "SESSIONKEY":
                this.sessionKey = msg.payload;
                this.setSessionKey();
                break;
        }
    },

    onError: function(err) {
    },

    setSessionKey: function() {
        $("#wipcSessionKey").text(this.sessionKey);
    },

    backendHalted: function() {
        if(!this.hidden) {
            this.hidden = true;
            this.showView();
        }
    },

    toggleView: function() {
        var self = this;
        if($("#wipcBox").hasClass("showView")) {
            self.hideView();
        } else {
            self.showView();
        }
    },

    showView: function() {
        $("#wipcBox").show();
        setTimeout(function() {
            $("#wipcBox").addClass("showView");
            $(".wipcSetter").each(function() {
                $( this ).removeClass("unclickable");
            });
        }, 100);
    },

    hideView: function() {
        setTimeout(function() {
            $("#wipcBox").hide();
        }, 300);
        $("#wipcBox").removeClass("showView");
        $(".wipcSetter").each(function() {
            $( this ).addClass("unclickable");
        });
    }
};
