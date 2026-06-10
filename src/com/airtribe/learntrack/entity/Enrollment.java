package com.airtribe.learntrack.entity;

import java.util.Locale;
import java.util.Objects;

/**
 * Enrollment domain entity linking a student to a course.
 */
public class Enrollment {
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_REJECTED = "REJECTED";

    private int id;
    private int studentId;
    private int courseId;
    private String enrollmentDate;
    private String status;

    /**
     * Creates an enrollment with all persisted fields.
     *
     * @param id unique positive enrollment identifier
     * @param studentId positive student foreign-key identifier
     * @param courseId positive course foreign-key identifier
     * @param enrollmentDate non-blank enrollment date text
     * @param status enrollment status: {@code PENDING}, {@code ACTIVE}, {@code COMPLETED},
     *         {@code CANCELLED}, or {@code REJECTED}
     * @throws IllegalArgumentException if any argument violates enrollment invariants
     */
    public Enrollment(int id, int studentId, int courseId, String enrollmentDate, String status) {
        setId(id);
        setStudentId(studentId);
        setCourseId(courseId);
        setEnrollmentDate(enrollmentDate);
        setStatus(status);
    }

    /**
     * Creates an active enrollment for the provided student, course, and date.
     *
     * @param id unique positive enrollment identifier
     * @param studentId positive student foreign-key identifier
     * @param courseId positive course foreign-key identifier
     * @param enrollmentDate non-blank enrollment date text
     * @throws IllegalArgumentException if any argument violates enrollment invariants
     */
    public Enrollment(int id, int studentId, int courseId, String enrollmentDate) {
        this(id, studentId, courseId, enrollmentDate, STATUS_ACTIVE);
    }

    /**
     * Returns the enrollment identifier.
     *
     * @return positive enrollment identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Updates the enrollment identifier.
     *
     * @param id positive enrollment identifier
     * @throws IllegalArgumentException if {@code id} is zero or negative
     */
    public void setId(int id) {
        this.id = requirePositive(id, "id");
    }

    /**
     * Returns the student foreign-key identifier.
     *
     * @return positive student identifier
     */
    public int getStudentId() {
        return studentId;
    }

    /**
     * Updates the student foreign-key identifier.
     *
     * @param studentId positive student identifier
     * @throws IllegalArgumentException if {@code studentId} is zero or negative
     */
    public void setStudentId(int studentId) {
        this.studentId = requirePositive(studentId, "studentId");
    }

    /**
     * Returns the course foreign-key identifier.
     *
     * @return positive course identifier
     */
    public int getCourseId() {
        return courseId;
    }

    /**
     * Updates the course foreign-key identifier.
     *
     * @param courseId positive course identifier
     * @throws IllegalArgumentException if {@code courseId} is zero or negative
     */
    public void setCourseId(int courseId) {
        this.courseId = requirePositive(courseId, "courseId");
    }

    /**
     * Returns the enrollment date.
     *
     * @return non-blank enrollment date text
     */
    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    /**
     * Updates the enrollment date.
     *
     * @param enrollmentDate non-blank enrollment date text
     * @throws IllegalArgumentException if {@code enrollmentDate} is null or blank
     */
    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = requireNotBlank(enrollmentDate, "enrollmentDate");
    }

    /**
     * Returns the enrollment status.
     *
     * @return status value: {@code PENDING}, {@code ACTIVE}, {@code COMPLETED},
     *         {@code CANCELLED}, or {@code REJECTED}
     */
    public String getStatus() {
        return status;
    }

    /**
     * Updates the enrollment status.
     *
     * @param status status value: {@code PENDING}, {@code ACTIVE}, {@code COMPLETED},
     *         {@code CANCELLED}, or {@code REJECTED}
     * @throws IllegalArgumentException if {@code status} is null, blank, or unsupported
     */
    public void setStatus(String status) {
        String normalizedStatus = requireNotBlank(status, "status").toUpperCase(Locale.ROOT);
        if (!isAllowedStatus(normalizedStatus)) {
            throw new IllegalArgumentException("status must be PENDING, ACTIVE, COMPLETED, CANCELLED, or REJECTED");
        }
        this.status = normalizedStatus;
    }

    /**
     * Returns a human-readable representation of enrollment state.
     *
     * @return enrollment state string
     */
    @Override
    public String toString() {
        return "Enrollment{"
                + "id=" + id
                + ", studentId=" + studentId
                + ", courseId=" + courseId
                + ", enrollmentDate='" + enrollmentDate + '\''
                + ", status='" + status + '\''
                + '}';
    }

    /**
     * Compares this enrollment with another object using field equality.
     *
     * @param object object to compare
     * @return {@code true} when both objects have the same enrollment state
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Enrollment that = (Enrollment) object;
        return id == that.id
                && studentId == that.studentId
                && courseId == that.courseId
                && Objects.equals(enrollmentDate, that.enrollmentDate)
                && Objects.equals(status, that.status);
    }

    /**
     * Computes a hash code from the validated enrollment state.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, studentId, courseId, enrollmentDate, status);
    }

    private static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
        return value;
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmedValue;
    }

    private static boolean isAllowedStatus(String status) {
        return STATUS_ACTIVE.equals(status)
                || STATUS_COMPLETED.equals(status)
                || STATUS_CANCELLED.equals(status)
                || STATUS_PENDING.equals(status)
                || STATUS_REJECTED.equals(status);
    }
}
