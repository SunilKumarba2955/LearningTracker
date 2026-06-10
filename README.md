# LearnTrack: Enterprise-Core Student and Course Management Engine

LearnTrack is a modular Student and Course Management System built with Core Java. It demonstrates Clean Architecture, transactional in-memory state tracking, CSV command automation, and robust exception-safe CLI workflows without external dependencies.

## Key Features

- Clean Architecture implementation with inward-pointing dependencies
- Domain entities for students, trainers, courses, and enrollments
- Service layer for business validation and referential integrity
- ArrayList-backed in-memory services with transaction snapshots
- Zero-dependency RFC 4180-style CSV parser and writer
- CSV command scripting for automated seeding and verification
- Dynamic tabular console presenter for readable terminal output
- Custom unchecked exception hierarchy for boundary-level error handling

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

On startup, the application automatically loads `data/seed.csv` when the file is present. It then opens the interactive CLI menu.

## Docker Compose

```bash
docker compose up --build
```

For an attached interactive session:

```bash
docker compose run --rm learntrack
```

## CSV Command Script Format

The default seed file uses these command shapes:

```csv
START_TX
POST,student,1001,Alexander,Hamilton,alexander@email.com,Cohort-A,true
POST,course,2001,Java Software Design,Clean Code and SOLID patterns,12,true
POST,enrollment,3001,1001,2001,2026-06-10,ACTIVE
GET,student,1001
PUT,enrollment,3001,COMPLETED
DELETE,student,1001
COMMIT_TX
ROLLBACK_TX
```

## Documentation

- `docs/Setup_Instructions.md`: compilation, execution, and Docker workflow
- `docs/JVM_Basics.md`: JVM/JRE/JDK overview
- `docs/Design_Notes.md`: design rationale, performance notes, and architectural trade-offs

## Validation

The final system is validated with:

- JDK 17-compatible compilation
- Default seed script execution through the CLI
- Transaction commit and rollback behavior
- CSV parsing for command automation
- Console table rendering through interactive menu flows
