# JVM, Memory & Internals — Java Interview Guide (8–10 Years Experience)

## Quick Reference

```
JVM Subsystems:  ClassLoader → Bytecode Verifier → Execution Engine (Interpreter + JIT) → GC
Memory Areas:    Heap (Young Gen + Old Gen + Metaspace) | Stack | PC Register | Native Stack
GC Algorithms:  Serial | Parallel | CMS | G1 (default Java 9-21) | ZGC | Shenandoah
ClassLoaders:   Bootstrap → Extension → Application (parent delegation model)
JIT:            HotSpot compiles bytecode to native after threshold (~10,000 calls)
```

---

## Q1. What is the JVM? Explain its architecture.

**Difficulty:** Senior | **Type:** Theory

**Answer:**

JVM (Java Virtual Machine) is an abstract computing machine that enables Java's "Write Once, Run Anywhere" by executing bytecode on any platform.

```
Source (.java) → javac → Bytecode (.class) → JVM → Machine code

JVM Architecture:
┌─────────────────────────────────────────────┐
│           Class Loader Subsystem            │
│   Bootstrap → Extension → Application       │
├─────────────────────────────────────────────┤
│               Runtime Data Areas            │
│  Method Area  │ Heap  │ Stack  │ PC Register │
│  (Metaspace)  │       │       │ Native Stack │
├─────────────────────────────────────────────┤
│             Execution Engine                │
│  Interpreter │ JIT Compiler │ GC           │
├─────────────────────────────────────────────┤
│      Native Method Interface (JNI)          │
└─────────────────────────────────────────────┘
```

**Steps:**
1. Source code compiled to `.class` (bytecode) by `javac`
2. ClassLoader loads `.class` files into method area
3. Bytecode verifier checks safety (no stack overflows, valid type usage)
4. Interpreter executes bytecode line by line (slow)
5. JIT compiler identifies "hot" code and compiles to native machine code (fast)
6. GC manages heap memory lifecycle

---

## Q2. Explain JVM Memory Areas in detail.

**Difficulty:** Senior | **Type:** Theory (most asked)

**Answer:**

```
┌──────────────────────────────────────────────┐
│                  HEAP                        │
│  ┌──────────────────┐  ┌──────────────────┐  │
│  │   Young Gen      │  │    Old Gen       │  │
│  │ ┌────┬────┬────┐ │  │  (Tenured)      │  │
│  │ │Eden│ S0 │ S1 │ │  │  Long-lived     │  │
│  │ └────┴────┴────┘ │  │  objects        │  │
│  │  New objects     │  │                 │  │
│  └──────────────────┘  └──────────────────┘  │
├──────────────────────────────────────────────┤
│   Metaspace (Java 8+) — was PermGen (≤Java 7)│
│   Class metadata, static variables, method    │
│   bytecode, constant pool                     │
├──────────────────────────────────────────────┤
│   Thread Stack (per thread)                  │
│   Stack frames: local vars, operand stack     │
│   method call chain                           │
├──────────────────────────────────────────────┤
│   PC Register (per thread) — current instr   │
│   Native Method Stack (JNI calls)            │
└──────────────────────────────────────────────┘
```

| Area | Thread-shared? | Stores | Error |
|------|---------------|--------|-------|
| Heap | Yes | Objects, arrays, instance vars | `OutOfMemoryError` |
| Metaspace | Yes | Class metadata, static vars | `OutOfMemoryError` |
| Stack | No (per-thread) | Stack frames, local vars, refs | `StackOverflowError` |
| PC Register | No (per-thread) | Next bytecode instruction address | — |
| Native Stack | No (per-thread) | Native method calls | — |

---

## Q3. What is the difference between Stack and Heap memory?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
public void method() {
    int x = 10;            // x: Stack (primitive)
    String s = "hello";    // s reference: Stack; "hello": Heap (String pool)
    Person p = new Person(); // p reference: Stack; Person object: Heap
}
// When method returns: stack frame is popped → x, s, p references gone
// Heap objects remain until GC collects them
```

| | Stack | Heap |
|-|-------|------|
| Stores | Primitive locals, object references, method frames | Objects, arrays |
| Access | LIFO — fast | Random access — slower |
| Size | Small (~256KB-1MB default) | Large (configured with -Xmx) |
| Thread | Private | Shared |
| Lifecycle | Auto (frame pop) | GC-managed |
| Error | `StackOverflowError` | `OutOfMemoryError` |

---

## Q4. What is the ClassLoader? Explain the parent delegation model.

**Difficulty:** Senior | **Type:** Theory

**Answer:**

ClassLoader loads `.class` bytecode into the JVM. Three built-in loaders:

| ClassLoader | Loads | Source |
|-------------|-------|--------|
| Bootstrap | Core Java classes (`java.lang`, `java.util`, etc.) | `$JAVA_HOME/lib/rt.jar` (or modules in Java 9+) |
| Extension (Platform) | Extension/platform classes | `$JAVA_HOME/lib/ext/` |
| Application (System) | Application classpath classes | `-classpath` |

**Parent Delegation Model:**
1. Application ClassLoader → asks Extension ClassLoader
2. Extension ClassLoader → asks Bootstrap ClassLoader
3. Bootstrap tries to load — if found, returns; if not, delegates back down
4. If no loader can find the class → `ClassNotFoundException`

```java
// Inspecting class loaders
System.out.println(String.class.getClassLoader());      // null (Bootstrap — native)
System.out.println(MyClass.class.getClassLoader());     // sun.misc.Launcher$AppClassLoader
System.out.println(MyClass.class.getClassLoader().getParent()); // Extension
System.out.println(MyClass.class.getClassLoader().getParent().getParent()); // null (Bootstrap)

// Custom ClassLoader
class HotSwapLoader extends ClassLoader {
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = loadBytesFromSomewhere(name);
        return defineClass(name, bytes, 0, bytes.length);
    }
}
```

**Why parent delegation?** Security — prevents malicious code from overriding `java.lang.String` with a custom version.

---

## Q5. What is the JIT Compiler? How does it work?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

JIT (Just-In-Time) Compiler converts bytecode to native machine code at runtime for frequently executed ("hot") code.

```
Bytecode → Interpreter (slow, runs each time)
         → JIT Compiler (when method called ~10,000 times)
           → Native Code (fast, cached and reused)
```

**HotSpot JVM has two JIT compilers:**
- **C1 (Client compiler)** — fast compilation, less optimization. Used for methods called moderately.
- **C2 (Server compiler)** — slow compilation, aggressive optimization. Used for hot loops.
- **Tiered Compilation (default Java 8+)** — starts with C1, moves to C2 as code gets hotter.

**JIT Optimizations:**
- Inlining — replaces method call with method body (most impactful)
- Loop unrolling — reduces loop overhead
- Dead code elimination — removes unreachable code
- Escape analysis — if object doesn't escape method, allocated on stack (not heap)
- Constant folding — computes constant expressions at compile time

```java
// This loop will be JIT-compiled to very efficient native code
long sum = 0;
for (int i = 0; i < 1_000_000; i++) {
    sum += i; // JIT may unroll, vectorize, eliminate bounds checks
}
```

---

## Q6. What is Garbage Collection? How does it work?

**Difficulty:** Senior | **Type:** Theory (most asked)

**Answer:**

GC automatically reclaims memory occupied by objects no longer reachable from any **GC root**.

**GC Roots:**
- Local variables in active stack frames
- Static variables
- Active threads
- JNI references

**Object Reachability:**
- **Strongly reachable** — reachable via strong references → NOT collected
- **Softly reachable** — only via SoftReference → collected when memory low
- **Weakly reachable** — only via WeakReference → collected on next GC
- **Phantom reachable** — only via PhantomReference → collected after finalize

**GC Process (Generational Hypothesis: most objects die young):**
```
1. Minor GC (Young Gen):
   - New objects → Eden
   - Eden full → Minor GC runs
   - Alive objects → Survivor (S0 or S1), age++
   - Objects surviving N GCs (threshold ~15) → promoted to Old Gen

2. Major/Full GC (Old Gen):
   - Old Gen fills up → Major GC
   - Slower — must collect large space
   - Causes longer Stop-The-World pause
```

---

## Q7. What are the GC algorithms in Java? Which is default?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

| GC | Algorithm | Pause | Throughput | Best for |
|----|-----------|-------|-----------|----------|
| Serial | Stop-The-World, single thread | Long | Low | Single-core, small heaps |
| Parallel (Throughput) | Stop-The-World, multi-thread | Medium | High | Batch processing |
| CMS | Concurrent mark-sweep | Short | Medium | Low-latency (deprecated Java 9) |
| **G1** | Concurrent + incremental | Short | Good | **Default Java 9–21+** |
| ZGC | Concurrent, colored pointers | <10ms | Good | Very large heaps, ultra-low latency |
| Shenandoah | Concurrent compaction | <10ms | Good | Red Hat's low-pause GC |

```bash
# Configure GC
java -XX:+UseG1GC MyApp
java -XX:+UseZGC MyApp
java -XX:+UseSerialGC MyApp
java -XX:+UseParallelGC MyApp

# Heap sizing
java -Xms512m -Xmx2g MyApp   # initial heap 512MB, max 2GB
java -Xmn256m MyApp           # young gen size 256MB

# GC logging
java -Xlog:gc MyApp           # Java 9+
java -XX:+PrintGCDetails MyApp # Java 8
```

---

## Q8. What is G1 GC? How is it different from older collectors?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

G1 (Garbage First) divides heap into equal-sized **regions** (~1-32MB each) rather than fixed Young/Old areas. Regions can be Eden, Survivor, Old, or Humongous (for large objects).

**Key features:**
- Collects the regions with most garbage first (hence "Garbage First")
- Concurrent marking happens while app runs
- Pause time can be set as a goal: `-XX:MaxGCPauseMillis=200`
- Self-tuning — adjusts region allocation to meet pause goals
- Humongous regions for objects > 50% of region size

```bash
java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:G1HeapRegionSize=16m \
     -Xmx8g MyApp
```

---

## Q9. What is the difference between PermGen and Metaspace?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

| | PermGen (≤Java 7) | Metaspace (Java 8+) |
|-|------------------|---------------------|
| Location | Part of JVM heap (fixed size) | Native memory (off-heap) |
| Default size | Fixed, e.g., 64MB | Grows automatically (limited by OS memory) |
| Common error | `java.lang.OutOfMemoryError: PermGen space` | `java.lang.OutOfMemoryError: Metaspace` |
| Stores | Class metadata, interned strings, static vars | Class metadata (strings moved to heap, static vars to Java heap) |

```bash
# Java 7 PermGen config
java -XX:PermSize=64m -XX:MaxPermSize=256m MyApp

# Java 8+ Metaspace config
java -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=256m MyApp
# If MaxMetaspaceSize not set, metaspace grows until OOM
```

**Why removed?** Fixed PermGen caused frequent `OutOfMemoryError: PermGen space` in apps with many dynamic class loading (OSGi, JSPs, Groovy scripts). Metaspace avoids this.

---

## Q10. What causes `OutOfMemoryError`? Different types.

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

```
java.lang.OutOfMemoryError: Java heap space
  → Too many live objects, heap too small
  → Fix: -Xmx, optimize object retention, fix memory leaks

java.lang.OutOfMemoryError: GC overhead limit exceeded
  → GC spending >98% of time collecting <2% memory
  → Fix: increase heap, fix memory leaks

java.lang.OutOfMemoryError: Metaspace
  → Too many loaded classes (dynamic class generation, class leaks)
  → Fix: -XX:MaxMetaspaceSize, unload classes

java.lang.OutOfMemoryError: unable to create native thread
  → Too many threads, OS limit exceeded
  → Fix: reduce thread count, increase OS limits

java.lang.OutOfMemoryError: Direct buffer memory
  → NIO direct buffers exhausted
  → Fix: -XX:MaxDirectMemorySize, release buffers

java.lang.StackOverflowError
  → Deep/infinite recursion
  → Fix: add base case, use iteration, -Xss (stack size)
```

```java
// Memory leak example — forgotten listeners
class EventBus {
    static List<Listener> listeners = new ArrayList<>(); // static = GC root
    static void register(Listener l) { listeners.add(l); }
    // No unregister method — listeners accumulate forever → OOM!
}

// GC root prevents collection:
// static List → holds Listener references → Listener objects → everything they reference
```

---

## Q11. What is `StackOverflowError`? When does it occur?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`StackOverflowError` occurs when the thread's call stack exceeds its maximum depth. Each method call adds a stack frame; infinite recursion fills the stack.

```java
// Infinite recursion
int factorial(int n) {
    return n * factorial(n - 1); // no base case → StackOverflowError
}

// Even with base case but large input
long factorial(long n) {
    if (n <= 1) return 1;
    return n * factorial(n - 1); // StackOverflowError for n > ~5000-10000
}

// Circular toString()
class A {
    B b;
    @Override public String toString() { return "A[" + b + "]"; }
}
class B {
    A a;
    @Override public String toString() { return "B[" + a + "]"; }
}
// System.out.println(new A()); → StackOverflowError

// Fix: use iteration
long factIterative(long n) {
    long result = 1;
    for (long i = 2; i <= n; i++) result *= i;
    return result;
}

// Increase stack size
// java -Xss8m MyApp  (default is ~512KB-1MB)
```

---

## Q12. What are `SoftReference`, `WeakReference`, and `PhantomReference`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

| Reference | GC behavior | Use case |
|-----------|------------|----------|
| Strong (default) | Never GC'd while reachable | Normal usage |
| `SoftReference` | GC'd only when memory is low | Memory-sensitive caches |
| `WeakReference` | GC'd on next GC cycle (whenever) | Canonical maps, `WeakHashMap` |
| `PhantomReference` | GC'd, referent always null; notification after finalization | Pre-mortem cleanup, alternative to finalize() |

```java
// SoftReference — cache that yields to memory pressure
SoftReference<byte[]> cache = new SoftReference<>(new byte[10_000_000]);
byte[] data = cache.get();
if (data == null) {
    data = reloadData(); // cache was collected
    cache = new SoftReference<>(data);
}

// WeakReference — non-owning reference
WeakReference<String> wr = new WeakReference<>(new String("hello"));
System.gc();
System.out.println(wr.get()); // null — GC collected it (no strong reference)

// ReferenceQueue — notification when reference is cleared
ReferenceQueue<Object> queue = new ReferenceQueue<>();
WeakReference<Object> ref = new WeakReference<>(new Object(), queue);
System.gc();
Reference<?> cleared = queue.poll(); // returns ref if cleared
System.out.println(cleared != null); // true
```

---

## Q13. What is object finalization? Why is `finalize()` deprecated?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`finalize()` is called by GC before collecting an object. Problems:
1. **Unpredictable** — may never run if JVM exits
2. **Performance** — objects with `finalize()` require extra GC cycles (two GC passes to collect)
3. **Can resurrect objects** — if `finalize()` stores `this` in a static field
4. **Security** — finalization attacks possible

```java
// Deprecated pattern
class Resource {
    @Override
    @Deprecated
    protected void finalize() throws Throwable {
        cleanup(); // unreliable!
    }
}

// Modern approach: AutoCloseable + try-with-resources
class Resource implements AutoCloseable {
    @Override
    public void close() {
        cleanup(); // predictable, explicit cleanup
    }
}

try (Resource r = new Resource()) {
    r.use();
} // r.close() called here, guaranteed

// Or Cleaner (Java 9+) — replaces PhantomReference pattern
Cleaner cleaner = Cleaner.create();
class Resource {
    private final Cleaner.Cleanable cleanable;
    Resource() {
        cleanable = cleaner.register(this, () -> System.out.println("Cleaning up"));
    }
    void close() { cleanable.clean(); }
}
```

---

## Q14. What is escape analysis?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

**Escape analysis** is a JIT optimization that determines if an object's lifetime is confined to a method or thread. If it doesn't "escape":
- **Stack allocation** — object allocated on stack (not heap) → no GC needed
- **Scalar replacement** — object is decomposed into primitive fields
- **Lock elision** — synchronized on non-escaping object → lock removed

```java
// Object doesn't escape — JIT may allocate on stack, not heap
void compute() {
    Point p = new Point(1, 2); // p never returned, never stored in field
    int result = p.x + p.y;    // compiler may eliminate Point object entirely
}

// Object escapes — must be on heap
Point createPoint() {
    return new Point(1, 2); // returns = escapes
}

// Thread escape — shared via field
static Point shared;
void setShared() {
    shared = new Point(1, 2); // escapes to another thread
}
```

**Practical implication:** Short-lived objects in methods are very cheap in modern JVMs — don't pre-optimize by reusing object pools unnecessarily.

---

## Q15. What is Stop-The-World (STW) pause?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

Stop-The-World pause = all application threads are paused while GC runs certain phases.

**Why needed?** During GC traversal of object graph, objects must not change (no new objects created, no references modified). Otherwise GC might miss live objects or collect live ones.

**STW phases by GC:**
- Serial/Parallel GC — full STW during collection
- G1 — STW during Initial Mark and Remark; concurrent during most of cycle
- ZGC/Shenandoah — near-zero STW (< 1ms typically)

```bash
# Diagnose STW pauses
java -Xlog:gc*:file=gc.log MyApp  # Java 9+

# If STW > 500ms, investigate:
# - Heap too large? (more objects = longer traversal)
# - Too many allocations? (frequent minor GC)
# - GC wrong for workload? (try ZGC for latency-sensitive apps)
```

---

## Q16. What is method area / Metaspace?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

The **Method Area** (called Metaspace in Java 8+) stores:
- Class metadata (class name, superclass, interfaces, fields, methods)
- Method bytecode
- Constant pool (string literals, class/method references)
- Static variables (moved to heap in Java 8+)

```java
// Things stored in Metaspace:
class Example {
    static int count = 0;         // static var: Java heap (not Metaspace in Java 8+)
    static final String NAME = "Example"; // String in pool, reference in Metaspace
    int instanceVar;               // instance var: per-object on heap
    void method() { }             // bytecode of method: Metaspace
}

// Metaspace grows when classes are loaded
// Can grow without bound (default) → OOM if too many classes
// Set limit: java -XX:MaxMetaspaceSize=256m
```

**Class loading leak:** In hot-deploy frameworks (Tomcat, OSGi), if old ClassLoader instances are not GC'd, their Metaspace is retained → memory leak.

---

## Q17. How does the GC identify live vs dead objects?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

GC uses **reachability analysis** (tracing from GC roots), not reference counting.

**Mark phase:** Start from GC roots, traverse all reachable objects, mark them.
**Sweep phase:** Collect all unmarked objects.
**Compact phase (optional):** Move live objects together to reduce fragmentation.

**GC Roots include:**
1. Local variables and parameters in active stack frames
2. Static fields of loaded classes
3. JNI (native) references
4. Active threads

```java
// This object is NOT reachable → will be collected
Object a = new Object(); // GC root via local var
a = null;                // root removed → object unreachable

// Circular references are NOT a problem (unlike ref counting)
class Node { Node next; }
Node n1 = new Node(); Node n2 = new Node();
n1.next = n2; n2.next = n1; // circular
n1 = null; n2 = null;
// Both are unreachable (no GC root) → both collected
// Java GC handles circular refs correctly
```

---

## Q18. What causes memory leaks in Java?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Memory leaks in Java occur when objects are **reachable but no longer needed** — GC cannot collect them.

**Common causes:**

```java
// 1. Static collections — accumulate forever
class Registry {
    static Map<String, Object> cache = new HashMap<>();
    static void put(String key, Object val) { cache.put(key, val); }
    // No removal → grows indefinitely
}

// 2. ThreadLocal without remove()
ThreadLocal<byte[]> tl = new ThreadLocal<>();
// In thread pool — thread never dies, ThreadLocal never cleared
// Fix: always call tl.remove() in finally

// 3. Listeners not deregistered
EventBus.register(myListener); // strong reference held in EventBus list
// When done: EventBus.unregister(myListener);

// 4. Inner class holding outer class reference
class Outer {
    class Inner { } // Inner holds implicit reference to Outer
    // If Inner is stored elsewhere and Outer is "done" → Outer can't be GC'd
}

// 5. Unclosed streams/connections
FileInputStream fis = new FileInputStream("file.txt");
// If exception thrown before fis.close() → connection never closed

// Fix: try-with-resources
try (FileInputStream fis2 = new FileInputStream("file.txt")) {
    // use fis2
}

// 6. String.intern() overuse in Java 6 — filled PermGen
```

---

## Q19. What JVM flags do you use in production?

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```bash
# Memory configuration
-Xms2g                          # initial heap size
-Xmx8g                          # max heap size
-XX:MetaspaceSize=256m          # initial metaspace
-XX:MaxMetaspaceSize=512m       # max metaspace

# GC configuration
-XX:+UseG1GC                    # G1 (default in Java 9+)
-XX:MaxGCPauseMillis=200        # GC pause target
-XX:G1HeapRegionSize=16m        # G1 region size
-XX:+UseZGC                     # ZGC for ultra-low latency (Java 15+)

# GC logging (Java 9+)
-Xlog:gc*:file=/logs/gc.log:time,uptime:filecount=5,filesize=20m

# Crash/OOM diagnostics
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/tmp/heapdump.hprof
-XX:+ExitOnOutOfMemoryError     # crash fast (avoid zombie state)

# Performance
-XX:+TieredCompilation          # on by default (C1 + C2)
-server                         # server-mode JVM optimizations

# Container awareness (Java 10+)
-XX:+UseContainerSupport        # respect Docker CPU/memory limits
-XX:MaxRAMPercentage=75.0       # use 75% of container memory
```

---

## Q20. What is bytecode? Can you read it?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

Bytecode is an intermediate representation — platform-independent instructions for the JVM.

```java
// Source
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello");
    }
}
```

```bash
# Disassemble
javap -c Hello.class
```

```
public static void main(java.lang.String[]);
    Code:
       0: getstatic     #7 // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #13 // String Hello
       5: invokevirtual #15 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: return
```

**Common opcodes:**
- `getstatic` / `putstatic` — static field access
- `getfield` / `putfield` — instance field access
- `invokevirtual` — instance method call (polymorphic)
- `invokestatic` — static method call
- `invokeinterface` — interface method call
- `invokespecial` — constructor, super calls
- `invokedynamic` — lambda, method handles (Java 7+)
- `new` — create object
- `return`, `ireturn`, `areturn` — method return

---

## Q21. What is the difference between `System.gc()` and `Runtime.gc()`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Both are hints to the JVM to run GC — neither guarantees GC will actually run.

```java
System.gc();            // calls Runtime.getRuntime().gc()
Runtime.getRuntime().gc(); // same thing — just a hint

// JVM may ignore the call
// Use case: before memory-intensive operation, to get a baseline
// Anti-pattern: calling in loops to "help" GC — actually hurts performance

// Force GC (not in production)
System.gc();
System.runFinalization(); // deprecated in Java 18+
Thread.sleep(1000); // wait, but not guaranteed

// Better: profile with JVisualVM, JFR, jmap
```

**When to call `System.gc()`:** Almost never in production. Legitimate uses: benchmarking, before taking heap dump in memory profiler, after unloading many classes.

---

## Q22. What is the `invokedynamic` instruction? (Lambda internals)

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`invokedynamic` (Java 7+) defers method binding to runtime. Used for:
- Lambda expressions
- String concatenation (Java 9+ with `StringConcatFactory`)
- Groovy, Kotlin dynamic dispatch

```java
Runnable r = () -> System.out.println("Hello");
// Compiled to something like:
// invokedynamic #bootstrapMethod → creates a Runnable implementation at runtime
// NOT compiled to a named inner class (as in older Java)

// The bootstrap method (LambdaMetafactory) generates the Runnable implementation
// on first call and caches it — subsequent calls reuse the same class
```

**Why invokedynamic?** Lambdas could have been compiled to anonymous inner classes, but:
- Anonymous classes are eager and verbose
- `invokedynamic` is lazy — class generated only when lambda is first called
- JVM can optimize lambda implementations over time without recompilation

---

## Q23. What is the difference between heap dump and thread dump?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

**Heap dump** — snapshot of all objects in heap at a point in time. Used to diagnose OOM errors and memory leaks.

```bash
# Generate heap dump
jmap -dump:format=b,file=heapdump.hprof <pid>
# Or on OOM: -XX:+HeapDumpOnOutOfMemoryError

# Analyze with: Eclipse Memory Analyzer (MAT), JVisualVM, VisualVM
```

**Thread dump** — snapshot of all thread states and their stack traces at a point in time. Used to diagnose deadlocks, high CPU, unresponsive threads.

```bash
# Generate thread dump
jstack <pid>                    # on Linux/Mac
kill -3 <pid>                   # sends SIGQUIT to JVM — prints to stdout

# Or programmatically
ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
ThreadInfo[] infos = tmx.dumpAllThreads(true, true);
// Analyze: look for BLOCKED threads, same lock address in multiple threads = deadlock
```

---

## Q24. What is Java Flight Recorder (JFR)?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

JFR (Java Flight Recorder) is a low-overhead profiling tool built into the JVM (open-sourced in Java 11). Records JVM events (GC, thread states, I/O, exceptions, method profiles) with minimal impact.

```bash
# Start recording
java -XX:StartFlightRecording=filename=recording.jfr,duration=60s MyApp

# Or at runtime with jcmd
jcmd <pid> JFR.start name=MyRecording duration=60s filename=recording.jfr
jcmd <pid> JFR.stop name=MyRecording

# Analyze with JDK Mission Control (JMC)
jmc  # opens GUI, load .jfr file
```

**Events captured:** GC pauses, thread activity, method profiling, heap usage, exception rates, socket/file I/O, classloading.

---

## Q25. What is a ClassCastException vs ClassNotFoundException vs NoClassDefFoundError?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| Exception | When | Example |
|-----------|------|---------|
| `ClassNotFoundException` | Checked — class not found on classpath at runtime | `Class.forName("com.example.Foo")` when jar missing |
| `NoClassDefFoundError` | Class was present at compile time, missing at runtime | Missing jar in production |
| `ClassCastException` | Casting object to incompatible type | `(String) new Integer(1)` |

```java
// ClassNotFoundException — checked, must handle
try {
    Class<?> c = Class.forName("com.example.NonExistent");
} catch (ClassNotFoundException e) {
    System.out.println("Class not found: " + e.getMessage());
}

// ClassCastException — runtime
Object obj = Integer.valueOf(42);
String s = (String) obj; // ClassCastException: Integer cannot be cast to String

// Safe cast with instanceof
if (obj instanceof String str) { // pattern matching (Java 16+)
    System.out.println(str.length());
}

// NoClassDefFoundError — class compiled against but missing at runtime
// java.lang.NoClassDefFoundError: com/example/Service
// Fix: add missing jar to classpath
```

---

## Q26. What is the String constant pool? Where is it in memory?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

- **Java 6 and earlier:** String pool was in **PermGen** (fixed, limited)
- **Java 7+:** String pool moved to **Heap** (GC-eligible, unlimited by heap)
- **Java 8+:** PermGen replaced by Metaspace; string pool stays in heap

```java
// Literals → string pool
String s1 = "hello";        // pool
String s2 = "hello";        // same pool reference
System.out.println(s1 == s2); // true

// new String() → heap (outside pool)
String s3 = new String("hello"); // new object on heap
System.out.println(s1 == s3);    // false

// intern() → add to pool or return existing
String s4 = s3.intern();
System.out.println(s1 == s4); // true — s4 is pool reference

// Runtime-created strings
String s5 = "hel" + "lo"; // compile-time constant → pool ("hello")
String prefix = "hel";
String s6 = prefix + "lo"; // runtime → heap object (not pool)

// How many pool entries after this code?
// "hello" — 1 pool entry (s1 == s2 == s5 == s4 all point to same)
```

---

## Q27. What is just-ahead-of-time (AOT) compilation?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

AOT compilation compiles Java bytecode to native machine code **before** execution (not at runtime like JIT). Available via:
- `jaotc` tool (Java 9–15, experimental, removed)
- GraalVM Native Image (production use)

```bash
# GraalVM Native Image
native-image -jar myapp.jar myapp-native

# Creates native executable
./myapp-native  # starts in milliseconds (no JVM startup time)
```

**JIT vs AOT:**
| | JIT | AOT |
|-|-----|-----|
| Startup time | Slow (warm-up needed) | Fast |
| Peak performance | Very high (runtime profiling info) | Lower (no runtime info) |
| Memory | JVM + JIT overhead | Smaller (no JVM) |
| Dynamic features | Full | Limited (reflection needs config) |
| Use case | Long-running servers | Serverless, CLI tools, microservices |

---

## Q28. What is the difference between interpreted and compiled execution?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```
Interpreted execution:
  bytecode → interpreter reads each instruction → executes → next
  + Works immediately, no compilation delay
  - Slow: re-interprets every execution

JIT compiled execution:
  bytecode → JIT detects "hot" code → compiles to native → caches
  + Fast: native code runs without interpreter overhead
  - Startup: needs warm-up time before JIT kicks in

Tiered compilation (default):
  Level 0: Interpreter (immediate)
  Level 1-3: C1 with progressive optimization
  Level 4: C2 (full optimization for very hot code)
```

---

## Q29. Scenario — Application is slow after startup. Diagnose it.

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```
Step 1: Identify if it's GC-related
  → Enable GC logging: -Xlog:gc*
  → Check: frequency of GC, pause duration
  → Is GC pausing > 500ms? → GC issue

Step 2: Identify if it's CPU-related  
  → jstack <pid> — take 3 dumps at 5s intervals
  → Look for threads in RUNNABLE state doing computation
  → Or threads stuck in BLOCKED (lock contention)

Step 3: Profile with JFR
  → jcmd <pid> JFR.start duration=60s
  → Analyze in JMC: hot methods, lock contention, GC events

Step 4: Check heap
  → jmap -histo <pid> — object histogram
  → Look for unexpectedly large counts of specific classes

Step 5: JIT warm-up
  → First requests slow due to interpretation? → normal
  → Use -XX:CompileThreshold=100 to JIT sooner (dev only)
  → Or warm up with load test before directing real traffic
```

---

## Q30. What is TLAB (Thread-Local Allocation Buffer)?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

TLAB is a small region in Eden space pre-allocated for each thread. New objects are allocated in the thread's own TLAB without synchronization.

**Why?** Heap allocation without TLAB would require synchronization for every `new` — expensive. With TLAB, each thread allocates in its own region — no synchronization needed.

```
Eden space
┌──────┬──────┬──────┬──────┐
│TLAB-1│TLAB-2│TLAB-3│ free │
│(T1)  │(T2)  │(T3)  │      │
└──────┴──────┴──────┴──────┘

Thread 1 allocates objects in TLAB-1 (no lock needed)
Thread 2 allocates in TLAB-2 (no lock needed)
...
When TLAB-1 fills: Thread 1 requests a new TLAB from JVM (lock needed, but infrequent)
```

```bash
# TLAB configuration
-XX:TLABSize=512k          # TLAB size
-XX:+PrintTLAB             # print TLAB stats
-XX:-UseTLAB               # disable (don't do this in production)
```

---

## Q31. What is class unloading? When does it happen?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

A class can be unloaded from Metaspace if:
1. All instances of that class have been GC'd
2. The `Class` object itself is no longer referenced
3. The ClassLoader that loaded the class is no longer referenced

**Bootstrap-loaded classes (java.lang.*, etc.) are NEVER unloaded.**

```java
// Class unloading example with custom ClassLoader
URLClassLoader loader = new URLClassLoader(new URL[]{new File("plugin.jar").toURI().toURL()});
Class<?> clazz = loader.loadClass("com.plugin.MyClass");
// use clazz...
clazz = null;     // remove Class reference
loader.close();   // close loader
loader = null;    // remove ClassLoader reference

System.gc(); // now clazz, all its instances, and the ClassLoader can be collected
// Metaspace freed
```

**Metaspace leak:** If ClassLoader is reachable (e.g., held by a ThreadLocal, or a static reference in another class), its classes are never unloaded → Metaspace grows → eventual OOM.

---

## Q32. What is the difference between `jstack`, `jmap`, `jstat`, `jcmd`?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

| Tool | Purpose |
|------|---------|
| `jstack <pid>` | Thread dump — all thread states and stack traces |
| `jmap -heap <pid>` | Heap summary — sizes, usage |
| `jmap -histo <pid>` | Object histogram — count by class |
| `jmap -dump:... <pid>` | Heap dump — full snapshot |
| `jstat -gcutil <pid> 1000` | GC statistics every 1 second |
| `jcmd <pid> help` | List all available commands |
| `jcmd <pid> VM.flags` | Show JVM flags |
| `jcmd <pid> GC.heap_info` | Heap info |
| `jcmd <pid> Thread.print` | Thread dump (like jstack) |
| `jcmd <pid> JFR.start ...` | Start JFR recording |

```bash
# Common diagnostic workflow
jps -l                              # find PID
jstat -gcutil <pid> 1000 30        # GC stats for 30 seconds
jstack <pid> > thread.txt          # thread dump
jmap -histo <pid> | head -30       # top 30 object types
jcmd <pid> VM.native_memory        # native memory tracking (if enabled)
```

---

## Summary — Key Takeaways for Interviews

| Topic | What interviewers test |
|-------|----------------------|
| Heap regions | Young Gen (Eden+Survivors) → Old Gen; Metaspace separate |
| GC algorithms | G1 is default; ZGC for ultra-low latency |
| PermGen vs Metaspace | Location, sizing, what changed in Java 8 |
| ClassLoader delegation | Parent-first model; why it matters for security |
| Memory leaks | Static collections, ThreadLocal, listeners, inner classes |
| OOM types | Heap, Metaspace, native thread — each has different cause |
| STW pauses | Why they happen, how to reduce (G1/ZGC) |
| Diagnostics | jstack (threads), jmap (heap), jstat (GC), JFR (profiling) |
