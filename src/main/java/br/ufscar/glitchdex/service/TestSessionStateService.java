package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.TestSession;
import br.ufscar.glitchdex.domain.TestSessionStateMachine;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TestSessionStateService {

    public void startSession(TestSession session) throws IllegalStatusChangeException {
        TestSessionStateMachine machine = new TestSessionStateMachine(session);
        machine.startSession();
        session.setStartTimestamp(LocalDateTime.now());
    }

    public void finishSession(TestSession session) throws IllegalStatusChangeException {
        TestSessionStateMachine machine = new TestSessionStateMachine(session);
        machine.finalizeSession();
        session.setFinalizationTimestamp(LocalDateTime.now());
    }

    public void canReportBug(TestSession session) throws IllegalStatusChangeException {
        TestSessionStateMachine machine = new TestSessionStateMachine(session);
        machine.canReportBug();
    }

    public void canUpdateSession(TestSession session) throws IllegalStatusChangeException {
        TestSessionStateMachine machine = new TestSessionStateMachine(session);
        machine.canUpdateSession();
    }
}