# LearnTrack CLI Menu Guide

This file explains how to run the console app, load the evaluator seed data, and verify every menu option.

## Compile And Run

PowerShell:

```powershell
New-Item -ItemType Directory -Force -Path bin
javac -d bin (Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName })
java -cp bin com.airtribe.learntrack.Main
```

Docker Compose:

```powershell
docker compose run --rm learntrack
```

## Main Menu

```text
1. Student Operations (Create / View / Search / Deactivate)
2. Course Operations (Create / View List / Change Status)
3. Enrollment Operations (Enroll / View By Student / Update Status)
4. Auto Ingest Default Seed Data (data/seed.csv)
9. Exit Application Engine
```

## Option 4: Auto Ingest Seed Data

Use this first during evaluation.

```text
Select Operational Code: 4
```

Expected result:

```text
Loading CSV commands from: data/seed.csv
POST (Student) successfully loaded with ID: 1001
...
POST (Student) successfully loaded with ID: 1220
POST (Course) successfully loaded with ID: 2001
...
POST (Course) successfully loaded with ID: 2012
POST (Enrollment) successfully loaded with ID: 3001
...
POST (Enrollment) successfully loaded with ID: 3240
```

The seed contains 220 students, 12 courses, and 240 enrollments. Load it once per app run.

## Option 1: Student Operations

Choose:

```text
Select Operational Code: 1
```

Student submenu:

```text
A. Create Student Profile
B. Display Student Ledger
C. Deactivate Student Profile
D. Search Student by ID
```

### 1A: Create Student

Example input:

```text
Option: A
First Name: Meera
Last Name: Joshi
Email (Press Enter to omit): meera.joshi@example.com
Batch Allocation: Cohort-Z
```

Expected output:

```text
Created student profile with ID: 1221
```

### 1B: Display Students

Example input:

```text
Option: B
```

Expected output: a table with student ID, full display name, and active status.

### 1C: Deactivate Student

Example input:

```text
Option: C
Enter Target Student ID: 1002
```

Expected output:

```text
Student status deactivated.
```

### 1D: Search Student By ID

Example input:

```text
Option: D
Enter Target Student ID: 1001
```

Expected output: one-row table for student `1001`.

## Option 2: Course Operations

Choose:

```text
Select Operational Code: 2
```

Course submenu:

```text
A. Create Course Blueprint
B. Display Course Catalog
C. Change Course Active Status
```

### 2A: Create Course

Example input:

```text
Option: A
Course Title: Core Java Practice
Description: Java basics and collections
Duration (Weeks): 6
```

Expected output:

```text
Created course with ID: 2013
```

### 2B: Display Courses

Example input:

```text
Option: B
```

Expected output: a table with course ID, title, duration, and active status.

### 2C: Activate Or Deactivate Course

Example input:

```text
Option: C
Course ID: 2002
Active (true/false): false
```

Expected output:

```text
Course active status updated.
```

## Option 3: Enrollment Operations

Choose:

```text
Select Operational Code: 3
```

Enrollment submenu:

```text
A. Enroll Student in Course
B. View Enrollments for Student
C. Update Enrollment Status
D. Display All Enrollment Ledger
```

### 3A: Enroll Student

Example input:

```text
Option: A
Student ID: 1003
Course ID: 2004
```

Expected output:

```text
Enrolled student under ID: 3241
```

If the student is inactive, the course is inactive, the student/course ID does not exist, or the student is already enrolled in that course, the CLI prints a clean `ERROR:` message.

### 3B: View Enrollments For Student

Example input:

```text
Option: B
Student ID: 1001
```

Expected output: a table with only student `1001` enrollments.

### 3C: Update Enrollment Status

Example input:

```text
Option: C
Enrollment ID: 3001
Status (ACTIVE / COMPLETED / CANCELLED): COMPLETED
```

Expected output:

```text
Enrollment status updated.
```

Only `ACTIVE`, `COMPLETED`, and `CANCELLED` are accepted.

### 3D: Display All Enrollments

Example input:

```text
Option: D
```

Expected output: a table with all enrollments.

## Option 9: Exit

Example input:

```text
Select Operational Code: 9
```

Expected output:

```text
System execution safely terminated. Goodbye.
```

## Exception Checks

Try these after loading seed data:

```text
Select Operational Code: 1
Option: D
Enter Target Student ID: 9999
```

Expected output:

```text
ERROR: Data violation: Target Student with ID 9999 could not be found.
```

```text
Select Operational Code: 2
Option: C
Course ID: abc
```

Expected output:

```text
ERROR: Field must be an integer: Course ID
```

```text
Select Operational Code: 3
Option: C
Enrollment ID: 3001
Status (ACTIVE / COMPLETED / CANCELLED): PAUSED
```

Expected output:

```text
ERROR: Enrollment status must be ACTIVE, COMPLETED, or CANCELLED.
```
