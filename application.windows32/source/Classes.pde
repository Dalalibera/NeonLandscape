//The Player class
class Player {

  PVector pos = new PVector();
  PVector size = new PVector();
  PVector speed = new PVector();
  PVector rotation = new PVector();
  float generator;

  PShape shape;

  //Just create a ship shape
  Player() {
    shape = createShip();
    size.x = 75;
    size.y = 15;
    size.z = 70;
    generator = 100;
  }

  //Reset every cycle; just do it
  void reset() {
    shape.resetMatrix();
  }

  //Renders to the screen
  void show() {
    shape.translate(0, 0, -100);
    shape(shape, pos.x, pos.y);
  }

  //Just some rotation
  void rotX(float angle) {
    shape.rotate(angle, 1, 0, 0);
  }

  void rotY(float angle) {
    shape.rotate(angle, 0, 1, 0);
  }

  void rotZ(float angle) {
    shape.rotate(angle, 0, 0, 1);
  }
}

//The Player class
class Road {

  PVector pos = new PVector();
  float speed;

  PShape segment;
  PShape floor = new PShape();
  PShape ceiling = new PShape();
  Obstacle panel = new Obstacle();
  Obstacle bridge = new Obstacle();
  Obstacle block = new Obstacle();
  Obstacle net = new Obstacle();

  boolean finish;
  boolean collided;

  //Just create a ship shape
  Road(float z) {

    collided = false;
    finish = false;

    this.pos.y = 200;
    this.pos.z = z;

    segment = createShape(GROUP);

    floor = createRoad();
    segment.addChild(floor);

    if ((course[roadCount % 300] & 1) != 0) {  //Tunnel
      ceiling = createCeiling();
      segment.addChild(ceiling);
    }

    if ((course[roadCount % 300] & 2) != 0) {  //Block
      block.create('k', pos);
    }

    if ((course[roadCount % 300] & 4) != 0) {  //Bridge
      bridge.create('d', pos);
    }

    if ((course[roadCount % 300] & 8) != 0) {  //Net
      net.create('n', pos);
    }

    if ((course[roadCount % 300] & 16) != 0) {  //BPanel
      panel.create('b', pos);
    }

    if ((course[roadCount % 300] & 32) != 0) {  //TPanel
      panel.create('t', pos);
    }

    if ((course[roadCount % 300] & 64) != 0) {  //RPanel
      panel.create('r', pos);
    }

    if ((course[roadCount % 300] & 128) != 0) {  //LPanel
      panel.create('l', pos);
    }

    if ((course[roadCount % 300] & 256) != 0) {  //LPanel
      panel.create('m', pos);
    }

    if (course[roadCount % 300] == 512) {  //LPanel
      finish = true;
      net.create('f', pos);
    }

    speed = 0;
    segment.translate(0, 200, z);
  }

  void update() {
    speed = player.speed.z;
    segment.translate(0, 0, speed);

    bridge.update(speed);
    block.update(speed);
    panel.update(speed);
    net.update(speed);

    pos.z += speed;
  }

  //Renders to the screen
  void showRoad() {
    shape(floor, 0, 0);
    bridge.show();
    block.show();
  }

  //Renders to the screen
  void showCeiling() {
    shape(ceiling, 0, 0);
  }

  //Renders to the screen
  void showObstacle() {
    panel.show();
    net.show();
  }
}

class Obstacle {
  PShape shape;
  PVector pos;
  BBox box;

  Obstacle() {
  }

  void create(char type, PVector p) {

    shape = new PShape();
    pos = new PVector();
    box = new BBox();

    switch(type) {

    case 't': 
      shape = createPanel(type);  
      break;
    case 'r': 
      shape = createPanel(type);  
      break;
    case 'b': 
      shape = createPanel(type);  
      break;
    case 'l': 
      shape = createPanel(type);  
      break;
    case 'm': 
      shape = createPanel(type);  
      break;
    case 'n': 
      shape = createNet();        
      break;
    case 'd': 
      shape = createBridge();     
      break;
    case 'k': 
      shape = createBlock();      
      break;
    case 'f': 
      shape = createFinish();      
      break;
    }

    pos.x = p.x;
    pos.y = p.y;
    pos.z = p.z;

    shape.translate(0, 200, pos.z);

    box = bounds(shape);
  }

  void show() {
    if (shape == null)
      return;
    shape(shape, 0, 0);
  }

  void update(float s) {
    if (shape == null)
      return;
    shape.translate(0, 0, s);
    pos.z += s;
    box.pos.z += s;
  }
}

//The "bounding box" class
class BBox {

  PVector pos;
  PVector size;

  BBox() {
    pos = new PVector();
    size = new PVector();
  }
}

class Shot {
  PVector pos = new PVector();
  PVector size = new PVector();
  float speed;

  PShape shape;

  Shot() {

    shape = createShot();
    pos.x = player.pos.x;
    pos.y = player.pos.y+20;
    pos.z = player.pos.z-190;

    speed = 100;

    size.x = 7;
    size.y = 7;
    size.z = 120;


    shape.translate(pos.x, pos.y, pos.z);
  }

  void update() {
    shape.translate(0, 0, -speed);
    pos.z -= speed;
  }

  void show() {
    shape(shape, 0, 0);
  }
}
