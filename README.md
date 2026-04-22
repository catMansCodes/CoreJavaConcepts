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
