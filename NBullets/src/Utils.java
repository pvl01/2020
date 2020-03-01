import java.awt.Color;
import java.util.function.*;
import tester.*;

// to represent utilities
class Utils {

  Utils() {
  }

  // returns the value if it's valid, else an IllegalArgumentException with the
  // string msg
  int checkRange(int val, int min, int max, String msg) {
    if (this.checkRange(val, min, max)) {
      return val;
    }
    else {
      throw new IllegalArgumentException(msg);
    }
  }

  // returns true if the value is valid
  boolean checkRange(int val, int min, int max) {
    if (val >= min && val <= max) {
      return true;
    }
    else {
      return false;
    }
  }
}

// to represent a function that moves the entity
class MoveEntity implements IEntityVisitor<IList<AEntity>>, Function<AEntity, IList<AEntity>> {

  MoveEntity() {
  }

  @Override
  public IList<AEntity> apply(AEntity e) {
    return e.accept(this);
  }

  @Override
  public IList<AEntity> visitCircleEntity(CircleEntity c) {
    try {
      return new ConsList<AEntity>(
          new CircleEntity(c.x + c.dx, c.y + c.dy, c.dx, c.dy, c.color, c.hasCollided, c.radius),
          new MtList<AEntity>());
    }
    catch (IllegalArgumentException e) {
      return new MtList<AEntity>();
    }
  }

  @Override
  public IList<AEntity> visitBullet(Bullet b) {
    try {
      return new ConsList<AEntity>(new Bullet(b.x + b.dx, b.y + b.dy, b.dx, b.dy, b.color,
          b.hasCollided, b.radius, b.collisions), new MtList<AEntity>());
    }
    catch (IllegalArgumentException e) {
      return new MtList<AEntity>();
    }
  }

  @Override
  public IList<AEntity> visitShip(Ship s) {
    try {
      return new ConsList<AEntity>(
          new Ship(s.x + s.dx, s.y + s.dy, s.dx, s.dy, s.color, s.hasCollided, s.radius),
          new MtList<AEntity>());
    }
    catch (IllegalArgumentException e) {
      return new MtList<AEntity>();
    }
  }
}

// to represent a function that handles entity collisions
class HandleCollision implements IEntityVisitor<IList<AEntity>>, Function<AEntity, IList<AEntity>> {
  IList<AEntity> entities;

  HandleCollision(IList<AEntity> entities) {
    this.entities = entities;
  }

  @Override
  public IList<AEntity> apply(AEntity e) {
    return e.accept(this);
  }

  @Override
  public IList<AEntity> visitCircleEntity(CircleEntity c) {
    if (entities.ormap(e -> e.hasCollided(c) && e != c && e instanceof Bullet)) {
      return new ConsList<AEntity>(new CircleEntity(c.x, c.y, c.dx, c.dy, c.color, true, c.radius),
          new MtList<AEntity>());
    }
    else {
      return new ConsList<AEntity>(c, new MtList<AEntity>());
    }
  }

  @Override
  public IList<AEntity> visitBullet(Bullet b) {
    if (entities.ormap(e -> e.hasCollided(b) && e != b && e instanceof Ship)) {
      return b.explode();
    }
    else {
      return new ConsList<AEntity>(b, new MtList<AEntity>());
    }
  }

  @Override
  public IList<AEntity> visitShip(Ship s) {
    if (entities.ormap(e -> e.hasCollided(s) && e != s && e instanceof Bullet)) {
      return new ConsList<AEntity>(new CircleEntity(s.x, s.y, s.dx, s.dy, s.color, true, s.radius),
          new MtList<AEntity>());
    }
    else {
      return new ConsList<AEntity>(s, new MtList<AEntity>());
    }
  }
}

// to represent a function that counts the entities of the given type
class CountEntity implements IEntityVisitor<Integer>, BiFunction<AEntity, Integer, Integer> {
  String className;

  CountEntity(String className) {
    switch (className) {
    case "CircleEntity":
      this.className = className;
      break;
    case "Bullet":
      this.className = className;
      break;
    case "Ship":
      this.className = className;
      break;
    default:
      throw new IllegalArgumentException("Invalid class name: " + className);
    }
  }

  @Override
  public Integer apply(AEntity e, Integer i) {
    return e.accept(this) + i;
  }

  @Override
  public Integer visitCircleEntity(CircleEntity c) {
    if (this.className.equals("CircleEntity")) {
      return 1;
    }
    else {
      return 0;
    }
  }

  @Override
  public Integer visitBullet(Bullet b) {
    if (this.className.equals("Bullet")) {
      return 1;
    }
    else {
      return 0;
    }
  }

  @Override
  public Integer visitShip(Ship s) {
    if (this.className.equals("Ship")) {
      return 1;
    }
    else {
      return 0;
    }
  }
}

// to represent tests and examples of utilities
class ExamplesUtils {

  ExamplesUtils() {
  }

  // tests the checkRange methods
  boolean testCheckRange(Tester t) {
    Utils utils = new Utils();
    return t.checkExpect(utils.checkRange(1, 0, 2, "Error!"), 1) && utils.checkRange(1, 0, 2)
        && !utils.checkRange(3, 0, 2)
        && t.checkException(new IllegalArgumentException("Error!"), utils, "checkRange", 3, 0, 2,
            "Error!")
        && t.checkException(new IllegalArgumentException("Error!"), utils, "checkRange", -1, 0, 2,
            "Error!");
  }

  // tests the MoveEntity function object
  boolean testMoveEntity(Tester t) {
    CircleEntity circleEntityCanMove = new CircleEntity(0, IConstants.SPAWN_HEIGHT_MAX, 0, 0,
        Color.black, 0);
    CircleEntity circleEntityCannotMove = new CircleEntity(0, IConstants.SPAWN_HEIGHT_MAX, 0,
        -IConstants.WIDTH, Color.black, 0);
    Bullet bulletCanMove = new Bullet();
    Bullet bulletCannotMove = new Bullet(0, IConstants.SPAWN_HEIGHT_MAX, 0, -IConstants.WIDTH,
        IConstants.BULLET_COLOR, false, 0, 0);
    Ship shipCanMove = new Ship(0, IConstants.SPAWN_HEIGHT_MAX, IConstants.SHIP_SPEED);
    Ship shipCannotMove = new Ship(0, IConstants.SPAWN_HEIGHT_MAX, -IConstants.WIDTH);
    IList<AEntity> mt = new MtList<AEntity>();
    MoveEntity moveEntity = new MoveEntity();
    return t.checkExpect(moveEntity.apply(circleEntityCanMove),
        new ConsList<AEntity>(circleEntityCanMove, mt))
        && t.checkExpect(moveEntity.apply(circleEntityCannotMove), mt)
        && t.checkExpect(moveEntity.apply(bulletCanMove),
            new ConsList<AEntity>(
                new Bullet(bulletCanMove.x + bulletCanMove.dx, bulletCanMove.y + bulletCanMove.dy,
                    bulletCanMove.dx, bulletCanMove.dy, bulletCanMove.color,
                    bulletCanMove.hasCollided, bulletCanMove.radius, bulletCanMove.collisions),
                mt))
        && t.checkExpect(moveEntity.apply(bulletCannotMove), mt)
        && t.checkExpect(moveEntity.apply(shipCanMove),
            new ConsList<AEntity>(new Ship(shipCanMove.dx, shipCanMove.y, shipCanMove.dx), mt))
        && t.checkExpect(moveEntity.apply(shipCannotMove), mt);
  }

  // tests the handleCollision function method
  boolean testHandleCollision(Tester t) {
    CircleEntity circleEntity = new CircleEntity(0, IConstants.SPAWN_HEIGHT_MAX, 0, 0, Color.black,
        0);
    Bullet bulletBefore = new Bullet();
    Bullet bulletAfter1 = new Bullet(bulletBefore.x, bulletBefore.y, -bulletBefore.dy, 0,
        bulletBefore.color, false, bulletBefore.radius + IConstants.BULLET_RADIUS_GROWTH_RATE,
        bulletBefore.collisions + 1);
    Bullet bulletAfter2 = new Bullet(bulletBefore.x, bulletBefore.y, bulletBefore.dy, 0,
        bulletBefore.color, false, bulletAfter1.radius, bulletBefore.collisions + 1);
    Ship ship = new Ship(IConstants.CENTER_WIDTH, IConstants.SPAWN_HEIGHT_MIN,
        IConstants.SHIP_SPEED, 0, IConstants.SHIP_COLOR, false, IConstants.SHIP_RADIUS);
    HandleCollision handleCollision = new HandleCollision(new ConsList<AEntity>(new Bullet(),
        new ConsList<AEntity>(ship, new MtList<AEntity>())));
    IList<AEntity> mt = new MtList<AEntity>();
    return t.checkExpect(handleCollision.apply(circleEntity),
        new ConsList<AEntity>(circleEntity, mt))
        && t.checkExpect(handleCollision.apply(bulletBefore),
            new ConsList<AEntity>(bulletAfter1, new ConsList<AEntity>(bulletAfter2, mt)));
  }

  // tests the CalculateScore function object
  boolean testCountEntity(Tester t) {
    CircleEntity circleEntity = new CircleEntity(0, IConstants.SPAWN_HEIGHT_MAX, 0, 0, Color.black,
        false, 0);
    Ship ship = new Ship(0, IConstants.SPAWN_HEIGHT_MAX, IConstants.SHIP_SPEED);
    return t.checkConstructorException(
        new IllegalArgumentException("Invalid class name: InvalidClass"), "CountEntity",
        "InvalidClass") && t.checkExpect(new CountEntity("CircleEntity").apply(circleEntity, 0), 1)
        && t.checkExpect(new CountEntity("CircleEntity").apply(ship, 1), 1)
        && t.checkExpect(new CountEntity("Bullet").apply(new Bullet(), 1), 2)
        && t.checkExpect(new CountEntity("Bullet").apply(ship, 0), 0)
        && t.checkExpect(new CountEntity("Ship").apply(ship, 0), 1)
        && t.checkExpect(new CountEntity("Ship").apply(circleEntity, 0), 0);
  }
}