# Multithreading & Concurrency — Java Interview Guide (8–10 Years Experience)

## Quick Reference

```
Thread states:    NEW → RUNNABLE → BLOCKED/WAITING/TIMED_WAITING → TERMINATED
Key terms:        Race condition, Deadlock, Livelock, Starvation
Synchronization:  synchronized, volatile, Locks (ReentrantLock)
Executor:         ThreadPoolExecutor, FixedThreadPool, CachedThreadPool, ScheduledExecutor
Concurrent:       ConcurrentHashMap, CopyOnWriteArrayList, BlockingQueue, Atomic classes
Java 8+:          CompletableFuture, ForkJoinPool
```

---

## Q1. What are the ways to create a thread in Java?

**Difficulty:** Basic | **Type:** Theory

**Answer:**

```java
// 1. Extend Thread class
class MyThread extends Thread {
    @Override public void run() {
        System.out.println("Thread via extend: " + Thread.currentThread().getName());
    }
}
new MyThread().start();

// 2. Implement Runnable (preferred — avoids single inheritance limit)
Thread t = new Thread(() -> System.out.println("Thread via Runnable"));
t.start();

// 3. Implement Callable (returns result, can throw checked exception)
Callable<Integer> task = () -> {
    System.out.println("Callable running");
    return 42;
};
ExecutorService exec = Executors.newSingleThreadExecutor();
Future<Integer> future = exec.submit(task);
System.out.println("Result: " + future.get()); // 42
exec.shutdown();

// 4. CompletableFuture (Java 8+)
CompletableFuture.runAsync(() -> System.out.println("Async task"));
CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> 100);
```

**Prefer Runnable/Callable over extending Thread** — keeps your class free to extend other classes, and separates the task from thread management.

---

## Q2. What are the thread states in Java?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```
NEW → created but not started
RUNNABLE → running or ready to run (scheduled by OS)
BLOCKED → waiting to acquire a monitor lock (synchronized)
WAITING → waiting indefinitely (wait(), join(), LockSupport.park())
TIMED_WAITING → waiting with timeout (sleep(), wait(ms), join(ms))
TERMINATED → run() completed or exception thrown
```

```java
Thread t = new Thread(() -> {
    try {
        Thread.sleep(2000); // → TIMED_WAITING
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

System.out.println(t.getState()); // NEW
t.start();
Thread.sleep(100);
System.out.println(t.getState()); // TIMED_WAITING
t.join();
System.out.println(t.getState()); // TERMINATED

// BLOCKED example
Object lock = new Object();
Thread t1 = new Thread(() -> { synchronized(lock) { Thread.sleep(2000); } });
Thread t2 = new Thread(() -> { synchronized(lock) { System.out.println("t2 got lock"); } });
t1.start(); Thread.sleep(100);
t2.start(); Thread.sleep(100);
System.out.println(t2.getState()); // BLOCKED — waiting for lock held by t1
```

---

## Q3. What is the difference between `start()` and `run()`?

**Difficulty:** Basic | **Type:** Tricky

**Answer:**

| | `start()` | `run()` |
|-|-----------|---------|
| Creates new thread | Yes | No — runs in calling thread |
| Thread state | NEW → RUNNABLE | No state change |
| JVM invokes | `run()` in new thread | Direct method call |

```java
Thread t = new Thread(() -> {
    System.out.println("Running in: " + Thread.currentThread().getName());
});

t.run();   // Running in: main — NO new thread!
t.start(); // Running in: Thread-0 — new thread created

// Calling start() twice on same thread
// t.start(); // ❌ IllegalThreadStateException — thread already started
```

---

## Q4. What is `synchronized`? Explain object-level vs class-level locking.

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`synchronized` ensures only one thread executes a block/method at a time by acquiring a **monitor lock**.

```java
class Counter {
    private int count = 0;
    private static int total = 0;

    // Object-level lock — each Counter instance has its own lock
    synchronized void increment() { count++; }

    // Equivalent block form
    void decrement() {
        synchronized (this) { count--; }
    }

    // Class-level lock — shared across ALL instances
    static synchronized void incrementTotal() { total++; }

    // Equivalent block form
    static void decrementTotal() {
        synchronized (Counter.class) { total--; }
    }
}

// Object-level: two Counter objects can run simultaneously
Counter c1 = new Counter(), c2 = new Counter();
// c1.increment() and c2.increment() can run in parallel — different locks

// Class-level: only one thread runs incrementTotal() at a time across ALL instances
```

---

## Q5. What is `volatile`? When to use it?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`volatile` ensures:
1. **Visibility** — writes by one thread are immediately visible to all other threads (bypasses CPU cache, writes directly to main memory)
2. **No reordering** — prevents compiler/CPU instruction reordering around the volatile variable
3. **Does NOT provide atomicity** — `volatile int i; i++` is still NOT atomic (read-modify-write)

```java
class StopTask implements Runnable {
    private volatile boolean running = true; // without volatile, may loop forever

    public void run() {
        while (running) {
            // do work
        }
        System.out.println("Stopped");
    }

    public void stop() { running = false; }
}

// Without volatile: JIT may cache `running` in register, never reads from memory
// With volatile: every read goes to main memory — sees the updated value

// volatile is NOT enough for compound operations:
volatile int counter = 0;
counter++; // read-modify-write — NOT atomic! Use AtomicInteger instead.
```

**When to use `volatile`:**
- Simple flag (start/stop) read by one thread, written by another
- Singleton double-checked locking pattern
- When only visibility is needed, not atomicity

---

## Q6. What is the difference between `wait()`, `notify()`, and `notifyAll()`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

These are methods on `Object`, used for **inter-thread communication**. Must be called from inside a `synchronized` block.

| Method | What it does |
|--------|-------------|
| `wait()` | Releases lock and waits until notified |
| `wait(ms)` | Waits up to ms milliseconds |
| `notify()` | Wakes up ONE waiting thread (JVM chooses which) |
| `notifyAll()` | Wakes up ALL waiting threads (all re-compete for lock) |

```java
// Producer-Consumer using wait/notify
class SharedBuffer {
    private final Queue<Integer> buffer = new LinkedList<>();
    private final int capacity;

    SharedBuffer(int capacity) { this.capacity = capacity; }

    synchronized void produce(int item) throws InterruptedException {
        while (buffer.size() == capacity) {
            wait(); // buffer full — release lock and wait
        }
        buffer.offer(item);
        System.out.println("Produced: " + item);
        notifyAll(); // notify consumers
    }

    synchronized int consume() throws InterruptedException {
        while (buffer.isEmpty()) {
            wait(); // buffer empty — release lock and wait
        }
        int item = buffer.poll();
        System.out.println("Consumed: " + item);
        notifyAll(); // notify producers
        return item;
    }
}
```

**Why `while` not `if`?** Spurious wakeups — a thread can wake up without being notified. Always re-check condition in a loop.

---

## Q7. What is a deadlock? How to prevent it?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

**Deadlock** occurs when two or more threads are blocked forever, each waiting for a lock held by another.

```java
// Classic deadlock
Object lock1 = new Object();
Object lock2 = new Object();

Thread t1 = new Thread(() -> {
    synchronized (lock1) {
        System.out.println("T1 acquired lock1");
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        synchronized (lock2) { // waits for lock2 held by t2
            System.out.println("T1 acquired lock2");
        }
    }
});

Thread t2 = new Thread(() -> {
    synchronized (lock2) {
        System.out.println("T2 acquired lock2");
        synchronized (lock1) { // waits for lock1 held by t1
            System.out.println("T2 acquired lock1");
        }
    }
});

t1.start(); t2.start(); // DEADLOCK — both threads wait forever
```

**Prevention strategies:**
1. **Lock ordering** — always acquire locks in the same order
2. **Lock timeout** — use `tryLock(timeout)` from `ReentrantLock`
3. **Deadlock detection** — detect and break (JVM thread dump)
4. **Single lock** — minimize number of locks

```java
// Fix: consistent lock ordering
Thread t1Fixed = new Thread(() -> {
    synchronized (lock1) { synchronized (lock2) { /* work */ } }
});
Thread t2Fixed = new Thread(() -> {
    synchronized (lock1) { synchronized (lock2) { /* work */ } } // same order
});

// Fix: tryLock with timeout
ReentrantLock l1 = new ReentrantLock();
ReentrantLock l2 = new ReentrantLock();
boolean got1 = l1.tryLock(1, TimeUnit.SECONDS);
boolean got2 = l2.tryLock(1, TimeUnit.SECONDS);
if (got1 && got2) { /* work */ }
else { if (got1) l1.unlock(); if (got2) l2.unlock(); /* retry */ }
```

---

## Q8. What is `ReentrantLock`? How is it different from `synchronized`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

| Feature | `synchronized` | `ReentrantLock` |
|---------|---------------|-----------------|
| Fairness | No (JVM decides) | Optional (`new ReentrantLock(true)`) |
| Try locking | No | `tryLock()`, `tryLock(timeout)` |
| Interruptible | No | `lockInterruptibly()` |
| Multiple conditions | One (`wait`/`notify`) | Multiple `Condition` objects |
| Unlock in finally | Automatic | Manual — must in `finally` |

```java
ReentrantLock lock = new ReentrantLock(true); // fair lock — FIFO order
Condition notFull  = lock.newCondition();
Condition notEmpty = lock.newCondition();

// Safe locking pattern
lock.lock();
try {
    // critical section
} finally {
    lock.unlock(); // always unlock in finally
}

// Non-blocking try
if (lock.tryLock()) {
    try { /* critical section */ }
    finally { lock.unlock(); }
} else {
    System.out.println("Could not acquire lock");
}

// With timeout
if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
    try { /* work */ }
    finally { lock.unlock(); }
}

// Multiple conditions (better than notifyAll)
lock.lock();
try {
    while (bufferFull()) notFull.await();
    addToBuffer(item);
    notEmpty.signal(); // only wake consumers, not producers
} finally { lock.unlock(); }
```

---

## Q9. What is `AtomicInteger`? When to use atomic classes?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

Atomic classes (`AtomicInteger`, `AtomicLong`, `AtomicBoolean`, `AtomicReference`) provide thread-safe operations without `synchronized`, using **CAS (Compare-And-Swap)** hardware instructions.

```java
// Problem without atomic
int counter = 0;
// counter++ is NOT atomic: read → increment → write (3 steps, race condition)

// Solution with AtomicInteger
AtomicInteger atomicCounter = new AtomicInteger(0);

// Thread-safe operations
atomicCounter.incrementAndGet();      // ++i
atomicCounter.getAndIncrement();      // i++
atomicCounter.addAndGet(5);           // i += 5
atomicCounter.compareAndSet(10, 20);  // if (i == 10) i = 20; — CAS

// Real-world: thread-safe counter
ExecutorService pool = Executors.newFixedThreadPool(10);
AtomicInteger count = new AtomicInteger(0);
for (int i = 0; i < 1000; i++) {
    pool.submit(() -> count.incrementAndGet());
}
pool.shutdown(); pool.awaitTermination(5, TimeUnit.SECONDS);
System.out.println(count.get()); // always 1000 — no race condition

// AtomicReference for object references
AtomicReference<String> ref = new AtomicReference<>("initial");
ref.compareAndSet("initial", "updated"); // CAS on object reference
System.out.println(ref.get()); // updated
```

---

## Q10. What is `ExecutorService`? What thread pool types exist?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`ExecutorService` manages a pool of threads, reusing them for submitted tasks — avoids the overhead of creating a new thread per task.

```java
// Fixed thread pool — fixed number of threads
ExecutorService fixed = Executors.newFixedThreadPool(4);

// Cached thread pool — creates new threads as needed, reuses idle ones
ExecutorService cached = Executors.newCachedThreadPool();

// Single thread executor — one thread, tasks in FIFO order
ExecutorService single = Executors.newSingleThreadExecutor();

// Scheduled executor — for periodic/delayed tasks
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);
scheduled.schedule(() -> System.out.println("Once after 2s"), 2, TimeUnit.SECONDS);
scheduled.scheduleAtFixedRate(() -> System.out.println("Every 1s"), 0, 1, TimeUnit.SECONDS);
scheduled.scheduleWithFixedDelay(() -> System.out.println("1s after prev done"), 0, 1, TimeUnit.SECONDS);

// Submitting tasks
Future<Integer> future = fixed.submit(() -> {
    Thread.sleep(1000);
    return 42;
});

System.out.println("Waiting...");
Integer result = future.get(2, TimeUnit.SECONDS); // blocks up to 2 seconds
System.out.println("Result: " + result); // 42

// Shutdown
fixed.shutdown();                        // no new tasks; waits for running to complete
fixed.shutdownNow();                     // interrupts running tasks, returns pending
fixed.awaitTermination(5, TimeUnit.SECONDS); // wait for termination
```

---

## Q11. What is `ThreadPoolExecutor`? How to configure it?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`ThreadPoolExecutor` is the underlying implementation of all `Executors` factory pools.

```
ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, threadFactory, rejectionHandler)
```

**Task flow:**
1. If running threads < corePoolSize → create new thread
2. If running threads ≥ corePoolSize → add to queue
3. If queue full AND threads < maxPoolSize → create new thread
4. If queue full AND threads = maxPoolSize → **rejection policy**

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2,                                    // corePoolSize
    10,                                   // maximumPoolSize
    60L, TimeUnit.SECONDS,               // keepAliveTime for idle threads above core
    new ArrayBlockingQueue<>(100),        // bounded work queue
    new ThreadFactory() {
        int count = 0;
        public Thread newThread(Runnable r) {
            return new Thread(r, "worker-" + ++count);
        }
    },
    new ThreadPoolExecutor.CallerRunsPolicy() // rejection: run in caller thread
);

// Rejection policies:
// AbortPolicy (default) — throws RejectedExecutionException
// CallerRunsPolicy — runs in caller's thread (backpressure)
// DiscardPolicy — silently discards task
// DiscardOldestPolicy — discards oldest queued task, retries new one

executor.submit(() -> System.out.println("Task in " + Thread.currentThread().getName()));
executor.shutdown();
```

---

## Q12. What is `CompletableFuture`? How to chain async tasks?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

`CompletableFuture` (Java 8) enables **non-blocking async** programming with chaining, combining, and exception handling.

```java
// Basic async
CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
    // runs in ForkJoinPool.commonPool()
    return "Hello";
});

// thenApply — transform result (Function)
CompletableFuture<Integer> length = cf.thenApply(String::length);

// thenAccept — consume result (Consumer, returns CompletableFuture<Void>)
cf.thenAccept(s -> System.out.println("Got: " + s));

// thenRun — run after (Runnable, ignores result)
cf.thenRun(() -> System.out.println("Done"));

// thenCompose — flatMap (returns CompletableFuture)
CompletableFuture<String> chained = cf.thenCompose(
    s -> CompletableFuture.supplyAsync(() -> s + " World")
);

// thenCombine — combine two independent futures
CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> "World");
CompletableFuture<String> combined = cf1.thenCombine(cf2, (s1, s2) -> s1 + " " + s2);
System.out.println(combined.get()); // Hello World

// allOf — wait for all to complete
CompletableFuture<Void> all = CompletableFuture.allOf(cf1, cf2);
all.join();

// anyOf — complete when first finishes
CompletableFuture<Object> any = CompletableFuture.anyOf(cf1, cf2);

// Exception handling
CompletableFuture<String> withError = CompletableFuture
    .supplyAsync(() -> { throw new RuntimeException("Oops"); })
    .exceptionally(ex -> "Fallback: " + ex.getMessage())
    .thenApply(String::toUpperCase);
System.out.println(withError.get()); // FALLBACK: OOPS

// handle — always runs (like finally)
cf.handle((result, ex) -> {
    if (ex != null) return "Error: " + ex.getMessage();
    return "Success: " + result;
});
```

---

## Q13. What is the difference between `sleep()` and `wait()`?

**Difficulty:** Medium | **Type:** Tricky

**Answer:**

| | `Thread.sleep(ms)` | `Object.wait()` |
|-|-------------------|----------------|
| Class | `Thread` | `Object` |
| Releases lock | No — holds lock while sleeping | Yes — releases monitor lock |
| Where called | Anywhere | Inside `synchronized` block |
| Woken by | Timeout expiry or `interrupt()` | `notify()`, `notifyAll()`, or `interrupt()` |
| Checked exception | `InterruptedException` | `InterruptedException` |

```java
synchronized (lock) {
    Thread.sleep(1000); // holds lock for 1 second — other threads blocked!
    lock.wait(1000);    // releases lock for 1 second — other threads can proceed
}
```

---

## Q14. What is `ThreadLocal`? When to use it?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

`ThreadLocal<T>` provides a **separate copy of a variable for each thread** — no sharing, no synchronization needed.

```java
// Classic use: per-thread user context in web applications
public class UserContext {
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    public static void setUser(String user) { currentUser.set(user); }
    public static String getUser() { return currentUser.get(); }
    public static void clear() { currentUser.remove(); } // IMPORTANT — prevent memory leaks
}

// In a web filter:
UserContext.setUser("alice");
// downstream code can call UserContext.getUser() without passing it around
UserContext.clear(); // always in finally block

// ThreadLocal with initial value
ThreadLocal<SimpleDateFormat> sdf = ThreadLocal.withInitial(
    () -> new SimpleDateFormat("yyyy-MM-dd") // each thread gets its own SDF
);

// InheritableThreadLocal — child threads inherit parent's value
InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
itl.set("parent value");
new Thread(() -> System.out.println(itl.get())).start(); // parent value
```

**Memory leak warning:** In thread pools, threads are reused. Always call `remove()` after use to avoid stale values leaking to the next request on the same thread.

---

## Q15. What is the `ForkJoinPool`? How does work-stealing work?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`ForkJoinPool` is a special executor for **divide-and-conquer** parallelism. Each worker thread has its own deque. When a thread finishes its tasks, it **steals** tasks from the tail of another thread's deque.

```java
// Recursive task that computes sum of array in parallel
class SumTask extends RecursiveTask<Long> {
    private final int[] arr;
    private final int start, end;
    private static final int THRESHOLD = 1000;

    SumTask(int[] arr, int start, int end) {
        this.arr = arr; this.start = start; this.end = end;
    }

    @Override
    protected Long compute() {
        if (end - start <= THRESHOLD) {
            // base case — compute directly
            long sum = 0;
            for (int i = start; i < end; i++) sum += arr[i];
            return sum;
        }
        int mid = (start + end) / 2;
        SumTask left  = new SumTask(arr, start, mid);
        SumTask right = new SumTask(arr, mid, end);
        left.fork();                      // submit left to pool asynchronously
        long rightResult = right.compute(); // compute right in current thread
        long leftResult  = left.join();    // wait for left
        return leftResult + rightResult;
    }
}

ForkJoinPool pool = ForkJoinPool.commonPool();
int[] arr = IntStream.rangeClosed(1, 1_000_000).toArray();
long sum = pool.invoke(new SumTask(arr, 0, arr.length));
System.out.println(sum); // 500000500000
```

`parallelStream()` uses `ForkJoinPool.commonPool()` internally.

---

## Q16. What is `Semaphore`? Show a use case.

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

`Semaphore` controls access to a resource with a **limited number of permits**. `acquire()` gets a permit (blocks if none available), `release()` returns it.

```java
// Database connection pool — max 5 concurrent connections
Semaphore connectionPool = new Semaphore(5, true); // 5 permits, fair

class DatabaseService {
    Connection getConnection() throws InterruptedException {
        connectionPool.acquire(); // blocks if 5 already in use
        return createConnection();
    }

    void releaseConnection(Connection conn) {
        conn.close();
        connectionPool.release(); // return permit
    }
}

// Rate limiting — allow max 10 concurrent requests
Semaphore rateLimiter = new Semaphore(10);
ExecutorService pool = Executors.newFixedThreadPool(50);

for (int i = 0; i < 100; i++) {
    pool.submit(() -> {
        try {
            rateLimiter.acquire();
            processRequest(); // max 10 at a time
        } finally {
            rateLimiter.release();
        }
    });
}
```

---

## Q17. What is `CountDownLatch`? What is `CyclicBarrier`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

| | `CountDownLatch` | `CyclicBarrier` |
|-|-----------------|----------------|
| Direction | Countdown to 0 | Counts up to N |
| Reusable | No | Yes (resets after reaching barrier) |
| Use case | Wait for N events | Wait for N threads to reach same point |

```java
// CountDownLatch — main thread waits for N workers to finish
CountDownLatch latch = new CountDownLatch(3);

for (int i = 0; i < 3; i++) {
    int taskId = i;
    new Thread(() -> {
        System.out.println("Task " + taskId + " done");
        latch.countDown(); // decrement count
    }).start();
}

latch.await(); // blocks until count reaches 0
System.out.println("All tasks complete");

// CyclicBarrier — all threads wait at barrier before proceeding
CyclicBarrier barrier = new CyclicBarrier(3, () -> {
    System.out.println("All threads reached barrier — proceeding");
});

for (int i = 0; i < 3; i++) {
    int id = i;
    new Thread(() -> {
        System.out.println("Thread " + id + " doing work...");
        barrier.await(); // waits until all 3 arrive
        System.out.println("Thread " + id + " passed barrier");
    }).start();
}
// Barrier resets automatically — can be reused for next phase
```

---

## Q18. What is `ReadWriteLock`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`ReentrantReadWriteLock` allows **multiple concurrent readers** OR **one exclusive writer**, but not both simultaneously. Improves performance when reads >> writes.

```java
ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
Lock readLock  = rwLock.readLock();
Lock writeLock = rwLock.writeLock();

Map<String, String> cache = new HashMap<>();

String read(String key) {
    readLock.lock();
    try {
        return cache.get(key); // multiple threads can read simultaneously
    } finally {
        readLock.unlock();
    }
}

void write(String key, String value) {
    writeLock.lock();
    try {
        cache.put(key, value); // exclusive — no readers or writers allowed
    } finally {
        writeLock.unlock();
    }
}
```

---

## Q19. Scenario — How would you implement a thread-safe Singleton?

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
// Option 1: Eager initialization (simplest — class loading is thread-safe)
class EagerSingleton {
    private static final EagerSingleton INSTANCE = new EagerSingleton();
    private EagerSingleton() {}
    public static EagerSingleton getInstance() { return INSTANCE; }
}

// Option 2: Double-checked locking (lazy + thread-safe)
class DCLSingleton {
    private static volatile DCLSingleton instance; // volatile required!

    private DCLSingleton() {}

    public static DCLSingleton getInstance() {
        if (instance == null) {                    // first check — no lock
            synchronized (DCLSingleton.class) {
                if (instance == null) {             // second check — with lock
                    instance = new DCLSingleton();
                }
            }
        }
        return instance;
    }
}
// volatile prevents the JVM from reordering: allocate → assign → initialize
// Without volatile, another thread might see a partially constructed object

// Option 3: Initialization-on-demand holder (best — lazy + thread-safe + no sync overhead)
class HolderSingleton {
    private HolderSingleton() {}

    private static class Holder {
        static final HolderSingleton INSTANCE = new HolderSingleton();
    }

    public static HolderSingleton getInstance() { return Holder.INSTANCE; }
    // Holder class is only loaded when getInstance() is first called — JVM guarantees thread safety
}

// Option 4: Enum (Josh Bloch — reflection-proof, serialization-safe)
enum EnumSingleton {
    INSTANCE;
    public void doWork() { System.out.println("Working"); }
}
EnumSingleton.INSTANCE.doWork();
```

---

## Q20. What is a race condition? Give an example.

**Difficulty:** Medium | **Type:** Theory + Example

**Answer:**

A **race condition** occurs when the correctness of a program depends on the relative timing/ordering of threads, and the actual ordering produces incorrect results.

```java
class UnsafeCounter {
    int count = 0;

    void increment() {
        count++; // NOT atomic: read → increment → write
    }
}

// Test with 1000 threads each incrementing 1000 times
UnsafeCounter counter = new UnsafeCounter();
ExecutorService pool = Executors.newFixedThreadPool(10);
CountDownLatch latch = new CountDownLatch(1000);

for (int i = 0; i < 1000; i++) {
    pool.submit(() -> {
        counter.increment();
        latch.countDown();
    });
}
latch.await();
System.out.println(counter.count); // Expected 1000, actual: LESS (e.g., 987) — race condition!

// Fix options:
// 1. synchronized
synchronized void increment() { count++; }

// 2. AtomicInteger
AtomicInteger safeCount = new AtomicInteger(0);
safeCount.incrementAndGet();

// 3. LongAdder (better than AtomicInteger under high contention)
LongAdder adder = new LongAdder();
adder.increment();
System.out.println(adder.sum());
```

---

## Q21. What is the difference between `Runnable` and `Callable`?

**Difficulty:** Basic | **Type:** Theory

**Answer:**

| | `Runnable` | `Callable<V>` |
|-|-----------|--------------|
| Method | `void run()` | `V call() throws Exception` |
| Return value | None | Returns `V` |
| Checked exception | Cannot throw | Can throw checked exception |
| Used with | `Thread`, `ExecutorService.execute()` | `ExecutorService.submit()` |
| Returns | — | `Future<V>` |

```java
Runnable r = () -> System.out.println("Running");
Thread t = new Thread(r);
t.start();

Callable<Integer> c = () -> {
    Thread.sleep(1000);
    return 42;
};
ExecutorService exec = Executors.newSingleThreadExecutor();
Future<Integer> future = exec.submit(c);
System.out.println(future.get()); // 42 (blocks until done)
exec.shutdown();
```

---

## Q22. What is `Future`? What are its limitations?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`Future<V>` represents the result of an async computation.

```java
ExecutorService exec = Executors.newFixedThreadPool(2);
Future<String> f1 = exec.submit(() -> { Thread.sleep(1000); return "Result 1"; });
Future<String> f2 = exec.submit(() -> { Thread.sleep(500);  return "Result 2"; });

System.out.println(f1.get()); // blocks until f1 done
System.out.println(f2.get()); // f2 may already be done

// isDone() — non-blocking check
f1.isDone();    // true/false
f1.cancel(true); // attempt to cancel (may interrupt)
f1.isCancelled();
```

**Limitations of `Future`:**
- `get()` blocks the calling thread
- Cannot chain/compose futures
- No way to react when future completes (callbacks)
- No exception handling chain
- `CompletableFuture` fixes all of these

---

## Q23. Tricky — What is the output? (synchronized and visibility)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
class Shared {
    boolean flag = false;

    void writer() {
        flag = true;
    }

    void reader() {
        while (!flag) { /* spin */ }
        System.out.println("Flag is true!");
    }
}
```

**Answer:** This may **loop forever** on a multiprocessor machine.

**Why?** Without `volatile` or synchronization, the JIT compiler may cache `flag` in a register. The reader thread never sees the writer's update from main memory.

**Fix:**
```java
volatile boolean flag = false; // ensures visibility
```

---

## Q24. What is `LockSupport`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`LockSupport` provides basic thread-blocking primitives used by higher-level synchronizers (like `ReentrantLock`).

```java
// park — block current thread (like wait, but no lock required)
Thread t = new Thread(() -> {
    System.out.println("Before park");
    LockSupport.park(); // blocks
    System.out.println("After unpark");
});

t.start();
Thread.sleep(500);
LockSupport.unpark(t); // unblock the thread — t resumes

// park with deadline
LockSupport.parkNanos(t, TimeUnit.SECONDS.toNanos(5));
LockSupport.parkUntil(t, System.currentTimeMillis() + 5000);
```

---

## Q25. What is `StampedLock`? (Java 8+)

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`StampedLock` is an improved read-write lock that adds **optimistic reading** — read without acquiring a lock, then validate.

```java
StampedLock lock = new StampedLock();
double x = 0, y = 0;

// Write
long stamp = lock.writeLock();
try { x = 1.0; y = 2.0; } finally { lock.unlockWrite(stamp); }

// Optimistic read (fastest path — no lock acquisition)
long stamp2 = lock.tryOptimisticRead();
double curX = x, curY = y;
if (!lock.validate(stamp2)) { // check if write happened during read
    // fallback to proper read lock
    stamp2 = lock.readLock();
    try { curX = x; curY = y; } finally { lock.unlockRead(stamp2); }
}
System.out.println(curX + ", " + curY);
```

---

## Q26. What is `Exchanger`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`Exchanger<V>` is a synchronization point where two threads swap objects. Both threads must call `exchange()` — the first to arrive blocks until the second arrives.

```java
Exchanger<List<Integer>> exchanger = new Exchanger<>();

// Producer fills buffer, Consumer drains it — swap when ready
Thread producer = new Thread(() -> {
    List<Integer> buffer = new ArrayList<>();
    for (int i = 0; i < 5; i++) buffer.add(i);
    try {
        List<Integer> empty = exchanger.exchange(buffer); // swap with consumer
        // now has empty buffer to refill
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});

Thread consumer = new Thread(() -> {
    List<Integer> buffer = new ArrayList<>();
    try {
        List<Integer> full = exchanger.exchange(buffer); // swap with producer
        System.out.println("Received: " + full); // [0, 1, 2, 3, 4]
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});

producer.start(); consumer.start();
```

---

## Q27. What is `Phaser`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`Phaser` is a flexible barrier supporting multiple **phases** of synchronization. More powerful than `CountDownLatch` or `CyclicBarrier`.

```java
int parties = 3;
Phaser phaser = new Phaser(parties);

for (int i = 0; i < parties; i++) {
    int id = i;
    new Thread(() -> {
        System.out.println("Thread " + id + " phase 1 done");
        phaser.arriveAndAwaitAdvance(); // barrier for phase 1

        System.out.println("Thread " + id + " phase 2 done");
        phaser.arriveAndAwaitAdvance(); // barrier for phase 2

        System.out.println("Thread " + id + " deregistering");
        phaser.arriveAndDeregister(); // leave the phaser
    }).start();
}
```

---

## Q28. Scenario — Implement a thread-safe bounded blocking queue from scratch.

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
class BoundedBlockingQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull  = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    BoundedBlockingQueue(int capacity) { this.capacity = capacity; }

    void put(T item) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) notFull.await();
            queue.offer(item);
            notEmpty.signal();
        } finally { lock.unlock(); }
    }

    T take() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) notEmpty.await();
            T item = queue.poll();
            notFull.signal();
            return item;
        } finally { lock.unlock(); }
    }

    int size() {
        lock.lock();
        try { return queue.size(); }
        finally { lock.unlock(); }
    }
}
```

---

## Q29. What is happens-before guarantee?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

The Java Memory Model (JMM) defines **happens-before** relationships that guarantee visibility:

1. **Program order** — each action in a thread happens-before every subsequent action in that thread
2. **Monitor lock** — unlock of a monitor happens-before every subsequent lock of that monitor
3. **volatile** — write to volatile field happens-before every subsequent read of that field
4. **Thread start** — `thread.start()` happens-before any action in the started thread
5. **Thread join** — all actions in a thread happen-before `thread.join()` returns
6. **Static initializer** — class initialization happens-before any thread uses the class

```java
int x = 0;
volatile boolean ready = false;

// Thread 1
x = 42;
ready = true; // volatile write — happens-before Thread 2's read

// Thread 2
if (ready) {
    System.out.println(x); // guaranteed to see 42 — happens-before chain
}
```

---

## Q30. What is `ConcurrentLinkedQueue` vs `LinkedBlockingQueue`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

| | `ConcurrentLinkedQueue` | `LinkedBlockingQueue` |
|-|------------------------|-----------------------|
| Blocking | No | Yes (`put`, `take` block) |
| Algorithm | Lock-free CAS | `ReentrantLock` per head/tail |
| Capacity | Unbounded | Optional bound |
| Null | Not allowed | Not allowed |
| Use case | Non-blocking concurrent access | Producer-Consumer with blocking |

```java
// ConcurrentLinkedQueue — high throughput, non-blocking
ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<>();
clq.offer("task1");
String t = clq.poll(); // returns null if empty — non-blocking

// LinkedBlockingQueue — blocking, great for Producer-Consumer
LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>(100);
lbq.put("task1");    // blocks if full
String task = lbq.take(); // blocks if empty
```

---

## Q31. Tricky — What happens when you call `interrupt()` on a sleeping thread?

**Difficulty:** Tricky | **Type:** Tricky

**Answer:**

```java
Thread t = new Thread(() -> {
    try {
        System.out.println("Going to sleep");
        Thread.sleep(10000);
        System.out.println("Sleep complete"); // this line is NOT reached
    } catch (InterruptedException e) {
        System.out.println("Interrupted! Status: " + Thread.currentThread().isInterrupted());
        // isInterrupted() returns false here — catching InterruptedException clears the flag
        Thread.currentThread().interrupt(); // re-set the flag — best practice
        System.out.println("After re-interrupt: " + Thread.currentThread().isInterrupted()); // true
    }
});

t.start();
Thread.sleep(500);
t.interrupt(); // throws InterruptedException in sleeping thread
```

**Output:**
```
Going to sleep
Interrupted! Status: false
After re-interrupt: true
```

**Best practice:** Always re-interrupt after catching `InterruptedException` so callers can detect the interruption.

---

## Q32. What is `LongAdder` vs `AtomicLong`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`LongAdder` (Java 8) is better than `AtomicLong` under high contention:
- `AtomicLong` — single variable, CAS loops spin under contention
- `LongAdder` — maintains multiple cells, threads update different cells, sum when needed

```java
// AtomicLong — good for single thread or low contention
AtomicLong atomicLong = new AtomicLong(0);
atomicLong.incrementAndGet(); // fast when few threads

// LongAdder — better for high-contention counters (web request counts, metrics)
LongAdder adder = new LongAdder();
adder.increment();
adder.add(5);
System.out.println(adder.sum()); // get total
adder.reset();                    // reset to 0
long sumAndReset = adder.sumThenReset();

// Benchmark: 10 threads, 1M increments each
// AtomicLong: ~2000ms (heavy CAS contention)
// LongAdder:  ~400ms  (distributed cells, less contention)
```

---

## Summary — Key Takeaways for Interviews

| Topic | What interviewers test |
|-------|----------------------|
| Deadlock | Can you identify it in code and prevent it |
| `synchronized` vs `Lock` | Trade-offs, when each is appropriate |
| `volatile` vs `AtomicInteger` | Visibility vs atomicity — different problems |
| `wait`/`notify` | Must be in synchronized, while not if loop |
| `CompletableFuture` | Chaining — thenApply vs thenCompose vs thenCombine |
| Thread pool | Types and when to use each; shutdown lifecycle |
| ThreadLocal | Memory leak in thread pool — must call remove() |
| Singleton | Double-checked locking — why volatile is needed |
| Race condition | Always verify with concurrent test, not just logic |
