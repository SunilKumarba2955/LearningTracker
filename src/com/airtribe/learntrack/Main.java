package com.airtribe.learntrack;

import com.airtribe.learntrack.entity.Course;
import com.airtribe.learntrack.entity.Enrollment;
import com.airtribe.learntrack.entity.Student;
import com.airtribe.learntrack.exception.InvalidInputException;
import com.airtribe.learntrack.exception.LearnTrackException;
import com.airtribe.learntrack.service.CourseService;
import com.airtribe.learntrack.service.EnrollmentService;
import com.airtribe.learntrack.service.StudentService;
import com.airtribe.learntrack.ui.ConsolePresenter;
import com.airtribe.learntrack.util.CSVParser;
import com.airtribe.learntrack.util.IdGenerator;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Main CLI orchestrator for LearnTrack.
 */
public class Main {
    private static final String DEFAULT_SEED_PATH = "data/seed.csv";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private static final StudentService studentService = new StudentService();
    private static final CourseService courseService = new CourseService();
    private static final EnrollmentService enrollmentService = new EnrollmentService(studentService, courseService);
    private static boolean defaultSeedLoaded;

    /**
     * Starts the LearnTrack command-line application.
     *
     * @param args optional CSV script paths to execute before interactive mode
     */
    public static void main(String[] args) {
        printBootstrapBanner();

        for (String scriptPath : args) {
            boolean loaded = executeCSVBulkLoad(scriptPath);
            if (loaded && DEFAULT_SEED_PATH.equals(scriptPath.replace('\\', '/'))) {
                defaultSeedLoaded = true;
            }
        }

        runInteractiveLoop(new Scanner(System.in));
    }

    private static void printBootstrapBanner() {
        System.out.println("====================================================");
        System.out.println("     LEARNTRACK SYSTEM ENGINE BOOTSTRAP ACTIVE      ");
        System.out.println("====================================================");
    }

    private static void ingestDefaultSeed() {
        if (defaultSeedLoaded) {
            System.out.println("Default seed data is already loaded in memory.");
            return;
        }

        File seedFile = new File(DEFAULT_SEED_PATH);
        if (seedFile.exists()) {
            defaultSeedLoaded = executeCSVBulkLoad(DEFAULT_SEED_PATH);
        } else {
            System.out.println("Default seed script not found at data/seed.csv.");
        }
    }

    private static void runInteractiveLoop(Scanner scanner) {
        boolean running = true;
        while (running) {
            try {
                printMainMenu();
                System.out.print("Select Operational Code: ");
                if (!scanner.hasNextLine()) {
                    System.out.println();
                    System.out.println("No interactive input stream detected. Shutting down CLI loop.");
                    running = false;
                    continue;
                }
                String input = scanner.nextLine().trim();
                if ("9".equals(input)) {
                    System.out.println("System execution safely terminated. Goodbye.");
                    running = false;
                } else {
                    processInput(input, scanner);
                }
            } catch (LearnTrackException | IllegalArgumentException exception) {
                System.out.println();
                System.out.println("ERROR: " + exception.getMessage());
            }
        }
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("--- LEARNTRACK CENTRAL CONFIGURATION PANEL ---");
        System.out.println("1. Student Operations (Create / View / Search / Deactivate)");
        System.out.println("2. Course Operations (Create / View List / Change Status)");
        System.out.println("3. Enrollment Operations (Enroll / View By Student / Update Status)");
        System.out.println("4. Auto Ingest Default Seed Data (data/seed.csv)");
        System.out.println("9. Exit Application Engine");
    }

    private static void processInput(String input, Scanner scanner) {
        switch (input) {
            case "1":
                handleStudentOperations(scanner);
                break;
            case "2":
                handleCourseOperations(scanner);
                break;
            case "3":
                handleEnrollmentOperations(scanner);
                break;
            case "4":
                ingestDefaultSeed();
                break;
            default:
                throw new InvalidInputException("Supplied operation code is invalid.");
        }
    }

    private static void handleStudentOperations(Scanner scanner) {
        System.out.println();
        System.out.println("--- Student Management Console ---");
        System.out.println("A. Create Student Profile");
        System.out.println("B. Display Student Ledger");
        System.out.println("C. Deactivate Student Profile");
        System.out.println("D. Search Student by ID");
        System.out.print("Option: ");

        String selection = scanner.nextLine().toUpperCase(Locale.ROOT).trim();
        switch (selection) {
            case "A":
                createStudent(scanner);
                break;
            case "B":
                printStudents();
                break;
            case "C":
                deactivateStudent(scanner);
                break;
            case "D":
                searchStudentById(scanner);
                break;
            default:
                throw new InvalidInputException("Selection is invalid.");
        }
    }

    private static void createStudent(Scanner scanner) {
        String firstName = readRequired(scanner, "First Name: ");
        String lastName = readRequired(scanner, "Last Name: ");
        System.out.print("Email (Press Enter to omit): ");
        String email = scanner.nextLine().trim();
        String batch = readRequired(scanner, "Batch Allocation: ");

        int nextId = IdGenerator.getNextStudentId();
        Student student = email.isEmpty()
                ? new Student(nextId, firstName, lastName, batch, true)
                : new Student(nextId, firstName, lastName, email, batch, true);
        studentService.addStudent(student);
        System.out.println("Created student profile with ID: " + nextId);
    }

    private static void printStudents() {
        List<String[]> dataRows = new ArrayList<>();
        for (Student student : studentService.listAllStudents()) {
            dataRows.add(new String[] {
                    String.valueOf(student.getId()),
                    student.getDisplayName(),
                    String.valueOf(student.isActive())
            });
        }
        ConsolePresenter.printTable(new String[] {"Student ID", "Full Display Name", "Active Status"}, dataRows);
    }

    private static void deactivateStudent(Scanner scanner) {
        int targetId = readInt(scanner, "Enter Target Student ID: ");
        studentService.deactivateStudent(targetId);
        System.out.println("Student status deactivated.");
    }

    private static void searchStudentById(Scanner scanner) {
        int targetId = readInt(scanner, "Enter Target Student ID: ");
        Student student = studentService.findStudentById(targetId);
        List<String[]> dataRows = new ArrayList<>();
        dataRows.add(new String[] {
                String.valueOf(student.getId()),
                student.getFirstName() + " " + student.getLastName(),
                student.getEmail(),
                student.getBatch(),
                String.valueOf(student.isActive())
        });
        ConsolePresenter.printTable(
                new String[] {"Student ID", "Name", "Email", "Batch", "Active"},
                dataRows
        );
    }

    private static void handleCourseOperations(Scanner scanner) {
        System.out.println();
        System.out.println("--- Course Management Console ---");
        System.out.println("A. Create Course Blueprint");
        System.out.println("B. Display Course Catalog");
        System.out.println("C. Change Course Active Status");
        System.out.print("Option: ");

        String selection = scanner.nextLine().toUpperCase(Locale.ROOT).trim();
        switch (selection) {
            case "A":
                createCourse(scanner);
                break;
            case "B":
                printCourses();
                break;
            case "C":
                changeCourseStatus(scanner);
                break;
            default:
                throw new InvalidInputException("Selection is invalid.");
        }
    }

    private static void createCourse(Scanner scanner) {
        String name = readRequired(scanner, "Course Title: ");
        String description = readRequired(scanner, "Description: ");
        int duration = readInt(scanner, "Duration (Weeks): ");

        int nextId = IdGenerator.getNextCourseId();
        courseService.addCourse(new Course(nextId, name, description, duration, true));
        System.out.println("Created course with ID: " + nextId);
    }

    private static void printCourses() {
        List<String[]> dataRows = new ArrayList<>();
        for (Course course : courseService.listAllCourses()) {
            dataRows.add(new String[] {
                    String.valueOf(course.getId()),
                    course.getCourseName(),
                    course.getDurationInWeeks() + " Weeks",
                    String.valueOf(course.isActive())
            });
        }
        ConsolePresenter.printTable(new String[] {"Course ID", "Title", "Duration", "Active Status"}, dataRows);
    }

    private static void changeCourseStatus(Scanner scanner) {
        int courseId = readInt(scanner, "Course ID: ");
        boolean active = readBoolean(scanner, "Active (true/false): ");
        courseService.setCourseStatus(courseId, active);
        System.out.println("Course active status updated.");
    }

    private static void handleEnrollmentOperations(Scanner scanner) {
        System.out.println();
        System.out.println("--- Enrollment Management Console ---");
        System.out.println("A. Enroll Student in Course");
        System.out.println("B. View Enrollments for Student");
        System.out.println("C. Update Enrollment Status");
        System.out.println("D. Display All Enrollment Ledger");
        System.out.print("Option: ");

        String selection = scanner.nextLine().toUpperCase(Locale.ROOT).trim();
        switch (selection) {
            case "A":
                enrollStudent(scanner);
                break;
            case "B":
                printEnrollmentsForStudent(scanner);
                break;
            case "C":
                updateEnrollmentStatus(scanner);
                break;
            case "D":
                printEnrollments();
                break;
            default:
                throw new InvalidInputException("Selection is invalid.");
        }
    }

    private static void enrollStudent(Scanner scanner) {
        int studentId = readInt(scanner, "Student ID: ");
        int courseId = readInt(scanner, "Course ID: ");

        int nextId = IdGenerator.getNextEnrollmentId();
        enrollmentService.enrollStudent(new Enrollment(
                nextId,
                studentId,
                courseId,
                LocalDate.now().toString(),
                "ACTIVE"
        ));
        System.out.println("Enrolled student under ID: " + nextId);
    }

    private static void printEnrollments() {
        List<String[]> dataRows = new ArrayList<>();
        for (Enrollment enrollment : enrollmentService.listAllEnrollments()) {
            Student student = studentService.findStudentById(enrollment.getStudentId());
            Course course = courseService.findCourseById(enrollment.getCourseId());
            dataRows.add(new String[] {
                    String.valueOf(enrollment.getId()),
                    student.getFirstName() + " " + student.getLastName(),
                    course.getCourseName(),
                    enrollment.getStatus()
            });
        }
        ConsolePresenter.printTable(new String[] {"Enrollment ID", "Student Name", "Course Title", "Status"}, dataRows);
    }

    private static void printEnrollmentsForStudent(Scanner scanner) {
        int studentId = readInt(scanner, "Student ID: ");
        Student student = studentService.findStudentById(studentId);
        List<String[]> dataRows = new ArrayList<>();
        for (Enrollment enrollment : enrollmentService.listEnrollmentsByStudentId(studentId)) {
            Course course = courseService.findCourseById(enrollment.getCourseId());
            dataRows.add(new String[] {
                    String.valueOf(enrollment.getId()),
                    student.getFirstName() + " " + student.getLastName(),
                    course.getCourseName(),
                    enrollment.getStatus()
            });
        }
        ConsolePresenter.printTable(new String[] {"Enrollment ID", "Student Name", "Course Title", "Status"}, dataRows);
    }

    private static void updateEnrollmentStatus(Scanner scanner) {
        int enrollmentId = readInt(scanner, "Enrollment ID: ");
        String status = readEnrollmentStatus(scanner, "Status (ACTIVE / COMPLETED / CANCELLED): ");
        enrollmentService.updateStatus(enrollmentId, status);
        System.out.println("Enrollment status updated.");
    }

    private static boolean executeCSVBulkLoad(String path) {
        String resolvedPath = requireText(path, "CSV script path");
        System.out.println();
        System.out.println("Loading CSV commands from: " + resolvedPath);

        try {
            List<String[]> operations = CSVParser.parseCSV(resolvedPath);
            for (String[] operation : operations) {
                executeCSVOperation(operation);
            }
            return true;
        } catch (IOException exception) {
            System.out.println();
            System.out.println("CSV Execution Failed: " + exception.getMessage());
            return false;
        } catch (LearnTrackException | IllegalArgumentException exception) {
            System.out.println();
            System.out.println("CSV Execution Failed: " + exception.getMessage());
            return false;
        }
    }

    private static void executeCSVOperation(String[] operation) {
        if (operation.length == 0 || operation[0].trim().isEmpty()) {
            return;
        }

        String commandType = removeByteOrderMark(operation[0].trim());
        if (commandType.isEmpty() || commandType.startsWith("#")) {
            return;
        }
        commandType = commandType.toUpperCase(Locale.ROOT);
        switch (commandType) {
            case "POST":
                executePostCSV(operation);
                break;
            case "PUT":
                executePutCSV(operation);
                break;
            case "GET":
                executeGetCSV(operation);
                break;
            default:
                System.out.println("  -> Unknown command skipped: " + commandType);
        }
    }

    private static void executePostCSV(String[] operation) {
        String entityType = getCell(operation, 1, "entityType").toLowerCase(Locale.ROOT);
        int entityId = parseIntCell(operation, 2, "entityId");

        switch (entityType) {
            case "student":
                studentService.addStudent(newStudentFromCSV(operation, entityId));
                IdGenerator.syncStudentId(entityId);
                System.out.println("     POST (Student) successfully loaded with ID: " + entityId);
                break;
            case "course":
                courseService.addCourse(new Course(
                        entityId,
                        getCell(operation, 3, "courseName"),
                        getCell(operation, 4, "description"),
                        parseIntCell(operation, 5, "durationInWeeks"),
                        parseBooleanCell(operation, 6, "active")
                ));
                IdGenerator.syncCourseId(entityId);
                System.out.println("     POST (Course) successfully loaded with ID: " + entityId);
                break;
            case "enrollment":
                enrollmentService.enrollStudent(new Enrollment(
                        entityId,
                        parseIntCell(operation, 3, "studentId"),
                        parseIntCell(operation, 4, "courseId"),
                        getCell(operation, 5, "enrollmentDate"),
                        getCell(operation, 6, "status")
                ));
                IdGenerator.syncEnrollmentId(entityId);
                System.out.println("     POST (Enrollment) successfully loaded with ID: " + entityId);
                break;
            default:
                throw new InvalidInputException("Invalid configuration entity: " + entityType);
        }
    }

    private static void executePutCSV(String[] operation) {
        String entityType = getCell(operation, 1, "entityType").toLowerCase(Locale.ROOT);
        int entityId = parseIntCell(operation, 2, "entityId");

        switch (entityType) {
            case "student":
                studentService.findStudentById(entityId);
                studentService.updateStudent(newStudentFromCSV(operation, entityId));
                System.out.println("     PUT (Student) updated successfully: " + entityId);
                break;
            case "course":
                courseService.setCourseStatus(entityId, parseBooleanCell(operation, 3, "active"));
                System.out.println("     PUT (Course) status updated successfully: " + entityId);
                break;
            case "enrollment":
                enrollmentService.updateStatus(entityId, normalizeEnrollmentStatus(getCell(operation, 3, "status")));
                System.out.println("     PUT (Enrollment) updated successfully: " + entityId);
                break;
            default:
                throw new InvalidInputException("Invalid update entity: " + entityType);
        }
    }

    private static void executeGetCSV(String[] operation) {
        String entityType = getCell(operation, 1, "entityType").toLowerCase(Locale.ROOT);
        int entityId = parseIntCell(operation, 2, "entityId");

        switch (entityType) {
            case "student":
                Student student = studentService.findStudentById(entityId);
                System.out.println("     GET (Student) " + entityId + " -> " + student.getDisplayName());
                break;
            case "course":
                Course course = courseService.findCourseById(entityId);
                System.out.println("     GET (Course) " + entityId + " -> " + course.getCourseName());
                break;
            case "enrollment":
                Enrollment enrollment = enrollmentService.findEnrollmentById(entityId);
                System.out.println("     GET (Enrollment) " + entityId + " -> " + enrollment.getStatus());
                break;
            default:
                throw new InvalidInputException("Invalid query entity type: " + entityType);
        }
    }

    private static Student newStudentFromCSV(String[] operation, int entityId) {
        String firstName = getCell(operation, 3, "firstName");
        String lastName = getCell(operation, 4, "lastName");
        String email = getCell(operation, 5, "email");
        String batch = getCell(operation, 6, "batch");
        boolean active = parseBooleanCell(operation, 7, "active");

        if (email.isEmpty() || "N/A".equalsIgnoreCase(email)) {
            return new Student(entityId, firstName, lastName, batch, active);
        }
        return new Student(entityId, firstName, lastName, email, batch, active);
    }

    private static String readRequired(Scanner scanner, String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new InvalidInputException("Interactive input stream ended unexpectedly.");
        }
        return requireText(scanner.nextLine(), prompt.replace(":", ""));
    }

    private static int readInt(Scanner scanner, String prompt) {
        String value = readRequired(scanner, prompt);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new InvalidInputException("Field must be an integer: " + cleanPrompt(prompt));
        }
    }

    private static boolean readBoolean(Scanner scanner, String prompt) {
        return parseBoolean(readRequired(scanner, prompt), cleanPrompt(prompt));
    }

    private static String readEnrollmentStatus(Scanner scanner, String prompt) {
        return normalizeEnrollmentStatus(readRequired(scanner, prompt));
    }

    private static String getCell(String[] operation, int index, String fieldName) {
        if (index >= operation.length) {
            throw new InvalidInputException("CSV command is missing required field: " + fieldName);
        }
        return requireText(operation[index], fieldName);
    }

    private static int parseIntCell(String[] operation, int index, String fieldName) {
        try {
            return Integer.parseInt(getCell(operation, index, fieldName));
        } catch (NumberFormatException exception) {
            throw new InvalidInputException("CSV field must be an integer: " + fieldName);
        }
    }

    private static boolean parseBooleanCell(String[] operation, int index, String fieldName) {
        return parseBoolean(getCell(operation, index, fieldName), fieldName);
    }

    private static boolean parseBoolean(String value, String fieldName) {
        String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(normalizedValue)) {
            return true;
        }
        if ("false".equals(normalizedValue)) {
            return false;
        }
        throw new InvalidInputException("Field must be true or false: " + fieldName);
    }

    private static String normalizeEnrollmentStatus(String status) {
        String normalizedStatus = requireText(status, "Enrollment status").toUpperCase(Locale.ROOT);
        if (STATUS_ACTIVE.equals(normalizedStatus)
                || STATUS_COMPLETED.equals(normalizedStatus)
                || STATUS_CANCELLED.equals(normalizedStatus)) {
            return normalizedStatus;
        }
        throw new InvalidInputException("Enrollment status must be ACTIVE, COMPLETED, or CANCELLED.");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(fieldName + " is required.");
        }
        return value.trim();
    }

    private static String removeByteOrderMark(String value) {
        if (!value.isEmpty() && value.charAt(0) == '\uFEFF') {
            return value.substring(1);
        }
        return value;
    }

    private static String cleanPrompt(String prompt) {
        return prompt.replace(":", "").trim();
    }
}
