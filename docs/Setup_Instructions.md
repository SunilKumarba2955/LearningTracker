# LearnTrack Compilation and Execution Instructions

LearnTrack is built with Core Java. It does not require Maven, Gradle, or any external build wrapper.

## Installed Prerequisites

- Java Development Kit (JDK) 17 or higher

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
