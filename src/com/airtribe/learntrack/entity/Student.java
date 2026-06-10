package com.airtribe.learntrack.entity;

import java.util.Objects;

/**
 * Student domain entity with enrollment-facing profile information.
 */
public class Student extends Person {
    private String batch;
    private boolean active;

    /**
     * Creates a student with explicit contact and batch details.
     *
     * @param id unique positive student identifier
     * @param firstName non-blank first name
     * @param lastName non-blank last name
     * @param email non-blank email address containing {@code @}
     * @param batch non-blank batch code or name
     * @param active whether the student can access active system workflows
     * @throws IllegalArgumentException if any argument violates the entity invariants
     */
    public Student(int id, String firstName, String lastName, String email, String batch, boolean active) {
        super(id, firstName, lastName, email);
        setBatch(batch);
        setActive(active);
    }

    /**
     * Creates a student and assigns a deterministic internal email address.
     *
     * @param id unique positive student identifier
     * @param firstName non-blank first name
     * @param lastName non-blank last name
     * @param batch non-blank batch code or name
     * @param active whether the student can access active system workflows
     * @throws IllegalArgumentException if any argument violates the entity invariants
     */
    public Student(int id, String firstName, String lastName, String batch, boolean active) {
        this(id, firstName, lastName, "student-" + id + "@learntrack.local", batch, active);
    }

    /**
     * Returns the assigned batch.
     *
     * @return non-blank batch code or name
     */
    public String getBatch() {
        return batch;
    }

    /**
     * Updates the assigned batch.
     *
     * @param batch non-blank batch code or name
     * @throws IllegalArgumentException if {@code batch} is null or blank
     */
    public void setBatch(String batch) {
        this.batch = requireNotBlank(batch, "batch");
    }

    /**
     * Returns whether the student is active.
     *
     * @return {@code true} when the student is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Updates the active flag.
     *
     * @param active whether the student can access active system workflows
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the student-specific business display label.
     *
     * @return formatted student display name
     */
    @Override
    public String getDisplayName() {
        return "Student: " + getFullName() + " (Batch: " + batch + ")";
    }

    /**
     * Returns a human-readable representation of student state.
     *
     * @return student state string
     */
    @Override
    public String toString() {
        return "Student{"
                + "id=" + getId()
                + ", firstName='" + getFirstName() + '\''
                + ", lastName='" + getLastName() + '\''
                + ", email='" + getEmail() + '\''
                + ", batch='" + batch + '\''
                + ", active=" + active
                + '}';
    }

    /**
     * Compares this student with another object using class and field equality.
     *
     * @param object object to compare
     * @return {@code true} when both objects have the same concrete class and state
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!super.equals(object)) {
            return false;
        }
        Student student = (Student) object;
        return active == student.active && Objects.equals(batch, student.batch);
    }

    /**
     * Computes a hash code from the validated student state.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), batch, active);
    }
}
