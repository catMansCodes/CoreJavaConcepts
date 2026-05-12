# Java 8 Stream — Tricky Questions | Java Interview Guide (8–10 Years Experience)

## Quick Reference

```
Lazy eval:     Intermediate ops do nothing until terminal op fires
Short-circuit: findFirst, findAny, anyMatch, allMatch, noneMatch, limit
Stateful ops:  sorted, distinct, limit, skip — may need to see all elements
Stateless ops: filter, map, peek — process element by element
Parallel:      ForkJoinPool.commonPool() — avoid stateful shared ops
Reuse:         Streams are single-use — IllegalStateException if reused
```

---

## Q1. Tricky — What is the output? (Lazy evaluation)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5);

Stream<Integer> stream = nums.stream()
    .filter(n -> {
        System.out.println("filter: " + n);
        return n > 2;
    })
    .map(n -> {
        System.out.println("map: " + n);
        return n * 10;
    });

System.out.println("Stream created, nothing executed yet");
List<Integer> result = stream.collect(Collectors.toList());
System.out.println(result);
```

**Output:**
```
Stream created, nothing executed yet
filter: 1
filter: 2
filter: 3
map: 3
filter: 4
map: 4
filter: 5
map: 5
[30, 40, 50]
```

**Why?** Streams are **lazy**. No intermediate operation executes until `collect()` is called. Also notice: elements are processed **vertically** (one at a time through the whole pipeline), not **horizontally** (all filter, then all map).

---

## Q2. Tricky — What is the output? (Short-circuit with findFirst)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
Optional<Integer> result = Stream.of(1, 2, 3, 4, 5)
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

**Why?** `findFirst()` short-circuits — once the first match (2) passes filter and map, it returns immediately. Elements 3, 4, 5 are never processed.

---

## Q3. Tricky — Can a stream be reused? What is the output?

**Difficulty:** Tricky | **Type:** Output Prediction

```java
Stream<String> stream = Stream.of("a", "b", "c");
long count = stream.count();
System.out.println("Count: " + count);

// Attempt to reuse
stream.forEach(System.out::println); // ?
```

**Output:**
```
Count: 3
IllegalStateException: stream has already been operated upon or closed
```

**Fix — use Supplier:**
```java
Supplier<Stream<String>> streamSupplier = () -> Stream.of("a", "b", "c");
System.out.println(streamSupplier.get().count());
streamSupplier.get().forEach(System.out::println);
```

---

## Q4. Tricky — What is the output? (`peek` in different contexts)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
// Case 1: peek with terminal op
long count = Stream.of(1, 2, 3)
    .peek(n -> System.out.println("A: " + n))
    .filter(n -> n > 1)
    .peek(n -> System.out.println("B: " + n))
    .count();
System.out.println("Count: " + count);

// Case 2: peek without terminal op
Stream.of(1, 2, 3).peek(System.out::println); // prints anything?
```

**Output (Case 1):**
```
A: 1
A: 2
B: 2
A: 3
B: 3
Count: 2
```

**Output (Case 2):**
```
(nothing)
```

**Why Case 2?** Without a terminal operation, the stream is never evaluated. `peek` is intermediate (lazy) — without a terminal op, nothing runs.

---

## Q5. Tricky — What is the output? (`distinct` + `sorted` order)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
List<Integer> result = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5)
    .peek(n -> System.out.print("IN:" + n + " "))
    .distinct()
    .peek(n -> System.out.print("POST-DISTINCT:" + n + " "))
    .sorted()
    .collect(Collectors.toList());
System.out.println("\nResult: " + result);
```

**Output:**
```
IN:3 IN:1 IN:4 IN:1 IN:5 IN:9 IN:2 IN:6 IN:5 IN:3 IN:5
POST-DISTINCT:3 POST-DISTINCT:1 POST-DISTINCT:4 POST-DISTINCT:5 POST-DISTINCT:9 POST-DISTINCT:2 POST-DISTINCT:6
Result: [1, 2, 3, 4, 5, 6, 9]
```

**Why?** `distinct()` and `sorted()` are **stateful intermediate operations** — they must see all elements before producing output. So `sorted()` buffers all elements then sorts.

---

## Q6. Tricky — `map` vs `flatMap` output prediction.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
List<String> words = Arrays.asList("Hello World", "Java Stream");

// map — each element → one element
Stream<String[]> mapResult = words.stream().map(s -> s.split(" "));
System.out.println(mapResult.count()); // ?

// flatMap — each element → zero or more elements
long flatMapCount = words.stream().flatMap(s -> Arrays.stream(s.split(" "))).count();
System.out.println(flatMapCount); // ?

// What is the type without flatMap?
words.stream()
    .map(s -> s.split(" "))
    .collect(Collectors.toList())
    .forEach(arr -> System.out.println(Arrays.toString(arr)));
// ?

words.stream()
    .flatMap(s -> Arrays.stream(s.split(" ")))
    .collect(Collectors.toList())
    .forEach(System.out::println);
// ?
```

**Output:**
```
2          — 2 String[] elements (one per word-sentence)
4          — 4 individual words
[Hello, World]
[Java, Stream]
Hello
World
Java
Stream
```

---

## Q7. Tricky — What is the output? (`reduce` with and without identity)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
// reduce with identity
int sum1 = Stream.of(1, 2, 3, 4, 5).reduce(0, Integer::sum);
System.out.println(sum1); // ?

// reduce WITHOUT identity — returns Optional
Optional<Integer> sum2 = Stream.<Integer>empty().reduce(Integer::sum);
System.out.println(sum2); // ?

int sum3 = Stream.of(1, 2, 3).reduce(0, Integer::sum);
System.out.println(sum3); // ?

// reduce with identity on empty stream
int sum4 = Stream.<Integer>empty().reduce(0, Integer::sum);
System.out.println(sum4); // ?

// Subtraction — NOT commutative (order matters!)
int sub = Stream.of(10, 2, 3).reduce(0, (a, b) -> a - b);
System.out.println(sub); // ?
```

**Output:**
```
15              — 0+1+2+3+4+5
Optional.empty  — empty stream, no identity
6               — 0+1+2+3
0               — empty stream + identity = identity
-15             — ((0-10)-2)-3 = -15 (left fold)
```

---

## Q8. Tricky — `Collectors.toMap()` with duplicates.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
List<String> words = Arrays.asList("hello", "world", "hello", "java");

// Case 1: No merge function — duplicate key
Map<String, Integer> map1 = words.stream()
    .collect(Collectors.toMap(w -> w, String::length)); // ?

// Case 2: With merge function
Map<String, Integer> map2 = words.stream()
    .collect(Collectors.toMap(w -> w, String::length, (e, n) -> e)); // ?
```

**Output:**
```
Case 1: IllegalStateException: Duplicate key hello (attempted merging values 5 and 5)
Case 2: {hello=5, world=5, java=4} — keeps first value for "hello"
```

---

## Q9. Tricky — `groupingBy` with counting and ordering.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
List<String> words = Arrays.asList("apple", "ant", "banana", "bear", "cherry", "cat");

// Group by first letter
Map<Character, List<String>> grouped = words.stream()
    .collect(Collectors.groupingBy(w -> w.charAt(0)));

System.out.println(grouped.get('a')); // ?
System.out.println(grouped.get('b')); // ?

// Count per group
Map<Character, Long> counts = words.stream()
    .collect(Collectors.groupingBy(w -> w.charAt(0), Collectors.counting()));
System.out.println(counts); // ?

// Is the outer map sorted?
System.out.println(grouped instanceof TreeMap); // ?
```

**Output:**
```
[apple, ant]
[banana, bear]
{a=2, b=2, c=2}      — or any order (HashMap underlying)
false                 — groupingBy returns HashMap by default
```

**To get sorted:** `Collectors.groupingBy(key, TreeMap::new, downstream)`

---

## Q10. Tricky — What is the output? (parallel stream ordering)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5);

// Sequential forEach — preserves order
nums.stream().forEach(n -> System.out.print(n + " ")); // 1 2 3 4 5

// Parallel forEach — order NOT guaranteed
nums.parallelStream().forEach(n -> System.out.print(n + " ")); // any order!

// Parallel forEachOrdered — preserves order (but slower)
nums.parallelStream().forEachOrdered(n -> System.out.print(n + " ")); // 1 2 3 4 5

// Parallel collect — result is always correct
List<Integer> collected = nums.parallelStream()
    .filter(n -> n % 2 == 0)
    .collect(Collectors.toList());
System.out.println(collected); // [2, 4] — order preserved because List is ordered
```

---

## Q11. Tricky — What is the output? (`limit` and `skip` with sorted)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
Stream<Integer> stream = Stream.of(5, 3, 1, 4, 2);

List<Integer> r1 = stream.limit(3).collect(Collectors.toList());
System.out.println(r1); // ?

List<Integer> r2 = Stream.of(5,3,1,4,2)
    .sorted()
    .limit(3)
    .collect(Collectors.toList()); // ?

List<Integer> r3 = Stream.of(5,3,1,4,2)
    .skip(2)
    .limit(2)
    .collect(Collectors.toList()); // ?
```

**Output:**
```
[5, 3, 1]       — limit takes first 3 (no sort)
[1, 2, 3]       — sorted first, then first 3
[1, 4]          — skip 2 (removes 5,3), limit 2 (takes 1,4)
```

---

## Q12. Tricky — `Stream.of()` with an array.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
int[] primitiveArr = {1, 2, 3, 4, 5};
Integer[] boxedArr  = {1, 2, 3, 4, 5};

Stream<int[]> s1 = Stream.of(primitiveArr); // ?
System.out.println(s1.count()); // ?

Stream<Integer> s2 = Stream.of(boxedArr); // ?
System.out.println(s2.count()); // ?

IntStream s3 = Arrays.stream(primitiveArr);
System.out.println(s3.count()); // ?
```

**Output:**
```
Stream<int[]>  — wraps entire int[] as ONE element
1              — only 1 element (the whole array)
Stream<Integer>— correct
5
5
```

**Rule:** `Stream.of(int[])` wraps the array as a single element. Use `Arrays.stream(int[])` to get `IntStream` over elements.

---

## Q13. Tricky — `anyMatch` on empty stream.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
System.out.println(Stream.empty().anyMatch(x -> true));   // ?
System.out.println(Stream.empty().allMatch(x -> false));  // ?
System.out.println(Stream.empty().noneMatch(x -> true));  // ?
System.out.println(Stream.empty().count());               // ?
System.out.println(Stream.empty().findFirst());           // ?
System.out.println(Stream.empty().min(Comparator.naturalOrder())); // ?
```

**Output:**
```
false          — anyMatch on empty = false (no element satisfied)
true           — allMatch on empty = true (vacuous truth)
true           — noneMatch on empty = true (no element violated)
0              — count of empty = 0
Optional.empty — no first element
Optional.empty — no min element
```

---

## Q14. Tricky — `Optional.get()` on empty Optional.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
Optional<String> opt = Optional.empty();
String s1 = opt.orElse("default");     // ?
String s2 = opt.orElseGet(() -> "lazy"); // ?
String s3 = opt.get();                  // ?
String s4 = opt.orElseThrow(
    () -> new RuntimeException("not found")); // ?
```

**Output:**
```
"default"
"lazy"
NoSuchElementException — never call get() without isPresent() check (or use orElse)
RuntimeException: not found
```

---

## Q15. Tricky — `Collectors.partitioningBy` output.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6);
Map<Boolean, List<Integer>> partitioned = nums.stream()
    .collect(Collectors.partitioningBy(n -> n % 2 == 0));

System.out.println(partitioned.get(true));  // ?
System.out.println(partitioned.get(false)); // ?
System.out.println(partitioned.get(null));  // ?
System.out.println(partitioned.size());     // ?
```

**Output:**
```
[2, 4, 6]
[1, 3, 5]
null          — no null key (only true and false)
2             — always exactly 2 entries (true and false)
```

---

## Q16. Tricky — Stateful lambda in parallel stream.

**Difficulty:** Senior | **Type:** Tricky + Race Condition

```java
List<Integer> result = new ArrayList<>();

// BAD: stateful, non-thread-safe operation in parallel
Stream.of(1, 2, 3, 4, 5)
    .parallel()
    .filter(n -> n % 2 == 0)
    .forEach(result::add); // ArrayList is not thread-safe!

System.out.println(result.size()); // may print wrong size, or throw exception!

// GOOD: use collect
List<Integer> safe = Stream.of(1, 2, 3, 4, 5)
    .parallel()
    .filter(n -> n % 2 == 0)
    .collect(Collectors.toList()); // thread-safe

System.out.println(safe); // [2, 4] always correct
```

---

## Q17. Tricky — What is the output? (`map` with side effects)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));

// map() should be side-effect free — but what happens?
List<String> result = list.stream()
    .map(s -> {
        list.add("x"); // ConcurrentModificationException?
        return s.toUpperCase();
    })
    .collect(Collectors.toList());
```

**Output:**
```
ConcurrentModificationException — modifying the source while streaming is illegal for ArrayList
```

**Rule:** Never modify the source collection inside stream operations. Stream operations must be **non-interfering**.

---

## Q18. Tricky — `sorted()` stability.

**Difficulty:** Senior | **Type:** Theory

**Answer:**

Stream's `sorted()` uses **TimSort** (stable sort for Objects). Stable means elements that compare as equal maintain their relative original order.

```java
record Person(String name, int age) {}
List<Person> people = Arrays.asList(
    new Person("Alice", 30),
    new Person("Bob", 25),
    new Person("Charlie", 30),
    new Person("Dave", 25)
);

List<Person> sortedByAge = people.stream()
    .sorted(Comparator.comparingInt(Person::age))
    .collect(Collectors.toList());

sortedByAge.forEach(p -> System.out.println(p.name() + " " + p.age()));
// Bob 25
// Dave 25    ← Bob before Dave — stable (original relative order preserved)
// Alice 30
// Charlie 30 ← Alice before Charlie — stable
```

---

## Q19. Tricky — `IntStream.range()` vs `IntStream.rangeClosed()`.

**Difficulty:** Medium | **Type:** Output Prediction

```java
System.out.println(IntStream.range(1, 5).boxed().collect(Collectors.toList()));       // ?
System.out.println(IntStream.rangeClosed(1, 5).boxed().collect(Collectors.toList())); // ?
System.out.println(IntStream.range(5, 1).count()); // ?
System.out.println(IntStream.range(1, 1).count()); // ?
```

**Output:**
```
[1, 2, 3, 4]        — exclusive end
[1, 2, 3, 4, 5]     — inclusive end
0                   — empty range (start > end)
0                   — empty range (start == end)
```

---

## Q20. Tricky — `Stream.iterate()` with Java 9 predicate.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
// Java 8 — need limit to stop
Stream.iterate(1, n -> n * 2).limit(5).forEach(System.out::print); // ?

// Java 9 — predicate stops iteration
Stream.iterate(1, n -> n < 100, n -> n * 2).forEach(System.out::print); // ?

// What if seed already fails predicate?
Stream.iterate(200, n -> n < 100, n -> n * 2).forEach(System.out::print); // ?
```

**Output:**
```
1 2 4 8 16
1 2 4 8 16 32 64
(nothing) — seed 200 fails predicate n < 100 immediately
```

---

## Q21. Tricky — `Collectors.joining()` with empty stream.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
String r1 = Stream.empty().collect(Collectors.joining());                      // ?
String r2 = Stream.empty().collect(Collectors.joining(", "));                  // ?
String r3 = Stream.empty().collect(Collectors.joining(", ", "[", "]"));       // ?
String r4 = Stream.of("a").collect(Collectors.joining(", ", "[", "]"));      // ?
String r5 = Stream.of("a","b","c").collect(Collectors.joining(", ", "[", "]")); // ?
```

**Output:**
```
""
""
"[]"         — prefix + suffix even when empty
"[a]"
"[a, b, c]"
```

---

## Q22. Tricky — `Optional.map()` vs `Optional.flatMap()`.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
Optional<String> name = Optional.of("Alice");

// map — wraps result in Optional
Optional<Optional<String>> wrong = name.map(s -> Optional.of(s.toUpperCase())); // Optional[Optional[ALICE]]
Optional<String> correct = name.flatMap(s -> Optional.of(s.toUpperCase()));      // Optional[ALICE]

// When the mapping itself can return null
Optional<String> nullable = name.map(s -> null); // Optional.empty — null wraps to empty
System.out.println(nullable);  // Optional.empty

// Chaining
Optional<String> city = Optional.of("user")
    .map(u -> null)  // simulating getAddress() returning null
    .map(a -> "city"); // mapping Optional.empty — skipped
System.out.println(city); // Optional.empty — not NPE
```

---

## Q23. Tricky — What is the output? (`reduce` with parallel stream)

**Difficulty:** Senior | **Type:** Output Prediction

```java
// Associative operation — safe for parallel
int sum = Stream.of(1,2,3,4,5).parallel().reduce(0, Integer::sum); // ?

// Non-associative — dangerous with parallel
int result = Stream.of(1,2,3,4,5).parallel().reduce(0, (a, b) -> a - b); // ?

// Same without parallel
int seqResult = Stream.of(1,2,3,4,5).reduce(0, (a, b) -> a - b); // ?
```

**Output:**
```
15          — Integer::sum is associative, parallel result same as sequential
Unpredictable — subtraction is NOT associative: (a-b)-c ≠ a-(b-c). Parallel splits and combines differently
-15         — sequential: ((((0-1)-2)-3)-4)-5 = -15
```

---

## Q24. Tricky — `forEach` modifying collection.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
Map<String, Integer> map = new HashMap<>();
map.put("a", 1); map.put("b", 2); map.put("c", 3);

// Modifying map using forEach — what happens?
map.forEach((k, v) -> {
    if (v == 2) map.remove(k); // ?
});
```

**Output:**
```
ConcurrentModificationException — modifying map while iterating it
```

**Fix:**
```java
map.entrySet().removeIf(e -> e.getValue() == 2); // safe — uses Iterator.remove() internally
```

---

## Q25. Tricky — `Stream.generate()` with mutable state.

**Difficulty:** Senior | **Type:** Tricky

```java
// Counter using AtomicInteger with generate()
AtomicInteger counter = new AtomicInteger(0);
Stream.generate(counter::incrementAndGet)
    .limit(5)
    .forEach(System.out::println); // ?

// What if parallel?
AtomicInteger counter2 = new AtomicInteger(0);
List<Integer> result = Stream.generate(counter2::incrementAndGet)
    .parallel()
    .limit(5)
    .collect(Collectors.toList());
System.out.println(result); // ?
```

**Output:**
```
1 2 3 4 5   — sequential, predictable order

[1,2,3,4,5] but in any order — parallel, values are 1-5 but order unpredictable
```

---

## Q26. Tricky — `Collectors.toList()` vs `Collectors.toUnmodifiableList()` (Java 10+)

**Difficulty:** Medium | **Type:** Tricky

```java
List<Integer> list1 = Stream.of(1,2,3).collect(Collectors.toList());
list1.add(4); // ?

List<Integer> list2 = Stream.of(1,2,3).collect(Collectors.toUnmodifiableList());
list2.add(4); // ?

List<Integer> list3 = Stream.of(1,2,3).toList(); // Java 16+
list3.add(4); // ?
```

**Output:**
```
OK — toList() returns mutable ArrayList
UnsupportedOperationException
UnsupportedOperationException — Stream.toList() always unmodifiable
```

---

## Q27. Tricky — `mapToInt` vs `map` performance.

**Difficulty:** Senior | **Type:** Theory

**Answer:**

```java
List<Integer> nums = List.of(1, 2, 3, 4, 5);

// map — boxing/unboxing overhead
int sum1 = nums.stream()
    .map(n -> n * 2)          // Stream<Integer> — boxing
    .mapToInt(Integer::intValue) // unboxing
    .sum();

// mapToInt — avoids boxing entirely
int sum2 = nums.stream()
    .mapToInt(n -> n * 2)    // IntStream — primitive int throughout
    .sum();

// For large data sets, mapToInt is significantly faster
// 1M elements: map + reduce ~200ms, mapToInt ~50ms (approx)

// reduce with Integer — boxing
Integer product = nums.stream().reduce(1, (a, b) -> a * b); // boxing in lambda

// reduce with IntStream — no boxing
int product2 = nums.stream().mapToInt(Integer::intValue).reduce(1, (a, b) -> a * b);
```

---

## Q28. Tricky — `Collectors.groupingBy` with `TreeMap` to maintain sorted keys.

**Difficulty:** Senior | **Type:** Theory

**Answer:**

```java
List<String> words = Arrays.asList("cherry", "apple", "banana", "avocado", "blueberry", "apricot");

// Default groupingBy — HashMap, any order
Map<Character, List<String>> unordered = words.stream()
    .collect(Collectors.groupingBy(w -> w.charAt(0)));

// Sorted keys using TreeMap supplier
Map<Character, List<String>> sorted = words.stream()
    .collect(Collectors.groupingBy(
        w -> w.charAt(0),
        TreeMap::new,         // supplier for the outer map
        Collectors.toList()   // downstream collector
    ));

System.out.println(sorted);
// {a=[apple, avocado, apricot], b=[banana, blueberry], c=[cherry]}

// Sorted downstream too
Map<Character, List<String>> fullySorted = words.stream()
    .collect(Collectors.groupingBy(
        w -> w.charAt(0),
        TreeMap::new,
        Collectors.collectingAndThen(Collectors.toList(), l -> { Collections.sort(l); return l; })
    ));
```

---

## Q29. Tricky — What is the output? (map returns null)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
List<String> words = Arrays.asList("hello", null, "world");

// Case 1: filter then map
List<String> r1 = words.stream()
    .filter(Objects::nonNull)
    .map(String::toUpperCase)
    .collect(Collectors.toList()); // ?

// Case 2: map first — NullPointerException?
List<String> r2 = words.stream()
    .map(String::toUpperCase) // null.toUpperCase() ?
    .collect(Collectors.toList());
```

**Output:**
```
[HELLO, WORLD]         — null filtered before map
NullPointerException   — null.toUpperCase() throws NPE
```

---

## Q30. Scenario — Find top 3 most frequent words using Stream.

**Difficulty:** Senior | **Type:** Scenario

```java
String text = "the quick brown fox jumps over the lazy dog the fox";

List<String> top3 = Arrays.stream(text.split(" "))
    .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
    .entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .limit(3)
    .map(Map.Entry::getKey)
    .collect(Collectors.toList());

System.out.println(top3); // [the, fox, quick] (or similar based on order)

// With counts
Map<String, Long> top3WithCounts = Arrays.stream(text.split(" "))
    .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
    .entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .limit(3)
    .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue,
        (e, n) -> e,
        LinkedHashMap::new
    ));
System.out.println(top3WithCounts); // {the=3, fox=2, ...}
```

---

## Q31. Tricky — `Optional.or()` vs `Optional.orElse()` (Java 9+)

**Difficulty:** Senior | **Type:** Theory

```java
Optional<String> primary   = Optional.empty();
Optional<String> secondary = Optional.of("secondary");
Optional<String> fallback  = Optional.of("fallback");

// orElse — always evaluates the argument (eager)
String r1 = primary.orElse(computeDefault()); // computeDefault() ALWAYS called

// orElseGet — evaluates only if empty (lazy)
String r2 = primary.orElseGet(() -> computeDefault()); // only called if empty

// Optional.or() — returns other Optional if empty (Java 9+)
Optional<String> r3 = primary.or(() -> secondary); // Optional[secondary]
Optional<String> r4 = primary.or(() -> secondary).or(() -> fallback); // Optional[secondary] — stops at first present

String computeDefault() {
    System.out.println("Computing..."); // this always runs with orElse!
    return "default";
}

// Practical: try primary source, fallback to secondary
Optional<User> user = findInCache(id).or(() -> findInDatabase(id));
```

---

## Q32. Scenario — Process a large CSV file lazily using Streams.

**Difficulty:** Senior | **Type:** Scenario

```java
// Process millions of records without loading all into memory
// Files.lines() returns a lazy Stream<String> backed by BufferedReader

long highSalaryCount;
try (Stream<String> lines = Files.lines(Paths.get("employees.csv"))) {
    highSalaryCount = lines
        .skip(1)              // skip header
        .map(line -> line.split(","))
        .filter(cols -> cols.length >= 3)
        .mapToDouble(cols -> Double.parseDouble(cols[2].trim()))
        .filter(salary -> salary > 75000)
        .count();
} // stream (and underlying file) auto-closed by try-with-resources

System.out.println("High earners: " + highSalaryCount);

// Key: Files.lines() is LAZY — only reads lines as the stream demands them
// Never loads entire file into memory
// Must close the stream — use try-with-resources
```

---

## Summary — Key Takeaways for Interviews

| Topic | What interviewers test |
|-------|----------------------|
| Lazy evaluation | When do intermediate ops actually run? |
| Short-circuit | findFirst stops processing early |
| Reuse | IllegalStateException on reused stream |
| peek | Nothing printed without terminal op |
| parallel + shared state | Race condition — always use collect not forEach |
| flatMap vs map | Type: `Stream<Stream<T>>` vs `Stream<T>` |
| reduce identity | Empty stream + identity = identity |
| partitioningBy | Always 2 entries (true/false), never null key |
| toMap duplicate | IllegalStateException without merge function |
| anyMatch empty | false; allMatch empty = true (vacuous) |
| Stream.of(int[]) | Wraps as 1 element — use Arrays.stream() |
| Files.lines | Lazy, must close — use try-with-resources |
