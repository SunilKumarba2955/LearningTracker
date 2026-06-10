package com.airtribe.learntrack.exception;

/**
 * Exception thrown when a requested entity cannot be located.
 */
public class EntityNotFoundException extends LearnTrackException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an entity-not-found exception with a standardized message.
     *
     * @param entityType name of the missing entity type
     * @param id identifier that could not be found
     */
    public EntityNotFoundException(String entityType, int id) {
        super(String.format("Data violation: Target %s with ID %d could not be found.", entityType, id));
    }
}
