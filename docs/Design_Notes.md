# Engineering Design Evaluations

This document summarizes the main engineering decisions behind the LearnTrack system and the trade-offs selected during implementation.

## End-to-End Execution Flow

```text
CSV Script / Console Input
          |
          v
Main CLI Orchestrator
          |
          v
Application Services
          |
          v
TransactionalStore
          |
          v
Domain Entities

Console CLI Output <--- ConsolePresenter <--- Service Query Results
```

The architecture keeps command parsing, CSV scripting, transaction coordination, presentation formatting, and domain rules in separate modules. This prevents changes to input schemas, display layout, or persistence mechanics from forcing changes in the core entities.

## Performance Analysis: ArrayList vs. Native Fixed Arrays

Native Java arrays allocate a fixed-size sequential block of memory. They are efficient when the size is known in advance, but they cannot grow dynamically. If the boundary is exceeded, a developer must allocate a new array, copy values manually, and update all references.

`ArrayList<T>` provides a safer abstraction for dynamic records. It manages resizing internally and supports amortized O(1) append behavior. LearnTrack uses collection abstractions where record counts are unknown, such as CSV rows, query results, and table-rendering models.

## Transaction Engine Trade-Off

LearnTrack uses a stack-based delta-frame transaction engine. Writes are recorded in the active transaction frame instead of cloning the full database map. Reads scan from the newest frame toward committed storage to find the visible value.

Compared with copy-on-write cloning, the delta-stack approach reduces memory growth from O(N * T) to O(K * T), where N is the total record count, K is the number of mutated keys, and T is transaction nesting depth. Rollback is fast because discarding the top frame removes all uncommitted changes at once.

## CSV Parser Trade-Off

The CSV parser uses a single-character state machine rather than `String.split(",")`. Naive splitting fails on quoted fields that contain commas, escaped quotes, or embedded newlines. The state machine tracks normal, quoted, and quote-escaped states so command scripts can safely include real CSV data.

## Thread-Safe Static Access vs. Dependency Injection

`IdGenerator` is a static utility with synchronized methods. This makes ID generation simple for the CLI and seed interpreter while preventing race conditions in multi-threaded usage.

For larger systems, dependency injection would be preferable because it makes components easier to mock, replace, and configure. LearnTrack already applies constructor injection for services and stores, so the codebase can evolve toward injectable ID generation later without disturbing domain entities.

## Code Quality Invariants

- Domain entities contain business state and validation only.
- Application services coordinate business rules and store access.
- `TransactionalStore` owns transaction behavior and does not know about specific entity types.
- `CSVParser` owns file parsing and writing mechanics.
- `ConsolePresenter` owns tabular terminal formatting.
- `Main` is the outer orchestration boundary that wires components together.

## Final Integration Verification

The final seed file exercises the startup automation path by opening a transaction, inserting students, courses, and enrollments, committing the transaction, running read checks, and applying a student update in a second committed transaction. This keeps the default runtime path useful for reviewers while validating the interaction among the CSV parser, command interpreter, services, ID synchronization, and transaction store.

## Architectural Comparison

| Technical Component | Custom Architecture Design | Alternative Design Option | Trade-Off |
| --- | --- | --- | --- |
| In-memory database | Stack-based delta frame engine | Full copy-on-write map cloning | Delta frames reduce memory churn and make rollback cheap. |
| CSV parsing | Character-level state machine | `line.split(",")` | State transitions correctly preserve quoted commas, quotes, and newlines. |
| Architecture layout | Inward-pointing Clean Architecture layers | Controller-service-repository coupling | Core rules stay independent of CLI, CSV, and storage details. |
| Error management | Custom unchecked exception hierarchy | Direct runtime termination | Errors can be reported at the boundary while allowing the JVM process to continue. |
| Console rendering | Dynamic table presenter | Ad hoc string concatenation in CLI handlers | Presentation logic stays reusable and out of business services. |

## Architectural Summary

LearnTrack demonstrates a zero-dependency Core Java application with clean boundaries, transaction safety, robust CSV scripting, custom terminal presentation, and service-layer business orchestration. The resulting system is small enough to inspect easily while still modeling patterns used in professional Java systems.

## References

- Robert C. Martin, Clean Architecture principles
- Java Platform documentation for collections, synchronization, and exception handling
- RFC 4180 CSV format conventions
