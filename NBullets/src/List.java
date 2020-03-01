import java.util.function.*;
import tester.*;

// to represent a list
interface IList<T> {
  // applies a function to each element in this list
  <R> IList<R> map(Function<T, R> f);

  // produces a list that satisfies the predicate
  IList<T> filter(Predicate<T> p);

  // returns true iff the predicate holds true for at least element in this list
  boolean ormap(Predicate<T> p);

  // folds this list with a function from right to left
  <R> R foldr(BiFunction<T, R, R> f, R base);

  // appends a list to this list
  IList<T> append(IList<T> l);

  // returns true if this list is empty
  boolean isEmpty();
  
  // returns the length of this list
  int length();
}

// to represent a non-empty list
class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  @Override
  public <R> IList<R> map(Function<T, R> f) {
    return new ConsList<R>(f.apply(this.first), this.rest.map(f));
  }

  @Override
  public IList<T> filter(Predicate<T> p) {
    if (p.test(this.first)) {
      return new ConsList<T>(this.first, this.rest.filter(p));
    }
    else {
      return this.rest.filter(p);
    }
  }

  @Override
  public boolean ormap(Predicate<T> p) {
    return p.test(this.first) || this.rest.ormap(p);
  }

  @Override
  public <R> R foldr(BiFunction<T, R, R> f, R base) {
    return f.apply(this.first, this.rest.foldr(f, base));
  }

  @Override
  public IList<T> append(IList<T> l) {
    return new ConsList<T>(this.first, this.rest.append(l));
  }

  @Override
  public boolean isEmpty() {
    return false;
  }
  
  @Override
  public int length() {
    return this.rest.length() + 1;
  }
}

// to represent an empty list
class MtList<T> implements IList<T> {

  MtList() {
  }

  @Override
  public <R> IList<R> map(Function<T, R> f) {
    return new MtList<R>();
  }

  @Override
  public IList<T> filter(Predicate<T> pred) {
    return this;
  }

  @Override
  public boolean ormap(Predicate<T> pred) {
    return false;
  }

  @Override
  public <R> R foldr(BiFunction<T, R, R> f, R base) {
    return base;
  }

  @Override
  public IList<T> append(IList<T> l) {
    return l;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public int length() {
    return 0;
  }
}

//to represent a function that flattens a list
class AppendList<T> implements BiFunction<IList<T>, IList<T>, IList<T>> {

  AppendList() {
  }

  @Override
  public IList<T> apply(IList<T> current, IList<T> soFar) {
    return current.append(soFar);
  }
}

// to represent tests and examples of a list
class ExamplesList {

  ExamplesList() {
  }

  IList<Integer> mtNums = new MtList<Integer>();
  IList<Integer> nums = new ConsList<Integer>(1,
      new ConsList<Integer>(2, new ConsList<Integer>(3, mtNums)));

  IList<String> mtNames = new MtList<String>();
  IList<String> names = new ConsList<String>("Bob", new ConsList<String>("Sarah", mtNames));

  // tests the map method
  boolean testMap(Tester t) {
    return t.checkExpect(mtNums.map(n -> +1), mtNums)
        && t.checkExpect(nums.map(n -> n + 1),
            new ConsList<Integer>(2, new ConsList<Integer>(3, new ConsList<Integer>(4, mtNums))))
        && t.checkExpect(mtNames.map(s -> s.length()), mtNums) && t.checkExpect(
            names.map(s -> s.length()), new ConsList<Integer>(3, new ConsList<Integer>(5, mtNums)));
  }

  // tests the filter method
  boolean testFilter(Tester t) {
    return t.checkExpect(mtNums.filter(n -> n == 1), mtNums)
        && t.checkExpect(nums.filter(n -> n == 1), new ConsList<Integer>(1, mtNums))
        && t.checkExpect(mtNames.filter(s -> s.equals("Bob")), mtNames)
        && t.checkExpect(names.filter(s -> s.equals("Bob")), new ConsList<String>("Bob", mtNames));
  }

  // tests the ormap method
  boolean testOrmap(Tester t) {
    return t.checkExpect(mtNums.ormap(n -> n == 1), false)
        && t.checkExpect(nums.ormap(n -> n == 4), false)
        && t.checkExpect(mtNames.ormap(s -> s.equals("Bob")), false)
        && t.checkExpect(names.ormap(s -> s.equals("Bob")), true);
  }

  // tests the foldr method
  boolean testFoldr(Tester t) {
    return t.checkExpect(mtNums.foldr((n1, n2) -> n1 + n2, 0), 0)
        && t.checkExpect(nums.foldr((n1, n2) -> n1 + n2, 0), 6)
        && t.checkExpect(mtNames.foldr((s1, s2) -> s1 + s2, ""), "")
        && t.checkExpect(names.foldr((s1, s2) -> s1 + s2, ""), "BobSarah");
  }

  // tests the append method
  boolean testAppend(Tester t) {
    return t.checkExpect(mtNums.append(nums), nums)
        && t.checkExpect(nums.append(nums),
            new ConsList<Integer>(1, new ConsList<Integer>(2, new ConsList<Integer>(3, nums))))
        && t.checkExpect(mtNames.append(names), names) && t.checkExpect(names.append(names),
            new ConsList<String>("Bob", new ConsList<String>("Sarah", names)));
  }

  // tests the isEmpty method
  boolean testIsEmpty(Tester t) {
    return t.checkExpect(mtNums.isEmpty(), true) && t.checkExpect(nums.isEmpty(), false)
        && t.checkExpect(mtNames.isEmpty(), true) && t.checkExpect(names.isEmpty(), false);
  }

  // test the AppendList<T> function object
  boolean testAppendList(Tester t) {
    AppendList<Integer> appendNums = new AppendList<Integer>();
    AppendList<String> appendNames = new AppendList<String>();
    IList<IList<Integer>> mtListOfListOfNums = new MtList<IList<Integer>>();
    IList<IList<Integer>> consListOfListOfNums = new ConsList<IList<Integer>>(this.nums,
        new MtList<IList<Integer>>());
    IList<IList<String>> mtListOfListOfNames = new MtList<IList<String>>();
    IList<IList<String>> consListOfListOfNames = new ConsList<IList<String>>(this.names,
        new MtList<IList<String>>());
    return t.checkExpect(mtListOfListOfNums.foldr(appendNums, this.mtNums), this.mtNums)
        && t.checkExpect(consListOfListOfNums.foldr(appendNums, this.mtNums), this.nums)
        && t.checkExpect(mtListOfListOfNames.foldr(appendNames, this.mtNames), this.mtNames)
        && t.checkExpect(consListOfListOfNames.foldr(appendNames, this.mtNames), this.names);
  }
}
