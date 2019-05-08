import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import ddf.minim.analysis.*; 
import ddf.minim.effects.*; 
import ddf.minim.signals.*; 
import ddf.minim.spi.*; 
import ddf.minim.ugens.*; 
import java.util.Iterator; 
import java.util.LinkedList; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class NeonLandscape extends PApplet {











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
int lightPurple = color(200, 23, 250);  //#C817FA;
int acidGreen   = color(122, 255, 57);  //#7AFF39;
int cyan        = color(57, 240, 255);  //#39F0FF;
int brightRed   = color(255, 33, 13);   //#FF210D;
int toxicYellow = color(255, 249, 52);  //#FFF934;
int gumBall     = color(255, 0, 153);   //#FF0099;
int deepBlue    = color(52, 60, 170);   //#343CAA;
int deepPurple  = color(92, 0, 139);    //#5C008B;

/* Styles */
int GROUND_FILL    = cyan;
int GROUND_STROKE  = deepBlue;
int BODY_FILL      = deepPurple;
int BODY_STROKE    = cyan;
int WINGS_FILL     = deepPurple;
int WINGS_STROKE   = cyan;
int ROAD_FILL      = color(10);
int ROAD_STROKE    = gumBall;
int BLOCK_FILL     = color(10);
int BLOCK_STROKE   = gumBall;
int BRIDGE_FILL    = color(100);
int BRIDGE_STROKE  = color( 80);
int GLASS_FILL     = color(55, 55, 55, 150);
int GLASS_STROKE   = color(255);
int PANEL_FILL     = color(130, 130, 130, 130);
int PANEL_STROKE   = gumBall;
int NET_FILL       = color(200, 23, 250, 160);
int NET_STROKE     = toxicYellow;

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

public void setup() {

  //Canvas setup
  
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
  mainBG     = drawMainBG();    //Menu background
  eraser     = drawBlack();     //Just a black screen; background() screws up the render

  //GROOVE; plays a loop
  minim = new Minim(this);
  groove = minim.loadFile("snd.mp3", 2048);
  groove.loop();

  gameState = 0;    //0 = Main menu; 1 = Game; 2 = Game over
  diff = 1;
  start = millis(); //Initializes a Timer
  setupGame();      //Sets everything up

  float cameraZ = (PApplet.parseFloat(height)/2.0f) / tan(PI*60.0f/360.0f);
  perspective(PI/3.0f, PApplet.parseFloat(width)/PApplet.parseFloat(height), cameraZ/10.0f, cameraZ*10.0f*10);
}

public void setupGame() {

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
public void update() {

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
public void show() {

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
      text(STR_PRESS_START, width/2, height/8*7.5f);
    }

    textAlign(LEFT);
    fill(gumBall);
    textFont(fntSystem, 32);
    text(STR_DIFF_EXPLANATION[diff], width/4*2.9f, height/8*5);

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
      rotateX(-player.rotation.x/60.0f);
      rotateZ(-player.rotation.z/60.0f);
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
    rect(0, height/10*3.5f, width, height/10*6);
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

public void draw() {    //Draw loop
  update();
  show();
}

public boolean bulletCollision(Shot shot) {
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

public void collision() {  //Collision detection

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

public void checkBoundaries(PVector p1, PVector p2, PVector pl) {

  //Check for road boundaries and correct the player course
  if (lineToCircle(p1, p2, pl)) {
    player.pos.x -= player.speed.x+2;
    player.pos.y -= player.speed.y+5;
    player.speed.y = 0;
    player.speed.x = 0;

    health -= 0.5f;

    player.speed.z -= 3;

    if (barrel) {    //Stop a barrel roll
      health -= 10;
      barrel = false;
      player.speed.z -= player.speed.z*0.2f;
    }
  }
}

public boolean checkCollision(PShape shape, float correction, PVector objCenter, PVector objSize) {

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

public BBox bounds(PShape s) {
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

public boolean AABBCollision(PVector ACenter, PVector ASize, PVector BCenter, PVector BSize) {
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

public boolean lineToCircle(PVector p1, PVector p2, PVector el) {

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
public void keyPressed() {

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

public void shoot() {
  if (player.generator >= 20) {
    shoot = true;
    player.generator -= 20;
  }
}

public void barrelRoll(int dir) {                //Initiates the barrelRoll
  barrelDir = dir;
  barrel = true;
  barrelDst = player.pos.x + (350*dir);
}

//Triggered when any key is released
public void keyReleased() {
  if (key == 'w' || key == 'w')  keys[0] = false;    //Controls
  if (key == 'a' || key == 'A')  keys[1] = false;    //Controls
  if (key == 's' || key == 'S')  keys[2] = false;    //Controls
  if (key == 'd' || key == 'D')  keys[3] = false;    //Controls
}

//Prints info to the screen
public void drawHUD() {

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
      textStroke("Shield: " + health + "  \n Alt: " + round((360 - player.pos.y) / 5)+"  \n Speed: " + PApplet.parseInt(player.speed.z)+ "  \n Generator: " + PApplet.parseInt(player.generator)+ "  ", 2, color(0), color(255), width - 10, 40);
    }
    break;

  case 2:

    //Draws info on close up camera
    translate(player.pos.x, player.pos.y);

    fill(255);
    textFont(fntText, 8);

    textAlign(RIGHT);
    text("Alt: " + round((360 - player.pos.y) / 5) + "\n Speed: " + PApplet.parseInt(player.speed.z), -30, 10);

    textAlign(LEFT);
    text("Shield: " + health + "\nGenerator: " + PApplet.parseInt(player.generator), 10, 10);

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
    rect(xTerm + 540, yTerm + 160 - health*1.6f, 60, health*1.6f);

    fill(60);
    rect(xTerm + 620, yTerm, 60, 160);

    fill(125, 125, 0);
    rect(xTerm + 620, yTerm + 160 - player.generator*1.6f, 60, player.generator*1.6f);

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

public void physics() {

  //if (player.speed.z < 50) {



  switch(diff) {
  case 0:
    player.speed.z += 0.005f;
    break;

  case 1:
    player.speed.z += 0.05f;
    break;

  case 2:
    player.generator += player.speed.z * 0.0005f;
    player.speed.z += 0.08f;
    break;

  case 3:
    player.speed.z += 0.05f;
    player.generator += player.speed.z * 0.001f;
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
  player.rotX(player.rotation.x/30.0f);
  player.rotZ(player.rotation.z/30.0f);

  //Calculates and cap player position
  player.pos.x += player.speed.x;
  player.pos.y += player.speed.y;
  player.pos.y = constrain(player.pos.y, -250, 360);
  player.pos.x = constrain(player.pos.x, -600, 600);

  //DO A BARREL ROLL!!!!
  if (barrel) {
    player.rotZ(radians(barrelRot * barrelDir));
    barrelRot += (constrain((720 - barrelRot) / 50, 5, 1000));
    player.pos.x += (barrelDst - player.pos.x) * 0.05f;

    if (abs(barrelRot) > 360) {
      barrelRot = 0;
      barrel = false;
    }
  }
}

//Handles the input for movement
//@TODO: rewrite this shit...
public void movement() {
  //Deals with vertical movement
  if (keys[0] || keys [2]) {

    //Up
    if (keys[0]) {
      player.speed.y -= 0.5f;

      //Rotates the ship
      if (player.rotation.x > -10) {
        player.rotation.x --;
      }
    }

    //Down
    if (keys[2]) {
      player.speed.y += 0.5f;

      //Rotates the ship
      if (player.rotation.x < 10) {
        player.rotation.x ++;
      }
    }
  } else {
    //Vertical deacceleration
    if (player.speed.y > 0)
      player.speed.y -= 0.5f;
    else if (player.speed.y < 0)
      player.speed.y += 0.5f;

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
      player.speed.x -= 0.5f;

      //Rotates the ship
      if (player.rotation.z > -10) {
        player.rotation.z --;
      }
    }
    //Right
    if (keys[3]) {
      player.speed.x += 0.5f;

      //Rotates the ship
      if (player.rotation.z < 10) {
        player.rotation.z ++;
      }
    }
  } else {
    //Horizontal deacceleration
    if (player.speed.x > 0)
      player.speed.x -= 0.5f;
    else if (player.speed.x < 0)
      player.speed.x += 0.5f;

    //Rotates again to original rotation
    if (player.rotation.z > 0) {
      player.rotation.z --;
    } else if (player.rotation.z < 0) {
      player.rotation.z ++;
    }
  }
}

public void textStroke(String text, int stroke, int cStroke, int cFill, int x, int y) {
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
public void ground() {

  //Some variables
  int scl = 390;
  int w = 12000;
  int h = 6200;

  int cols = w / scl;
  int rows = h/ scl;

  float[][] terrain;
  terrain = new float[cols][rows];

  //The "speed" of the terrain
  flying -= 0.01f;

  //Maps the noise to a more organic look
  float yoff = flying;
  for (int y = 0; y < rows; y++) {
    float xoff = 0;
    for (int x = 0; x < cols; x++) {
      terrain[x][y] = map(noise(xoff, yoff), 0, 1, -300, 200);
      xoff += 0.2f;
    }
    yoff += 0.2f;
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
public PGraphics drawBG() {

  //Draws the bacgkround to a PGraphics
  PGraphics bg = createGraphics(width, height);
  int h = height;

  bg.beginDraw();

  // Define colors
  int c1 = color(37, 23, 98);
  c1 = color(0);
  int c2 = color(37, 23, 98);
  int c3 = lightPurple;

  bg.noFill();

  //Draws the sky
  for (int i = 0; i <= h/3; i++) {
    float inter = map(i, 0, h/3, 0, 1);
    int c = lerpColor(c1, c2, inter);
    bg.stroke(c);
    bg.line(0, i, width, i);
  }

  //Draws the sky
  for (int i = 0; i <= h/3*2; i++) {
    float inter = map(i, 0, h/3*2, 0, 2);
    int c = lerpColor(c2, c3, inter);
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

  for (int i = 0; i < PApplet.parseInt(random(25, 45)); i++) {
    int r = PApplet.parseInt(random(1, 2));
    bg.ellipse(random(15, width-15), random(15, h-15), r, r);
  }

  bg.endDraw();

  return bg;
}

public PGraphics drawMainBG() {

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

public PGraphics drawBlack() {

  //Draws just a black screen to avoid bugs
  PGraphics bg = createGraphics(width, height);
  bg.beginDraw();
  bg.background(0);
  bg.endDraw();

  return bg;
}
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
  public void reset() {
    shape.resetMatrix();
  }

  //Renders to the screen
  public void show() {
    shape.translate(0, 0, -100);
    shape(shape, pos.x, pos.y);
  }

  //Just some rotation
  public void rotX(float angle) {
    shape.rotate(angle, 1, 0, 0);
  }

  public void rotY(float angle) {
    shape.rotate(angle, 0, 1, 0);
  }

  public void rotZ(float angle) {
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

  public void update() {
    speed = player.speed.z;
    segment.translate(0, 0, speed);

    bridge.update(speed);
    block.update(speed);
    panel.update(speed);
    net.update(speed);

    pos.z += speed;
  }

  //Renders to the screen
  public void showRoad() {
    shape(floor, 0, 0);
    bridge.show();
    block.show();
  }

  //Renders to the screen
  public void showCeiling() {
    shape(ceiling, 0, 0);
  }

  //Renders to the screen
  public void showObstacle() {
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

  public void create(char type, PVector p) {

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

  public void show() {
    if (shape == null)
      return;
    shape(shape, 0, 0);
  }

  public void update(float s) {
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

  public void update() {
    shape.translate(0, 0, -speed);
    pos.z -= speed;
  }

  public void show() {
    shape(shape, 0, 0);
  }
}
public void createCourse() {

  course = new int[300];
  int[] lines = new int[0];
  switch(diff) {
  case 0:
    lines = PApplet.parseInt(split(loadStrings("easy.trk")[0], ','));
    break;
  case 1:
    lines = PApplet.parseInt(split(loadStrings("medium.trk")[0], ','));
    break;
  case 2:
    lines = PApplet.parseInt(split(loadStrings("medium.trk")[0], ','));
    break;
  case 3:
    generateCourse();
    break;
  }


  for (int i = 0; i < lines.length; i++) {
    course[i] = lines[i];
  }
}

public void generateCourse() {

  for (int i = 0; i<300; i++) {
    course[i] = 0;
  }

  boolean tunnel = false;
  int tunnelLenght = 0;
  int weight = 0;

  for (int i = 0; i<300; i++) {
    if (random(1) > 0.90f && !tunnel) {
      tunnel = true;
      tunnelLenght = PApplet.parseInt(random(0, 20));
    }

    if (tunnel) {
      course[i] += 1;
      tunnelLenght--;

      if (tunnelLenght == 0) {
        tunnel = false;
      }
    }

    if ((course[i] & 1) == 0) {
      if (random(1) > 0.92f && weight == 0) {
        course[i] += 4;
        weight = 2;
      } else if (random(1) > 0.70f && weight == 0) {
        course[i] += 2;
        weight = 2;
      } 
      if (random(1)> 0.8f && (course[i] & 1) == 0 && weight == 0) {
        course[i] += 8;
        weight = 3;
      }
    } else {
      if (random(1) > 0.65f && weight == 0) {

        switch(PApplet.parseInt(random(1, 6))) {
        case 1:
          course[i] += 16;
          weight = 2;
          break;
        case 2:
          course[i] += 32;
          weight = 2;
          if (random(1)> 0.66f) {
            course[i] += 2;
            weight +=2;
          }
          break;
        case 3:
          course[i] += 64;
          weight = 2;
          break;
        case 4:
          course[i] += 128;
          weight = 2;
          break;
        case 5:
          course[i] += 256;
          weight = 3;
          break;
        }
      }
    }

    weight--;
    weight = constrain(weight, 0, 10);
  }

  for (int i = 0; i < 10; i++) {
    course[i] = 0;
  }
}
//The ship shape
public PShape createShip() {

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
public PShape createRoad() {

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
public PShape createCeiling() {
  return createShape(QUADS, GLASS_STROKE, GLASS_FILL, MEDIUM_STROKE, ceilingVertices);
}

//The block obstacle shape
public PShape createBlock() {
  return createShape(QUADS, BLOCK_STROKE, BLOCK_FILL, MEDIUM_STROKE, blockVertices);
}

//The bridge obstacle shape
public PShape createBridge() {
  return createShape(QUADS, BRIDGE_STROKE, BRIDGE_FILL, MEDIUM_STROKE, bridgeVertices);
}

//The panel obstacle shape
public PShape createPanel(char type) {

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
public PShape createNet() {

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
public PShape createFinish() {

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
public PShape createShot() {
  return createShape(QUADS, acidGreen, acidGreen, LIGHT_STROKE, shotVertices);
}

public PShape createShape(int type, int stroke, int fill, int strokeWeight, int[][] vertices) {
  PShape shape = createShape(type, stroke, fill, strokeWeight);
  createVertices(shape, vertices);
  shape.endShape(CLOSE);
  return shape;
}

public PShape createShape(int type, int stroke, int fill, int strokeWeight) {
  PShape shape = createShape();
  shape.beginShape(type);
  shape.stroke(stroke);
  shape.fill(fill);
  shape.strokeWeight(strokeWeight);
  return shape;
}

public void createVertices(PShape shape, int[][] vertices) {
  createVertices(shape, vertices, ' ');
}

public void createVertices(PShape shape, int[][] vertices, char reverse) {
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
int[][] wingVertices = {
  {20, 10, 70, 20, 0, 70, 75, 5, 70, 75, 10, 70}, 
  {75, 5, 70, 75, 10, 70, 75, 10, 40, 75, 5, 40}, 
  {75, 10, 40, 75, 5, 40, 70, 5, 40, 70, 10, 40}, 
  {70, 10, 40, 70, 5, 40, 70, 5, 50, 70, 10, 50}, 
  {70, 10, 50, 70, 5, 50, 20, 0, -10, 20, 10, -10}, 
  {20, 0, -10, 70, 5, 50, 70, 5, 70, 20, 0, 70}, 
  {70, 5, 40, 75, 5, 40, 75, 5, 70, 70, 5, 70}, 
  {20, 10, -10, 70, 10, 50, 70, 10, 70, 20, 10, 70}, 
  {70, 10, 40, 75, 10, 40, 75, 10, 70, 70, 10, 70}
};

int[][] bodyVertices = {
  {5, -5, -70, 5, 15, -70, -5, 15, -70, -5, -5, -70}, 
  {-5, 15, -70, -5, -5, -70, -15, -5, -50, -15, 15, -50}, 
  {-15, 15, -50, -15, -5, -50, -20, -5, -30, -20, 15, -30}, 
  {-20, 15, -30, -20, -5, -30, -20, -5, 70, -20, 15, 70}, 
  {-20, 15, 70, -20, -5, 70, 20, -5, 70, 20, 15, 70}, 
  {20, 15, 70, 20, -5, 70, 20, -5, -30, 20, 15, -30}, 
  {20, 15, -30, 20, -5, -30, 15, -5, -50, 15, 15, -50}, 
  {15, 15, -50, 15, -5, -50, 5, -5, -70, 5, 15, -70}, 
  {-20, -5, -30, -10, -15, -30, -10, -15, 70, -20, -5, 70}, 
  {-20, -5, 70, -10, -15, 70, 10, -15, 70, 20, -5, 70}, 
  {20, -5, 70, 10, -15, 70, 10, -15, -30, 20, -5, -30}, 
  {10, -15, -30, -10, -15, -30, -10, -15, 70, 10, -15, 70}, 
  {5, 15, -70, -5, 15, -70, -15, 15, -50, 15, 15, -50}, 
  {-15, 15, -50, 15, 15, -50, 20, 15, -30, -20, 15, -30}, 
  {-20, 15, -30, 20, 15, -30, 20, 15, 70, -20, 15, 70}
};

int[][] cockPitVertices = {
  {-5, -5, -70, -5, -15, -50, -5, -15, -50, -15, -5, -50}, 
  {-15, -5, -50, -5, -15, -50, -10, -15, -30, -20, -5, -30}, 
  {20, -5, -30, 10, -15, -30, 5, -15, -50, 15, -5, -50}, 
  {15, -5, -50, 5, -15, -50, 5, -15, -50, 5, -5, -70}, 
  {5, -5, -70, 5, -15, -50, -5, -15, -50, -5, -5, -70}, 
  {5, -15, -50, -5, -15, -50, -10, -15, -30, 10, -15, -30}
};

int[][] roadVertices = {
  {-750, 0, -400, -750, 0, 0, -450, 0, 0, -450, 0, -400}, 
  {-450, 0, 0, -450, 0, -400, -250, 200, -400, -250, 200, 0}, 
  {-250, 200, -400, -250, 200, 0, 250, 200, 0, 250, 200, -400}, 
  {250, 200, 0, 250, 200, -400, 450, 0, -400, 450, 0, 0}, 
  {450, 0, -400, 450, 0, 0, 750, 0, 0, 750, 0, -400}
};

int[][] lowerPanelVertices = {
  {-250, 200, -20, -250, 0, -20, -450, 0, -20, -250, 200, -20}, 
  {-250, 0, -20, -450, 0, -20, -450, -200, -20, -250, -200, -20}, 
  {-250, 200, -20, 250, 200, -20, 250, -200, -20, -250, -200, -20}, 
  {250, 200, -20, 250, 0, -20, 450, 0, -20, 250, 200, -20}, 
  {250, 0, -20, 450, 0, -20, 450, -200, -20, 250, -200, -20}
};
int[][] higerPanelVertices = {
  {-250, -500, -20, -250, -250, -20, -450, -250, -20, -250, -500, -20}, 
  {-250, -250, -20, -450, -250, -20, -450, 0, -20, -250, 0, -20}, 
  {-250, -500, -20, 250, -500, -20, 250, 0, -20, -250, 0, -20}, 
  {250, -500, -20, 250, -250, -20, 450, -250, -20, 250, -500, -20}, 
  {250, -250, -20, 450, -250, -20, 450, 0, -20, 250, 0, -20}
};
int[][] lateralPanelVertices = {
  {250, -500, -20, 0, -500, -20, 0, 200, -20, 250, 200, -20}, 
  {250, -500, -20, 250, -250, -20, 450, -250, -20, 250, -500, -20}, 
  {450, -250, -20, 250, -250, -20, 250, 0, -20, 450, 0, -20}, 
  {250, 0, -20, 450, 0, -20, 250, 200, -20, 250, 0, -20}
};
int[][] fullPanelVertices = {
  {250, -500, -60, 450, -250, -60, 250, -250, -60, 250, -500, -60}, 
  {-250, -500, -60, -450, -250, -60, -250, -250, -60, -250, -500, -60}, 
  {250, 0, -60, 450, 0, -60, 250, 200, -60, 250, 0, -60}, 
  {-250, 0, -60, -450, 0, -60, -250, 200, -60, -250, 0, -60}, 
  {450, -250, -60, 250, -250, -60, 250, 0, -60, 450, 0, -60}, 
  {-450, -250, -60, -250, -250, -60, -250, 0, -60, -450, 0, -60}, 
  {-250, -500, -60, 250, -500, -60, 250, 200, -60, -250, 200, -60}
};

int[][] shotVertices = {
  {5, -5, -200, 5, 5, -200, 5, 5, 200, 5, -5, 200}, 
  {-5, -5, 200, -5, 5, 200, -5, 5, -200, -5, -5, -200}, 
  {-5, -5, 200, -5, 5, 200, 5, 5, 200, 5, -5, 200}, 
  {-5, -5, -200, -5, 5, -200, 5, 5, -200, 5, -5, -200}, 
  {-5, 5, -200, -5, 5, 200, 5, 5, 200, 5, 5, -200}, 
  {-5, -5, -200, -5, -5, 200, 5, -5, 200, 5, -5, -200}
};

int[][] finishVertices = {
  {-605, -705, 605, -705, 605, -205, -605, -205}
};

int[][] ceilingVertices = {
  {450, 0, 400, 450, 0, -400, 450, -250, -400, 450, -250, 400}, 
  {-450, 0, 400, -450, 0, -400, -450, -250, -400, -450, -250, 400}, 
  {-450, -250, 400, -450, -250, -400, -250, -500, -400, -250, -500, 400}, 
  {-250, -500, 400, -250, -500, -400, 250, -500, -400, 250, -500, 400}, 
  {250, -500, 400, 250, -500, -400, 450, -250, -400, 450, -250, 400}
};

int[][] blockVertices = {
  {-750, 0, 400, -750, 0, 0, 750, 0, 0, 750, 0, 400}, 
  {-750, 0, 0, -450, 200, 0, 450, 200, 0, 750, 0, 0}, 
  {-750, 0, 400, -450, 200, 400, 450, 200, 400, 750, 0, 400}
};

int[][] bridgeVertices = {
  {-750, 0, 400, -750, 0, 0, 750, 0, 0, 750, 0, 400}
};
  public void settings() {  size(1366, 768, P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "NeonLandscape" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
