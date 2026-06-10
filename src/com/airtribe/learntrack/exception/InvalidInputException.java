package com.airtribe.learntrack.exception;

/**
 * Exception thrown when caller-provided input violates application rules.
 */
public class InvalidInputException extends LearnTrackException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an invalid-input exception with a diagnostic message.
     *
     * @param message explanation of the invalid input
     */
    public InvalidInputException(String message) {
        super(message);
    }
}
