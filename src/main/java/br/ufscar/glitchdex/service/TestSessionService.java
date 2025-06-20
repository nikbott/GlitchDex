package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.*;
import br.ufscar.glitchdex.dto.TestSessionDTO;
import br.ufscar.glitchdex.dto.TestSessionRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.mapper.TestSessionMapper;
import br.ufscar.glitchdex.repository.ProjectRepository;
import br.ufscar.glitchdex.repository.StrategyRepository;
import br.ufscar.glitchdex.repository.TestSessionRepository;
import br.ufscar.glitchdex.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing test sessions.
 * Provides business logic for creating, retrieving, updating, and deleting test sessions,
 * and for managing their lifecycle.
 */
@Service
@RequiredArgsConstructor
public class TestSessionService {

    private static final Logger log = LoggerFactory.getLogger(TestSessionService.class);
    private final TestSessionRepository testSessionRepository;
    private final StrategyRepository strategyRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TestSessionMapper testSessionMapper;

    /**
     * Finds all test sessions for a given project.
     *
     * @param projectId The ID of the project.
     * @return A list of TestSessionDTOs.
     */
    public List<TestSessionDTO> findByProjectId(Long projectId) {
        log.info("Finding test sessions for project with id: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        return testSessionMapper.toTestSessionDTOs(testSessionRepository.findByProject(project));
    }

    /**
     * Finds all test sessions using a given strategy.
     *
     * @param strategy The strategy.
     * @return A list of TestSessionDTOs.
     */
    public List<TestSessionDTO> findByStrategy(Strategy strategy) {
        log.info("Finding test sessions for strategy with id: {}", strategy.getId());
        return testSessionMapper.toTestSessionDTOs(testSessionRepository.findByStrategy(strategy));
    }

    /**
     * Finds all test sessions using a given strategy by its ID.
     *
     * @param strategyId The ID of the strategy.
     * @return A list of TestSessionDTOs.
     */
    public List<TestSessionDTO> findByStrategyId(Long strategyId) {
        log.info("Finding test sessions for strategy with id: {}", strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found with id: " + strategyId));
        return findByStrategy(strategy);
    }

    /**
     * Finds a test session by its ID and returns its DTO.
     *
     * @param id The ID of the test session.
     * @return The TestSessionDTO.
     * @throws ResourceNotFoundException if no test session is found with the given ID.
     */
    public TestSessionDTO findById(Long id) {
        log.info("Finding test session with id: {}", id);
        TestSession session = testSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test Session not found with id: " + id));
        return testSessionMapper.toTestSessionDTO(session);
    }

    /**
     * Creates a new test session.
     *
     * @param sessionRequest The request object with the session details.
     * @param testerDto      The DTO of the user conducting the test.
     * @return The created TestSessionDTO.
     */
    @Transactional
    public TestSessionDTO create(TestSessionRequest sessionRequest, UserDTO testerDto) {
        log.info("User {} is creating a test session for project {}", testerDto.getEmail(), sessionRequest.getProjectId());
        User tester = userRepository.findById(testerDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + testerDto.getId()));
        Strategy strategy = strategyRepository.findById(sessionRequest.getStrategyId())
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found with id: " + sessionRequest.getStrategyId()));
        Project project = projectRepository.findById(sessionRequest.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + sessionRequest.getProjectId()));

        TestSession testSession = new TestSession();
        testSession.setStrategy(strategy);
        testSession.setTester(tester);
        testSession.setDurationInMinutes(sessionRequest.getDurationInMinutes());
        testSession.setDescription(sessionRequest.getDescription());
        testSession.setProject(project);

        TestSession savedSession = testSessionRepository.save(testSession);
        log.info("Test session created successfully with id: {}", savedSession.getId());
        return testSessionMapper.toTestSessionDTO(savedSession);
    }

    /**
     * Updates an existing test session.
     *
     * @param id                 The ID of the session to update.
     * @param testSessionRequest The request object with the updated session details.
     * @param userDto            The DTO of the user performing the update.
     * @return The updated TestSessionDTO.
     * @throws IllegalStatusChangeException if the session's state transition is invalid.
     */
    @Transactional
    public TestSessionDTO update(Long id, TestSessionRequest testSessionRequest, UserDTO userDto) throws IllegalStatusChangeException {
        log.info("User {} is updating test session with id: {}", userDto.getEmail(), id);
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userDto.getId()));
        TestSession testSession = testSessionRepository.findByIdAndTester(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Test Session not found with id: " + id));

        TestSessionStateMachine stateMachine = new TestSessionStateMachine(testSession);
        stateMachine.canUpdateSession();

        Strategy strategy = strategyRepository.findById(testSessionRequest.getStrategyId())
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found with id: " + testSessionRequest.getStrategyId()));

        testSession.setStrategy(strategy);
        testSession.setTester(user);
        testSession.setDurationInMinutes(testSessionRequest.getDurationInMinutes());

        TestSession updatedSession = testSessionRepository.save(testSession);
        log.info("Test session with id {} updated successfully", id);
        return testSessionMapper.toTestSessionDTO(updatedSession);
    }

    /**
     * Deletes a test session by its ID.
     *
     * @param id The ID of the session to delete.
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting test session with id: {}", id);
        if (!testSessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Test Session not found with id: " + id);
        }
        testSessionRepository.deleteById(id);
        log.info("Test session with id {} deleted successfully", id);
    }

    /**
     * Starts a test session.
     *
     * @param id The ID of the session to start.
     * @return The updated TestSessionDTO.
     * @throws IllegalStatusChangeException if the session cannot be started.
     */
    @Transactional
    public TestSessionDTO startSession(Long id) throws IllegalStatusChangeException {
        log.info("Starting test session with id: {}", id);
        TestSession testSession = testSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test Session not found with id: " + id));

        TestSessionStateMachine stateMachine = new TestSessionStateMachine(testSession);
        stateMachine.startSession();

        testSession.setStartTimestamp(LocalDateTime.now());
        TestSessionDTO dto = testSessionMapper.toTestSessionDTO(testSessionRepository.save(testSession));
        log.info("Test session with id {} started successfully", id);
        return dto;
    }

    /**
     * Finalizes a test session.
     *
     * @param id The ID of the session to finalize.
     * @return The updated TestSessionDTO.
     * @throws IllegalStatusChangeException if the session cannot be finalized.
     */
    @Transactional
    public TestSessionDTO finalizeSession(Long id) throws IllegalStatusChangeException {
        log.info("Finalizing test session with id: {}", id);
        TestSession testSession = testSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test Session not found with id: " + id));

        TestSessionStateMachine stateMachine = new TestSessionStateMachine(testSession);
        stateMachine.finalizeSession();

        testSession.setFinalizationTimestamp(LocalDateTime.now());
        TestSessionDTO dto = testSessionMapper.toTestSessionDTO(testSessionRepository.save(testSession));
        log.info("Test session with id {} finalized successfully", id);
        return dto;
    }
}