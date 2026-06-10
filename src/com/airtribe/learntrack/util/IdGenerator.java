package com.airtribe.learntrack.util;

/**
 * Thread-safe identifier generator for LearnTrack aggregate roots.
 *
 * <p>All counter reads and writes are guarded by the {@code IdGenerator.class}
 * monitor through static synchronized methods. This keeps each increment and
 * synchronization operation atomic, prevents lost updates across threads, and
 * guarantees that counters never move backward.</p>
 */
public final class IdGenerator {
    private static int studentIdCounter = 1000;
    private static int courseIdCounter = 2000;
    private static int enrollmentIdCounter = 3000;

    private IdGenerator() {
        throw new AssertionError("IdGenerator cannot be instantiated");
    }

    /**
     * Returns the next student identifier.
     *
     * @return next positive student identifier
     */
    public static synchronized int getNextStudentId() {
        studentIdCounter++;
        return studentIdCounter;
    }

    /**
     * Returns the next course identifier.
     *
     * @return next positive course identifier
     */
    public static synchronized int getNextCourseId() {
        courseIdCounter++;
        return courseIdCounter;
    }

    /**
     * Returns the next enrollment identifier.
     *
     * @return next positive enrollment identifier
     */
    public static synchronized int getNextEnrollmentId() {
        enrollmentIdCounter++;
        return enrollmentIdCounter;
    }

    /**
     * Synchronizes the student counter with an externally known maximum id.
     *
     * @param id externally known non-negative student id
     * @throws IllegalArgumentException if {@code id} is negative
     */
    public static synchronized void syncStudentId(int id) {
        studentIdCounter = syncCounter(studentIdCounter, id, "studentId");
    }

    /**
     * Synchronizes the course counter with an externally known maximum id.
     *
     * @param id externally known non-negative course id
     * @throws IllegalArgumentException if {@code id} is negative
     */
    public static synchronized void syncCourseId(int id) {
        courseIdCounter = syncCounter(courseIdCounter, id, "courseId");
    }

    /**
     * Synchronizes the enrollment counter with an externally known maximum id.
     *
     * @param id externally known non-negative enrollment id
     * @throws IllegalArgumentException if {@code id} is negative
     */
    public static synchronized void syncEnrollmentId(int id) {
        enrollmentIdCounter = syncCounter(enrollmentIdCounter, id, "enrollmentId");
    }

    private static int syncCounter(int currentCounter, int externalId, String fieldName) {
        if (externalId < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return Math.max(currentCounter, externalId);
    }
}
