//The ship shape
PShape createShip() {

  // Make the Ship parent shape
  PShape ship = createShape();

  /*Wings*/
  ship.beginShape(QUADS);
  ship.strokeWeight(LIGHT_STROKE);

  ship.fill(WINGS_FILL);
  ship.stroke(WINGS_STROKE);
  createVertices(ship, wingVertices);
  createVertices(ship, wingVertices, 'x');

  /* Body */
  ship.fill(BODY_FILL);
  ship.stroke(BODY_STROKE);
  createVertices(ship, bodyVertices);

  ship.fill(GLASS_FILL);
  createVertices(ship, cockPitVertices);

  ship.endShape(CLOSE);
  return ship;
}

//The road shape
PShape createRoad() {

  PShape road;

  //Child shapes
  road = createShape();

  //Creates the road (upper view); rotates to correct
  road.beginShape(QUADS);

  road.stroke(ROAD_STROKE);
  road.fill(ROAD_FILL);
  road.strokeWeight(MEDIUM_STROKE);

  createVertices(road, roadVertices);
  createVertices(road, roadVertices, 'z');

  //Ends and rotate shape
  road.endShape(CLOSE);

  return road;
}

//The tunnel ceiling shape
PShape createCeiling() {
  return createShape(QUADS, GLASS_STROKE, GLASS_FILL, MEDIUM_STROKE, ceilingVertices);
}

//The block obstacle shape
PShape createBlock() {
  return createShape(QUADS, BLOCK_STROKE, BLOCK_FILL, MEDIUM_STROKE, blockVertices);
}

//The bridge obstacle shape
PShape createBridge() {
  return createShape(QUADS, BRIDGE_STROKE, BRIDGE_FILL, MEDIUM_STROKE, bridgeVertices);
}

//The panel obstacle shape
PShape createPanel(char type) {

  PShape panel = createShape(QUADS, PANEL_STROKE, PANEL_FILL, MEDIUM_STROKE);
  boolean invertLateralPanel = false;

  switch(type) {

  case 'l': //invert the panel side
    invertLateralPanel = true;

  case 'r': //Creates the lateral panel
    if (invertLateralPanel)
      createVertices(panel, lateralPanelVertices, 'x');
    else
      createVertices(panel, lateralPanelVertices);
    break;

  case 'm': //Creates the full panel
    createVertices(panel, fullPanelVertices);
    break;

  case 't': //Creates the higher panel
    createVertices(panel, higerPanelVertices);
    break;

  case 'b': //Creates the lower panel
    createVertices(panel, lowerPanelVertices);
    break;
  }

  panel.endShape(CLOSE);
  return panel;
}

//The panel obstacle shape
PShape createNet() {

  PShape net = createShape(QUAD_STRIP, NET_STROKE, NET_FILL, LIGHT_STROKE);
  for (int cols = 0; cols <= 7; cols ++) {

    int rows = 0;
    int inc = 0;

    if (cols % 2 == 0) {
      rows = 1;
      inc = 1;
    } else {
      rows = 5;
      inc = -1;
    }

    for (; rows < 6 && rows > 0; rows += inc) {
      net.vertex(-600 + (cols*150), -500 + (rows*100));
      net.vertex(-600 + ((cols+1)*150), -500 + (rows*100));
    }
  }

  net.endShape(CLOSE);
  return net;
}

//The Finish line shape
PShape createFinish() {

  PShape finish = createShape(QUAD, 0, 0, MEDIUM_STROKE);
  createVertices(finish, finishVertices);
  finish.endShape(CLOSE);

  finish.beginShape(QUADS);
  finish.noStroke();
  finish.fill(255);
  finish.strokeWeight(MEDIUM_STROKE);

  int cont = 0;
  //Creates net
  for (int cols = 0; cols <= 7; cols ++) {
    int rows = 0;
    int inc = 0;

    if (cols % 2 == 0) {
      rows = 1;
      inc = 1;
    } else {
      rows = 5;
      inc = -1;
    }

    for (; rows < 6 && rows > 0; rows += inc) {
      if (cont % 2 == 0) {
        finish.vertex(-600 + (cols*150), -800 + (rows*100));
        finish.vertex(-600 + ((cols+1)*150), -800 + (rows*100));
        finish.vertex(-600 + ((cols+1)*150), -800 + ((rows+1)*100));
        finish.vertex(-600 + ((cols)*150), -800 + ((rows+1)*100));
      }
      cont++;
    }
  }

  //Ends and rotate shape
  finish.endShape(CLOSE);

  finish.beginShape(QUADS);
  finish.noStroke();
  finish.fill(0);
  finish.strokeWeight(MEDIUM_STROKE);

  cont = 0;
  //Creates net
  for (int cols = 0; cols <= 7; cols ++) {
    int rows = 0;
    int inc = 0;

    if (cols % 2 == 0) {
      rows = 1;
      inc = 1;
    } else {
      rows = 5;
      inc = -1;
    }

    for (; rows < 6 && rows > 0; rows += inc) {
      if (cont % 2 == 1) {
        finish.vertex(-600 + (cols*150), -800 + (rows*100));
        finish.vertex(-600 + ((cols+1)*150), -800 + (rows*100));
        finish.vertex(-600 + ((cols+1)*150), -800 + ((rows+1)*100));
        finish.vertex(-600 + ((cols)*150), -800 + ((rows+1)*100));
      }
      cont++;
    }
  }

  finish.endShape(CLOSE);
  return finish;
}

//The panel obstacle shape
PShape createShot() {
  return createShape(QUADS, acidGreen, acidGreen, LIGHT_STROKE, shotVertices);
}

PShape createShape(int type, color stroke, color fill, int strokeWeight, int[][] vertices) {
  PShape shape = createShape(type, stroke, fill, strokeWeight);
  createVertices(shape, vertices);
  shape.endShape(CLOSE);
  return shape;
}

PShape createShape(int type, color stroke, color fill, int strokeWeight) {
  PShape shape = createShape();
  shape.beginShape(type);
  shape.stroke(stroke);
  shape.fill(fill);
  shape.strokeWeight(strokeWeight);
  return shape;
}

void createVertices(PShape shape, int[][] vertices) {
  createVertices(shape, vertices, ' ');
}

void createVertices(PShape shape, int[][] vertices, char reverse) {
  int x = 1;
  int y = 1;
  int z = 1;

  switch(reverse) {
  case 'x': 
    x = -x; 
    break;
  case 'y': 
    y = -y; 
    break;
  case 'z': 
    z = -z; 
    break;
  default: 
    break;
  }

  for (int j = 0; j < vertices.length; j++) {
    shape.vertex(vertices[j][0] * x, vertices[j][1] * y, vertices[j][2] * z);
    shape.vertex(vertices[j][3] * x, vertices[j][4] * y, vertices[j][5] * z);
    shape.vertex(vertices[j][6] * x, vertices[j][7] * y, vertices[j][8] * z);
    shape.vertex(vertices[j][9] * x, vertices[j][10] * y, vertices[j][11] * z);
  }
};
