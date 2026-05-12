# Exception Handling — Java Interview Guide (8–10 Years Experience)

## Quick Reference

```
Hierarchy:  Throwable → Error (JVM) | Exception → RuntimeException (unchecked)
Checked:    Must handle (IOException, SQLException, ClassNotFoundException)
Unchecked:  Optional to handle (NullPointerException, ArrayIndexOutOfBoundsException)
Keywords:   try, catch, finally, throw, throws
Java 7+:    try-with-resources (AutoCloseable), multi-catch (|)
Java 9+:    try-with-resources with effectively-final variable
```

---

## Q1. What is the Exception hierarchy in Java?

**Difficulty:** Basic | **Type:** Theory

**Answer:**

```
java.lang.Throwable
├── java.lang.Error                     (serious JVM problems — don't catch)
│   ├── OutOfMemoryError
│   ├── StackOverflowError
│   ├── VirtualMachineError
│   └── AssertionError
│
└── java.lang.Exception                 (recoverable conditions)
    ├── Checked Exceptions              (must handle)
    │   ├── IOException
    │   │   ├── FileNotFoundException
    │   │   └── SocketException
    │   ├── SQLException
    │   ├── ClassNotFoundException
    │   ├── CloneNotSupportedException
    │   ├── InterruptedException
    │   └── ParseException
    │
    └── RuntimeException (Unchecked)    (programming bugs — optional to handle)
        ├── NullPointerException
        ├── ArrayIndexOutOfBoundsException
        ├── ClassCastException
        ├── NumberFormatException
        ├── IllegalArgumentException
        ├── IllegalStateException
        ├── ArithmeticException
        ├── UnsupportedOperationException
        └── ConcurrentModificationException
```

---

## Q2. What is the difference between checked and unchecked exceptions?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| | Checked | Unchecked (RuntimeException) |
|-|---------|------------------------------|
| Checked by | Compiler at compile time | Only discovered at runtime |
| Must handle | Yes — or declare with `throws` | No (optional) |
| Extends | `Exception` (not RuntimeException) | `RuntimeException` |
| Cause | External/recoverable (file missing, DB down) | Programming errors (null access, bad cast) |
| Examples | `IOException`, `SQLException` | `NullPointerException`, `IllegalArgumentException` |

```java
// Checked — must declare or handle
void readFile(String path) throws IOException { // declares
    new FileReader(path);
}

// OR
void readFile2(String path) {
    try {
        new FileReader(path);
    } catch (IOException e) {
        System.out.println("File not found");
    }
}

// Unchecked — no declaration required
void divide(int a, int b) {
    System.out.println(a / b); // ArithmeticException if b == 0 — no throws needed
}
```

---

## Q3. What is `try-with-resources`? How is it different from `finally`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`try-with-resources` (Java 7+) automatically closes resources that implement `AutoCloseable`. The resource is closed even if an exception occurs.

```java
// Old way — error-prone
FileReader fr = null;
try {
    fr = new FileReader("file.txt");
    // use fr
} catch (IOException e) {
    // handle
} finally {
    if (fr != null) {
        try { fr.close(); } catch (IOException e) { /* ignore */ }
    }
}

// Modern — try-with-resources
try (FileReader fr = new FileReader("file.txt");
     BufferedReader br = new BufferedReader(fr)) { // multiple resources
    String line;
    while ((line = br.readLine()) != null) {
        System.out.println(line);
    }
} catch (IOException e) {
    System.out.println("Error: " + e.getMessage());
}
// fr and br are closed automatically, in REVERSE ORDER of declaration
// br.close() called first, then fr.close()
// close() is called even if exception occurs

// Java 9+: effectively final variable
BufferedReader br = new BufferedReader(new FileReader("file.txt"));
try (br) { // just reference the existing variable
    // use br
}
```

**Order of operations in try-with-resources:**
1. `try` block executes
2. Resource's `close()` called (even on exception)
3. `catch` block (if exception)
4. `finally` block (if any)

---

## Q4. What are suppressed exceptions?

**Difficulty:** Senior | **Type:** Theory + Tricky

**Answer:**

In try-with-resources, if both the `try` block and `close()` throw exceptions, the `close()` exception is **suppressed** (attached to the main exception), not lost.

```java
class BrokenResource implements AutoCloseable {
    @Override
    public void close() throws Exception {
        throw new Exception("Close failed!");
    }
}

try (BrokenResource r = new BrokenResource()) {
    throw new RuntimeException("Try block failed!");
} catch (RuntimeException e) {
    System.out.println("Main: " + e.getMessage());   // Try block failed!
    Throwable[] suppressed = e.getSuppressed();
    System.out.println("Suppressed: " + suppressed[0].getMessage()); // Close failed!
}

// Manually add suppressed exception
Exception main = new Exception("main");
Exception secondary = new Exception("secondary");
main.addSuppressed(secondary);
throw main; // main has secondary attached
```

**Before try-with-resources (old `finally`):** If `finally` throws, the original exception was **lost** — the `finally` exception silently replaced it. This was a major bug source.

---

## Q5. What is the difference between `throw` and `throws`?

**Difficulty:** Basic | **Type:** Theory

**Answer:**

| | `throw` | `throws` |
|-|---------|---------|
| Type | Statement | Keyword in method signature |
| Purpose | Actually throws an exception object | Declares exceptions a method might throw |
| Followed by | Exception instance | Exception class name(s) |
| Location | Method body | Method signature |

```java
// throws — declaration
public void validate(int age) throws IllegalArgumentException, IOException {
    if (age < 0) {
        throw new IllegalArgumentException("Age cannot be negative: " + age); // throw — actual
    }
    if (age > 150) {
        throw new IllegalArgumentException("Invalid age: " + age);
    }
}

// throw for rethrowing
try {
    riskyOperation();
} catch (IOException e) {
    log(e);
    throw e; // rethrow original
    // OR wrap: throw new ServiceException("Operation failed", e);
}
```

---

## Q6. What is exception chaining? Why is it important?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

Exception chaining (wrapping) preserves the original cause when converting between exception types. The `cause` chain shows the full error origin.

```java
// Without chaining — loses root cause
try {
    connectToDatabase();
} catch (SQLException e) {
    throw new ServiceException("DB error"); // ❌ root cause lost!
}

// With chaining — preserves cause
try {
    connectToDatabase();
} catch (SQLException e) {
    throw new ServiceException("DB error", e); // ✅ SQL exception preserved as cause
}

// Accessing the cause chain
try {
    service.doWork();
} catch (ServiceException e) {
    System.out.println("Service: " + e.getMessage());
    Throwable cause = e.getCause();
    while (cause != null) {
        System.out.println("Caused by: " + cause.getMessage());
        cause = cause.getCause();
    }
}

// Custom exception with chaining
class PaymentException extends RuntimeException {
    PaymentException(String message) { super(message); }
    PaymentException(String message, Throwable cause) { super(message, cause); }
}
```

---

## Q7. How to create a custom exception?

**Difficulty:** Medium | **Type:** Theory + Scenario

**Answer:**

```java
// Custom checked exception
class InsufficientFundsException extends Exception {
    private final double deficit;

    InsufficientFundsException(double deficit) {
        super("Insufficient funds. Deficit: " + deficit);
        this.deficit = deficit;
    }

    InsufficientFundsException(double deficit, Throwable cause) {
        super("Insufficient funds. Deficit: " + deficit, cause);
        this.deficit = deficit;
    }

    double getDeficit() { return deficit; }
}

// Custom unchecked exception
class ValidationException extends RuntimeException {
    private final String field;

    ValidationException(String field, String message) {
        super(field + ": " + message);
        this.field = field;
    }

    String getField() { return field; }
}

// Usage
void withdraw(double amount) throws InsufficientFundsException {
    if (amount > balance) {
        throw new InsufficientFundsException(amount - balance);
    }
    balance -= amount;
}

try {
    account.withdraw(5000);
} catch (InsufficientFundsException e) {
    System.out.println("Cannot withdraw: " + e.getDeficit());
}

// Best practices for custom exceptions:
// 1. Checked if caller can reasonably recover (InsufficientFundsException)
// 2. Unchecked if it's a programming error or unrecoverable (ValidationException)
// 3. Always provide both message-only and message+cause constructors
// 4. Add domain-specific fields (deficit, field, errorCode)
// 5. End name in Exception: FooException, not FooError (unless it extends Error)
```

---

## Q8. What is multi-catch (`|`)? Java 7+

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Multi-catch allows catching multiple exception types in a single catch block, avoiding code duplication.

```java
// Before Java 7 — repetitive
try {
    riskyOperation();
} catch (IOException e) {
    log(e); notify(e);
} catch (SQLException e) {
    log(e); notify(e); // same code repeated
}

// Java 7+ multi-catch
try {
    riskyOperation();
} catch (IOException | SQLException e) {
    log(e); notify(e); // single handler
}

// TRICKY: multi-catch variable is implicitly final
try {
    throw new IOException("test");
} catch (IOException | RuntimeException e) {
    // e = new IOException(); // ❌ compile error — e is effectively final in multi-catch
    throw e; // ✅ re-throw is fine
}

// Cannot catch related exceptions in multi-catch
// catch (IOException | FileNotFoundException e) // ❌ FileNotFoundException IS-A IOException
```

---

## Q9. What happens with `finally` and `return`?

**Difficulty:** Tricky | **Type:** Output Prediction (most asked)

```java
int test1() {
    try {
        return 1;
    } finally {
        System.out.println("finally runs"); // ?
    }
}

int test2() {
    try {
        return 1;
    } finally {
        return 2; // return in finally
    }
}

int test3() {
    try {
        throw new RuntimeException();
    } catch (RuntimeException e) {
        return 1;
    } finally {
        return 2;
    }
}
```

**Output:**
```java
test1() → prints "finally runs", returns 1
          // finally always runs, but return value (1) is returned AFTER finally

test2() → returns 2
          // return in finally overrides return in try — dangerous!

test3() → returns 2
          // return in finally suppresses the exception AND overrides catch return
```

**Rule:** Never use `return` in `finally` — it swallows exceptions and overrides return values silently.

---

## Q10. Can `finally` be skipped?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`finally` is skipped in these cases:
1. `System.exit()` called
2. JVM crash (hardware failure, kill -9)
3. Thread is killed (rare, not recommended)
4. Infinite loop in `try` block

```java
try {
    System.exit(0); // JVM terminates — finally SKIPPED
} finally {
    System.out.println("This never prints");
}

try {
    while (true) { } // infinite loop — finally NEVER reached
} finally {
    System.out.println("Never");
}

// finally runs in all normal cases:
try { return; }      finally { System.out.println("runs"); } // ✅
try { throw e; }     finally { System.out.println("runs"); } // ✅
try { /* normal */}  finally { System.out.println("runs"); } // ✅
```

---

## Q11. What is the output? (Exception in catch and finally)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
void method() {
    try {
        System.out.println("try");
        throw new RuntimeException("from try");
    } catch (RuntimeException e) {
        System.out.println("catch: " + e.getMessage());
        throw new RuntimeException("from catch");
    } finally {
        System.out.println("finally");
        // Note: NO return here and NO throw here
    }
}

// What if finally also throws?
void method2() {
    try {
        throw new RuntimeException("from try");
    } finally {
        throw new RuntimeException("from finally"); // original exception LOST!
    }
}
```

**Output (method):**
```
try
catch: from try
finally
RuntimeException: from catch  ← thrown to caller
```

**Output (method2):**
```
RuntimeException: from finally  ← "from try" is completely lost!
```

---

## Q12. What is `Exception.printStackTrace()`? Is it production-safe?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`printStackTrace()` prints the exception type, message, and full stack trace to `System.err`.

```java
try {
    int result = 10 / 0;
} catch (ArithmeticException e) {
    e.printStackTrace();
    // java.lang.ArithmeticException: / by zero
    //     at com.example.Calc.divide(Calc.java:10)
    //     at com.example.Main.main(Main.java:5)
}

// Not production-safe because:
// 1. Writes to System.err, not your logging system
// 2. Interleaved output from multiple threads (not atomic)
// 3. No correlation ID, request context, timestamps

// Production approach: use SLF4J/Logback/Log4j
private static final Logger log = LoggerFactory.getLogger(MyClass.class);
try {
    // ...
} catch (Exception e) {
    log.error("Operation failed for user {}", userId, e); // structured, with context
}
```

---

## Q13. What is exception propagation?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

Uncaught exceptions propagate up the call stack until caught or reach the top (thread dies).

```java
void methodC() throws IOException {
    throw new IOException("IO Error in C");
}

void methodB() throws IOException {
    methodC(); // propagates up
}

void methodA() {
    try {
        methodB();
    } catch (IOException e) {
        System.out.println("Caught in A: " + e.getMessage());
    }
}

// Call stack when exception thrown:
// methodC → throws IOException
// methodB → not caught, propagates
// methodA → catches IOException

// Unchecked propagation (no throws needed)
void methodX() {
    throw new NullPointerException("NPE"); // no declaration
}

void methodY() {
    methodX(); // NPE propagates without declaration
}

void methodZ() {
    try {
        methodY();
    } catch (NullPointerException e) {
        System.out.println("Caught NPE");
    }
}
```

---

## Q14. What is the difference between `Exception` and `Error`? Should you catch `Error`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

- **`Error`** — serious JVM problems that cannot be recovered from (OOM, StackOverflow, VirtualMachineError)
- **`Exception`** — conditions that programs can handle

**Should you catch `Error`?** Generally **no** — but there are narrow exceptions:

```java
// Generally should NOT catch
try {
    doWork();
} catch (OutOfMemoryError e) {
    // Catching OOM is almost always wrong — JVM is in undefined state
    // Can't even create new objects to log!
}

// Legitimate cases
// 1. Test frameworks catching all throwables
try {
    runTest();
} catch (AssertionError e) {
    // test assertion failed — legitimate to catch
}

// 2. Top-level shutdown handlers
try {
    runApplication();
} catch (Throwable t) {
    // last resort — log and exit cleanly
    System.err.println("Fatal: " + t);
    System.exit(1);
}

// 3. Server containers — must not crash for one request's OOM
// (Spring, Tomcat handle this in request processing loops)
```

---

## Q15. What is `RuntimeException`? Why was it made unchecked?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`RuntimeException` (and subclasses) are unchecked because they represent **programming errors** that should be fixed by the developer, not handled at runtime:

- `NullPointerException` — fix the null check in code
- `ArrayIndexOutOfBoundsException` — fix the index logic
- `ClassCastException` — fix the type check
- `NumberFormatException` — validate input before parsing

Making these checked would require `try-catch` around every array access and null check — making code unreadable.

```java
// If ArrayIndexOutOfBoundsException were checked:
try {
    arr[i] = value; // throws ArrayIndexOutOfBoundsException (checked — hypothetical)
} catch (ArrayIndexOutOfBoundsException e) { /* everywhere! */ }

// Legitimate use of unchecked exceptions for API design
void setAge(int age) {
    if (age < 0 || age > 150) {
        throw new IllegalArgumentException("Invalid age: " + age); // unchecked — caller's bug
    }
    this.age = age;
}
```

---

## Q16. Tricky — What is the output? (catch ordering)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
// Case 1: catch more specific first
try {
    throw new FileNotFoundException("file not found");
} catch (FileNotFoundException e) {
    System.out.println("FileNotFoundException");
} catch (IOException e) {
    System.out.println("IOException");
}

// Case 2: catch less specific first — compile error?
try {
    throw new FileNotFoundException("file");
} catch (IOException e) {        // catches it first (superset)
    System.out.println("IOException");
} catch (FileNotFoundException e) { // ❌ COMPILE ERROR
    System.out.println("FileNotFoundException");
}
```

**Output:**
```
FileNotFoundException   ← caught by most specific first

Case 2: COMPILATION ERROR — "FileNotFoundException has already been caught"
Unreachable catch block for FileNotFoundException — it is already caught by IOException
```

**Rule:** Always order catch blocks from most specific to least specific (child before parent).

---

## Q17. What is a `NullPointerException`? How to avoid it?

**Difficulty:** Medium | **Type:** Theory + Scenario

**Answer:**

NPE occurs when you call a method or access a field on a `null` reference.

```java
// Common NPE causes
String s = null;
s.length();          // NPE
s.toUpperCase();     // NPE
s.equals("hello");   // NPE — use "hello".equals(s) or Objects.equals

int[] arr = null;
arr[0] = 5;          // NPE
arr.length;          // NPE

List<String> list = null;
list.size();         // NPE
for (String item : list) {} // NPE

// Java 14+ Helpful NPEs — better messages
// "Cannot invoke 'String.length()' because 's' is null"
// Use JVM flag: -XX:+ShowCodeDetailsInExceptionMessages (default in Java 14+)

// Prevention strategies
// 1. Optional (Java 8+)
Optional.ofNullable(name).map(String::toUpperCase).orElse("UNKNOWN");

// 2. Objects.requireNonNull (fail-fast)
this.name = Objects.requireNonNull(name, "name must not be null");

// 3. Null-safe comparison
Objects.equals(s, "hello"); // null-safe — returns false if s is null

// 4. Defensive check
if (name != null && name.length() > 0) { /* safe */ }

// 5. Use @NotNull annotations
void process(@NotNull String input) { } // documented contract

// 6. Return empty instead of null
List<String> getItems() {
    return items != null ? items : Collections.emptyList(); // never return null collections
}
```

---

## Q18. What is `try-with-resources` with multiple resources? Close order?

**Difficulty:** Medium | **Type:** Theory + Tricky

**Answer:**

Resources are **closed in reverse order** of declaration, guaranteeing that outer resources are closed after inner ones.

```java
try (
    Connection conn = getConnection();         // opened first
    PreparedStatement stmt = conn.prepareStatement(sql); // opened second
    ResultSet rs = stmt.executeQuery()        // opened third
) {
    // process rs
} // rs.close() first, then stmt.close(), then conn.close()
// Even if one close() throws, others still called

// Custom AutoCloseable
class Transaction implements AutoCloseable {
    final String name;

    Transaction(String name) {
        System.out.println("Opening: " + name);
        this.name = name;
    }

    @Override
    public void close() {
        System.out.println("Closing: " + name);
    }
}

try (Transaction t1 = new Transaction("Outer");
     Transaction t2 = new Transaction("Inner")) {
    System.out.println("In try block");
}
// Opening: Outer
// Opening: Inner
// In try block
// Closing: Inner   ← inner closed first
// Closing: Outer   ← outer closed last
```

---

## Q19. Scenario — Design exception strategy for a REST API.

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
// Layer-specific exceptions
class RepositoryException extends RuntimeException {
    RepositoryException(String msg, Throwable cause) { super(msg, cause); }
}

class ServiceException extends RuntimeException {
    private final String errorCode;
    ServiceException(String errorCode, String msg) { super(msg); this.errorCode = errorCode; }
    ServiceException(String errorCode, String msg, Throwable cause) { super(msg, cause); this.errorCode = errorCode; }
    String getErrorCode() { return errorCode; }
}

// Domain-specific exceptions
class UserNotFoundException extends ServiceException {
    UserNotFoundException(long userId) {
        super("USER_NOT_FOUND", "User not found: " + userId);
    }
}

class DuplicateEmailException extends ServiceException {
    DuplicateEmailException(String email) {
        super("DUPLICATE_EMAIL", "Email already exists: " + email);
    }
}

// Repository layer — wraps low-level exceptions
class UserRepository {
    User findById(long id) {
        try {
            return db.query("SELECT * FROM users WHERE id = ?", id);
        } catch (SQLException e) {
            throw new RepositoryException("DB query failed for user " + id, e);
        }
    }
}

// Service layer — translates to domain exceptions
class UserService {
    User getUser(long id) {
        User user = repo.findById(id); // may throw RepositoryException
        if (user == null) throw new UserNotFoundException(id);
        return user;
    }
}

// Global exception handler (Spring)
@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse handleNotFound(UserNotFoundException e) {
        return new ErrorResponse(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleService(ServiceException e) {
        return new ErrorResponse(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponse handleGeneral(Exception e) {
        log.error("Unexpected error", e);
        return new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
    }
}
```

---

## Q20. What is the difference between `assert` and `throw`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
// assert — disabled in production (must enable with -ea flag)
assert age > 0 : "Age must be positive"; // AssertionError if fails, with message
assert list != null;                     // no message

// throw — always active
if (age <= 0) throw new IllegalArgumentException("Age must be positive: " + age);

// assert is for:
// - Development/testing invariants
// - Preconditions that should NEVER fail if code is correct
// - Not for user input validation!

// throw is for:
// - Input validation (always active)
// - Error conditions that can happen in production
// - API contracts

// WRONG:
assert userInput.matches("\\d+") : "Input must be digits"; // disabled in prod!

// RIGHT:
if (!userInput.matches("\\d+")) throw new IllegalArgumentException("Input must be digits");
```

---

## Q21. What is `ExceptionInInitializerError`?

**Difficulty:** Medium | **Type:** Tricky

**Answer:**

Thrown when a static initializer block or static field initializer throws an exception.

```java
class BadInit {
    static int value = Integer.parseInt("not-a-number"); // NumberFormatException
    // → wrapped in ExceptionInInitializerError

    static {
        if (true) throw new RuntimeException("init failed");
        // → ExceptionInInitializerError
    }
}

try {
    Class.forName("BadInit"); // ExceptionInInitializerError
} catch (ExceptionInInitializerError e) {
    System.out.println("Init failed: " + e.getCause()); // NumberFormatException
}

// After class fails to init:
try {
    new BadInit(); // NoClassDefFoundError (not ExceptionInInitializerError again)
} catch (NoClassDefFoundError e) {
    System.out.println("Class broken: " + e.getMessage());
}
```

---

## Q22. What is `NoSuchMethodException` vs `NoSuchMethodError`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| | `NoSuchMethodException` | `NoSuchMethodError` |
|-|------------------------|---------------------|
| Type | Checked Exception | Error |
| Thrown by | Reflection API (`Method m = cls.getMethod(...)`) | JVM at runtime (class A calls method in class B that no longer exists) |
| Cause | Method doesn't exist on the class | Compiled against version with method, but deployed without |

```java
// NoSuchMethodException
try {
    Method m = String.class.getMethod("nonExistentMethod");
} catch (NoSuchMethodException e) {
    System.out.println("Reflection: " + e.getMessage());
}

// NoSuchMethodError
// Class A compiled against version 1 of Class B (has methodX())
// At runtime, Class B is version 2 (methodX() removed)
// → NoSuchMethodError (often from jar version mismatch)
```

---

## Q23. What is `ClassCastException`? How to prevent it?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
Object obj = "Hello";
Integer i = (Integer) obj; // ClassCastException: String cannot be cast to Integer

// Prevention: instanceof check
if (obj instanceof Integer) {
    Integer i2 = (Integer) obj;
}

// Java 16+ pattern matching — safer
if (obj instanceof Integer i2) {
    System.out.println(i2 + 1);
}

// With generics — compiler prevents ClassCastException at compile time
List<String> strings = new ArrayList<>();
strings.add("hello");
String s = strings.get(0); // no cast needed, no risk

// Heap pollution (raw types) can cause ClassCastException
@SuppressWarnings("unchecked")
List<String> rawList = new ArrayList<>();
List<Integer> intList = (List<Integer>) (List<?>) rawList; // compiles!
rawList.add("hello");
Integer val = intList.get(0); // ClassCastException at runtime — heap pollution
```

---

## Q24. Tricky — Exception in static initializer and class loading.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
class Config {
    static final String VALUE;
    static {
        System.out.println("Loading Config");
        if (System.getProperty("config.file") == null) {
            throw new RuntimeException("config.file required");
        }
        VALUE = System.getProperty("config.file");
    }
}

// Without -Dconfig.file=...
try {
    System.out.println(Config.VALUE); // triggers class loading
} catch (ExceptionInInitializerError e) {
    System.out.println("First: " + e.getCause().getMessage());
}

try {
    new Config(); // try again
} catch (NoClassDefFoundError e) {
    System.out.println("Second: Class is now broken");
}
```

**Output:**
```
Loading Config
First: config.file required
Second: Class is now broken
```

Once a class fails to initialize, subsequent attempts throw `NoClassDefFoundError` (not `ExceptionInInitializerError` again).

---

## Q25. What is exception handling best practices for service layers?

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
// 1. Log ONCE — at the boundary where you handle (not rethrow)
// BAD: log + rethrow + log again
catch (Exception e) {
    log.error("Error in service", e);  // ❌ log here
    throw new ServiceException("msg", e); // AND rethrow — will be logged again above
}

// GOOD: log only when you handle, not when you rethrow
catch (Exception e) {
    throw new ServiceException("msg", e); // rethrow — let controller log it once
}

// 2. Include context in exception messages
throw new ServiceException(
    "Failed to process payment for order " + orderId + " user " + userId, cause);

// 3. Use specific exceptions for specific failures
throw new UserNotFoundException(userId);    // vs generic ServiceException
throw new InsufficientFundsException(deficit); // vs generic RuntimeException

// 4. Don't swallow exceptions
catch (Exception e) {
    // NOTHING — ❌ completely hides the problem
}

// 5. Convert checked to unchecked at boundaries (Spring does this)
catch (SQLException e) {
    throw new DataAccessException("DB error", e); // unchecked wrapper
}

// 6. Don't catch Throwable routinely
catch (Throwable t) { } // ❌ catches OutOfMemoryError, etc.

// 7. Validate early, throw quickly
void processOrder(Order order) {
    Objects.requireNonNull(order, "order must not be null");
    if (order.getItems().isEmpty()) throw new IllegalArgumentException("Order has no items");
    // ... processing
}
```

---

## Q26. Tricky — What is the output? (Exception during return)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
String test() {
    String result = "try";
    try {
        result = "try2";
        return result;
    } finally {
        result = "finally"; // modifies local variable — does this affect return value?
    }
}

System.out.println(test()); // ?
```

**Output:**
```
try2
```

**Why?** When `return result` executes, the return value (`"try2"`) is saved. `finally` runs, and changes `result` local variable to `"finally"` — but the saved return value is still `"try2"`. The local variable and return value are independent.

---

## Q27. Scenario — Wrap checked exceptions in Stream pipelines.

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

Stream operations cannot throw checked exceptions (lambda interfaces like `Function` don't declare `throws`).

```java
// PROBLEM: can't throw checked exception from lambda
List<String> paths = List.of("a.txt", "b.txt");
paths.stream()
    .map(p -> new FileReader(p)) // ❌ compile error — FileReader throws IOException
    .collect(Collectors.toList());

// SOLUTION 1: wrap in RuntimeException
paths.stream()
    .map(p -> {
        try {
            return new FileReader(p);
        } catch (IOException e) {
            throw new RuntimeException(e); // wrap
        }
    })
    .collect(Collectors.toList());

// SOLUTION 2: utility method (cleaner)
@FunctionalInterface
interface CheckedFunction<T, R> {
    R apply(T t) throws Exception;
}

static <T, R> Function<T, R> wrap(CheckedFunction<T, R> fn) {
    return t -> {
        try {
            return fn.apply(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };
}

paths.stream()
    .map(wrap(FileReader::new)) // clean!
    .collect(Collectors.toList());
```

---

## Q28. What is `UnsupportedOperationException`? Common causes.

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`UnsupportedOperationException` is thrown when an operation is not supported.

```java
// 1. Immutable collections
List<String> immutable = List.of("a", "b");
immutable.add("c"); // UnsupportedOperationException

List<String> fromArrays = Arrays.asList("a", "b");
fromArrays.add("c"); // UnsupportedOperationException (fixed size)
fromArrays.set(0, "x"); // ✅ allowed (not add/remove)

// 2. Abstract method not overridden
abstract class AbstractShape {
    abstract double area();
    double perimeter() { throw new UnsupportedOperationException("Not implemented"); }
}

// 3. Optional.get() on empty — throws NoSuchElementException, not UOE
// Optional.of() with null — throws NullPointerException

// 4. Collections.unmodifiableXxx wrappers
Set<String> unmodifiable = Collections.unmodifiableSet(new HashSet<>(Set.of("a")));
unmodifiable.add("b"); // UnsupportedOperationException
```

---

## Q29. What is `StackOverflowError` vs `OutOfMemoryError`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| | StackOverflowError | OutOfMemoryError |
|-|--------------------|-----------------|
| Cause | Too deep method call chain | Too many objects, insufficient heap |
| Memory area | Stack | Heap or Metaspace |
| Typical trigger | Infinite/deep recursion | Object accumulation, memory leak |
| Recovery | Rare — thread may terminate | Very rare — JVM unstable |
| Prevention | Add base case, use iteration, increase -Xss | Fix leak, increase -Xmx, reduce objects |

```java
// StackOverflowError
int count = 0;
void recurse() {
    count++;
    recurse(); // StackOverflowError after ~5000-10000 calls
}

// OutOfMemoryError
List<byte[]> list = new ArrayList<>();
while (true) {
    list.add(new byte[1024 * 1024]); // 1MB each → OOM
}
```

---

## Q30. Tricky — What is the output? (Exception in constructor)

**Difficulty:** Tricky | **Type:** Output Prediction

```java
class Resource {
    Resource() {
        System.out.println("Constructor start");
        if (true) throw new RuntimeException("Constructor failed");
        System.out.println("Constructor end"); // reached?
    }
    @Override protected void finalize() {
        System.out.println("Finalize called"); // called?
    }
}

try {
    Resource r = new Resource();
} catch (RuntimeException e) {
    System.out.println("Caught: " + e.getMessage());
}
```

**Output:**
```
Constructor start
Caught: Constructor failed
```

**Key points:**
- "Constructor end" is never reached
- The partially constructed object IS eligible for GC (finalize may eventually be called, but not guaranteed and not shown here)
- `r` is never assigned — no reference to the object exists
- Object may be GC'd immediately

---

## Q31. What is `NumberFormatException`? Common scenarios.

**Difficulty:** Basic | **Type:** Theory + Tricky

**Answer:**

```java
// Parsing invalid strings
Integer.parseInt("abc");     // NumberFormatException: For input string: "abc"
Integer.parseInt("12.5");    // NumberFormatException: not an integer
Integer.parseInt("");        // NumberFormatException: For input string: ""
Integer.parseInt(null);      // NumberFormatException: null
Double.parseDouble("1,000"); // NumberFormatException: comma not allowed

// Safe parsing patterns
try {
    int n = Integer.parseInt(input);
} catch (NumberFormatException e) {
    System.out.println("Invalid number: " + input);
}

// Using Optional
static OptionalInt parseOptional(String s) {
    try { return OptionalInt.of(Integer.parseInt(s)); }
    catch (NumberFormatException e) { return OptionalInt.empty(); }
}

// Check before parsing
if (input != null && input.matches("-?\\d+")) {
    int n = Integer.parseInt(input); // safe
}
```

---

## Q32. Scenario — Handle multiple types of exceptions in a robust way.

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
class RobustService {

    Result process(Request request) {
        // 1. Input validation — fail fast
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(request.getId(), "Request ID must not be null");

        try {
            // 2. Main processing
            Data data = repository.findById(request.getId());
            if (data == null) throw new NotFoundException(request.getId());

            return transform(data);

        } catch (NotFoundException e) {
            // 3. Known business exception — rethrow or return error result
            log.warn("Data not found for id={}", request.getId());
            throw e; // let caller handle (or return Result.notFound())

        } catch (DataAccessException e) {
            // 4. Infrastructure exception — wrap and rethrow
            log.error("DB error processing request {}", request.getId(), e);
            throw new ServiceException("PROCESSING_FAILED", "Unable to process request", e);

        } catch (Exception e) {
            // 5. Unexpected — log full context, wrap
            log.error("Unexpected error processing request: {}", request, e);
            throw new ServiceException("INTERNAL_ERROR", "Unexpected error", e);

        } finally {
            // 6. Cleanup — always runs
            auditLog.record(request.getId(), "process_attempt");
        }
    }
}
```

---

## Summary — Key Takeaways for Interviews

| Topic | What interviewers test |
|-------|----------------------|
| Checked vs Unchecked | When to use each; why RuntimeException is unchecked |
| try-with-resources | AutoCloseable, close order, suppressed exceptions |
| finally + return | return in finally overrides try return — never do it |
| Exception chaining | Always wrap with cause; getCause() chain |
| Custom exceptions | Checked or unchecked? Domain-specific fields? |
| Multi-catch | Must be independent types (not parent-child) |
| Catch order | Specific before general — compile error if unreachable |
| NPE prevention | Optional, Objects.requireNonNull, null-safe patterns |
| Production logging | Log once at handling boundary, include context |
