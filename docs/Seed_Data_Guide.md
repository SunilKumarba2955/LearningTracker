# Seed Data Guide

This project uses command-script CSV files for bulk loading. These files are different from plain entity export CSVs because each row starts with an operation such as `POST`, `PUT`, `GET`, `START_TX`, `COMMIT_TX`, or `ROLLBACK_TX`.

## Main Large Seed

Use:

```text
data\seed.csv
```

Contents:

- 220 students with IDs `1001` through `1220`
- 12 courses with IDs `2001` through `2012`
- 240 enrollments with IDs `3001` through `3240`
- Safe `GET`, `PUT`, `DELETE`, and `ROLLBACK_TX` verification commands

The app auto-loads this file at startup when it exists:

```powershell
java -cp bin com.airtribe.learntrack.Main
```

You can also load it from menu option `4`:

```text
Select Operational Code: 4
Provide system path to target CSV script file: data\seed.csv
```

## Exception Check Scripts

These scripts are intentionally invalid. Run one at a time from menu option `4`. Each script should produce a clear error message and rollback any active transaction frames.

```text
data\exception-checks\missing-student.csv
data\exception-checks\missing-course.csv
data\exception-checks\invalid-status.csv
data\exception-checks\invalid-student-email.csv
data\exception-checks\invalid-course-duration.csv
data\exception-checks\invalid-boolean.csv
data\exception-checks\invalid-entity.csv
```

Expected behavior:

- The CLI prints `CSV Execution Failed: ...`
- The CLI prints `Rolling back active transaction contexts...`
- The CLI prints `State restored successfully.`

Do not merge the exception rows into `data\seed.csv`; the default seed should remain a valid startup script.
