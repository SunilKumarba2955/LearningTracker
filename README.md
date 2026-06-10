# LearnTrack: Student and Course Management System

LearnTrack is a console-based Student and Course Management System built with Core Java. It focuses on the fundamentals from the assignment brief: packages, classes, constructors, encapsulation, inheritance, method overriding, `ArrayList` collection handling, custom exceptions, and a menu-driven CLI.

## Features

- Manage students, courses, and enrollments from a terminal menu
- Add, list, search, update, deactivate, and enroll records in memory
- Store runtime data in service-owned `ArrayList` collections
- Use `Person` as a base class and `Student` / `Trainer` as child classes
- Generate IDs with a static `IdGenerator` utility
- Handle invalid menu input, missing IDs, invalid emails, invalid booleans, and unsupported enrollment statuses with clean messages
- Load the large evaluator seed file from menu option `4`
- Print readable ASCII tables through `ConsolePresenter`

## Class Diagram

```text
                 +----------------------+
                 |       Person         |
                 +----------------------+
                 | id                   |
                 | firstName            |
                 | lastName             |
                 | email                |
                 +----------------------+
                 | getDisplayName()     |
                 +----------^-----------+
                            |
              +-------------+-------------+
              |                           |
      +-------+--------+          +-------+--------+
      |    Student     |          |    Trainer     |
      +----------------+          +----------------+
      | batch          |          | specialization |
      | active         |          +----------------+
      +----------------+

      +----------------+          +----------------+
      |     Course     |          |   Enrollment   |
      +----------------+          +----------------+
      | id             |          | id             |
      | courseName     |          | studentId      |
      | description    |          | courseId       |
      | durationWeeks  |          | enrollmentDate |
      | active         |          | status         |
      +----------------+          +----------------+

      StudentService       CourseService       EnrollmentService
      ArrayList<Student>   ArrayList<Course>   ArrayList<Enrollment>
```

## Repository Layout

```text
src/com/airtribe/learntrack/
  Main.java
  entity/
  exception/
  service/
  ui/
  util/
data/
docs/
```

## Prerequisites

- JDK 17 or higher
- Optional: Docker Desktop or Docker Engine with Docker Compose

## Compile

PowerShell:

```powershell
New-Item -ItemType Directory -Force -Path bin
javac -d bin (Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName })
```

Bash:

```bash
mkdir -p bin
javac -d bin $(find src -name "*.java")
```

## Run

```bash
java -cp bin com.airtribe.learntrack.Main
```

The app opens the interactive menu. Select option `4` to load `data/seed.csv` into memory.

## Docker Compose

```bash
docker compose run --rm learntrack
```

## Seed Data

The default seed file is:

```text
data/seed.csv
```

It contains:

- 220 students
- 12 courses
- 240 enrollments
- Safe `GET` and `PUT` verification rows

From the main menu, use:

```text
Select Operational Code: 4
```

## More Help

- `output.md`: complete CLI menu guide and verification examples
- `docs/Setup_Instructions.md`: local and Docker setup
- `docs/JVM_Basics.md`: JDK, JRE, JVM, bytecode, and WORA
- `docs/Design_Notes.md`: ArrayList, static utility, inheritance, and clean code notes
