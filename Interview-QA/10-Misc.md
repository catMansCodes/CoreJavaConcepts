# Misc — Object class, Inner classes, Generics, GC, Reflection | Java Interview Guide (8–10 Years Experience)

## Quick Reference

```
Object methods:  toString, equals, hashCode, clone, wait, notify, notifyAll, getClass, finalize
Inner classes:   Member (non-static), Static nested, Local, Anonymous
Generics:        Type erasure — T removed at runtime; bounded wildcards <? extends T> <? super T>
Reflection:      Class.forName(), getMethod(), invoke() — runtime class inspection
GC:              Minor (Young), Major (Old), Full (both) — see 08-JVM-Memory.md for detail
```

---

## Q1. What methods does the `Object` class define?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Every Java class implicitly extends `Object`. Key methods:

| Method | Signature | Purpose |
|--------|-----------|---------|
| `toString()` | `String toString()` | String representation |
| `equals()` | `boolean equals(Object o)` | Logical equality |
| `hashCode()` | `int hashCode()` | Hash code for collections |
| `clone()` | `protected Object clone()` | Shallow copy |
| `getClass()` | `Class<?> getClass()` | Runtime class |
| `wait()` | `void wait()` | Release lock, wait for notify |
| `wait(ms)` | `void wait(long ms)` | Timed wait |
| `notify()` | `void notify()` | Wake one waiting thread |
| `notifyAll()` | `void notifyAll()` | Wake all waiting threads |
| `finalize()` | `protected void finalize()` | Pre-GC cleanup (deprecated) |

```java
Object obj = new Object();

System.out.println(obj.getClass().getName()); // java.lang.Object
System.out.println(obj.getClass().getSimpleName()); // Object
System.out.println(obj.getClass().isArray()); // false

System.out.println(obj.toString()); // java.lang.Object@1b6d3586
System.out.println(obj.hashCode()); // identity hash code (integer)

Object obj2 = obj;
System.out.println(obj.equals(obj2)); // true (same reference)
System.out.println(obj.equals(new Object())); // false (different objects)
```

---

## Q2. What is the `equals()` and `hashCode()` contract?

**Difficulty:** Senior | **Type:** Theory (most asked)

**Answer:**

**`equals()` contract:**
1. Reflexive: `x.equals(x)` = true
2. Symmetric: `x.equals(y)` ↔ `y.equals(x)`
3. Transitive: if `x.equals(y)` && `y.equals(z)` then `x.equals(z)`
4. Consistent: same result on repeated calls (no state change)
5. Null-safe: `x.equals(null)` = false

**`hashCode()` contract:**
- If `x.equals(y)` then `x.hashCode() == y.hashCode()` (MUST)
- If `x.hashCode() == y.hashCode()` then `x.equals(y)` MAY or MAY NOT be true (collision OK)

```java
import java.util.Objects;

class Student {
    String name;
    int rollNo;

    Student(String name, int rollNo) { this.name = name; this.rollNo = rollNo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                  // reflexive + optimization
        if (!(o instanceof Student s)) return false;  // null-safe + type check
        return rollNo == s.rollNo && Objects.equals(name, s.name); // field comparison
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rollNo); // same fields as equals!
    }
}

// Why hashCode matters in collections
Set<Student> students = new HashSet<>();
students.add(new Student("Alice", 1));
students.add(new Student("Alice", 1)); // duplicate

System.out.println(students.size()); // 1 if equals+hashCode correct, 2 if not
```

---

## Q3. What is `clone()`? Deep vs shallow copy.

**Difficulty:** Senior | **Type:** Theory

**Answer:**

See detailed coverage in [01-OOPS.md](01-OOPS.md) Q17.

```java
// Quick recap
class Point implements Cloneable {
    int x, y;
    @Override protected Point clone() throws CloneNotSupportedException {
        return (Point) super.clone(); // shallow
    }
}

class Line implements Cloneable {
    Point start, end;

    // Deep clone — manually clone nested objects
    @Override protected Line clone() throws CloneNotSupportedException {
        Line copy = (Line) super.clone();
        copy.start = start.clone(); // deep copy of nested Point
        copy.end = end.clone();
        return copy;
    }
}

// Modern alternatives to clone()
// 1. Copy constructor
Line(Line other) {
    this.start = new Point(other.start.x, other.start.y);
    this.end = new Point(other.end.x, other.end.y);
}

// 2. Serialization for deep copy (slow but simple)
@SuppressWarnings("unchecked")
static <T extends Serializable> T deepCopy(T original) throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    new ObjectOutputStream(bos).writeObject(original);
    return (T) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
}
```

---

## Q4. What are inner classes? Explain all four types.

**Difficulty:** Senior | **Type:** Theory

**Answer:**

```java
class Outer {
    private int x = 10;

    // 1. Member inner class (non-static) — has access to outer's private members
    class Inner {
        void display() { System.out.println("x = " + x); } // accesses outer.x
    }

    // 2. Static nested class — no access to outer's instance members
    static class StaticNested {
        void display() {
            // System.out.println(x); // ❌ cannot access instance member
            System.out.println("Static nested");
        }
    }

    void method() {
        int localVar = 5; // effectively final

        // 3. Local inner class — inside a method
        class Local {
            void display() {
                System.out.println("local: " + localVar + ", x: " + x); // both accessible
            }
        }
        new Local().display();

        // 4. Anonymous inner class — one-time use, no name
        Runnable r = new Runnable() {
            @Override public void run() { System.out.println("Anonymous, x=" + x); }
        };
        r.run();

        // Lambda — functional anonymous (Java 8+)
        Runnable lambda = () -> System.out.println("Lambda, x=" + x);
        lambda.run();
    }
}

// Usage
Outer outer = new Outer();
Outer.Inner inner = outer.new Inner();  // needs outer instance
inner.display();

Outer.StaticNested sn = new Outer.StaticNested(); // no outer instance needed
sn.display();
```

---

## Q5. What is the difference between static nested class and inner class?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| | Inner Class (non-static member) | Static Nested Class |
|-|--------------------------------|---------------------|
| Instance needed | Yes — inner instance tied to outer instance | No — independent of outer instance |
| Outer member access | All (including private instance) | Only static members |
| Memory | Holds implicit reference to outer | No outer reference |
| Instantiation | `outer.new Inner()` | `new Outer.Static()` |
| Use case | Iterator, Builder that accesses outer fields | Logically grouped helper class |

```java
// Inner class creates implicit reference to outer — memory leak risk!
class View {
    class ClickListener { // holds View reference → View can't be GC'd if listener lives longer
        void onClick() { /* uses View.this */ }
    }
    static class StaticListener { // no View reference — safe
        void onClick() { }
    }
}
```

---

## Q6. What are Generics? What problem do they solve?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Generics provide **compile-time type safety** for collections and algorithms, eliminating the need for casting and preventing `ClassCastException`.

```java
// Before generics (Java 1.4) — unsafe
List list = new ArrayList();
list.add("hello");
list.add(42); // compiles! Mixed types
String s = (String) list.get(1); // ClassCastException at runtime

// With generics — type-safe
List<String> strings = new ArrayList<>();
strings.add("hello");
// strings.add(42); // ❌ compile error — caught early!
String s2 = strings.get(0); // no cast needed

// Generic class
class Pair<A, B> {
    A first; B second;
    Pair(A first, B second) { this.first = first; this.second = second; }
    @Override public String toString() { return "(" + first + ", " + second + ")"; }
}
Pair<String, Integer> pair = new Pair<>("hello", 42);
System.out.println(pair); // (hello, 42)

// Generic method
static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}
System.out.println(max(3, 7));         // 7
System.out.println(max("apple", "banana")); // banana
```

---

## Q7. What is type erasure? What are its implications?

**Difficulty:** Senior | **Type:** Theory + Tricky

**Answer:**

Java generics use **type erasure** — generic type parameters are removed at compile time and replaced with their bounds (or `Object`).

```java
// At compile time:
List<String> strings = new ArrayList<String>();
List<Integer> integers = new ArrayList<Integer>();

// After type erasure (what JVM sees at runtime):
List strings = new ArrayList();  // T → Object
List integers = new ArrayList();

// Implications:
// 1. Cannot check generic type at runtime
List<String> list = new ArrayList<>();
System.out.println(list instanceof List<String>); // ❌ compile error
System.out.println(list instanceof List<?>);      // ✅ OK (unbounded wildcard)
System.out.println(list instanceof List);          // ✅ raw type check

// 2. Cannot create generic array
T[] arr = new T[10]; // ❌ compile error
// Fix: cast from Object array
T[] arr2 = (T[]) new Object[10]; // ✅ with warning

// 3. Cannot use primitives as type args
List<int> ints = new ArrayList<>(); // ❌ must use Integer
List<Integer> ints2 = new ArrayList<>(); // ✅

// 4. Static members cannot use class type parameters
class Foo<T> {
    static T instance; // ❌ compile error — T is per-instance, static is per-class
}

// 5. Overloading with same erasure fails
void process(List<String> l) {}
void process(List<Integer> l) {} // ❌ compile error — same erasure: process(List)
```

---

## Q8. What are wildcards? Explain `? extends T` and `? super T` (PECS).

**Difficulty:** Senior | **Type:** Theory

**Answer:**

**PECS: Producer Extends, Consumer Super**

- `<? extends T>` — **upper bounded** — read from (produce), cannot write (except null)
- `<? super T>` — **lower bounded** — write to (consume), reading gives Object
- `<?>` — **unbounded** — read as Object only

```java
// Upper bounded — read-only (covariance)
void printList(List<? extends Number> list) {
    for (Number n : list) { // can read as Number
        System.out.println(n);
    }
    // list.add(1); // ❌ cannot add — type unknown (Integer? Double? Long?)
}
printList(new ArrayList<Integer>()); // ✅
printList(new ArrayList<Double>());  // ✅

// Lower bounded — write (contravariance)
void addNumbers(List<? super Integer> list) {
    list.add(1);    // ✅ can add Integer (safe — list accepts Integer or supertypes)
    list.add(2);
    // Integer n = list.get(0); // ❌ get returns Object (type unknown)
    Object obj = list.get(0); // ✅
}
addNumbers(new ArrayList<Integer>()); // ✅
addNumbers(new ArrayList<Number>());  // ✅
addNumbers(new ArrayList<Object>());  // ✅

// PECS in action — copy from src to dest
static <T> void copy(List<? extends T> src, List<? super T> dest) {
    for (T item : src) dest.add(item); // src produces T, dest consumes T
}

List<Integer> ints = Arrays.asList(1, 2, 3);
List<Number> nums = new ArrayList<>();
copy(ints, nums); // ✅
```

---

## Q9. What is bounded type parameter vs wildcard?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

```java
// Bounded type parameter — <T extends X> — for defining generic methods
// T is a type variable usable within the method
static <T extends Comparable<T>> T findMax(List<T> list) {
    T max = list.get(0);
    for (T item : list) if (item.compareTo(max) > 0) max = item;
    return max; // can return T — know the exact type
}

// Wildcard — <?> — for using generics (reading existing generic values)
static double sumList(List<? extends Number> list) {
    double sum = 0;
    for (Number n : list) sum += n.doubleValue();
    return sum; // cannot return T — type unknown
}

// When to use which:
// Use T when: method returns the type, or relates two parameters of same type
static <T> void swap(List<T> list, int i, int j) {
    T temp = list.get(i); list.set(i, list.get(j)); list.set(j, temp);
}
// Use ? when: you only care about reading or writing, not the specific type
static void printAll(List<?> list) { list.forEach(System.out::println); }
```

---

## Q10. What is Reflection? How to use it?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Reflection allows runtime inspection and manipulation of classes, methods, fields — even private ones.

```java
import java.lang.reflect.*;

// Get Class object
Class<?> cls = String.class;
Class<?> cls2 = "hello".getClass();
Class<?> cls3 = Class.forName("java.lang.String");

// Inspect class
System.out.println(cls.getName());        // java.lang.String
System.out.println(cls.getSimpleName());  // String
System.out.println(cls.getSuperclass());  // class java.lang.Object
System.out.println(Arrays.toString(cls.getInterfaces())); // [Serializable, Comparable, ...]

// Get and invoke methods
Method method = cls.getMethod("substring", int.class, int.class); // public only
String result = (String) method.invoke("Hello World", 0, 5); // "Hello"

// Access private fields
class Secret { private String value = "hidden"; }
Field field = Secret.class.getDeclaredField("value");
field.setAccessible(true); // bypass access control
Secret s = new Secret();
System.out.println(field.get(s)); // hidden
field.set(s, "changed");
System.out.println(field.get(s)); // changed

// Construct via reflection
Constructor<StringBuilder> ctor = StringBuilder.class.getConstructor(String.class);
StringBuilder sb = ctor.newInstance("Hello");

// Get annotations
Method m = MyClass.class.getMethod("myMethod");
MyAnnotation ann = m.getAnnotation(MyAnnotation.class);

// Get generic type information (despite erasure — from class metadata)
// Field type info is preserved
class Container { List<String> items; }
Field f = Container.class.getDeclaredField("items");
ParameterizedType pt = (ParameterizedType) f.getGenericType();
System.out.println(pt.getActualTypeArguments()[0]); // class java.lang.String
```

**Use cases:** Frameworks (Spring DI, Jackson serialization), testing (Mockito), ORM (Hibernate), plugin systems.

**Cost:** Reflection is 10-50x slower than direct calls. Cache `Method`/`Field` objects, don't call `getMethod()` in hot paths.

---

## Q11. What is `instanceof` and pattern matching? (Java 16+)

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
// Old style — redundant cast
if (obj instanceof String) {
    String s = (String) obj; // cast after check
    System.out.println(s.length());
}

// Java 16+ pattern matching — binds variable in one step
if (obj instanceof String s) { // no separate cast needed
    System.out.println(s.length());
}

// With condition in same expression
if (obj instanceof String s && s.length() > 5) {
    System.out.println("Long string: " + s);
}

// switch pattern matching (Java 21)
String describe(Object obj) {
    return switch (obj) {
        case Integer i -> "Integer: " + i;
        case String s  -> "String: " + s;
        case null      -> "null";
        default        -> "Other: " + obj;
    };
}
```

---

## Q12. What is `Serialization`? How does it work?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

Serialization converts an object to a byte stream; deserialization converts it back.

```java
import java.io.*;

// Must implement Serializable (marker interface)
class Employee implements Serializable {
    private static final long serialVersionUID = 1L; // important for version control
    String name;
    int age;
    transient String password; // transient — NOT serialized
    static String company;    // static — NOT serialized (belongs to class, not object)
}

// Serialize
Employee emp = new Employee("Alice", 30);
emp.password = "secret";
try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("emp.ser"))) {
    oos.writeObject(emp);
}

// Deserialize
try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("emp.ser"))) {
    Employee loaded = (Employee) ois.readObject();
    System.out.println(loaded.name);     // Alice
    System.out.println(loaded.password); // null — transient not serialized
}
```

**`serialVersionUID`:** If not specified, JVM generates one based on class structure. If class changes (field added/removed) without updating `serialVersionUID`, deserialization fails with `InvalidClassException`.

```java
// Custom serialization
class CustomSerial implements Serializable {
    private String name;
    private transient char[] password; // custom handling

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(encode(password)); // encrypt before writing
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        password = decode((String) ois.readObject()); // decrypt on read
    }
}
```

---

## Q13. What is `Comparable` vs `Comparator`? (covered in Collections — summary here)

**Difficulty:** Basic | **Type:** Theory

Quick reference — full coverage in [02-Collections.md](02-Collections.md) Q24.

```java
// Comparable — natural ordering IN the class
class Product implements Comparable<Product> {
    String name; double price;
    @Override public int compareTo(Product other) {
        return Double.compare(this.price, other.price);
    }
}

// Comparator — external, multiple orderings
Comparator<Product> byName  = Comparator.comparing(p -> p.name);
Comparator<Product> byPrice = Comparator.comparingDouble(p -> p.price);
Comparator<Product> byPriceDesc = byPrice.reversed();
Comparator<Product> byNameThenPrice = byName.thenComparing(byPrice);
```

---

## Q14. What is `Enum`? Features beyond constants.

**Difficulty:** Senior | **Type:** Theory

**Answer:**

Enums are full-fledged classes with:
- Constructors (private)
- Fields and methods
- Can implement interfaces
- Cannot extend classes (implicitly extends `Enum`)
- Singleton instances (guaranteed by JVM)

```java
enum Planet {
    MERCURY(3.303e+23, 2.4397e6),
    VENUS  (4.869e+24, 6.0518e6),
    EARTH  (5.976e+24, 6.37814e6);

    private final double mass;   // in kg
    private final double radius; // in meters
    static final double G = 6.67300E-11;

    Planet(double mass, double radius) {
        this.mass = mass; this.radius = radius;
    }

    double surfaceGravity() { return G * mass / (radius * radius); }
    double surfaceWeight(double otherMass) { return otherMass * surfaceGravity(); }
}

// Methods
Planet p = Planet.EARTH;
System.out.println(p.name());    // EARTH
System.out.println(p.ordinal()); // 2 (0-based)

Planet p2 = Planet.valueOf("EARTH"); // from String
Planet[] all = Planet.values();      // all enum constants

// Enum with abstract method
enum Operation {
    PLUS  { @Override public int apply(int x, int y) { return x + y; } },
    MINUS { @Override public int apply(int x, int y) { return x - y; } },
    TIMES { @Override public int apply(int x, int y) { return x * y; } };

    public abstract int apply(int x, int y);
}
System.out.println(Operation.PLUS.apply(3, 4)); // 7

// Enum implements interface
interface Describable { String describe(); }
enum Status implements Describable {
    ACTIVE { public String describe() { return "Active and running"; } },
    INACTIVE { public String describe() { return "Not active"; } };
}
```

---

## Q15. What is `EnumSet` and `EnumMap`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

```java
enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

// EnumSet — ultra-fast Set for enums (uses bit vector internally)
EnumSet<Day> weekdays = EnumSet.of(Day.MON, Day.TUE, Day.WED, Day.THU, Day.FRI);
EnumSet<Day> weekend  = EnumSet.of(Day.SAT, Day.SUN);
EnumSet<Day> allDays  = EnumSet.allOf(Day.class);
EnumSet<Day> none     = EnumSet.noneOf(Day.class);
EnumSet<Day> complement = EnumSet.complementOf(weekdays); // SAT, SUN

System.out.println(weekdays.contains(Day.MON)); // true — O(1)
System.out.println(weekdays); // [MON, TUE, WED, THU, FRI] — always declaration order

// EnumMap — array-backed, fastest map with enum keys
EnumMap<Day, String> schedule = new EnumMap<>(Day.class);
schedule.put(Day.MON, "Standup");
schedule.put(Day.FRI, "Retro");

// Operations are O(1) — no hashing, just ordinal index
System.out.println(schedule.get(Day.MON)); // Standup
```

---

## Q16. What is `var` (local variable type inference)? Java 10+

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`var` lets the compiler infer the type from the initializer. It's still **statically typed** — the type is fixed at compile time.

```java
var list = new ArrayList<String>(); // inferred: ArrayList<String>
var map  = new HashMap<String, Integer>(); // inferred: HashMap<String, Integer>
var msg  = "Hello World"; // inferred: String

// Works in for loops
for (var entry : map.entrySet()) { // inferred: Map.Entry<String, Integer>
    System.out.println(entry.getKey() + "=" + entry.getValue());
}

// With try-with-resources
try (var br = new BufferedReader(new FileReader("file.txt"))) {
    var line = br.readLine(); // String
}

// Cannot use var:
// var x;           // ❌ no initializer
// var x = null;    // ❌ cannot infer from null
// var x = (String) null; // ✅ explicitly cast null
// Fields, method params, return types — ❌ only local variables

// TRICKY: infers most specific type (can be surprising)
var list2 = new ArrayList<>(); // ArrayList<Object> — diamond infers Object!
// Prefer: var list3 = new ArrayList<String>(); // ArrayList<String>
```

---

## Q17. What is the Singleton pattern? (full coverage in Design Patterns — quick version)

**Difficulty:** Senior | **Type:** Theory

```java
// Safest: Enum Singleton (Josh Bloch recommendation)
enum DatabaseConnection {
    INSTANCE;
    public void query(String sql) { /* ... */ }
}
DatabaseConnection.INSTANCE.query("SELECT 1");

// Double-checked locking (lazy + thread-safe)
class Config {
    private static volatile Config instance;
    private Config() { }
    public static Config getInstance() {
        if (instance == null) {
            synchronized (Config.class) {
                if (instance == null) instance = new Config();
            }
        }
        return instance;
    }
}
```

---

## Q18. What is `Iterable` vs `Iterator`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
// Iterable — the for-each contract
// Implementing Iterable allows your class to be used in enhanced for loop
class NumberRange implements Iterable<Integer> {
    private final int start, end;
    NumberRange(int start, int end) { this.start = start; this.end = end; }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            int current = start;

            @Override public boolean hasNext() { return current <= end; }
            @Override public Integer next() {
                if (!hasNext()) throw new NoSuchElementException();
                return current++;
            }
        };
    }
}

// Usage
for (int n : new NumberRange(1, 5)) {
    System.out.print(n + " "); // 1 2 3 4 5
}

// Iterator methods
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String s = it.next();
    if (s.equals("remove")) it.remove(); // safe removal
}
```

---

## Q19. What are annotations? How to create custom annotations?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

Annotations are metadata — they don't directly affect execution but can be read by compiler or frameworks at runtime via reflection.

```java
// Built-in annotations
@Override              // compiler checks you're actually overriding
@Deprecated            // marks API as obsolete
@SuppressWarnings("unchecked") // suppress compiler warnings
@FunctionalInterface   // ensures exactly one abstract method

// Custom annotation
@Retention(RetentionPolicy.RUNTIME)  // visible at runtime (for reflection)
@Target({ElementType.METHOD, ElementType.TYPE}) // where it can be applied
public @interface Logged {
    String level() default "INFO";
    String message() default "";
}

// Usage
@Logged(level = "DEBUG", message = "Processing request")
public void process(Request req) { }

// Read annotation at runtime
Method m = MyClass.class.getMethod("process", Request.class);
if (m.isAnnotationPresent(Logged.class)) {
    Logged logged = m.getAnnotation(Logged.class);
    System.out.println("Level: " + logged.level()); // DEBUG
}

// Annotation retention policies
// SOURCE  — available in source only (e.g., @Override)
// CLASS   — in .class file but not at runtime (default)
// RUNTIME — available at runtime via reflection
```

---

## Q20. What is `Comparable` returned value convention?

**Difficulty:** Tricky | **Type:** Tricky

**Answer:**

```java
// compareTo() contract:
// negative → this < other
// zero     → this == other
// positive → this > other

class Temperature implements Comparable<Temperature> {
    double celsius;
    Temperature(double celsius) { this.celsius = celsius; }

    @Override
    public int compareTo(Temperature other) {
        return Double.compare(this.celsius, other.celsius);
        // Correct! Double.compare handles -0.0, NaN, etc.

        // WRONG: return (int)(this.celsius - other.celsius);
        // Overflow! 2.0 - 3.0 = -1.0 → (int) = -1 ✅ but
        // 2.0 - (-Integer.MAX_VALUE) → overflow → wrong sign!
    }
}

// Tricky: Comparator.compare() should also follow this
Comparator<Integer> wrong = (a, b) -> a - b; // ❌ overflow for extreme values
Comparator<Integer> correct = Integer::compare; // ✅
```

---

## Q21. What is `transient`? What is `volatile`? Are they same?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| | `transient` | `volatile` |
|-|-------------|------------|
| Purpose | Skip during serialization | Ensure visibility across threads |
| Context | Serialization | Concurrency |
| Effect | Field not written to stream | Field bypasses CPU cache |

```java
class User implements Serializable {
    String name;
    transient String password; // NOT serialized — security
    transient Connection conn; // NOT serialized — not serializable anyway
}

class SharedState {
    volatile boolean running = true; // changes immediately visible to all threads
}

// They can be combined:
volatile transient int temp; // volatile for thread safety, transient for serialization
```

---

## Q22. What is `instanceof` chain — design smell?

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

Long `instanceof` chains indicate a missing polymorphic design — replace with method overriding.

```java
// Bad — instanceof chain
double area(Shape s) {
    if (s instanceof Circle) {
        return Math.PI * ((Circle)s).radius * ((Circle)s).radius;
    } else if (s instanceof Rectangle) {
        return ((Rectangle)s).width * ((Rectangle)s).height;
    } else if (s instanceof Triangle) {
        // ...
    }
    throw new IllegalArgumentException("Unknown shape");
}

// Good — polymorphism
abstract class Shape { abstract double area(); }
class Circle extends Shape {
    double radius;
    @Override double area() { return Math.PI * radius * radius; }
}
class Rectangle extends Shape {
    double width, height;
    @Override double area() { return width * height; }
}

// Caller just calls:
double area = shape.area(); // JVM dispatches to correct implementation
```

---

## Q23. What is `Marker Interface`? Examples.

**Difficulty:** Medium | **Type:** Theory

**Answer:**

A **marker interface** has no methods — it's used purely as a tag to give type information to the JVM or frameworks.

```java
// Marker interfaces in JDK
java.io.Serializable   // marks class as serializable
java.lang.Cloneable    // marks class as supporting clone()
java.util.RandomAccess // marks List as supporting O(1) random access
java.rmi.Remote        // marks remote object interface

// Used with instanceof
if (obj instanceof Serializable) {
    // safe to serialize
}

// Modern alternative: Annotations
@interface JsonSerializable { } // annotation as marker — more flexible
@JsonSerializable
class MyData { }
```

---

## Q24. What is autoboxing and unboxing? What are the tricky cases?

**Difficulty:** Medium | **Type:** Tricky

**Answer:**

**Autoboxing** = automatic conversion from primitive to wrapper. **Unboxing** = wrapper to primitive.

```java
Integer a = 5;   // autoboxing: int 5 → Integer.valueOf(5)
int b = a;       // unboxing: a.intValue()

// Integer cache (-128 to 127)
Integer x = 100, y = 100;
Integer p = 200, q = 200;
System.out.println(x == y); // true  — cached
System.out.println(p == q); // false — outside cache range

// TRICKY: null unboxing
Integer n = null;
int i = n; // ❌ NullPointerException — unboxing null calls null.intValue()

// TRICKY: performance in loops
Long sum = 0L;
for (long i = 0; i < 1_000_000; i++) {
    sum += i; // creates 1M Long objects (autoboxing i into sum)
}
// Fix: use primitive long sum = 0L;

// TRICKY: == vs equals
Double d1 = 1.0, d2 = 1.0;
System.out.println(d1 == d2);     // false — Double NOT cached (only Integer -128..127)
System.out.println(d1.equals(d2)); // true

// TRICKY: method overloading
void process(int i) { System.out.println("int"); }
void process(Integer i) { System.out.println("Integer"); }
process(5);          // int — widening preferred over boxing
process(Integer.valueOf(5)); // Integer
```

---

## Q25. What is `Generics` bounded wildcard covariance vs contravariance?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

```java
// Covariance (upper bound) — read-only
List<? extends Animal> animals = new ArrayList<Dog>(); // ✅
Animal a = animals.get(0);  // ✅ read as Animal
// animals.add(new Dog()); // ❌ cannot write (type unknown at compile time)

// Contravariance (lower bound) — write
List<? super Dog> kennel = new ArrayList<Animal>(); // ✅
kennel.add(new Dog()); // ✅ can add Dog (and subtypes)
kennel.add(new Puppy()); // ✅ Puppy IS-A Dog
Object obj = kennel.get(0); // ✅ can only read as Object

// Invariance — neither
List<Animal> exact = new ArrayList<Animal>();
// List<Animal> wrong = new ArrayList<Dog>(); // ❌ compile error

// Why generics are invariant:
// If List<Dog> were a List<Animal>, you could do:
List<Animal> animals2 = new ArrayList<Dog>(); // hypothetically
animals2.add(new Cat()); // add Cat to a Dog list! — type unsafety
```

---

## Q26. What is `java.lang.reflect.Proxy`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`java.lang.reflect.Proxy` creates dynamic proxy objects at runtime that implement specified interfaces. The proxy forwards method calls to an `InvocationHandler`.

```java
interface PaymentService {
    void pay(String user, double amount);
    String status(String txnId);
}

class PaymentServiceImpl implements PaymentService {
    public void pay(String user, double amount) {
        System.out.println("Processing payment: " + amount + " for " + user);
    }
    public String status(String txnId) { return "SUCCESS"; }
}

// Create logging proxy
PaymentService real = new PaymentServiceImpl();
PaymentService proxy = (PaymentService) Proxy.newProxyInstance(
    real.getClass().getClassLoader(),
    new Class<?>[]{ PaymentService.class },
    (proxyObj, method, args) -> {
        System.out.println("[LOG] Calling: " + method.getName());
        Object result = method.invoke(real, args); // delegate to real
        System.out.println("[LOG] Done: " + method.getName());
        return result;
    }
);

proxy.pay("Alice", 500.0);
// [LOG] Calling: pay
// Processing payment: 500.0 for Alice
// [LOG] Done: pay

// Used by: Spring AOP, Spring @Transactional, Mockito mocks, JPA lazy loading
```

---

## Q27. What is `Optional` vs `null`? Anti-patterns.

**Difficulty:** Senior | **Type:** Theory

**Answer:**

```java
// Good uses of Optional
Optional<User> findUser(String email) {
    return Optional.ofNullable(userRepo.findByEmail(email)); // may not exist
}

findUser("alice@example.com")
    .map(User::getName)
    .filter(name -> name.length() > 2)
    .ifPresent(name -> System.out.println("Found: " + name));

// Bad uses of Optional
// 1. As method parameter
void process(Optional<String> name) { } // ❌ use overloads or @Nullable
void process(String name) { }
void process() { }

// 2. As class field
class User { Optional<String> phone; } // ❌ use nullable field with annotation

// 3. Using isPresent() + get() — defeats purpose
if (opt.isPresent()) opt.get(); // ❌ same as null check
opt.ifPresent(v -> use(v));     // ✅

// 4. For collections — return empty collection, not Optional<Collection>
Optional<List<Item>> items(); // ❌
List<Item> items();           // ✅ return Collections.emptyList() if empty
```

---

## Q28. What is `String.intern()` revisited — why use it?

See [05-String-Tricky.md](05-String-Tricky.md) Q8 for full coverage.

Quick reference:
```java
String s1 = new String("hello");
String s2 = new String("hello");
s1.intern() == s2.intern(); // true — both point to pool
// Use for: deduplicating millions of identical strings (e.g., from CSV parsing)
// Don't use for: short-lived strings or unique strings
```

---

## Q29. What is `record`? (Java 16+) Quick overview

**Difficulty:** Senior | **Type:** Theory

Full coverage in [12-Java9-17.md](12-Java9-17.md). Quick summary:

```java
// Record — immutable data carrier (auto-generates constructor, getters, equals, hashCode, toString)
record Point(int x, int y) {}

Point p = new Point(3, 4);
System.out.println(p.x());    // accessor (not getX() — just x())
System.out.println(p);        // Point[x=3, y=4]
System.out.println(p.equals(new Point(3, 4))); // true

// Record features:
// - All fields are private final (auto)
// - Canonical constructor generated
// - equals(), hashCode(), toString() generated from all fields
// - Can add custom methods and constructors
// - Cannot extend other classes
// - Can implement interfaces
```

---

## Q30. What is garbage collection tuning scenario?

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```
Scenario: Service has p99 latency spikes every 2 minutes.

Investigation:
1. Enable GC logging: -Xlog:gc*:file=gc.log
2. Find: Full GC running every 2 minutes, pausing 2-4 seconds
3. Heap dump shows: large objects promoted to Old Gen quickly

Root cause: Objects outliving multiple Minor GCs without dying

Solution options:
A. Increase young gen size: -Xmn1g (more space → fewer promotions)
B. Increase tenure threshold: -XX:MaxTenuringThreshold=15
C. Switch to G1: -XX:+UseG1GC -XX:MaxGCPauseMillis=200
D. Switch to ZGC for < 10ms pauses: -XX:+UseZGC
E. Fix code: reduce long-lived object creation (object pooling, smaller caches)
```

---

## Q31. What is `instanceof` for primitive type checks?

**Difficulty:** Tricky | **Type:** Tricky

**Answer:**

```java
// instanceof with primitives
int i = 5;
// i instanceof Integer // ❌ compile error — cannot use instanceof with primitives

// But autoboxed
Object obj = 5; // autoboxed Integer
System.out.println(obj instanceof Integer); // true
System.out.println(obj instanceof Number);  // true (Integer IS-A Number)
System.out.println(obj instanceof Comparable); // true (Integer implements Comparable)
System.out.println(obj instanceof Long); // false
System.out.println(obj instanceof int);  // ❌ compile error

// null instanceof
Object nullRef = null;
System.out.println(nullRef instanceof String); // false (not NPE — safe check)
System.out.println(null instanceof Object);    // false
```

---

## Q32. Scenario — Implement a generic Stack.

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
class Stack<T> {
    private final List<T> elements = new ArrayList<>();

    void push(T item) {
        elements.add(item);
    }

    T pop() {
        if (isEmpty()) throw new EmptyStackException();
        return elements.remove(elements.size() - 1);
    }

    T peek() {
        if (isEmpty()) throw new EmptyStackException();
        return elements.get(elements.size() - 1);
    }

    boolean isEmpty() { return elements.isEmpty(); }
    int size()       { return elements.size(); }

    @Override public String toString() { return elements.toString(); }
}

// Usage
Stack<Integer> stack = new Stack<>();
stack.push(1); stack.push(2); stack.push(3);
System.out.println(stack.peek()); // 3
System.out.println(stack.pop());  // 3
System.out.println(stack);        // [1, 2]

// Generic bounded stack — only Comparable items
class SortedStack<T extends Comparable<T>> extends Stack<T> {
    // Can now use compareTo() on elements
}
```

---

## Summary — Key Takeaways for Interviews

| Topic | What interviewers test |
|-------|----------------------|
| equals+hashCode | Contract, same fields in both, impact on HashMap/HashSet |
| Type erasure | Cannot do T.class, T[], instanceof List<String> |
| PECS | Producer extends (read), Consumer super (write) |
| Inner class | Non-static holds outer reference (memory leak risk) |
| Reflection | Performance cost; cache Method/Field objects |
| Autoboxing | null unboxing NPE, Integer cache -128..127, == danger |
| Serialization | transient, serialVersionUID, custom read/writeObject |
| Enum | Full class — constructors, methods, implements interfaces |
| var | Compile-time type inference — still static typed |
