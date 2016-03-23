
var debug = true;

$(function() {
  "use strint;";
  
  /**
   */
  function _scan(elem, max_depth, depth) {
    var jelem = $(elem);
    var pos = jelem.position();
    var left = pos.left;
    var top = pos.top;
    var width = jelem.width();
    var height = jelem.height();
    
    if (debug) {
    console.log("elem=", elem);
    console.log("left=%d, top=%d, width=%d, height=%d", left, top, width, height);
    console.log("depth=%d, max_depth=%d", depth, max_depth);
    }
    
    if (depth < max_depth) {
      jelem
        .children()
        .each(function() {
          _scan(this, max_depth, depth+1);
      });
    }
  }
  
  function scan(elem, max_depth) {
    _scan(elem, max_depth, 0);
  }
  
  /*
  console.log("parse start");
  console.time("timer1");
  
  //scan(document.body, 3);
  
  console.timeEnd("timer1");
  console.log("parse end");
  
  var viewer_container = document.createElement("div");
  viewer_container.className = "viewer-container";
  var menu_container = document.createElement("div");
  menu_container.className = "menu-container";
  var menu = document.createElement("div");
  menu.className = "menu";
  var head = document.createElement("span");
  head.className = "head";
  head.textContent = "bite";
  var rank = document.createElement("span");
  rank.className = "rank";
  rank.textContent = "1";
  var field_selector = document.createElement("span");
  field_selector.className = "field-selector";
  field_selector.textContent = "Wikipedia";
  var touch_area = document.createElement("div");
  touch_area.className = "touch-area";
  
  viewer_container.appendChild(menu_container);
  menu_container.appendChild(menu);
  menu.appendChild(head);
  menu.appendChild(rank);
  menu.appendChild(field_selector);
  viewer_container.appendChild(touch_area);
  document.body.appendChild(viewer_container);
  */
  
  (function() {
    var Viewer = function() {};
    Viewer.prototype.goPrevPage = function() {
      window.scrollTo(window.scrollX-screen.width, 0);
    };
    Viewer.prototype.goNextPage = function() {
      window.scrollTo(window.scrollX+screen.width, 0);
    };
    Viewer.prototype.playSound = function() {
      
    };
    
    var TouchEvent = function(viewer) {
      this.viewer = viewer;
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
      alert("longUp");
    };
    TouchEvent.prototype.longDown = function() {
      alert("longDown");
    };
    TouchEvent.prototype.longLeft = function() {
      alert("longLeft");
    };
    TouchEvent.prototype.longRight = function() {
      alert("longRight");
    };
    TouchEvent.prototype.tap = function() {
      alert("tap");
    };
    
    var Dispacher = function(touchEvent) {
      this.touchEvent = touchEvent;
      this.state = {};
      var _1em = this._getOneEmInPixels();
      this._minShortSwipeSize = _1em * 4;
      this._minLongSwipeSize = Math.floor(Math.max(screen.height, screen.width) * 0.4);
      
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
    
    var viewer = new Viewer();
    var touchEvent = new TouchEvent(viewer);
    var mouseDispacher = new Dispacher(touchEvent);
    var touchDispacher = new Dispacher(touchEvent);
    
    //https://developer.mozilla.org/ja/docs/Web/Guide/DOM/Events/Touch_events
    document.addEventListener('mousedown', function(event) {
      mouseDispacher.dispatchStart(event.pageX, event.pageY);
    }, false);
    document.addEventListener('mouseup', function(event) {
      mouseDispacher.dispatchEnd(event.pageX, event.pageY);
    }, false);
    document.addEventListener('touchstart', function(event) {
      if (event.changedTouches.length == 1) {
        var touch = event.changedTouches[0];
        touchDispacher.dispatchStart(touch.pageX, touch.pageY);
      }
    }, false);
    document.addEventListener('touchend', function(event) {
      if (event.changedTouches.length == 1) {
        var touch = event.changedTouches[0];
        touchDispacher.dispatchEnd(touch.pageX, touch.pageY);
      }
    }, false);
    
  })();
  
});
