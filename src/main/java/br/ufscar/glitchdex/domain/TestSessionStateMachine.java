package br.ufscar.glitchdex.domain;

import br.ufscar.glitchdex.exception.IllegalStatusChangeException;

public class TestSessionStateMachine {

    private final TestSession testSession;

    public TestSessionStateMachine(TestSession testSession) {
        this.testSession = testSession;
    }

    public void startSession() throws IllegalStatusChangeException {
        if (testSession.getStatus() != SessionStatus.CREATED) {
            throw new IllegalStatusChangeException("error.session.start_invalid_status");
        }
        testSession.setStatus(SessionStatus.IN_EXECUTION);
    }

    public void finalizeSession() throws IllegalStatusChangeException {
        if (testSession.getStatus() != SessionStatus.IN_EXECUTION) {
            throw new IllegalStatusChangeException("error.session.finalize_invalid_status");
        }
        testSession.setStatus(SessionStatus.FINALIZED);
    }

    public void canReportBug() throws IllegalStatusChangeException {
        if (testSession.getStatus() != SessionStatus.IN_EXECUTION) {
            throw new IllegalStatusChangeException("error.session.report_bug_invalid_status");
        }
    }

    public void canUpdateSession() throws IllegalStatusChangeException {
        if (testSession.getStatus() == SessionStatus.FINALIZED) {
            throw new IllegalStatusChangeException("error.session.update_finalized");
        }
    }
}