package com.airtribe.learntrack.entity;

import java.util.Objects;

/**
 * Course domain entity representing a learning track offered by LearnTrack.
 */
public class Course {
    private int id;
    private String courseName;
    private String description;
    private int durationInWeeks;
    private int trainerId;
    private String batchName;
    private int maxCapacity;
    private boolean active;

    /**
     * Creates a course with all persisted fields.
     *
     * @param id unique positive course identifier
     * @param courseName non-blank course name
     * @param description non-null course description
     * @param durationInWeeks positive course duration in weeks
     * @param trainerId assigned trainer identifier; zero means unassigned
     * @param batchName non-blank batch code or name
     * @param maxCapacity maximum accepted students for the batch
     * @param active whether the course is active
     * @throws IllegalArgumentException if any argument violates course invariants
     */
    public Course(
            int id,
            String courseName,
            String description,
            int durationInWeeks,
            int trainerId,
            String batchName,
            int maxCapacity,
            boolean active
    ) {
        setId(id);
        setCourseName(courseName);
        setDescription(description);
        setDurationInWeeks(durationInWeeks);
        setTrainerId(trainerId);
        setBatchName(batchName);
        setMaxCapacity(maxCapacity);
        setActive(active);
    }

    /**
     * Creates a course with default batch capacity and no assigned trainer.
     *
     * @param id unique positive course identifier
     * @param courseName non-blank course name
     * @param description non-null course description
     * @param durationInWeeks positive course duration in weeks
     * @param active whether the course is active
     * @throws IllegalArgumentException if any argument violates course invariants
     */
    public Course(int id, String courseName, String description, int durationInWeeks, boolean active) {
        this(id, courseName, description, durationInWeeks, 0, "Unassigned", 60, active);
    }

    /**
     * Creates an active course with an empty description.
     *
     * @param id unique positive course identifier
     * @param courseName non-blank course name
     * @param durationInWeeks positive course duration in weeks
     * @throws IllegalArgumentException if any argument violates course invariants
     */
    public Course(int id, String courseName, int durationInWeeks) {
        this(id, courseName, "", durationInWeeks, true);
    }

    /**
     * Returns the course identifier.
     *
     * @return positive course identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Updates the course identifier.
     *
     * @param id positive course identifier
     * @throws IllegalArgumentException if {@code id} is zero or negative
     */
    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be greater than zero");
        }
        this.id = id;
    }

    /**
     * Returns the course name.
     *
     * @return non-blank course name
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * Updates the course name.
     *
     * @param courseName non-blank course name
     * @throws IllegalArgumentException if {@code courseName} is null or blank
     */
    public void setCourseName(String courseName) {
        this.courseName = requireNotBlank(courseName, "courseName");
    }

    /**
     * Returns the course description.
     *
     * @return non-null course description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Updates the course description.
     *
     * @param description non-null course description
     * @throws IllegalArgumentException if {@code description} is null
     */
    public void setDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null");
        }
        this.description = description;
    }

    /**
     * Returns the course duration in weeks.
     *
     * @return positive duration in weeks
     */
    public int getDurationInWeeks() {
        return durationInWeeks;
    }

    /**
     * Updates the course duration in weeks.
     *
     * @param durationInWeeks positive duration in weeks
     * @throws IllegalArgumentException if {@code durationInWeeks} is zero or negative
     */
    public void setDurationInWeeks(int durationInWeeks) {
        if (durationInWeeks <= 0) {
            throw new IllegalArgumentException("durationInWeeks must be greater than zero");
        }
        this.durationInWeeks = durationInWeeks;
    }

    /**
     * Returns the assigned trainer identifier.
     *
     * @return trainer identifier, or zero when unassigned
     */
    public int getTrainerId() {
        return trainerId;
    }

    /**
     * Updates the assigned trainer identifier.
     *
     * @param trainerId trainer identifier, or zero when unassigned
     * @throws IllegalArgumentException if {@code trainerId} is negative
     */
    public void setTrainerId(int trainerId) {
        if (trainerId < 0) {
            throw new IllegalArgumentException("trainerId must not be negative");
        }
        this.trainerId = trainerId;
    }

    /**
     * Returns the course batch name.
     *
     * @return non-blank batch code or name
     */
    public String getBatchName() {
        return batchName;
    }

    /**
     * Updates the course batch name.
     *
     * @param batchName non-blank batch code or name
     * @throws IllegalArgumentException if {@code batchName} is null or blank
     */
    public void setBatchName(String batchName) {
        this.batchName = requireNotBlank(batchName, "batchName");
    }

    /**
     * Returns maximum accepted students for the batch.
     *
     * @return positive capacity value
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Updates maximum accepted students for the batch.
     *
     * @param maxCapacity positive capacity value
     * @throws IllegalArgumentException if {@code maxCapacity} is zero or negative
     */
    public void setMaxCapacity(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity must be greater than zero");
        }
        this.maxCapacity = maxCapacity;
    }

    /**
     * Returns whether the course is active.
     *
     * @return {@code true} when the course is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Updates the active flag.
     *
     * @param active whether the course is active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns a human-readable representation of course state.
     *
     * @return course state string
     */
    @Override
    public String toString() {
        return "Course{"
                + "id=" + id
                + ", courseName='" + courseName + '\''
                + ", description='" + description + '\''
                + ", durationInWeeks=" + durationInWeeks
                + ", trainerId=" + trainerId
                + ", batchName='" + batchName + '\''
                + ", maxCapacity=" + maxCapacity
                + ", active=" + active
                + '}';
    }

    /**
     * Compares this course with another object using field equality.
     *
     * @param object object to compare
     * @return {@code true} when both objects have the same course state
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Course course = (Course) object;
        return id == course.id
                && durationInWeeks == course.durationInWeeks
                && trainerId == course.trainerId
                && maxCapacity == course.maxCapacity
                && active == course.active
                && Objects.equals(courseName, course.courseName)
                && Objects.equals(description, course.description)
                && Objects.equals(batchName, course.batchName);
    }

    /**
     * Computes a hash code from the validated course state.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, courseName, description, durationInWeeks, trainerId, batchName, maxCapacity, active);
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
}
