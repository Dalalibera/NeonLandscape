import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.effects.*;
import ddf.minim.signals.*;
import ddf.minim.spi.*;
import ddf.minim.ugens.*;

import java.util.Iterator;
import java.util.LinkedList;

//Fugue to the Neon Landscape
//Author: Giovanne Dalalibera
//Description:
//Trabalho para a disciplina Prolegômenos ao Computar
//do primeiro semestre da turma de BSI 2017

//Strings
String STR_GAME_TITLE = "Fugue to the Neon Landscape";
String STR_DIFFICULTY[] = new String[]{"Easy", "Normal", "Hard", "Endless"};
String STR_DIFF_EXPLANATION[] = new String[]{" +Slow speed \n +Easy course \n +Limited ammo", " +Normal speed \n +Normal course \n +Limited ammo", " +Faster speed \n +Normal course \n +Slow ammo-regen", " +Normal speed \n +RNG endless course \n +Fast ammo-regen \n\n Not always fair...\n Sorry about that ):"};
String STR_SELECT_DIFF = "Select difficulty (UP/DOWN):\n";
String STR_PRESS_START = "Press F2 to start";

String STR_BSOD = "Foi detectado um problema e o Windows foi \ndesligado para evitar danos ao computador\n\nSe esta for a primeira vez que você vê esta \ntela de erro de parada, reinicie o computador. \n\nInformações técnicas:\n\n*** STOP: 0x0000008E \n(0XC0000005,0XBFABFF1B,0XB8F61B14,0X00000000)\n\n*** nv4_disp.dll – Address BFABBF1B \nbase at BF9D4000, Datestamp 4410c8d4\n\n\nIniciando despejo de memória física.\nDespejo de memória física concluída.\nContate o administrador do sistema ou grupo \nde suporte técnico para obter a informação.";

int LEFT_PANEL   = 128;
int RIGHT_PANEL  = 64;
int TOP_PANEL    = 32;
int BOTTOM_PANEL = 16;
int NET          = 8;
int BRIDGE       = 4;
int BLOCK        = 2;
int TUNNEL       = 1;

/* Colors */
color lightPurple = color(200, 23, 250);  //#C817FA;
color acidGreen   = color(122, 255, 57);  //#7AFF39;
color cyan        = color(57, 240, 255);  //#39F0FF;
color brightRed   = color(255, 33, 13);   //#FF210D;
color toxicYellow = color(255, 249, 52);  //#FFF934;
color gumBall     = color(255, 0, 153);   //#FF0099;
color deepBlue    = color(52, 60, 170);   //#343CAA;
color deepPurple  = color(92, 0, 139);    //#5C008B;

/* Styles */
color GROUND_FILL    = cyan;
color GROUND_STROKE  = deepBlue;
color BODY_FILL      = deepPurple;
color BODY_STROKE    = cyan;
color WINGS_FILL     = deepPurple;
color WINGS_STROKE   = cyan;
color ROAD_FILL      = color(10);
color ROAD_STROKE    = gumBall;
color BLOCK_FILL     = color(10);
color BLOCK_STROKE   = gumBall;
color BRIDGE_FILL    = color(100);
color BRIDGE_STROKE  = color( 80);
color GLASS_FILL     = color(55, 55, 55, 150);
color GLASS_STROKE   = color(255);
color PANEL_FILL     = color(130, 130, 130, 130);
color PANEL_STROKE   = gumBall;
color NET_FILL       = color(200, 23, 250, 160);
color NET_STROKE     = toxicYellow;

int LIGHT_STROKE   = 1;
int MEDIUM_STROKE  = 3;
int BOLD_STROKE    = 5;

/* AUDIO ENGINE */
Minim minim;
AudioPlayer groove;

/* Rendering related */
PGraphics background, eraser, mainBG;
PFont fntText, fntDisplay, fntNeon, fntWasted, fntSystem, fntBSD;
int camera;

/* "Physics" stuff */
float flying;
float barrelRot;
float barrelDst;
int barrelDir;
boolean barrel;
boolean collided;
boolean shoot;

/* Game Logic */
int gameState;
int timer, start;
int health;
int score;
boolean winnerz;
int diff;

/* Objects and instances */
//Road road[];
int MAX_ROAD = 15;    //Number of road segments to create and render (more is slower)
int roadCount;
int course[];
Player player;

ArrayList<Road> road = new ArrayList<Road>();
ArrayList<Shot> shot = new ArrayList<Shot>();

/*Input handling*/
boolean keys[] = new boolean [4];

void setup() {

  //Canvas setup
  size(1366, 768, P3D);
  surface.setTitle(STR_GAME_TITLE);
  frameRate(60);
  background(0);

  hint(ENABLE_OPTIMIZED_STROKE);

  /* Loads the fonts */
  fntSystem   = loadFont("Glass_TTY_VT220-80.vlw");
  fntText     = loadFont("The-2K12-80.vlw");
  fntDisplay  = loadFont("BebasNeueRegular-80.vlw");
  fntNeon     = loadFont("Audiowide-Regular-80.vlw");
  fntWasted   = loadFont("PricedownBl-Regular-80.vlw");
  fntBSD      = loadFont("LucidaConsole-48.vlw");

  //Draws the backgrounds and saves to a variable
  background = drawBG();    //Game Background
  mainBG = drawMainBG();    //Menu background
  eraser = drawBlack();     //Just a black screen; background() screws up the render

  //GROOVE; plays a loop
  minim = new Minim(this);
  groove = minim.loadFile("snd.mp3", 2048);
  groove.loop();

  gameState = 0;    //0 = Main menu; 1 = Game; 2 = Game over
  diff = 1;
  start = millis(); //Initializes a Timer
  setupGame();      //Sets everything up

  float cameraZ = (float(height)/2.0) / tan(PI*60.0/360.0);
  perspective(PI/3.0, float(width)/float(height), cameraZ/10.0, cameraZ*10.0*10);
}

void setupGame() {

  //Initializes some stuff
  //Creates the player
  player = new Player();
  player.pos.x = 0;
  player.pos.y = 150;
  player.speed.z = 30;

  barrelRot = 0;
  barrelDst = 0;
  barrelDir = 1;

  createCourse();

  roadCount = 0;
  road.clear();

  for (roadCount = 0; roadCount < MAX_ROAD; roadCount++) {
    //road[roadCount] = new Road(roadCount*-800);
    road.add(new Road(roadCount*-800));
  }

  health = 100;   //Lives amount
  camera = 1;     //Initial camera mode
  score = 0;      //Clears score
}

/*****************************/
/********** UPDATE ***********/
/*****************************/
void update() {

  if (gameState == 1) {
    player.reset();    //Need to reset rotation every frame to avoid rotation speed
    movement();        //Deals with movement
    physics();         //Deals with Physics

    if (shoot) {
      shot.add(new Shot());
      shoot = false;
    }

    // set Iterator as descending
    Iterator<Road> x = road.listIterator();

    // print list with descending order
    while (x.hasNext()) {
      Road temp = x.next();
      temp.update();      //Draws the road segment
    }

    for (int i = 0; i < road.size(); i++) {
      if (road.get(i).pos.z >= 800) {
        if (road.get(i).finish) {
          winnerz = true;
        }
        road.remove(i);
        road.add(new Road(road.get(road.size()-1).pos.z - 800));
        roadCount++;
        score += 10;
      }
    }

    for (int i = 0; i < shot.size(); i++) {

      shot.get(i).update();

      if (shot.get(i).pos.z <= -12000 || bulletCollision(shot.get(i))) {
        shot.remove(i);
      }
    }

    collided = false;            //Resets flag to check for collision
    collision();                 //Collision check

    if (collided) health -= 20;  //Decreases health when collided

    health = constrain(health, 0, 100);

    if (health == 0 || winnerz) {           //Check for Game Over
      gameState = 2;
    }
  }

  timer = millis() - start;      //Updates Timer
}

/*****************************/
/********* DRAW LOOP *********/
/*****************************/
void show() {

  switch(gameState) {

  case 0:    

    /* Main Menu */
    hint(DISABLE_DEPTH_TEST);        //Disables Z-buffer (for 2D render)
    image(mainBG, 0, 0);             //Draws background
    drawHUD();                       //Draws HUD

    textAlign(CENTER);
    textFont(fntSystem, 36);

    fill(gumBall);
    text((STR_DIFFICULTY[diff]), width/2, height/8*5+36);

    fill(lightPurple);
    text(STR_SELECT_DIFF, width/2, height/8*5);



    if (timer / 100 % 5 > 1) {       //Blinks the "Press start"
      textFont(fntSystem, 36);
      text(STR_PRESS_START, width/2, height/8*7.5);
    }

    textAlign(LEFT);
    fill(gumBall);
    textFont(fntSystem, 32);
    text(STR_DIFF_EXPLANATION[diff], width/4*2.9, height/8*5);

    break;

  case 1: 

    /* Game Room */
    lights();                                    //Sets the lights
    //directionalLight(255, 248, 183, 0, 1, 0);    //for 3D Render
    directionalLight(0, 0, 0, 0, 1, 0);    //for 3D Render

    hint(DISABLE_DEPTH_TEST);                    //Disables Z-buffer (for 2D render)
    image(background, 0, -100);                  //Draws background
    hint(ENABLE_DEPTH_TEST);                     //Re-enable Z-buffe (for 3D render)

    switch(camera) {                             //Camera translations/rotations

    case 1:

      //Third person
      translate(width/2 - player.pos.x/2, height/2)  ;
      break;

    case 2:

      //Close up
      translate(width/2 - player.pos.x, height/2 - player.pos.y + 50, 400);
      break;

    case 3:

      //First person
      hint(DISABLE_DEPTH_TEST);                              //Simulates a parallax effect
      image(background, 0, -player.rotation.x *2 -100);      //when drawing the background
      hint(ENABLE_DEPTH_TEST);                               //by X rotation

      translate(width/2 - player.pos.x, height/2 - player.pos.y, 850);
      rotateX(-player.rotation.x/60.0);
      rotateZ(-player.rotation.z/60.0);
      break;

    default:
      camera = 1;
      break;
    }

    player.show();    //Draws the player
    ground();         //Draws the ground

    for (int i = 0; i < shot.size(); i++) {
      shot.get(i).show();      //Draws the road segment
    }

    for (int i = road.size()-1; i >= 0; i--) {
      road.get(i).showRoad();      //Draws the road segment
      road.get(i).showCeiling();  //Draws the tunnel ceiling
      road.get(i).showObstacle(); //Draws the obstacles
    }

    drawHUD();                //Draws the HUD
    break;

  case 2: 

    /* GameOver */
    hint(DISABLE_DEPTH_TEST);      //Disables Z-buffer (for 2D render)

    //Draws the Game Over screen
    noStroke();                                
    fill(0, 0, 0, 180);
    rectMode(CORNERS);
    rect(0, height/10*3.5, width, height/10*6);
    filter(GRAY);

    //Renders the text
    textAlign(CENTER);
    textFont(fntWasted, 82);
    if (winnerz) {
      textStroke("Congratulations!!", 5, 0, brightRed, width/2, height/2);
    } else {
      textStroke("Wasted", 5, 0, brightRed, width/2, height/2);
    }
    textFont(fntNeon, 36);
    textAlign(RIGHT);
    textStroke("F2 - Try Again!", 2, 0, lightPurple, width/3, height/8*7-36);
    textAlign(CENTER);
    textStroke("F3 - Main Menu!", 2, 0, lightPurple, width/2, height/8*7-36);
    textAlign(LEFT);
    textStroke("Esc - Exit", 2, 0, lightPurple, width/3*2, height/8*7-36);

    noLoop();    //Stops the loop until an option is selected
    break;
  }
}

void draw() {    //Draw loop
  update();
  show();
}

boolean bulletCollision(Shot shot) {
  for (int i = 0; i < road.size(); i++) {
    float z = road.get(i).pos.z;

    if (road.get(i).block.shape != null) {
      if (checkCollision(road.get(i).block.shape, z, shot.pos, shot.size)) {

        //road.get(i).panel = new Obstacle();
        //road.get(i).collided = true;
        return true;
      }
    }

    if (road.get(i).panel.shape != null) {
      if (checkCollision(road.get(i).panel.shape, z, shot.pos, shot.size)) {

        road.get(i).panel = new Obstacle();
        return true;
      }
    }
  }

  return false;
}

void collision() {  //Collision detection

  for (int i = 0; i < road.size(); i++) {

    float z = road.get(i).pos.z;

    //Tries to check only the relevant segments
    //if (road.get(i).pos.z >= -400 && road.get(i).pos.z <= 400 && !road.get(i).collided) {
    if (!road.get(i).collided) {

      //Check the bridges
      if (road.get(i).bridge.shape != null) {
        if (checkCollision(road.get(i).bridge.shape, z, player.pos, player.size)) {
          collided = true;
          road.get(i).collided = true;
          player.speed.z -= 10;
        }
      }

      //Check the blocks
      if (road.get(i).block.shape != null) {
        if (checkCollision(road.get(i).block.shape, z, player.pos, player.size)) {
          collided = true;
          road.get(i).collided = true;
          player.speed.z -= 10;
        }
      }

      //Check the panels
      if (road.get(i).panel.shape != null) {
        if (checkCollision(road.get(i).panel.shape, z, player.pos, player.size)) {
          road.get(i).panel = new Obstacle();
          collided = true;
          road.get(i).collided = true;
          player.speed.z -= 10;
        }
      }

      //Check the nets
      if (road.get(i).net.shape != null) {
        if (checkCollision(road.get(i).net.shape, z, player.pos, player.size)) {
          collided = true;
          road.get(i).collided = true;
          player.speed.z -= 10;
        }
      }
    }
  }

  //Now check for boundaries to correct course
  //and decreases health

  /*left rect*/
  checkBoundaries(new PVector(-750, 197), new PVector(-450, 198), new PVector(player.pos.x-75, player.pos.y));

  /*left ramp*/
  checkBoundaries(new PVector(-450, 200), new PVector(-250, 400), new PVector(player.pos.x-75, player.pos.y));

  /*right ramp*/
  checkBoundaries(new PVector(450, 200), new PVector(250, 400), new PVector(player.pos.x+75, player.pos.y));

  /*right rect*/
  checkBoundaries(new PVector(750, 197), new PVector(450, 198), new PVector(player.pos.x+75, player.pos.y));
}

void checkBoundaries(PVector p1, PVector p2, PVector pl) {

  //Check for road boundaries and correct the player course
  if (lineToCircle(p1, p2, pl)) {
    player.pos.x -= player.speed.x+2;
    player.pos.y -= player.speed.y+5;
    player.speed.y = 0;
    player.speed.x = 0;

    health -= 0.5;

    player.speed.z -= 3;

    if (barrel) {    //Stop a barrel roll
      health -= 10;
      barrel = false;
      player.speed.z -= player.speed.z*0.2;
    }
  }
}

boolean checkCollision(PShape shape, float correction, PVector objCenter, PVector objSize) {

  BBox obstacle = bounds(shape);

  //Correct the obstacle position
  obstacle.pos.y += 200;
  obstacle.pos.z += correction;

  //Check for AABB collision
  if (AABBCollision(objCenter, objSize, obstacle.pos, obstacle.size)) {
    return true;
  }

  return false;
}

BBox bounds(PShape s) {
  //Get the min/max vertex of a shape for calculate the
  //minimum bounding box

  PVector max = new PVector();
  PVector min = new PVector();
  PVector v;

  //iterate through vertices and get the min/max for all the three axis
  for (int i = 0; i < s.getVertexCount(); i++) {
    v = s.getVertex(i);
    max.x = max(max.x, v.x);
    max.y = max(max.y, v.y);
    max.z = max(max.z, v.z);
    min.x = min(min.x, v.x);
    min.y = min(min.y, v.y);
    min.z = min(min.z, v.z);
  }

  BBox res = new BBox();

  //Calculates its size (half)
  res.size.x = (max.x - min.x)/2;
  res.size.y = (max.y - min.y)/2;
  res.size.z = (max.z - min.z)/2;

  //Calculates its position
  res.pos.x = max.x - res.size.x;
  res.pos.y = max.y - res.size.y;
  res.pos.z = max.z - res.size.z;

  return res;
}

boolean AABBCollision(PVector ACenter, PVector ASize, PVector BCenter, PVector BSize) {
  //Axis Aligned Bounding Box collision
  //For non-rotated boxes collision

  //check the X axis
  if (abs(ACenter.x - BCenter.x) < ASize.x + BSize.x) {
    //check the Y axis
    if (abs(ACenter.y - BCenter.y) < ASize.y + BSize.y) {
      //check the Z axis
      if (abs(ACenter.z - BCenter.z) < ASize.z + BSize.z) {
        return true;
      }
    }
  }
  return false;
} 

boolean lineToCircle(PVector p1, PVector p2, PVector el) {

  //Detects line to circle collision with some
  //Math Wizardry
  boolean col = false;
  PVector sub = PVector.sub(p2, p1);

  float a = sub.y / sub.x;
  float b = p1.y - a * p1.x;

  float A = (1 + a * a);
  float B = (2 * a *( b - el.y) - 2 * el.x);
  float C = (el.x * el.x + (b - el.y) * (b - el.y)) - (10 * 10);
  float delta = B * B - 4 * A * C;

  if (delta >= 0) {
    float x1 = (-B - sqrt(delta)) / (2 * A);
    float y1 = a * x1 + b;

    if ((x1 > min(p1.x, p2.x)) && (x1 < max(p1.x, p2.x)) && (y1 > min(p1.y, p2.y)) && (y1 < max(p1.y, p2.y))) {
      col = true;
    }

    float x2 = (-B + sqrt(delta)) / (2 * A);
    float y2 = a * x2 + b;
    if ((x2 > min(p1.x, p2.x)) && (x2 < max(p1.x, p2.x)) && (y2 > min(p1.y, p2.y)) && (y2 < max(p1.y, p2.y))) {
      col = true;
    }
  }

  return col;
}

//Triggered when any key is pressed
void keyPressed() {

  if (keyCode == 98 && (gameState == 0 || gameState == 2)) {
    //Start game when F2 is pressed on a menu
    setupGame();
    gameState = 1;
    winnerz = false;
    loop();
  }

  if (keyCode == 99 && (gameState == 2)) {
    //Start game when F2 is pressed on a menu
    gameState = 0;
    winnerz = false;
    loop();
  }

  if (keyCode == UP && gameState == 0) diff = (diff == 3 ? 0 : diff + 1);
  if (keyCode == DOWN && gameState == 0) diff = (diff == 0 ? 3 : diff - 1);

  if (key == 'r' || key == 'R')  setupGame();                //Restart the game
  if (key == 'f' || key == 'F')  shoot();                //Restart the game
  if (key == 'w' || key == 'W')  keys[0] = true;             //Controls
  if (key == 'a' || key == 'A')  keys[1] = true;             //
  if (key == 's' || key == 'S')  keys[2] = true;             //
  if (key == 'd' || key == 'D')  keys[3] = true;             //
  if (key == 'c' || key == 'C') camera++;                    //Change the camera
  if ((key == 'e' || key == 'E') && !barrel) barrelRoll(1);  //Right barrel
  if ((key == 'q' || key == 'Q') && !barrel) barrelRoll(-1); //Left barrel
  if (key == 'm' || key == 'M') {                            //Mute the music
    if (groove.isMuted()) {
      groove.unmute();
    } else {
      groove.mute();
    }
  }
}

void shoot() {
  if (player.generator >= 20) {
    shoot = true;
    player.generator -= 20;
  }
}

void barrelRoll(int dir) {                //Initiates the barrelRoll
  barrelDir = dir;
  barrel = true;
  barrelDst = player.pos.x + (350*dir);
}

//Triggered when any key is released
void keyReleased() {
  if (key == 'w' || key == 'w')  keys[0] = false;    //Controls
  if (key == 'a' || key == 'A')  keys[1] = false;    //Controls
  if (key == 's' || key == 'S')  keys[2] = false;    //Controls
  if (key == 'd' || key == 'D')  keys[3] = false;    //Controls
}

//Prints info to the screen
void drawHUD() {

  hint(DISABLE_DEPTH_TEST);    //Disables Z-buffer

  switch(camera) {
  case 1:

    //Draws info on third person camera
    camera();
    noLights();

    fill(255);
    textFont(fntSystem, 28);

    textAlign(LEFT);
    textStroke("   *- CONTROLS -* \n ------------------ \n WASD - Flight; \n Q/E - Barrel roll; \n (F)ire; \n (M)ute; \n (C)amera modes", 2, color(0), color(255), 10, 40);

    if (gameState != 0) {
      textAlign(RIGHT);
      textStroke("Shield: " + health + "  \n Alt: " + round((360 - player.pos.y) / 5)+"  \n Speed: " + int(player.speed.z)+ "  \n Generator: " + int(player.generator)+ "  ", 2, color(0), color(255), width - 10, 40);
    }
    break;

  case 2:

    //Draws info on close up camera
    translate(player.pos.x, player.pos.y);

    fill(255);
    textFont(fntText, 8);

    textAlign(RIGHT);
    text("Alt: " + round((360 - player.pos.y) / 5) + "\n Speed: " + int(player.speed.z), -30, 10);

    textAlign(LEFT);
    text("Shield: " + health + "\nGenerator: " + int(player.generator), 10, 10);

    camera();
    textFont(fntSystem, 28);
    textStroke("   *- CONTROLS -* \n ------------------ \n WASD - Flight; \n Q/E - Barrel roll; \n (F)ire; \n (M)ute; \n (C)amera modes;", 2, color(0), color(255), 10, 40);

    break;

  case 3:

    //draws info on first person camera
    camera();
    noLights();

    fill(54, 240, 255, 40);
    stroke(cyan);

    beginShape();
    vertex(0, 0);
    vertex(0, 30);
    vertex(width/6, height/6*4);
    vertex(width/6*5, height/6*4);
    vertex(width, 30);
    vertex(width, 0);
    endShape();

    beginShape();
    vertex(0, 30);
    vertex(width/6, height/6*4);
    vertex(width/7, height/5*4);
    vertex(0, height/5*4+40);
    endShape();

    beginShape();
    vertex(width, 30);
    vertex(width/6*5, height/6*4);
    vertex(width/7*6, height/5*4);
    vertex(width, height/5*4+40);
    endShape();

    fill(120);

    beginShape();
    vertex(0, height/5*4+40);
    vertex(width/7, height/5*4);
    vertex(width/6, height/6*4);
    vertex(width/6*5, height/6*4);
    vertex(width/7*6, height/5*4);
    vertex(width, height/5*4+40);
    vertex(width, height);
    vertex(0, height);
    endShape(CLOSE);

    fill(25);
    stroke(20);

    //Cockpit terminal
    int xTerm = width/6+20;
    int yTerm = height/6*4+20;

    rectMode(CORNER);
    rect(xTerm, yTerm, 320, 160, 4);

    fill(acidGreen);
    textFont(fntSystem, 26);
    textAlign(LEFT);
    text("Controls--------", xTerm+10, yTerm+30);
    text("WASD - Flight;", xTerm+10, yTerm+50);
    text("Q/E - Barrel roll!", xTerm+10, yTerm+70);
    text("F - Fire", xTerm+10, yTerm+90);
    text("M - Mute;", xTerm+10, yTerm+110);
    text("C - Camera mode;", xTerm+10, yTerm+130);



    if (timer/200 % 3 != 0) {
      text("                     _", xTerm+10, yTerm+150) ;
    }

    fill(60);
    rect(xTerm + 340, yTerm, 60, 160);

    stroke(255);
    rect(xTerm + 340, yTerm+155 - ((360 - player.pos.y) / 5 / 120) * 150, 60, 5);

    int radius = 50;

    ellipseMode(RADIUS);
    ellipse(xTerm + 460, yTerm + radius, radius, radius);
    float angle = radians(map(player.speed.z, 20, 90, 135, 300));
    stroke(125, 0, 0);
    strokeWeight(5);
    line(xTerm + 460, yTerm + radius, xTerm + 460 + cos(angle) * radius, yTerm + radius + sin(angle) * radius);

    strokeWeight(2);
    stroke(20);

    fill(60);
    rect(xTerm + 540, yTerm, 60, 160);

    fill(125, 125, 255);
    rect(xTerm + 540, yTerm + 160 - health*1.6, 60, health*1.6);

    fill(60);
    rect(xTerm + 620, yTerm, 60, 160);

    fill(125, 125, 0);
    rect(xTerm + 620, yTerm + 160 - player.generator*1.6, 60, player.generator*1.6);

    textFont(fntNeon, 26);
    fill(15);
    textAlign(CENTER);
    text("Altitude", xTerm + 370, yTerm + 185);
    text("Speed", xTerm + 460, yTerm + 120);
    text("Shield", xTerm + 570, yTerm + 185);
    textAlign(LEFT);
    text("Generator", xTerm + 620, yTerm + 185);

    rectMode(CORNER);
    fill(0, 0, 170);
    xTerm += 700;
    rect(xTerm, yTerm, 180, 160, 4);

    fill(255);
    textFont(fntBSD, 6);
    textAlign(LEFT);
    text(STR_BSOD, xTerm+10, yTerm+10);
    text(STR_BSOD, xTerm+10, yTerm+10);

    break;
  }

  textFont(fntSystem);
  textSize(48);
  textAlign(CENTER);
  if (gameState != 0)
    textStroke("SCORE: " + score, 2, color(0), color(255), width/2, 40);
}

void physics() {

  //if (player.speed.z < 50) {



  switch(diff) {
  case 0:
    player.speed.z += 0.005;
    break;

  case 1:
    player.speed.z += 0.05;
    break;

  case 2:
    player.generator += player.speed.z * 0.0005;
    player.speed.z += 0.08;
    break;

  case 3:
    player.speed.z += 0.05;
    player.generator += player.speed.z * 0.001;
    break;
  }


  player.speed.z = constrain(player.speed.z, 20, 90);
  player.generator = constrain(player.generator, 0, 100);
  health = constrain(health, 0, 100);


  //Some physics related shit
  //Caps acceleration
  player.speed.x = constrain(player.speed.x, -12, 12);
  player.speed.y = constrain(player.speed.y, -12, 12);

  //Rotates
  player.rotX(player.rotation.x/30.0);
  player.rotZ(player.rotation.z/30.0);

  //Calculates and cap player position
  player.pos.x += player.speed.x;
  player.pos.y += player.speed.y;
  player.pos.y = constrain(player.pos.y, -250, 360);
  player.pos.x = constrain(player.pos.x, -600, 600);

  //DO A BARREL ROLL!!!!
  if (barrel) {
    player.rotZ(radians(barrelRot * barrelDir));
    barrelRot += (constrain((720 - barrelRot) / 50, 5, 1000));
    player.pos.x += (barrelDst - player.pos.x) * 0.05;

    if (abs(barrelRot) > 360) {
      barrelRot = 0;
      barrel = false;
    }
  }
}

//Handles the input for movement
//@TODO: rewrite this shit...
void movement() {
  //Deals with vertical movement
  if (keys[0] || keys [2]) {

    //Up
    if (keys[0]) {
      player.speed.y -= 0.5;

      //Rotates the ship
      if (player.rotation.x > -10) {
        player.rotation.x --;
      }
    }

    //Down
    if (keys[2]) {
      player.speed.y += 0.5;

      //Rotates the ship
      if (player.rotation.x < 10) {
        player.rotation.x ++;
      }
    }
  } else {
    //Vertical deacceleration
    if (player.speed.y > 0)
      player.speed.y -= 0.5;
    else if (player.speed.y < 0)
      player.speed.y += 0.5;

    //Rotates again to original rotation
    if (player.rotation.x > 0) {
      player.rotation.x --;
    } else if (player.rotation.x < 0) {
      player.rotation.x ++;
    }
  }

  //Deals with horizontal movement
  if (keys[1] || keys [3]) {

    //Left
    if (keys[1]) {
      player.speed.x -= 0.5;

      //Rotates the ship
      if (player.rotation.z > -10) {
        player.rotation.z --;
      }
    }
    //Right
    if (keys[3]) {
      player.speed.x += 0.5;

      //Rotates the ship
      if (player.rotation.z < 10) {
        player.rotation.z ++;
      }
    }
  } else {
    //Horizontal deacceleration
    if (player.speed.x > 0)
      player.speed.x -= 0.5;
    else if (player.speed.x < 0)
      player.speed.x += 0.5;

    //Rotates again to original rotation
    if (player.rotation.z > 0) {
      player.rotation.z --;
    } else if (player.rotation.z < 0) {
      player.rotation.z ++;
    }
  }
}

void textStroke(String text, int stroke, color cStroke, color cFill, int x, int y) {
  //Draws a text with Stroke (kinda)...
  fill(cStroke);

  text(text, x-stroke, y-stroke);
  text(text, x+stroke, y-stroke);
  text(text, x+stroke, y+stroke);
  text(text, x-stroke, y+stroke);

  fill(cFill);
  text(text, x, y);
}

//Terrain generation
void ground() {

  //Some variables
  int scl = 390;
  int w = 12000;
  int h = 6200;

  int cols = w / scl;
  int rows = h/ scl;

  float[][] terrain;
  terrain = new float[cols][rows];

  //The "speed" of the terrain
  flying -= 0.01;

  //Maps the noise to a more organic look
  float yoff = flying;
  for (int y = 0; y < rows; y++) {
    float xoff = 0;
    for (int x = 0; x < cols; x++) {
      terrain[x][y] = map(noise(xoff, yoff), 0, 1, -300, 200);
      xoff += 0.2;
    }
    yoff += 0.2;
  }

  //The style
  stroke(GROUND_STROKE);
  strokeWeight(2);
  //fill(#050534);
  fill(GROUND_FILL);

  //Some transformations to put everything in place
  pushMatrix();
  rotateX(PI/2);

  translate(-w/2, -h/2);

  //Left portion of the ground
  for (int y = 0; y < rows-1; y++) {
    beginShape(TRIANGLE_STRIP);
    for (int x = 0; x < cols; x++) {
      vertex(x*scl, y*scl - 3000, terrain[x][y] - 550);
      vertex(x*scl, (y+1)*scl - 3000, terrain[x][y+1] - 550);
      //rect(x*scl, y*scl, scl, scl);
    }
    endShape();
  }

  popMatrix();
}
