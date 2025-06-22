package br.ufscar.glitchdex.domain;

import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class TestSessionStateMachine {

    private final TestSession testSession;
    private final MessageSource messageSource;

    public TestSessionStateMachine(TestSession testSession, MessageSource messageSource) {
        this.testSession = testSession;
        this.messageSource = messageSource;
    }

    public void startSession() throws IllegalStatusChangeException {
        if (testSession.getStatus() != SessionStatus.CREATED) {
            throw new IllegalStatusChangeException(messageSource.getMessage("error.session.start_invalid_status", null, LocaleContextHolder.getLocale()));
        }
        testSession.setStatus(SessionStatus.IN_EXECUTION);
    }

    public void finalizeSession() throws IllegalStatusChangeException {
        if (testSession.getStatus() != SessionStatus.IN_EXECUTION) {
            throw new IllegalStatusChangeException(messageSource.getMessage("error.session.finalize_invalid_status", null, LocaleContextHolder.getLocale()));
        }
        testSession.setStatus(SessionStatus.FINALIZED);
    }

    public void canReportBug() throws IllegalStatusChangeException {
        if (testSession.getStatus() != SessionStatus.IN_EXECUTION) {
            throw new IllegalStatusChangeException(messageSource.getMessage("error.session.report_bug_invalid_status", null, LocaleContextHolder.getLocale()));
        }
    }

    public void canUpdateSession() throws IllegalStatusChangeException {
        if (testSession.getStatus() == SessionStatus.FINALIZED) {
            throw new IllegalStatusChangeException(messageSource.getMessage("error.session.update_finalized", null, LocaleContextHolder.getLocale()));
        }
    }
}