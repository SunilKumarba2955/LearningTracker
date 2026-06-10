package com.airtribe.learntrack.exception;

/**
 * Exception thrown when an application transaction cannot be completed safely.
 */
public class TransactionException extends LearnTrackException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a transaction exception with a diagnostic message.
     *
     * @param message explanation of the transaction failure
     */
    public TransactionException(String message) {
        super(message);
    }
}
