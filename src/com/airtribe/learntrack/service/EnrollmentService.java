package com.airtribe.learntrack.service;

import com.airtribe.learntrack.entity.Course;
import com.airtribe.learntrack.entity.Enrollment;
import com.airtribe.learntrack.entity.Student;
import com.airtribe.learntrack.exception.EntityNotFoundException;
import com.airtribe.learntrack.exception.InvalidInputException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Application service for enrollment business operations.
 */
public class EnrollmentService {
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_REJECTED = "REJECTED";

    private final List<Enrollment> enrollments = new ArrayList<>();
    private final StudentService studentService;
    private final CourseService courseService;

    /**
     * Creates an enrollment service using related services for referential checks.
     *
     * @param studentService service used to validate student references
     * @param courseService service used to validate course references
     * @throws InvalidInputException if any dependency is null
     */
    public EnrollmentService(StudentService studentService, CourseService courseService) {
        if (studentService == null) {
            throw new InvalidInputException("Student service was not provided.");
        }
        if (courseService == null) {
            throw new InvalidInputException("Course service was not provided.");
        }
        this.studentService = studentService;
        this.courseService = courseService;
    }

    /**
     * Enrolls a student in a course after validating business rules.
     *
     * @param enrollment enrollment to add
     * @throws InvalidInputException if enrollment details are missing, invalid, duplicated, or inactive
     * @throws EntityNotFoundException if the student or course reference does not exist
     */
    public void enrollStudent(Enrollment enrollment) {
        enrollStudent(enrollment, false);
    }

    /**
     * Enrolls a student in a course after validating business rules and capacity.
     *
     * @param enrollment enrollment to add
     * @param trainerApprovedOverCapacity whether the trainer accepted a full-batch override
     * @throws InvalidInputException if enrollment details are missing, invalid, duplicated, or inactive
     * @throws EntityNotFoundException if the student or course reference does not exist
     */
    public void enrollStudent(Enrollment enrollment, boolean trainerApprovedOverCapacity) {
        validateEnrollment(enrollment);
        if (findEnrollmentIndexById(enrollment.getId()) >= 0) {
            throw new InvalidInputException("Validation error: Enrollment ID already exists.");
        }
        if (hasEnrollmentForStudentAndCourse(enrollment.getStudentId(), enrollment.getCourseId())) {
            throw new InvalidInputException("Validation error: Student is already enrolled in this course.");
        }

        Student student = studentService.findStudentById(enrollment.getStudentId());
        Course course = courseService.findCourseById(enrollment.getCourseId());
        if (!student.isActive()) {
            throw new InvalidInputException("Validation error: Cannot enroll an inactive student.");
        }
        if (!course.isActive()) {
            throw new InvalidInputException("Validation error: Cannot enroll in an inactive course.");
        }

        String status = normalizeStatus(enrollment.getStatus());
        if (STATUS_ACTIVE.equals(status) || STATUS_PENDING.equals(status)) {
            boolean capacityReached = countAcceptedEnrollmentsForCourse(course.getId()) >= course.getMaxCapacity();
            if (capacityReached && !trainerApprovedOverCapacity) {
                throw new InvalidInputException("Validation error: Course batch capacity is full. Trainer approval is required.");
            }
        }

        enrollments.add(copyEnrollment(enrollment));
    }

    /**
     * Updates enrollment status.
     *
     * @param enrollmentId enrollment identifier
     * @param status requested status: PENDING, ACTIVE, COMPLETED, CANCELLED, or REJECTED
     * @throws InvalidInputException if the status is blank or unsupported
     * @throws EntityNotFoundException if no enrollment exists for the id
     */
    public void updateStatus(int enrollmentId, String status) {
        Enrollment enrollment = findEnrollmentInternal(enrollmentId);
        String normalizedStatus = normalizeStatus(status);
        Course course = courseService.findCourseById(enrollment.getCourseId());
        if (!course.isActive() && !STATUS_CANCELLED.equals(normalizedStatus) && !STATUS_REJECTED.equals(normalizedStatus)) {
            throw new InvalidInputException("Validation error: Inactive course enrollments can only be CANCELLED or REJECTED.");
        }
        enrollment.setStatus(normalizedStatus);
    }

    /**
     * Finds an enrollment by id.
     *
     * @param enrollmentId enrollment identifier
     * @return matching enrollment copy
     * @throws EntityNotFoundException if no enrollment exists for the id
     */
    public Enrollment findEnrollmentById(int enrollmentId) {
        return copyEnrollment(findEnrollmentInternal(enrollmentId));
    }

    /**
     * Lists all enrollments.
     *
     * @return defensive list copy containing enrollment copies
     */
    public List<Enrollment> listAllEnrollments() {
        List<Enrollment> result = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            result.add(copyEnrollment(enrollment));
        }
        return result;
    }

    /**
     * Lists enrollments for a specific student.
     *
     * @param studentId student identifier
     * @return defensive list copy of matching enrollments
     */
    public List<Enrollment> listEnrollmentsByStudentId(int studentId) {
        studentService.findStudentById(studentId);
        List<Enrollment> result = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudentId() == studentId) {
                result.add(copyEnrollment(enrollment));
            }
        }
        return result;
    }

    /**
     * Lists enrollments for a specific course.
     *
     * @param courseId course identifier
     * @return defensive list copy of matching enrollments
     */
    public List<Enrollment> listEnrollmentsByCourseId(int courseId) {
        courseService.findCourseById(courseId);
        List<Enrollment> result = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getCourseId() == courseId) {
                result.add(copyEnrollment(enrollment));
            }
        }
        return result;
    }

    /**
     * Lists enrollments for a student filtered by status.
     *
     * @param studentId student identifier
     * @param status requested enrollment status
     * @return defensive list copy of matching enrollments
     */
    public List<Enrollment> listEnrollmentsByStudentIdAndStatus(int studentId, String status) {
        studentService.findStudentById(studentId);
        String normalizedStatus = normalizeStatus(status);
        List<Enrollment> result = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudentId() == studentId && normalizedStatus.equals(enrollment.getStatus())) {
                result.add(copyEnrollment(enrollment));
            }
        }
        return result;
    }

    /**
     * Counts accepted enrollments for a course.
     *
     * @param courseId course identifier
     * @return active or completed enrollment count
     */
    public int countAcceptedEnrollmentsForCourse(int courseId) {
        courseService.findCourseById(courseId);
        int count = 0;
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getCourseId() == courseId && isAcceptedStatus(enrollment.getStatus())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Indicates whether a course has reached its configured capacity.
     *
     * @param courseId course identifier
     * @return {@code true} when accepted enrollments are at or above capacity
     */
    public boolean isCourseAtCapacity(int courseId) {
        Course course = courseService.findCourseById(courseId);
        return countAcceptedEnrollmentsForCourse(courseId) >= course.getMaxCapacity();
    }

    /**
     * Cancels every enrollment for an inactive course.
     *
     * @param courseId course identifier
     * @return number of enrollments changed to CANCELLED
     */
    public int cancelEnrollmentsForCourse(int courseId) {
        courseService.findCourseById(courseId);
        int changed = 0;
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getCourseId() == courseId
                    && !STATUS_CANCELLED.equals(enrollment.getStatus())
                    && !STATUS_REJECTED.equals(enrollment.getStatus())) {
                enrollment.setStatus(STATUS_CANCELLED);
                changed++;
            }
        }
        return changed;
    }

    /**
     * Deletes an enrollment by id.
     *
     * @param enrollmentId enrollment identifier
     * @throws EntityNotFoundException if no enrollment exists for the id
     */
    public void deleteEnrollment(int enrollmentId) {
        int index = findEnrollmentIndexById(enrollmentId);
        if (index < 0) {
            throw new EntityNotFoundException("Enrollment", enrollmentId);
        }
        enrollments.remove(index);
    }

    private Enrollment findEnrollmentInternal(int enrollmentId) {
        int index = findEnrollmentIndexById(enrollmentId);
        if (index < 0) {
            throw new EntityNotFoundException("Enrollment", enrollmentId);
        }
        return enrollments.get(index);
    }

    private int findEnrollmentIndexById(int enrollmentId) {
        for (int i = 0; i < enrollments.size(); i++) {
            if (enrollments.get(i).getId() == enrollmentId) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasEnrollmentForStudentAndCourse(int studentId, int courseId) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudentId() == studentId
                    && enrollment.getCourseId() == courseId
                    && !STATUS_CANCELLED.equals(enrollment.getStatus())
                    && !STATUS_REJECTED.equals(enrollment.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private void validateEnrollment(Enrollment enrollment) {
        if (enrollment == null) {
            throw new InvalidInputException("Enrollment details cannot be empty.");
        }
        if (enrollment.getId() <= 0) {
            throw new InvalidInputException("Validation error: Enrollment ID must be positive.");
        }
        if (enrollment.getStudentId() <= 0) {
            throw new InvalidInputException("Validation error: Student ID must be positive.");
        }
        if (enrollment.getCourseId() <= 0) {
            throw new InvalidInputException("Validation error: Course ID must be positive.");
        }
        if (isBlank(enrollment.getEnrollmentDate())) {
            throw new InvalidInputException("Validation error: Enrollment date is required.");
        }
        normalizeStatus(enrollment.getStatus());
    }

    private String normalizeStatus(String status) {
        if (isBlank(status)) {
            throw new InvalidInputException("Validation error: Enrollment status is required.");
        }

        String formattedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!STATUS_ACTIVE.equals(formattedStatus)
                && !STATUS_COMPLETED.equals(formattedStatus)
                && !STATUS_CANCELLED.equals(formattedStatus)
                && !STATUS_PENDING.equals(formattedStatus)
                && !STATUS_REJECTED.equals(formattedStatus)) {
            throw new InvalidInputException("Validation error: Invalid enrollment status.");
        }
        return formattedStatus;
    }

    private boolean isAcceptedStatus(String status) {
        return STATUS_ACTIVE.equals(status) || STATUS_COMPLETED.equals(status);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Enrollment copyEnrollment(Enrollment enrollment) {
        return new Enrollment(
                enrollment.getId(),
                enrollment.getStudentId(),
                enrollment.getCourseId(),
                enrollment.getEnrollmentDate(),
                enrollment.getStatus()
        );
    }
}
