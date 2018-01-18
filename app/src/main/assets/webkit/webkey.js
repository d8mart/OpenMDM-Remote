function WebkeyApp() {}
WebkeyApp.prototype = {
    views: {},

    API: void 0,

    currentView: "",

    loggedInPanelState: false,

    androidButtonsEnabled: false,

    spinner: void 0,

    init: function() {
        var self = this;
        self.changeIndicatorStatus("connecting");
        this.API = new WebkeyApi();
        self.spinner = new SpinnerController().spin();

        var proto = ('https:' == document.location.protocol ? 'wss://' : 'ws://');
        this.API.setUrl([proto + window.location.hostname + WS_PORT]);

        this.addView("login", new LoginController(this, this.API));
        this.addView("signup", new SignupController(this));
        this.addView("phone", new PhoneController(this, this.API));
        this.addView("openurl", new OpenURLController(this, this.API));
        this.addView("location", new LocationController(this.API));
        this.addView("wipc", new WIPCController(this, this.API));

        this.views["phone"].showView();
        this.setNavbarState("LOGGED_OUT");
        this.setPanelState("LOGGED_OUT");

        this.API.addHandler("AUTH", this.getView("login"));
        this.API.addHandler("SIGNUP", this.getView("signup"));

        this.API.addHandler("ARRAYBUFFER", this.getView("phone"));
        this.API.addHandler("SCREEN_OPTIONS", this.getView("phone"));
        this.API.addHandler("CONNECTED", this.getView("phone"));
        this.API.addHandler("CONNECTED", this.getView("login"));

        this.API.addHandler("LOCATION", this.getView("location"));

        this.API.addHandler("BACKENDHALTED", this.getView("wipc"));
        this.API.addHandler("SESSIONKEY", this.getView("wipc"));

        this.API.addHandler("CONNECTED", {
            onMessage: function(data) {
                self.changeIndicatorStatus("connected");
            }
        });

        this.API.addHandler("DISCONNECTED", {
            onMessage: function(data) {
                self.changeIndicatorStatus("disconnected");
                self.showSpinner();
            }
        });

        this.API.addHandler("RECONNECTING", {
            onMessage: function(data) {
                self.changeIndicatorStatus("connecting");
                self.showSpinner();
            }
        });

        this.API.addHandler("RESTART", {
            onMessage: function(data) {
                location.reload();
            }
        });

        this.API.addHandler("TOAST", {
            onMessage: function(data) {
                if (self.currentView != "login" && self.currentView != "signup") {
                    var opts = {
                        text: data.jsonPayload.text,
                        sticky: data.jsonPayload.sticky,
                        position: "middle-right"
                    }

                    switch (data.jsonPayload.type) {
                        case "INFO":
                            opts.type = "notice";
                            break;
                        case "WARNING":
                            opts.type = "warning";
                            break;
                        case "ERROR":
                            opts.type = "error";
                            break;
                    }
                    $.fn.toastmessage('showToast', opts);
                } else {
                    if (data.jsonPayload.type == "ERROR")
                        self.getView(self.currentView).onError(data.jsonPayload.text);
                }
            }
        });

        //menu valtas
        $(".navbar li").on("click", function() {
            $(".navbar li").removeClass("active");
            $(this).addClass("active");
            self.showView(this.id.substr(0, this.id.length - 3));
        });

        this.initAnimatedInputWrapperStyle();
    },

    initAnimatedInputWrapperStyle: function() {
        $('input').each(function() {
            $(this).on('focus', function() {
                $(this).parent('.animatedInput').addClass('active');
            });
            $(this).on('blur', function() {
                if ($(this).val().length == 0) {
                    $(this).parent('.animatedInput').removeClass('active');
                }
            });
            if ($(this).val() != '') $(this).parent('.animatedInput').addClass('active');
        });
    },

    setAndroidButtons: function() {
        //Chrome buttons to Java buttons
        var self = this;
        var androidKeys = {
            "40": "40", //"ARROW_DOWN",
            "39": "39", //"ARROW_RIGHT",
            "38": "38", //"ARROW_UP",
            "37": "37", //"ARROW_LEFT",
            "17": "17", //"CTRL",
            "18": "18", //"ALT",
            "13": "13", //"ENTER",
            "16": "16", //"SHIFT",
            "8": "8" //"BACKSPACE",
        };

        //Buttons
        $(document).on('keydown', function(e) {
            if (e.which in androidKeys) {
                e.preventDefault();
                //UP a backend miatt
                self.API.sendButtonEvent("UP", androidKeys[e.which]);
            }
            //Keys
        }).on('keypress', function(e) {
            self.API.sendKeyEvent("PRESS", String.fromCharCode(e.which));
        });
    },

    disableAndroidButtons: function() {
        // disable phone buttons
        $(document).off('keydown');
        $(document).off('keypress');
    },

    getView: function(name) {
        return this.views[name];
    },

    showView: function(name) {
        // enable or disable android buttons.
        // only needed when phone view is in the foreground.
        if(name=="phone") {
            if(!this.androidButtonsEnabled) {
                this.androidButtonsEnabled = true;
                this.setAndroidButtons();
            }
        } else {
            if(this.androidButtonsEnabled) {
                this.androidButtonsEnabled = false;
                this.disableAndroidButtons();
            }
        }

        if(this.views[name].transparent) {
            this.currentView = name;
            this.views[name].showView();
            return;
        }

        for (var k in this.views) {
            if (k === name) {
                if (this.currentView != name) {
                    this.currentView = name;
                    this.views[k].showView();
                }
            } else this.views[k].hideView();
        }
    },

    changeIndicatorStatus: function(state) {
        var statuses = ['connected', 'connecting', 'disconnected'];
        for(var i in statuses) {
            if(statuses[i] == state) {
                $('#'+statuses[i]+'StatusIndicator').show();
            } else {
                $('#'+statuses[i]+'StatusIndicator').hide();
            }
        }
    },

    showOneView: function(name) {
        this.views[name].showView();
    },

    hideOneView: function(name) {
        this.views[name].hideView();
    },

    addView: function(name, view) {
        this.views[name] = view;
    },

    showSpinner: function() {
        this.spinner.spin();
    },

    hideSpinner: function() {
        this.spinner.stop();
    },

    isPanelStateLoggedIn: function() {
        return this.loggedInPanelState;
    },

    setPanelState: function(state) {
        switch (state) {
            case "LOGGED_IN":
                $("#connectedStatusIndicator").removeClass('notClickable');
                $("#qualitybtn").removeClass('notClickable');
                this.loggedInPanelState = true;
                break;
            case "LOGGED_OUT":
                $("#connectedStatusIndicator").addClass('notClickable');
                $("#qualitybtn").addClass('notClickable');
                this.loggedInPanelState = false;
                break;
        }
    },

    setNavbarState: function(state) {
        switch (state) {
            case "LOGGED_IN":
                $("#sidebarContainer").show();
                break;
            case "LOGGED_OUT":
                $("#sidebarContainer").hide();
                break;
        }
    }
};
