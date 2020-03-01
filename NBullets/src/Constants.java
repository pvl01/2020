import java.awt.Color;

// to represent world constants
interface IConstants {
  // positional constants
  int WIDTH = 500; // in pixels
  int HEIGHT = 300; // in pixels
  int CENTER_WIDTH = (int) Math.round(WIDTH / 2.0);
  int SPAWN_HEIGHT_MAX = (int) Math.round(HEIGHT / 7.0); // top bound of screen
  int SPAWN_HEIGHT_MIN = HEIGHT - SPAWN_HEIGHT_MAX; // bottom bound of screen
  
  // rate constants
  double TICK_RATE = 1.0 / 28.0; // in seconds per frame
  double SECOND_RATE = Math.pow(TICK_RATE, -1); // in frames per second
  
  // bullet constants
  int MIN_BULLETS = 5;
  int MAX_BULLETS = 10;
  int INITIAL_BULLET_RADIUS = 2; // in pixels
  int BULLET_RADIUS_GROWTH_RATE = 2; // in pixels per explosion
  int MAX_BULLET_RADIUS = 10; // in pixels
  Color BULLET_COLOR = Color.pink;
  int BULLET_SPEED = 8; // in pixels per tick
  
  // ship constants
  int MIN_SHIP_SPAWNS = 2;
  int MAX_SHIP_SPAWNS = 4;
  int SHIP_RADIUS = (int) Math.round(HEIGHT / 30.0);
  Color SHIP_COLOR = Color.cyan;
  int SHIP_SPEED = (int) Math.round(BULLET_SPEED / 2.0);
  
  // text constants
  Color FONT_COLOR = Color.black;
  int FONT_SIZE = 13; // in pixels
}