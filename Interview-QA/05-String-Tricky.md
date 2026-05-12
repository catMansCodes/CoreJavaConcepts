# String — Tricky Questions | Java Interview Guide (8–10 Years Experience)

## Quick Reference

```
String Pool:    "hello" → String Pool (heap) | new String("hello") → Heap (outside pool)
Immutable:      String object never changes — all methods return new String
Interning:      s.intern() → returns pool reference
Performance:    String concat in loop → O(n²) — use StringBuilder
Comparison:     == for reference, equals() for content, equalsIgnoreCase(), compareTo()
```

---

## Q1. Why is `String` immutable in Java?

**Difficulty:** Senior | **Type:** Theory (most asked)

**Answer:**

`String` is declared `final` and its internal `char[]` (or `byte[]` in Java 9+) is `private final`. No setter methods exist.

**Reasons for immutability:**

1. **String Pool** — multiple references can safely share the same object. If mutable, changing one would affect all.
2. **Security** — class names, file paths, network URLs are Strings. If mutable, malicious code could change path after security check.
3. **Thread safety** — immutable objects are inherently thread-safe, no synchronization needed.
4. **Hashcode caching** — `hashCode()` is computed once and cached. This makes `String` an ideal `HashMap` key.
5. **ClassLoader safety** — class names passed as Strings must not be alterable.

```java
String s = "hello";
s.toUpperCase(); // returns NEW String "HELLO", s is still "hello"
System.out.println(s); // hello — unchanged

// Internal implementation (simplified)
public final class String {
    private final byte[] value; // Java 9+ compact strings
    private int hash;           // cached hashCode, default 0

    // No setter — no way to change value
}

// Demonstration
String a = "Java";
String b = a; // both point to same object
a = a.concat(" 17"); // a now points to new String "Java 17"
System.out.println(b); // Java — b still points to original
```

---

## Q2. What is the String Pool (String Intern Pool)?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

The **String Pool** (also called String Intern Pool) is a special area in the **heap** (moved from PermGen to heap in Java 7) where the JVM stores string literals. When you create a string literal, JVM first checks if an identical string exists in the pool — if yes, returns that reference; if no, creates a new one.

```java
// Literals → go to String Pool
String s1 = "hello";
String s2 = "hello";
System.out.println(s1 == s2); // true — same pool reference

// new String() → bypasses pool, creates object in heap
String s3 = new String("hello");
System.out.println(s1 == s3); // false — different objects
System.out.println(s1.equals(s3)); // true — same content

// intern() → move to pool (or return existing pool reference)
String s4 = s3.intern();
System.out.println(s1 == s4); // true — s4 now points to pool object

// Concatenation at compile time → goes to pool
String s5 = "hel" + "lo"; // compile-time constant → pooled
System.out.println(s1 == s5); // true

// Concatenation at runtime → new heap object
String prefix = "hel";
String s6 = prefix + "lo"; // runtime concat → NOT pooled
System.out.println(s1 == s6); // false
System.out.println(s1.equals(s6)); // true
```

---

## Q3. Tricky — What is the output?

**Difficulty:** Tricky | **Type:** Output Prediction

```java
String a = "Java";
String b = "Java";
String c = new String("Java");
String d = c.intern();

System.out.println(a == b);      // ?
System.out.println(a == c);      // ?
System.out.println(a == d);      // ?
System.out.println(c == d);      // ?
System.out.println(a.equals(c)); // ?
```

**Output:**
```
true    — same pool reference
false   — c is new heap object
true    — d = intern() returns pool reference = a
false   — c is heap, d is pool
true    — same content
```

---

## Q4. Tricky — How many String objects are created?

**Difficulty:** Tricky | **Type:** Output Prediction

```java
String s1 = "hello";         // Q: How many objects?
String s2 = new String("hello"); // Q: How many objects?
String s3 = "hel" + "lo";   // Q: How many objects?

String prefix = "hel";
String s4 = prefix + "lo";  // Q: How many objects?
```

**Answer:**

```
s1 = "hello"              → 1 object (in pool) — if "hello" not yet in pool
s2 = new String("hello")  → 1 new heap object (pool already has "hello" from s1)
                             TOTAL so far: 2 objects
s3 = "hel" + "lo"         → 0 new objects! Compiler folds constants → "hello" already in pool
s4 = prefix + "lo"        → 1 new heap object ("hello" via StringBuilder internally)
                             ("hel" may or may not be pooled depending on context)
```

**Tricky detail:** `s2 = new String("hello")` creates **1 or 2** objects:
- If "hello" already in pool → 1 new heap object
- If "hello" NOT in pool → 1 pool object + 1 heap object = 2 objects

---

## Q5. What is the performance problem with String concatenation in a loop?

**Difficulty:** Senior | **Type:** Theory + Scenario

**Answer:**

`String + String` creates a **new String object** every time. In a loop, this is O(n²) in time and memory.

```java
// BAD — O(n²) — creates n intermediate String objects
String result = "";
for (int i = 0; i < 10000; i++) {
    result += i; // new String object each iteration
}

// Bytecode of result += i is: result = new StringBuilder(result).append(i).toString()
// But a new StringBuilder is created each iteration → O(n²) overall

// GOOD — O(n) — one StringBuilder, one final toString()
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 10000; i++) {
    sb.append(i);
}
String result2 = sb.toString();

// For single-line concat (non-loop) — compiler optimizes automatically
String name = "Hello " + firstName + " " + lastName; // compiler uses StringBuilder internally
```

**Benchmark (10,000 iterations):**
- `String +=` → ~200ms
- `StringBuilder.append()` → ~1ms

**When NOT to use StringBuilder:**
- Single statement: `String s = "Hello " + name + "!";` — compiler optimizes this
- Java 8+ with `String.join()` or `Collectors.joining()` for collections

---

## Q6. What is the difference between `String`, `StringBuilder`, and `StringBuffer`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

| Feature | `String` | `StringBuilder` | `StringBuffer` |
|---------|---------|-----------------|----------------|
| Mutable | No | Yes | Yes |
| Thread-safe | Yes (immutable) | No | Yes (synchronized) |
| Performance | Slowest (new object per operation) | Fastest | Slower than StringBuilder |
| API | Limited | Full (append, insert, delete, reverse) | Same as StringBuilder |
| Use case | Fixed text, keys, literals | Single-threaded string building | Multi-threaded string building |

```java
// StringBuilder — not thread-safe, use in single-threaded context
StringBuilder sb = new StringBuilder("Hello");
sb.append(" World");
sb.insert(5, ",");
sb.delete(6, 12);
sb.replace(0, 5, "Hi");
sb.reverse();
System.out.println(sb);     // reversed string
System.out.println(sb.length());
System.out.println(sb.charAt(0));
System.out.println(sb.indexOf("World"));

// StringBuffer — thread-safe but almost never needed
// Java's StringBuffer has synchronized on every method → slow under contention
// Prefer String.join() or Collectors.joining() for concurrent scenarios
```

---

## Q7. Tricky — What is the output of `String.valueOf(null)` vs `null.toString()`?

**Difficulty:** Tricky | **Type:** Output Prediction

```java
String s1 = String.valueOf(null); // ?
String s2 = null + "";           // ?
String s3 = "" + null;           // ?
String s4 = ((Object) null).toString(); // ?
String s5 = String.valueOf((Object) null); // ?
```

**Output:**
```java
String s1 = String.valueOf(null);         // ❌ NullPointerException — ambiguous overload, calls char[] version
String s1fixed = String.valueOf((Object) null); // "null" ← String literal "null"
String s2 = null + "";  // "null" — null coerces to string "null" in concat
String s3 = "" + null;  // "null"
String s4 = ((Object)null).toString(); // ❌ NullPointerException
```

**Key rule:** String concatenation with null converts null to the string `"null"`. But calling methods on null always throws NPE.

---

## Q8. What does `String.intern()` do? When to use it?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

`intern()` looks up the String in the pool:
- If pool has equal string → returns pool reference (discards the caller)
- If pool doesn't have it → adds it and returns pool reference

```java
String s1 = new String("Java");
String s2 = new String("Java");

System.out.println(s1 == s2);          // false — different heap objects
System.out.println(s1.intern() == s2.intern()); // true — same pool reference

// Memory optimization: if you have millions of duplicate strings (e.g., city names from CSV)
// interning can save significant memory
String city1 = new String("Mumbai").intern(); // now uses pool
String city2 = new String("Mumbai").intern(); // same pool reference
System.out.println(city1 == city2); // true
```

**When NOT to use `intern()`:**
- Strings that are unique — interning has overhead and won't save memory
- Short-lived strings — GC cannot collect pooled strings easily
- In Java 7+, pool is in heap (not PermGen), so OOM from interning is less common

---

## Q9. What are common `String` methods? Tricky edge cases.

**Difficulty:** Medium | **Type:** Theory + Tricky

**Answer:**

```java
String s = "Hello World";

// Basic
s.length();             // 11
s.charAt(0);            // 'H'
s.indexOf('o');         // 4 (first occurrence)
s.lastIndexOf('o');     // 7 (last occurrence)
s.indexOf("World");     // 6
s.contains("World");    // true
s.startsWith("Hello");  // true
s.endsWith("World");    // true

// Case
s.toUpperCase();        // HELLO WORLD
s.toLowerCase();        // hello world

// Trim / Strip
"  hello  ".trim();     // "hello" (removes ASCII whitespace)
"  hello  ".strip();    // "hello" (Java 11+ Unicode-aware)
"  hello  ".stripLeading();  // "hello  "
"  hello  ".stripTrailing(); // "  hello"

// Substring
s.substring(6);         // "World"  (from index 6 to end)
s.substring(0, 5);      // "Hello"  (0 inclusive, 5 exclusive)

// Replace
s.replace('l', 'r');    // "Herro Worrd" (char replace)
s.replace("World", "Java"); // "Hello Java"
s.replaceAll("\\s+", "_"); // "Hello_World" (regex)
s.replaceFirst("l", "L");  // "HeLlo World"

// Split
"a,b,,c".split(",");    // ["a", "b", "", "c"] — 4 elements
"a,b,,c".split(",", -1); // ["a", "b", "", "c"] — keeps trailing empty
"a,b,,c".split(",", 2);  // ["a", "b,,c"] — max 2 parts

// TRICKY: split(".")
"a.b.c".split("\\.");   // ["a", "b", "c"] — must escape dot!
"a.b.c".split(".");     // [] — dot matches any char in regex → splits on every char

// Join
String.join(", ", "a", "b", "c"); // "a, b, c"
String.join("-", List.of("x", "y")); // "x-y"

// Comparison
"abc".compareTo("abd");      // negative (c < d)
"ABC".compareToIgnoreCase("abc"); // 0

// Blank / Empty (Java 11+)
"".isEmpty();           // true
"  ".isEmpty();         // false
"  ".isBlank();         // true (Java 11+)

// Repeat (Java 11+)
"ha".repeat(3);         // "hahaha"

// chars() stream
"Hello".chars().forEach(c -> System.out.print((char)c + " ")); // H e l l o
```

---

## Q10. Tricky — What is the output of `substring` memory leak (old JDK)?

**Difficulty:** Senior | **Type:** Theory (historical)

**Answer:**

In **Java 6 and earlier**, `substring()` shared the underlying `char[]` with the original string. This caused memory leaks:

```java
// Java 6 behavior (not Java 7+)
String huge = readHugeFile(); // 100MB string
String tiny = huge.substring(0, 10); // tiny holds reference to the 100MB char[]!
huge = null; // "huge" reference gone, but char[] still alive because tiny holds it
// GC cannot collect the 100MB char[]
```

**Java 7+ fix:** `substring()` creates a new backing array — no shared reference, no leak.

```java
// If on Java 6 and need to avoid leak:
String tiny = new String(huge.substring(0, 10)); // force copy of char[]
// or
String tiny = huge.substring(0, 10).intern();
```

---

## Q11. Tricky — String comparison in switch statement.

**Difficulty:** Tricky | **Type:** Tricky

**Answer:**

`switch` with `String` (Java 7+) uses `equals()` and `hashCode()` internally — so it works correctly for content comparison.

```java
String command = "START";

switch (command) {
    case "START": System.out.println("Starting"); break;
    case "STOP":  System.out.println("Stopping"); break;
    default:      System.out.println("Unknown");
}
// Output: Starting

// What if command is null?
String nullCmd = null;
switch (nullCmd) { // ❌ NullPointerException!
    case "START": break;
}
// Always null-check before switch on String
```

---

## Q12. What is `String.format()` vs `formatted()` vs `MessageFormat`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
// String.format — static method
String msg1 = String.format("Hello %s, you are %d years old", "Alice", 30);

// Common format specifiers
String.format("%s", "text");          // string
String.format("%d", 42);              // integer
String.format("%f", 3.14);            // float (default 6 decimals)
String.format("%.2f", 3.14159);       // float with 2 decimals → 3.14
String.format("%10s", "hi");          // right-align in 10 chars
String.format("%-10s", "hi");         // left-align in 10 chars
String.format("%05d", 42);            // zero-padded → 00042
String.format("%,d", 1000000);        // thousand separator → 1,000,000
String.format("%n");                  // platform-specific newline

// formatted() — instance method (Java 15+)
String msg2 = "Hello %s, age %d".formatted("Bob", 25);

// MessageFormat — for internationalization
String msg3 = MessageFormat.format("Hello {0}, you have {1} messages", "Alice", 5);

// Text block (Java 15+) — multi-line
String json = """
        {
            "name": "%s",
            "age": %d
        }
        """.formatted("Alice", 30);
```

---

## Q13. Tricky — What is the output?

**Difficulty:** Tricky | **Type:** Output Prediction

```java
String s = "abcde";
System.out.println(s.charAt(5));   // ?
System.out.println(s.substring(5)); // ?
System.out.println(s.substring(2, 2)); // ?
System.out.println(s.indexOf("xyz")); // ?
```

**Output:**
```java
s.charAt(5)      → ❌ StringIndexOutOfBoundsException (valid indices: 0-4)
s.substring(5)   → "" (empty string — valid! substring from end to end)
s.substring(2,2) → "" (empty string — start == end)
s.indexOf("xyz") → -1 (not found, no exception)
```

---

## Q14. What is `String.chars()` and `String.codePoints()`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

```java
String s = "Hello";

// chars() — returns IntStream of char values
s.chars().forEach(c -> System.out.print((char) c)); // Hello
long upperCount = s.chars().filter(Character::isUpperCase).count(); // 1

// Convert char stream to String
String upper = s.chars()
    .map(Character::toUpperCase)
    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
    .toString(); // HELLO

// Count vowels
long vowels = "Hello World".chars()
    .filter(c -> "aeiouAEIOU".indexOf(c) >= 0)
    .count(); // 3

// codePoints() — handles supplementary characters (emoji, etc.)
"Hello 😊".codePoints().count(); // 7 (6 chars + 1 emoji code point)
"Hello 😊".chars().count();      // 8 (emoji = 2 char units in UTF-16)
```

---

## Q15. Tricky — String equality with `+` operator.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
String s1 = "Hello" + " " + "World"; // all literals
String s2 = "Hello World";

String a = "Hello";
String s3 = a + " World"; // runtime concat (a is variable)

final String b = "Hello";
String s4 = b + " World"; // b is final → compile-time constant

System.out.println(s1 == s2); // ?
System.out.println(s2 == s3); // ?
System.out.println(s2 == s4); // ?
```

**Output:**
```
true  — all literals → compile-time → same pool entry
false — s3 uses variable `a` → runtime concat → new heap object
true  — b is final (compile-time constant) → compiler folds → same pool entry
```

---

## Q16. What are `toCharArray()` and character-level operations?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
String s = "Hello World";

// toCharArray — modifiable copy
char[] chars = s.toCharArray();
chars[0] = 'h'; // modify the copy
String modified = new String(chars); // "hello World"

// Reverse a string
String original = "abcde";
StringBuilder sb = new StringBuilder(original);
String reversed = sb.reverse().toString(); // "edcba"

// Manual reverse
char[] arr = original.toCharArray();
int left = 0, right = arr.length - 1;
while (left < right) {
    char temp = arr[left]; arr[left] = arr[right]; arr[right] = temp;
    left++; right--;
}
String manualReversed = new String(arr);

// Check palindrome
boolean isPalindrome = original.equals(reversed);

// Anagram check
char[] c1 = "listen".toCharArray();
char[] c2 = "silent".toCharArray();
Arrays.sort(c1); Arrays.sort(c2);
boolean isAnagram = Arrays.equals(c1, c2); // true
```

---

## Q17. What is `String.join()` and `Collectors.joining()`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
// String.join — static, simple
String joined = String.join(", ", "Alice", "Bob", "Charlie");
System.out.println(joined); // Alice, Bob, Charlie

String.join("-", List.of("a", "b", "c")); // a-b-c

// Collectors.joining — for Streams
List<String> names = List.of("Alice", "Bob", "Charlie");

String simple   = names.stream().collect(Collectors.joining());         // AliceBobCharlie
String commas   = names.stream().collect(Collectors.joining(", "));     // Alice, Bob, Charlie
String brackets = names.stream().collect(Collectors.joining(", ", "[", "]")); // [Alice, Bob, Charlie]

// Building CSV
List<String[]> rows = List.of(
    new String[]{"Alice", "30", "Engineer"},
    new String[]{"Bob", "25", "Designer"}
);
String csv = rows.stream()
    .map(row -> String.join(",", row))
    .collect(Collectors.joining("\n"));
```

---

## Q18. Tricky — What is the output of `equals` vs `equalsIgnoreCase` with special characters?

**Difficulty:** Tricky | **Type:** Tricky

**Answer:**

```java
System.out.println("abc".equals("ABC"));             // false
System.out.println("abc".equalsIgnoreCase("ABC"));   // true
System.out.println("abc".equalsIgnoreCase("ABC "));  // false — trailing space

// Turkish locale trap
String s = "TITLE";
System.out.println(s.toLowerCase().equals("title")); // true in English locale
// In Turkish locale: I → ı (dotless i), not i
// s.toLowerCase(Locale.ENGLISH) is safer for ASCII
System.out.println(s.toLowerCase(Locale.ENGLISH).equals("title")); // always true

// compareToIgnoreCase
System.out.println("apple".compareToIgnoreCase("APPLE")); // 0
System.out.println("apple".compareToIgnoreCase("BANANA")); // negative (a < b)
```

---

## Q19. What is `String.matches()` vs `Pattern`?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

```java
// String.matches() — compiles regex each time (slower for repeated use)
String email = "user@example.com";
boolean valid = email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

// TRICKY: matches() requires FULL string match (like '^...$')
"Hello World".matches("World");  // false — doesn't match full string
"Hello World".matches(".*World"); // true — .* matches any prefix

// Pattern + Matcher — compiled once, reused (faster for loops)
Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
Matcher matcher = emailPattern.matcher(email);
boolean valid2 = matcher.matches(); // full match
boolean found  = matcher.find();    // find anywhere in string

// Extracting groups
Pattern datePattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
Matcher m = datePattern.matcher("Today is 2024-01-15, yesterday was 2024-01-14");
while (m.find()) {
    System.out.println("Year: " + m.group(1) + ", Month: " + m.group(2) + ", Day: " + m.group(3));
}

// replaceAll with regex
String cleaned = "Hello   World  Java".replaceAll("\\s+", " "); // "Hello World Java"
```

---

## Q20. Tricky — `String.split()` edge cases.

**Difficulty:** Tricky | **Type:** Tricky

```java
// How many elements?
String[] a = "a,b,c".split(",");       // ?
String[] b = "a,b,c,".split(",");      // ?
String[] c = ",a,b,c".split(",");      // ?
String[] d = "a,,b".split(",");        // ?
String[] e = "a,b,c".split(",", -1);   // ?
String[] f = "a,b,c,".split(",", -1);  // ?
```

**Output:**
```java
"a,b,c".split(",")     → [a, b, c]         — 3 elements
"a,b,c,".split(",")    → [a, b, c]         — trailing empty string REMOVED (default)
",a,b,c".split(",")    → ["", a, b, c]     — 4 elements (leading empty kept)
"a,,b".split(",")      → [a, "", b]        — 3 elements (middle empty kept)
"a,b,c".split(",", -1) → [a, b, c]        — same as default here
"a,b,c,".split(",", -1)→ [a, b, c, ""]    — trailing empty KEPT with limit -1
```

**Rule:** Default `split()` removes trailing empty strings. Use `split(regex, -1)` to keep them.

---

## Q21. What is compact strings in Java 9+?

**Difficulty:** Senior | **Type:** Theory

**Answer:**

Before Java 9, `String` stored characters as `char[]` (2 bytes per char = UTF-16). Most Strings contain only Latin-1 characters (1 byte each).

**Java 9 Compact Strings:**
- If all characters fit in Latin-1 (0-255) → stored as `byte[]` with 1 byte/char
- Otherwise → stored as `byte[]` with 2 bytes/char (UTF-16)
- `coder` field indicates encoding (0 = LATIN1, 1 = UTF16)

```java
// Transparent to developer — no API change
String ascii = "Hello"; // stored as 5 bytes (compact)
String unicode = "Hello 😊"; // stored as UTF-16 (non-compact)

// Performance impact: ~15% less memory for ASCII-heavy applications
// Operations are slightly slower due to encoding checks, but net benefit is positive
```

---

## Q22. Tricky — Output prediction with `+` and `null`.

**Difficulty:** Tricky | **Type:** Output Prediction

```java
String s = null;
System.out.println("Value: " + s);    // ?
System.out.println(s + " is null");   // ?
System.out.println(s.toString());     // ?
System.out.println("" + s + s);      // ?
System.out.println(1 + 2 + "3");      // ?
System.out.println("1" + 2 + 3);      // ?
System.out.println("1" + (2 + 3));    // ?
```

**Output:**
```
Value: null
null is null
NullPointerException
nullnull
33           ← 1+2=3 (int math first), then 3+"3"="33"
123          ← "1"+2="12" (String concat left to right), "12"+3="123"
15           ← (2+3)=5 first (int), then "1"+5="15"
```

---

## Q23. What is `String.valueOf()` vs `toString()`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
int i = 42;
double d = 3.14;
boolean b = true;
Object obj = null;

// String.valueOf — null-safe, handles primitives
String.valueOf(i);     // "42"
String.valueOf(d);     // "3.14"
String.valueOf(b);     // "true"
String.valueOf(obj);   // "null" — null-safe!
String.valueOf(null);  // NullPointerException — ambiguous overload (char[] vs Object)
String.valueOf((Object) null); // "null"

// toString() — not null-safe
Integer.toString(42);      // "42"
Double.toString(3.14);     // "3.14"
obj.toString();            // NullPointerException if obj is null

// "" + value — works but less readable
"" + 42;      // "42"
"" + null;    // "null"
"" + obj;     // "null"
```

---

## Q24. How to check if a String is a palindrome, anagram, or contains only digits?

**Difficulty:** Medium | **Type:** Scenario

**Answer:**

```java
// Palindrome
boolean isPalindrome(String s) {
    String cleaned = s.toLowerCase().replaceAll("[^a-z0-9]", "");
    String reversed = new StringBuilder(cleaned).reverse().toString();
    return cleaned.equals(reversed);
}
System.out.println(isPalindrome("A man, a plan, a canal: Panama")); // true

// Anagram
boolean isAnagram(String s, String t) {
    char[] a = s.toLowerCase().toCharArray();
    char[] b = t.toLowerCase().toCharArray();
    Arrays.sort(a); Arrays.sort(b);
    return Arrays.equals(a, b);
}
System.out.println(isAnagram("listen", "silent")); // true

// Contains only digits
boolean isNumeric(String s) {
    return s != null && !s.isEmpty() && s.chars().allMatch(Character::isDigit);
}
// OR
boolean isNumericRegex(String s) {
    return s != null && s.matches("\\d+");
}
System.out.println(isNumeric("12345")); // true
System.out.println(isNumeric("123a5")); // false

// Count occurrences of each character
Map<Character, Long> freq = "hello world".chars()
    .filter(c -> c != ' ')
    .mapToObj(c -> (char) c)
    .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
System.out.println(freq); // {h=1, e=1, l=3, o=2, w=1, r=1, d=1}
```

---

## Q25. What is `StringTokenizer`? Is it still used?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

`StringTokenizer` is a legacy class for splitting strings by delimiters. Largely replaced by `String.split()` and `Scanner`.

```java
// Legacy: StringTokenizer
StringTokenizer st = new StringTokenizer("Hello World Java", " ");
while (st.hasMoreTokens()) {
    System.out.println(st.nextToken()); // Hello, World, Java
}

// Modern: String.split()
String[] tokens = "Hello World Java".split(" ");

// Key difference: StringTokenizer doesn't handle consecutive delimiters as empty tokens
StringTokenizer st2 = new StringTokenizer("a,,b", ",");
st2.countTokens(); // 2 (skips empty between ,,)

"a,,b".split(","); // ["a", "", "b"] — 3 elements (empty string preserved)
```

---

## Q26. Tricky — What is the output of string operations with empty strings?

**Difficulty:** Tricky | **Type:** Tricky

```java
String s = "";
System.out.println(s.length());        // ?
System.out.println(s.isEmpty());       // ?
System.out.println(s.toUpperCase());   // ?
System.out.println(s.charAt(0));       // ?
System.out.println(s.substring(0,0));  // ?
System.out.println(s.indexOf(""));    // ?
System.out.println("abc".indexOf("")); // ?
```

**Output:**
```
0
true
""          (empty string — no exception)
StringIndexOutOfBoundsException
""          (empty substring — valid)
0           (empty string found at position 0)
0           (empty string always found at position 0)
```

---

## Q27. What is `String.format` precision and width?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
// Width (minimum field width)
System.out.printf("%10d%n", 42);     //         42 (right-aligned, 10 wide)
System.out.printf("%-10d%n", 42);    // 42         (left-aligned)
System.out.printf("%010d%n", 42);    // 0000000042 (zero-padded)

// Precision (decimal places for floats, max chars for strings)
System.out.printf("%.2f%n", 3.14159); // 3.14
System.out.printf("%.5s%n", "Hello World"); // Hello (truncate string)

// Width + precision
System.out.printf("%10.2f%n", 3.14); //       3.14

// Argument index (reuse)
System.out.printf("%1$s %2$s %1$s%n", "Hello", "World"); // Hello World Hello

// Commonly used format letters
// %s String  %d int  %f float  %c char  %b boolean
// %x hex     %o octal  %n newline  %% literal %
```

---

## Q28. Scenario — How would you find duplicate characters in a String?

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
// Method 1: Using Map and Stream
String input = "programming";

Map<Character, Long> freq = input.chars()
    .mapToObj(c -> (char) c)
    .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

Set<Character> duplicates = freq.entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .collect(Collectors.toSet());

System.out.println("Duplicates: " + duplicates); // {r, g, m}

// Method 2: Using int array (fastest — O(n))
int[] count = new int[256]; // ASCII
for (char c : input.toCharArray()) count[c]++;
for (int i = 0; i < 256; i++) {
    if (count[i] > 1) System.out.print((char) i + " ");
}

// Method 3: Find first non-repeating character
char firstUnique = input.chars()
    .mapToObj(c -> (char) c)
    .filter(c -> freq.get(c) == 1)
    .findFirst()
    .orElseThrow();
System.out.println("First unique: " + firstUnique); // p
```

---

## Q29. What is the difference between `contentEquals()` and `equals()`?

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
String s = "hello";
StringBuilder sb = new StringBuilder("hello");
StringBuffer sbuf = new StringBuffer("hello");
CharSequence cs = "hello";

// equals() — compares with another String only
s.equals(sb);   // false — sb is StringBuilder, not String
s.equals(cs);   // true — cs is a String literal (CharSequence + String)

// contentEquals() — compares content with any CharSequence
s.contentEquals(sb);   // true — same content
s.contentEquals(sbuf); // true
s.contentEquals(cs);   // true
s.contentEquals("hello"); // true

// equalsIgnoreCase — only for String
"HELLO".equalsIgnoreCase("hello"); // true
// "HELLO".equalsIgnoreCase(sb); // compile error — only takes String
```

---

## Q30. Tricky — What is the output? (Autoboxing and String equality)

**Difficulty:** Tricky | **Type:** Tricky

```java
Integer a = 127, b = 127;
Integer c = 128, d = 128;
System.out.println(a == b);   // ?
System.out.println(c == d);   // ?

String s1 = "100";
String s2 = String.valueOf(100);
String s3 = Integer.toString(100);
System.out.println(s1 == s2); // ?
System.out.println(s1 == s3); // ?
System.out.println(s1.equals(s2)); // ?
```

**Output:**
```
true    — Integer cache range -128 to 127
false   — 128 outside cache range → new Integer objects
true    — "100" literal is in pool; String.valueOf() returns "100" from pool (implementation detail — may vary)
false   — Integer.toString() creates new String object
true    — same content
```

**Lesson:** Never use `==` for Integer or String comparisons in business logic. Always use `.equals()`.

---

## Q31. Scenario — Reverse words in a sentence without using split().

**Difficulty:** Senior | **Type:** Scenario

**Answer:**

```java
// Using split (simple way)
String sentence = "Hello World Java";
String[] words = sentence.split(" ");
StringBuilder sb = new StringBuilder();
for (int i = words.length - 1; i >= 0; i--) {
    sb.append(words[i]);
    if (i > 0) sb.append(" ");
}
System.out.println(sb.toString()); // Java World Hello

// Without split — two-pass reversal
char[] chars = sentence.toCharArray();
// Step 1: reverse entire string
reverse(chars, 0, chars.length - 1);
// Step 2: reverse each word
int start = 0;
for (int i = 0; i <= chars.length; i++) {
    if (i == chars.length || chars[i] == ' ') {
        reverse(chars, start, i - 1);
        start = i + 1;
    }
}
System.out.println(new String(chars)); // Java World Hello

static void reverse(char[] arr, int l, int r) {
    while (l < r) { char t = arr[l]; arr[l] = arr[r]; arr[r] = t; l++; r--; }
}

// Using Stream
String reversed = Arrays.stream(sentence.split(" "))
    .collect(Collectors.collectingAndThen(
        Collectors.toList(),
        list -> { Collections.reverse(list); return list.stream().collect(Collectors.joining(" ")); }
    ));
```

---

## Q32. What are `strip()`, `stripLeading()`, `stripTrailing()` vs `trim()`? (Java 11+)

**Difficulty:** Medium | **Type:** Theory

**Answer:**

```java
String s = " Hello World "; //   is Unicode ENQUAD (whitespace, not ASCII)

s.trim().equals("Hello World");          // false — trim() only removes ASCII whitespace (≤ 0x20)
s.strip().equals("Hello World");         // true — strip() removes Unicode whitespace
s.stripLeading().endsWith(" ");     // true — only removes leading
s.stripTrailing().startsWith(" ");  // true — only removes trailing

// ASCII whitespace
"\t Hello \n".trim();   // "Hello"
"\t Hello \n".strip();  // "Hello"

// Unicode check
Character.isWhitespace(' '); // true — Unicode whitespace
' ' <= 0x20;                      // true — ASCII space (trim handles this)
```

**Rule:** Prefer `strip()` over `trim()` in Java 11+ for correct Unicode handling.

---

## Summary — Key Takeaways for Interviews

| Topic | What interviewers test |
|-------|----------------------|
| Immutability | Why + consequences (pool, thread safety, hashCode caching) |
| `==` vs `equals` | Reference vs content — always use equals() |
| String Pool | When in pool, when on heap, intern() |
| Performance | + in loop is O(n²), use StringBuilder |
| split() edge cases | Trailing empties, regex dot escape |
| null handling | null + "" = "null", null.method() = NPE |
| Operator precedence | 1+2+"3" vs "1"+2+3 |
| strip vs trim | Unicode awareness (Java 11+) |
