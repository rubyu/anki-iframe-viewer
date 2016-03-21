
$(function() {
  "use strint;";
  
  /**
   *  全てのノードを探索すると
   *  timer1: 2251.739ms
   */
  function scan(elem) {
    var jelem = $(elem);
    var pos = jelem.position();
    var left = pos.left;
    var top = pos.top;
    var width = jelem.width();
    var height = jelem.height();
    
    console.log("elem=", elem);
    console.log("left=%d, top=%d, width=%d, height=%d", left, top, width, height);
    
    jelem
      .children()
      .each(function() {
        scan(this);
    });
  }
  
  console.log("parse start");
  console.time("timer1");
  
  scan(document.body);
  
  console.timeEnd("timer1");
  console.log("parse end");
});
