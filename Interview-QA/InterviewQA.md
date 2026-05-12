# Most Asked Java Interview Q&A

---

## Q1. What is `final`, `finally` & `finalize`?

These three look similar but serve completely different purposes.

---

### `final` — keyword (restricts modification)

Used to make a variable, method, or class unchangeable/non-overridable.

| Applied to | Effect |
|------------|--------|
| Variable   | Value cannot be reassigned (constant) |
| Method     | Cannot be overridden in a subclass |
| Class      | Cannot be subclassed (e.g., `String`, `Integer`) |

**Example:**

```java
// final variable
final int MAX = 100;
// MAX = 200; // ❌ Compilation error

// final method
class Parent {
    final void show() {
        System.out.println("Parent show");
    }
}

class Child extends Parent {
    // void show() { } // ❌ Compilation error — cannot override final method
}

// final class
final class Utility {
    // class body
}

// class Extended extends Utility { } // ❌ Cannot extend final class
```

---

### `finally` — block (guarantees execution)

A block used with `try-catch` that **always executes** regardless of whether an exception was thrown or caught. Used for cleanup — closing connections, releasing resources, etc.

**Example:**

```java
public class FinallyDemo {
    public static void main(String[] args) {
        try {
            int result = 10 / 0; // throws ArithmeticException
        } catch (ArithmeticException e) {
            System.out.println("Exception caught: " + e.getMessage());
        } finally {
            System.out.println("Finally block always runs"); // always prints
        }
    }
}
// Output:
// Exception caught: / by zero
// Finally block always runs
```

**Does `finally` always run?**
- YES — even if there is a `return` inside `try` or `catch`.
- NO — only if `System.exit()` is called or the JVM crashes.

```java
static int test() {
    try {
        return 1;
    } finally {
        System.out.println("finally runs before return"); // still executes!
    }
}
```

---

### `finalize()` — method (deprecated, garbage collection hook)

A method defined in `Object` class, called by the **Garbage Collector** just before an object is destroyed. Used to perform cleanup of native resources.

> **Deprecated since Java 9** — unreliable and unpredictable. Prefer `AutoCloseable` / try-with-resources instead.

**Example:**

```java
public class FinalizeDemo {

    @Override
    protected void finalize() throws Throwable {
        System.out.println("finalize() called before GC collects this object");
    }

    public static void main(String[] args) throws InterruptedException {
        FinalizeDemo obj = new FinalizeDemo();
        obj = null;             // make eligible for GC
        System.gc();            // request GC (not guaranteed to run immediately)
        Thread.sleep(1000);
        System.out.println("Main method ends");
    }
}
// Output (order not guaranteed):
// finalize() called before GC collects this object
// Main method ends
```

---

### Quick Comparison Table

| Feature      | `final`              | `finally`                       | `finalize()`                        |
|--------------|----------------------|---------------------------------|-------------------------------------|
| Type         | Keyword              | Block                           | Method                              |
| Purpose      | Restrict modification| Guaranteed cleanup after try    | Pre-GC cleanup (deprecated)         |
| Applied to   | Variable/Method/Class| try-catch block                 | Object (defined in `Object` class)  |
| Execution    | Compile-time         | Always at runtime               | Called by GC (unpredictable)        |
| Still used?  | Yes                  | Yes                             | Deprecated since Java 9             |

---

## Q2. What is the difference between `==` and `.equals()`?

### `==` — reference comparison

Checks whether two references point to the **same object in memory**.

### `.equals()` — content comparison

Checks whether two objects are **logically equal** (content). By default `Object.equals()` behaves like `==`, but classes like `String`, `Integer` override it to compare values.

**Example:**

```java
public class EqualsDemo {
    public static void main(String[] args) {

        // String pool example
        String a = "hello";
        String b = "hello";
        String c = new String("hello");

        System.out.println(a == b);        // true  — same pool reference
        System.out.println(a == c);        // false — c is a new heap object
        System.out.println(a.equals(c));   // true  — same content

        // Integer cache (-128 to 127)
        Integer x = 100;
        Integer y = 100;
        System.out.println(x == y);        // true  — cached
        System.out.println(x.equals(y));   // true

        Integer p = 200;
        Integer q = 200;
        System.out.println(p == q);        // false — outside cache range
        System.out.println(p.equals(q));   // true
    }
}
```

**Custom `equals()` override:**

```java
class Person {
    String name;
    int age;

    Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person p = (Person) o;
        return age == p.age && name.equals(p.name);
    }
}

// Person p1 = new Person("Alice", 30);
// Person p2 = new Person("Alice", 30);
// p1 == p2      → false (different objects)
// p1.equals(p2) → true  (same content)
```

> **Rule:** Always override `hashCode()` when you override `equals()` — required for correct behavior in `HashMap`, `HashSet`, etc.

---

## Q3. What is the difference between `String`, `StringBuilder`, and `StringBuffer`?

| Feature         | `String`            | `StringBuilder`      | `StringBuffer`       |
|-----------------|---------------------|----------------------|----------------------|
| Mutability      | Immutable           | Mutable              | Mutable              |
| Thread-safe     | Yes (immutable)     | No                   | Yes (synchronized)   |
| Performance     | Slow (creates new objects) | Fast          | Slower than StringBuilder |
| Use case        | Fixed text          | Single-threaded concat | Multi-threaded concat |

**Example:**

```java
public class StringDemo {
    public static void main(String[] args) {

        // String — immutable, each concat creates a new object
        String s = "Hello";
        s += " World"; // new object created
        System.out.println(s); // Hello World

        // StringBuilder — mutable, no new object on append
        StringBuilder sb = new StringBuilder("Hello");
        sb.append(" World");
        sb.insert(5, ",");
        sb.reverse();
        System.out.println(sb); // dlroW ,olleH

        // StringBuffer — same API as StringBuilder but thread-safe
        StringBuffer sbf = new StringBuffer("Hello");
        sbf.append(" World");
        System.out.println(sbf); // Hello World
    }
}
```

**Why is `String` immutable?**
- Security (class names, passwords passed as String cannot be altered mid-call)
- String pool (safe to share references)
- Thread safety by design
- `hashCode` can be cached (used heavily in `HashMap` keys)

---

## Q4. What is the difference between `abstract class` and `interface`?

| Feature              | Abstract Class                        | Interface                              |
|----------------------|---------------------------------------|----------------------------------------|
| Keyword              | `abstract class`                      | `interface`                            |
| Instantiation        | Cannot be instantiated                | Cannot be instantiated                 |
| Methods              | Abstract + concrete methods           | Abstract (default/static since Java 8) |
| Variables            | Any type (instance vars allowed)      | `public static final` only             |
| Constructor          | Can have constructors                 | Cannot have constructors               |
| Inheritance          | Single inheritance only               | Multiple interfaces allowed            |
| Access modifiers     | Any                                   | `public` by default                    |
| Use case             | Shared base with common state/behavior| Define a contract/capability           |

**Example:**

```java
// Abstract class — IS-A relationship with shared state
abstract class Animal {
    String name; // instance variable

    Animal(String name) { this.name = name; }

    abstract void sound(); // subclass must implement

    void breathe() { // concrete — shared behavior
        System.out.println(name + " breathes air");
    }
}

// Interface — CAN-DO contract
interface Swimmable {
    void swim(); // implicitly public abstract
}

interface Flyable {
    default void fly() { // default method since Java 8
        System.out.println("Flying...");
    }
}

// Duck extends one class, implements multiple interfaces
class Duck extends Animal implements Swimmable, Flyable {
    Duck() { super("Duck"); }

    @Override public void sound() { System.out.println("Quack"); }
    @Override public void swim()  { System.out.println("Duck swims"); }
}

// Usage
Duck d = new Duck();
d.sound();   // Quack
d.swim();    // Duck swims
d.fly();     // Flying...
d.breathe(); // Duck breathes air
```

> **Rule of thumb:** Use an **abstract class** when classes share common state or code. Use an **interface** when you want to define a capability/contract that unrelated classes can implement.

---

## Q5. What is the difference between `checked` and `unchecked` exceptions?

### Exception Hierarchy

```
Throwable
├── Error          (JVM-level, don't catch — OutOfMemoryError, StackOverflowError)
└── Exception
    ├── Checked    (must handle — IOException, SQLException, ClassNotFoundException)
    └── RuntimeException (unchecked — NullPointerException, ArrayIndexOutOfBoundsException)
```

| Feature         | Checked Exception                     | Unchecked Exception (RuntimeException) |
|-----------------|---------------------------------------|----------------------------------------|
| Checked by      | Compiler                              | Only at runtime                        |
| Must handle?    | Yes — try-catch or `throws`           | No                                     |
| Examples        | `IOException`, `SQLException`         | `NullPointerException`, `IllegalArgumentException` |
| Cause           | External/recoverable issues           | Programming bugs                       |

**Example:**

```java
import java.io.*;

public class ExceptionDemo {

    // Checked — compiler forces you to declare or handle
    void readFile(String path) throws IOException {
        FileReader fr = new FileReader(path); // throws checked IOException
    }

    // Unchecked — no forced handling
    void divide(int a, int b) {
        System.out.println(a / b); // throws ArithmeticException at runtime if b==0
    }

    public static void main(String[] args) {
        ExceptionDemo demo = new ExceptionDemo();

        // Handling checked exception
        try {
            demo.readFile("missing.txt");
        } catch (IOException e) {
            System.out.println("File not found: " + e.getMessage());
        }

        // Unchecked — optional to catch
        try {
            demo.divide(10, 0);
        } catch (ArithmeticException e) {
            System.out.println("Cannot divide by zero");
        }
    }
}
```

---

## Q6. What is the difference between `throw` and `throws`?

| Feature   | `throw`                               | `throws`                                  |
|-----------|---------------------------------------|-------------------------------------------|
| Purpose   | Actually throws an exception object   | Declares that a method may throw an exception |
| Location  | Inside method body                    | In method signature                       |
| Followed by | An exception instance               | Exception class name(s)                   |

**Example:**

```java
// throws — declaration in signature
public void validate(int age) throws IllegalArgumentException {
    if (age < 0) {
        throw new IllegalArgumentException("Age cannot be negative: " + age); // throw — actual throwing
    }
    System.out.println("Valid age: " + age);
}
```

---

## Q7. What is the Java Memory Model — Stack vs Heap?

| Feature        | Stack                             | Heap                                   |
|----------------|-----------------------------------|----------------------------------------|
| Stores         | Primitive variables, method frames, references | Objects, instance variables  |
| Size           | Small, fixed per thread           | Large, shared across threads           |
| Lifecycle      | Automatically managed (LIFO)      | Managed by Garbage Collector           |
| Access speed   | Faster                            | Slower                                 |
| Thread sharing | Private to each thread            | Shared by all threads                  |
| Error          | `StackOverflowError`              | `OutOfMemoryError`                     |

**Example:**

```java
public class MemoryDemo {
    public static void main(String[] args) {
        int x = 10;               // x stored in Stack
        String name = "Java";     // reference 'name' in Stack, "Java" object in Heap (String pool)
        Person p = new Person();  // reference 'p' in Stack, Person object in Heap
    }
}
```

**Stack Overflow example:**

```java
void recursive() {
    recursive(); // no base case → StackOverflowError
}
```

---

## Q8. What is `static` keyword in Java?

`static` means the member belongs to the **class** rather than any specific instance.

| Applied to     | Meaning                                              |
|----------------|------------------------------------------------------|
| Variable       | Single copy shared across all objects                |
| Method         | Can be called without creating an object             |
| Block          | Runs once when the class is loaded                   |
| Nested class   | Does not need an instance of the outer class         |

**Example:**

```java
public class StaticDemo {

    static int count = 0; // shared across all instances

    static { // static block — runs once at class loading
        System.out.println("Class loaded");
        count = 10;
    }

    String name;

    StaticDemo(String name) {
        this.name = name;
        count++;
    }

    static void showCount() { // static method
        System.out.println("Total objects: " + count);
        // System.out.println(name); // ❌ cannot access instance variable
    }

    public static void main(String[] args) {
        StaticDemo.showCount();       // Total objects: 10
        new StaticDemo("A");
        new StaticDemo("B");
        StaticDemo.showCount();       // Total objects: 12
    }
}
```

---

## Q9. What is method overloading vs method overriding?

| Feature         | Overloading (compile-time polymorphism) | Overriding (runtime polymorphism)       |
|-----------------|-----------------------------------------|-----------------------------------------|
| Definition      | Same method name, different parameters  | Same method name + parameters in subclass |
| Class           | Same class                              | Parent and child class                  |
| Return type     | Can differ                              | Must be same (or covariant)             |
| `static`        | Can overload static methods             | Cannot override static methods          |
| Resolved at     | Compile time                            | Runtime                                 |

**Example:**

```java
// Overloading
class Calculator {
    int add(int a, int b)          { return a + b; }
    double add(double a, double b) { return a + b; }
    int add(int a, int b, int c)   { return a + b + c; }
}

// Overriding
class Shape {
    void draw() { System.out.println("Drawing shape"); }
}

class Circle extends Shape {
    @Override
    void draw() { System.out.println("Drawing circle"); } // runtime decides this
}

Shape s = new Circle();
s.draw(); // Drawing circle  ← runtime polymorphism
```

---

## Q10. What is the difference between `HashMap` and `HashTable`?

| Feature          | `HashMap`                  | `Hashtable`                    |
|------------------|----------------------------|--------------------------------|
| Thread-safe      | No                         | Yes (all methods synchronized) |
| Null keys/values | 1 null key, multiple null values | No null keys or values   |
| Performance      | Faster                     | Slower (due to synchronization)|
| Introduced       | Java 1.2 (Collections)     | Java 1.0 (legacy)              |
| Iterator         | Fail-fast                  | Fail-safe (Enumerator)         |
| Preferred over   | —                          | Use `ConcurrentHashMap` instead|

**Example:**

```java
import java.util.*;

public class MapDemo {
    public static void main(String[] args) {

        // HashMap — allows null
        HashMap<String, Integer> map = new HashMap<>();
        map.put(null, 1);       // ✅
        map.put("a", null);     // ✅
        System.out.println(map);

        // Hashtable — no null
        Hashtable<String, Integer> table = new Hashtable<>();
        // table.put(null, 1);  // ❌ NullPointerException
        // table.put("a", null);// ❌ NullPointerException
        table.put("a", 1);
        System.out.println(table);
    }
}
```

> For thread-safe scenarios, prefer `ConcurrentHashMap` over `Hashtable` — it uses segment-level locking and is far more performant.