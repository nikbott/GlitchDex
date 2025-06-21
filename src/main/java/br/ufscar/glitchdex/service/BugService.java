package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.*;
import br.ufscar.glitchdex.dto.BugDTO;
import br.ufscar.glitchdex.dto.BugRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.mapper.BugMapper;
import br.ufscar.glitchdex.repository.BugRepository;
import br.ufscar.glitchdex.repository.TestSessionRepository;
import br.ufscar.glitchdex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors; // Adicionado import para Collectors

/**
 * Service class for managing bugs.
 * Provides business logic for creating, retrieving, updating, and deleting bugs.
 */
@Service
@RequiredArgsConstructor
public class BugService {

    private static final Logger log = LoggerFactory.getLogger(BugService.class);
    private final BugRepository bugRepository;
    private final TestSessionRepository testSessionRepository;
    private final UserRepository userRepository;
    private final BugMapper bugMapper;
    private final FileStorageService fileStorageService;

    /**
     * Finds a bug by its ID.
     *
     * @param id The ID of the bug.
     * @return The BugDTO for the found bug.
     * @throws ResourceNotFoundException if no bug is found with the given ID.
     */
    public BugDTO findById(Long id) {
        log.info("Finding bug with id: {}", id);
        // ALTERAÇÃO AQUI: Usar findByIdWithReporter para carregar o reporter junto
        Bug bug = bugRepository.findByIdWithReporter(id) // <-- MUDANÇA PRINCIPAL
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id " + id));
        return bugMapper.toBugDTO(bug);
    }

    /**
     * Finds all bugs associated with a given test session.
     *
     * @param testSession The test session.
     * @return A list of BugDTOs.
     */
    // Se este método também for exibir o nome do reporter, você precisaria de um
    // método correspondente no BugRepository com JOIN FETCH.
    // Por exemplo:
    // @Query("SELECT b FROM Bug b JOIN FETCH b.reporter WHERE b.testSession.id = :testSessionId")
    // List<Bug> findByTestSessionIdWithReporter(@Param("testSessionId") Long testSessionId);
    // E então chamaria este método aqui:
    // return bugRepository.findByTestSessionIdWithReporter(testSession.getId()).stream()
    //         .map(bugMapper::toBugDTO)
    //         .collect(Collectors.toList());
    public List<BugDTO> findByTestSession(TestSession testSession) {
        log.info("Finding bugs for test session with id: {}", testSession.getId());
        return bugRepository.findByTestSession(testSession).stream()
                .map(bugMapper::toBugDTO)
                .collect(Collectors.toList());
    }


    /**
     * Creates a new bug.
     *
     * @param bugRequest  The request object containing the bug details.
     * @param reporterDto The DTO of the user reporting the bug.
     * @param attachment  The file attached to the bug report.
     * @return The created BugDTO.
     * @throws IllegalStatusChangeException if a bug cannot be reported for the session's current state.
     */
    @Transactional
    public BugDTO create(BugRequest bugRequest, UserDTO reporterDto, MultipartFile attachment) throws IllegalStatusChangeException {
        log.info("User {} is creating a bug for test session {}", reporterDto.getEmail(), bugRequest.getTestSessionId());
        TestSession testSession = testSessionRepository.findById(bugRequest.getTestSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Test Session not found with id: " + bugRequest.getTestSessionId()));

        TestSessionStateMachine stateMachine = new TestSessionStateMachine(testSession);
        stateMachine.canReportBug();

        User reporter = userRepository.findById(reporterDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + reporterDto.getId()));

        String attachmentFilename = null;
        if (attachment != null && !attachment.isEmpty()) {
            attachmentFilename = fileStorageService.store(attachment);
        }

        Bug bug = buildBugFromRequest(bugRequest, reporter, testSession, attachmentFilename);

        // O @PrePersist em Bug.java já cuidará disso, então esta linha é redundante,
        // mas não causa problemas se deixada. Removê-la pode simplificar.
        // bug.setReportDate(LocalDateTime.now());

        Bug savedBug = bugRepository.save(bug);
        log.info("Bug with title '{}' created successfully with id {}", savedBug.getTitle(), savedBug.getId());
        return bugMapper.toBugDTO(savedBug);
    }

    /**
     * Builds a Bug object from a request.
     *
     * @param request            The request object.
     * @param reporter           The user reporting the bug.
     * @param session            The test session.
     * @param attachmentFilename The name of the attached file.
     * @return The constructed Bug object.
     */
    private Bug buildBugFromRequest(BugRequest request, User reporter, TestSession session, String attachmentFilename) {
        Bug bug = new Bug();
        bug.setTitle(request.getTitle());
        bug.setDescription(request.getDescription());
        bug.setStepsToReproduce(request.getStepsToReproduce());
        bug.setStatus(request.getStatus());
        bug.setSeverity(request.getSeverity());
        bug.setPriority(request.getPriority());
        bug.setTestSession(session);
        bug.setReporter(reporter);
        bug.setAttachmentFilename(attachmentFilename);
        // bug.setReportDate(LocalDateTime.now()); // Este é definido por @PrePersist na entidade Bug
        return bug;
    }


    /**
     * Updates an existing bug.
     *
     * @param id         The ID of the bug to update.
     * @param bugRequest The request object with the updated bug details.
     * @return The updated BugDTO.
     */
    @Transactional
    public BugDTO update(Long id, BugRequest bugRequest) {
        log.info("Updating bug with id: {}", id);
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + id));

        bug.setTitle(bugRequest.getTitle());
        bug.setDescription(bugRequest.getDescription());
        bug.setStepsToReproduce(bugRequest.getStepsToReproduce());
        bug.setStatus(bugRequest.getStatus());
        bug.setSeverity(bugRequest.getSeverity());
        bug.setPriority(bugRequest.getPriority());

        Bug updatedBug = bugRepository.save(bug);
        log.info("Bug with id {} updated successfully", id);
        return bugMapper.toBugDTO(updatedBug);
    }

    /**
     * Deletes a bug by its ID.
     *
     * @param id The ID of the bug to delete.
     * @return The ID of the test session the bug belonged to.
     */
    @Transactional
    public Long delete(Long id) {
        log.info("Deleting bug with id: {}", id);
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + id));

        // APERFEIÇOAMENTO: Apenas tentar deletar o arquivo se houver um nome de arquivo
        if (bug.getAttachmentFilename() != null && !bug.getAttachmentFilename().isEmpty()) {
            fileStorageService.delete(bug.getAttachmentFilename());
        }

        Long testSessionId = bug.getTestSession().getId();
        bugRepository.deleteById(id);
        log.info("Bug with id {} deleted successfully", id);
        return testSessionId;
    }

    /**
     * Finds bugs reported in the last 7 days.
     *
     * @return A list of recent Bug objects.
     */
    public List<Bug> findRecentBugs() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        log.info("Finding bugs reported after {}", sevenDaysAgo);
        return bugRepository.findByReportDateAfter(sevenDaysAgo);
    }

    /**
     * Finds bugs by their status.
     *
     * @param status The status to search for.
     * @return A list of Bug objects with the given status.
     */
    public List<Bug> findByStatus(BugStatus status) {
        log.info("Finding bugs with status: {}", status);
        return bugRepository.findByStatus(status);
    }

    /**
     * Finds bugs by their priority.
     *
     * @param priority The priority to search for.
     * @return A list of Bug objects with the given priority.
     */
    public List<Bug> findByPriority(BugPriority priority) {
        log.info("Finding bugs with priority: {}", priority);
        return bugRepository.findByPriority(priority);
    }

    /**
     * Finds bugs by their severity.
     *
     * @param severity The severity to search for.
     * @return A list of Bug objects with the given severity.
     */
    public List<Bug> findBySeverity(BugSeverity severity) {
        log.info("Finding bugs with severity: {}", severity);
        return bugRepository.findBySeverity(severity);
    }
}