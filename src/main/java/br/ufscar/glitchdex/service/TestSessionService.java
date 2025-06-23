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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
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
    private final MessageSource messageSource;

    /**
     * Finds all test sessions for a given project.
     *
     * @param projectId The ID of the project.
     * @return A list of TestSessionDTOs.
     */
    public List<TestSessionDTO> findByProjectId(Long projectId) {
        log.info("Finding test sessions for project with id: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.project.not_found", new Object[]{projectId}, LocaleContextHolder.getLocale())));
        return testSessionMapper.toTestSessionDTOs(testSessionRepository.findByProject(project));
    }

    /**
     * Finds all test sessions for a given strategy.
     *
     * @param strategy The strategy to search by.
     * @return A list of TestSessionDTOs.
     */
    public List<TestSessionDTO> findByStrategy(Strategy strategy) {
        log.info("Finding test sessions for strategy with id: {}", strategy.getId());
        return testSessionMapper.toTestSessionDTOs(testSessionRepository.findByStrategy(strategy));
    }

    /**
     * Finds all test sessions for a given strategy ID.
     *
     * @param strategyId The ID of the strategy.
     * @return A list of TestSessionDTOs.
     */
    public List<TestSessionDTO> findByStrategyId(Long strategyId) {
        log.info("Finding test sessions for strategy with id: {}", strategyId);
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.strategy.not_found", new Object[]{strategyId}, LocaleContextHolder.getLocale())));
        return findByStrategy(strategy);
    }

    /**
     * Finds a test session by its ID.
     *
     * @param id The ID of the test session.
     * @return The TestSessionDTO for the requested session.
     */
    public TestSessionDTO findById(Long id) {
        log.info("Finding test session with id: {}", id);
        TestSession session = testSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.session.not_found", new Object[]{id}, LocaleContextHolder.getLocale())));
        return testSessionMapper.toTestSessionDTO(session);
    }

    /**
     * Creates a new test session.
     *
     * @param sessionRequest The request body containing the test session details.
     * @param testerDto      The DTO of the user creating the session.
     * @return The created TestSessionDTO.
     */
    @Transactional
    public TestSessionDTO create(TestSessionRequest sessionRequest, UserDTO testerDto) {
        log.info("User {} is creating a test session for project {}", testerDto.getEmail(), sessionRequest.getProjectId());
        User tester = userRepository.findById(testerDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.user.not_found", new Object[]{testerDto.getId()}, LocaleContextHolder.getLocale())));
        Strategy strategy = strategyRepository.findById(sessionRequest.getStrategyId())
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.strategy.not_found", new Object[]{sessionRequest.getStrategyId()}, LocaleContextHolder.getLocale())));
        Project project = projectRepository.findById(sessionRequest.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.project.not_found", new Object[]{sessionRequest.getProjectId()}, LocaleContextHolder.getLocale())));

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
     * @param id                 The ID of the test session to update.
     * @param testSessionRequest The request body containing the updated session details.
     * @param userDto            The DTO of the user updating the session.
     * @return The updated TestSessionDTO.
     * @throws IllegalStatusChangeException if the session status change is invalid.
     */
    @Transactional
    public TestSessionDTO update(Long id, TestSessionRequest testSessionRequest, UserDTO userDto) throws IllegalStatusChangeException {
        log.info("User {} is updating test session with id: {}", userDto.getEmail(), id);
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.user.not_found", new Object[]{userDto.getId()}, LocaleContextHolder.getLocale())));
        TestSession testSession = testSessionRepository.findByIdAndTester(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.session.not_found", new Object[]{id}, LocaleContextHolder.getLocale())));

        TestSessionStateMachine machine = new TestSessionStateMachine(testSession);
        machine.canUpdateSession();

        Strategy strategy = strategyRepository.findById(testSessionRequest.getStrategyId())
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.strategy.not_found", new Object[]{testSessionRequest.getStrategyId()}, LocaleContextHolder.getLocale())));

        testSession.setStrategy(strategy);
        testSession.setDurationInMinutes(testSessionRequest.getDurationInMinutes());
        testSession.setDescription(testSessionRequest.getDescription());

        TestSession updatedSession = testSessionRepository.save(testSession);
        log.info("Test session with id {} updated successfully", id);
        return testSessionMapper.toTestSessionDTO(updatedSession);
    }

    /**
     * Deletes a test session by its ID.
     *
     * @param id The ID of the test session to delete.
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting test session with id: {}", id);
        if (!testSessionRepository.existsById(id)) {
            throw new ResourceNotFoundException(messageSource.getMessage("error.session.not_found", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        testSessionRepository.deleteById(id);
        log.info("Test session with id {} deleted successfully", id);
    }

    /**
     * Starts a test session.
     *
     * @param id   The ID of the test session to start.
     * @param user The DTO of the user starting the session.
     * @return The updated TestSessionDTO.
     * @throws IllegalStatusChangeException if the session cannot be started.
     */
    @Transactional
    public TestSessionDTO startSession(Long id, UserDTO user) throws IllegalStatusChangeException {
        verifyOwnership(id, user.getId());
        log.info("Starting test session with id: {}", id);
        TestSession testSession = findByIdEntity(id);
        TestSessionStateMachine machine = new TestSessionStateMachine(testSession);
        machine.startSession();
        testSession.setStartTimestamp(LocalDateTime.now());
        TestSessionDTO dto = testSessionMapper.toTestSessionDTO(save(testSession));
        log.info("Test session with id {} started successfully", id);
        return dto;
    }

    /**
     * Finalizes a test session.
     *
     * @param id   The ID of the test session to finalize.
     * @param user The DTO of the user finalizing the session.
     * @return The updated TestSessionDTO.
     * @throws IllegalStatusChangeException if the session cannot be finalized.
     */
    @Transactional
    public TestSessionDTO finishSession(Long id, UserDTO user) throws IllegalStatusChangeException {
        verifyOwnership(id, user.getId());
        log.info("Finalizing test session with id: {}", id);
        TestSession testSession = findByIdEntity(id);
        TestSessionStateMachine machine = new TestSessionStateMachine(testSession);
        machine.finalizeSession();
        testSession.setFinalizationTimestamp(LocalDateTime.now());
        TestSessionDTO dto = testSessionMapper.toTestSessionDTO(save(testSession));
        log.info("Test session with id {} finalized successfully", id);
        return dto;
    }

    /**
     * Finds a test session by its ID.
     *
     * @param id The ID of the test session.
     * @return The TestSession entity.
     */
    public TestSession findByIdEntity(Long id) {
        return testSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.session.not_found", new Object[]{id}, LocaleContextHolder.getLocale())));
    }

    /**
     * Verifies that a user is the owner of a test session.
     *
     * @param sessionId The ID of the test session.
     * @param userId    The ID of the user.
     * @throws AccessDeniedException if the user is not the owner.
     */
    public void verifyOwnership(Long sessionId, Long userId) {
        TestSession session = testSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.session.not_found", new Object[]{sessionId}, LocaleContextHolder.getLocale())));
        if (!session.getTester().getId().equals(userId)) {
            throw new AccessDeniedException(messageSource.getMessage("error.session.access_denied", null, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Saves a test session.
     *
     * @param session The TestSession to save.
     * @return The saved TestSession.
     */
    @Transactional
    public TestSession save(TestSession session) {
        return testSessionRepository.save(session);
    }

    /**
     * Appends a description to a test session if it is in execution.
     *
     * @param id                    The ID of the test session.
     * @param additionalDescription The description to append.
     */
    @Transactional
    public void appendDescriptionIfInExecution(Long id, String additionalDescription) {
        TestSession session = findByIdEntity(id);

        if (session.getStatus() != SessionStatus.IN_EXECUTION) {
            throw new IllegalStateException(messageSource.getMessage("error.session.update_description_not_in_execution", null, LocaleContextHolder.getLocale()));
        }

        String currentDescription = session.getDescription() != null ? session.getDescription() : "";
        String newDescription = currentDescription + (currentDescription.isEmpty() ? "" : " ") + additionalDescription;

        session.setDescription(newDescription);

        save(session);
    }

    /**
     * Updates expired test sessions for a project.
     *
     * @param projectId The ID of the project.
     */
    @Transactional
    public void updateExpiredSessionsByProject(Long projectId) {
        log.info("Verificando sessões expiradas para o projeto {}", projectId);
        List<TestSession> activeSessions = testSessionRepository.findByProjectIdAndStatus(
                projectId, SessionStatus.IN_EXECUTION
        );
        LocalDateTime now = LocalDateTime.now();
        long updatedCount = activeSessions.stream()
                .filter(session -> isSessionExpired(session, now))
                .map(session -> finalizeAndSaveSilently(session, now))
                .filter(success -> success)
                .count();

        if (updatedCount > 0) {
            log.info("Finalizadas {} sessões expiradas do projeto {}", updatedCount, projectId);
        }
    }

    private boolean finalizeAndSaveSilently(TestSession session, LocalDateTime now) {
        try {
            TestSessionStateMachine machine = new TestSessionStateMachine(session);
            machine.finalizeSession();
            session.setFinalizationTimestamp(now);
            save(session);
            log.debug("Sessão {} expirada e finalizada automaticamente", session.getId());
            return true;
        } catch (IllegalStatusChangeException e) {
            log.error("Erro ao finalizar sessão expirada {}: {}", session.getId(), e.getMessage());
            return false;
        }
    }

    private boolean isSessionExpired(TestSession session, LocalDateTime now) {
        if (session.getDurationInMinutes() == null) {
            return false;
        }
        LocalDateTime referenceTime = session.getStartTimestamp() != null
                ? session.getStartTimestamp()
                : session.getCreatedAt();

        LocalDateTime expirationTime = referenceTime.plusMinutes(session.getDurationInMinutes());
        return now.isAfter(expirationTime);
    }

    /**
     * Checks if a bug can be reported for a session.
     *
     * @param session The session to check.
     * @throws IllegalStatusChangeException if a bug cannot be reported.
     */
    public void canReportBug(TestSession session) throws IllegalStatusChangeException {
        TestSessionStateMachine machine = new TestSessionStateMachine(session);
        machine.canReportBug();
    }
}