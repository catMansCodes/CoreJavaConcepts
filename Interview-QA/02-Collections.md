# Collections Framework — Java Interview Guide (8–10 Years Experience)

## Quick Reference

```
Hierarchy:
  Iterable → Collection → List   → ArrayList, LinkedList, Vector, Stack
                        → Set    → HashSet, LinkedHashSet, TreeSet
                        → Queue  → PriorityQueue, ArrayDeque
  Map (separate)       → HashMap, LinkedHashMap, TreeMap, Hashtable,
                          ConcurrentHashMap, WeakHashMap, EnumMap
```

---

## Q1. Explain the Java Collections hierarchy.

**Difficulty:** Basic | **Type:** Theory

**Answer:**

```
java.lang.Iterable
  └── java.util.Collection
        ├── List (ordered, duplicates allowed)
        │     ├── ArrayList
        │     ├── LinkedList
        │     ├── Vector
        │     └── Stack
        ├── Set (no duplicates)
        │     ├── HashSet
        │     ├── LinkedHashSet
        │     └── TreeSet (SortedSet)
        └── Queue (FIFO/Priority)
              ├── PriorityQueue
              └── Deque
                    ├── ArrayDeque
                    └── LinkedList

java.util.Map (not a Collection)
  ├── HashMap
  ├── LinkedHashMap
  ├── TreeMap (SortedMap)
  ├── Hashtable
  ├── ConcurrentHashMap
  ├── WeakHashMap
  └── EnumMap
```

**Key interfaces:**
- `List` — ordered, index-based, allows duplicates
- `Set` — unordered (except LinkedHashSet/TreeSet), no duplicates
- `Queue` — FIFO access (`offer`, `poll`, `peek`)
- `Deque` — double-ended queue, can be used as Stack
- `Map` — key-value pairs, keys unique

---

## Q2. How does `HashMap` work internally?

**Difficulty:** Senior | **Type:** Theory (most asked)

**Answer:**

`HashMap` uses an **array of buckets** (Node[] table). Each entry is a `Node<K,V>` with `key`, `value`, `hash`, and `next` pointer.

**Steps when you call `put(key, value)`:**
1. Compute `hash = key.hashCode()` then spread bits: `h ^ (h >>> 16)`
2. Compute bucket index: `index = hash & (n - 1)` where n = table length (power of 2)
3. If bucket is empty → insert node
4. If bucket has a node → compare `hash` and `equals()`:
   - Same key → update value
   - Different key → add to chain (linked list or tree)
5. If size exceeds `capacity * loadFactor` (default 0.75) → **resize** (double capacity, rehash all)
6. If a single bucket's chain length exceeds **8** and table size ≥ 64 → convert chain to **Red-Black Tree** (Java 8+) for O(log n) lookup

```java
// Simplified internal structure
static class Node<K,V> {
    final int hash;
    final K key;
    V value;
    Node<K,V> next; // linked list for collisions
}

// put logic simplified
int hash = hash(key);
int index = hash & (table.length - 1);
// check existing nodes at table[index], update or append
```

**Complexity:**
| Operation | Average | Worst (all in one bucket, no tree) |
|-----------|---------|--------------------------------------|
| put/get | O(1) | O(n) → O(log n) with tree |
| containsKey | O(1) | O(log n) |

**Follow-up:** What if `hashCode()` always returns the same value?
> All entries go into one bucket → linked list → O(n) lookup. Java 8 mitigates this with treeification.

---

## Q3. What is the difference between `HashMap` and `ConcurrentHashMap`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

| Feature | HashMap | ConcurrentHashMap |
|---------|---------|-------------------|
| Thread-safe | No | Yes |
| Null keys | 1 allowed | Not allowed |
| Null values | Allowed | Not allowed |
| Locking (Java 8+) | None | Bucket-level CAS + synchronized |
| Performance | Fastest single-thread | Better than Hashtable (no full lock) |
| Iteration | Fail-fast | Weakly consistent (no ConcurrentModificationException) |

**Java 8 ConcurrentHashMap internals:**
- Reads are lock-free (volatile reads)
- Writes use CAS (Compare-And-Swap) for empty buckets
- `synchronized` only on the **first node** of each bucket (not full map)
- No more segment-level locking (Java 7 had 16 segments)

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// Thread-safe atomic operations
map.put("a", 1);
map.putIfAbsent("b", 2);          // atomic
map.computeIfAbsent("c", k -> 3); // atomic
map.merge("a", 10, Integer::sum); // atomic increment

// Safe iteration (weakly consistent — no CME)
map.forEach((k, v) -> System.out.println(k + "=" + v));
```

---

## Q4. What is the difference between `fail-fast` and `fail-safe` iterators?

**Difficulty:** Senior | **Type:** Theory + Tricky

**Answer:**

| | Fail-fast | Fail-safe |
|-|-----------|-----------|
| Behavior | Throws `ConcurrentModificationException` if collection modified during iteration | Works on a copy, no exception |
| How detected | `modCount` field checked on each `next()` call | Iterates over a snapshot |
| Collections | `ArrayList`, `HashMap`, `HashSet`, `LinkedList` | `ConcurrentHashMap`, `CopyOnWriteArrayList` |
| Memory | No copy | Copy of underlying structure |

```java
// Fail-fast — ConcurrentModificationException
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
for (String s : list) {
    if (s.equals("b")) list.remove(s); // ❌ ConcurrentModificationException
}

// Safe removal during iteration — use Iterator.remove()
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("b")) it.remove(); // ✅
}

// Or use removeIf (Java 8)
list.removeIf(s -> s.equals("b")); // ✅

// Fail-safe — CopyOnWriteArrayList
List<String> cowList = new CopyOnWriteArrayList<>(Arrays.asList("a", "b", "c"));
for (String s : cowList) {
    if (s.equals("b")) cowList.remove(s); // ✅ no exception, works on snapshot
}
```

---

## Q5. What is the difference between `ArrayList` and `LinkedList`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| Operation | ArrayList | LinkedList |
|-----------|-----------|-----------|
| Internal structure | Dynamic array | Doubly linked list |
| `get(index)` | O(1) random access | O(n) traverse |
| `add(end)` | O(1) amortized | O(1) |
| `add(middle)` | O(n) shift | O(1) (once at node) |
| `remove(middle)` | O(n) shift | O(1) (once at node) |
| Memory | Less (no node overhead) | More (node + prev + next pointers) |
| Cache | Cache-friendly (contiguous) | Poor (scattered nodes) |
| Use as Queue/Deque | No | Yes (`implements Deque`) |

```java
// ArrayList — best for random access
ArrayList<Integer> al = new ArrayList<>();
al.add(10); al.add(20); al.add(30);
System.out.println(al.get(2)); // O(1) — 30

// LinkedList — best for frequent insertions/deletions at ends
LinkedList<Integer> ll = new LinkedList<>();
ll.addFirst(10); // O(1)
ll.addLast(20);  // O(1)
ll.removeFirst(); // O(1)

// LinkedList as Queue
Queue<String> queue = new LinkedList<>();
queue.offer("first");
queue.offer("second");
System.out.println(queue.poll()); // first
```

**Scenario:** For a task queue where items are added at the back and removed from front → use `LinkedList` or `ArrayDeque`. For a list that is mostly read → use `ArrayList`.

---

## Q6. How does `HashSet` work internally?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`HashSet` is backed by a `HashMap`. It stores elements as **keys** in the map, with a constant dummy `PRESENT` object as the value.

```java
// Simplified HashSet internals
class HashSet<E> {
    private HashMap<E, Object> map = new HashMap<>();
    private static final Object PRESENT = new Object();

    boolean add(E e) {
        return map.put(e, PRESENT) == null; // uses hashCode + equals
    }

    boolean contains(Object o) {
        return map.containsKey(o);
    }
}
```

**Implication:** For custom objects in a HashSet, you **must** override both `hashCode()` and `equals()` correctly.

```java
class Point {
    int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Point p)) return false;
        return x == p.x && y == p.y;
    }

    @Override public int hashCode() {
        return Objects.hash(x, y);
    }
}

Set<Point> points = new HashSet<>();
points.add(new Point(1, 2));
points.add(new Point(1, 2)); // duplicate — won't be added
System.out.println(points.size()); // 1
```

---

## Q7. What is the difference between `HashSet`, `LinkedHashSet`, and `TreeSet`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| Feature | HashSet | LinkedHashSet | TreeSet |
|---------|---------|---------------|---------|
| Order | No guarantee | Insertion order | Sorted (natural or Comparator) |
| Null element | 1 allowed | 1 allowed | Not allowed (comparison fails) |
| Performance | O(1) add/contains | O(1) with overhead | O(log n) |
| Backed by | HashMap | LinkedHashMap | Red-Black Tree |
| Implements | Set | Set | SortedSet, NavigableSet |

```java
Set<String> hashSet   = new HashSet<>(Arrays.asList("c", "a", "b"));
Set<String> linkedSet = new LinkedHashSet<>(Arrays.asList("c", "a", "b"));
Set<String> treeSet   = new TreeSet<>(Arrays.asList("c", "a", "b"));

System.out.println(hashSet);   // [a, b, c] or any order
System.out.println(linkedSet); // [c, a, b] insertion order
System.out.println(treeSet);   // [a, b, c] sorted

// TreeSet navigable operations
TreeSet<Integer> ts = new TreeSet<>(Arrays.asList(1,3,5,7,9));
System.out.println(ts.floor(6));   // 5 (greatest ≤ 6)
System.out.println(ts.ceiling(6)); // 7 (smallest ≥ 6)
System.out.println(ts.headSet(5)); // [1, 3] (strictly less than 5)
System.out.println(ts.tailSet(5)); // [5, 7, 9] (≥ 5)
```

---

## Q8. How does `TreeMap` work? When to use it over `HashMap`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`TreeMap` stores entries in a **Red-Black Tree** (self-balancing BST), always sorted by key.

```java
TreeMap<String, Integer> salaries = new TreeMap<>();
salaries.put("Zara", 90000);
salaries.put("Alice", 70000);
salaries.put("Bob", 80000);

System.out.println(salaries);            // {Alice=70000, Bob=80000, Zara=90000} sorted
System.out.println(salaries.firstKey()); // Alice
System.out.println(salaries.lastKey());  // Zara
System.out.println(salaries.floorKey("C")); // Bob
System.out.println(salaries.subMap("Alice", "Zara")); // {Alice=70000, Bob=80000}

// Custom Comparator — reverse order
TreeMap<String, Integer> reversed = new TreeMap<>(Comparator.reverseOrder());
reversed.put("a", 1); reversed.put("b", 2); reversed.put("c", 3);
System.out.println(reversed); // {c=3, b=2, a=1}
```

**Use TreeMap when:** you need sorted keys, range queries, or floor/ceiling/first/last key operations.

---

## Q9. What happens when two keys have the same `hashCode()` in a `HashMap`?

**Difficulty:** Senior | **Type:** Tricky

**Answer:**

This is a **hash collision**. The keys land in the same bucket. HashMap then uses `equals()` to distinguish them:
- If `equals()` returns true → same key, value updated
- If `equals()` returns false → different keys, added to the **linked list** (or tree) at that bucket

```java
// Demonstrating collision
class BadKey {
    String val;
    BadKey(String val) { this.val = val; }

    @Override public int hashCode() { return 42; } // always same bucket!
    @Override public boolean equals(Object o) {
        if (!(o instanceof BadKey)) return false;
        return val.equals(((BadKey) o).val);
    }
}

HashMap<BadKey, String> map = new HashMap<>();
map.put(new BadKey("a"), "Apple");
map.put(new BadKey("b"), "Banana"); // same bucket, different equals → chained
map.put(new BadKey("a"), "Avocado"); // same bucket, same equals → updated

System.out.println(map.size()); // 2
System.out.println(map.get(new BadKey("a"))); // Avocado
```

**Performance impact:** With a bad `hashCode()` all entries pile into one bucket → O(n) gets. Java 8 converts to Red-Black Tree after 8 entries in a bucket (table size ≥ 64) → O(log n).

---

## Q10. What is the default initial capacity and load factor of `HashMap`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

- **Initial capacity:** 16 (must be power of 2)
- **Load factor:** 0.75
- **Resize threshold:** capacity × loadFactor = 16 × 0.75 = **12** entries triggers resize (doubles to 32)

```java
// Custom capacity and load factor
HashMap<String, Integer> map = new HashMap<>(32, 0.5f);
// Resize at 32 * 0.5 = 16 entries

// Why power of 2? Bucket index = hash & (n-1)
// With n=16: hash & 15 (binary: 1111) — fast bitwise operation
// With non-power-of-2: must use modulo (%) — slower

// Pre-size when count is known (avoid rehashing)
int expectedEntries = 1000;
int capacity = (int)(expectedEntries / 0.75) + 1;
HashMap<String, Integer> optimized = new HashMap<>(capacity);
```

---

## Q11. What is the difference between `Iterator` and `ListIterator`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| Feature | Iterator | ListIterator |
|---------|---------|-------------|
| Direction | Forward only | Forward + Backward |
| Available for | All Collections | List only |
| Methods | `hasNext()`, `next()`, `remove()` | All of Iterator + `hasPrevious()`, `previous()`, `add()`, `set()`, `nextIndex()`, `previousIndex()` |

```java
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));

// ListIterator — traverse and modify
ListIterator<String> it = list.listIterator(list.size()); // start from end
while (it.hasPrevious()) {
    System.out.print(it.previous() + " "); // d c b a
}

// Add during iteration
ListIterator<String> it2 = list.listIterator();
while (it2.hasNext()) {
    String s = it2.next();
    if (s.equals("b")) it2.add("B+"); // safe add
}
System.out.println(list); // [a, b, B+, c, d]
```

---

## Q12. What is `CopyOnWriteArrayList`? When to use it?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`CopyOnWriteArrayList` creates a **fresh copy** of the underlying array on every write (add/remove/set). Reads use the original array without locking.

**Pros:** Thread-safe without locking for reads; no `ConcurrentModificationException`
**Cons:** Expensive writes (full array copy); stale reads during iteration

```java
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(Arrays.asList("a","b","c"));

// Safe to modify during iteration
for (String s : list) {
    System.out.println(s);
    list.add("x"); // does NOT throw CME — iterating over snapshot
}
System.out.println(list.size()); // 6 (3 original + 3 added)
```

**Use when:** Reads >> Writes (e.g., event listener lists, configuration lists read by multiple threads).

---

## Q13. What is `PriorityQueue`? How does it work?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`PriorityQueue` is a min-heap by default. The head is always the **smallest** element (natural ordering or custom Comparator).

- **Not sorted** — only the head (minimum) is guaranteed at the front
- Not thread-safe (use `PriorityBlockingQueue` for concurrency)
- Does not allow null

```java
// Min-heap (default)
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
minHeap.offer(5); minHeap.offer(1); minHeap.offer(3);
System.out.println(minHeap.poll()); // 1 (minimum)
System.out.println(minHeap.poll()); // 3

// Max-heap
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
maxHeap.offer(5); maxHeap.offer(1); maxHeap.offer(3);
System.out.println(maxHeap.poll()); // 5 (maximum)

// Custom comparator — priority by task priority field
record Task(String name, int priority) {}
PriorityQueue<Task> taskQueue = new PriorityQueue<>(
    Comparator.comparingInt(Task::priority)
);
taskQueue.offer(new Task("Low", 3));
taskQueue.offer(new Task("High", 1));
taskQueue.offer(new Task("Med", 2));
while (!taskQueue.isEmpty()) {
    System.out.println(taskQueue.poll().name()); // High, Med, Low
}
```

---

## Q14. What is `ArrayDeque`? How is it different from `LinkedList`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`ArrayDeque` is a resizable-array double-ended queue. Preferred over `LinkedList` as a Stack or Queue.

| | ArrayDeque | LinkedList |
|-|------------|-----------|
| Internal | Circular array | Doubly linked list |
| Memory | Less (no node overhead) | More |
| Performance | Faster (cache-friendly) | Slower |
| Null | Not allowed | Allowed |
| Implements | Deque | Deque, List, Queue |

```java
Deque<String> deque = new ArrayDeque<>();
deque.offerFirst("b");
deque.offerFirst("a"); // head → [a, b]
deque.offerLast("c");  // head → [a, b, c]

System.out.println(deque.pollFirst()); // a
System.out.println(deque.pollLast());  // c

// Used as Stack (LIFO)
Deque<Integer> stack = new ArrayDeque<>();
stack.push(1); stack.push(2); stack.push(3);
System.out.println(stack.pop()); // 3 (LIFO)
```

**Prefer `ArrayDeque` over `Stack` class** — `Stack` extends `Vector` (synchronized overhead).

---

## Q15. What is `WeakHashMap`? When to use it?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`WeakHashMap` holds **weak references** to keys. If no other strong reference to a key exists, the GC can collect the key → the entry is automatically removed from the map.

```java
WeakHashMap<Object, String> cache = new WeakHashMap<>();

Object key1 = new Object();
Object key2 = new Object();
cache.put(key1, "value1");
cache.put(key2, "value2");

System.out.println(cache.size()); // 2

key1 = null; // remove strong reference to key1
System.gc();
Thread.sleep(100);

System.out.println(cache.size()); // 1 — key1's entry GC'd
```

**Use case:** Memory-sensitive caches where entries should expire when the key is no longer used elsewhere (e.g., class metadata caches, per-object configuration stores).

---

## Q16. Tricky — What is the output? (HashMap key mutation)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
HashMap<List<Integer>, String> map = new HashMap<>();

List<Integer> key = new ArrayList<>(Arrays.asList(1, 2, 3));
map.put(key, "original");

System.out.println(map.get(key)); // ?

key.add(4); // mutate the key!

System.out.println(map.get(key)); // ?
System.out.println(map.size());   // ?
```

**Output:**
```
original
null
1
```

**Why?**
1. Before mutation: `hashCode([1,2,3])` maps to bucket X → found
2. After mutation: `hashCode([1,2,3,4])` maps to bucket Y (different) → not found
3. The entry still exists but is now **orphaned** — unreachable because its hash changed

**Rule:** Never use mutable objects as HashMap keys. Use `String`, `Integer`, or other immutable types.

---

## Q17. What is the difference between `remove()` methods in `HashMap`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
HashMap<String, Integer> map = new HashMap<>();
map.put("a", 1); map.put("b", 2); map.put("c", 3);

// remove(key) — removes if key exists, returns old value
Integer v = map.remove("a"); // returns 1

// remove(key, value) — removes only if key maps to the specified value (atomic)
boolean removed = map.remove("b", 999); // false — "b" maps to 2, not 999
System.out.println(map.containsKey("b")); // true — not removed

removed = map.remove("b", 2); // true — matches
System.out.println(map.containsKey("b")); // false
```

The `remove(key, value)` overload is important in `ConcurrentHashMap` for conditional atomic removal without explicit locking.

---

## Q18. What are `computeIfAbsent`, `computeIfPresent`, `merge` in `Map`?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

```java
Map<String, List<String>> groupedEmails = new HashMap<>();

// computeIfAbsent — create value only if key absent (useful for grouping)
groupedEmails.computeIfAbsent("admin", k -> new ArrayList<>()).add("alert@example.com");
groupedEmails.computeIfAbsent("admin", k -> new ArrayList<>()).add("warn@example.com");
// admin → [alert@example.com, warn@example.com]

// computeIfPresent — update only if key present
Map<String, Integer> wordCount = new HashMap<>();
wordCount.put("hello", 5);
wordCount.computeIfPresent("hello", (k, v) -> v + 1); // hello → 6
wordCount.computeIfPresent("world", (k, v) -> v + 1); // no-op

// merge — combine new value with existing using a function (word counting pattern)
String[] words = {"apple", "banana", "apple", "cherry", "banana", "apple"};
Map<String, Integer> freq = new HashMap<>();
for (String w : words) {
    freq.merge(w, 1, Integer::sum); // if absent: put 1, if present: add 1
}
System.out.println(freq); // {apple=3, banana=2, cherry=1}
```

---

## Q19. What is `EnumMap`? Why is it faster than `HashMap` with enum keys?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`EnumMap` is a specialized Map where keys must be from a single Enum. It uses an **array indexed by enum ordinal** internally — no hashing at all.

- Extremely fast O(1) lookup and insertion
- Memory efficient
- Maintains **natural enum declaration order**
- Not thread-safe

```java
enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

EnumMap<Day, String> schedule = new EnumMap<>(Day.class);
schedule.put(Day.MON, "Standup at 9am");
schedule.put(Day.WED, "Sprint review");
schedule.put(Day.FRI, "Retrospective");

System.out.println(schedule); // {MON=Standup at 9am, WED=Sprint review, FRI=Retrospective}
System.out.println(schedule.get(Day.WED)); // Sprint review
```

---

## Q20. What is `Collections` utility class? Name important methods.

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
List<Integer> list = new ArrayList<>(Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6));

Collections.sort(list);                    // [1, 1, 2, 3, 4, 5, 6, 9]
Collections.sort(list, Comparator.reverseOrder()); // [9, 6, 5, 4, 3, 2, 1, 1]
Collections.reverse(list);                 // reverses in place
Collections.shuffle(list);                 // random shuffle
Collections.min(list);                     // minimum element
Collections.max(list);                     // maximum element
Collections.frequency(list, 1);            // count of element
Collections.binarySearch(list, 5);         // requires sorted list

// Unmodifiable wrappers
List<Integer> immutable = Collections.unmodifiableList(list);
// immutable.add(1); // UnsupportedOperationException

// Synchronized wrappers
List<Integer> syncList = Collections.synchronizedList(list);

// Singleton/Empty collections
List<String> single  = Collections.singletonList("only");
List<String> empty   = Collections.emptyList(); // immutable, shareable
Map<String, Integer> emptyMap = Collections.emptyMap();

// Fill and copy
Collections.fill(list, 0); // fill all with 0
List<Integer> dest = new ArrayList<>(Collections.nCopies(5, 0)); // [0,0,0,0,0]
Collections.copy(dest, list); // copy src into dest (dest must be at least as large)
```

---

## Q21. What is `List.of()` vs `Arrays.asList()` vs `new ArrayList<>()`?

**Difficulty:** Medium | **Type:** Tricky

**Answer:**

| | `List.of()` (Java 9+) | `Arrays.asList()` | `new ArrayList<>()` |
|-|----------------------|-------------------|---------------------|
| Mutable size | No | No | Yes |
| Mutable values | No | Yes (set works) | Yes |
| Null elements | Not allowed | Allowed | Allowed |
| Backed by array | No | Yes | No |
| Serializable | Yes | Yes | Yes |

```java
List<String> listOf    = List.of("a", "b", "c");    // fully immutable
List<String> asList    = Arrays.asList("a", "b", "c"); // fixed size, mutable values
List<String> arrayList = new ArrayList<>(Arrays.asList("a", "b", "c")); // fully mutable

// listOf.add("d");    // UnsupportedOperationException
// listOf.set(0, "x"); // UnsupportedOperationException

// asList.add("d");    // UnsupportedOperationException (fixed size)
asList.set(0, "x");    // ✅ OK

arrayList.add("d");    // ✅ OK
arrayList.set(0, "x"); // ✅ OK

// Tricky: asList is backed by original array
String[] arr = {"a", "b", "c"};
List<String> view = Arrays.asList(arr);
arr[0] = "X";
System.out.println(view.get(0)); // X — changes reflected!
```

---

## Q22. How does `LinkedHashMap` maintain insertion order?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`LinkedHashMap` extends `HashMap` and adds a **doubly linked list** through all entries in insertion (or access) order.

```java
// Insertion-order LinkedHashMap
LinkedHashMap<String, Integer> lhm = new LinkedHashMap<>();
lhm.put("banana", 2); lhm.put("apple", 1); lhm.put("cherry", 3);
System.out.println(lhm); // {banana=2, apple=1, cherry=3} — insertion order

// Access-order LinkedHashMap (LRU cache base)
LinkedHashMap<String, Integer> lru = new LinkedHashMap<>(16, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
        return size() > 3; // keep only 3 most recently accessed
    }
};
lru.put("a", 1); lru.put("b", 2); lru.put("c", 3);
lru.get("a"); // access "a" — moves it to end
lru.put("d", 4); // triggers removeEldestEntry → "b" removed (least recently used)
System.out.println(lru); // {c=3, a=1, d=4}
```

**Scenario:** `LinkedHashMap` with access-order is the foundation of a simple LRU cache — used heavily in interview questions.

---

## Q23. Scenario — Implement a simple LRU Cache using Java Collections.

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    LRUCache(int capacity) {
        super(capacity, 0.75f, true); // true = access order
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

    public V get(K key) {
        return super.getOrDefault(key, null);
    }

    public void put(K key, V value) {
        super.put(key, value);
    }
}

LRUCache<Integer, String> cache = new LRUCache<>(3);
cache.put(1, "one");
cache.put(2, "two");
cache.put(3, "three");
cache.get(1);          // access 1 → moves to end
cache.put(4, "four");  // evicts 2 (LRU)
System.out.println(cache.containsKey(2)); // false — evicted
System.out.println(cache);               // {3=three, 1=one, 4=four}
```

---

## Q24. What is the difference between `Comparable` and `Comparator`? (Collections context)

**Difficulty:** Medium | **Type:** Theory + Scenario

**Answer:**

```java
class Student implements Comparable<Student> {
    String name;
    int grade;
    Student(String name, int grade) { this.name = name; this.grade = grade; }

    @Override
    public int compareTo(Student other) {
        return Integer.compare(this.grade, other.grade); // natural = by grade
    }

    @Override public String toString() { return name + "(" + grade + ")"; }
}

List<Student> students = Arrays.asList(
    new Student("Zara", 85),
    new Student("Alice", 92),
    new Student("Bob", 78)
);

Collections.sort(students); // uses Comparable
System.out.println(students); // [Bob(78), Zara(85), Alice(92)]

// Multiple sort strategies with Comparator
students.sort(Comparator.comparing(s -> s.name));
System.out.println(students); // [Alice(92), Bob(78), Zara(85)]

students.sort(Comparator.comparingInt((Student s) -> s.grade).reversed());
System.out.println(students); // [Alice(92), Zara(85), Bob(78)]
```

---

## Q25. What happens when you put a `null` key in `HashMap`, `TreeMap`, and `Hashtable`?

**Difficulty:** Tricky | **Type:** Tricky

**Answer:**

```java
// HashMap — null key allowed, stored in bucket 0
HashMap<String, Integer> hm = new HashMap<>();
hm.put(null, 100);
hm.put(null, 200); // replaces previous null key
System.out.println(hm.get(null)); // 200

// TreeMap — null key NOT allowed (needs comparison)
TreeMap<String, Integer> tm = new TreeMap<>();
// tm.put(null, 100); // NullPointerException — compareTo(null) fails

// Hashtable — null key NOT allowed
Hashtable<String, Integer> ht = new Hashtable<>();
// ht.put(null, 100); // NullPointerException

// ConcurrentHashMap — null key NOT allowed
ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<>();
// chm.put(null, 100); // NullPointerException
```

---

## Q26. What is `BlockingQueue`? Name implementations.

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`BlockingQueue` is a thread-safe queue where:
- `put()` blocks if queue is full
- `take()` blocks if queue is empty

Used in **Producer-Consumer** patterns.

| Implementation | Bounded? | Notes |
|---------------|----------|-------|
| `ArrayBlockingQueue` | Yes | Fixed capacity, array-backed |
| `LinkedBlockingQueue` | Optional | Default unbounded (Integer.MAX_VALUE) |
| `PriorityBlockingQueue` | No | Priority ordering, unbounded |
| `SynchronousQueue` | 0 capacity | Handoff queue — put blocks until take, vice versa |
| `DelayQueue` | No | Elements available only after delay |

```java
BlockingQueue<String> queue = new ArrayBlockingQueue<>(3);

// Producer thread
Thread producer = new Thread(() -> {
    try {
        queue.put("Task1");
        queue.put("Task2");
        queue.put("Task3");
        queue.put("Task4"); // blocks here if queue full
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});

// Consumer thread
Thread consumer = new Thread(() -> {
    try {
        for (int i = 0; i < 4; i++) {
            System.out.println("Processing: " + queue.take()); // blocks if empty
        }
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});

producer.start(); consumer.start();
```

---

## Q27. What is the difference between `poll()` and `take()` in a Queue?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| Method | Returns | Behavior when empty |
|--------|---------|---------------------|
| `poll()` | Head element or null | Returns null immediately |
| `poll(timeout, unit)` | Head element or null | Waits up to timeout |
| `take()` | Head element | Blocks indefinitely |
| `peek()` | Head element or null | Does not remove, returns null if empty |
| `remove()` | Head element | Throws `NoSuchElementException` if empty |

```java
BlockingQueue<String> q = new LinkedBlockingQueue<>();
q.offer("item");

System.out.println(q.poll());          // item
System.out.println(q.poll());          // null (non-blocking)
System.out.println(q.poll(100, TimeUnit.MILLISECONDS)); // waits 100ms then null
// q.take() // would block indefinitely on empty queue
```

---

## Q28. Tricky — What is the output? (TreeSet with custom objects)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
TreeSet<String> set = new TreeSet<>();
set.add("banana");
set.add("apple");
set.add("cherry");
set.add("Apple"); // capital A
System.out.println(set);
System.out.println(set.size());
```

**Output:**
```
[Apple, apple, banana, cherry]
4
```

**Why?** `TreeSet<String>` uses `String.compareTo()` which is case-sensitive. 'A' (65) < 'a' (97) in Unicode, so "Apple" ≠ "apple" and both are inserted.

**Follow-up:** How to make it case-insensitive?
```java
TreeSet<String> caseInsensitive = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
caseInsensitive.add("apple"); caseInsensitive.add("Apple");
System.out.println(caseInsensitive.size()); // 1 — treated as duplicate
```

---

## Q29. Scenario — Which collection to use for each use case?

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

| Use Case | Best Choice | Reason |
|----------|-------------|--------|
| Fast random access | `ArrayList` | O(1) get by index |
| Frequent inserts/deletes at ends | `ArrayDeque` or `LinkedList` | O(1) addFirst/addLast |
| No duplicates, fast lookup | `HashSet` | O(1) contains |
| No duplicates, sorted | `TreeSet` | O(log n), sorted |
| No duplicates, insertion order | `LinkedHashSet` | Insertion order + O(1) |
| Key-value, fast lookup | `HashMap` | O(1) average |
| Key-value, sorted keys | `TreeMap` | O(log n), sorted |
| Key-value, insertion order | `LinkedHashMap` | Insertion order |
| Thread-safe key-value | `ConcurrentHashMap` | Segment locking |
| Thread-safe list, many reads | `CopyOnWriteArrayList` | Lock-free reads |
| Producer-Consumer queue | `ArrayBlockingQueue` | Blocking, bounded |
| Priority processing | `PriorityQueue` | Min-heap |
| Enum keys | `EnumMap` | Array-backed, fastest |
| Memory-sensitive cache | `WeakHashMap` | GC-eligible entries |
| LRU cache | `LinkedHashMap` (access-order) | Built-in eldest removal |

---

## Q30. What is `Map.Entry` and how to iterate a Map?

**Difficulty:** Basic | **Type:** Theory

**Answer:**

```java
Map<String, Integer> scores = new HashMap<>();
scores.put("Alice", 95); scores.put("Bob", 87); scores.put("Charlie", 92);

// 1. keySet() — iterate keys (then get value)
for (String key : scores.keySet()) {
    System.out.println(key + " = " + scores.get(key)); // extra lookup
}

// 2. entrySet() — best for both key and value
for (Map.Entry<String, Integer> entry : scores.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}

// 3. values() — values only
for (int val : scores.values()) {
    System.out.println(val);
}

// 4. forEach (Java 8+) — cleanest
scores.forEach((k, v) -> System.out.println(k + " = " + v));

// 5. Stream
scores.entrySet().stream()
    .filter(e -> e.getValue() > 90)
    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
    .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));
```

**Performance tip:** Always prefer `entrySet()` over `keySet()` when you need both key and value — avoids the second `get()` lookup.

---

## Q31. Scenario — Count word frequency from a large file using Collections.

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
import java.util.*;
import java.util.stream.*;

public class WordFrequency {

    static Map<String, Long> countWords(List<String> lines) {
        return lines.stream()
            .flatMap(line -> Arrays.stream(line.toLowerCase().split("\\W+")))
            .filter(w -> !w.isEmpty())
            .collect(Collectors.groupingBy(
                w -> w,
                LinkedHashMap::new,   // preserve insertion order
                Collectors.counting()
            ));
    }

    static List<Map.Entry<String, Long>> topN(Map<String, Long> freq, int n) {
        return freq.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(n)
            .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<String> lines = List.of(
            "the quick brown fox",
            "the fox jumped over the lazy dog",
            "the quick brown dog"
        );

        Map<String, Long> freq = countWords(lines);
        System.out.println("Top 3: " + topN(freq, 3));
        // Top 3: [the=4, fox=2, quick=2]
    }
}
```

---

## Q32. What is `IdentityHashMap`? When is it used?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`IdentityHashMap` uses **reference equality** (`==`) instead of `equals()` and `System.identityHashCode()` instead of `hashCode()` for key comparison.

```java
IdentityHashMap<String, Integer> map = new IdentityHashMap<>();

String a = new String("key");
String b = new String("key");

map.put(a, 1);
map.put(b, 2); // different reference — treated as different key!

System.out.println(map.size()); // 2 (not 1!)
System.out.println(map.get(a)); // 1
System.out.println(map.get(b)); // 2

// Regular HashMap: a.equals(b) → true → size = 1, second put updates
```

**Use case:** Object graph traversal, serialization frameworks, tracking objects by identity (not value), avoiding infinite loops in graphs.

---

## Summary — Key Takeaways for Interviews

| Topic | What interviewers test |
|-------|----------------------|
| HashMap internals | hashCode → bucket → equals chain → treeify → resize |
| Fail-fast vs fail-safe | When CME thrown, how to safely remove during iteration |
| Thread-safe collections | ConcurrentHashMap vs Collections.synchronizedMap |
| Mutable key in HashMap | Classic trap — always use immutable keys |
| null handling | HashMap allows, TreeMap/ConcurrentHashMap/Hashtable don't |
| equals + hashCode | Contract — if equals, must have same hashCode |
| Choosing collections | Trade-offs of each for given access patterns |
