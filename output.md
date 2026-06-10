# LearnTrack CLI Menu Guide

This file explains how to run the console app, ingest seed data, and verify the realistic student, trainer, course, enrollment, and edge-case workflows.

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
5. Trainer Operations (Create / View / Courses / Students / Batches)
9. Exit Application Engine
```

## Option 4: Auto Ingest Seed Data

Use this first during evaluation:

```text
Select Operational Code: 4
```

Expected result:

```text
Loading CSV commands from: data/seed.csv
POST (Student) successfully loaded with ID: 1001
...
POST (Student) successfully loaded with ID: 1220
POST (Trainer) successfully loaded with ID: 4001
...
POST (Trainer) successfully loaded with ID: 4008
POST (Course) successfully loaded with ID: 2001
...
POST (Course) successfully loaded with ID: 2012
POST (Enrollment) successfully loaded with ID: 3001
...
POST (Enrollment) successfully loaded with ID: 3240
```

The seed contains 220 students, 8 trainers, 12 courses, and 240 enrollments. Every course batch has a capacity of 60.

## Option 1: Student Operations

```text
Select Operational Code: 1
```

Student submenu:

```text
A. Create Student Profile
B. Display Student Ledger
C. Deactivate Student Profile
D. Search Student by ID
E. View Student Full Details
F. View Student In-Progress Courses
G. View Student Completed Courses
H. View Student Cancelled / Rejected Courses
```

### 1A: Create Student

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

```text
Option: B
```

Expected output: table with student ID, display name, and active status.

### 1C: Deactivate Student

```text
Option: C
Enter Target Student ID: 1002
```

Expected output:

```text
Student status deactivated.
```

After this, enrolling student `1002` into a new active course should show:

```text
ERROR: Validation error: Cannot enroll an inactive student.
```

### 1D: Search Student By ID

```text
Option: D
Enter Target Student ID: 1001
```

Expected output: one-row profile table for student `1001`.

### 1E: View Student Full Details

```text
Option: E
Enter Target Student ID: 1001
```

Expected output:

- Student profile table
- Summary table: total, in-progress, completed, cancelled, rejected
- Course table showing enrollment ID, course, trainer, batch, and status

### 1F: View In-Progress Courses

```text
Option: F
Enter Target Student ID: 1001
```

Expected output: only `ACTIVE` enrollments for that student.

### 1G: View Completed Courses

```text
Option: G
Enter Target Student ID: 1001
```

Expected output: only `COMPLETED` enrollments for that student.

### 1H: View Cancelled / Rejected Courses

```text
Option: H
Enter Target Student ID: 1001
```

Expected output: cancelled or rejected enrollment history.

## Option 2: Course Operations

```text
Select Operational Code: 2
```

Course submenu:

```text
A. Create Course Blueprint
B. Display Course Catalog
C. Change Course Active Status
D. View Course Full Details
E. View Courses by Trainer
```

### 2A: Create Course

Create a trainer first, or use a seeded trainer such as `4001`.

```text
Option: A
Course Title: Core Java Practice
Description: Java basics and collections
Duration (Weeks): 6
Trainer ID: 4001
Batch Name: Cohort-Z
```

Expected output:

```text
Created course with ID: 2013
```

The batch capacity is automatically fixed at `60`.

### 2B: Display Courses

```text
Option: B
```

Expected output: table with course ID, title, trainer, batch, seats, duration, and active flag.

### 2C: Activate Or Deactivate Course

```text
Option: C
Course ID: 2002
Active (true/false): false
```

Expected output:

```text
Course deactivated. Cancelled related enrollments: <count>
```

When a course is deactivated, all enrollments for that course are shown as `CANCELLED`.

### 2D: View Course Full Details

```text
Option: D
Course ID: 2002
```

Expected output:

- Course detail table
- Enrollment table for students in that course

### 2E: View Courses By Trainer

```text
Option: E
Trainer ID: 4001
```

Expected output: courses handled by trainer `4001`, including batch and seat counts.

## Option 3: Enrollment Operations

```text
Select Operational Code: 3
```

Enrollment submenu:

```text
A. Enroll Student in Course
B. View Enrollments for Student
C. Update Enrollment Status
D. Display All Enrollment Ledger
E. View Enrollments for Course
F. View Course Capacity Dashboard
```

### 3A: Enroll Student

```text
Option: A
Student ID: 1003
Course ID: 2004
```

Expected output:

```text
Enrolled student under ID: 3241
```

If the course already has 60 accepted students, the CLI asks:

```text
Trainer accepts over-capacity enrollment (true/false):
```

If the trainer enters `true`, the student is accepted. If the trainer enters `false`, the request is saved as `REJECTED`.

### 3B: View Enrollments For Student

```text
Option: B
Student ID: 1001
```

Expected output: only student `1001` enrollments with course, trainer, batch, and status.

### 3C: Update Enrollment Status

```text
Option: C
Enrollment ID: 3001
Status (PENDING / ACTIVE / COMPLETED / CANCELLED / REJECTED): COMPLETED
```

Expected output:

```text
Enrollment status updated.
```

Allowed statuses are `PENDING`, `ACTIVE`, `COMPLETED`, `CANCELLED`, and `REJECTED`.

### 3D: Display All Enrollments

```text
Option: D
```

Expected output: table with all enrollments, student names, course names, trainers, batches, and statuses.

### 3E: View Enrollments For Course

```text
Option: E
Course ID: 2004
```

Expected output: students enrolled in the selected course.

### 3F: View Course Capacity Dashboard

```text
Option: F
```

Expected output: all courses with accepted seats shown as `<accepted>/60` and capacity marked `OPEN` or `FULL`.

## Option 5: Trainer Operations

```text
Select Operational Code: 5
```

Trainer submenu:

```text
A. Create Trainer Profile
B. Display Trainer Ledger
C. View Trainer Full Details
D. View Courses Handled by Trainer
E. View Students Under Trainer
F. View Batches Handled by Trainer
G. Change Trainer Active Status
```

### 5A: Create Trainer

```text
Option: A
First Name: Anil
Last Name: Rao
Email: anil.rao@example.com
Specialization: Backend Java
```

Expected output:

```text
Created trainer profile with ID: 4009
```

### 5B: Display Trainers

```text
Option: B
```

Expected output: table of trainers and active status.

### 5C: View Trainer Full Details

```text
Option: C
Trainer ID: 4001
```

Expected output:

- Trainer profile
- Courses handled by the trainer
- Students under those courses

### 5D: View Courses Handled By Trainer

```text
Option: D
Trainer ID: 4001
```

Expected output: course, batch, seats, and active status.

### 5E: View Students Under Trainer

```text
Option: E
Trainer ID: 4001
```

Expected output: student names grouped through the trainer's course assignments.

### 5F: View Batches Handled By Trainer

```text
Option: F
Trainer ID: 4001
```

Expected output: distinct batches handled by that trainer.

### 5G: Activate Or Deactivate Trainer

```text
Option: G
Trainer ID: 4001
Active (true/false): false
```

Expected output:

```text
Trainer active status updated.
```

Inactive trainers cannot be assigned to newly created courses.

## Option 9: Exit

```text
Select Operational Code: 9
```

Expected output:

```text
System execution safely terminated. Goodbye.
```

## Edge-Case Checks

### Missing Student

```text
Select Operational Code: 1
Option: D
Enter Target Student ID: 9999
```

Expected output:

```text
ERROR: Data violation: Target Student with ID 9999 could not be found.
```

### Invalid Number

```text
Select Operational Code: 2
Option: C
Course ID: abc
```

Expected output:

```text
ERROR: Field must be an integer: Course ID
```

### Invalid Enrollment Status

```text
Select Operational Code: 3
Option: C
Enrollment ID: 3001
Status (PENDING / ACTIVE / COMPLETED / CANCELLED / REJECTED): PAUSED
```

Expected output:

```text
ERROR: Enrollment status must be PENDING, ACTIVE, COMPLETED, CANCELLED, or REJECTED.
```

### Duplicate Active Enrollment

Try enrolling the same active student into the same active course twice.

Expected output:

```text
ERROR: Validation error: Student is already enrolled in this course.
```

### Inactive Course Enrollment

Deactivate a course with option `2C`, then try option `3A` for that course.

Expected output:

```text
ERROR: Validation error: Cannot enroll in an inactive course.
```

### Inactive Trainer Assignment

Deactivate a trainer with option `5G`, then try creating a course with that trainer.

Expected output:

```text
ERROR: Validation error: Cannot assign an inactive trainer.
```
