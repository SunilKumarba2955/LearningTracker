package com.airtribe.learntrack;

import com.airtribe.learntrack.entity.Course;
import com.airtribe.learntrack.entity.Enrollment;
import com.airtribe.learntrack.entity.Student;
import com.airtribe.learntrack.entity.Trainer;
import com.airtribe.learntrack.exception.InvalidInputException;
import com.airtribe.learntrack.exception.LearnTrackException;
import com.airtribe.learntrack.service.CourseService;
import com.airtribe.learntrack.service.EnrollmentService;
import com.airtribe.learntrack.service.StudentService;
import com.airtribe.learntrack.service.TrainerService;
import com.airtribe.learntrack.ui.ConsolePresenter;
import com.airtribe.learntrack.util.CSVParser;
import com.airtribe.learntrack.util.IdGenerator;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

/**
 * Main CLI orchestrator for LearnTrack.
 */
public class Main {
    private static final String DEFAULT_SEED_PATH = "data/seed.csv";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_REJECTED = "REJECTED";

    private static final StudentService studentService = new StudentService();
    private static final TrainerService trainerService = new TrainerService();
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
        System.out.println("5. Trainer Operations (Create / View / Courses / Students / Batches)");
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
            case "5":
                handleTrainerOperations(scanner);
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
        System.out.println("E. View Student Full Details");
        System.out.println("F. View Student In-Progress Courses");
        System.out.println("G. View Student Completed Courses");
        System.out.println("H. View Student Cancelled / Rejected Courses");
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
            case "E":
                printStudentFullDetails(scanner);
                break;
            case "F":
                printStudentEnrollmentsByStatus(scanner, STATUS_ACTIVE, "No in-progress courses found for this student.");
                break;
            case "G":
                printStudentEnrollmentsByStatus(scanner, STATUS_COMPLETED, "No completed courses found for this student.");
                break;
            case "H":
                printStudentInactiveEnrollments(scanner);
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
        printStudentProfile(student);
    }

    private static void printStudentFullDetails(Scanner scanner) {
        int targetId = readInt(scanner, "Enter Target Student ID: ");
        Student student = studentService.findStudentById(targetId);
        printStudentProfile(student);

        List<Enrollment> enrollments = enrollmentService.listEnrollmentsByStudentId(targetId);
        List<String[]> summaryRows = new ArrayList<>();
        summaryRows.add(new String[] {
                String.valueOf(enrollments.size()),
                String.valueOf(countEnrollmentsByStatus(enrollments, STATUS_ACTIVE)),
                String.valueOf(countEnrollmentsByStatus(enrollments, STATUS_COMPLETED)),
                String.valueOf(countEnrollmentsByStatus(enrollments, STATUS_CANCELLED)),
                String.valueOf(countEnrollmentsByStatus(enrollments, STATUS_REJECTED))
        });
        ConsolePresenter.printTable(
                new String[] {"Total", "In Progress", "Completed", "Cancelled", "Rejected"},
                summaryRows
        );

        printTableOrMessage(
                new String[] {"Enrollment ID", "Course", "Trainer", "Batch", "Status"},
                buildStudentEnrollmentRows(enrollments),
                "No enrollments found for this student."
        );
    }

    private static void printStudentEnrollmentsByStatus(Scanner scanner, String status, String emptyMessage) {
        int targetId = readInt(scanner, "Enter Target Student ID: ");
        Student student = studentService.findStudentById(targetId);
        List<Enrollment> enrollments = enrollmentService.listEnrollmentsByStudentIdAndStatus(targetId, status);
        System.out.println(student.getFirstName() + " " + student.getLastName() + " - " + status + " courses");
        printTableOrMessage(
                new String[] {"Enrollment ID", "Course", "Trainer", "Batch", "Status"},
                buildStudentEnrollmentRows(enrollments),
                emptyMessage
        );
    }

    private static void printStudentInactiveEnrollments(Scanner scanner) {
        int targetId = readInt(scanner, "Enter Target Student ID: ");
        Student student = studentService.findStudentById(targetId);
        List<Enrollment> inactiveEnrollments = new ArrayList<>();
        inactiveEnrollments.addAll(enrollmentService.listEnrollmentsByStudentIdAndStatus(targetId, STATUS_CANCELLED));
        inactiveEnrollments.addAll(enrollmentService.listEnrollmentsByStudentIdAndStatus(targetId, STATUS_REJECTED));
        System.out.println(student.getFirstName() + " " + student.getLastName() + " - inactive enrollment history");
        printTableOrMessage(
                new String[] {"Enrollment ID", "Course", "Trainer", "Batch", "Status"},
                buildStudentEnrollmentRows(inactiveEnrollments),
                "No cancelled or rejected courses found for this student."
        );
    }

    private static void printStudentProfile(Student student) {
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
        System.out.println("D. View Course Full Details");
        System.out.println("E. View Courses by Trainer");
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
            case "D":
                printCourseDetails(scanner);
                break;
            case "E":
                printCoursesByTrainer(scanner);
                break;
            default:
                throw new InvalidInputException("Selection is invalid.");
        }
    }

    private static void createCourse(Scanner scanner) {
        String name = readRequired(scanner, "Course Title: ");
        String description = readRequired(scanner, "Description: ");
        int duration = readInt(scanner, "Duration (Weeks): ");
        int trainerId = readInt(scanner, "Trainer ID: ");
        Trainer trainer = trainerService.findTrainerById(trainerId);
        if (!trainer.isActive()) {
            throw new InvalidInputException("Validation error: Cannot assign an inactive trainer.");
        }
        String batchName = readRequired(scanner, "Batch Name: ");

        int nextId = IdGenerator.getNextCourseId();
        courseService.addCourse(new Course(nextId, name, description, duration, trainerId, batchName, 60, true));
        System.out.println("Created course with ID: " + nextId);
    }

    private static void printCourses() {
        List<String[]> dataRows = new ArrayList<>();
        for (Course course : courseService.listAllCourses()) {
            Trainer trainer = findTrainerOrPlaceholder(course.getTrainerId());
            dataRows.add(new String[] {
                    String.valueOf(course.getId()),
                    course.getCourseName(),
                    trainer.getFirstName() + " " + trainer.getLastName(),
                    course.getBatchName(),
                    enrollmentService.countAcceptedEnrollmentsForCourse(course.getId()) + "/" + course.getMaxCapacity(),
                    course.getDurationInWeeks() + " Weeks",
                    String.valueOf(course.isActive())
            });
        }
        ConsolePresenter.printTable(
                new String[] {"Course ID", "Title", "Trainer", "Batch", "Seats", "Duration", "Active"},
                dataRows
        );
    }

    private static void changeCourseStatus(Scanner scanner) {
        int courseId = readInt(scanner, "Course ID: ");
        boolean active = readBoolean(scanner, "Active (true/false): ");
        courseService.setCourseStatus(courseId, active);
        if (active) {
            System.out.println("Course active status updated.");
        } else {
            int cancelled = enrollmentService.cancelEnrollmentsForCourse(courseId);
            System.out.println("Course deactivated. Cancelled related enrollments: " + cancelled);
        }
    }

    private static void printCourseDetails(Scanner scanner) {
        int courseId = readInt(scanner, "Course ID: ");
        Course course = courseService.findCourseById(courseId);
        Trainer trainer = findTrainerOrPlaceholder(course.getTrainerId());
        List<String[]> dataRows = new ArrayList<>();
        dataRows.add(new String[] {
                String.valueOf(course.getId()),
                course.getCourseName(),
                trainer.getFirstName() + " " + trainer.getLastName(),
                course.getBatchName(),
                enrollmentService.countAcceptedEnrollmentsForCourse(course.getId()) + "/" + course.getMaxCapacity(),
                course.getDurationInWeeks() + " Weeks",
                String.valueOf(course.isActive())
        });
        ConsolePresenter.printTable(
                new String[] {"Course ID", "Title", "Trainer", "Batch", "Seats", "Duration", "Active"},
                dataRows
        );

        printTableOrMessage(
                new String[] {"Enrollment ID", "Student", "Status"},
                buildCourseEnrollmentRows(enrollmentService.listEnrollmentsByCourseId(courseId)),
                "No enrollments found for this course."
        );
    }

    private static void printCoursesByTrainer(Scanner scanner) {
        int trainerId = readInt(scanner, "Trainer ID: ");
        Trainer trainer = trainerService.findTrainerById(trainerId);
        System.out.println(trainer.getDisplayName());
        printTableOrMessage(
                new String[] {"Course ID", "Title", "Batch", "Seats", "Active"},
                buildCourseRowsForTrainer(courseService.listCoursesByTrainerId(trainerId)),
                "No courses assigned to this trainer."
        );
    }

    private static void handleEnrollmentOperations(Scanner scanner) {
        System.out.println();
        System.out.println("--- Enrollment Management Console ---");
        System.out.println("A. Enroll Student in Course");
        System.out.println("B. View Enrollments for Student");
        System.out.println("C. Update Enrollment Status");
        System.out.println("D. Display All Enrollment Ledger");
        System.out.println("E. View Enrollments for Course");
        System.out.println("F. View Course Capacity Dashboard");
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
            case "E":
                printEnrollmentsForCourse(scanner);
                break;
            case "F":
                printCourseCapacityDashboard();
                break;
            default:
                throw new InvalidInputException("Selection is invalid.");
        }
    }

    private static void enrollStudent(Scanner scanner) {
        int studentId = readInt(scanner, "Student ID: ");
        int courseId = readInt(scanner, "Course ID: ");
        Course course = courseService.findCourseById(courseId);
        Trainer trainer = findTrainerOrPlaceholder(course.getTrainerId());

        boolean trainerApprovedOverCapacity = false;
        String initialStatus = STATUS_ACTIVE;
        if (enrollmentService.isCourseAtCapacity(courseId)) {
            System.out.println("Batch capacity is full for " + course.getCourseName()
                    + " (" + course.getBatchName() + "). Trainer approval is required.");
            System.out.println("Trainer: " + trainer.getFirstName() + " " + trainer.getLastName());
            boolean approved = readBoolean(scanner, "Trainer accepts over-capacity enrollment (true/false): ");
            if (approved) {
                trainerApprovedOverCapacity = true;
            } else {
                initialStatus = STATUS_REJECTED;
            }
        }

        int nextId = IdGenerator.getNextEnrollmentId();
        enrollmentService.enrollStudent(new Enrollment(
                nextId,
                studentId,
                courseId,
                LocalDate.now().toString(),
                initialStatus
        ), trainerApprovedOverCapacity);
        if (STATUS_REJECTED.equals(initialStatus)) {
            System.out.println("Enrollment request rejected by trainer under ID: " + nextId);
        } else {
            System.out.println("Enrolled student under ID: " + nextId);
        }
    }

    private static void printEnrollments() {
        printTableOrMessage(
                new String[] {"Enrollment ID", "Student", "Course", "Trainer", "Batch", "Status"},
                buildEnrollmentRows(enrollmentService.listAllEnrollments()),
                "No enrollments found."
        );
    }

    private static void printEnrollmentsForStudent(Scanner scanner) {
        int studentId = readInt(scanner, "Student ID: ");
        Student student = studentService.findStudentById(studentId);
        System.out.println(student.getFirstName() + " " + student.getLastName() + " - enrollments");
        printTableOrMessage(
                new String[] {"Enrollment ID", "Student", "Course", "Trainer", "Batch", "Status"},
                buildEnrollmentRows(enrollmentService.listEnrollmentsByStudentId(studentId)),
                "No enrollments found for this student."
        );
    }

    private static void updateEnrollmentStatus(Scanner scanner) {
        int enrollmentId = readInt(scanner, "Enrollment ID: ");
        String status = readEnrollmentStatus(scanner, "Status (PENDING / ACTIVE / COMPLETED / CANCELLED / REJECTED): ");
        enrollmentService.updateStatus(enrollmentId, status);
        System.out.println("Enrollment status updated.");
    }

    private static void printEnrollmentsForCourse(Scanner scanner) {
        int courseId = readInt(scanner, "Course ID: ");
        Course course = courseService.findCourseById(courseId);
        System.out.println(course.getCourseName() + " - " + course.getBatchName());
        printTableOrMessage(
                new String[] {"Enrollment ID", "Student", "Status"},
                buildCourseEnrollmentRows(enrollmentService.listEnrollmentsByCourseId(courseId)),
                "No enrollments found for this course."
        );
    }

    private static void printCourseCapacityDashboard() {
        List<String[]> dataRows = new ArrayList<>();
        for (Course course : courseService.listAllCourses()) {
            Trainer trainer = findTrainerOrPlaceholder(course.getTrainerId());
            int accepted = enrollmentService.countAcceptedEnrollmentsForCourse(course.getId());
            dataRows.add(new String[] {
                    String.valueOf(course.getId()),
                    course.getCourseName(),
                    trainer.getFirstName() + " " + trainer.getLastName(),
                    course.getBatchName(),
                    accepted + "/" + course.getMaxCapacity(),
                    accepted >= course.getMaxCapacity() ? "FULL" : "OPEN",
                    String.valueOf(course.isActive())
            });
        }
        ConsolePresenter.printTable(
                new String[] {"Course ID", "Course", "Trainer", "Batch", "Seats", "Capacity", "Active"},
                dataRows
        );
    }

    private static void handleTrainerOperations(Scanner scanner) {
        System.out.println();
        System.out.println("--- Trainer Management Console ---");
        System.out.println("A. Create Trainer Profile");
        System.out.println("B. Display Trainer Ledger");
        System.out.println("C. View Trainer Full Details");
        System.out.println("D. View Courses Handled by Trainer");
        System.out.println("E. View Students Under Trainer");
        System.out.println("F. View Batches Handled by Trainer");
        System.out.println("G. Change Trainer Active Status");
        System.out.print("Option: ");

        String selection = scanner.nextLine().toUpperCase(Locale.ROOT).trim();
        switch (selection) {
            case "A":
                createTrainer(scanner);
                break;
            case "B":
                printTrainers();
                break;
            case "C":
                printTrainerFullDetails(scanner);
                break;
            case "D":
                printTrainerCourses(scanner);
                break;
            case "E":
                printStudentsUnderTrainer(scanner);
                break;
            case "F":
                printTrainerBatches(scanner);
                break;
            case "G":
                changeTrainerStatus(scanner);
                break;
            default:
                throw new InvalidInputException("Selection is invalid.");
        }
    }

    private static void createTrainer(Scanner scanner) {
        String firstName = readRequired(scanner, "First Name: ");
        String lastName = readRequired(scanner, "Last Name: ");
        String email = readRequired(scanner, "Email: ");
        String specialization = readRequired(scanner, "Specialization: ");

        int nextId = IdGenerator.getNextTrainerId();
        trainerService.addTrainer(new Trainer(nextId, firstName, lastName, email, specialization, true));
        System.out.println("Created trainer profile with ID: " + nextId);
    }

    private static void printTrainers() {
        List<String[]> dataRows = new ArrayList<>();
        for (Trainer trainer : trainerService.listAllTrainers()) {
            dataRows.add(new String[] {
                    String.valueOf(trainer.getId()),
                    trainer.getFirstName() + " " + trainer.getLastName(),
                    trainer.getSpecialization(),
                    String.valueOf(trainer.isActive())
            });
        }
        ConsolePresenter.printTable(new String[] {"Trainer ID", "Name", "Specialization", "Active"}, dataRows);
    }

    private static void printTrainerFullDetails(Scanner scanner) {
        int trainerId = readInt(scanner, "Trainer ID: ");
        Trainer trainer = trainerService.findTrainerById(trainerId);
        printTrainerProfile(trainer);
        printTrainerCoursesForId(trainerId);
        printStudentsUnderTrainerId(trainerId);
    }

    private static void printTrainerCourses(Scanner scanner) {
        int trainerId = readInt(scanner, "Trainer ID: ");
        trainerService.findTrainerById(trainerId);
        printTrainerCoursesForId(trainerId);
    }

    private static void printStudentsUnderTrainer(Scanner scanner) {
        int trainerId = readInt(scanner, "Trainer ID: ");
        trainerService.findTrainerById(trainerId);
        printStudentsUnderTrainerId(trainerId);
    }

    private static void printTrainerBatches(Scanner scanner) {
        int trainerId = readInt(scanner, "Trainer ID: ");
        trainerService.findTrainerById(trainerId);
        Set<String> batches = new LinkedHashSet<>();
        for (Course course : courseService.listCoursesByTrainerId(trainerId)) {
            batches.add(course.getBatchName());
        }

        List<String[]> dataRows = new ArrayList<>();
        for (String batch : batches) {
            dataRows.add(new String[] {batch});
        }
        printTableOrMessage(new String[] {"Batch"}, dataRows, "No batches assigned to this trainer.");
    }

    private static void changeTrainerStatus(Scanner scanner) {
        int trainerId = readInt(scanner, "Trainer ID: ");
        boolean active = readBoolean(scanner, "Active (true/false): ");
        trainerService.setTrainerStatus(trainerId, active);
        System.out.println("Trainer active status updated.");
    }

    private static void printTrainerProfile(Trainer trainer) {
        List<String[]> dataRows = new ArrayList<>();
        dataRows.add(new String[] {
                String.valueOf(trainer.getId()),
                trainer.getFirstName() + " " + trainer.getLastName(),
                trainer.getEmail(),
                trainer.getSpecialization(),
                String.valueOf(trainer.isActive())
        });
        ConsolePresenter.printTable(new String[] {"Trainer ID", "Name", "Email", "Specialization", "Active"}, dataRows);
    }

    private static void printTrainerCoursesForId(int trainerId) {
        printTableOrMessage(
                new String[] {"Course ID", "Title", "Batch", "Seats", "Active"},
                buildCourseRowsForTrainer(courseService.listCoursesByTrainerId(trainerId)),
                "No courses assigned to this trainer."
        );
    }

    private static void printStudentsUnderTrainerId(int trainerId) {
        printTableOrMessage(
                new String[] {"Student ID", "Student", "Course", "Batch", "Status"},
                buildTrainerStudentRows(trainerId),
                "No students found under this trainer."
        );
    }

    private static List<String[]> buildStudentEnrollmentRows(List<Enrollment> enrollments) {
        List<String[]> dataRows = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            Course course = courseService.findCourseById(enrollment.getCourseId());
            Trainer trainer = findTrainerOrPlaceholder(course.getTrainerId());
            dataRows.add(new String[] {
                    String.valueOf(enrollment.getId()),
                    course.getCourseName(),
                    trainer.getFirstName() + " " + trainer.getLastName(),
                    course.getBatchName(),
                    enrollment.getStatus()
            });
        }
        return dataRows;
    }

    private static List<String[]> buildEnrollmentRows(List<Enrollment> enrollments) {
        List<String[]> dataRows = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            Student student = studentService.findStudentById(enrollment.getStudentId());
            Course course = courseService.findCourseById(enrollment.getCourseId());
            Trainer trainer = findTrainerOrPlaceholder(course.getTrainerId());
            dataRows.add(new String[] {
                    String.valueOf(enrollment.getId()),
                    student.getFirstName() + " " + student.getLastName(),
                    course.getCourseName(),
                    trainer.getFirstName() + " " + trainer.getLastName(),
                    course.getBatchName(),
                    enrollment.getStatus()
            });
        }
        return dataRows;
    }

    private static List<String[]> buildCourseEnrollmentRows(List<Enrollment> enrollments) {
        List<String[]> dataRows = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            Student student = studentService.findStudentById(enrollment.getStudentId());
            dataRows.add(new String[] {
                    String.valueOf(enrollment.getId()),
                    student.getFirstName() + " " + student.getLastName(),
                    enrollment.getStatus()
            });
        }
        return dataRows;
    }

    private static List<String[]> buildCourseRowsForTrainer(List<Course> courses) {
        List<String[]> dataRows = new ArrayList<>();
        for (Course course : courses) {
            int accepted = enrollmentService.countAcceptedEnrollmentsForCourse(course.getId());
            dataRows.add(new String[] {
                    String.valueOf(course.getId()),
                    course.getCourseName(),
                    course.getBatchName(),
                    accepted + "/" + course.getMaxCapacity(),
                    String.valueOf(course.isActive())
            });
        }
        return dataRows;
    }

    private static List<String[]> buildTrainerStudentRows(int trainerId) {
        List<String[]> dataRows = new ArrayList<>();
        Set<Integer> seenEnrollments = new LinkedHashSet<>();
        for (Course course : courseService.listCoursesByTrainerId(trainerId)) {
            for (Enrollment enrollment : enrollmentService.listEnrollmentsByCourseId(course.getId())) {
                if (seenEnrollments.add(enrollment.getId())) {
                    Student student = studentService.findStudentById(enrollment.getStudentId());
                    dataRows.add(new String[] {
                            String.valueOf(student.getId()),
                            student.getFirstName() + " " + student.getLastName(),
                            course.getCourseName(),
                            course.getBatchName(),
                            enrollment.getStatus()
                    });
                }
            }
        }
        return dataRows;
    }

    private static int countEnrollmentsByStatus(List<Enrollment> enrollments, String status) {
        int count = 0;
        for (Enrollment enrollment : enrollments) {
            if (status.equals(enrollment.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private static void printTableOrMessage(String[] headers, List<String[]> dataRows, String emptyMessage) {
        if (dataRows.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        ConsolePresenter.printTable(headers, dataRows);
    }

    private static Trainer findTrainerOrPlaceholder(int trainerId) {
        if (trainerId == 0) {
            return new Trainer(1, "Unassigned", "Trainer", "unassigned.trainer@learntrack.local", "Unassigned", false);
        }
        return trainerService.findTrainerById(trainerId);
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
            case "trainer":
                trainerService.addTrainer(newTrainerFromCSV(operation, entityId));
                IdGenerator.syncTrainerId(entityId);
                System.out.println("     POST (Trainer) successfully loaded with ID: " + entityId);
                break;
            case "course":
                Course course = newCourseFromCSV(operation, entityId);
                if (course.getTrainerId() > 0 && !trainerService.findTrainerById(course.getTrainerId()).isActive()) {
                    throw new InvalidInputException("Validation error: Cannot assign an inactive trainer.");
                }
                courseService.addCourse(course);
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
            case "trainer":
                trainerService.setTrainerStatus(entityId, parseBooleanCell(operation, 3, "active"));
                System.out.println("     PUT (Trainer) status updated successfully: " + entityId);
                break;
            case "course":
                boolean active = parseBooleanCell(operation, 3, "active");
                courseService.setCourseStatus(entityId, active);
                if (active) {
                    System.out.println("     PUT (Course) status updated successfully: " + entityId);
                } else {
                    int cancelled = enrollmentService.cancelEnrollmentsForCourse(entityId);
                    System.out.println("     PUT (Course) deactivated and cancelled enrollments: " + cancelled);
                }
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
            case "trainer":
                Trainer trainer = trainerService.findTrainerById(entityId);
                System.out.println("     GET (Trainer) " + entityId + " -> " + trainer.getDisplayName());
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

    private static Trainer newTrainerFromCSV(String[] operation, int entityId) {
        return new Trainer(
                entityId,
                getCell(operation, 3, "firstName"),
                getCell(operation, 4, "lastName"),
                getCell(operation, 5, "email"),
                getCell(operation, 6, "specialization"),
                parseBooleanCell(operation, 7, "active")
        );
    }

    private static Course newCourseFromCSV(String[] operation, int entityId) {
        if (operation.length >= 10) {
            return new Course(
                    entityId,
                    getCell(operation, 3, "courseName"),
                    getCell(operation, 4, "description"),
                    parseIntCell(operation, 5, "durationInWeeks"),
                    parseIntCell(operation, 6, "trainerId"),
                    getCell(operation, 7, "batchName"),
                    parseIntCell(operation, 8, "maxCapacity"),
                    parseBooleanCell(operation, 9, "active")
            );
        }

        return new Course(
                entityId,
                getCell(operation, 3, "courseName"),
                getCell(operation, 4, "description"),
                parseIntCell(operation, 5, "durationInWeeks"),
                parseBooleanCell(operation, 6, "active")
        );
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
                || STATUS_CANCELLED.equals(normalizedStatus)
                || STATUS_REJECTED.equals(normalizedStatus)
                || STATUS_PENDING.equals(normalizedStatus)) {
            return normalizedStatus;
        }
        throw new InvalidInputException("Enrollment status must be PENDING, ACTIVE, COMPLETED, CANCELLED, or REJECTED.");
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
