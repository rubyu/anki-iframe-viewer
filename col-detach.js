"use strint;";

function loadScript(url) {
  var script = document.createElement("script");
  script.type = "text/javascript";
  script.src = url;
  var s = document.getElementsByTagName("script")[0];
  s.parentNode.insertBefore(script, s);
}
loadScript("woothee.js");

(function() {
  var defaultFontSize = 14;
  function getAdjustedFontSize() {
    if (navigator.userAgent.indexOf("SO-03G") != -1) {
      return 10;
    } 
    if (navigator.userAgent.indexOf("Nexus7") != -1) {
      return 12;
    }
    console.log("device pixel ratio: %d", window.devicePixelRatio);
    var n = defaultFontSize - window.devicePixelRatio;
    if (n % 2 == 0) {
      return n;
    } else {
      return n+1;
    }
  }
  var html = document.getElementsByTagName("html")[0];
  var size = getAdjustedFontSize() + "px";
  console.log("adjusted font size: %s", size);
  html.style.fontSize = size;
})();

(function() {
  /* General Anki does not support `-webkit-margin-width` because it was made by QtWeb and
   * the UserArget of it is: Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/534.34 (KHTML, like Gecko) Anki Safari/534.34
   */
  if (navigator.userAgent.indexOf("Anki") != -1) {
    var html = document.getElementsByTagName("html")[0];
    var body = document.getElementsByTagName("body")[0];
    html.style.overflowY = "auto";
    body.style.height = "auto";
  }
})();

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
  /* Caution: This may return a wrong value due to the implimentation of browsers.
  * There is need to use `elem.offsetLeft` here.
  */
  //var lect = elem.getBoundingClientRect();
  //console.log("offsetLeft: %d", elem.offsetLeft);
  //console.log("lect.left: %d", lect.left);
  //console.log("lect.right: %d,", lect.right);
  //console.log("window.pageXOffset: %d", window.pageXOffset);
  //console.log("window.pageXOffset + lect.left: %d", window.pageXOffset + lect.left);
  //return lect.left;

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
  this.remaining = 0;
};
AudioPlayer.prototype.load = function() {
  if (this.audio) {
    this.audio.loop = false;
    this.audio.load();
  }
};
AudioPlayer.prototype.playOnce = function() {
  var self = this;
  this.remaining += 1;
  console.log("remaining: %d", this.remaining);
  if (this.audio && !this.setup) {
    console.log("setup");
    this.setup = true;
    this.audio.addEventListener("ended", function() {
      self.remaining -= 1;
      console.log("remaining: %d", self.remaining);
      if (self.remaining > 0) {
        self.audio.play();
      }
    }, false);
  }
  if (this.audio && this.remaining == 1) {
    console.log("play");
    this.audio.play();
  }
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
  this.audioPlayer.playOnce();
};

var Dispacher = function(touchEvent, preferredDispacher) {
  this.touchEvent = touchEvent;
  this.state = {};
  var _1em = this._getOneEmInPixels();
  this._minShortSwipeSize = _1em * 2;
  this._minLongSwipeSize = Math.floor(Math.min(screen.height, screen.width) * 0.7);
  this._preferredDispatcher = preferredDispacher;
  this.lastDispatchTime = Date.now();
};
Dispacher.prototype._getOneEmInPixels = function() {
  var elem = document.createElement('div');
  elem.style.cssText = 'margin:0; padding:0; width:1px; height:1rem; visibility:hidden;';
  document.body.appendChild(elem);
  var _1em = elem.clientHeight;
  document.body.removeChild(elem);
  return _1em;
};
Dispacher.prototype.dispatchStart = function(x, y) {
  this.state.x = x;
  this.state.y = y;
};
Dispacher.prototype.dispatchEnd = function(x, y) {
  this.lastDispatchTime = Date.now();
  // prevent duplicate fire of mouseup after touchend
  if (this._preferredDispatcher &&
      this.lastDispatchTime - this._preferredDispatcher.lastDispatchTime < 1500) {
    return;
  }
  var _x = this.state.x;
  var _y = this.state.y;
  var dx = _x - x;
  var dy = _y - y;
  var ax = Math.abs(dx);
  var ay = Math.abs(dy);
  if (ax > ay) {
    if (ax > this._minLongSwipeSize) {
      if (dx > 0) {
        this.touchEvent.longLeft();
      } else {
        this.touchEvent.longRight();
      }
    } else if (ax > this._minShortSwipeSize) {
      if (dx > 0) {
        this.touchEvent.left();
      } else {
        this.touchEvent.right();
      }
    } else {
      this.touchEvent.tap();
    }
  } else {
    if (ay > this._minLongSwipeSize) {
      if (dy > 0) {
        this.touchEvent.longUp();
      } else {
        this.touchEvent.longDown();
      }
    } else if (ay > this._minShortSwipeSize) {
      if (dy > 0) {
        this.touchEvent.up();
      } else {
        this.touchEvent.down();
      }
    } else {
      this.touchEvent.tap();
    }
  }
};

var flash,
    audioPlayer,
    viewer,
    mouseEvent,
    touchEvent,
    touchDispacher,
    mouseDispatcher = null;

function prepare() {
  flash = new Flash();
  audioPlayer = new AudioPlayer();
  audioPlayer.load();
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
  
  //https://developer.mozilla.org/en-US/docs/Web/API/MouseWheelEvent
  document.addEventListener("mousewheel", function(event) {
    var delta = event.wheelDelta;
    if (delta > 0) {
      mouseEvent.wheelUp();
    } else {
      mouseEvent.wheelDown();
    }
  });
  
  //https://developer.mozilla.org/ja/docs/Web/Guide/DOM/Events/Touch_events
  document.addEventListener('mousedown', function(event) {
    mouseDispacher.dispatchStart(event.pageX, event.pageY);
  }, false);
  document.addEventListener('mouseup', function(event) {
    mouseDispacher.dispatchEnd(event.pageX, event.pageY);
  }, false);
  document.addEventListener('touchstart', function(event) {
    if (event.changedTouches.length >= 1) {
      var touch = event.changedTouches[0];
      touchDispacher.dispatchStart(touch.pageX, touch.pageY);
    }
  }, false);
  document.addEventListener('touchend', function(event) {
    if (event.changedTouches.length >= 1) {
      var touch = event.changedTouches[0];
      touchDispacher.dispatchEnd(touch.pageX, touch.pageY);
    }
  }, false);
}

function complete() {
  viewer.castCurrentState();
}

window.addEventListener("DOMContentLoaded", prepare, false);
window.addEventListener("load", complete, false);
