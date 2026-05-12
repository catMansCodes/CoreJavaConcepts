# Design Patterns — Java Interview Guide (8–10 Years Experience)

## Quick Reference

```
Creational:   Singleton, Factory Method, Abstract Factory, Builder, Prototype
Structural:   Adapter, Decorator, Proxy, Facade, Composite, Bridge, Flyweight
Behavioral:   Strategy, Observer, Template Method, Command, Iterator, Chain of Responsibility, State
```

---

## Q1. What is the Singleton pattern? How to implement it thread-safely?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Ensures only one instance of a class exists. 4 implementations (best to worst):

```java
// Option 1: Enum Singleton (BEST — reflection-proof, serialization-safe)
enum AppConfig {
    INSTANCE;
    private final String host = "localhost";
    public String getHost() { return host; }
}
AppConfig.INSTANCE.getHost();

// Option 2: Initialization-on-demand Holder (lazy, thread-safe, no sync overhead)
class DatabasePool {
    private DatabasePool() { }

    private static class Holder {
        static final DatabasePool INSTANCE = new DatabasePool();
        // JVM guarantees class initialization is thread-safe
    }

    public static DatabasePool getInstance() { return Holder.INSTANCE; }
}

// Option 3: Double-Checked Locking (lazy, volatile required)
class CacheManager {
    private static volatile CacheManager instance; // volatile prevents partial init visibility

    private CacheManager() { }

    public static CacheManager getInstance() {
        if (instance == null) {                    // first check (no lock)
            synchronized (CacheManager.class) {
                if (instance == null) {             // second check (with lock)
                    instance = new CacheManager(); // volatile write
                }
            }
        }
        return instance;
    }
}

// Option 4: Eager initialization (simple, not lazy)
class Logger {
    private static final Logger INSTANCE = new Logger();
    private Logger() { }
    public static Logger getInstance() { return INSTANCE; }
}
```

**Why `volatile` in DCL?** Without it, JVM may reorder: allocate memory → assign reference → initialize fields. Another thread may see a non-null but partially initialized object.

---

## Q2. What is the Factory Method pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Defines an interface for creating objects, but lets subclasses decide which class to instantiate.

```java
// Abstract creator
abstract class NotificationFactory {
    public final void send(String recipient, String message) { // template method
        Notification n = createNotification(); // factory method
        n.setRecipient(recipient);
        n.setMessage(message);
        n.deliver();
    }
    protected abstract Notification createNotification(); // factory method
}

// Concrete factories
class EmailFactory extends NotificationFactory {
    @Override protected Notification createNotification() { return new EmailNotification(); }
}

class SMSFactory extends NotificationFactory {
    @Override protected Notification createNotification() { return new SMSNotification(); }
}

// Usage
NotificationFactory factory = new EmailFactory();
factory.send("alice@example.com", "Hello!");

// Static factory method variant (simpler — no subclassing)
class ConnectionFactory {
    public static Connection create(String type) {
        return switch (type) {
            case "mysql"    -> new MySQLConnection();
            case "postgres" -> new PostgresConnection();
            case "mongo"    -> new MongoConnection();
            default -> throw new IllegalArgumentException("Unknown DB: " + type);
        };
    }
}
Connection conn = ConnectionFactory.create("mysql");
```

---

## Q3. What is the Abstract Factory pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Creates **families of related objects** without specifying their concrete classes.

```java
// Product families
interface Button { void click(); }
interface Checkbox { void check(); }

// Concrete products — Windows family
class WindowsButton implements Button { public void click() { System.out.println("Windows click"); } }
class WindowsCheckbox implements Checkbox { public void check() { System.out.println("Windows check"); } }

// Concrete products — Mac family
class MacButton implements Button { public void click() { System.out.println("Mac click"); } }
class MacCheckbox implements Checkbox { public void check() { System.out.println("Mac check"); } }

// Abstract factory
interface UIFactory {
    Button createButton();
    Checkbox createCheckbox();
}

// Concrete factories
class WindowsFactory implements UIFactory {
    public Button createButton()     { return new WindowsButton(); }
    public Checkbox createCheckbox() { return new WindowsCheckbox(); }
}

class MacFactory implements UIFactory {
    public Button createButton()     { return new MacButton(); }
    public Checkbox createCheckbox() { return new MacCheckbox(); }
}

// Client — depends only on abstractions
class Application {
    private final Button button;
    private final Checkbox checkbox;

    Application(UIFactory factory) {
        button   = factory.createButton();
        checkbox = factory.createCheckbox();
    }

    void render() { button.click(); checkbox.check(); }
}

// Switch factory to change the whole UI family
String os = System.getProperty("os.name");
UIFactory factory = os.contains("Windows") ? new WindowsFactory() : new MacFactory();
new Application(factory).render();
```

---

## Q4. What is the Builder pattern? When to use it?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Constructs complex objects step by step, separating construction from representation.

```java
// Without Builder — telescoping constructors problem
new HttpRequest("GET", "https://api.example.com", null, null, 30, true, null);
// What does each null mean?

// With Builder — readable, self-documenting
class HttpRequest {
    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final String body;
    private final int timeoutSeconds;
    private final boolean followRedirects;
    private final String proxyHost;

    private HttpRequest(Builder b) {
        this.method = b.method;
        this.url = b.url;
        this.headers = Collections.unmodifiableMap(b.headers);
        this.body = b.body;
        this.timeoutSeconds = b.timeoutSeconds;
        this.followRedirects = b.followRedirects;
        this.proxyHost = b.proxyHost;
    }

    static class Builder {
        private final String method; // required
        private final String url;   // required
        private Map<String, String> headers = new HashMap<>();
        private String body;
        private int timeoutSeconds = 30;
        private boolean followRedirects = true;
        private String proxyHost;

        Builder(String method, String url) {
            this.method = Objects.requireNonNull(method);
            this.url = Objects.requireNonNull(url);
        }

        Builder header(String key, String value) { headers.put(key, value); return this; }
        Builder body(String body) { this.body = body; return this; }
        Builder timeout(int seconds) { this.timeoutSeconds = seconds; return this; }
        Builder noRedirects() { this.followRedirects = false; return this; }
        Builder proxy(String host) { this.proxyHost = host; return this; }

        HttpRequest build() {
            if (body != null && "GET".equals(method)) throw new IllegalStateException("GET cannot have body");
            return new HttpRequest(this);
        }
    }
}

// Fluent usage
HttpRequest request = new HttpRequest.Builder("POST", "https://api.example.com/users")
    .header("Authorization", "Bearer token123")
    .header("Content-Type", "application/json")
    .body("{\"name\": \"Alice\"}")
    .timeout(60)
    .build();
```

**When to use Builder:**
- Object has many optional parameters
- Object construction is complex with validation
- Want immutable objects built step by step
- Lombok `@Builder` generates this automatically

---

## Q5. What is the Prototype pattern?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Creates new objects by cloning an existing object (prototype) rather than instantiating from scratch.

```java
abstract class Shape implements Cloneable {
    String color;
    abstract double area();

    @Override
    public Shape clone() {
        try { return (Shape) super.clone(); }
        catch (CloneNotSupportedException e) { throw new AssertionError(); }
    }
}

class Circle extends Shape {
    double radius;
    Circle(double radius, String color) { this.radius = radius; this.color = color; }
    @Override public double area() { return Math.PI * radius * radius; }
}

// Registry of prototypes
class ShapeRegistry {
    private final Map<String, Shape> prototypes = new HashMap<>();

    void register(String name, Shape prototype) { prototypes.put(name, prototype); }

    Shape create(String name) {
        Shape prototype = prototypes.get(name);
        if (prototype == null) throw new IllegalArgumentException("Unknown shape: " + name);
        return prototype.clone(); // clone, not new!
    }
}

ShapeRegistry registry = new ShapeRegistry();
registry.register("smallCircle", new Circle(5, "red"));
registry.register("bigCircle",   new Circle(100, "blue"));

Shape c1 = registry.create("smallCircle"); // clone of prototype
Shape c2 = registry.create("smallCircle"); // another clone
System.out.println(c1 == c2); // false — different objects
```

**When useful:** Object creation is expensive (DB query, complex initialization) and cloning is cheaper.

---

## Q6. What is the Adapter pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Converts the interface of a class into another interface that clients expect. Makes incompatible interfaces work together.

```java
// Old payment API (adaptee — cannot change)
class LegacyPaymentProcessor {
    void processPaymentInCents(int amountCents, String currency) {
        System.out.println("Legacy: " + amountCents + " cents in " + currency);
    }
}

// New interface your system expects (target)
interface ModernPaymentGateway {
    void pay(double amountInRupees, String currency);
}

// Adapter — wraps legacy to match new interface
class LegacyPaymentAdapter implements ModernPaymentGateway {
    private final LegacyPaymentProcessor legacy;

    LegacyPaymentAdapter(LegacyPaymentProcessor legacy) { this.legacy = legacy; }

    @Override
    public void pay(double amountInRupees, String currency) {
        int cents = (int)(amountInRupees * 100); // convert
        legacy.processPaymentInCents(cents, currency);
    }
}

// Client uses new interface — doesn't know about legacy
ModernPaymentGateway gateway = new LegacyPaymentAdapter(new LegacyPaymentProcessor());
gateway.pay(499.99, "INR");
// Legacy: 49999 cents in INR

// Real Java examples: Arrays.asList() (Array → List), InputStreamReader (InputStream → Reader)
BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); // Adapter
```

---

## Q7. What is the Decorator pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Dynamically adds behavior to objects by wrapping them in decorator objects. Alternative to subclassing for extending functionality.

```java
// Component interface
interface TextProcessor {
    String process(String text);
}

// Base component
class PlainTextProcessor implements TextProcessor {
    @Override public String process(String text) { return text; }
}

// Base decorator
abstract class TextDecorator implements TextProcessor {
    protected final TextProcessor wrapped;
    TextDecorator(TextProcessor wrapped) { this.wrapped = wrapped; }
    @Override public String process(String text) { return wrapped.process(text); }
}

// Concrete decorators
class UpperCaseDecorator extends TextDecorator {
    UpperCaseDecorator(TextProcessor tp) { super(tp); }
    @Override public String process(String text) { return super.process(text).toUpperCase(); }
}

class TrimDecorator extends TextDecorator {
    TrimDecorator(TextProcessor tp) { super(tp); }
    @Override public String process(String text) { return super.process(text).trim(); }
}

class PrefixDecorator extends TextDecorator {
    private final String prefix;
    PrefixDecorator(TextProcessor tp, String prefix) { super(tp); this.prefix = prefix; }
    @Override public String process(String text) { return prefix + super.process(text); }
}

// Composing decorators at runtime
TextProcessor processor = new PrefixDecorator(
    new UpperCaseDecorator(
        new TrimDecorator(
            new PlainTextProcessor()
        )
    ),
    "[PROCESSED] "
);

System.out.println(processor.process("  hello world  "));
// [PROCESSED] HELLO WORLD

// Real Java examples: I/O streams, Collections.synchronizedList, Collections.unmodifiableList
InputStream is = new BufferedInputStream(new GZIPInputStream(new FileInputStream("file.gz")));
```

---

## Q8. What is the Proxy pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Provides a substitute or placeholder for another object to control access to it.

```java
// Service interface
interface OrderService {
    Order getOrder(long id);
    void placeOrder(Order order);
}

// Real service
class OrderServiceImpl implements OrderService {
    @Override public Order getOrder(long id) { /* DB call */ return new Order(id); }
    @Override public void placeOrder(Order order) { /* process */ }
}

// Proxy types:

// 1. Caching Proxy
class CachingOrderProxy implements OrderService {
    private final OrderService real;
    private final Map<Long, Order> cache = new HashMap<>();

    CachingOrderProxy(OrderService real) { this.real = real; }

    @Override
    public Order getOrder(long id) {
        return cache.computeIfAbsent(id, real::getOrder);
    }

    @Override public void placeOrder(Order order) { real.placeOrder(order); }
}

// 2. Logging/Audit Proxy
class AuditOrderProxy implements OrderService {
    private final OrderService real;
    AuditOrderProxy(OrderService real) { this.real = real; }

    @Override
    public void placeOrder(Order order) {
        System.out.println("[AUDIT] Order placed by: " + order.userId + " at " + Instant.now());
        real.placeOrder(order);
    }

    @Override public Order getOrder(long id) { return real.getOrder(id); }
}

// 3. Security Proxy
class SecureOrderProxy implements OrderService {
    private final OrderService real;
    SecureOrderProxy(OrderService real) { this.real = real; }

    @Override
    public void placeOrder(Order order) {
        if (!SecurityContext.hasRole("BUYER")) throw new SecurityException("Access denied");
        real.placeOrder(order);
    }

    @Override public Order getOrder(long id) { return real.getOrder(id); }
}

// Chain proxies
OrderService service = new AuditOrderProxy(new CachingOrderProxy(new OrderServiceImpl()));

// Spring AOP creates JDK dynamic proxies (or CGLIB) for @Transactional, @Cacheable, etc.
```

---

## Q9. What is the Facade pattern?

**Difficulty:** Medium | **Type:** Theory + Scenario

**Answer:**

Provides a simplified interface to a complex subsystem.

```java
// Complex subsystems
class OrderValidator { void validate(Order o) { /* complex validation */ } }
class PaymentProcessor { void charge(Order o, PaymentInfo p) { /* complex */ } }
class InventoryService { void reserve(Order o) { /* complex */ } }
class ShippingService { void createShipment(Order o) { /* complex */ } }
class NotificationService { void notify(Order o) { /* email/SMS */ } }

// Facade — simple interface over all subsystems
class OrderFacade {
    private final OrderValidator validator = new OrderValidator();
    private final PaymentProcessor payment = new PaymentProcessor();
    private final InventoryService inventory = new InventoryService();
    private final ShippingService shipping = new ShippingService();
    private final NotificationService notifications = new NotificationService();

    // Single simple method — hides all complexity
    void placeOrder(Order order, PaymentInfo paymentInfo) {
        validator.validate(order);
        payment.charge(order, paymentInfo);
        inventory.reserve(order);
        shipping.createShipment(order);
        notifications.notify(order);
    }
}

// Client code — only interacts with Facade
OrderFacade facade = new OrderFacade();
facade.placeOrder(myOrder, paymentInfo); // simple!

// Real examples: SLF4J over Log4j/Logback, JDBC over database drivers
```

---

## Q10. What is the Strategy pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario (most commonly asked behavioral)

**Answer:**

Defines a family of algorithms, encapsulates each one, and makes them interchangeable at runtime.

```java
// Strategy interface
interface SortingStrategy {
    void sort(int[] array);
    String name();
}

// Concrete strategies
class BubbleSort implements SortingStrategy {
    @Override public void sort(int[] arr) { /* bubble sort */ }
    @Override public String name() { return "BubbleSort"; }
}

class QuickSort implements SortingStrategy {
    @Override public void sort(int[] arr) { /* quicksort */ }
    @Override public String name() { return "QuickSort"; }
}

class MergeSort implements SortingStrategy {
    @Override public void sort(int[] arr) { /* merge sort */ }
    @Override public String name() { return "MergeSort"; }
}

// Context — holds strategy, delegates to it
class Sorter {
    private SortingStrategy strategy;

    Sorter(SortingStrategy strategy) { this.strategy = strategy; }

    void setStrategy(SortingStrategy strategy) { this.strategy = strategy; } // swap at runtime

    void sort(int[] arr) {
        System.out.println("Using: " + strategy.name());
        strategy.sort(arr);
    }
}

// Smart strategy selection
Sorter sorter;
if (arr.length < 100) {
    sorter = new Sorter(new BubbleSort());
} else if (arr.length < 100_000) {
    sorter = new Sorter(new QuickSort());
} else {
    sorter = new Sorter(new MergeSort());
}
sorter.sort(arr);

// Lambda-based strategy (Java 8+)
Sorter lambdaSorter = new Sorter(arr2 -> Arrays.sort(arr2)); // strategy as lambda
Sorter refSorter    = new Sorter(Arrays::sort);               // method reference
```

---

## Q11. What is the Observer pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Defines a one-to-many dependency — when one object changes state, all dependents are notified automatically. Also called Publish-Subscribe.

```java
// Observer interface
interface EventListener<T> {
    void onEvent(T event);
}

// Subject (Observable)
class EventBus<T> {
    private final List<EventListener<T>> listeners = new CopyOnWriteArrayList<>();

    void subscribe(EventListener<T> listener) { listeners.add(listener); }
    void unsubscribe(EventListener<T> listener) { listeners.remove(listener); }

    void publish(T event) {
        listeners.forEach(l -> l.onEvent(event));
    }
}

// Events
record OrderPlacedEvent(long orderId, String userId, double amount) {}

// Listeners
class EmailService implements EventListener<OrderPlacedEvent> {
    @Override public void onEvent(OrderPlacedEvent e) {
        System.out.println("Sending confirmation email for order " + e.orderId());
    }
}

class InventoryService implements EventListener<OrderPlacedEvent> {
    @Override public void onEvent(OrderPlacedEvent e) {
        System.out.println("Reserving inventory for order " + e.orderId());
    }
}

class AnalyticsService implements EventListener<OrderPlacedEvent> {
    @Override public void onEvent(OrderPlacedEvent e) {
        System.out.println("Recording analytics for " + e.amount());
    }
}

// Wiring
EventBus<OrderPlacedEvent> bus = new EventBus<>();
bus.subscribe(new EmailService());
bus.subscribe(new InventoryService());
bus.subscribe(new AnalyticsService());

// Trigger — all subscribers notified
bus.publish(new OrderPlacedEvent(101L, "alice", 599.0));

// Real examples: Java's PropertyChangeListener, Spring's ApplicationEvent/ApplicationListener,
// RxJava Observable, Reactor Flux
```

---

## Q12. What is the Template Method pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Defines the **skeleton** of an algorithm in a base class, deferring some steps to subclasses.

```java
// Abstract template
abstract class DataProcessor {
    // Template method — defines the algorithm skeleton
    public final void process() {
        readData();
        processData();
        writeOutput();
        sendNotification(); // hook method with default behavior
    }

    protected abstract void readData();
    protected abstract void processData();
    protected abstract void writeOutput();

    // Hook method — optional override
    protected void sendNotification() {
        System.out.println("Processing complete");
    }
}

// Concrete implementations
class CsvDataProcessor extends DataProcessor {
    @Override protected void readData()    { System.out.println("Reading CSV"); }
    @Override protected void processData() { System.out.println("Processing CSV data"); }
    @Override protected void writeOutput() { System.out.println("Writing CSV report"); }
}

class JsonDataProcessor extends DataProcessor {
    @Override protected void readData()    { System.out.println("Reading JSON"); }
    @Override protected void processData() { System.out.println("Processing JSON data"); }
    @Override protected void writeOutput() { System.out.println("Writing JSON response"); }

    @Override
    protected void sendNotification() {
        System.out.println("JSON processing done — sending webhook");
    }
}

// Usage — algorithm order is always: read → process → write → notify
new CsvDataProcessor().process();
new JsonDataProcessor().process();

// Real Java examples:
// AbstractList (implements List, leaves get(int) + size() abstract)
// HttpServlet.service() calls doGet() or doPost()
// JUnit's TestCase.runTest()
```

---

## Q13. What is the Command pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Encapsulates a request as an object. Supports undo, redo, queuing, logging of operations.

```java
// Command interface
interface Command {
    void execute();
    void undo();
}

// Receiver
class TextEditor {
    StringBuilder text = new StringBuilder();
    void insert(int pos, String str) { text.insert(pos, str); }
    void delete(int start, int end) { text.delete(start, end); }
    @Override public String toString() { return text.toString(); }
}

// Concrete commands
class InsertCommand implements Command {
    private final TextEditor editor;
    private final int position;
    private final String text;

    InsertCommand(TextEditor editor, int position, String text) {
        this.editor = editor; this.position = position; this.text = text;
    }

    @Override public void execute() { editor.insert(position, text); }
    @Override public void undo()    { editor.delete(position, position + text.length()); }
}

// Invoker — manages command history
class CommandInvoker {
    private final Deque<Command> history = new ArrayDeque<>();

    void execute(Command cmd) {
        cmd.execute();
        history.push(cmd);
    }

    void undo() {
        if (!history.isEmpty()) history.pop().undo();
    }
}

// Usage
TextEditor editor = new TextEditor();
CommandInvoker invoker = new CommandInvoker();

invoker.execute(new InsertCommand(editor, 0, "Hello"));
System.out.println(editor); // Hello
invoker.execute(new InsertCommand(editor, 5, " World"));
System.out.println(editor); // Hello World
invoker.undo();
System.out.println(editor); // Hello
invoker.undo();
System.out.println(editor); // (empty)

// Real examples: java.lang.Runnable, javax.swing.Action, Spring Batch Step
```

---

## Q14. What is the Chain of Responsibility pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Passes a request along a chain of handlers — each handler decides to process or pass it on.

```java
// Handler
abstract class RequestHandler {
    protected RequestHandler next;

    RequestHandler setNext(RequestHandler next) { this.next = next; return next; }

    abstract boolean handle(HttpRequest request);

    protected boolean passToNext(HttpRequest request) {
        return next != null && next.handle(request);
    }
}

// Concrete handlers
class AuthHandler extends RequestHandler {
    @Override public boolean handle(HttpRequest req) {
        if (req.getHeader("Authorization") == null) {
            System.out.println("Auth failed — no token");
            return false;
        }
        System.out.println("Auth OK");
        return passToNext(req);
    }
}

class RateLimitHandler extends RequestHandler {
    private int requestCount = 0;
    @Override public boolean handle(HttpRequest req) {
        if (++requestCount > 100) {
            System.out.println("Rate limit exceeded");
            return false;
        }
        return passToNext(req);
    }
}

class LoggingHandler extends RequestHandler {
    @Override public boolean handle(HttpRequest req) {
        System.out.println("Logging: " + req.getPath());
        return passToNext(req);
    }
}

class BusinessHandler extends RequestHandler {
    @Override public boolean handle(HttpRequest req) {
        System.out.println("Processing business logic");
        return true;
    }
}

// Build the chain
AuthHandler auth = new AuthHandler();
auth.setNext(new RateLimitHandler()).setNext(new LoggingHandler()).setNext(new BusinessHandler());

auth.handle(new HttpRequest("/api/orders", "Bearer token")); // all pass
auth.handle(new HttpRequest("/api/orders", null)); // auth fails at first handler
```

---

## Q15. What is the State pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Allows an object to alter its behavior when its internal state changes. Eliminates large if-else chains for state management.

```java
// State interface
interface OrderState {
    void pay(Order order);
    void ship(Order order);
    void deliver(Order order);
    void cancel(Order order);
    String getStatus();
}

// Concrete states
class PendingState implements OrderState {
    public void pay(Order o) {
        System.out.println("Payment received");
        o.setState(new PaidState());
    }
    public void ship(Order o) { System.out.println("Cannot ship — not paid"); }
    public void deliver(Order o) { System.out.println("Cannot deliver — not paid"); }
    public void cancel(Order o) {
        System.out.println("Order cancelled");
        o.setState(new CancelledState());
    }
    public String getStatus() { return "PENDING"; }
}

class PaidState implements OrderState {
    public void pay(Order o) { System.out.println("Already paid"); }
    public void ship(Order o) {
        System.out.println("Order shipped");
        o.setState(new ShippedState());
    }
    public void deliver(Order o) { System.out.println("Cannot deliver — not shipped"); }
    public void cancel(Order o) {
        System.out.println("Order cancelled — refund initiated");
        o.setState(new CancelledState());
    }
    public String getStatus() { return "PAID"; }
}

class ShippedState implements OrderState {
    public void pay(Order o) { System.out.println("Already paid"); }
    public void ship(Order o) { System.out.println("Already shipped"); }
    public void deliver(Order o) {
        System.out.println("Order delivered");
        o.setState(new DeliveredState());
    }
    public void cancel(Order o) { System.out.println("Cannot cancel — already shipped"); }
    public String getStatus() { return "SHIPPED"; }
}

class DeliveredState implements OrderState {
    public void pay(Order o) { System.out.println("Already paid"); }
    public void ship(Order o) { System.out.println("Already delivered"); }
    public void deliver(Order o) { System.out.println("Already delivered"); }
    public void cancel(Order o) { System.out.println("Cannot cancel — delivered"); }
    public String getStatus() { return "DELIVERED"; }
}

class CancelledState implements OrderState {
    public void pay(Order o) { System.out.println("Order cancelled"); }
    public void ship(Order o) { System.out.println("Order cancelled"); }
    public void deliver(Order o) { System.out.println("Order cancelled"); }
    public void cancel(Order o) { System.out.println("Already cancelled"); }
    public String getStatus() { return "CANCELLED"; }
}

// Context
class Order {
    private OrderState state = new PendingState();
    void setState(OrderState s) { this.state = s; }
    void pay()     { state.pay(this); }
    void ship()    { state.ship(this); }
    void deliver() { state.deliver(this); }
    void cancel()  { state.cancel(this); }
    String status() { return state.getStatus(); }
}

Order order = new Order();
System.out.println(order.status()); // PENDING
order.pay();
System.out.println(order.status()); // PAID
order.ship();
order.deliver();
System.out.println(order.status()); // DELIVERED
```

---

## Q16. What is the Flyweight pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Reduces memory usage by sharing common parts of state between many objects. Split state into intrinsic (shared) and extrinsic (unique per object).

```java
// Flyweight — intrinsic (shared) state
class CharacterGlyph {
    private final char character;  // intrinsic — shared
    private final Font font;       // intrinsic — shared
    private final int size;        // intrinsic — shared

    CharacterGlyph(char character, Font font, int size) {
        this.character = character; this.font = font; this.size = size;
    }

    void render(int x, int y, Color color) { // x, y, color = extrinsic — passed in
        System.out.printf("Rendering '%c' at (%d,%d) in %s%n", character, x, y, color);
    }
}

// Flyweight factory — cache and share
class GlyphFactory {
    private static final Map<String, CharacterGlyph> cache = new HashMap<>();

    static CharacterGlyph get(char c, Font font, int size) {
        String key = c + "-" + font + "-" + size;
        return cache.computeIfAbsent(key, k -> new CharacterGlyph(c, font, size));
    }
}

// Document with millions of characters — only ~256 glyph objects needed
class TextDocument {
    record CharInstance(CharacterGlyph glyph, int x, int y, Color color) {}
    List<CharInstance> chars = new ArrayList<>();

    void addChar(char c, Font font, int size, int x, int y, Color color) {
        CharacterGlyph glyph = GlyphFactory.get(c, font, size); // shared!
        chars.add(new CharInstance(glyph, x, y, color));
    }

    void render() { chars.forEach(ci -> ci.glyph().render(ci.x(), ci.y(), ci.color())); }
}

// Real Java examples: Integer.valueOf(-128..127), String pool
```

---

## Q17. What is the Composite pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Composes objects into tree structures. Clients treat individual objects and compositions uniformly.

```java
// Component
interface FileSystemItem {
    void display(String indent);
    long size();
}

// Leaf
class File implements FileSystemItem {
    private final String name;
    private final long sizeBytes;

    File(String name, long sizeBytes) { this.name = name; this.sizeBytes = sizeBytes; }

    @Override public void display(String indent) {
        System.out.println(indent + "📄 " + name + " (" + sizeBytes + " bytes)");
    }
    @Override public long size() { return sizeBytes; }
}

// Composite
class Directory implements FileSystemItem {
    private final String name;
    private final List<FileSystemItem> children = new ArrayList<>();

    Directory(String name) { this.name = name; }

    void add(FileSystemItem item) { children.add(item); }
    void remove(FileSystemItem item) { children.remove(item); }

    @Override public void display(String indent) {
        System.out.println(indent + "📁 " + name + "/");
        children.forEach(child -> child.display(indent + "  "));
    }
    @Override public long size() { return children.stream().mapToLong(FileSystemItem::size).sum(); }
}

// Build tree
Directory root = new Directory("root");
Directory src  = new Directory("src");
Directory test = new Directory("test");

src.add(new File("Main.java", 5000));
src.add(new File("Service.java", 12000));
test.add(new File("MainTest.java", 3000));
root.add(src); root.add(test); root.add(new File("README.md", 2000));

root.display(""); // displays entire tree
System.out.println("Total: " + root.size() + " bytes"); // 22000
```

---

## Q18. What is the Bridge pattern?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

Decouples abstraction from implementation so both can vary independently.

```java
// Implementation interface
interface Renderer {
    void renderShape(String shape, String color);
}

// Concrete implementations
class VectorRenderer implements Renderer {
    public void renderShape(String shape, String color) {
        System.out.println("Vector rendering: " + color + " " + shape);
    }
}

class RasterRenderer implements Renderer {
    public void renderShape(String shape, String color) {
        System.out.println("Raster rendering: " + color + " " + shape);
    }
}

// Abstraction
abstract class Shape {
    protected final Renderer renderer;
    Shape(Renderer renderer) { this.renderer = renderer; }
    abstract void draw(String color);
}

// Refined abstractions
class Circle extends Shape {
    Circle(Renderer r) { super(r); }
    @Override public void draw(String color) { renderer.renderShape("circle", color); }
}

class Square extends Shape {
    Square(Renderer r) { super(r); }
    @Override public void draw(String color) { renderer.renderShape("square", color); }
}

// Mix and match: 2 shapes × 2 renderers = 4 combinations without 4 subclasses
Shape vectorCircle = new Circle(new VectorRenderer());
Shape rasterSquare = new Square(new RasterRenderer());
vectorCircle.draw("red");   // Vector rendering: red circle
rasterSquare.draw("blue");  // Raster rendering: blue square
```

---

## Q19. Scenario — When do you choose which design pattern?

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

| Scenario | Pattern | Reason |
|----------|---------|--------|
| Need exactly one instance | Singleton | Controlled global access |
| Object creation is complex | Builder | Readable, immutable construction |
| Choose algorithm at runtime | Strategy | Swap implementations without changing client |
| Notify many on change | Observer | Decoupled event-driven communication |
| Simplify complex subsystem | Facade | Simple API over complex internals |
| Add behavior without subclassing | Decorator | Dynamic feature composition |
| Control access to object | Proxy | Caching, security, logging wrappers |
| Incompatible interfaces | Adapter | Bridge old/new APIs |
| Algorithm skeleton, details vary | Template Method | Code reuse, steps customizable |
| State-dependent behavior | State | Replace if-else state machines |
| Undo/redo needed | Command | Encapsulate operations as objects |
| Tree structures | Composite | Uniform treatment of leaf/container |
| Many similar objects | Flyweight | Memory optimization via sharing |
| Family of products | Abstract Factory | Consistent product families |
| Filter/process chain | Chain of Responsibility | Middleware pipeline |

---

## Q20. Scenario — How does Spring use design patterns?

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```
Singleton:    All Spring beans are Singleton by default (@Bean scope = singleton)
Factory:      ApplicationContext IS a factory for beans
Factory Method: @Bean methods are factory methods
Proxy:        AOP (@Transactional, @Cacheable) → JDK proxy or CGLIB
Decorator:    Spring Security filter chain wraps each filter
Template Method: JdbcTemplate, RestTemplate, JpaTemplate
Observer:     ApplicationEvent/ApplicationListener; @EventListener
Strategy:     ResourceLoader, TaskExecutor are strategies
Adapter:      HandlerAdapter in DispatcherServlet (adapts controllers to common interface)
Composite:    CompositeValidator, CompositeCacheManager
Facade:       JdbcTemplate (simplifies JDBC boilerplate)
Chain of Resp: Filter chain in Spring Security/Spring MVC
Command:      Spring Batch Step (encapsulates batch operation)
```

---

## Q21. What is the difference between Decorator and Proxy?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

| | Decorator | Proxy |
|-|-----------|-------|
| Purpose | Add functionality dynamically | Control access |
| Client creates? | Client constructs decorator chain | Proxy created by framework |
| Wrapping | Multiple layers, composable | Usually single layer |
| Knows about real object? | May be same reference | Proxy hides real object details |
| Examples | `BufferedInputStream(new FileInputStream())` | Spring AOP proxy, `Collections.synchronizedList` |

```java
// Decorator — client chooses layers
TextProcessor p = new TrimDecorator(new UpperCaseDecorator(new PlainTextProcessor()));

// Proxy — transparent, client shouldn't know
OrderService service = ProxyFactory.createProxy(new OrderServiceImpl()); // framework creates
// client uses service normally — doesn't know it's a proxy
service.placeOrder(order); // transparently transactional
```

---

## Q22. What is the Iterator pattern?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Provides a way to access elements of a collection sequentially without exposing its internal representation.

```java
// Custom iterator for a binary tree (in-order traversal)
class BinaryTree<T extends Comparable<T>> implements Iterable<T> {
    private Node<T> root;

    static class Node<T> {
        T value; Node<T> left, right;
        Node(T value) { this.value = value; }
    }

    // ... insert methods ...

    @Override
    public Iterator<T> iterator() {
        return new InOrderIterator();
    }

    private class InOrderIterator implements Iterator<T> {
        private final Stack<Node<T>> stack = new Stack<>();

        InOrderIterator() { pushLeft(root); }

        private void pushLeft(Node<T> node) {
            while (node != null) { stack.push(node); node = node.left; }
        }

        @Override public boolean hasNext() { return !stack.isEmpty(); }

        @Override public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            Node<T> node = stack.pop();
            T value = node.value;
            pushLeft(node.right);
            return value;
        }
    }
}

BinaryTree<Integer> tree = new BinaryTree<>();
// insert 5, 3, 7, 1, 4...
for (int val : tree) { // uses Iterator
    System.out.print(val + " "); // in-order: 1 3 4 5 7
}
```

---

## Q23. What is the Null Object pattern?

**Difficulty:** Medium | **Type:** Theory + Scenario

**Answer:**

Provides a default object that does nothing, instead of using null references. Eliminates null checks.

```java
interface Logger {
    void log(String message);
}

class ConsoleLogger implements Logger {
    @Override public void log(String message) { System.out.println("[LOG] " + message); }
}

// Null Object — does nothing
class NoOpLogger implements Logger {
    @Override public void log(String message) { } // intentionally empty
    static final Logger INSTANCE = new NoOpLogger();
}

class Service {
    private final Logger logger;

    Service(Logger logger) {
        this.logger = logger != null ? logger : NoOpLogger.INSTANCE;
    }

    void process() {
        logger.log("Starting process"); // never need null check
        // do work
        logger.log("Done");
    }
}

// Client — pass null to disable logging, no NPE
Service s1 = new Service(new ConsoleLogger()); // logs to console
Service s2 = new Service(null); // NoOpLogger — silent
```

---

## Q24. Scenario — Design a discount system using Strategy + Open-Closed Principle.

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
interface DiscountStrategy {
    double applyDiscount(double price);
    String description();
}

class NoDiscount implements DiscountStrategy {
    public double applyDiscount(double price) { return price; }
    public String description() { return "No discount"; }
}

class SeasonalDiscount implements DiscountStrategy {
    private final double percentage;
    SeasonalDiscount(double percentage) { this.percentage = percentage; }
    public double applyDiscount(double price) { return price * (1 - percentage / 100); }
    public String description() { return percentage + "% seasonal discount"; }
}

class BulkDiscount implements DiscountStrategy {
    private final int minQuantity;
    private final double discountRate;
    BulkDiscount(int minQuantity, double rate) { this.minQuantity = minQuantity; this.discountRate = rate; }
    public double applyDiscount(double price) { return price * (1 - discountRate); }
    public String description() { return "Bulk discount " + (discountRate*100) + "% for " + minQuantity + "+ items"; }
}

class PrimeDiscount implements DiscountStrategy {
    public double applyDiscount(double price) { return price * 0.85; } // 15% off
    public String description() { return "Prime member 15% discount"; }
}

// Composed discount (Decorator variant)
class CompositeDiscount implements DiscountStrategy {
    private final List<DiscountStrategy> strategies;
    CompositeDiscount(List<DiscountStrategy> strategies) { this.strategies = strategies; }
    public double applyDiscount(double price) {
        double result = price;
        for (DiscountStrategy s : strategies) result = s.applyDiscount(result); // chain
        return result;
    }
    public String description() {
        return strategies.stream().map(DiscountStrategy::description).collect(Collectors.joining(" + "));
    }
}

// Context
class PriceCalculator {
    private DiscountStrategy strategy;
    PriceCalculator(DiscountStrategy strategy) { this.strategy = strategy; }
    void setStrategy(DiscountStrategy strategy) { this.strategy = strategy; }

    double calculate(double basePrice) {
        double final_ = strategy.applyDiscount(basePrice);
        System.out.printf("%s: %.2f → %.2f%n", strategy.description(), basePrice, final_);
        return final_;
    }
}

// Adding new discount = new class — no modification needed (OCP)
PriceCalculator calc = new PriceCalculator(
    new CompositeDiscount(List.of(new SeasonalDiscount(10), new PrimeDiscount()))
);
calc.calculate(1000); // 10% seasonal + 15% prime = 76.5% of original
```

---

## Q25. What is the difference between Factory Method and Abstract Factory?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

| | Factory Method | Abstract Factory |
|-|----------------|-----------------|
| Creates | One product | Family of related products |
| Implemented via | Subclassing (override factory method) | Object composition (inject factory) |
| Scope | Single class | Multiple related classes |
| Extensibility | New subclass = new product | New factory = new product family |

```
Factory Method:
  Creator (abstract) → ConcreteCreatorA, ConcreteCreatorB
  Each creates ONE type of product

Abstract Factory:
  AbstractFactory → ConcreteFactoryA, ConcreteFactoryB
  Each creates MULTIPLE related products (Button + Checkbox + Dialog)
```

---

## Q26. Scenario — Implement a simple event-driven system.

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
// Type-safe event bus using generics
class EventBus {
    private final Map<Class<?>, List<Consumer<Object>>> handlers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add((Consumer<Object>) handler);
    }

    void publish(Object event) {
        List<Consumer<Object>> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers != null) {
            eventHandlers.forEach(h -> h.accept(event));
        }
    }
}

record UserRegisteredEvent(String userId, String email) {}
record OrderPlacedEvent(long orderId, String userId) {}

EventBus bus = new EventBus();

bus.subscribe(UserRegisteredEvent.class, event -> {
    System.out.println("Sending welcome email to: " + event.email());
});

bus.subscribe(OrderPlacedEvent.class, event -> {
    System.out.println("Processing order: " + event.orderId());
});

bus.publish(new UserRegisteredEvent("u1", "alice@example.com"));
bus.publish(new OrderPlacedEvent(101L, "u1"));
```

---

## Q27. What is the Repository pattern?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Mediates between domain and data mapping layers using collection-like interface for accessing domain objects.

```java
// Domain entity
record User(Long id, String name, String email) {}

// Repository interface — domain layer
interface UserRepository {
    Optional<User> findById(Long id);
    List<User> findByName(String name);
    void save(User user);
    void delete(Long id);
    List<User> findAll();
}

// JPA implementation — infrastructure layer
class JpaUserRepository implements UserRepository {
    @PersistenceContext EntityManager em;

    @Override public Optional<User> findById(Long id) {
        return Optional.ofNullable(em.find(UserEntity.class, id))
                       .map(this::toDomain);
    }

    @Override public void save(User user) {
        em.merge(toEntity(user));
    }
    // ...
}

// In-memory implementation — testing
class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> store = new HashMap<>();
    private long nextId = 1;

    @Override public Optional<User> findById(Long id) { return Optional.ofNullable(store.get(id)); }
    @Override public void save(User user) {
        store.put(user.id() != null ? user.id() : nextId++, user);
    }
    @Override public List<User> findAll() { return new ArrayList<>(store.values()); }
    // ...
}

// Service depends on abstraction — swap implementations freely
class UserService {
    private final UserRepository repo;
    UserService(UserRepository repo) { this.repo = repo; }

    User getUser(Long id) {
        return repo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}
```

---

## Q28. What is the Visitor pattern?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

Separates an algorithm from the objects it operates on. Lets you add operations without modifying the classes.

```java
// Visitor interface
interface ShapeVisitor {
    double visit(Circle circle);
    double visit(Rectangle rectangle);
    double visit(Triangle triangle);
}

// Element interface
interface Shape {
    double accept(ShapeVisitor visitor);
}

// Concrete elements
class Circle implements Shape {
    double radius;
    Circle(double radius) { this.radius = radius; }
    @Override public double accept(ShapeVisitor v) { return v.visit(this); }
}

class Rectangle implements Shape {
    double width, height;
    Rectangle(double w, double h) { this.width = w; this.height = h; }
    @Override public double accept(ShapeVisitor v) { return v.visit(this); }
}

// Concrete visitors — add operations without modifying shapes
class AreaCalculator implements ShapeVisitor {
    public double visit(Circle c)    { return Math.PI * c.radius * c.radius; }
    public double visit(Rectangle r) { return r.width * r.height; }
    public double visit(Triangle t)  { return 0.5 * t.base * t.height; }
}

class PerimeterCalculator implements ShapeVisitor {
    public double visit(Circle c)    { return 2 * Math.PI * c.radius; }
    public double visit(Rectangle r) { return 2 * (r.width + r.height); }
    public double visit(Triangle t)  { return t.side1 + t.side2 + t.side3; }
}

// Usage
List<Shape> shapes = List.of(new Circle(5), new Rectangle(3, 4));
ShapeVisitor area = new AreaCalculator();
shapes.forEach(s -> System.out.println("Area: " + s.accept(area)));
```

---

## Q29. What anti-patterns should you avoid?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

```
God Class:
  → One class does everything (500+ lines, 50+ methods)
  → Fix: Extract classes by responsibility (SRP)

Singleton Abuse:
  → Overusing Singleton creates hidden global state, makes testing hard
  → Fix: Dependency injection — let framework manage lifecycle

Premature Optimization:
  → Optimizing before profiling; object pools, complex caching before measuring
  → Fix: Profile first, optimize hotspots

Anemic Domain Model:
  → Entity classes with only getters/setters, all logic in services
  → Fix: Move behavior into entities (DDD)

Copy-Paste Programming:
  → Same code copy-pasted in multiple places
  → Fix: Extract method, Template Method pattern

Magic Numbers/Strings:
  → if (status == 3) → if (status == Status.SHIPPED.ordinal())
  → Fix: Named constants, enums

Returning null instead of empty:
  → getUsers() returns null instead of empty List
  → Fix: Return Collections.emptyList(), Optional

Service Locator:
  → Components look up their dependencies from a registry
  → Makes dependencies hidden, testing hard
  → Fix: Constructor injection (DI)
```

---

## Q30. What is the difference between MVC, MVP, and MVVM?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

All separate concerns between Model (data/business), View (UI), and something in between:

| | MVC | MVP | MVVM |
|-|-----|-----|------|
| Mediator | Controller | Presenter | ViewModel |
| View knows Model? | Yes (indirectly) | No | No |
| Testability | Hard (View-Controller coupled) | Good (Presenter testable) | Best (ViewModel testable) |
| Data binding | Manual | Manual | Automatic (two-way) |
| Used in | Spring MVC, Struts | Android (classic) | Angular, Vue, Android (modern) |

---

## Summary — Key Takeaways for Interviews

| Pattern | Core idea | One-liner |
|---------|-----------|-----------|
| Singleton | One instance | Use Enum or Holder idiom |
| Builder | Step-by-step construction | Telescoping constructor alternative |
| Factory | Create without specifying class | `ConnectionFactory.create("mysql")` |
| Strategy | Swappable algorithms | `sorter.setStrategy(new QuickSort())` |
| Observer | One-to-many notification | EventBus, event-driven systems |
| Template Method | Algorithm skeleton | Base defines order, subclass fills steps |
| Decorator | Dynamic feature addition | `new BufferedInput(new FileInput(...))` |
| Proxy | Controlled access | Caching, security, logging wrappers |
| Adapter | Interface translation | Legacy API → modern interface |
| Facade | Simplified API | Hide subsystem complexity |
| Command | Encapsulate operation | Undo, redo, queuing |
| Chain of Resp. | Filter pipeline | Middleware, security filters |
| State | State-dependent behavior | Replace state if-else |
| Composite | Tree structures | File system, UI hierarchy |
