# OOP Concepts — Java Interview Guide (8–10 Years Experience)

## Quick Reference

```
4 Pillars:  Encapsulation | Inheritance | Polymorphism | Abstraction
SOLID:      S-RP | O-CP | L-SP | I-SP | D-IP
Key terms:  IS-A (inheritance) | HAS-A (composition) | CAN-DO (interface)
```

---

## Q1. What are the four pillars of OOP? Explain each with an example.

**Difficulty:** Basic | **Type:** Theory

**Answer:**

| Pillar | Definition | Java Mechanism |
|--------|-----------|----------------|
| Encapsulation | Bundling data + behavior; hide internal state | `private` fields + public getters/setters |
| Inheritance | Child class acquires parent's properties | `extends` |
| Polymorphism | One interface, many implementations | Overloading (compile-time), Overriding (runtime) |
| Abstraction | Hide implementation, expose only what's needed | `abstract class`, `interface` |

```java
// Encapsulation
class BankAccount {
    private double balance; // hidden

    public double getBalance() { return balance; }
    public void deposit(double amount) {
        if (amount > 0) balance += amount;
    }
}

// Inheritance
class SavingsAccount extends BankAccount {
    private double interestRate;
    // inherits deposit(), getBalance()
}

// Polymorphism (runtime)
class Shape { void draw() { System.out.println("Drawing shape"); } }
class Circle extends Shape { @Override void draw() { System.out.println("Drawing circle"); } }

Shape s = new Circle();
s.draw(); // Drawing circle — runtime decision

// Abstraction
interface Payment { void pay(double amount); }
class UpiPayment implements Payment {
    public void pay(double amount) { System.out.println("Paying " + amount + " via UPI"); }
}
```

**Follow-up:** What is the difference between abstraction and encapsulation?
> Encapsulation is about *hiding data* (how it's stored). Abstraction is about *hiding complexity* (how it works). A car's gear box is encapsulated; the steering wheel is the abstraction.

---

## Q2. What is the difference between Abstraction and Encapsulation?

**Difficulty:** Medium | **Type:** Theory + Tricky

**Answer:**

| | Abstraction | Encapsulation |
|-|-------------|---------------|
| What it hides | Implementation details | Internal state / data |
| How achieved | Abstract classes, Interfaces | Access modifiers (private, protected) |
| Focus | Design level — WHAT to do | Implementation level — HOW to protect |
| Example | `List` interface hides ArrayList/LinkedList internals | `private balance` in BankAccount |

```java
// Abstraction — user knows WHAT but not HOW
interface DatabaseRepository {
    void save(Object entity);
    Object findById(int id);
}

// Encapsulation — internal state protected
class Employee {
    private String name;
    private double salary; // nobody modifies this directly

    public void applyRaise(double percent) {
        if (percent > 0 && percent <= 50) {
            salary += salary * (percent / 100);
        }
    }
}
```

**Follow-up:** Can a class be both abstract and encapsulated? Yes — almost all well-designed classes are both.

---

## Q3. What is the difference between IS-A and HAS-A relationships?

**Difficulty:** Medium | **Type:** Theory + Scenario

**Answer:**

- **IS-A** → Inheritance (`extends` / `implements`). Child IS-A type of parent.
- **HAS-A** → Composition/Aggregation. A class HAS-A reference to another class.

```java
// IS-A: Dog IS-A Animal
class Animal { void breathe() { System.out.println("Breathing"); } }
class Dog extends Animal {
    void bark() { System.out.println("Woof"); }
}

// HAS-A: Car HAS-A Engine (composition)
class Engine {
    void start() { System.out.println("Engine started"); }
}

class Car {
    private Engine engine = new Engine(); // HAS-A

    void startCar() {
        engine.start();
        System.out.println("Car moving");
    }
}
```

**Scenario:** Should `Stack` extend `Vector`? No — this is a famous Java design mistake. A Stack IS-NOT-A Vector in a conceptual sense. Composition would have been better.

**Follow-up:** When to prefer composition over inheritance?
> Prefer composition when: the relationship is "uses" not "is", you need flexibility to swap implementations, or to avoid tight coupling.

---

## Q4. What is method overloading vs method overriding? What are the rules?

**Difficulty:** Medium | **Type:** Theory + Tricky

**Answer:**

| Rule | Overloading | Overriding |
|------|------------|-----------|
| Class | Same class | Parent-child |
| Signature | Must differ (params) | Must be identical |
| Return type | Can differ | Must be same or covariant |
| Access | Any | Cannot be more restrictive |
| `static` | Can overload static | Cannot override static (method hiding) |
| `private` | Can overload private | Cannot override private |
| `final` | Can overload final | Cannot override final |
| Resolved | Compile time | Runtime |

```java
class MathUtils {
    // Overloading — compile-time polymorphism
    int add(int a, int b) { return a + b; }
    double add(double a, double b) { return a + b; }
    int add(int a, int b, int c) { return a + b + c; }
}

class Animal {
    Animal create() { return new Animal(); } // return type
}
class Dog extends Animal {
    @Override
    Dog create() { return new Dog(); } // covariant return type — OK since Java 5
}
```

**Tricky:** What is the output?
```java
class Parent {
    static void display() { System.out.println("Parent static"); }
    void show() { System.out.println("Parent instance"); }
}
class Child extends Parent {
    static void display() { System.out.println("Child static"); } // hiding, not overriding
    @Override void show() { System.out.println("Child instance"); }
}

Parent p = new Child();
p.display(); // Parent static  ← static — resolved at compile time by reference type
p.show();    // Child instance ← instance — resolved at runtime by object type
```

---

## Q5. What is the difference between `super` and `this`?

**Difficulty:** Basic | **Type:** Theory

**Answer:**

| | `this` | `super` |
|-|--------|---------|
| Refers to | Current class instance | Parent class |
| Constructor call | `this()` — calls current class constructor | `super()` — calls parent constructor |
| Method call | `this.method()` — current class | `super.method()` — parent class |
| Must be first line | Yes (if used as constructor call) | Yes (if used as constructor call) |

```java
class Vehicle {
    String type;
    Vehicle(String type) { this.type = type; }
    void info() { System.out.println("Vehicle: " + type); }
}

class Car extends Vehicle {
    String brand;

    Car(String brand) {
        super("Car");      // must be first line — calls Vehicle(String)
        this.brand = brand; // this refers to current Car instance
    }

    void info() {
        super.info();                          // calls Vehicle.info()
        System.out.println("Brand: " + brand);
    }
}
```

**Tricky:** Can you use both `this()` and `super()` in the same constructor? No — both must be the first statement, so only one can exist.

---

## Q6. What is a constructor? What are the rules?

**Difficulty:** Basic | **Type:** Theory + Tricky

**Answer:**

Rules:
- Same name as the class, no return type (not even `void`)
- If no constructor defined, compiler adds a default no-arg constructor
- If any constructor is defined, the default is NOT added
- First line must be `this()` or `super()` (compiler inserts `super()` if omitted)
- Cannot be `static`, `final`, `abstract`, or `synchronized`
- Can be `private` (Singleton pattern)

```java
class Singleton {
    private static Singleton instance;
    private Singleton() {} // private constructor

    public static Singleton getInstance() {
        if (instance == null) instance = new Singleton();
        return instance;
    }
}
```

**Tricky — Constructor chaining:**
```java
class Demo {
    Demo() {
        this(10);  // calls Demo(int)
        System.out.println("No-arg");
    }
    Demo(int x) {
        System.out.println("int: " + x);
    }
}
new Demo();
// Output:
// int: 10
// No-arg
```

---

## Q7. What is the difference between composition and aggregation?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Both are HAS-A relationships, but differ in lifecycle dependency:

| | Composition | Aggregation |
|-|-------------|------------|
| Relationship | Strong — part cannot exist without whole | Weak — part can exist independently |
| Lifecycle | Child destroyed when parent is destroyed | Child lives independently |
| Example | House HAS-A Room (room can't exist without house) | University HAS-A Professor (professor can exist without university) |

```java
// Composition — Room cannot exist without House
class House {
    private final Room room; // created inside House
    House() { this.room = new Room(); }
}

// Aggregation — Department references Professor (created externally)
class Professor { String name; }
class Department {
    private Professor professor; // passed in, not created here
    Department(Professor p) { this.professor = p; }
}
```

---

## Q8. What is the SOLID principle? Explain each with a Java example.

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

### S — Single Responsibility Principle (SRP)
A class should have only one reason to change.

```java
// Bad — one class doing too much
class UserService {
    void saveUser(User u) { /* DB logic */ }
    void sendEmail(User u) { /* Email logic */ }
    String formatReport(User u) { /* Report logic */ }
}

// Good — separate concerns
class UserRepository { void save(User u) { } }
class EmailService { void send(User u) { } }
class UserReportService { String format(User u) { return ""; } }
```

### O — Open/Closed Principle (OCP)
Open for extension, closed for modification.

```java
interface Discount { double apply(double price); }
class RegularDiscount implements Discount { public double apply(double p) { return p * 0.9; } }
class PremiumDiscount implements Discount { public double apply(double p) { return p * 0.7; } }
// Adding new discount = new class, not modifying existing ones
```

### L — Liskov Substitution Principle (LSP)
Subclass must be substitutable for its superclass without breaking the program.

```java
// Violation — Square IS-A Rectangle but breaks behavior
class Rectangle {
    int width, height;
    void setWidth(int w)  { width = w; }
    void setHeight(int h) { height = h; }
    int area() { return width * height; }
}
class Square extends Rectangle {
    @Override void setWidth(int w)  { width = height = w; } // breaks Rectangle contract!
    @Override void setHeight(int h) { width = height = h; }
}
Rectangle r = new Square();
r.setWidth(5); r.setHeight(10);
System.out.println(r.area()); // 100, expected 50 — LSP violated
```

### I — Interface Segregation Principle (ISP)
Clients should not be forced to depend on interfaces they don't use.

```java
// Bad — fat interface
interface Worker { void work(); void eat(); void sleep(); }

// Good — segregated
interface Workable { void work(); }
interface Eatable { void eat(); }
class Robot implements Workable { public void work() { } } // no forced eat()
class Human implements Workable, Eatable { public void work() {} public void eat() {} }
```

### D — Dependency Inversion Principle (DIP)
Depend on abstractions, not concretions.

```java
// Bad — high level depends on low level
class OrderService {
    MySqlRepository repo = new MySqlRepository(); // tight coupling
}

// Good — depend on abstraction
interface OrderRepository { void save(Order o); }
class OrderService {
    private final OrderRepository repo;
    OrderService(OrderRepository repo) { this.repo = repo; } // injected
}
```

---

## Q9. What is covariant return type?

**Difficulty:** Medium | **Type:** Theory + Tricky

**Answer:**

Since Java 5, an overriding method can return a subtype of the parent method's return type. This is called **covariant return type**.

```java
class Animal {
    Animal create() { return new Animal(); }
}

class Dog extends Animal {
    @Override
    Dog create() { return new Dog(); } // Dog IS-A Animal — covariant, compiles fine
}

Animal a = new Dog().create(); // returns Dog but assigned as Animal — works
Dog d = new Dog().create();    // also works
```

**Why useful?** Avoids casting at the call site in builder patterns and factory methods.

---

## Q10. Can we override a `private` or `static` method?

**Difficulty:** Tricky | **Type:** Tricky

**Answer:**

- **`private`** — Cannot be overridden. Private methods are not visible to child class. If child defines same signature, it's a new method, not an override.
- **`static`** — Cannot be overridden (it's resolved at compile time). Defining same static method in child is **method hiding**, not overriding. `@Override` annotation will cause a compile error on static methods.

```java
class Parent {
    private void secret() { System.out.println("Parent private"); }
    static void staticMethod() { System.out.println("Parent static"); }
}

class Child extends Parent {
    void secret() { System.out.println("Child secret"); } // new method, NOT override
    static void staticMethod() { System.out.println("Child static"); } // hiding, NOT override
}

Parent p = new Child();
// p.secret() is NOT accessible
// p.staticMethod() → "Parent static" (compile-time resolution)
```

---

## Q11. What is the difference between an interface and an abstract class? When to use which?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| Feature | Abstract Class | Interface |
|---------|---------------|-----------|
| Instantiation | No | No |
| Methods | Abstract + concrete | Abstract + default + static (Java 8+) |
| Variables | Any (instance vars) | `public static final` only |
| Constructor | Yes | No |
| Multiple inheritance | No | Yes (multiple interfaces) |
| Access modifiers | Any | `public` by default |
| Use for | IS-A + shared state/code | CAN-DO contract across unrelated types |

**When to choose abstract class:**
- Classes share common code/state
- You need protected/package-private members
- You want to provide template methods

**When to choose interface:**
- Unrelated classes share a behavior (e.g., `Comparable`, `Serializable`)
- You need multiple inheritance of type
- You want to define an API contract

```java
// Abstract class — shared behavior + state
abstract class Vehicle {
    int speed;
    abstract void fuelType();
    void accelerate() { speed += 10; } // shared
}

// Interface — capability
interface Electric { void charge(); }
interface Autonomous { void selfDrive(); }

class Tesla extends Vehicle implements Electric, Autonomous {
    public void fuelType()  { System.out.println("Electric"); }
    public void charge()    { System.out.println("Charging"); }
    public void selfDrive() { System.out.println("Self-driving"); }
}
```

---

## Q12. What are `default` and `static` methods in interfaces? Why were they added?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Added in **Java 8** to allow adding new methods to interfaces without breaking existing implementations.

- **`default`** — Has a body, can be overridden by implementing class. Called on instance.
- **`static`** — Has a body, belongs to interface, cannot be overridden. Called on interface name.

```java
interface Greeter {
    void greet(String name); // abstract

    default void greetAll(String... names) { // default
        for (String n : names) greet(n);
    }

    static Greeter formal() { // static factory
        return name -> System.out.println("Dear " + name);
    }
}

class SimpleGreeter implements Greeter {
    public void greet(String name) { System.out.println("Hello, " + name); }
    // greetAll() inherited — or can override
}

Greeter g = new SimpleGreeter();
g.greetAll("Alice", "Bob"); // Hello, Alice \n Hello, Bob

Greeter f = Greeter.formal(); // static method on interface
f.greet("Mr. Smith");        // Dear Mr. Smith
```

**Diamond problem with default methods:**
```java
interface A { default void hello() { System.out.println("A"); } }
interface B { default void hello() { System.out.println("B"); } }
class C implements A, B {
    public void hello() { A.super.hello(); } // must explicitly resolve
}
```

---

## Q13. What is the diamond problem? How does Java handle it?

**Difficulty:** Senior | **Type:** Theory + Tricky

**Answer:**

The diamond problem occurs when a class inherits the same method from multiple sources, creating ambiguity.

Java's resolution rules for `default` method conflicts:
1. **Class/overriding method wins** — a concrete method in a class always wins over interface defaults.
2. **More specific interface wins** — if one interface extends another, the subinterface wins.
3. **Explicitly override** — if ambiguous (two unrelated interfaces), the class must override and choose.

```java
interface Flyable  { default String move() { return "Flying"; } }
interface Swimmable { default String move() { return "Swimming"; } }

// Rule 3: class must resolve
class Duck implements Flyable, Swimmable {
    @Override
    public String move() {
        return Flyable.super.move() + " and " + Swimmable.super.move();
    }
}

System.out.println(new Duck().move()); // Flying and Swimming
```

**Note:** Java does NOT have multiple class inheritance — this avoids the classic diamond problem that C++ has.

---

## Q14. What is the difference between aggregation and inheritance? When should you prefer one over the other?

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

**Scenario:** You are designing a `Stack` class. Should it extend `ArrayList` or use one internally?

```java
// Bad — inheritance (Stack IS-A ArrayList? No!)
class BadStack extends ArrayList<Integer> {
    // Exposes add(index, element), set(index, element), etc.
    // Stack should not allow insertion at arbitrary positions
}

// Good — composition (Stack HAS-A list internally)
class GoodStack {
    private final LinkedList<Integer> storage = new LinkedList<>();

    public void push(int val) { storage.addFirst(val); }
    public int pop()  { return storage.removeFirst(); }
    public int peek() { return storage.getFirst(); }
    public boolean isEmpty() { return storage.isEmpty(); }
}
```

**Rule:** Favor composition over inheritance when:
- The "IS-A" relationship isn't truly permanent across all use contexts
- You want to hide the implementation entirely
- You need to swap the underlying implementation later

---

## Q15. What is polymorphism? Explain compile-time vs runtime polymorphism.

**Difficulty:** Medium | **Type:** Theory + Tricky

**Answer:**

**Polymorphism** = "many forms" — the ability to process objects differently based on their type.

| | Compile-time (Static) | Runtime (Dynamic) |
|-|-----------------------|-------------------|
| Also called | Method overloading | Method overriding |
| Resolved by | Compiler | JVM at runtime |
| Binding | Early binding | Late binding |
| Example | `add(int, int)` vs `add(double, double)` | `shape.draw()` on Circle/Rectangle |

```java
// Runtime polymorphism
class Payment {
    void process(double amount) { System.out.println("Processing " + amount); }
}
class CreditCard extends Payment {
    @Override void process(double amount) { System.out.println("CC: " + amount); }
}
class UPI extends Payment {
    @Override void process(double amount) { System.out.println("UPI: " + amount); }
}

List<Payment> payments = List.of(new CreditCard(), new UPI(), new CreditCard());
payments.forEach(p -> p.process(100.0));
// CC: 100.0 | UPI: 100.0 | CC: 100.0
// — actual type determined at runtime
```

**Tricky:** Can you achieve runtime polymorphism with static methods? No — static methods use early binding (compile-time resolution based on reference type).

---

## Q16. What is the output? (Tricky — constructor execution order)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
class A {
    static { System.out.println("A static block"); }
    { System.out.println("A instance block"); }
    A() { System.out.println("A constructor"); }
}

class B extends A {
    static { System.out.println("B static block"); }
    { System.out.println("B instance block"); }
    B() { System.out.println("B constructor"); }
}

public class Main {
    public static void main(String[] args) {
        new B();
        new B();
    }
}
```

**Output:**
```
A static block
B static block
A instance block
A constructor
B instance block
B constructor
A instance block
A constructor
B instance block
B constructor
```

**Rule:**
1. Static blocks run once when class is loaded (parent first, then child)
2. Instance blocks + constructor run on every object creation (parent first, then child)
3. Order per object: parent static (once) → child static (once) → parent instance block → parent constructor → child instance block → child constructor

---

## Q17. What is object cloning? What is the difference between shallow and deep copy?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

`Object.clone()` creates a copy of the object. Must implement `Cloneable` marker interface.

| | Shallow Copy | Deep Copy |
|-|-------------|-----------|
| Primitives | Copied by value | Copied by value |
| Objects/References | Reference copied (same object) | New copy of nested object |
| `clone()` default | Shallow | Need to override |

```java
class Address implements Cloneable {
    String city;
    Address(String city) { this.city = city; }

    @Override
    protected Address clone() throws CloneNotSupportedException {
        return (Address) super.clone();
    }
}

class Employee implements Cloneable {
    String name;
    Address address;

    Employee(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    // Shallow copy
    protected Employee shallowClone() throws CloneNotSupportedException {
        return (Employee) super.clone(); // address reference shared!
    }

    // Deep copy
    protected Employee deepClone() throws CloneNotSupportedException {
        Employee copy = (Employee) super.clone();
        copy.address = address.clone(); // new Address object
        return copy;
    }
}

Employee e1 = new Employee("Alice", new Address("Mumbai"));
Employee e2 = e1.shallowClone();
e2.address.city = "Delhi";
System.out.println(e1.address.city); // Delhi — shared reference, both changed!

Employee e3 = e1.deepClone();
e3.address.city = "Pune";
System.out.println(e1.address.city); // Delhi — unaffected, separate object
```

**Alternative:** Use copy constructor or serialization for deep copy. Avoid `clone()` in new code — it's considered broken by Joshua Bloch (Effective Java).

---

## Q18. What is the difference between `instanceof` and `getClass()`?

**Difficulty:** Medium | **Type:** Tricky

**Answer:**

```java
class Animal {}
class Dog extends Animal {}

Dog d = new Dog();

System.out.println(d instanceof Dog);    // true
System.out.println(d instanceof Animal); // true  — IS-A check (includes hierarchy)

System.out.println(d.getClass() == Dog.class);    // true
System.out.println(d.getClass() == Animal.class); // false — exact class check only
```

- `instanceof` — checks the full inheritance hierarchy (including interfaces). Used for safe casting.
- `getClass()` — returns the exact runtime class. Useful when you need to distinguish exact types.

**Pattern matching (Java 16+):**
```java
Object obj = new Dog();
if (obj instanceof Dog dog) { // pattern matching — no separate cast needed
    dog.bark();
}
```

---

## Q19. Scenario — How would you design a notification system using OOP?

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
// Abstraction — contract for all notification types
interface NotificationService {
    void send(String recipient, String message);
}

// Concrete implementations
class EmailNotification implements NotificationService {
    public void send(String recipient, String message) {
        System.out.println("Email to " + recipient + ": " + message);
    }
}

class SmsNotification implements NotificationService {
    public void send(String recipient, String message) {
        System.out.println("SMS to " + recipient + ": " + message);
    }
}

class PushNotification implements NotificationService {
    public void send(String recipient, String message) {
        System.out.println("Push to " + recipient + ": " + message);
    }
}

// Composition — NotificationManager HAS-A list of services
class NotificationManager {
    private final List<NotificationService> services;

    NotificationManager(List<NotificationService> services) {
        this.services = services;
    }

    void notifyAll(String recipient, String message) {
        services.forEach(s -> s.send(recipient, message));
    }
}

// Usage
NotificationManager mgr = new NotificationManager(
    List.of(new EmailNotification(), new SmsNotification())
);
mgr.notifyAll("user@example.com", "Your order shipped!");
// Adding a new channel = new class, no modification needed (OCP)
```

---

## Q20. What is the difference between method hiding and method overriding?

**Difficulty:** Senior | **Type:** Tricky

**Answer:**

| | Method Overriding | Method Hiding |
|-|-------------------|---------------|
| Applies to | Instance methods | Static methods |
| Resolved | Runtime (JVM) | Compile time (compiler) |
| Annotation | `@Override` works | `@Override` causes error |
| Reference type | Ignored — object type used | Used — reference type determines method |

```java
class Parent {
    static void staticMethod() { System.out.println("Parent static"); }
    void instanceMethod()      { System.out.println("Parent instance"); }
}

class Child extends Parent {
    static void staticMethod() { System.out.println("Child static"); }   // HIDING
    @Override void instanceMethod() { System.out.println("Child instance"); } // OVERRIDING
}

Parent ref = new Child();
ref.staticMethod();   // Parent static   (hiding — ref type used)
ref.instanceMethod(); // Child instance  (overriding — object type used)

Child ref2 = new Child();
ref2.staticMethod();   // Child static
ref2.instanceMethod(); // Child instance
```

---

## Q21. What is an inner class? What are the types?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| Type | Description | Can access outer? | `static`? |
|------|-------------|-------------------|-----------|
| Regular inner class | Non-static, inside outer class | Yes (all members) | No |
| Static nested class | Static, inside outer class | Only static members | Yes |
| Local inner class | Inside a method | Yes (effectively final vars) | No |
| Anonymous inner class | No name, one-time use | Yes | No |

```java
class Outer {
    private int x = 10;

    // Regular inner class
    class Inner {
        void show() { System.out.println("x = " + x); } // accesses outer's private
    }

    // Static nested class
    static class StaticNested {
        void show() { System.out.println("Static nested"); }
        // System.out.println(x); // ❌ cannot access instance member
    }

    void method() {
        int localVar = 5; // effectively final

        // Local inner class
        class Local {
            void show() { System.out.println("local: " + localVar); }
        }
        new Local().show();

        // Anonymous inner class
        Runnable r = new Runnable() {
            public void run() { System.out.println("Anonymous runnable"); }
        };
        r.run();
    }
}

// Usage
Outer o = new Outer();
Outer.Inner inner = o.new Inner();
inner.show(); // x = 10

Outer.StaticNested sn = new Outer.StaticNested(); // no outer instance needed
sn.show();
```

---

## Q22. What happens if you don't call `super()` in a child class constructor?

**Difficulty:** Tricky | **Type:** Tricky

**Answer:**

The compiler automatically inserts `super()` (no-arg) as the first statement if:
- No explicit `super(...)` or `this(...)` call is made.
- Parent has a no-arg constructor (default or explicit).

**Compilation fails** if parent has only a parameterized constructor and child doesn't call it explicitly.

```java
class Animal {
    Animal(String name) { System.out.println("Animal: " + name); }
    // No default constructor — compiler does NOT add one
}

class Dog extends Animal {
    Dog() {
        // compiler would insert super() here — but Animal has no no-arg constructor!
        // ❌ Compilation error: constructor Animal() is undefined
    }

    Dog(String name) {
        super(name); // ✅ must explicitly call
        System.out.println("Dog: " + name);
    }
}
```

---

## Q23. What is method chaining? How is it implemented?

**Difficulty:** Medium | **Type:** Theory + Scenario

**Answer:**

Method chaining (also called fluent interface) — each method returns `this` so calls can be chained.

```java
class QueryBuilder {
    private String table;
    private String condition;
    private int limit = Integer.MAX_VALUE;

    QueryBuilder from(String table) {
        this.table = table;
        return this; // return current instance
    }

    QueryBuilder where(String condition) {
        this.condition = condition;
        return this;
    }

    QueryBuilder limit(int n) {
        this.limit = n;
        return this;
    }

    String build() {
        return "SELECT * FROM " + table
             + (condition != null ? " WHERE " + condition : "")
             + (limit != Integer.MAX_VALUE ? " LIMIT " + limit : "");
    }
}

String query = new QueryBuilder()
    .from("users")
    .where("age > 18")
    .limit(10)
    .build();

System.out.println(query);
// SELECT * FROM users WHERE age > 18 LIMIT 10
```

**Real-world use:** `StringBuilder`, `Stream`, `Optional`, Lombok's `@Builder`.

---

## Q24. What is the difference between `final`, `finally`, and `finalize()`?

**Difficulty:** Basic | **Type:** Theory

See [InterviewQA.md](InterviewQA.md) — Q1 for the detailed answer.

Quick summary:
- `final` — keyword: restricts modification of variable/method/class
- `finally` — block: always executes after try-catch, used for cleanup
- `finalize()` — deprecated method: called by GC before object destruction

---

## Q25. What is object-level locking vs class-level locking?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

- **Object-level locking** — `synchronized` on instance method or `synchronized(this)` — acquires lock on the specific object instance.
- **Class-level locking** — `synchronized` on static method or `synchronized(ClassName.class)` — acquires lock on the Class object (shared across all instances).

```java
class Counter {
    private int count = 0;
    private static int totalCount = 0;

    // Object-level lock — each Counter instance has its own lock
    synchronized void increment() {
        count++;
    }

    // Class-level lock — one lock for all Counter objects
    static synchronized void incrementTotal() {
        totalCount++;
    }

    // Explicit block form
    void incrementBoth() {
        synchronized (this) {           // object lock
            count++;
        }
        synchronized (Counter.class) {  // class lock
            totalCount++;
        }
    }
}
```

---

## Q26. Can an interface extend another interface? Can a class implement multiple interfaces?

**Difficulty:** Basic | **Type:** Theory

**Answer:**

- **Interface extending interface** — Yes, and it can extend multiple interfaces.
- **Class implementing multiple interfaces** — Yes.

```java
interface Readable  { String read(); }
interface Writable  { void write(String data); }
interface ReadWrite extends Readable, Writable { } // interface extends multiple interfaces

class FileStream implements ReadWrite {
    public String read() { return "data"; }
    public void write(String data) { System.out.println("Writing: " + data); }
}
```

---

## Q27. What is an abstract class? Can it have a constructor?

**Difficulty:** Medium | **Type:** Theory + Tricky

**Answer:**

An abstract class **can** have a constructor — it's called via `super()` from the concrete subclass constructor. It cannot be instantiated directly.

```java
abstract class Shape {
    String color;

    Shape(String color) { // constructor in abstract class
        this.color = color;
        System.out.println("Shape created with color: " + color);
    }

    abstract double area(); // subclass must implement

    void describe() {  // concrete method
        System.out.println("Color: " + color + ", Area: " + area());
    }
}

class Circle extends Shape {
    double radius;

    Circle(String color, double radius) {
        super(color); // calls abstract class constructor
        this.radius = radius;
    }

    @Override
    double area() { return Math.PI * radius * radius; }
}

Circle c = new Circle("Red", 5);
c.describe(); // Color: Red, Area: 78.53...
```

---

## Q28. Scenario — What design would you use for a payment gateway that supports multiple providers?

**Difficulty:** Senior | **Type:** Scenario + Design

**Answer:**

Use **Strategy Pattern** — define a common interface, swap implementations at runtime.

```java
// Strategy interface
interface PaymentGateway {
    boolean processPayment(double amount, String currency);
    String getGatewayName();
}

// Concrete strategies
class RazorpayGateway implements PaymentGateway {
    public boolean processPayment(double amount, String currency) {
        System.out.println("Razorpay: processing " + amount + " " + currency);
        return true;
    }
    public String getGatewayName() { return "Razorpay"; }
}

class StripeGateway implements PaymentGateway {
    public boolean processPayment(double amount, String currency) {
        System.out.println("Stripe: processing " + amount + " " + currency);
        return true;
    }
    public String getGatewayName() { return "Stripe"; }
}

// Context — uses whichever strategy is injected
class PaymentProcessor {
    private PaymentGateway gateway;

    PaymentProcessor(PaymentGateway gateway) { this.gateway = gateway; }

    void setGateway(PaymentGateway gateway) { this.gateway = gateway; } // swap at runtime

    void pay(double amount) {
        System.out.println("Using: " + gateway.getGatewayName());
        boolean success = gateway.processPayment(amount, "INR");
        System.out.println(success ? "Payment successful" : "Payment failed");
    }
}

PaymentProcessor processor = new PaymentProcessor(new RazorpayGateway());
processor.pay(500);  // Razorpay
processor.setGateway(new StripeGateway());
processor.pay(1000); // Stripe
```

---

## Q29. What is the difference between early binding and late binding?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

| | Early Binding | Late Binding |
|-|---------------|-------------|
| Also called | Static binding, Compile-time | Dynamic binding, Runtime |
| Resolved by | Compiler | JVM |
| Used for | Overloaded methods, static methods, final methods, private methods | Overridden instance methods |
| Performance | Faster | Slight overhead (vtable lookup) |

```java
class Animal {
    void sound() { System.out.println("Some sound"); } // late binding
    static void type() { System.out.println("Animal"); } // early binding
}

class Cat extends Animal {
    @Override void sound() { System.out.println("Meow"); }
    static void type() { System.out.println("Cat"); }
}

Animal a = new Cat();
a.sound(); // Meow   — late binding (JVM checks actual type: Cat)
a.type();  // Animal — early binding (compiler uses reference type: Animal)
```

---

## Q30. What is `toString()`, `equals()`, and `hashCode()` contract?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

All three are defined in `Object` and should be overridden together.

**`equals()` contract (reflexive, symmetric, transitive, consistent, null-safe):**
```java
x.equals(x)           // true (reflexive)
x.equals(y) == y.equals(x) // symmetric
if x.equals(y) && y.equals(z) then x.equals(z) // transitive
x.equals(null)        // false (null-safe)
```

**`hashCode()` contract:**
- Objects that are `equals()` MUST have the same `hashCode()`
- Objects with same `hashCode()` do NOT have to be `equals()` (collision is OK)

**Why this matters:** `HashMap` and `HashSet` use `hashCode()` to find the bucket, then `equals()` to confirm the key. Breaking the contract causes lost entries.

```java
import java.util.Objects;

class Product {
    String sku;
    String name;
    double price;

    Product(String sku, String name, double price) {
        this.sku = sku; this.name = name; this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product p)) return false;
        return sku.equals(p.sku); // business key = sku
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku); // must use same fields as equals()
    }

    @Override
    public String toString() {
        return "Product{sku='" + sku + "', name='" + name + "', price=" + price + "}";
    }
}

// If equals() is overridden without hashCode():
Set<Product> set = new HashSet<>();
set.add(new Product("P001", "Laptop", 50000));
set.add(new Product("P001", "Laptop", 50000)); // same sku
System.out.println(set.size()); // 2 if hashCode() not overridden! (should be 1)
```

---

## Q31. Scenario — You find a class with 500 lines. How do you refactor it using OOP?

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

This is a **God Class** anti-pattern. Apply SRP and decompose:

1. **Identify responsibilities** — list what the class does (DB, email, validation, formatting, etc.)
2. **Extract classes** — one class per responsibility
3. **Use interfaces** — define contracts for each extracted class
4. **Wire with composition** — original class orchestrates via HAS-A
5. **Apply DIP** — inject dependencies, not concrete implementations

```java
// Before: OrderService does everything
class OrderService {
    void placeOrder(Order o) {
        // 100 lines: validate
        // 100 lines: calculate price
        // 100 lines: save to DB
        // 100 lines: send email
        // 100 lines: update inventory
    }
}

// After: each responsibility is its own class
interface OrderValidator    { void validate(Order o); }
interface PricingEngine     { double calculate(Order o); }
interface OrderRepository   { void save(Order o); }
interface NotificationService { void notify(Order o); }
interface InventoryService  { void updateStock(Order o); }

class OrderService {
    private final OrderValidator validator;
    private final PricingEngine pricing;
    private final OrderRepository repo;
    private final NotificationService notifier;
    private final InventoryService inventory;

    // injected via constructor
    OrderService(OrderValidator v, PricingEngine p, OrderRepository r,
                 NotificationService n, InventoryService i) {
        validator = v; pricing = p; repo = r; notifier = n; inventory = i;
    }

    void placeOrder(Order o) {
        validator.validate(o);
        o.setTotal(pricing.calculate(o));
        repo.save(o);
        notifier.notify(o);
        inventory.updateStock(o);
    }
}
```

---

## Q32. What is the difference between `Comparable` and `Comparator`?

**Difficulty:** Medium | **Type:** Theory + Scenario

**Answer:**

| | `Comparable` | `Comparator` |
|-|-------------|-------------|
| Package | `java.lang` | `java.util` |
| Method | `compareTo(T o)` | `compare(T o1, T o2)` |
| Defined in | The class itself (natural ordering) | Separate class or lambda (custom ordering) |
| Single sort | Only one natural order | Multiple sort strategies |
| Use case | Sorting when you own the class | Sorting third-party classes, multiple orderings |

```java
// Comparable — natural ordering defined in class
class Employee implements Comparable<Employee> {
    String name;
    int salary;

    Employee(String name, int salary) { this.name = name; this.salary = salary; }

    @Override
    public int compareTo(Employee other) {
        return Integer.compare(this.salary, other.salary); // natural = by salary
    }
}

// Comparator — multiple orderings without modifying class
Comparator<Employee> byName   = Comparator.comparing(e -> e.name);
Comparator<Employee> bySalary = Comparator.comparingInt(e -> e.salary);
Comparator<Employee> byNameThenSalary = byName.thenComparingInt(e -> e.salary);

List<Employee> employees = Arrays.asList(
    new Employee("Zara", 80000),
    new Employee("Alice", 60000),
    new Employee("Bob", 80000)
);

Collections.sort(employees);              // uses Comparable (natural — by salary)
employees.sort(byName);                   // uses Comparator (by name)
employees.sort(byNameThenSalary);         // chained comparator
employees.sort(Comparator.reverseOrder()); // reversed natural order
```

---

## Summary — Key Takeaways for Interviews

| Topic | What interviewers test |
|-------|----------------------|
| 4 Pillars | Can you give real-world examples, not textbook definitions |
| SOLID | Can you spot violations in existing code |
| Inheritance vs Composition | When to choose which — judgment call |
| Overloading vs Overriding | Output prediction with reference/object type mismatch |
| Abstract class vs Interface | Justify choice in a design scenario |
| `equals()`/`hashCode()` | Why contract matters in collections |
| Constructor order | Static block → instance block → constructor, parent first |
