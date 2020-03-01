import java.awt.Color;
import javalib.worldimages.*;
import tester.*;

// to represent an entity
abstract class AEntity {
  int x, y; // in pixels
  int dx, dy; // in pixels per second
  Color color;
  boolean hasCollided;

  // returns true if this entity has collided with that entity
  abstract boolean hasCollided(AEntity that);

  // returns true if this entity has collided with that bullet
  abstract boolean hasCollided(CircleEntity that);

  // returns the result of applying the visitor to this entity
  abstract <R> R accept(IEntityVisitor<R> visitor);

  // draws this entity
  abstract WorldImage draw();
}

// to represent the visitor pattern
interface IEntityVisitor<R> {
  // returns the result of applying the circle entity to this visitor
  R visitCircleEntity(CircleEntity c);

  // returns the result of applying the bullet to this visitor
  R visitBullet(Bullet b);

  // returns the result of applying the ship to this visitor
  R visitShip(Ship s);
}

// to represent a circular entity
class CircleEntity extends AEntity {
  int radius;

  // for general purposes
  CircleEntity(int x, int y, int dx, int dy, Color color, boolean hasCollided, int radius) {
    Utils utils = new Utils();
    this.radius = utils.checkRange(radius, 0, radius + 1, "Invalid radius: " + radius);
    this.x = utils.checkRange(x, 0 - radius, IConstants.WIDTH + radius, "Invalid x position: " + x);
    this.y = utils.checkRange(y, IConstants.SPAWN_HEIGHT_MAX - radius,
        IConstants.SPAWN_HEIGHT_MIN + radius, "Invalid y position: " + y);
    this.dx = dx;
    this.dy = dy;
    this.color = color;
    this.hasCollided = hasCollided;
  }

  // for initial spawns
  CircleEntity(int x, int y, int dx, int dy, Color color, int radius) {
    this(x, y, dx, dy, color, false, radius);
  }

  @Override
  public boolean hasCollided(AEntity that) {
    return that.hasCollided(this);
  }

  @Override
  // returns true if this circle entity has collided with that circle entity
  public boolean hasCollided(CircleEntity that) {
    if (Math.hypot(this.x - that.x, this.y - that.y) <= this.radius + that.radius) {
      return true;
    }
    else {
      return false;
    }
  }

  @Override
  // to return the result of applying the visitor to this circle entity
  public <R> R accept(IEntityVisitor<R> visitor) {
    return visitor.visitCircleEntity(this);
  }

  @Override
  public WorldImage draw() {
    return new CircleImage(this.radius, OutlineMode.SOLID, this.color);
  }
}

// to represent a bullet
class Bullet extends CircleEntity {
  int collisions;

  // for general purposes
  Bullet(int x, int y, int dx, int dy, Color color, boolean hasCollided, int radius,
      int collisions) {
    super(x, y, dx, dy, color, hasCollided, radius);
    this.collisions = new Utils().checkRange(collisions, 0, collisions + 1,
        "Invalid number of collisions: " + collisions);
  }

  // for initial bullet spawns
  Bullet() {
    this(IConstants.CENTER_WIDTH, IConstants.SPAWN_HEIGHT_MIN, 0, -IConstants.BULLET_SPEED,
        IConstants.BULLET_COLOR, false, IConstants.INITIAL_BULLET_RADIUS, 0);
  }

  // explodes this bullet into bullets
  IList<AEntity> explode() {
    return new Bullet(this.x, this.y, this.dx, this.dy, this.color, false, this.radius,
        this.collisions + 1).explodeHelp(this.collisions + 2);
  }

  // helps the explode method
  IList<AEntity> explodeHelp(int numToSpawn) {
    if (numToSpawn < 0) {
      throw new IllegalArgumentException("Invalid number of bullets to spawn: " + numToSpawn);
    }
    else if (numToSpawn == 0) {
      return new MtList<AEntity>();
    }
    else {
      double angle = Math.toRadians(numToSpawn * 360 / (this.collisions + 1));
      int radius = Math.min(IConstants.MAX_BULLET_RADIUS,
          this.collisions * IConstants.BULLET_RADIUS_GROWTH_RATE
              + IConstants.INITIAL_BULLET_RADIUS);
      int dx = (int) Math.round(Math.cos(angle) * IConstants.BULLET_SPEED);
      int dy = (int) Math.round(Math.sin(angle) * IConstants.BULLET_SPEED);
      Bullet bullet = new Bullet(this.x, this.y, dx, dy, this.color, false, radius,
          this.collisions);
      return new ConsList<AEntity>(bullet, this.explodeHelp(numToSpawn - 1));
    }
  }

  @Override
  // to return the result of applying the visitor to this bullet
  public <R> R accept(IEntityVisitor<R> visitor) {
    return visitor.visitBullet(this);
  }
}

// to represent a ship
class Ship extends CircleEntity {

  // for general purposes
  Ship(int x, int y, int dx, int dy, Color color, boolean hasCollided, int radius) {
    super(x, y, dx, dy, color, hasCollided, radius);
  }

  // for initial ship spawns
  Ship(int x, int y, int dx) {
    this(x, y, dx, 0, IConstants.SHIP_COLOR, false, IConstants.SHIP_RADIUS);
  }

  @Override
  // to return the result of applying the visitor to this ship
  public <R> R accept(IEntityVisitor<R> visitor) {
    return visitor.visitShip(this);
  }
}

// to represent tests and examples of an entity
class ExamplesEntity {

  ExamplesEntity() {
  }

  // tests the circle entity constructor
  boolean testCircleEntity(Tester t) {
    boolean invalidRadius = t.checkConstructorException(
        new IllegalArgumentException("Invalid radius: -1"), "CircleEntity", 0,
        IConstants.SPAWN_HEIGHT_MIN, 0, 0, IConstants.BULLET_COLOR, -1);
    boolean xTooSmall = t.checkConstructorException(
        new IllegalArgumentException("Invalid x position: " + (-1 - IConstants.MAX_BULLET_RADIUS)),
        "CircleEntity", -1 - IConstants.MAX_BULLET_RADIUS, IConstants.SPAWN_HEIGHT_MIN, 0, 0,
        IConstants.BULLET_COLOR, IConstants.INITIAL_BULLET_RADIUS);
    boolean xTooBig = t.checkConstructorException(
        new IllegalArgumentException(
            "Invalid x position: " + (IConstants.WIDTH + IConstants.MAX_BULLET_RADIUS)),
        "CircleEntity", IConstants.WIDTH + IConstants.MAX_BULLET_RADIUS,
        IConstants.SPAWN_HEIGHT_MIN, 0, 0, IConstants.BULLET_COLOR,
        IConstants.INITIAL_BULLET_RADIUS);
    boolean yTooSmall = t.checkConstructorException(
        new IllegalArgumentException("Invalid y position: " + (-1 - IConstants.MAX_BULLET_RADIUS)),
        "CircleEntity", 0, -1 - IConstants.MAX_BULLET_RADIUS, 0, 0, IConstants.BULLET_COLOR,
        IConstants.INITIAL_BULLET_RADIUS);
    boolean yTooBig = t.checkConstructorException(
        new IllegalArgumentException(
            "Invalid y position: " + (IConstants.SPAWN_HEIGHT_MIN + IConstants.MAX_BULLET_RADIUS)),
        "CircleEntity", 0, IConstants.SPAWN_HEIGHT_MIN + IConstants.MAX_BULLET_RADIUS, 0, 0,
        IConstants.BULLET_COLOR, IConstants.INITIAL_BULLET_RADIUS);
    return invalidRadius && xTooSmall && xTooBig && yTooSmall && yTooBig;
  }

  // tests the bullet constructor
  boolean testBullet(Tester t) {
    boolean invalidCollisions = t.checkConstructorException(
        new IllegalArgumentException("Invalid number of collisions: -1"), "Bullet", 0,
        IConstants.SPAWN_HEIGHT_MIN, 0, 0, IConstants.BULLET_COLOR, false,
        IConstants.INITIAL_BULLET_RADIUS, -1);
    return invalidCollisions;
  }

  // tests the hasCollided method
  boolean testHasCollided(Tester t) {
    boolean twoBulletsCollided = new Bullet().hasCollided(new Bullet());
    boolean twoBulletsNotCollided = new Bullet(IConstants.WIDTH, IConstants.SPAWN_HEIGHT_MIN, 0, 0,
        IConstants.BULLET_COLOR, false, IConstants.INITIAL_BULLET_RADIUS, 0)
            .hasCollided(new Bullet());
    return twoBulletsCollided && !twoBulletsNotCollided;
  }

  // tests the explode method
  boolean testExplode(Tester t) {
    Bullet bullet = new Bullet();
    Bullet bullet1 = new Bullet(bullet.x, bullet.y, IConstants.BULLET_SPEED, 0, bullet.color,
        bullet.hasCollided, bullet.radius + IConstants.BULLET_RADIUS_GROWTH_RATE,
        bullet.collisions + 1);
    Bullet bullet2 = new Bullet(bullet.x, bullet.y, -IConstants.BULLET_SPEED, 0, bullet.color,
        bullet.hasCollided, bullet.radius + IConstants.BULLET_RADIUS_GROWTH_RATE,
        bullet.collisions + 1);
    return t.checkExpect(bullet.explode(),
        new ConsList<Bullet>(bullet1, new ConsList<Bullet>(bullet2, new MtList<Bullet>())));
  }

  // tests the explodeHelp method
  boolean testExplodeHelp(Tester t) {
    IList<Bullet> mt = new MtList<Bullet>();
    Bullet bullet = new Bullet();
    Bullet newBullet = new Bullet(bullet.x, bullet.y, IConstants.BULLET_SPEED, 0, bullet.color,
        bullet.hasCollided, bullet.radius, bullet.collisions);
    return t.checkException(new IllegalArgumentException("Invalid number of bullets to spawn: -1"),
        bullet, "explodeHelp", -1) && t.checkExpect(bullet.explodeHelp(0), mt)
        && t.checkExpect(bullet.explodeHelp(1), new ConsList<Bullet>(newBullet, mt));
  }

  // tests the draw method
  boolean testDraw(Tester t) {
    return t.checkExpect(
        new CircleEntity(0, IConstants.SPAWN_HEIGHT_MIN, 0, 0, Color.black, 1).draw(),
        new CircleImage(1, OutlineMode.SOLID, Color.black))
        && t.checkExpect(new Bullet().draw(),
            new CircleImage(IConstants.INITIAL_BULLET_RADIUS, OutlineMode.SOLID,
                IConstants.BULLET_COLOR))
        && t.checkExpect(
            new Ship(0, IConstants.SPAWN_HEIGHT_MIN, 0, 0, Color.black, false, 1).draw(),
            new CircleImage(1, OutlineMode.SOLID, Color.black));
  }
}