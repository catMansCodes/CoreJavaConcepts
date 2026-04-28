# 📘 Java Mastery Repository (Beginner → Senior Level)

## 🎯 Objective

This repository is designed as a **single source of truth** for mastering **Core Java from fundamentals to advanced (senior-level interview readiness)**.

It combines:

* 📚 Concept explanations (deep + structured)
* 💻 Hands-on coding examples
* ❓ Interview Q&A (basic → advanced)
* 🧠 Tricky & edge-case problems
* 🧩 Scenario-based questions (real-world production thinking)

The goal is simple:

> You should **not need any external resource** after completing this repository.

---

## 🧱 Repository Design Principles

* **Topic-wise modular structure**
* **Each topic = self-contained learning unit**
* **Code + Theory + Interview prep together**
* **Progressive learning (basic → advanced → expert)**
* **Production-level insights (not just academic)**

---

## 📂 Project Structure

```
java-mastery-repo/
│
├── README.md   <-- (This file - Master Guide)
│
└── src/
    └── com/
        └── catmanscode/
            │
            ├── oops/
            │   ├── README.md
            │   ├── examples/
            │   ├── interview-qa/
            │   ├── tricky-questions/
            │   └── scenarios/
            │
            ├── array/
            │   ├── README.md
            │   ├── examples/
            │   ├── interview-qa/
            │   ├── tricky-questions/
            │   └── scenarios/
            │
            ├── string/
            ├── exception/
            ├── collection/
            ├── java8/
            ├── java9/
            ├── java10/
            ├── java11/
            ├── java14/
            ├── java15/
            ├── java16/
            ├── java17/
            ├── multithreading/
            ├── jvm/
            ├── generics/
            ├── innerclass/
            └── misc/
```

---

## 📦 Folder Standard (STRICT TEMPLATE)

Every topic folder will follow **exact same structure**:

```
topic-name/
│
├── README.md                <-- Full theory + notes
│
├── examples/                <-- Coding examples (basic → advanced)
│   ├── Example1.java
│   ├── Example2.java
│   └── RealWorldCase.java
│
├── interview-qa/
│   ├── basic.md
│   ├── intermediate.md
│   └── advanced.md
│
├── tricky-questions/
│   ├── tricky1.java
│   ├── tricky2.java
│   └── explanation.md
│
└── scenarios/
    ├── scenario1.md
    ├── scenario2.md
    └── production-cases.md
```

---

## 📚 Topics Coverage Plan

### 🔹 1. OOPs

* Pillars: Encapsulation, Inheritance, Polymorphism, Abstraction
* SOLID Principles
* Composition vs Inheritance
* Real-world modeling

---

### 🔹 2. Arrays

* Basics → Advanced patterns
* Sliding window, Two pointer
* Interview problems (Amazon/Google level)

---

### 🔹 3. Strings

* Immutability internals
* String pool
* Performance traps
* Advanced string manipulation

---

### 🔹 4. Exception Handling

* Checked vs Unchecked
* Custom exceptions
* Best practices (enterprise-level)

---

### 🔹 5. Collections Framework

* List, Set, Map internals
* HashMap deep dive (VERY IMPORTANT)
* Concurrent collections
* Performance comparison

---

### 🔹 6. Java 8 (🔥 MOST IMPORTANT)

* Lambda expressions
* Functional interfaces
* Stream API (deep dive)
* Optional
* Method references

👉 Includes:

* Tricky stream questions
* Performance pitfalls
* Parallel stream behavior

---

### 🔹 7–13. Java Version Features

| Version | Features                 |
| ------- | ------------------------ |
| Java 9  | Modules (JPMS)           |
| Java 10 | `var` keyword            |
| Java 11 | HTTP Client, String APIs |
| Java 14 | Switch Expressions       |
| Java 15 | Text Blocks              |
| Java 16 | Records                  |
| Java 17 | Sealed Classes           |

Each will include:

* Why introduced?
* Before vs After
* Real-world usage

---

### 🔹 14. Multithreading (CRITICAL)

* Thread lifecycle
* Synchronization
* Locks (ReentrantLock)
* Executor Framework
* CompletableFuture
* Race conditions & debugging

---

### 🔹 15. Misc (Senior-Level Core)

Includes:

* JVM internals
* Garbage Collection (G1, ZGC basics)
* Memory management
* Generics deep dive
* Inner classes
* ClassLoader
* Reflection

---

## 🧠 Learning Flow Strategy

Each topic follows:

```
1. Concept Understanding
2. Code Examples
3. Edge Cases
4. Interview Questions
5. Real-world Scenarios
```

---

## 🧪 Example: Topic Breakdown (Array)

### README.md will include:

* What is array?
* Memory representation
* Time complexity
* Common patterns

### examples/

* Reverse array
* Rotate array
* Subarray problems

### interview-qa/

* Difference Array vs ArrayList?
* Why array is faster?

### tricky-questions/

* Output-based questions
* Edge case failures

### scenarios/

* Real production problem using arrays

---

## 🎯 End Goal

After completing this repo, you should be able to:

* Crack **Senior Java Interviews (8–12 years)**
* Explain **JVM internals confidently**
* Solve **DSA + Java problems**
* Design **production-grade systems**
* Debug **real-world issues**

---

## 🚀 Execution Plan

We will proceed step-by-step:

### Phase 1:

* ✅ OOPs (Deep Dive)
* ✅ Array
* ✅ String

### Phase 2:

* Collections
* Exception Handling
* Java 8

### Phase 3:

* Java 9–17 features

### Phase 4:

* Multithreading
* JVM & GC
* Advanced topics

---

## 🔜 Next Step

We will start with:

👉 **`com.catmanscode.oops` (Deep Dive + Interview Focus)**

This will include:

* Theory (senior-level)
* Code examples
* Interview Q&A (0 → 10 years level)
* Real production scenarios

---

## ⚡ Final Note

This is not just a repository —
this is your **Java Engineering Knowledge Base**.

Consistency + Depth = Mastery.

---


---

# Java Functional Interfaces – Interview Guide 🚀

This repository demonstrates the core Java functional interfaces with practical examples and interview-ready explanations.

---

## 📌 Overview

Java 8 introduced **functional interfaces** to support **lambda expressions** and **functional programming**.

This guide covers:

* Predicate
* BiPredicate
* Function
* Consumer
* Supplier
* Function chaining (`andThen`, `compose`)

---

## 🧪 Demo Use Case: Employee Processing

We use an `Employee` class to demonstrate:

| Interface   | Purpose           |
| ----------- | ----------------- |
| Predicate   | Filter employees  |
| BiPredicate | Compare employees |
| Function    | Transform data    |
| Consumer    | Perform action    |
| Supplier    | Generate object   |

---

## 💻 Example Snippets

### 🔹 Predicate

```java
Predicate<Employee> highSalary = emp -> emp.salary > 60000;
```

---

### 🔹 BiPredicate

```java
BiPredicate<Employee, Employee> isFirstHigher =
        (e1, e2) -> e1.salary > e2.salary;
```

---

### 🔹 Function

```java
Function<Employee, String> empToString =
        emp -> emp.name + " earns " + emp.salary;
```

---

### 🔹 Consumer

```java
Consumer<Employee> printEmp = emp -> System.out.println(emp);
```

---

### 🔹 Supplier

```java
Supplier<Employee> empSupplier =
        () -> new Employee("Generated", 25, 40000);
```

---

# 🔗 Function Chaining (Very Important for Interviews)

## ✅ andThen() → Left to Right Execution

```java
Function<Integer, Integer> multiply = x -> x * 2;
Function<Integer, Integer> add = x -> x + 3;

Function<Integer, Integer> result = multiply.andThen(add);

System.out.println(result.apply(5)); // (5 * 2) + 3 = 13
```

---

## ✅ compose() → Right to Left Execution

```java
Function<Integer, Integer> result = multiply.compose(add);

System.out.println(result.apply(5)); // (5 + 3) * 2 = 16
```

---

## 🧠 Key Difference

| Method  | Execution Order |
| ------- | --------------- |
| andThen | first → second  |
| compose | second → first  |

---

# 🔥 Top 10 Interview Questions (with Answers)

### 1. What is Predicate?

A functional interface that takes one input and returns a boolean.

---

### 2. Predicate vs Function?

* Predicate → boolean output
* Function → transformed output

---

### 3. What is BiPredicate?

Takes two inputs and returns boolean (used for comparison).

---

### 4. What is Consumer?

Consumes input but returns nothing (used for side effects).

---

### 5. What is Supplier?

Takes no input and returns a value.

---

### 6. Function vs Supplier?

* Function → needs input
* Supplier → no input

---

### 7. What are Predicate default methods?

* `and()`
* `or()`
* `negate()`

---

### 8. What is UnaryOperator & BinaryOperator?

* UnaryOperator → Function<T, T>
* BinaryOperator → BiFunction<T, T, T>

---

### 9. Why use built-in functional interfaces?

* Standard
* Cleaner code
* Works with Streams API

---

### 10. Real-world usage?

Used in:

* Streams API
* Spring Boot validations
* Data transformations
* Logging & pipelines
* Reactive programming

---

# 🎯 Interview Tip

Always explain like this:

* Predicate → filtering
* Function → transformation
* Consumer → action
* Supplier → creation
* BiPredicate → comparison

👉 This clarity = strong signal for senior-level understanding.

---

# 📦 Conclusion

Mastering these interfaces is critical for:

* Writing clean functional code
* Using Streams effectively
* Performing well in Java/Spring interviews

---

Happy Coding! 🚀
