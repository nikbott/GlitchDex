package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.*;
import br.ufscar.glitchdex.dto.TestSessionDTO;
import br.ufscar.glitchdex.dto.TestSessionRequest;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.mapper.TestSessionMapper;
import br.ufscar.glitchdex.repository.ProjectRepository;
import br.ufscar.glitchdex.repository.StrategyRepository;
import br.ufscar.glitchdex.repository.TestSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestSessionService {

    private final TestSessionRepository testSessionRepository;
    private final StrategyRepository strategyRepository;
    private final ProjectRepository projectRepository;
    private final TestSessionMapper testSessionMapper;

    public List<TestSessionDTO> findByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        return testSessionMapper.toTestSessionDTOs(testSessionRepository.findByProject(project));
    }

    public List<TestSessionDTO> findByStrategy(Strategy strategy) {
        return testSessionMapper.toTestSessionDTOs(testSessionRepository.findByStrategy(strategy));
    }

    public TestSessionDTO findById(Long id) {
        TestSession session = testSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test Session not found with id: " + id));
        return testSessionMapper.toTestSessionDTO(session);
    }

    @Transactional
    public TestSessionDTO create(TestSessionRequest sessionRequest, User tester) {
        Strategy strategy = strategyRepository.findById(sessionRequest.getStrategyId())
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found with id: " + sessionRequest.getStrategyId()));

        TestSession testSession = new TestSession();
        testSession.setStrategy(strategy);
        testSession.setTester(tester);
        testSession.setStatus(sessionRequest.getStatus());
        testSession.setSessionDate(LocalDate.now());
        testSession.setDurationInMinutes(sessionRequest.getDurationInMinutes());


        TestSession savedSession = testSessionRepository.save(testSession);
        return testSessionMapper.toTestSessionDTO(savedSession);
    }

    @Transactional
    public TestSessionDTO update(Long id, TestSessionRequest testSessionRequest, User user) throws IllegalStatusChangeException {
        TestSession testSession = testSessionRepository.findByIdAndTester(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Test Session not found with id: " + id));

        if (SessionStatus.FINALIZED == testSession.getStatus()) {
            throw new IllegalStatusChangeException("Cannot modify a finalized session.");
        }

        Strategy strategy = strategyRepository.findById(testSessionRequest.getStrategyId())
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found with id: " + testSessionRequest.getStrategyId()));

        // Perform a "full update" by setting all fields from the request
        testSession.setStrategy(strategy);
        testSession.setTester(user);
        testSession.setStatus(testSessionRequest.getStatus());
        testSession.setDurationInMinutes(testSessionRequest.getDurationInMinutes());


        TestSession updatedSession = testSessionRepository.save(testSession);
        return testSessionMapper.toTestSessionDTO(updatedSession);
    }

    @Transactional
    public void delete(Long id) {
        if (!testSessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Test Session not found with id: " + id);
        }
        testSessionRepository.deleteById(id);
    }

    /**
     * Starts a test session by setting its status to IN_EXECUTION.
     *
     * @param id The ID of the test session to start.
     * @return The updated TestSession entity.
     */
    @Transactional
    public TestSession startSession(Long id) {
        TestSession testSession = testSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test Session not found with id: " + id));
        testSession.start(); // This calls the logic within your TestSession entity
        return testSessionRepository.save(testSession);
    }

    /**
     * Finalizes a test session by setting its status to FINALIZED.
     *
     * @param id The ID of the test session to finalize.
     * @return The updated TestSession entity.
     */
    @Transactional
    public TestSession finalizeSession(Long id) {
        TestSession testSession = testSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test Session not found with id: " + id));
        testSession.finish();
        return testSessionRepository.save(testSession);
    }
}