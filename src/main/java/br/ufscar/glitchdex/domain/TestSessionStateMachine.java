package br.ufscar.glitchdex.domain;

import br.ufscar.glitchdex.exception.IllegalStatusChangeException;

/**
 * Manages the state transitions of a TestSession.
 * Ensures that status changes follow the defined lifecycle rules.
 */
public class TestSessionStateMachine {

    private final TestSession testSession;

    /**
     * Constructs a new state machine for the given test session.
     *
     * @param testSession The test session to manage.
     */
    public TestSessionStateMachine(TestSession testSession) {
        this.testSession = testSession;
    }

    /**
     * Transitions the session to the IN_EXECUTION state.
     *
     * @throws IllegalStatusChangeException if the session is not in the CREATED state.
     */
    public void startSession() throws IllegalStatusChangeException {
        if (testSession.getStatus() != SessionStatus.CREATED) {
            throw new IllegalStatusChangeException("error.session.start_invalid_status");
        }
        testSession.setStatus(SessionStatus.IN_EXECUTION);
    }

    /**
     * Transitions the session to the FINALIZED state.
     *
     * @throws IllegalStatusChangeException if the session is not in the IN_EXECUTION state.
     */
    public void finalizeSession() throws IllegalStatusChangeException {
        if (testSession.getStatus() != SessionStatus.IN_EXECUTION) {
            throw new IllegalStatusChangeException("error.session.finalize_invalid_status");
        }
        testSession.setStatus(SessionStatus.FINALIZED);
    }

    /**
     * Checks if a bug can be reported for the session.
     *
     * @throws IllegalStatusChangeException if the session is not in the IN_EXECUTION state.
     */
    public void canReportBug() throws IllegalStatusChangeException {
        if (testSession.getStatus() != SessionStatus.IN_EXECUTION) {
            throw new IllegalStatusChangeException("error.session.report_bug_invalid_status");
        }
    }

    /**
     * Checks if the session can be updated.
     *
     * @throws IllegalStatusChangeException if the session is in the FINALIZED state.
     */
    public void canUpdateSession() throws IllegalStatusChangeException {
        if (testSession.getStatus() == SessionStatus.FINALIZED) {
            throw new IllegalStatusChangeException("error.session.update_finalized");
        }
    }
}