function LoginController(webkey, webkeyApi) {
    this.APP = webkey;
    this.API = webkeyApi;
    this.init();
}

LoginController.prototype = {
    transparent: true,

    hasCredentials: false,

    adminLoginCookie: "admin-token",

    init: function() {
        var self = this;

        $("#loginForm").on("submit", function(e) {
            e.preventDefault();
            var auth = {
                username: $("#inputUsername").val(),
                password: $("#inputPassword").val(),
                browseragent: navigator.userAgent,
                platform: navigator.platform
            };

            $("#loginContainer").children().hide()

            self.APP.API.sendAuth(auth);
            self.hasCredentials = true;
        });

        $("#signupNav").on("click", function(e) {
            e.preventDefault();
            self.APP.hideOneView("login");
            self.APP.showView("signup");
        });
    },

    onMessage: function(msg) {
        var self = this;
        switch (msg.type) {
            case "CONNECTED":
                if(self.getAdminCookie() != null) {
                    self.sendAdminLogin();
                    return;
                }

                if(!this.hasCredentials) {
                    this.showView();
                }
                break;

            case "AUTH":
                if(msg.jsonPayload && msg.jsonPayload.error) {
                    $("#loginContainer").show();
                    this.onError(msg.jsonPayload.errorMsg)
                    return;
                } else {
                    $("input").val("");
                    if(msg.jsonPayload) {
                        $("#deviceNickName").text(msg.jsonPayload.deviceNick);
                    }

                    this.APP.setNavbarState("LOGGED_IN");
                    this.APP.setPanelState("LOGGED_IN");
                    this.APP.showView("phone");
                }
            break;
        }
    },

    onError: function(err) {
        this.APP.hideSpinner();
        $("#loginContainer").children().show()
        $(".webkey-error").text(err).show();
    },

    showView: function() {
        this.APP.showSpinner();
        $("#deviceNickName").text("");
        $(".webkey-error").hide();
        $("#loginContainer").show();
    },

    hideView: function() {
        this.APP.hideSpinner();
        $("#loginContainer").children().show()
        $(".webkey-error").hide();
        $("#loginContainer").hide();
    },

    getAdminCookie: function() {
        var c_value = " " + document.cookie;
        var c_start = c_value.indexOf(" " + this.adminLoginCookie + "=");
        if (c_start == -1) {
            c_value = null;
        }
        else {
            c_start = c_value.indexOf("=", c_start) + 1;
            var c_end = c_value.indexOf(";", c_start);
            if (c_end == -1) {
                c_end = c_value.length;
            }
            c_value = unescape(c_value.substring(c_start,c_end));
        }
        return c_value;
    },

    sendAdminLogin: function() {
        var token = this.getAdminCookie();
        var auth = {
            token: token,
            browseragent: navigator.userAgent,
            platform: navigator.platform
        };

        console.log("send admin token");
        this.APP.API.sendAdminToken(auth);
    }
};
