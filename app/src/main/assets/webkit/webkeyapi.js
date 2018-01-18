function MockWebSocket(url) {
    var self = this;
    console.log("using MockWebSocket: " + url);
    setTimeout(function() {
        self.onopen();
    }, 500);

    this.send = function(msg) {
        if (msg.type != 'PING')
            self.onmessage({
                data: msg
            });
    }
}

function WebkeyApi() {}

WebkeyApi.prototype = {
    websocket: void 0,

    debug: false,

    handlers: {},

    pingLoop: void 0,

    // the chosen url to use for connection
    url: void 0,

    init: function(cb) {
        var self = this;
        var ping = function() {
            self.send({
                id: "1",
                type: "PING"
            })
            if (self.websocket.readyState == 0 || self.websocket.readyState == 1) {
                self.pingLoop = setTimeout(ping, 1000);
            } else {
                console.log("stopping ping because ws readystate changed to :" + self.websocket.readyState);
            }
        };

        this.websocket = new WebSocket(this.url);
        this.websocket.binaryType = "arraybuffer";

        this.websocket.onmessage = this.receive();
        this.websocket.onopen = function() {
            self.reconnecting = false;
            clearTimeout(self.pingLoop);
            self.pingLoop = setTimeout(ping, 1000);
            cb();
            self.notifyHandlers("CONNECTED", {
                type: "CONNECTED"
            })
        };

        this.websocket.onclose = function() {
            if (!self.reconnecting) {
                self.notifyHandlers("DISCONNECTED", {
                    type: "DISCONNECTED"
                })
            }


            var reconnect = function() {
                if (self.reconnecting) {
                    self.init(function() {
                        if (self.currentAuth != void 0) {
                            self.send(self.currentAuth);
                        }
                    })

                    if (self.websocket.readyState != 0 || self.websocket.readyState != 1) {
                        setTimeout(reconnect, 3000);
                        console.log("reconnecting..");
                        self.notifyHandlers("RECONNECTING", {
                            type: "RECONNECTING"
                        })
                    }
                }
            }

            if (!self.reconnecting) {
                self.reconnecting = true;
                console.log("started reconnect loop");
                setTimeout(reconnect, 3000);
            }
        };

        this.websocket.onerror = function() {
            if (!self.reconnecting) {
                self.notifyHandlers("DISCONNECTED", {
                    type: "DISCONNECTED"
                })
            }
        };
    },

    // the current auth data
    currentAuth: void 0,

    // wether reconnect loop is already running or not
    reconnecting: false,

    // set url to connect to
    setUrl: function(url) {
        var self = this;
        // close down previous connection
        if (self.websocket != void 0) {
            self.close();
        }

        // init with new url
        self.url = url;
        self.init(function() {
            if (self.currentAuth != void 0) {
                self.send(self.currentAuth);
            }
        })
    },

    close: function() {
        if (this.websocket != void 0) {
            this.closed = true;
            // clear onclose function so reconnect loop and related things wont start
            this.websocket.onclose = function() {};
            this.websocket.close();
        }
    },

    send: function(msg) {
        if (msg.type == "AUTH") {
            this.currentAuth = JSON.parse(JSON.stringify(msg));
        }

        if (msg.payload != void 0) msg.payload = JSON.stringify(msg.payload);
        msg = JSON.stringify(msg);

        if (this.websocket != void 0 && this.websocket.readyState == 1) {
            this.websocket.send(msg);
        }
    },

    // need to wrap this fn, otherwise 'this' would point to the websocket instance
    receive: function() {
        var self = this;
        return function(evt) {
            if (evt.data instanceof ArrayBuffer) {
                self.notifyHandlers("ARRAYBUFFER", evt.data);
                return;
            }

            var msg = JSON.parse(evt.data.substr(0, evt.data.lastIndexOf("}") + 1));
            if (self.debug) {
                console.log("receive: " + JSON.stringify(msg));
            }
            if (msg.type == "ERROR") {
                self.broadcastError(msg)
            }

            self.notifyHandlers(msg.type, msg);

        }
    },


    broadcastError: function(msg) {
        for (var key in this.handlers["TOAST"]) {
            this.handlers["TOAST"][key].onMessage({
                jsonPayload: {
                    type: "ERROR",
                    text: msg.payload
                }
            });
        }
    },

    notifyHandlers: function(key, data) {
        for (var k in this.handlers[key]) {
            this.handlers[key][k].onMessage(data);
        }
    },

    addHandler: function(key, handler) {
        if (this.handlers[key] == void 0) this.handlers[key] = [];
        this.handlers[key].push(handler);
    },

    // webkey API methods

    sendAuth: function(data) {
        this.send({
            id: "1",
            type: "AUTH",
            payload: data
        });
    },

    sendAdminToken: function(data) {
        this.send({
            id: "1",
            type: "ADMINAUTH",
            payload: data
        });
    },


    sendSignUp: function(data) {
        this.send({
            id: "1",
            type: "SIGNUP",
            payload: data
        });
    },

    sendOpts: function(data) {
        this.send({
            id: "1",
            type: "SCREEN_OPTIONS",
            payload: data
        });
    },

    sendAck: function(seqnum, utime) {
        this.send({
            id: "1",
            type: "SCREEN_ACK",
            payload: {
                sequenceNumber: seqnum,
                timestamp: utime
            }
        });
    },

    sendKeyEvent: function(event, keycode) {
        this.send({
            id: "1",
            type: "KEY",
            payload: {
                type: event,
                code: keycode
            }
        });
    },

    sendButtonEvent: function(type, code) {
        this.send({
            id: "1",
            type: "BUTTON",
            payload: {
                type: type,
                code: code,
            }
        });
    },

    sendTouchEvents: function(events) {
        this.send({
            id: "1",
            type: "TOUCH",
            payload: {
                events: events
            }
        });
    },

    startStream: function() {
        this.send({
            id: "1",
            type: "SCREEN_START",
            payload: "start"
        });
    },

    stopStream: function() {
        this.send({
            id: "1",
            type: "SCREEN_STOP",
            payload: "stop"
        });
    },

    openURL: function(link) {
        this.send({
            id: "1",
            type: "OPENURL",
            payload: {
                url: link,
            }
        });
    },

    startLocation: function() {
        this.send({
            id: "1",
            type: "LOCATION_START"
        });
    },

    stopLocation: function() {
        this.send({
            id: "1",
            type: "LOCATION_STOP"
        });
    },
};
