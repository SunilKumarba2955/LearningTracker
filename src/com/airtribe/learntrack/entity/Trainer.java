package com.airtribe.learntrack.entity;

import java.util.Objects;

/**
 * Trainer domain entity with specialization details.
 */
public class Trainer extends Person {
    private String specialization;

    /**
     * Creates a trainer with validated identity, contact, and specialization details.
     *
     * @param id unique positive trainer identifier
     * @param firstName non-blank first name
     * @param lastName non-blank last name
     * @param email non-blank email address containing {@code @}
     * @param specialization non-blank teaching specialization
     * @throws IllegalArgumentException if any argument violates the entity invariants
     */
    public Trainer(int id, String firstName, String lastName, String email, String specialization) {
        super(id, firstName, lastName, email);
        setSpecialization(specialization);
    }

    /**
     * Returns the trainer specialization.
     *
     * @return non-blank specialization
     */
    public String getSpecialization() {
        return specialization;
    }

    /**
     * Updates the trainer specialization.
     *
     * @param specialization non-blank teaching specialization
     * @throws IllegalArgumentException if {@code specialization} is null or blank
     */
    public void setSpecialization(String specialization) {
        this.specialization = requireNotBlank(specialization, "specialization");
    }

    /**
     * Returns the trainer-specific business display label.
     *
     * @return formatted trainer display name
     */
    @Override
    public String getDisplayName() {
        return "Trainer: " + getFullName() + " (Specialization: " + specialization + ")";
    }

    /**
     * Returns a human-readable representation of trainer state.
     *
     * @return trainer state string
     */
    @Override
    public String toString() {
        return "Trainer{"
                + "id=" + getId()
                + ", firstName='" + getFirstName() + '\''
                + ", lastName='" + getLastName() + '\''
                + ", email='" + getEmail() + '\''
                + ", specialization='" + specialization + '\''
                + '}';
    }

    /**
     * Compares this trainer with another object using class and field equality.
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
        Trainer trainer = (Trainer) object;
        return Objects.equals(specialization, trainer.specialization);
    }

    /**
     * Computes a hash code from the validated trainer state.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), specialization);
    }
}
