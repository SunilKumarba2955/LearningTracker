# Engineering Design Notes

This document explains the main LearnTrack design choices in the language of the assignment brief.

## End-to-End Flow

```text
Console Input / Seed CSV
          |
          v
Main CLI Menu
          |
          v
Service Classes
          |
          v
ArrayList Records
          |
          v
Domain Entities

Console Output <--- ConsolePresenter <--- Service Query Results
```

`Main` displays menus, reads input, and calls service methods. The service classes hold the `ArrayList` data and enforce business rules. Entity classes keep private fields and expose getters/setters for encapsulation.

## Why ArrayList Instead of Array

Native arrays have a fixed size. A student-management system does not know in advance how many students, courses, or enrollments an admin will create.

`ArrayList` grows dynamically and keeps add, search, update, and list operations simple. LearnTrack uses:

- `ArrayList<Student>` in `StudentService`
- `ArrayList<Course>` in `CourseService`
- `ArrayList<Enrollment>` in `EnrollmentService`

## Static Members

`IdGenerator` uses private static counters and public static methods such as `getNextStudentId()`. This keeps ID creation centralized and avoids duplicating counter logic in the menu code.

## Inheritance and Polymorphism

`Person` is the base class for people in the system. `Student` and `Trainer` extend `Person`.

`Student` overrides `getDisplayName()` to include student-specific batch information. This demonstrates basic polymorphism because a `Person` reference can call `getDisplayName()` and receive subtype-specific behavior.

## Exception Handling

LearnTrack uses custom unchecked exceptions for expected application faults:

- `InvalidInputException` for invalid menu, CSV, or field values
- `EntityNotFoundException` when a requested student, course, or enrollment ID does not exist

The CLI catches these known exceptions and prints clean messages so the menu can continue.

## Seed CSV Trade-Off

The project is in-memory only, as required. `data/seed.csv` is a convenience script for quickly loading enough records for evaluation, not persistent storage. The evaluator can select menu option `4` and load all sample students, courses, and enrollments without typing hundreds of records manually.

## Code Quality Invariants

- Entity classes own state and simple invariants.
- Service classes own `ArrayList` storage and business rules.
- `Main` owns menu display and user input flow.
- `CSVParser` owns seed CSV parsing.
- `ConsolePresenter` owns table formatting.
- Invalid input is reported with clear messages instead of stack traces.

## Architectural Comparison

| Component | LearnTrack Choice | Why |
| --- | --- | --- |
| In-memory storage | Service-owned `ArrayList` collections | Matches the assignment goal of practicing collection operations directly. |
| ID creation | Static `IdGenerator` | Demonstrates static members with one shared counter source. |
| Person model | `Person`, `Student`, `Trainer` | Demonstrates inheritance, `super`, and method overriding. |
| Error handling | Custom exceptions plus CLI catch blocks | Keeps the app running after expected input mistakes. |
| Output rendering | `ConsolePresenter` | Keeps table formatting out of service logic. |
