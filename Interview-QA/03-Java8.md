# Java 8 Features — Java Interview Guide (8–10 Years Experience)

## Quick Reference

```
Lambda          → (params) -> body
Functional IF   → @FunctionalInterface (single abstract method)
Built-in FI     → Predicate<T>, Function<T,R>, Consumer<T>, Supplier<T>, BiXxx variants
Method Ref      → Class::method, instance::method, Class::new
Stream          → source → intermediate ops → terminal op
Optional        → container that may or may not hold a value
Date/Time       → LocalDate, LocalTime, LocalDateTime, ZonedDateTime, Period, Duration
Default method  → interface method with body (keyword: default)
Static method   → interface static method
```

---

## Q1. What is a Lambda expression? What are its rules?

**Difficulty:** Basic | **Type:** Theory

**Answer:**

A lambda is an **anonymous function** — a short block of code passed as an argument or stored in a variable. It implements a **functional interface** (interface with exactly one abstract method).

**Syntax:** `(parameters) -> expression` or `(parameters) -> { statements; }`

```java
// Before Java 8 — anonymous class
Runnable r1 = new Runnable() {
    @Override public void run() { System.out.println("Hello"); }
};

// Java 8 — lambda
Runnable r2 = () -> System.out.println("Hello");

// With parameters
Comparator<String> c1 = (a, b) -> a.compareTo(b);

// Block body with return
Comparator<String> c2 = (a, b) -> {
    System.out.println("Comparing: " + a + " vs " + b);
    return a.compareTo(b);
};

// Type inference — compiler infers parameter types
List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
names.sort((a, b) -> a.compareTo(b));
```

**Rules:**
- Can only be used where a functional interface is expected
- Can access `effectively final` local variables from enclosing scope
- Can access instance/static fields freely
- If single expression: return is implicit, no braces needed
- If single parameter: parentheses optional: `x -> x * 2`

---

## Q2. What is a Functional Interface? What built-in ones exist?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

A **Functional Interface** has exactly one abstract method (SAM — Single Abstract Method). Annotated with `@FunctionalInterface` (optional but recommended).

```java
@FunctionalInterface
interface Transformer<T, R> {
    R transform(T input);
    // can have default and static methods — still functional
    default void describe() { System.out.println("Transforms input"); }
}
```

**Built-in functional interfaces (`java.util.function`):**

| Interface | Abstract Method | Use case |
|-----------|----------------|----------|
| `Predicate<T>` | `boolean test(T t)` | Test a condition |
| `Function<T,R>` | `R apply(T t)` | Transform T to R |
| `Consumer<T>` | `void accept(T t)` | Consume/process T |
| `Supplier<T>` | `T get()` | Provide/create T |
| `UnaryOperator<T>` | `T apply(T t)` | Transform T to same T |
| `BinaryOperator<T>` | `T apply(T t1, T t2)` | Combine two T into T |
| `BiPredicate<T,U>` | `boolean test(T t, U u)` | Test two arguments |
| `BiFunction<T,U,R>` | `R apply(T t, U u)` | Transform two args to R |
| `BiConsumer<T,U>` | `void accept(T t, U u)` | Consume two arguments |

```java
Predicate<String>    isEmpty  = String::isEmpty;
Function<String,Integer> len = String::length;
Consumer<String>     printer = System.out::println;
Supplier<List<String>> listMaker = ArrayList::new;
UnaryOperator<String> upper  = String::toUpperCase;
BinaryOperator<Integer> sum  = Integer::sum;

// Chaining
Predicate<String> notEmpty = isEmpty.negate();
Predicate<String> longAndNotEmpty = notEmpty.and(s -> s.length() > 5);

Function<String,String> trimAndUpper = ((Function<String,String>) String::trim).andThen(upper);
System.out.println(trimAndUpper.apply("  hello world  ")); // HELLO WORLD
```

---

## Q3. What are method references? What are the four types?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Method references are a shorthand for lambdas that simply call an existing method.

| Type | Syntax | Lambda equivalent |
|------|--------|-------------------|
| Static method | `Class::staticMethod` | `(args) -> Class.staticMethod(args)` |
| Instance method on instance | `obj::instanceMethod` | `(args) -> obj.instanceMethod(args)` |
| Instance method on type | `Class::instanceMethod` | `(obj, args) -> obj.instanceMethod(args)` |
| Constructor | `Class::new` | `(args) -> new Class(args)` |

```java
// 1. Static method reference
Function<String, Integer> parseInt = Integer::parseInt; // Integer.parseInt(s)
System.out.println(parseInt.apply("42")); // 42

// 2. Instance method on specific instance
String prefix = "Hello ";
Function<String, String> greet = prefix::concat; // prefix.concat(s)
System.out.println(greet.apply("World")); // Hello World

// 3. Instance method on arbitrary instance
Function<String, String> toUpper = String::toUpperCase; // s.toUpperCase()
Predicate<String> isBlank = String::isBlank; // s.isBlank()

// 4. Constructor reference
Supplier<ArrayList<String>> listFactory = ArrayList::new;
ArrayList<String> list = listFactory.get();

Function<String, StringBuilder> sbFactory = StringBuilder::new; // new StringBuilder(s)
```

---

## Q4. What is `Predicate`? Show chaining with `and`, `or`, `negate`.

**Difficulty:** Medium | **Type:** Theory + Scenario

**Answer:**

```java
Predicate<Integer> isPositive  = n -> n > 0;
Predicate<Integer> isEven      = n -> n % 2 == 0;
Predicate<Integer> isLessThan10 = n -> n < 10;

// Composition
Predicate<Integer> isPositiveEven       = isPositive.and(isEven);
Predicate<Integer> isPositiveOrEven     = isPositive.or(isEven);
Predicate<Integer> isNotPositive        = isPositive.negate();
Predicate<Integer> positiveEvenUnder10  = isPositive.and(isEven).and(isLessThan10);

List<Integer> numbers = Arrays.asList(-4, -1, 0, 2, 3, 6, 8, 12);

// Filter using composed predicate
numbers.stream()
    .filter(positiveEvenUnder10)
    .forEach(System.out::println); // 2, 6, 8

// Predicate.not (Java 11+)
List<String> words = Arrays.asList("hello", "", "world", " ", "java");
words.stream()
    .filter(Predicate.not(String::isBlank))
    .forEach(System.out::println); // hello, world, java
```

---

## Q5. What is `Function`? Show `andThen` and `compose`.

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
Function<String, String>  trim     = String::trim;
Function<String, String>  toUpper  = String::toUpperCase;
Function<String, Integer> length   = String::length;

// andThen — f then g: g(f(x))
Function<String, String> trimThenUpper = trim.andThen(toUpper);
System.out.println(trimThenUpper.apply("  hello  ")); // HELLO

// compose — g then f: f(g(x))  (reverse of andThen)
Function<String, String> upperThenTrim = trim.compose(toUpper); // toUpper first, then trim
System.out.println(upperThenTrim.apply("  hello  ")); // HELLO (trim has no effect after upper)

// Chaining multiple functions
Function<String, Integer> pipeline = trim.andThen(toUpper).andThen(length);
System.out.println(pipeline.apply("  hello world  ")); // 11

// BiFunction — two inputs, one output
BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
System.out.println(repeat.apply("ha", 3)); // hahaha

// BiFunction.andThen
BiFunction<Integer, Integer, Integer> multiply = (a, b) -> a * b;
Function<Integer, String> toStr = n -> "Result: " + n;
BiFunction<Integer, Integer, String> multiplyToStr = multiply.andThen(toStr);
System.out.println(multiplyToStr.apply(3, 4)); // Result: 12
```

---

## Q6. What is `Consumer`? Show `andThen`.

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`Consumer<T>` takes an argument, performs an action, returns nothing (void).

```java
Consumer<String> print  = System.out::println;
Consumer<String> log    = s -> System.out.println("[LOG] " + s);
Consumer<String> save   = s -> System.out.println("[SAVE] " + s);

// andThen — chain consumers
Consumer<String> printAndLog = print.andThen(log);
printAndLog.accept("Hello");
// Hello
// [LOG] Hello

// Chain multiple
Consumer<String> full = print.andThen(log).andThen(save);
full.accept("Event");

// BiConsumer
BiConsumer<String, Integer> printN = (s, n) -> {
    for (int i = 0; i < n; i++) System.out.println(s);
};
printN.accept("hi", 3); // hi hi hi

// forEach uses Consumer
List<String> names = List.of("Alice", "Bob", "Charlie");
names.forEach(name -> System.out.println("Hello, " + name));
Map<String, Integer> map = Map.of("a", 1, "b", 2);
map.forEach((k, v) -> System.out.println(k + "=" + v)); // BiConsumer
```

---

## Q7. What is `Supplier`? When to use it?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`Supplier<T>` takes no arguments and returns a value. Used for lazy initialization, factory methods, deferred computation.

```java
Supplier<String> greeting = () -> "Hello, World!";
System.out.println(greeting.get()); // Hello, World!

// Lazy initialization
Supplier<List<String>> lazyList = () -> {
    System.out.println("Creating list...");
    return new ArrayList<>();
};
// List not created yet
List<String> list = lazyList.get(); // Created only when needed

// Factory pattern
Supplier<Connection> connectionFactory = () -> createDbConnection();

// Optional with Supplier
Optional<String> opt = Optional.empty();
String value = opt.orElseGet(() -> "default"); // Supplier called only if empty

// Expensive computation deferred
Supplier<String> expensiveReport = () -> generateHugeReport();
if (someCondition) {
    System.out.println(expensiveReport.get()); // computed only if needed
}
```

---

## Q8. What is `Optional`? How to use it correctly?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

`Optional<T>` is a container that may or may not hold a value. Designed to avoid `NullPointerException` and force explicit null handling.

```java
// Creating Optional
Optional<String> present = Optional.of("Hello");       // throws NPE if null
Optional<String> nullable = Optional.ofNullable(null); // OK with null
Optional<String> empty    = Optional.empty();

// Checking and getting
present.isPresent(); // true
present.isEmpty();   // false (Java 11+)
present.get();       // "Hello" (throws NoSuchElementException if empty)

// Safe access
present.orElse("default");              // "Hello"
empty.orElse("default");               // "default"
empty.orElseGet(() -> computeDefault()); // lazy (Supplier)
empty.orElseThrow(() -> new RuntimeException("Not found")); // throw if empty

// Transformations
Optional<Integer> length = present.map(String::length);        // Optional[5]
Optional<String>  upper  = present.map(String::toUpperCase);   // Optional[HELLO]

// flatMap — when mapping returns Optional
Optional<String> name = Optional.of("Alice");
Optional<String> upperName = name.flatMap(n -> Optional.of(n.toUpperCase()));

// filter
Optional<String> longWord = present.filter(s -> s.length() > 3); // present if > 3 chars

// ifPresent / ifPresentOrElse (Java 9+)
present.ifPresent(s -> System.out.println("Found: " + s));
empty.ifPresentOrElse(
    s -> System.out.println("Found: " + s),
    () -> System.out.println("Not found")
);

// Real-world: service layer
Optional<User> findUserById(int id) {
    return Optional.ofNullable(userRepo.findById(id));
}

// Caller handles absence explicitly
findUserById(1)
    .map(User::getEmail)
    .filter(email -> email.contains("@"))
    .ifPresent(email -> sendEmail(email));
```

**Anti-patterns to avoid:**
```java
// BAD — defeats the purpose
if (opt.isPresent()) { User u = opt.get(); } // just use ifPresent or map

// BAD — Optional as method parameter
void process(Optional<String> name) { } // use overloads instead

// BAD — Optional as field
class User { Optional<String> phone; } // use nullable field with @Nullable annotation
```

---

## Q9. What is the Stream API? Explain intermediate vs terminal operations.

**Difficulty:** Medium | **Type:** Theory

**Answer:**

A Stream is a sequence of elements supporting sequential or parallel aggregate operations. Streams are **lazy** — intermediate operations don't execute until a terminal operation is called.

**Pipeline:** `source → intermediate (lazy) → terminal (triggers execution)`

| Type | Operations | Returns |
|------|-----------|---------|
| Intermediate (lazy) | `filter`, `map`, `flatMap`, `distinct`, `sorted`, `peek`, `limit`, `skip` | `Stream` |
| Terminal (eager) | `forEach`, `collect`, `reduce`, `count`, `findFirst`, `findAny`, `anyMatch`, `allMatch`, `noneMatch`, `min`, `max`, `toArray` | Non-Stream value |

```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "Alice", "Dave", "Bob");

long count = names.stream()             // source
    .filter(n -> n.length() > 3)        // intermediate — lazy
    .distinct()                          // intermediate — lazy
    .sorted()                            // intermediate — lazy
    .peek(n -> System.out.println("Processing: " + n)) // debug peek
    .count();                            // terminal — triggers all above

System.out.println("Count: " + count);
// Processing: Alice
// Processing: Charlie
// Processing: Dave
// Count: 3
```

---

## Q10. What is `flatMap`? How is it different from `map`?

**Difficulty:** Senior | **Type:** Theory + Tricky

**Answer:**

- `map` — transforms each element to one output element → `Stream<Stream<T>>`
- `flatMap` — transforms each element to zero or more elements and flattens → `Stream<T>`

```java
// map — each list becomes a Stream<String>, result is Stream<List<String>>
List<List<String>> nested = Arrays.asList(
    Arrays.asList("Alice", "Bob"),
    Arrays.asList("Charlie", "Dave")
);

// Without flatMap
Stream<List<String>> streamsOfLists = nested.stream().map(Collection::stream); // Stream<Stream>

// With flatMap — flattens into single Stream<String>
List<String> flat = nested.stream()
    .flatMap(Collection::stream)
    .collect(Collectors.toList());
System.out.println(flat); // [Alice, Bob, Charlie, Dave]

// Real use: split sentences into words
List<String> sentences = Arrays.asList("Hello World", "Java 8 Streams");
List<String> words = sentences.stream()
    .flatMap(sentence -> Arrays.stream(sentence.split(" ")))
    .distinct()
    .sorted()
    .collect(Collectors.toList());
System.out.println(words); // [8, Hello, Java, Streams, World]

// flatMapToInt for primitives
OptionalInt max = sentences.stream()
    .flatMapToInt(s -> s.chars())
    .max();
```

---

## Q11. What are `Collectors`? Show common collectors.

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

```java
List<Employee> employees = Arrays.asList(
    new Employee("Alice", "Engineering", 90000),
    new Employee("Bob", "Marketing", 60000),
    new Employee("Charlie", "Engineering", 80000),
    new Employee("Dave", "Marketing", 70000),
    new Employee("Eve", "Engineering", 95000)
);

// toList, toSet
List<String> names = employees.stream().map(Employee::getName).collect(Collectors.toList());

// joining
String namesCsv = employees.stream()
    .map(Employee::getName)
    .collect(Collectors.joining(", ", "[", "]"));
System.out.println(namesCsv); // [Alice, Bob, Charlie, Dave, Eve]

// groupingBy
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// groupingBy with downstream collector
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));
// {Engineering=3, Marketing=2}

Map<String, Double> avgSalaryByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment,
             Collectors.averagingDouble(Employee::getSalary)));

Map<String, Optional<Employee>> highestByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment,
             Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary))));

// partitioningBy — splits into true/false
Map<Boolean, List<Employee>> partition = employees.stream()
    .collect(Collectors.partitioningBy(e -> e.getSalary() > 75000));
// {true=[Alice, Charlie, Eve], false=[Bob, Dave]}

// toMap
Map<String, Double> salaryMap = employees.stream()
    .collect(Collectors.toMap(Employee::getName, Employee::getSalary));

// summarizingDouble
DoubleSummaryStatistics stats = employees.stream()
    .collect(Collectors.summarizingDouble(Employee::getSalary));
System.out.println("Avg: " + stats.getAverage() + ", Max: " + stats.getMax());

// Collectors.toUnmodifiableList (Java 10+)
List<String> immutableNames = employees.stream()
    .map(Employee::getName)
    .collect(Collectors.toUnmodifiableList());
```

---

## Q12. What is `reduce`? How does it work?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`reduce` combines stream elements into a single result using an accumulator function.

```java
// reduce(identity, accumulator)
int sum = IntStream.rangeClosed(1, 10).reduce(0, Integer::sum); // 55

List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5);

// Sum
int total = nums.stream().reduce(0, (a, b) -> a + b);

// Product
int product = nums.stream().reduce(1, (a, b) -> a * b); // 120

// Max without identity — returns Optional (stream may be empty)
Optional<Integer> max = nums.stream().reduce(Integer::max);

// String concatenation (StringBuilder is better for performance)
String concat = Stream.of("a", "b", "c").reduce("", (a, b) -> a + b); // "abc"

// Three-arg reduce (for parallel streams)
// reduce(identity, accumulator, combiner)
// combiner combines partial results from parallel threads
int parallelSum = nums.parallelStream().reduce(0, Integer::sum, Integer::sum);

// Real-world: factorial
long factorial = LongStream.rangeClosed(1, 10).reduce(1L, (a, b) -> a * b);
System.out.println(factorial); // 3628800
```

---

## Q13. What is `Optional.stream()` and `Stream.ofNullable()`? (Java 9+)

**Difficulty:** Senior | **Type:** Theory

**Answer:**

```java
// Optional.stream() — convert Optional to Stream (0 or 1 element)
Optional<String> opt = Optional.of("hello");
opt.stream().forEach(System.out::println); // hello

Optional.empty().stream().forEach(System.out::println); // nothing

// Useful in flatMap to filter nulls
List<Optional<String>> optionals = Arrays.asList(
    Optional.of("a"), Optional.empty(), Optional.of("b"), Optional.empty()
);
List<String> values = optionals.stream()
    .flatMap(Optional::stream) // replaces filter(Optional::isPresent).map(Optional::get)
    .collect(Collectors.toList());
System.out.println(values); // [a, b]

// Stream.ofNullable (Java 9+) — 0 or 1 element stream
Stream.ofNullable(null).forEach(System.out::println);   // nothing
Stream.ofNullable("hi").forEach(System.out::println);   // hi
```

---

## Q14. What are default methods in interfaces? How to resolve diamond problem?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Default methods allow adding method implementations to interfaces without breaking existing implementations. This was needed to add bulk methods like `forEach`, `stream()` to `Collection` in Java 8.

```java
interface Printable {
    default void print() { System.out.println("Printable default"); }
}

interface Loggable {
    default void print() { System.out.println("Loggable default"); }
}

// Diamond problem resolution
class Document implements Printable, Loggable {
    @Override
    public void print() {
        Printable.super.print(); // explicitly choose one
        // or provide own implementation
        System.out.println("Document print");
    }
}

// Interface with utility static methods
interface StringUtils {
    static String capitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
System.out.println(StringUtils.capitalize("hello")); // Hello
```

---

## Q15. What is the `java.time` API? How is it different from `Date`/`Calendar`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Problems with old `Date`/`Calendar`:
- Mutable — not thread-safe
- Month is 0-based in `Calendar`
- No separation of date vs time vs timezone

`java.time` (Java 8, inspired by Joda-Time):
- **Immutable and thread-safe**
- Clear separation of concerns
- Human-readable API

```java
// Core types
LocalDate date = LocalDate.now();              // date only: 2024-01-15
LocalTime time = LocalTime.now();              // time only: 10:30:45.123
LocalDateTime dateTime = LocalDateTime.now();  // date + time, no timezone
ZonedDateTime zoned = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")); // with timezone
Instant instant = Instant.now();               // machine timestamp (epoch)

// Creating specific dates
LocalDate dob = LocalDate.of(1995, Month.JUNE, 15);
LocalDate dob2 = LocalDate.of(1995, 6, 15); // same
LocalDate parsed = LocalDate.parse("2024-01-15"); // ISO format

// Operations (all return new instance — immutable)
LocalDate tomorrow = date.plusDays(1);
LocalDate lastYear = date.minusYears(1);
LocalDate nextMonday = date.with(DayOfWeek.MONDAY); // adjust to next Monday

// Period (date-based) and Duration (time-based)
Period age = Period.between(dob, LocalDate.now());
System.out.println("Age: " + age.getYears() + " years");

Duration workDay = Duration.ofHours(8);
Duration meetingLength = Duration.between(
    LocalTime.of(10, 0), LocalTime.of(11, 30));
System.out.println("Meeting: " + meetingLength.toMinutes() + " minutes"); // 90

// Formatting
DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
String formatted = dateTime.format(fmt);
LocalDateTime parsed2 = LocalDateTime.parse("15/01/2024 10:30", fmt);

// Comparison
LocalDate d1 = LocalDate.of(2024, 1, 1);
LocalDate d2 = LocalDate.of(2024, 6, 15);
System.out.println(d1.isBefore(d2));  // true
System.out.println(d1.isAfter(d2));   // false
System.out.println(ChronoUnit.DAYS.between(d1, d2)); // 166
```

---

## Q16. What is `Stream.parallel()`? When to use it?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

`parallelStream()` / `.parallel()` splits the stream into multiple chunks and processes them concurrently using the **ForkJoinPool.commonPool()**.

```java
// Sequential vs Parallel
List<Integer> numbers = IntStream.rangeClosed(1, 1_000_000).boxed().collect(Collectors.toList());

long seqSum  = numbers.stream().mapToLong(Integer::longValue).sum();
long paraSum = numbers.parallelStream().mapToLong(Integer::longValue).sum();

// Both give same result for associative operations
System.out.println(seqSum == paraSum); // true
```

**When parallel is beneficial:**
- Large data sets (> 10,000 elements typically)
- CPU-bound operations (not I/O-bound)
- Operations that are independent (stateless, no shared mutable state)
- Associative/commutative operations (sum, max, min — order doesn't matter)

**Pitfalls:**
```java
// BAD — stateful, shared mutable state — race condition
List<Integer> result = new ArrayList<>();
numbers.parallelStream().forEach(result::add); // ❌ not thread-safe

// GOOD — use collect
List<Integer> result2 = numbers.parallelStream().collect(Collectors.toList()); // ✅

// BAD — parallel with ordered operations (encounter order maintained = overhead)
numbers.parallelStream().sorted().limit(10).collect(Collectors.toList()); // sequential is faster

// BAD — I/O operations (threads wait on I/O, no CPU gain)
urls.parallelStream().map(url -> fetchFromInternet(url)).collect(Collectors.toList()); // ❌
```

---

## Q17. What is `Stream.iterate()` and `Stream.generate()`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
// Stream.iterate — generates infinite sequence
Stream.iterate(0, n -> n + 2)
    .limit(5)
    .forEach(System.out::println); // 0 2 4 6 8

// Java 9+ — iterate with predicate (no need for limit)
Stream.iterate(1, n -> n <= 100, n -> n * 2)
    .forEach(System.out::println); // 1 2 4 8 16 32 64

// Fibonacci
Stream.iterate(new int[]{0, 1}, f -> new int[]{f[1], f[0] + f[1]})
    .limit(10)
    .map(f -> f[0])
    .forEach(System.out::print); // 0 1 1 2 3 5 8 13 21 34

// Stream.generate — infinite stream from Supplier
Stream.generate(Math::random).limit(3).forEach(System.out::println);
Stream.generate(() -> "hello").limit(3).forEach(System.out::println);

// UUID generator
Stream.generate(UUID::randomUUID).limit(5).forEach(System.out::println);
```

---

## Q18. What are primitive streams? (`IntStream`, `LongStream`, `DoubleStream`)

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Primitive streams avoid boxing/unboxing overhead of `Stream<Integer>`, `Stream<Long>`, `Stream<Double>`.

```java
// IntStream — range operations
IntStream.range(1, 5).forEach(System.out::print);       // 1234 (exclusive end)
IntStream.rangeClosed(1, 5).forEach(System.out::print);  // 12345 (inclusive end)

// Statistical operations
IntStream nums = IntStream.of(3, 1, 4, 1, 5, 9, 2, 6);
System.out.println(nums.sum());     // 31
System.out.println(nums.average()); // OptionalDouble[3.875]
System.out.println(nums.min());     // OptionalInt[1]
System.out.println(nums.max());     // OptionalInt[9]

IntSummaryStatistics stats = IntStream.rangeClosed(1, 10).summaryStatistics();
System.out.println(stats); // count=10, sum=55, min=1, average=5.5, max=10

// Boxing — convert to Stream<Integer>
Stream<Integer> boxed = IntStream.rangeClosed(1, 5).boxed();

// mapToInt — convert Stream to IntStream (unbox)
List<String> words = List.of("hello", "world", "java");
int totalLength = words.stream().mapToInt(String::length).sum(); // 14

// mapToObj — IntStream back to Stream
Stream<String> chars = IntStream.rangeClosed('a', 'z')
    .mapToObj(c -> String.valueOf((char) c));
```

---

## Q19. Tricky — What is the output? (Stream short-circuit evaluation)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5);

Optional<Integer> result = nums.stream()
    .filter(n -> {
        System.out.println("filter: " + n);
        return n % 2 == 0;
    })
    .map(n -> {
        System.out.println("map: " + n);
        return n * 10;
    })
    .findFirst();

System.out.println("Result: " + result.get());
```

**Output:**
```
filter: 1
filter: 2
map: 2
Result: 20
```

**Why?** `findFirst()` is a short-circuit terminal operation. Once the first match is found (2), processing stops. Elements 3, 4, 5 are never processed. This demonstrates **lazy evaluation** — elements are processed one at a time through the pipeline, not in bulk stages.

---

## Q20. What is the difference between `findFirst()` and `findAny()`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| | `findFirst()` | `findAny()` |
|-|--------------|-------------|
| Sequential | Returns first element | Returns first element |
| Parallel | Returns first in encounter order (with overhead) | Returns any element (non-deterministic, faster) |
| Returns | `Optional<T>` | `Optional<T>` |

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// Sequential — both return same (first even = 2)
System.out.println(numbers.stream().filter(n -> n % 2 == 0).findFirst()); // Optional[2]
System.out.println(numbers.stream().filter(n -> n % 2 == 0).findAny());   // Optional[2]

// Parallel — findFirst still returns 2 (encounter order preserved)
//            findAny may return 2, 4, 6, 8, or 10 (whichever thread finishes first)
System.out.println(numbers.parallelStream().filter(n -> n % 2 == 0).findFirst()); // Optional[2]
System.out.println(numbers.parallelStream().filter(n -> n % 2 == 0).findAny());   // non-deterministic
```

**Rule:** Use `findAny()` in parallel streams when you don't care about which element is returned — it's faster because it doesn't have to maintain encounter order.

---

## Q21. What is `Comparator.comparing()` and how to chain it?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
record Person(String name, int age, String city) {}

List<Person> people = Arrays.asList(
    new Person("Alice", 30, "Mumbai"),
    new Person("Bob", 25, "Delhi"),
    new Person("Charlie", 30, "Mumbai"),
    new Person("Dave", 25, "Delhi"),
    new Person("Eve", 28, "Mumbai")
);

// Single key sort
people.sort(Comparator.comparing(Person::name));
people.sort(Comparator.comparingInt(Person::age));

// Reversed
people.sort(Comparator.comparingInt(Person::age).reversed());

// Multi-key chaining (thenComparing)
people.sort(
    Comparator.comparingInt(Person::age)
              .thenComparing(Person::name)         // if same age, sort by name
              .thenComparing(Person::city)          // if same age+name, sort by city
);

// Null-safe comparator
List<String> withNulls = Arrays.asList("b", null, "a", null, "c");
withNulls.sort(Comparator.nullsFirst(Comparator.naturalOrder()));  // [null, null, a, b, c]
withNulls.sort(Comparator.nullsLast(Comparator.naturalOrder()));   // [a, b, c, null, null]
```

---

## Q22. Scenario — Using Streams to process employee data.

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
record Employee(String name, String dept, double salary, int yearsExp) {}

List<Employee> employees = Arrays.asList(
    new Employee("Alice", "Engineering", 90000, 8),
    new Employee("Bob", "Marketing", 60000, 3),
    new Employee("Charlie", "Engineering", 80000, 6),
    new Employee("Dave", "Marketing", 70000, 5),
    new Employee("Eve", "Engineering", 95000, 10),
    new Employee("Frank", "HR", 55000, 2)
);

// 1. Get all Engineering employees sorted by salary desc
List<String> engNames = employees.stream()
    .filter(e -> "Engineering".equals(e.dept()))
    .sorted(Comparator.comparingDouble(Employee::salary).reversed())
    .map(Employee::name)
    .collect(Collectors.toList());
System.out.println(engNames); // [Eve, Alice, Charlie]

// 2. Average salary per department
Map<String, Double> avgByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::dept,
             Collectors.averagingDouble(Employee::salary)));

// 3. Highest paid in each department
Map<String, Optional<Employee>> topByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::dept,
             Collectors.maxBy(Comparator.comparingDouble(Employee::salary))));

// 4. Total salary bill
double totalSalary = employees.stream().mapToDouble(Employee::salary).sum();

// 5. Senior employees (exp > 5) earning less than avg salary
double avgSalary = employees.stream().mapToDouble(Employee::salary).average().orElse(0);
List<Employee> underpaidSeniors = employees.stream()
    .filter(e -> e.yearsExp() > 5 && e.salary() < avgSalary)
    .collect(Collectors.toList());

// 6. Group into high/low earners (> 75000)
Map<Boolean, List<Employee>> incomeGroups = employees.stream()
    .collect(Collectors.partitioningBy(e -> e.salary() > 75000));

// 7. Department with highest average salary
String bestDept = avgByDept.entrySet().stream()
    .max(Map.Entry.comparingByValue())
    .map(Map.Entry::getKey)
    .orElse("N/A");
System.out.println("Best paying dept: " + bestDept); // Engineering
```

---

## Q23. What is `UnaryOperator` and `BinaryOperator`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

- `UnaryOperator<T>` extends `Function<T,T>` — same input and output type
- `BinaryOperator<T>` extends `BiFunction<T,T,T>` — two inputs of same type, same output type

```java
UnaryOperator<String> trim  = String::trim;
UnaryOperator<String> upper = String::toUpperCase;
UnaryOperator<String> pipeline = trim.andThen(upper);
System.out.println(pipeline.apply("  hello  ")); // HELLO

BinaryOperator<Integer> max = Integer::max;
BinaryOperator<String> concat = (a, b) -> a + " " + b;

// List.replaceAll takes UnaryOperator
List<String> names = new ArrayList<>(Arrays.asList("  alice  ", " bob ", "charlie "));
names.replaceAll(String::trim);
System.out.println(names); // [alice, bob, charlie]

// reduce uses BinaryOperator
List<Integer> nums = Arrays.asList(3, 1, 4, 1, 5);
int product = nums.stream().reduce(1, (a, b) -> a * b);
```

---

## Q24. Tricky — Can a stream be reused? What happens?

**Difficulty:** Tricky | **Type:** Tricky

```java
Stream<String> stream = Stream.of("a", "b", "c");
stream.forEach(System.out::println); // a b c
stream.forEach(System.out::println); // ❌ IllegalStateException: stream has already been operated upon or closed
```

**Answer:** No — a Stream can only be traversed **once**. After a terminal operation, the stream is **closed**. Attempting to reuse it throws `IllegalStateException`.

**Fix:** Create a new stream each time, or use a `Supplier<Stream<T>>`:
```java
Supplier<Stream<String>> streamSupplier = () -> Stream.of("a", "b", "c");
streamSupplier.get().forEach(System.out::println); // OK
streamSupplier.get().count(); // OK — fresh stream
```

---

## Q25. What is `peek()`? Is it safe for production use?

**Difficulty:** Senior | **Type:** Tricky

**Answer:**

`peek()` is an intermediate operation that performs an action on each element **without modifying** the stream. Primarily designed for debugging.

```java
List<String> result = Stream.of("alice", "bob", "charlie", "dave")
    .filter(s -> s.length() > 3)
    .peek(s -> System.out.println("After filter: " + s))
    .map(String::toUpperCase)
    .peek(s -> System.out.println("After map: " + s))
    .collect(Collectors.toList());
// After filter: alice
// After map: ALICE
// After filter: charlie
// After map: CHARLIE
// After filter: dave
// After map: DAVE
```

**Warning:** Do NOT use `peek()` for side effects like saving to DB or sending emails — in parallel streams, order is not guaranteed; in sequential streams with short-circuit terminals, it may not be called for all elements.

---

## Q26. What is `Collectors.toMap()` and what is the duplicate key problem?

**Difficulty:** Senior | **Type:** Tricky

**Answer:**

```java
List<String> words = Arrays.asList("hello", "world", "java");

// toMap(keyMapper, valueMapper)
Map<String, Integer> wordLengths = words.stream()
    .collect(Collectors.toMap(w -> w, String::length));

// TRICKY: duplicate keys throw IllegalStateException
List<String> withDupes = Arrays.asList("hello", "world", "hello");
// withDupes.stream().collect(Collectors.toMap(w -> w, String::length)); // ❌ duplicate key "hello"

// Fix: provide merge function
Map<String, Integer> safe = withDupes.stream()
    .collect(Collectors.toMap(
        w -> w,
        String::length,
        (existing, newVal) -> existing // keep first
    ));

// With LinkedHashMap to maintain order
Map<String, Integer> ordered = words.stream()
    .collect(Collectors.toMap(
        w -> w,
        String::length,
        (e, n) -> e,
        LinkedHashMap::new
    ));
```

---

## Q27. What is `mapToInt`, `mapToLong`, `mapToDouble` vs `map`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`mapToInt/Long/Double` converts a `Stream<T>` to a primitive stream to avoid boxing overhead.

```java
List<String> words = Arrays.asList("hello", "world", "java", "stream");

// map — keeps boxing (Stream<Integer>)
Stream<Integer> boxed = words.stream().map(String::length); // Integer objects

// mapToInt — no boxing (IntStream)
IntStream lengths = words.stream().mapToInt(String::length); // primitive int

// Enables sum, average, min, max without manual reduce
System.out.println(words.stream().mapToInt(String::length).sum());     // 19
System.out.println(words.stream().mapToInt(String::length).average()); // OptionalDouble[4.75]

// mapToObj — primitive stream back to Stream<T>
IntStream.rangeClosed(1, 5)
    .mapToObj(i -> "Item-" + i)
    .forEach(System.out::println); // Item-1, Item-2, ...
```

---

## Q28. What are `anyMatch`, `allMatch`, `noneMatch`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Short-circuit terminal operations that return `boolean`:

```java
List<Integer> nums = Arrays.asList(2, 4, 6, 7, 8, 10);

boolean anyOdd   = nums.stream().anyMatch(n -> n % 2 != 0);  // true (7)
boolean allEven  = nums.stream().allMatch(n -> n % 2 == 0);  // false (7 is odd)
boolean noneNeg  = nums.stream().noneMatch(n -> n < 0);       // true

// Short-circuit: stops as soon as result is determined
// anyMatch stops at first true
// allMatch stops at first false
// noneMatch stops at first true

// On empty stream:
boolean emptyAny  = Stream.empty().anyMatch(x -> true);  // false
boolean emptyAll  = Stream.empty().allMatch(x -> false); // true (vacuously true)
boolean emptyNone = Stream.empty().noneMatch(x -> true); // true
```

---

## Q29. What are `distinct()`, `sorted()`, `limit()`, `skip()`?

**Difficulty:** Basic | **Type:** Theory

**Answer:**

```java
List<Integer> nums = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);

// distinct — removes duplicates using equals()/hashCode()
nums.stream().distinct().forEach(System.out::print); // 3 1 4 5 9 2 6

// sorted — natural order
nums.stream().distinct().sorted().forEach(System.out::print); // 1 2 3 4 5 6 9

// sorted with Comparator
nums.stream().distinct().sorted(Comparator.reverseOrder()).forEach(System.out::print); // 9 6 5 4 3 2 1

// limit — take first N
nums.stream().limit(3).forEach(System.out::print); // 3 1 4

// skip — skip first N
nums.stream().skip(3).forEach(System.out::print); // 1 5 9 2 6 5 3 5

// Pagination pattern
int page = 2, pageSize = 3;
nums.stream()
    .skip((long)(page - 1) * pageSize)
    .limit(pageSize)
    .forEach(System.out::print); // 1 5 9 (elements 4-6)
```

---

## Q30. Scenario — How would you implement a custom Collector?

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
// Custom collector to join strings with prefix/suffix on each element
Collector<String, StringBuilder, String> bulletCollector = Collector.of(
    StringBuilder::new,                          // supplier — create accumulator
    (sb, s) -> sb.append("• ").append(s).append("\n"), // accumulator — fold element in
    (sb1, sb2) -> sb1.append(sb2),              // combiner — merge parallel results
    StringBuilder::toString                      // finisher — convert to result
);

List<String> tasks = List.of("Write tests", "Fix bugs", "Deploy", "Review PR");
String bulletList = tasks.stream().collect(bulletCollector);
System.out.println(bulletList);
// • Write tests
// • Fix bugs
// • Deploy
// • Review PR

// More practical: collect to ImmutableList
Collector<String, List<String>, List<String>> immutableListCollector = Collector.of(
    ArrayList::new,
    List::add,
    (l1, l2) -> { l1.addAll(l2); return l1; },
    Collections::unmodifiableList
);
```

---

## Q31. What is `Stream.of()` vs `Arrays.stream()` vs `Collection.stream()`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
// Stream.of — varargs, always Stream<T>
Stream<Integer> s1 = Stream.of(1, 2, 3);
Stream<int[]> s2  = Stream.of(new int[]{1, 2, 3}); // ⚠ wraps array as single element!

// Arrays.stream — handles primitive arrays correctly
IntStream s3 = Arrays.stream(new int[]{1, 2, 3}); // IntStream, not Stream<int[]>
Stream<Integer> s4 = Arrays.stream(new Integer[]{1, 2, 3}); // Stream<Integer>
Stream<String> s5 = Arrays.stream(new String[]{"a","b","c"});

// Partial array
IntStream partial = Arrays.stream(new int[]{1,2,3,4,5}, 1, 4); // elements [1..4) = 2,3,4

// Collection.stream()
List<String> list = List.of("a", "b", "c");
Stream<String> s6 = list.stream();
Stream<String> s7 = list.parallelStream(); // parallel
```

---

## Q32. What is `Collectors.teeing()`? (Java 12+)

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`Collectors.teeing()` applies two collectors to the same stream and merges their results with a merger function.

```java
// Calculate both average and max in one pass
record Stats(double avg, int max) {}

List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

Stats stats = nums.stream().collect(
    Collectors.teeing(
        Collectors.averagingInt(Integer::intValue), // collector 1
        Collectors.maxBy(Comparator.naturalOrder()), // collector 2
        (avg, max) -> new Stats(avg, max.orElse(0))  // merger
    )
);
System.out.println("Avg: " + stats.avg() + ", Max: " + stats.max()); // Avg: 5.5, Max: 10
```

---

## Summary — Key Takeaways for Interviews

| Topic | What interviewers test |
|-------|----------------------|
| Lambda + Functional IF | Can you identify valid lambda targets, effectively final variable rule |
| Stream pipeline | Lazy evaluation — what operations run and when |
| flatMap vs map | Classic confuser — know when each is appropriate |
| Collectors | groupingBy, partitioningBy, toMap duplicate key trap |
| Optional | Anti-patterns (don't use isPresent+get), chain map/flatMap/filter |
| parallel() | When it helps vs hurts — stateless, CPU-bound, large data |
| Stream reuse | IllegalStateException — stream is single-use |
| reduce vs collect | reduce for immutable folding, collect for mutable containers |
