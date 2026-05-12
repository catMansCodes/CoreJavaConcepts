# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A **Java learning repository** (beginner → senior-level) covering Core Java concepts with hands-on code examples, theory notes (README.md files), and interview Q&A. The goal is comprehensive interview readiness — no external resources needed.

- **Java version:** 17 (compiled with `maven.compiler.source/target = 17`)
- **Build tool:** Maven (`pom.xml` at root, no test framework configured yet)
- **Group ID:** `com.catmanscode` / package prefix: `com.catmanscodes`

## Build & Run Commands

```bash
# Compile the project
mvn compile

# Run a specific main class
mvn exec:java -Dexec.mainClass="com.catmanscodes.java8.stream.StreamMainApp"

# Package the project
mvn package
```

There are no tests yet. Each topic has standalone `*MainApp.java` or `*Main.java` entry points that can be run directly.

## Repository Structure

```
src/main/java/com/catmanscodes/
├── collections/          # Java Collections Framework
│   ├── README.md         # Full theory + hierarchy + interview Q&A
│   ├── list/             # ArrayList, LinkedList, Vector, Stack, CopyOnWriteArrayList
│   ├── set/              # HashSet, LinkedHashSet, TreeSet (SetAllMain.java)
│   ├── map/              # HashMap, LinkedHashMap, TreeMap, Hashtable, ConcurrentHashMap,
│   │                     #   WeakHashMap, IdentityHashMap, EnumMap, SortedMap, ImmutableMap
│   └── queue/            # PriorityQueue, Deque
├── java8/                # Java 8 features (most complete module)
│   ├── lamda/            # Lambda expressions + Comparator examples
│   ├── predicate/        # Predicate, BiPredicate
│   ├── function/         # Function, BiFunction
│   ├── consumer/         # Consumer, BiConsumer
│   ├── suppiler/         # Supplier
│   ├── operator/         # UnaryOperator, BinaryOperator
│   ├── defaultmethod/    # Default methods in interfaces
│   ├── stream/           # Stream API + creation methods
│   ├── optional/         # Optional usage
│   ├── dateandtime/      # java.time API
│   └── AppDemo/          # Employee-based functional demo combining all concepts
└── mostasked/            # Top interview Q&A (InterviewQA.md — in progress)
```

## Design Conventions

- Each package maps to one concept. Code is **demonstrative**, not production — main methods print output to illustrate behavior.
- Theory lives in `README.md` files alongside the code (currently: `collections/README.md`).
- Entry point naming: `*MainApp.java` (java8 module) or `*Main.java` (collections module).
- New topics should follow the folder template: `README.md` + `examples/` + `interview-qa/` + `tricky-questions/` + `scenarios/` (see root `README.md`).
- Planned but not yet implemented: `oops/`, `array/`, `string/`, `exception/`, `java9/`–`java17/`, `multithreading/`, `jvm/`, `generics/`, `innerclass/`.