# Seed Data Guide

LearnTrack stores data in memory with `ArrayList` collections. The seed CSV is only a convenience file for loading many sample records during evaluation.

## Main Seed File

```text
data\seed.csv
```

Contents:

- 220 students with IDs `1001` through `1220`
- 12 courses with IDs `2001` through `2012`
- 240 enrollments with IDs `3001` through `3240`
- Safe `GET` and `PUT` verification rows

## Load Seed Data From the Menu

Compile and run the app, then choose menu option `4`.

```text
Select Operational Code: 4
```

The app loads `data/seed.csv` automatically. No file path is required.

## Exception Check Scripts

These scripts are intentionally invalid and are useful for manual testing from a fresh process with a command-line argument.

```text
data\exception-checks\missing-student.csv
data\exception-checks\missing-course.csv
data\exception-checks\invalid-status.csv
data\exception-checks\invalid-student-email.csv
data\exception-checks\invalid-course-duration.csv
data\exception-checks\invalid-boolean.csv
data\exception-checks\invalid-entity.csv
```

Example:

```powershell
java -cp bin com.airtribe.learntrack.Main data\exception-checks\invalid-status.csv
```

Expected behavior:

- The CLI prints `CSV Execution Failed: ...`
- The JVM process does not crash with a stack trace for expected validation errors
