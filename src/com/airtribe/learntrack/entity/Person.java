package com.airtribe.learntrack.entity;

import java.util.Objects;

/**
 * Base abstraction for people represented in LearnTrack.
 *
 * <p>The class owns shared identity and contact invariants while leaving
 * role-specific display behavior to subclasses.</p>
 */
public abstract class Person {
    private int id;
    private String firstName;
    private String lastName;
    private String email;

    /**
     * Creates a person with validated identity and contact details.
     *
     * @param id unique positive identifier
     * @param firstName non-blank first name
     * @param lastName non-blank last name
     * @param email non-blank email address containing {@code @}
     * @throws IllegalArgumentException if any argument violates the entity invariants
     */
    protected Person(int id, String firstName, String lastName, String email) {
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
    }

    /**
     * Returns the unique identifier.
     *
     * @return positive person identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Updates the unique identifier.
     *
     * @param id positive person identifier
     * @throws IllegalArgumentException if {@code id} is zero or negative
     */
    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be greater than zero");
        }
        this.id = id;
    }

    /**
     * Returns the first name.
     *
     * @return non-blank first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Updates the first name.
     *
     * @param firstName non-blank first name
     * @throws IllegalArgumentException if {@code firstName} is null or blank
     */
    public void setFirstName(String firstName) {
        this.firstName = requireNotBlank(firstName, "firstName");
    }

    /**
     * Returns the last name.
     *
     * @return non-blank last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Updates the last name.
     *
     * @param lastName non-blank last name
     * @throws IllegalArgumentException if {@code lastName} is null or blank
     */
    public void setLastName(String lastName) {
        this.lastName = requireNotBlank(lastName, "lastName");
    }

    /**
     * Returns the email address.
     *
     * @return non-blank email address containing {@code @}
     */
    public String getEmail() {
        return email;
    }

    /**
     * Updates the email address.
     *
     * @param email non-blank email address containing {@code @}
     * @throws IllegalArgumentException if {@code email} is null, blank, or missing {@code @}
     */
    public void setEmail(String email) {
        String sanitizedEmail = requireNotBlank(email, "email");
        if (!sanitizedEmail.contains("@")) {
            throw new IllegalArgumentException("email must contain @");
        }
        this.email = sanitizedEmail;
    }

    /**
     * Returns the business-facing display label for the person subtype.
     *
     * @return formatted display name
     */
    public abstract String getDisplayName();

    /**
     * Returns a human-readable representation of shared person state.
     *
     * @return person state string
     */
    @Override
    public String toString() {
        return "Person{"
                + "id=" + id
                + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", email='" + email + '\''
                + '}';
    }

    /**
     * Compares this person with another object using class and field equality.
     *
     * @param object object to compare
     * @return {@code true} when both objects have the same concrete class and state
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Person person = (Person) object;
        return id == person.id
                && Objects.equals(firstName, person.firstName)
                && Objects.equals(lastName, person.lastName)
                && Objects.equals(email, person.email);
    }

    /**
     * Computes a hash code from the validated person state.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, email);
    }

    protected static String requireNotBlank(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmedValue;
    }

    protected final String getFullName() {
        return firstName + " " + lastName;
    }
}
