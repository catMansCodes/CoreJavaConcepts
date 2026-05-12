# Java Collection Framework - Complete Step-by-Step Guide

## 1. What Is the Java Collection Framework?

The Java Collection Framework is a standard set of interfaces, classes, and algorithms used to store, retrieve, process, and manipulate groups of objects.

It gives Java developers ready-made data structures such as:

- `ArrayList`
- `LinkedList`
- `HashSet`
- `TreeSet`
- `HashMap`
- `TreeMap`
- `PriorityQueue`
- `Deque`
- Concurrent collections

Important package:

```java
java.util
```

Concurrent collection package:

```java
java.util.concurrent
```

---

## 2. Collection Framework Root Hierarchy

At a high level, the Java Collection Framework is divided into two main parts:

```text
Iterable
  |
  +-- Collection
        |
        +-- List
        |     +-- ArrayList
        |     +-- LinkedList
        |     +-- Vector
        |           +-- Stack
        |
        +-- Set
        |     +-- HashSet
        |     +-- LinkedHashSet
        |     +-- SortedSet
        |           +-- NavigableSet
        |                 +-- TreeSet
        |
        +-- Queue
              +-- PriorityQueue
              +-- Deque
                    +-- ArrayDeque
                    +-- LinkedList

Map
  |
  +-- HashMap
  +-- LinkedHashMap
  +-- Hashtable
  +-- SortedMap
        +-- NavigableMap
              +-- TreeMap
```

Important point:

`Map` is part of the Java Collection Framework, but it does **not** extend the `Collection` interface.

---

## 3. Iterable Interface

`Iterable` is the root interface that allows an object to be used in an enhanced `for` loop.

```java
for (String name : names) {
    System.out.println(name);
}
```

Main method:

```java
Iterator<T> iterator();
```

Every `Collection` is `Iterable`.

---

## 4. Collection Interface

`Collection` is the root interface for most collection types except `Map`.

Common methods:

```java
add(E e)
remove(Object o)
contains(Object o)
size()
isEmpty()
clear()
iterator()
toArray()
```

Direct child interfaces:

- `List`
- `Set`
- `Queue`

---

## 5. List Interface

`List` is an ordered collection that allows duplicate elements.

Key properties:

- Maintains insertion order
- Allows duplicates
- Allows index-based access
- Usually allows `null`

Common methods:

```java
get(int index)
set(int index, E element)
add(int index, E element)
remove(int index)
indexOf(Object o)
lastIndexOf(Object o)
```

### 5.1 ArrayList

`ArrayList` uses a dynamic array internally.

Best for:

- Fast read/access by index
- Frequent iteration
- Mostly add at end

Not best for:

- Frequent insert/delete in the middle

Example:

```java
List<String> names = new ArrayList<>();
names.add("Amit");
names.add("Rahul");
names.add("Amit");
```

Time complexity:

| Operation | Complexity |
| --------- | ---------- |
| get       | O(1)       |
| add end   | O(1) amortized |
| add middle | O(n)      |
| remove middle | O(n)   |
| search    | O(n)       |

### 5.2 LinkedList

`LinkedList` uses a doubly linked list internally.

It implements both:

- `List`
- `Deque`

Best for:

- Frequent insertion/deletion at beginning or end
- Queue or deque behavior

Not best for:

- Random access by index

Example:

```java
LinkedList<String> queue = new LinkedList<>();
queue.addLast("Task-1");
queue.addLast("Task-2");
queue.removeFirst();
```

Time complexity:

| Operation | Complexity |
| --------- | ---------- |
| get       | O(n)       |
| add first | O(1)       |
| add last  | O(1)       |
| remove first | O(1)   |
| remove last | O(1)    |
| search    | O(n)       |

### 5.3 Vector

`Vector` is a legacy synchronized dynamic array.

Key points:

- Thread-safe because methods are synchronized
- Slower than `ArrayList` in normal single-threaded usage
- Rarely preferred in modern code

Use `ArrayList` for normal cases and concurrent collections for multi-threaded cases.

### 5.4 Stack

`Stack` extends `Vector` and represents LIFO behavior.

LIFO means Last In, First Out.

Example:

```java
Stack<Integer> stack = new Stack<>();
stack.push(10);
stack.push(20);
stack.pop(); // 20
```

Modern recommendation:

Use `ArrayDeque` instead of `Stack`.

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(10);
stack.push(20);
stack.pop();
```

---

## 6. Set Interface

`Set` is a collection that does not allow duplicate elements.

Key properties:

- No duplicates
- At most one `null` depending on implementation
- Useful for uniqueness

### 6.1 HashSet

`HashSet` is backed by a `HashMap`.

Key properties:

- Does not maintain insertion order
- Allows one `null`
- Very fast for add, remove, and contains

Example:

```java
Set<String> emails = new HashSet<>();
emails.add("a@test.com");
emails.add("b@test.com");
emails.add("a@test.com"); // duplicate ignored
```

Time complexity:

| Operation | Complexity |
| --------- | ---------- |
| add       | O(1) average |
| remove    | O(1) average |
| contains  | O(1) average |

### 6.2 LinkedHashSet

`LinkedHashSet` maintains insertion order.

Best for:

- Unique elements
- Predictable iteration order

Example:

```java
Set<String> names = new LinkedHashSet<>();
names.add("A");
names.add("B");
names.add("C");
```

Iteration order will be:

```text
A, B, C
```

### 6.3 SortedSet

`SortedSet` stores elements in sorted order.

Important method examples:

```java
first()
last()
headSet(E toElement)
tailSet(E fromElement)
subSet(E fromElement, E toElement)
```

### 6.4 NavigableSet

`NavigableSet` extends `SortedSet` and provides navigation methods.

Important methods:

```java
lower(E e)
floor(E e)
ceiling(E e)
higher(E e)
descendingSet()
```

### 6.5 TreeSet

`TreeSet` implements `NavigableSet`.

Key properties:

- Stores elements in sorted order
- Does not allow duplicates
- Does not allow `null` in modern Java
- Uses Red-Black Tree internally

Example:

```java
Set<Integer> numbers = new TreeSet<>();
numbers.add(30);
numbers.add(10);
numbers.add(20);
```

Iteration order:

```text
10, 20, 30
```

Time complexity:

| Operation | Complexity |
| --------- | ---------- |
| add       | O(log n)   |
| remove    | O(log n)   |
| contains  | O(log n)   |

---

## 7. Queue Interface

`Queue` is used to process elements in a specific order, commonly FIFO.

FIFO means First In, First Out.

Common methods:

| Method | Throws Exception | Returns Special Value |
| ------ | ---------------- | --------------------- |
| Insert | `add(e)`         | `offer(e)`            |
| Remove | `remove()`       | `poll()`              |
| Read   | `element()`      | `peek()`              |

Prefer `offer`, `poll`, and `peek` when failure is possible.

### 7.1 PriorityQueue

`PriorityQueue` processes elements based on priority, not insertion order.

By default, it uses natural ordering.

Example:

```java
Queue<Integer> queue = new PriorityQueue<>();
queue.offer(30);
queue.offer(10);
queue.offer(20);

System.out.println(queue.poll()); // 10
```

For custom ordering:

```java
Queue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
```

Key points:

- Does not allow `null`
- Not thread-safe
- Uses heap internally

---

## 8. Deque Interface

`Deque` means Double Ended Queue.

It allows insertion and deletion from both ends.

Common methods:

```java
addFirst(E e)
addLast(E e)
removeFirst()
removeLast()
peekFirst()
peekLast()
push(E e)
pop()
```

### 8.1 ArrayDeque

`ArrayDeque` is a resizable array implementation of `Deque`.

Best for:

- Queue behavior
- Stack behavior
- Faster replacement for `Stack`

Example as stack:

```java
Deque<String> stack = new ArrayDeque<>();
stack.push("A");
stack.push("B");
stack.pop(); // B
```

Example as queue:

```java
Deque<String> queue = new ArrayDeque<>();
queue.offerLast("A");
queue.offerLast("B");
queue.pollFirst(); // A
```

---

## 9. Map Interface

`Map` stores data in key-value pairs.

Key properties:

- Keys must be unique
- Values can be duplicate
- A key maps to exactly one value

Common methods:

```java
put(K key, V value)
get(Object key)
remove(Object key)
containsKey(Object key)
containsValue(Object value)
keySet()
values()
entrySet()
```

Example:

```java
Map<Integer, String> users = new HashMap<>();
users.put(1, "Amit");
users.put(2, "Rahul");
users.put(1, "Neha"); // replaces Amit
```

### 9.1 HashMap

`HashMap` is the most commonly used `Map` implementation.

Key properties:

- Does not maintain insertion order
- Allows one `null` key
- Allows multiple `null` values
- Not synchronized
- Uses hashing internally

Average complexity:

| Operation | Complexity |
| --------- | ---------- |
| put       | O(1) average |
| get       | O(1) average |
| remove    | O(1) average |

Important interview point:

From Java 8 onward, when too many keys collide in the same bucket, `HashMap` can convert the bucket from a linked list to a balanced tree. This improves worst-case lookup from O(n) to O(log n) for that bucket.

### 9.2 LinkedHashMap

`LinkedHashMap` maintains insertion order.

Best for:

- Predictable iteration order
- LRU cache implementation

Example:

```java
Map<Integer, String> map = new LinkedHashMap<>();
map.put(3, "C");
map.put(1, "A");
map.put(2, "B");
```

Iteration order:

```text
3, 1, 2
```

### 9.3 TreeMap

`TreeMap` stores keys in sorted order.

Key properties:

- Implements `NavigableMap`
- Uses Red-Black Tree internally
- Does not allow `null` keys
- Allows multiple `null` values

Example:

```java
Map<Integer, String> map = new TreeMap<>();
map.put(3, "C");
map.put(1, "A");
map.put(2, "B");
```

Iteration order:

```text
1, 2, 3
```

Time complexity:

| Operation | Complexity |
| --------- | ---------- |
| put       | O(log n)   |
| get       | O(log n)   |
| remove    | O(log n)   |

### 9.4 Hashtable

`Hashtable` is a legacy synchronized map.

Key properties:

- Thread-safe because methods are synchronized
- Does not allow `null` key
- Does not allow `null` value
- Slower than `HashMap`

Modern recommendation:

Use `ConcurrentHashMap` for concurrent use cases.

---

## 10. Concurrent Collections

Concurrent collections are designed for multi-threaded environments.

Common classes:

```text
ConcurrentHashMap
CopyOnWriteArrayList
CopyOnWriteArraySet
BlockingQueue
ArrayBlockingQueue
LinkedBlockingQueue
PriorityBlockingQueue
ConcurrentLinkedQueue
ConcurrentLinkedDeque
```

### 10.1 ConcurrentHashMap

Best replacement for synchronized map usage.

Key properties:

- Thread-safe
- High performance for concurrent reads/writes
- Does not allow `null` key or value

Example:

```java
Map<String, Integer> scores = new ConcurrentHashMap<>();
scores.put("Amit", 90);
scores.put("Rahul", 85);
```

### 10.2 CopyOnWriteArrayList

Best for:

- Many reads
- Very few writes

It creates a fresh copy of the internal array during modification.

Example use cases:

- Listener lists
- Configuration snapshots
- Read-heavy collections

### 10.3 BlockingQueue

`BlockingQueue` is commonly used in producer-consumer problems.

Important methods:

```java
put(E e)   // waits if queue is full
take()     // waits if queue is empty
offer(E e)
poll()
```

Example:

```java
BlockingQueue<String> queue = new LinkedBlockingQueue<>();
queue.put("task");
String task = queue.take();
```

---

## 11. Sorting Collections

### 11.1 Natural Sorting with Comparable

Use `Comparable` when the class has a default natural order.

```java
class Employee implements Comparable<Employee> {
    private int id;

    @Override
    public int compareTo(Employee other) {
        return Integer.compare(this.id, other.id);
    }
}
```

### 11.2 Custom Sorting with Comparator

Use `Comparator` when sorting logic is external or when multiple sorting rules are needed.

```java
List<String> names = new ArrayList<>();
names.sort(Comparator.naturalOrder());
names.sort(Comparator.reverseOrder());
```

Custom object sorting:

```java
employees.sort(Comparator.comparing(Employee::getName));
employees.sort(Comparator.comparing(Employee::getSalary).reversed());
```

---

## 12. Iterator, ListIterator, and Fail-Fast Behavior

### 12.1 Iterator

Used to traverse a collection.

```java
Iterator<String> iterator = names.iterator();
while (iterator.hasNext()) {
    String name = iterator.next();
}
```

Safe removal during iteration:

```java
Iterator<String> iterator = names.iterator();
while (iterator.hasNext()) {
    if (iterator.next().startsWith("A")) {
        iterator.remove();
    }
}
```

### 12.2 ListIterator

`ListIterator` works only with `List`.

Extra features:

- Traverse forward
- Traverse backward
- Add element
- Replace element

### 12.3 Fail-Fast Iterators

Most collection iterators are fail-fast.

That means if a collection is structurally modified while iterating, except through the iterator's own `remove` method, Java may throw:

```java
ConcurrentModificationException
```

Example problem:

```java
for (String name : names) {
    names.remove(name); // can throw ConcurrentModificationException
}
```

Correct approach:

```java
names.removeIf(name -> name.startsWith("A"));
```

---

## 13. Null, Duplicate, and Ordering Rules

| Collection | Duplicates | Null Allowed | Ordering |
| ---------- | ---------- | ------------ | -------- |
| ArrayList | Yes | Yes | Insertion order |
| LinkedList | Yes | Yes | Insertion order |
| Vector | Yes | Yes | Insertion order |
| Stack | Yes | Yes | LIFO access |
| HashSet | No | One null | No guaranteed order |
| LinkedHashSet | No | One null | Insertion order |
| TreeSet | No | No | Sorted order |
| PriorityQueue | Yes | No | Priority order |
| ArrayDeque | Yes | No | Queue/deque order |
| HashMap | Unique keys | One null key, multiple null values | No guaranteed order |
| LinkedHashMap | Unique keys | One null key, multiple null values | Insertion order |
| TreeMap | Unique keys | No null key, multiple null values | Sorted by key |
| Hashtable | Unique keys | No null key/value | No guaranteed order |
| ConcurrentHashMap | Unique keys | No null key/value | No guaranteed order |

---

## 14. Which Collection Should You Use?

| Requirement | Best Choice |
| ----------- | ----------- |
| Fast index-based access | `ArrayList` |
| Frequent add/remove at both ends | `ArrayDeque` |
| Queue behavior | `ArrayDeque` |
| Stack behavior | `ArrayDeque` |
| Unique values, fastest lookup | `HashSet` |
| Unique values with insertion order | `LinkedHashSet` |
| Unique sorted values | `TreeSet` |
| Key-value lookup | `HashMap` |
| Key-value with insertion order | `LinkedHashMap` |
| Key-value sorted by key | `TreeMap` |
| Thread-safe high performance map | `ConcurrentHashMap` |
| Producer-consumer queue | `BlockingQueue` |
| Read-heavy thread-safe list | `CopyOnWriteArrayList` |

---

## 15. Important Interview Questions

### 1. What is the difference between Collection and Collections?

`Collection` is an interface. It is the root interface for `List`, `Set`, and `Queue`.

`Collections` is a utility class. It provides static helper methods like `sort`, `reverse`, `shuffle`, `min`, `max`, and `synchronizedList`.

### 2. Why does Map not extend Collection?

`Collection` represents a group of individual elements.

`Map` represents key-value pairs, so its structure is different.

### 3. ArrayList vs LinkedList?

Use `ArrayList` when read operations are frequent.

Use `LinkedList` when frequent insert/delete operations happen at the beginning or end.

In most real-world cases, `ArrayList` is preferred because it is memory-efficient and cache-friendly.

### 4. HashSet vs TreeSet?

`HashSet` is faster and does not maintain order.

`TreeSet` is slower but keeps elements sorted.

### 5. HashMap vs Hashtable?

`HashMap` is not synchronized and allows one `null` key.

`Hashtable` is synchronized and does not allow `null` keys or values.

`Hashtable` is legacy. Prefer `ConcurrentHashMap` for thread-safe code.

### 6. HashMap vs ConcurrentHashMap?

`HashMap` is not thread-safe.

`ConcurrentHashMap` is thread-safe and optimized for concurrent access.

### 7. Comparable vs Comparator?

`Comparable` defines natural ordering inside the class.

`Comparator` defines external custom ordering.

### 8. Why should ArrayDeque be preferred over Stack?

`Stack` is legacy and extends synchronized `Vector`.

`ArrayDeque` is faster and cleaner for stack operations in modern Java.

---

## 16. Step-by-Step Learning Path

Follow this order:

1. Learn `Iterable` and `Collection`.
2. Learn `List`: `ArrayList`, `LinkedList`, `Vector`, `Stack`.
3. Learn `Set`: `HashSet`, `LinkedHashSet`, `TreeSet`.
4. Learn `Queue`: `PriorityQueue`.
5. Learn `Deque`: `ArrayDeque`, `LinkedList`.
6. Learn `Map`: `HashMap`, `LinkedHashMap`, `TreeMap`, `Hashtable`.
7. Learn sorting using `Comparable` and `Comparator`.
8. Learn iteration using `Iterator` and `ListIterator`.
9. Learn fail-fast behavior and safe removal.
10. Learn concurrent collections.
11. Practice common interview questions and coding examples.

---

## 17. Quick Summary

```text
List  -> ordered, duplicates allowed
Set   -> unique elements
Queue -> processing order
Deque -> add/remove from both ends
Map   -> key-value pairs
```

Most commonly used implementations:

```text
ArrayList
HashSet
HashMap
LinkedHashMap
TreeMap
ArrayDeque
PriorityQueue
ConcurrentHashMap
```

For interviews, focus deeply on:

- `ArrayList` internals
- `LinkedList` internals
- `HashMap` internals
- `HashSet` and `HashMap` relationship
- `Comparable` vs `Comparator`
- Fail-fast behavior
- `ConcurrentHashMap`
- Choosing the right collection for the right scenario
