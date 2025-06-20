package br.ufscar.glitchdex.domain;

import br.ufscar.glitchdex.exception.IllegalStatusChangeException;

public class TestSessionStateMachine {

    private final TestSession testSession;

    public TestSessionStateMachine(TestSession testSession) {
        this.testSession = testSession;
    }

    public void startSession() throws IllegalStatusChangeException {
        if (SessionStatus.CREATED != testSession.getStatus()) {
            throw new IllegalStatusChangeException("Session can only be started when in CREATED status");
        }
        testSession.setStatus(SessionStatus.IN_EXECUTION);
    }

    public void finalizeSession() throws IllegalStatusChangeException {
        if (SessionStatus.IN_EXECUTION != testSession.getStatus()) {
            throw new IllegalStatusChangeException("Session can only be finalized when in IN_EXECUTION status");
        }
        testSession.setStatus(SessionStatus.FINALIZED);
    }

    public void canReportBug() throws IllegalStatusChangeException {
        if (SessionStatus.IN_EXECUTION != testSession.getStatus()) {
            throw new IllegalStatusChangeException("Bugs can only be reported for sessions that are in execution.");
        }
    }

    public void canUpdateSession() throws IllegalStatusChangeException {
        if (SessionStatus.FINALIZED == testSession.getStatus()) {
            throw new IllegalStatusChangeException("Cannot modify a finalized session.");
        }
    }
}