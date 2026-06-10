# LearnTrack Compilation and Execution Instructions

LearnTrack is built with Core Java. It does not require Maven, Gradle, or any external build wrapper.

## Installed Prerequisites

- Java Development Kit (JDK) 17 or higher
- Docker Desktop or Docker Engine with Docker Compose, if using the containerized workflow

## Verifying Local Installation

Run these commands from a terminal:

```bash
javac -version
java -version
```

Both commands should print version information. If either command is not found, install a JDK and make sure the JDK `bin` directory is available on your `PATH`.

## Compilation Command Sequence

From the repository root, compile all Java source files into the `bin` directory.

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

## Running the Application

After compilation, run the main class with:

```bash
java -cp bin com.airtribe.learntrack.Main
```

## Docker Compose Workflow

The project includes a Docker Compose setup for running LearnTrack inside a JDK 21 container. This keeps local execution consistent without requiring Java to be installed directly on the host machine.

Build and run the application container:

```bash
docker compose up --build
```

For the interactive CLI workflow, run the service with an attached terminal:

```bash
docker compose run --rm learntrack
```

The container compiles Java files from `src` into `out` and runs:

```bash
java -cp out com.airtribe.learntrack.Main
```

At the scaffold stage, there may be no Java source files or no `Main` class yet. In that case, the container exits cleanly with a message explaining what is missing.

To run one-off commands inside the project container:

```bash
docker compose run --rm learntrack javac -version
```
