
/* Anki does not support `-webkit-margin-width` because it was made by QtWeb, and
 * the UserArget of it is: Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/534.34 (KHTML, like Gecko) Anki Safari/534.34
 */
if (navigator.userAgent.indexOf("Anki") != -1) {
  var html = document.getElementsByTagName("html")[0];
  var body = document.getElementsByTagName("body")[0];
  html.style.overflowY = "auto";
  body.style.height = "auto";
  throw "anki-iframe-viewer cannot work on old browsers :(";
}

(function() {
  "use strict";
  /* Lastest JS features, supported by Android WebView, can be used from here. */
  
  /* Global application settings. */
  const App = {};
  App.devicePreferredBaseFontSizes = new Map([
    ["SO-03G", 10],
    ["Nexus7", 12]
  ]);
  App.baseFontSize = (function(self) {
    const defaultFontSize = 14;
    console.log("defaultFontSize: %d", defaultFontSize);
    console.log("devicePixelRatio: %d", window.devicePixelRatio);
    function getAdjustedFontSize() {
      const device = Array.from(self.devicePreferredBaseFontSizes.keys()).find(function(key) {
        return navigator.userAgent.indexOf(key) != -1;
      });
      if (device) {
        const devicePreferredBaseFontSize = self.devicePreferredBaseFontSizes.get(device);
        console.log("device [%s] detected, preferred base font size: %d", device, devicePreferredBaseFontSize);
        return devicePreferredBaseFontSize;
      }
      console.log("calculate adjusted font size based on devicePixelRatio");
      const fontSize = defaultFontSize - window.devicePixelRatio;
      if (fontSize % 2 == 0) {
        return fontSize;
      } else {
        console.log("round up to be multiple of 2; %d to %d", fontSize, fontSize+1);
        return fontSize+1;
      }
    }
    const fontSize = getAdjustedFontSize();
    console.log("baseFontSize: %s", fontSize);
    return fontSize;
  })(App);
  App.alreadyTouched = false;
  App.minSwipeSize = App.baseFontSize * 2;
  App.minLongSwipeSize = Math.min(screen.height, screen.width) * 0.65;
  App.minLongTouchMillis = 1000;
  App.maxGestureMillis = 3000;

  Object.keys(App).forEach(function(key) {
    console.log("App.%s: %s", key, App[key]);
  });
  /* Assertions for application settings. */
  console.assert(App.alreadyTouched === false);
  console.assert(App.minSwipeSize < App.minLongSwipeSize);
  console.assert(App.minLongTouchMillis < App.maxGestureMillis);
  
  /* Adjust applictaion font size based on App.baseFontSize */
  (function() {
    var html = document.getElementsByTagName("html")[0];
    var size = App.baseFontSize + "px";
    console.log("set html.style.fontSize = %s", size);
    html.style.fontSize = size;
  })();
  
  
  /* Definitions of the application components. */
  
  
  var Viewer = function(flash) {
    this.flash = flash;
    this.fields = [];
  };
  Viewer.prototype.registerField = function(elem, caption) {
    this.fields.push({element: elem, caption: caption});
  };
  Viewer.prototype.castCurrentState = function() {
    this.flash.cast(this._getActiveField().caption);
  };
  Viewer.prototype._setViewLeft = function(left, caption) {
    console.log("current view left: %d", window.scrollX);
    console.log("set view left: %d", left);
    window.scrollTo(left, 0);
    this.flash.cast(caption);
  };
  Viewer.prototype._getActiveField = function() {
    var self = this;
    var _left = window.scrollX;
    console.log("current left: %d", _left);
    return this.fields.slice().reverse().find(function(field) {
      var left = self._left(field.element);
      console.log("left of '%s': %d", field.caption, left);
      if (_left >= left) {
        return true;
      }
    });
  };
  Viewer.prototype._left = function(elem) {
    return elem.offsetLeft;
  };
  Viewer.prototype.goPrevPage = function() {
    //todo adjust left alignment
    //todo show caption when over pages
    this._setViewLeft(window.scrollX-screen.width);
  };
  Viewer.prototype.goNextPage = function() {
    this._setViewLeft(window.scrollX+screen.width);
  };
  Viewer.prototype.goPrevField = function() {
    var self = this;
    var _left = window.scrollX;
    console.log("current left: %d", _left);
    if (_left == 0) {
      this._setViewLeft(0, this.fields[0].caption);
      return;
    }
    this.fields.slice().reverse().some(function(field) {
      var left = self._left(field.element);
      console.log("left of '%s': %d", field.caption, left);
      if (_left > left) {
        self._setViewLeft(left, field.caption);
        return true;
      }
    });
  };
  Viewer.prototype.goNextField = function() {
    var self = this;
    var _left = window.scrollX;
    console.log("current left: %d", _left);
    this.fields.some(function(field, index, array) {
      var left = self._left(field.element);
      console.log("left of '%s': %d", field.caption, left);
      if (_left < left) {
        self._setViewLeft(left, field.caption);
        return true;
      } else if (index+1 == array.length) {
        self._setViewLeft(document.body.scrollWidth, field.caption);
        return true;
      }
    });
  };
  Viewer.prototype.goFirstField = function() {
    var field  = this.fields[0];
    this._setViewLeft(this._left(field.element), field.caption);
  };
  Viewer.prototype.goLastField = function() {
    var field = this.fields[this.fields.length-1];
    this._setViewLeft(this._left(field.element), field.caption);
  };

  var AudioPlayer = function() {
    var audio = document.getElementsByTagName("audio");
    if (audio.length > 0) {
      this.audio = audio[0];
    }
    this.stop = true;
    this.setuped = false;
  };
  AudioPlayer.prototype.setup = function() {
    var self = this;
    if (this.audio && !this.setuped) {
      console.log("audio.setup");
      this.setuped = true;
      this.audio.load();
      this.audio.play();
      this.audio.addEventListener("ended", function() {
        if (!self.stop) {
          self._play();
        }
      }, false);
    }
  };
  AudioPlayer.prototype._play = function() {
    console.log("audio.play");
    if (this.audio) {
      this.audio.play();
    }
  };
  AudioPlayer.prototype.playStart = function() {
    console.log("audio.stop: %s -> false", this.stop);
    if (this.stop) {
      this._play();
    }
    this.stop = false;
  };
  AudioPlayer.prototype.playEnd = function() {
    console.log("audio.stop: %s -> true", this.stop);
    this.stop = true;
  };

  var Flash = function() {
    var container = document.createElement("div");
    container.id = "flash-container";
    container.style.visibility = "hidden";
    var left = document.createElement("div");
    left.id = "flash-container-left";
    var right = document.createElement("div");
    right.id = "flash-container-right";
    var numerator = document.createElement("div");
    numerator.id = "flash-container-numerator";
    var denominator = document.createElement("div");
    denominator.id = "flash-container-denominator";
    var text = document.createElement("div");
    text.id = "flash-container-text";
    this.container = container;
    this.left = left;
    this.right = right;
    this.numerator = numerator;
    this.denominator = denominator;
    this.text = text;
    left.appendChild(text);
    right.appendChild(numerator);
    right.appendChild(denominator);
    container.appendChild(left);
    container.appendChild(right);
    document.body.appendChild(container);
  };
  Flash.prototype.reflesh = function() {
    var page = Math.floor(window.scrollX / screen.width) + 1;
    var pages = Math.floor(document.body.scrollWidth / screen.width);
    console.log("page: %d/%d", page, pages);
    this.numerator.innerHTML = page;
    this.denominator.innerHTML = pages;
  };
  Flash.prototype.cast = function(text) {
    var self = this;
    text = text || "";
    console.log("cast: %s", text);
    if (this.timer) {
      window.clearInterval(this.timer);
    }
    this.reflesh();
    this.text.innerHTML = text;
    this.reflesh();
    this.container.style.opacity = 1.0;
    this.container.style.visibility = "visible";
    this.timerStart = Date.now();
    this.timer = window.setInterval(function() {
      var delta = Date.now() - self.timerStart;
      if (delta < 500) {
        // do nothing
      } else if (delta < 10000) {
        var alpha = 1 - Math.pow(((delta - 500) / 500), 2);
        if (alpha < 0) {
          window.clearInterval(self.timer);
          self.timer = null;
          self.container.style.visibility = "hidden";
        } else {
          self.container.style.opacity = alpha;
        }
      }
    }, 10);
  };

  var MouseEvent = function(viewer, audioPlayer) {
    this.viewer = viewer;
    this.audioPlayer = audioPlayer;
  };
  MouseEvent.prototype.wheelUp = function() {
    this.viewer.goPrevPage();
  };
  MouseEvent.prototype.wheelDown = function() {
    this.viewer.goNextPage();
  };

  var TouchEvent = function(viewer, audioPlayer) {
    this.viewer = viewer;
    this.audioPlayer = audioPlayer;
  };
  TouchEvent.prototype.up = function() {
    this.viewer.goNextPage();
  };
  TouchEvent.prototype.down = function() {
    this.viewer.goPrevPage();
  };
  TouchEvent.prototype.left = function() {
    this.viewer.goNextPage();
  };
  TouchEvent.prototype.right = function() {
    this.viewer.goPrevPage();
  };
  TouchEvent.prototype.longUp = function() {
    this.viewer.goNextField();
  };
  TouchEvent.prototype.longDown = function() {
    this.viewer.goPrevField();
  };
  TouchEvent.prototype.longLeft = function() {
    this.viewer.goNextField();
  };
  TouchEvent.prototype.longRight = function() {
    this.viewer.goPrevField();
  };
  TouchEvent.prototype.tap = function() {
    this.viewer.goNextPage();
  };
  TouchEvent.prototype.longTapStart = function() {
    this.audioPlayer.playStart();
  };
  TouchEvent.prototype.longTapEnd = function() {
    this.audioPlayer.playEnd();
  };
  TouchEvent.prototype.firstTouch = function() {
    this.audioPlayer.setup();
  };

  var Gesture = function(appEvent) {
    this.appEvent = appEvent;
    this.touches = {};
  };
  Gesture.prototype.type = {
    tap: 1,
    longTap: 2,
    swipeLeft: 3,
    swipeRight: 4,
    swipeUp: 5,
    swipeDown: 6,
    longSwipeLeft: 7,
    longSwipeRight: 8,
    longSwipeUp: 9,
    longSwipeDown: 10
  };
  Gesture.prototype.tryFirstTouch = function() {
    if (!App.alreadyTouched) {
      this.appEvent.firstTouch();
      App.alreadyTouched = true;
    }
  };
  Gesture.prototype.pushStart = function(id, x, y) {
    console.log("gesture.start", id, x, y);
    var self = this;
    this.tryFirstTouch();
    var event = {
      x: x,
      y: y,
      timestamp: Date.now()
    };
    var touch = {
      start: event,
      moves: [],
      end: null,
      type: 0,
      longTapChecked: false
    };
    this.touches[id] = touch;
    // set a timer for detecting a longTap
    window.setTimeout(function() {
      console.log("callback of a timer to check longTap; id: ", id);
      self._checkLongTap(id);
      if (self.isLongTap(id)) {
        console.log("longTap");
        self.appEvent.longTapStart();
      } else {
        console.log("not longTap");
      }
    }, App.minLongTouchMillis);
  };
  Gesture.prototype.pushMove = function(id, x, y) {
    //console.log("gesture.move", id, x, y);
    this.tryFirstTouch();
    var touch = this.touches[id];
    if (!touch) {
      // this event can be ignored because the gesture not started
      return;
    }
    var event = {
      x: x,
      y: y,
      timestamp: Date.now()
    };
    touch.moves.push(event);
  };
  Gesture.prototype.pushEnd = function(id, x, y) {
    console.log("gesture.end", id, x, y);
    var touch = this.touches[id];
    var end = {
      x: x,
      y: y,
    };
    touch.end = end;
    if (touch && touch.longTapChecked)
    this._checkLongTap(id);
    if (this.isLongTap(id)) {
      // this event can be ignored because the gesture already ended
      return;
    }
    var type = this._getSwipeType(touch.start, end);
    console.log("swipeType: ", type);
    touch.type = type;
  };
  Gesture.prototype._hasNoMoves = function(id) {
    var minSwipeSize = App.minSwipeSize;
    var touch = this.touches[id];
    var x = touch.start.x;
    var y = touch.start.y;
    return touch.moves.every(function(event) {
      return Math.max(Math.abs(event.x - x), Math.abs(event.y - y)) < minSwipeSize;
    });
  };
  Gesture.prototype._checkLongTap = function(id) {
    var touch = this.touches[id];
    if (touch &&
       !touch.longTapChecked &&
       !touch.end &&
        this._hasNoMoves(id)) {
      touch.type = this.type.longTap;
    }
  };
  Gesture.prototype._getSwipeType = function(start, end) {
    var dx = start.x - end.x;
    var dy = start.y - end.y;
    var x = Math.abs(dx);
    var y = Math.abs(dy);
    if (x > y) {
      if (x > App.minLongSwipeSize) {
        if (dx > 0) {
          return this.type.longSwipeLeft;
        } else {
          return this.type.longSwipeRight;
        }
      } else if (x > App.minSwipeSize) {
        if (dx > 0) {
          return this.type.swipeLeft;
        } else {
          return this.type.swipeRight;
        }
      } else {
        //todo tapLeft
        return this.type.tap;
      }
    } else {
      if (y > App.minLongSwipeSize) {
        if (dy > 0) {
          return this.type.longSwipeUp;
        } else {
          return this.type.longSwipeDown;
        }
      } else if (y > App.minSwipeSize) {
        if (dy > 0) {
          return this.type.swipeUp;
        } else {
          return this.type.swipeDown;
        }
      } else {
        return this.type.tap;
      }
    }
  };
  Gesture.prototype.delete = function(id) {
    console.log("delete", id);
    if (id in this.touches) {
      delete this.touches[id];
    }
  };
  Gesture.prototype.has = function(id) {
    return id in this.touches;
  };
  Gesture.prototype.isTap = function(id) {
    if (id in this.touches) {
      return this.touches[id].type === this.type.tap;
    }
  };
  Gesture.prototype.isLongTap = function(id) {
    if (id in this.touches) {
      return this.touches[id].type === this.type.longTap;
    }
  };
  Gesture.prototype.isSwipeLeft = function(id) {
    if (id in this.touches) {
      return this.touches[id].type === this.type.swipeLeft;
    }
  };
  Gesture.prototype.isSwipeRight = function(id) {
    if (id in this.touches) {
      return this.touches[id].type === this.type.swipeRight;
    }
  };
  Gesture.prototype.isSwipeUp = function(id) {
    if (id in this.touches) {
      return this.touches[id].type === this.type.swipeUp;
    }
  };
  Gesture.prototype.isSwipeDown = function(id) {
    if (id in this.touches) {
      return this.touches[id].type === this.type.swipeDown;
    }
  };
  Gesture.prototype.isLongSwipeLeft = function(id) {
    if (id in this.touches) {
      return this.touches[id].type === this.type.longSwipeLeft;
    }
  };
  Gesture.prototype.isLongSwipeRight = function(id) {
    if (id in this.touches) {
      return this.touches[id].type === this.type.longSwipeRight;
    }
  };
  Gesture.prototype.isLongSwipeUp = function(id) {
    if (id in this.touches) {
      return this.touches[id].type === this.type.longSwipeUp;
    }
  };
  Gesture.prototype.isLongSwipeDown = function(id) {
    if (id in this.touches) {
      return this.touches[id].type === this.type.longSwipeDown;
    }
  };

  var Dispacher = function(appEvent, preferredDispacher) {
    this.appEvent = appEvent;
    this.gesture = new Gesture(this.appEvent);
    this.lastDispatchTime = Date.now();
  };
  Dispacher.prototype.dispatchStart = function(id, x, y) {
    this.gesture.pushStart(id, x, y);
  };
  Dispacher.prototype.dispatchMove = function(id, x, y) {
    this.gesture.pushMove(id, x, y);
  };
  Dispacher.prototype.dispatchEnd = function(id, x, y) {
    this.lastDispatchTime = Date.now();
    // prevent duplicate fire of mouseup event after touchend
    if (this._preferredDispatcher &&
        this.lastDispatchTime - this._preferredDispatcher.lastDispatchTime < 1500) {
      return;
    }
    this.gesture.pushEnd(id, x, y);
    if (this.gesture.isTap(id)) {
      this.appEvent.tap();
    } else if (this.gesture.isLongTap(id)) {
      // tear up a longTap event, triggered by a timer
      this.appEvent.longTapEnd();
    } else if (this.gesture.isSwipeLeft(id)) {
      this.appEvent.left();
    } else if (this.gesture.isSwipeRight(id)) {
      this.appEvent.right();
    } else if (this.gesture.isSwipeUp(id)) {
      this.appEvent.up();
    } else if (this.gesture.isSwipeDown(id)) {
      this.appEvent.down();
    } else if (this.gesture.isLongSwipeLeft(id)) {
      this.appEvent.longLeft();
    } else if (this.gesture.isLongSwipeRight(id)) {
      this.appEvent.longRight();
    } else if (this.gesture.isLongSwipeUp(id)) {
      this.appEvent.longUp();
    } else if (this.gesture.isLongSwipeDown(id)) {
      this.appEvent.longDown();
    }
    this.gesture.delete(id);
  };
  
  
  
  
  
  
  
  var flash,
      audioPlayer,
      viewer,
      mouseEvent,
      touchEvent,
      touchDispacher,
      mouseDispacher = null;

  function prepare() {
    flash = new Flash();
    audioPlayer = new AudioPlayer();
    viewer = new Viewer(flash);
    // detached-00 is `head`
    // detached-01 is `rank`
    // detached-02 is `COCA`
    // detached-03 is `voice`
    viewer.registerField(document.getElementById("detached-04"), "ランダムハウス英語辞典");
    viewer.registerField(document.getElementById("detached-05"), "研究社 新英和中辞典");
    viewer.registerField(document.getElementById("detached-06"), "研究社 新英和大辞典");
    viewer.registerField(document.getElementById("detached-07"), "斎藤和英大辞典");
    viewer.registerField(document.getElementById("detached-08"), "ロングマン現代英英辞典");

    mouseEvent = new MouseEvent(viewer, audioPlayer);
    touchEvent = new TouchEvent(viewer, audioPlayer);
    touchDispacher = new Dispacher(touchEvent);
    mouseDispacher = new Dispacher(touchEvent, touchDispacher);

    // https://developer.mozilla.org/en-US/docs/Web/API/MouseWheelEvent
    document.addEventListener("mousewheel", function(event) {
      var delta = event.wheelDelta;
      if (delta > 0) {
        mouseEvent.wheelUp();
      } else {
        mouseEvent.wheelDown();
      }
    });

    // for debug use
    // https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent
    document.addEventListener("mousedown", function(event) {
      mouseDispacher.dispatchStart(0, event.pageX, event.pageY);
      event.preventDefault();
    }, false);
    document.addEventListener("mousemove", function(event) {
      mouseDispacher.dispatchMove(0, event.pageX, event.pageY);
      event.preventDefault();
    }, false);
    document.addEventListener("mouseup", function(event) {
      mouseDispacher.dispatchEnd(0, event.pageX, event.pageY);
      event.preventDefault();
    }, false);

    // https://developer.mozilla.org/en-US/docs/Web/API/Touch
    // https://developer.mozilla.org/ja/docs/Web/Guide/DOM/Events/Touch_events
    document.addEventListener("touchstart", function(event) {
      var touches = event.changedTouches;
      var size = touches.length;
      for (var i=0; i < size; ++i) {
        var touch = touches[i];
        touchDispacher.dispatchStart(touch.identifier, touch.pageX, touch.pageY);
      }
      event.preventDefault();
    }, false);
    document.addEventListener("touchmove", function(event) {
      var touches = event.changedTouches;
      var size = touches.length;
      for (var i=0; i < size; ++i) {
        var touch = touches[i];
        touchDispacher.dispatchMove(touch.identifier, touch.pageX, touch.pageY);
      }
      event.preventDefault();
    }, false);
    document.addEventListener("touchend", function(event) {
      var touches = event.changedTouches;
      var size = touches.length;
      for (var i=0; i < size; ++i) {
        var touch = touches[i];
        touchDispacher.dispatchEnd(touch.identifier, touch.pageX, touch.pageY);
      }
      event.preventDefault();
    }, false);
  }
  
  function complete() {
    viewer.castCurrentState();
  }
  
  window.addEventListener("DOMContentLoaded", prepare, false);
  window.addEventListener("load", complete, false);
})();
