PGraphics drawBG() {

  //Draws the bacgkround to a PGraphics
  PGraphics bg = createGraphics(width, height);
  int h = height;

  bg.beginDraw();

  // Define colors
  color c1 = color(37, 23, 98);
  c1 = color(0);
  color c2 = color(37, 23, 98);
  color c3 = lightPurple;

  bg.noFill();

  //Draws the sky
  for (int i = 0; i <= h/3; i++) {
    float inter = map(i, 0, h/3, 0, 1);
    color c = lerpColor(c1, c2, inter);
    bg.stroke(c);
    bg.line(0, i, width, i);
  }

  //Draws the sky
  for (int i = 0; i <= h/3*2; i++) {
    float inter = map(i, 0, h/3*2, 0, 2);
    color c = lerpColor(c2, c3, inter);
    bg.stroke(c);
    bg.line(0, i+h/3, width, i+h/3);
  }

  //Draws the first "planet"
  int diameter = width/3;
  int radius = diameter/2;

  bg.ellipseMode(RADIUS);
  bg.stroke(lightPurple);
  bg.stroke(200, 23, 250, 80);
  bg.strokeWeight(15);
  bg.fill(0, 0, 0, 120);
  bg.ellipse(width/2, h/2, radius, radius);

  radius /=5 ;
  bg.strokeWeight(3);
  bg.stroke(255, 255, 255, 120);
  bg.fill(250, 250, 250);
  bg.ellipse(width/3, h/3, radius, radius);

  bg.stroke(255);
  bg.fill(255);

  for (int i = 0; i < int(random(25, 45)); i++) {
    int r = int(random(1, 2));
    bg.ellipse(random(15, width-15), random(15, h-15), r, r);
  }

  bg.endDraw();

  return bg;
}

PGraphics drawMainBG() {

  //Draws the main menu background to a PGraphics
  PGraphics bg = createGraphics(width, height, P3D);

  bg.beginDraw();
  bg.background(0);

  bg.rectMode(CORNERS);
  bg.rect(width/5*2, height/3, width/5*4, height/3*2);

  bg.textAlign(RIGHT);  
  bg.textFont(fntDisplay, 56);
  bg.fill(cyan);
  bg.text("Fugue to the", width/2, height/10);

  bg.textAlign(RIGHT);
  bg.textFont(fntNeon, 48);
  bg.fill(gumBall);
  bg.text("NEON", width/2-50, height/10+48);
  bg.fill(gumBall);
  bg.textAlign(LEFT);
  bg.text(" Landscape", width/2-50, height/10+48);

  Player tempPlayer = new Player();

  tempPlayer.rotZ(radians(25));
  tempPlayer.rotX(radians(-15));
  tempPlayer.rotY(radians(-35));


  bg.textAlign(RIGHT);  
  bg.textFont(fntDisplay, 56);
  bg.fill(cyan);
  bg.text("Fugue to the", width/2, height/10);

  bg.fill(255, 255, 255, 120);


  int x = width/7*2;
  int y = height/3-100;
  int w = width/7*5 - x;
  int h = height/3*2 - y-100;


  int scl = 50;

  int col = w / scl;
  int row = (h / scl)*4;

  bg.hint(DISABLE_DEPTH_TEST);

  bg.fill(20);
  bg.rect(x, y, x+w, y+h);
  bg.hint(ENABLE_DEPTH_TEST);

  bg.stroke(acidGreen);
  bg.strokeWeight(3);
  bg.noFill();

  for (int i = 0; i < row; i++) {
    bg.beginShape(QUADS);
    for (int j = 0; j < col; j++) {
      bg.vertex(x + w / row * i, y + h/col*j);
      bg.vertex(x + w/row*i, y + h/col*(j+1));
      bg.vertex(x + w/row*(i+1), y + h/col*(j+1));
      bg.vertex(x + w/row*(i+1), y + h/col*j);
    }
    bg.endShape();
  }



  bg.shape(tempPlayer.shape, width/2, height/2-100);

  bg.endDraw();

  return bg;
}

PGraphics drawBlack() {

  //Draws just a black screen to avoid bugs
  PGraphics bg = createGraphics(width, height);
  bg.beginDraw();
  bg.background(0);
  bg.endDraw();

  return bg;
}