package br.ufscar.glitchdex.domain;

/**
 * Enum representing the status of a test session.
 */
public enum SessionStatus {
    CREATED,       // Session has been created but not started
    IN_EXECUTION,  // Session is currently being executed
    FINALIZED      // Session has been completed
}