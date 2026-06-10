package com.airtribe.learntrack.exception;

/**
 * Base unchecked exception for LearnTrack application faults.
 *
 * <p>Unchecked exceptions keep inner layers free from repetitive catch-or-throw
 * boilerplate while still allowing the application boundary to handle failures
 * consistently.</p>
 */
public class LearnTrackException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a LearnTrack exception with a user-facing diagnostic message.
     *
     * @param message explanation of the application fault
     */
    public LearnTrackException(String message) {
        super(message);
    }
}
