function once(fn, context) {
    var result;
    return function() {
        if (fn) {
            result = fn.apply(context || this, arguments);
            fn = null;
        }
        return result;
    };
}

function PhoneController(webkey, webkeyApi) {
    this.APP = webkey;
    this.API = webkeyApi;
    this.init();
}

PhoneController.prototype = {
    transparent: false,

    deviceScreenWidth: 500,

    deviceScreenHeight: 900,

    minWidth: 300,

    logoImage: new Image(),

    lastImage: new Image(),

    lastImageTime: void 0,

    alreadyReceivedImage: false,

    quality: 'resAuto',

    drawContext: void 0,

    deviceOpts: {
        rotation: "ROTATION_0"
    },

    canvasOpts: void 0,

    touchEvents: [],

    screenDivOrientation: 0,


    QUALITIES: {
        'resAuto': 70,
        'resHigh': 100,
        'resMedium': 60,
        'resLow': 40,
    },

    init: function() {
        var self = this;
        this.drawContext = document.getElementById('phone-display').getContext('2d');
        this.rotateScreenOnce = once(this.rotateScreen);

        this.lastImage.onload = self.loadScreen();

        var getTouchOpts = function() {
            return {
                flip: false,
                mirror: false
            }
        }

        var createTouchEvent = function(event, element, type) {
            var offset = $(element).offset();
            var coordX = event.pageX - offset.left;
            var coordY = event.pageY - offset.top;
            var percentX;
            var percetnY;

            var width = element.width > 0 ? element.width : element.clientWidth;
            var height = element.height > 0 ? element.height : element.clientHeight;

            switch (self.deviceOpts.rotation) {
                case "ROTATION_0":
                    percentX = coordX / width;
                    percentY = coordY / height;
                    break;
                case "ROTATION_90":
                    percentX = coordX / height;
                    percentY = coordY / width;
                    t = percentX;
                    percentX = 1 - percentY;
                    percentY = t;
                    break;
                case "ROTATION_180":
                    percentX = coordX / width;
                    percentY = coordY / height;
                    percentX = 1 - percentX;
                    percentY = 1 - percentY;
                    break;
                case "ROTATION_270":
                    percentX = coordY / width;
                    percentY = coordX / height;
                    percentY = 1 - percentY;
                    break;
            }

            var opts = getTouchOpts();
            var ts = new Date().getTime() - self.touchEventStart;
            self.touchEvents.push({
                type: type,
                x: percentX,
                y: percentY,
                timestamp: ts,
                flip: opts.flip,
                mirror: opts.mirror
            });
        };

        $('#phone-display').on("mousedown", function(e) {

            createTouchEvent(e, this, "DOWN");

            $('#phone-display').on("mousemove", function(e) {
                createTouchEvent(e, this, "MOVE")
            });

            $('#phone-display').on("mouseout", function(e) {
                createTouchEvent(e, this, "UP")
                $('#phone-display').off('mousemove');
                $('#phone-display').off('mouseout');
                $('#phone-display').off('mouseup');
            });

            $('#phone-display').on("mouseup", function(e) {
                createTouchEvent(e, this, "UP")
                $('#phone-display').off('mousemove');
                $('#phone-display').off('mouseout');
                $('#phone-display').off('mouseup');
            });
        });

        var bindButton = function(selector, code) {
            var start;
            $(selector).on("mousedown", function(e) {
                e.preventDefault();
                start = new Date().getTime();
                self.API.sendButtonEvent("DOWN", code)
            });

            $(selector).on('mouseup', function(e) {
                e.preventDefault();
                if (new Date().getTime() >= (start + 400)) {
                    self.API.sendButtonEvent("LONGPRESS", code)
                } else {
                    self.API.sendButtonEvent("UP", code)
                }
                start = 0;
            });
        }

        //The id come from the java definition
        bindButton("#cellphone-volup-button", "0");
        bindButton("#cellphone-voldown-button", "1");
        bindButton("#cellphone-bot-back-button", "2");
        bindButton("#cellphone-bot-home-button", "3");
        bindButton("#cellphone-bot-menu-button", "4");
        bindButton("#cellphone-power-button", "5");

        var touchSend = function() {
            if (self.touchEvents.length) self.API.sendTouchEvents(self.touchEvents)
            self.touchEvents = [];
            self.touchEventStart = new Date().getTime();
            setTimeout(touchSend, 25);
        };

        touchSend();
        self.hideQualityBox();

        $("#qualitybtn").on("click", function() {
            if(self.APP.isPanelStateLoggedIn()) {
                self.toggleQualityBox();
            }
        });

        $(".qualitySetter").on("click", function($event) {
            if(!$($event.currentTarget).hasClass('unclickable')) {
                var qualityKey = $event.currentTarget.id;
                self.switchToQuality(qualityKey);
                self.quality = qualityKey;
                self.updateQuality();
                setTimeout(function() {
                    self.hideQualityBox();
                }, 100);
            }
        });

        self.updateQualityDesc();

        // ========= TESTING =============
        // var mockDraw = function() {
        //     setTimeout(mockDraw, 1000);
        //     $(".loggedIn").show();
        // };
        // mockDraw();

        // ===============================
    },

    switchToQuality: function(qualityStr) {
        for(var q in this.QUALITIES) {
            if(q == qualityStr) {
                $("#" + q + " .glyphicon").removeClass("notVisible");
            } else {
                $("#" + q + " .glyphicon").addClass("notVisible");
            }
        }
    },

    hideQualityBox: function() {
        setTimeout(function() {
            $("#qualityBox").hide();
        }, 300);
        $("#qualityBox").removeClass("animatedQuality");
        $(".qualitySetter").each(function() {
            $( this ).addClass("unclickable");
        });
    },

    toggleQualityBox: function() {
        if($("#qualityBox").hasClass("animatedQuality")) {
            setTimeout(function() {
                $("#qualityBox").hide();
            }, 300);
            $("#qualityBox").removeClass("animatedQuality");
            $(".qualitySetter").each(function() {
                $( this ).addClass("unclickable");
            });
        } else {
            $("#qualityBox").show();
            setTimeout(function() {
                $("#qualityBox").addClass("animatedQuality");
                $(".qualitySetter").each(function() {
                    $( this ).removeClass("unclickable");
                });
            }, 100);
        }
    },

    onMessage: function(msg) {
        var self = this;

        if (msg instanceof ArrayBuffer) {
            var currTime = new Date().getTime();
            var data = new Uint8Array(msg);

            //parse from data the img's seq number
            var seqnum = data[data.length - 1];

            self.drawScreen(data);

            var delay = Math.abs(self.lastImageTime - currTime);

            if (!self.alreadyReceivedImage) {
                // first image, send 0
                self.alreadyReceivedImage = true;
                delay = 0;
                this.APP.hideSpinner();
            }

            self.API.sendAck(seqnum, delay);
            self.lastImageTime = currTime;
            return;
        }

        switch (msg.type) {
            case "SCREEN_OPTIONS":
                if(msg.jsonPayload.hasnavbar) {
                    self.toggleSoftKey(false);
                } else {
                    self.toggleSoftKey(true);
                }

                if (msg.jsonPayload.rotation == "ROTATION_0" || msg.jsonPayload.rotation == "ROTATION_180") {
                    //got landscanpe
                    self.deviceScreenWidth = msg.jsonPayload.screenX;
                    self.deviceScreenHeight = msg.jsonPayload.screenY;
                } else {
                    self.deviceScreenWidth = msg.jsonPayload.screenY;
                    self.deviceScreenHeight = msg.jsonPayload.screenX;
                }
                self.deviceOpts.rotation = msg.jsonPayload.rotation;

                if (self.deviceScreenWidth > self.deviceScreenHeight) {
                    self.screenDivOrientation = 1;
                } else {
                    self.screenDivOrientation = 0;
                }

                self.rotateWireframe();
                self.rotateButtons();
                self.resetScreen();
                self.updateQualityDesc();
                break;
            case "CONNECTED":
                self.alreadyReceivedImage=false;
                break;
        }
    },

    rotateScreenOnce: void 0,

    rotateScreen: function() {
        this.drawContext.translate($('.cellphone-mid').width(), 0);
        this.drawContext.rotate(Math.PI / 180 * 90);
    },

    loadScreen: function() {
        var self = this;
        return function() {
            //check the got screen orientation
            var gotImageOrientation;
            if (self.lastImage.width > self.lastImage.height) {
                gotImageOrientation = 1;
            } else {
                gotImageOrientation = 0;
            }

            //A screnDivOrientacio talan mindig portrait lesz. Hardkodolni lehetne.
            if (gotImageOrientation != self.screenDivOrientation) {
                self.rotateScreenOnce();
                self.doRotate = true;
                self.drawContext.drawImage(self.lastImage, 0, 0, $('.cellphone-mid').height(), $('.cellphone-mid').width());
            } else {
                self.doRotate = false;
                self.drawContext.drawImage(self.lastImage, 0, 0, $('.cellphone-mid').width(), $('.cellphone-mid').height());
            }
        }
    },

    drawScreen: function(imgData) {
        var self = this;

        var i = imgData.length;
        var binaryString = [i];
        while (i--) {
            binaryString[i] = String.fromCharCode(imgData[i]);
        }
        var data = binaryString.join('');

        var base64 = window.btoa(data);
        this.lastImage.src = "data:image/jpeg;base64," + base64;
    },

    transFormPhysicalBtn: function(id, btnRotate, btnTranslateX, btnTranslateY) {
        var btn = document.getElementById(id);

        btn.style.transform = 'translate('+btnTranslateY+'%, '+btnTranslateX+'%) rotate(' + btnRotate + 'deg)';
        btn.style.webkitTransform = 'translate('+btnTranslateY+'%, '+btnTranslateX+'%) rotate(' + btnRotate + 'deg)';
        btn.style.mozTransform = 'translate('+btnTranslateY+'%, '+btnTranslateX+'%) rotate(' + btnRotate + 'deg)';
        btn.style.msTransform = 'translate('+btnTranslateY+'%, '+btnTranslateX+'%) rotate(' + btnRotate + 'deg)';
        btn.style.oTransform = 'translate('+btnTranslateY+'%, '+btnTranslateX+'%) rotate(' + btnRotate + 'deg)';
    },

    rotateButtons: function() {
        var btnRotate, rightBtnTranslateX, rightBtnTranslateY, leftBtnTranslateX, leftBtnTranslateY;
        switch (this.deviceOpts.rotation) {
            case "ROTATION_0":
                btnRotate = 0;
                rightBtnTranslateX = 0;
                rightBtnTranslateY = 0;
                leftBtnTranslateX = 0;
                leftBtnTranslateY = 0;
                break;
            case "ROTATION_90":
                btnRotate = 90;
                rightBtnTranslateX = -180;
                rightBtnTranslateY = 20;
                leftBtnTranslateX = 180;
                leftBtnTranslateY = -35;
                break;
            case "ROTATION_180":
                btnRotate = 180;
                rightBtnTranslateY = 35;
                rightBtnTranslateX = 180;
                leftBtnTranslateY = -20;
                leftBtnTranslateX = 180;
                break;
            case "ROTATION_270":
                btnRotate = 270;
                rightBtnTranslateX = 180;
                rightBtnTranslateY = 20;
                leftBtnTranslateX = -180;
                leftBtnTranslateY = -35;
                break;
        }

        this.transFormPhysicalBtn("cellphone-volume-button", btnRotate, leftBtnTranslateX, leftBtnTranslateY);
        this.transFormPhysicalBtn("cellphone-power-button", btnRotate, rightBtnTranslateX, rightBtnTranslateY);
    },

    rotateWireframe: function() {
        var deg;
        var move; //todo: calculate the move
        switch (this.deviceOpts.rotation) {
             case "ROTATION_0":
                deg = 0;
                move= 0;
                break;
             case "ROTATION_90":
                deg = -90;
                move = -15;
                break;
             case "ROTATION_180":
                 deg = -180;
                 move = 0;
                 break;
             case "ROTATION_270":
                 deg = 90;
                move = -15;
                break;
        }

        if(this.deviceScreenWidth > this.deviceScreenHeight) {
            move *= -1;
        }

        var div = document.getElementById("wireframe");

        div.style.webkitTransform = 'translate(0%, '+move+'%) rotate(' + deg + 'deg)';
        div.style.mozTransform = 'translate(0%, '+move+'%) rotate(' + deg + 'deg)';
        div.style.msTransform = 'translate(0%, '+move+'%) rotate(' + deg + 'deg)';
        div.style.oTransform = 'translate(0%, '+move+'%) rotate(' + deg + 'deg)';
        div.style.transform = 'translate(0%, '+move+'%) rotate(' + deg + 'deg)';
    },

    getSizeOfCurrentHorizontalEdge: function() {
        switch (this.deviceOpts.rotation) {
            case "ROTATION_0":
                return this.deviceScreenWidth;
                break;
            case "ROTATION_90":
                return this.deviceScreenHeight;
                break;
            case "ROTATION_180":
                return this.deviceScreenWidth;
                break;
            case "ROTATION_270":
                return this.deviceScreenHeight;
                break;
        }
    },

    getSizeOfCurrentVerticalEdge: function() {
        switch (this.deviceOpts.rotation) {
            case "ROTATION_0":
                return this.deviceScreenHeight;
                break;
            case "ROTATION_90":
                return this.deviceScreenWidth;
                break;
            case "ROTATION_180":
                return this.deviceScreenHeight;
                break;
            case "ROTATION_270":
                return this.deviceScreenWidth;
                break;
        }
    },

    resetScreen: function() {
        var newHeight, newWidth;
        var wpWidth = $(".content").width() - (132*2)-144;
        var wpHeight = $(".content").height() - (100*2)-51;
        var aspect = this.deviceScreenHeight / this.deviceScreenWidth;

        //calculate the scale
        //It depend from the device screen orientation and the monitor (workspace) orientation
        var deviceEdge;
        var workspaceEdge;

        if(wpHeight < wpWidth) {
            workspaceEdge = wpHeight;
            deviceEdge = this.getSizeOfCurrentVerticalEdge();
        } else {
            workspaceEdge = wpWidth;
            deviceEdge = this.getSizeOfCurrentHorizontalEdge();
        }
        scale = workspaceEdge / deviceEdge;

        //calculate the new width
        newWidth = this.deviceScreenWidth * scale;
        newWidth = Math.max(newWidth, this.minWidth);

        //caluclate the new height
        newHeight = newWidth * aspect;

        $('#phone-display').height(newHeight);
        $('#phone-display').width(newWidth);

        this.drawContext.canvas.width = newWidth;
        this.drawContext.canvas.height = newHeight;

        /*
         Ha mar egyszer elforgattuk es uj meretet kap
         akkor ujra el kell forgatni.
         */
        if (this.doRotate) {
            this.rotateScreen();
        }

        if (this.quality == 'resAuto') {
            this.updateQuality();
        }
    },

    //Akkor hivodik meg ha csuszkat allitunk vagy resize van
    updateQuality: function() {
        var quality = this.QUALITIES[this.quality] / 100;
        var opts = {
            screenX: Math.floor(this.deviceScreenWidth * quality),
            screenY: Math.floor(this.deviceScreenHeight * quality)
        }
        if (this.canvasOpts == void 0 || opts.screenX != this.canvasOpts.screenX || opts.screenY != this.canvasOpts.screenY) {
            this.API.sendOpts(opts);
            this.canvasOpts = opts;
        }

        return opts
    },

    updateQualityDesc: function() {
        var self = this;
        var calcAndShow = function(tag, res) {
            var percent = self.QUALITIES[res];
            var h = self.deviceScreenHeight * (self.QUALITIES[res] / 100);
            var w = self.deviceScreenWidth * (self.QUALITIES[res] / 100);
            $(tag).text(h+"x"+w+", "+percent+"%");
        };
        calcAndShow("#resHighDesc" ,'resHigh');
        calcAndShow("#resMediumDesc" ,'resMedium');
        calcAndShow("#resLowDesc" ,'resLow');
    },

    toggleSoftKey: function(show) {
        if(show) {
            $("#navbar").show();
        } else {
            $("#navbar").hide();
        }
    },

    onError: function(err) {
        // $(".webkey-error").text(err).show();
        // $("canvas.cellphone-mid").hide();
        // var prev = this.deviceOpts.rotation;
        // this.deviceOpts.rotation = "ROTATION_0";
        // this.rotateWireframe();
        // this.deviceOpts.rotation = prev;
    },

    hideError: function() {
        // $(".webkey-error").hide();
        // $("canvas.cellphone-mid").show();
        // this.rotateWireframe();
    },

    showView: function() {
        var self = this;
        self.alreadyReceivedImage = false;

        this.APP.showSpinner();

        this.API.startStream();
        $("#phoneContainer").show();
        $("#phoneNav").addClass("active");

        $(window).on("resize", function() {
            self.resetScreen();
        });

        self.resetScreen();
    },

    hideView: function() {
        this.API.stopStream();
        this.APP.hideSpinner();
        $("#phoneContainer").hide();

        $(document).off("keydown");
        $(document).off("keypress");
        $(window).on("resize");
    }
};
