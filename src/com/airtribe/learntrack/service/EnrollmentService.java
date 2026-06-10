package com.airtribe.learntrack.service;

import com.airtribe.learntrack.db.TransactionalStore;
import com.airtribe.learntrack.entity.Enrollment;
import com.airtribe.learntrack.exception.EntityNotFoundException;
import com.airtribe.learntrack.exception.InvalidInputException;

import java.util.List;
import java.util.Locale;

/**
 * Application service for enrollment business operations.
 */
public class EnrollmentService {
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final TransactionalStore<Integer, Enrollment> db;
    private final StudentService studentService;
    private final CourseService courseService;

    /**
     * Creates an enrollment service backed by transactional storage and related services.
     *
     * @param db transactional enrollment store
     * @param studentService service used to validate student references
     * @param courseService service used to validate course references
     * @throws InvalidInputException if any dependency is null
     */
    public EnrollmentService(
            TransactionalStore<Integer, Enrollment> db,
            StudentService studentService,
            CourseService courseService
    ) {
        if (db == null) {
            throw new InvalidInputException("Enrollment store cannot be empty.");
        }
        if (studentService == null) {
            throw new InvalidInputException("Student service cannot be empty.");
        }
        if (courseService == null) {
            throw new InvalidInputException("Course service cannot be empty.");
        }
        this.db = db;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    /**
     * Enrolls a student in a course after validating referential integrity.
     *
     * @param enrollment enrollment to add
     * @throws InvalidInputException if enrollment details are missing
     * @throws EntityNotFoundException if the student or course reference does not exist
     */
    public void enrollStudent(Enrollment enrollment) {
        validateEnrollment(enrollment);
        studentService.findStudentById(enrollment.getStudentId());
        courseService.findCourseById(enrollment.getCourseId());
        db.put(enrollment.getId(), enrollment);
    }

    /**
     * Updates enrollment status.
     *
     * @param enrollmentId enrollment identifier
     * @param status requested status: ACTIVE, COMPLETED, or CANCELLED
     * @throws InvalidInputException if the status is blank or unsupported
     * @throws EntityNotFoundException if no enrollment exists for the id
     */
    public void updateStatus(int enrollmentId, String status) {
        Enrollment enrollment = findEnrollmentById(enrollmentId);
        String formattedStatus = normalizeStatus(status);
        Enrollment updatedEnrollment = new Enrollment(
                enrollment.getId(),
                enrollment.getStudentId(),
                enrollment.getCourseId(),
                enrollment.getEnrollmentDate(),
                formattedStatus
        );
        db.put(enrollmentId, updatedEnrollment);
    }

    /**
     * Finds an enrollment by id.
     *
     * @param enrollmentId enrollment identifier
     * @return matching enrollment
     * @throws EntityNotFoundException if no enrollment exists for the id
     */
    public Enrollment findEnrollmentById(int enrollmentId) {
        Enrollment enrollment = db.get(enrollmentId);
        if (enrollment == null) {
            throw new EntityNotFoundException("Enrollment", enrollmentId);
        }
        return enrollment;
    }

    /**
     * Lists all visible enrollments from the transactional store.
     *
     * @return current enrollment list
     */
    public List<Enrollment> listAllEnrollments() {
        return db.getAll();
    }

    private void validateEnrollment(Enrollment enrollment) {
        if (enrollment == null) {
            throw new InvalidInputException("Enrollment details cannot be empty.");
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new InvalidInputException("Validation error: Enrollment status is required.");
        }

        String formattedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!STATUS_ACTIVE.equals(formattedStatus)
                && !STATUS_COMPLETED.equals(formattedStatus)
                && !STATUS_CANCELLED.equals(formattedStatus)) {
            throw new InvalidInputException("Validation error: Invalid enrollment status.");
        }
        return formattedStatus;
    }
}
