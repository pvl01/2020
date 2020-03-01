import java.awt.Color;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.util.Random;
import tester.*;

// to represent the NBullets game
class NBullets extends World {
  int shotsLeft;
  int score; // in number of ships destroyed
  int ticks;
  Random rand;
  IList<AEntity> entities;
  
  public static void main(String[] args) {
    new NBullets().bigBang(IConstants.WIDTH, IConstants.HEIGHT, IConstants.TICK_RATE);
  }

  // for general purposes
  NBullets(int shotsLeft, int score, int ticks, Random rand, IList<AEntity> entities) {
    Utils utils = new Utils();
    this.shotsLeft = utils.checkRange(shotsLeft, 0, shotsLeft + 1,
        "Invalid shots left: " + shotsLeft);
    this.score = score;
    this.ticks = utils.checkRange(ticks, 0, ticks + 1, "Invalid ticks left: " + ticks);
    this.rand = rand;
    this.entities = entities;
  }

  // for testing initialization
  NBullets(Random rand) {
    this(Math.max(rand.nextInt(IConstants.MAX_BULLETS), IConstants.MIN_BULLETS), 0, 0, rand,
        new MtList<AEntity>());
  }

  // for initialization
  NBullets() {
    this(new Random());
  }

  @Override
  public NBullets onTick() {

    IList<AEntity> collidedEntities = this.spawnShips().map(new HandleCollision(this.entities))
        .foldr(new AppendList<AEntity>(), new MtList<AEntity>());
    int score = collidedEntities.filter(e -> e.hasCollided).length() + this.score;
    IList<AEntity> updatedEntities = collidedEntities.filter(e -> !e.hasCollided)
        .map(new MoveEntity()).foldr(new AppendList<AEntity>(), new MtList<AEntity>());
    return new NBullets(this.shotsLeft, score, this.ticks + 1, this.rand, updatedEntities);
  }

  @Override
  public WorldScene makeScene() {
    return this.entities.foldr((e, s) -> s.placeImageXY(e.draw(), e.x, e.y), this.placeScoreText());
  }

  @Override
  // updates the world state based on the key-released event
  public NBullets onKeyReleased(String key) {
    if (key.equals(" ") && this.shotsLeft > 0) {
      IList<AEntity> newEntities = new ConsList<AEntity>(new Bullet(), this.entities);
      return new NBullets(this.shotsLeft - 1, this.score, this.ticks, this.rand, newEntities);
    }
    else {
      return this;
    }
  }

  @Override
  // ends the world if the end conditions are met
  public WorldEnd worldEnds() {
    if (this.shotsLeft == 0 && this.entities.foldr(new CountEntity("Bullet"), 0) == 0) {
      return new WorldEnd(true, this.makeFinalScene());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  // produces the final scene
  WorldScene makeFinalScene() {
    return this.getEmptyScene().placeImageXY(
        new TextImage("You've destroyed " + this.score + "ships!", IConstants.FONT_SIZE,
            IConstants.FONT_COLOR),
        IConstants.CENTER_WIDTH, (int) Math.round(IConstants.HEIGHT / 2.0));
  }

  // places the amount of shots left and current score onto the scene at the lower
  // left corner
  WorldScene placeScoreText() {
    WorldImage text = new TextImage(
        "shots left: " + this.shotsLeft + "; ships destroyed: " + this.score, IConstants.FONT_SIZE,
        IConstants.FONT_COLOR);
    int textX = (int) Math.round(text.getWidth() / 2.0);
    int textY = IConstants.HEIGHT - (int) Math.round(text.getHeight() / 2.0);
    return this.getEmptyScene().placeImageXY(text, textX, textY);
  }

  // returns the new score based on the number of ships destroyed
  int calculateNewScore() {
    return this.entities.filter(e -> e.hasCollided).foldr(new CountEntity("Ship"), this.score);
  }

  // spawns ships for real use
  IList<AEntity> spawnShips() {
    return this.spawnShipsHelp(
        Math.max(this.rand.nextInt(IConstants.MAX_SHIP_SPAWNS), IConstants.MIN_SHIP_SPAWNS));
  }

  // helps the spawnShips method
  IList<AEntity> spawnShipsHelp(int numToSpawn) {
    if (!new Utils().checkRange(numToSpawn, 0, IConstants.MAX_SHIP_SPAWNS)) {
      throw new IllegalArgumentException("Invalid number of ships to spawn: " + numToSpawn);
    }

    if (this.ticks % IConstants.SECOND_RATE == 0 && numToSpawn > 0) {
      boolean spawnOnLeft = rand.nextBoolean();
      int y = Math.max(this.rand.nextInt(IConstants.SPAWN_HEIGHT_MIN), IConstants.SPAWN_HEIGHT_MAX);
      if (spawnOnLeft) {
        Ship newShip = new Ship(0, y, IConstants.SHIP_SPEED);
        return new ConsList<AEntity>(newShip, this.spawnShipsHelp(numToSpawn - 1));
      }
      else {
        Ship newShip = new Ship(IConstants.WIDTH, y, -IConstants.SHIP_SPEED);
        return new ConsList<AEntity>(newShip, this.spawnShipsHelp(numToSpawn - 1));
      }
    }
    else {
      return this.entities;
    }
  }

  // moves all entities
  IList<AEntity> moveAll() {
    return this.entities.map(new MoveEntity()).foldr(new AppendList<AEntity>(),
        new MtList<AEntity>());
  }
}

// to represent examples and tests of the NBullets game
class ExamplesNBullets {

  ExamplesNBullets() {
  }

  // tests the NBullets game
  boolean testBigBang(Tester t) {
    return new NBullets().bigBang(IConstants.WIDTH, IConstants.HEIGHT, IConstants.TICK_RATE);
  }

  // tests the NBullets constructor
  boolean testNBullets(Tester t) {
    IList<AEntity> mt = new MtList<AEntity>();
    return t.checkConstructorException(new IllegalArgumentException("Invalid shots left: -1"),
        "NBullets", -1, 0, 0, new Random(), mt)
        && t.checkConstructorException(new IllegalArgumentException("Invalid ticks left: -1"),
            "NBullets", 0, 0, -1, new Random(), mt)
        && t.checkExpect(new NBullets(new Random(1)),
            new NBullets(
                Math.max(new Random(1).nextInt(IConstants.MAX_BULLETS), IConstants.MIN_BULLETS), 0,
                0, new Random(), new MtList<AEntity>()));
  }

  // tests the makeScene method
  boolean testMakeScene(Tester t) {
    NBullets nb = new NBullets();
    Bullet bullet1 = new Bullet();
    Bullet bullet2 = new Bullet();
    IList<AEntity> twoBullets = new ConsList<AEntity>(bullet1,
        new ConsList<AEntity>(bullet2, new MtList<AEntity>()));
    NBullets nbWithBullet = new NBullets(0, 0, 0, new Random(),
        new ConsList<AEntity>(bullet1, new MtList<AEntity>()));
    NBullets nbWithTwoBullets = new NBullets(0, 0, 0, new Random(), twoBullets);
    WorldScene sceneWithText = nb.placeScoreText();
    return t.checkExpect(nb.makeScene(), sceneWithText)
        && t.checkExpect(nbWithBullet.makeScene(),
            sceneWithText.placeImageXY(bullet1.draw(), bullet1.x, bullet1.y))
        && t.checkExpect(nbWithTwoBullets.makeScene(), nbWithTwoBullets.entities
            .foldr((e, s) -> s.placeImageXY(e.draw(), e.x, e.y), sceneWithText));
  }

  // tests the worldEnds method
  boolean testworldEnds(Tester t) {
    NBullets nb = new NBullets();
    IList<AEntity> mt = new MtList<AEntity>();
    IList<AEntity> singleBulletList = new ConsList<AEntity>(new Bullet(), mt);
    NBullets nbNoShotsLeft = new NBullets(0, 0, 0, new Random(), singleBulletList);
    NBullets nbNoBulletsPresent = new NBullets(1, 0, 0, new Random(), mt);
    NBullets nbBothMet = new NBullets(0, 0, 0, new Random(), mt);
    return t.checkExpect(nb.worldEnds(), new WorldEnd(false, nb.makeScene()))
        && t.checkExpect(nbNoShotsLeft.worldEnds(), new WorldEnd(false, nbNoShotsLeft.makeScene()))
        && t.checkExpect(nbNoBulletsPresent.worldEnds(),
            new WorldEnd(false, nbNoBulletsPresent.makeScene()))
        && t.checkExpect(nbBothMet.worldEnds(), new WorldEnd(true, nbBothMet.makeFinalScene()));
  }

  // tests the makeFinalScene method
  boolean testMakeFinalScene(Tester t) {
    NBullets nb = new NBullets();
    return t.checkExpect(nb.makeFinalScene(),
        nb.getEmptyScene().placeImageXY(
            new TextImage("You've destroyed " + nb.score + "ships!", IConstants.FONT_SIZE,
                IConstants.FONT_COLOR),
            IConstants.CENTER_WIDTH, (int) Math.round(IConstants.HEIGHT / 2.0)));
  }

  // tests the onKeyReleased method
  boolean testOnKeyReleased(Tester t) {
    NBullets nbBefore = new NBullets();
    NBullets nbAfter = new NBullets(nbBefore.shotsLeft - 1, nbBefore.score, nbBefore.ticks,
        new Random(), new ConsList<AEntity>(new Bullet(), nbBefore.entities));
    NBullets nbWithoutShots = new NBullets(0, nbBefore.score, nbBefore.ticks, new Random(),
        nbBefore.entities);
    return t.checkExpect(nbBefore.onKeyReleased("f"), nbBefore)
        && t.checkExpect(nbBefore.onKeyReleased(" "), nbAfter)
        && t.checkExpect(nbWithoutShots.onKeyReleased("g"), nbWithoutShots)
        && t.checkExpect(nbWithoutShots.onKeyReleased(" "), nbWithoutShots);
  }

  // tests the placeScoreText method
  boolean testPlaceScoreText(Tester t) {
    NBullets nb = new NBullets();
    WorldScene emptyScene = nb.getEmptyScene();
    WorldImage defaultText = new TextImage("shots left: " + nb.shotsLeft + "; ships destroyed: 0",
        IConstants.FONT_SIZE, IConstants.FONT_COLOR);
    int defaultTextX = (int) Math.round(defaultText.getWidth() / 2.0);
    int defaultTextY = IConstants.HEIGHT - (int) Math.round(defaultText.getHeight() / 2.0);
    return t.checkExpect(nb.placeScoreText(),
        emptyScene.placeImageXY(defaultText, defaultTextX, defaultTextY));
  }

  // tests the calculateNewScore method
  boolean testCalculateScore(Tester t) {
    Ship collidedShip = new Ship(0, IConstants.SPAWN_HEIGHT_MAX, 0, 0, Color.black, true, 0);
    IList<AEntity> collidedShips = new ConsList<AEntity>(collidedShip,
        new ConsList<AEntity>(collidedShip, new MtList<AEntity>()));
    NBullets twoCollidedShipsNBullets = new NBullets(0, 10, 0, new Random(), collidedShips);
    return t.checkExpect(new NBullets().calculateNewScore(), 0)
        && t.checkExpect(twoCollidedShipsNBullets.calculateNewScore(), 12);
  }

  // tests the spawnShips method
  boolean testSpawnShips(Tester t) {
    NBullets nbRandLocked = new NBullets(new Random(1));
    return t.checkExpect(new NBullets(new Random(1)).spawnShips(), nbRandLocked.spawnShipsHelp(Math
        .max(nbRandLocked.rand.nextInt(IConstants.MAX_SHIP_SPAWNS), IConstants.MIN_SHIP_SPAWNS)));
  }

  // tests the spawnShipsHelp method
  boolean testSpawnShipsHelp(Tester t) {
    IList<AEntity> mt = new MtList<AEntity>();
    NBullets nbRandLocked = new NBullets(new Random(1));
    int y;
    IList<AEntity> newShips;
    if (nbRandLocked.rand.nextBoolean()) {
      y = Math.max(nbRandLocked.rand.nextInt(IConstants.SPAWN_HEIGHT_MIN),
          IConstants.SPAWN_HEIGHT_MAX);
      newShips = new ConsList<AEntity>(new Ship(0, y, IConstants.SHIP_SPEED), mt);
    }
    else {
      y = Math.max(nbRandLocked.rand.nextInt(IConstants.SPAWN_HEIGHT_MIN),
          IConstants.SPAWN_HEIGHT_MAX);
      newShips = new ConsList<AEntity>(new Ship(500, y, -IConstants.SHIP_SPEED), mt);
    }
    return t.checkException(
        new IllegalArgumentException(
            "Invalid number of ships to spawn: " + (IConstants.SPAWN_HEIGHT_MAX + 1)),
        nbRandLocked, "spawnShipsHelp", (IConstants.SPAWN_HEIGHT_MAX + 1))
        && t.checkException(new IllegalArgumentException("Invalid number of ships to spawn: -1"),
            nbRandLocked, "spawnShipsHelp", -1)
        && t.checkExpect(nbRandLocked.spawnShipsHelp(0), mt)
        && t.checkExpect(new NBullets(new Random(1)).spawnShipsHelp(1), newShips);
  }

  // tests the moveAll method
  boolean testMoveAll(Tester t) {
    Bullet bulletBefore = new Bullet();
    Bullet bulletAfter = new Bullet(bulletBefore.x + bulletBefore.dx,
        bulletBefore.y + bulletBefore.dy, bulletBefore.dx, bulletBefore.dy, bulletBefore.color,
        bulletBefore.hasCollided, bulletBefore.radius, bulletBefore.collisions);
    IList<AEntity> entitiesBefore = new ConsList<AEntity>(bulletBefore,
        new ConsList<AEntity>(bulletBefore, new MtList<AEntity>()));
    IList<AEntity> entitiesAfter = new ConsList<AEntity>(bulletAfter,
        new ConsList<AEntity>(bulletAfter, new MtList<AEntity>()));
    Random rand = new Random(1);
    NBullets nbBefore = new NBullets(1, 0, 0, rand, entitiesBefore);
    return t.checkExpect(nbBefore.moveAll(), entitiesAfter);
  }
}