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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BugService {

    private static final Logger log = LoggerFactory.getLogger(BugService.class);
    private final BugRepository bugRepository;
    private final TestSessionRepository testSessionRepository;
    private final UserRepository userRepository;
    private final BugMapper bugMapper;
    private final FileStorageService fileStorageService;
    private final MessageSource messageSource;

    public BugDTO findById(Long id) {
        log.info("Finding bug with id: {}", id);
        Bug bug = bugRepository.findByIdWithReporter(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.bug.not_found", new Object[]{id}, LocaleContextHolder.getLocale())));
        return bugMapper.toBugDTO(bug);
    }

    public List<BugDTO> findByTestSession(TestSession testSession) {
        log.info("Finding bugs for test session with id: {}", testSession.getId());
        return bugRepository.findByTestSession(testSession).stream()
                .map(bugMapper::toBugDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BugDTO create(BugRequest bugRequest, UserDTO reporterDto, MultipartFile[] attachments) throws IllegalStatusChangeException {
        log.info("User {} is creating a bug for test session {}", reporterDto.getEmail(), bugRequest.getTestSessionId());
        TestSession testSession = testSessionRepository.findById(bugRequest.getTestSessionId())
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.session.not_found", new Object[]{bugRequest.getTestSessionId()}, LocaleContextHolder.getLocale())));

        TestSessionStateMachine stateMachine = new TestSessionStateMachine(testSession, messageSource);
        stateMachine.canReportBug();

        User reporter = userRepository.findById(reporterDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.user.not_found", new Object[]{reporterDto.getId()}, LocaleContextHolder.getLocale())));

        Bug bug = this.buildBugFromRequest(bugRequest, reporter, testSession);

        if (attachments != null) {
            for (MultipartFile attachment : attachments) {
                if (!attachment.isEmpty()) {
                    String filename = fileStorageService.store(attachment);
                    BugAttachment bugAttachment = new BugAttachment();
                    bugAttachment.setFilename(filename);
                    bugAttachment.setBug(bug);
                    bug.getAttachments().add(bugAttachment);
                }
            }
        }

        Bug savedBug = bugRepository.save(bug);
        log.info("Bug with title '{}' created successfully with id {}", savedBug.getTitle(), savedBug.getId());
        return bugMapper.toBugDTO(savedBug);
    }

    private Bug buildBugFromRequest(BugRequest request, User reporter, TestSession session) {
        Bug bug = new Bug();
        bug.setTitle(request.getTitle());
        bug.setDescription(request.getDescription());
        bug.setStepsToReproduce(request.getStepsToReproduce());
        bug.setStatus(request.getStatus());
        bug.setSeverity(request.getSeverity());
        bug.setPriority(request.getPriority());
        bug.setTestSession(session);
        bug.setReporter(reporter);
        return bug;
    }

    @Transactional
    public BugDTO update(Long id, BugRequest bugRequest, MultipartFile[] newAttachments) {
        log.info("Updating bug with id: {}", id);
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.bug.not_found", new Object[]{id}, LocaleContextHolder.getLocale())));

        bug.setTitle(bugRequest.getTitle());
        bug.setDescription(bugRequest.getDescription());
        bug.setStepsToReproduce(bugRequest.getStepsToReproduce());
        bug.setStatus(bugRequest.getStatus());
        bug.setSeverity(bugRequest.getSeverity());
        bug.setPriority(bugRequest.getPriority());

        if (newAttachments != null) {
            for (MultipartFile attachment : newAttachments) {
                if (!attachment.isEmpty()) {
                    if (bug.getAttachments().size() >= 5) {
                        throw new IllegalStateException(messageSource.getMessage("bug.form.error.max_files", null, LocaleContextHolder.getLocale()));
                    }
                    String filename = fileStorageService.store(attachment);
                    BugAttachment bugAttachment = new BugAttachment();
                    bugAttachment.setFilename(filename);
                    bugAttachment.setBug(bug);
                    bug.getAttachments().add(bugAttachment);
                }
            }
        }

        Bug updatedBug = bugRepository.save(bug);
        log.info("Bug with id {} updated successfully", id);
        return bugMapper.toBugDTO(updatedBug);
    }

    @Transactional
    public Long delete(Long id) {
        log.info("Deleting bug with id: {}", id);
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.bug.not_found", new Object[]{id}, LocaleContextHolder.getLocale())));

        for (BugAttachment attachment : bug.getAttachments()) {
            fileStorageService.delete(attachment.getFilename());
        }

        Long testSessionId = bug.getTestSession().getId();
        bugRepository.deleteById(id);
        log.info("Bug with id {} deleted successfully", id);
        return testSessionId;
    }

    public BugRequest toBugRequest(BugDTO bugDto) {
        BugRequest request = new BugRequest();
        request.setTitle(bugDto.getTitle());
        request.setDescription(bugDto.getDescription());
        request.setStepsToReproduce(bugDto.getStepsToReproduce());
        request.setStatus(bugDto.getStatus());
        request.setSeverity(bugDto.getSeverity());
        request.setPriority(bugDto.getPriority());
        request.setTestSessionId(bugDto.getTestSessionId());
        return request;
    }

    public List<Bug> findRecentBugs() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        log.info("Finding bugs reported after {}", sevenDaysAgo);
        return bugRepository.findByReportDateAfter(sevenDaysAgo);
    }

    public List<Bug> findByStatus(BugStatus status) {
        log.info("Finding bugs with status: {}", status);
        return bugRepository.findByStatus(status);
    }

    public List<Bug> findByPriority(BugPriority priority) {
        log.info("Finding bugs with priority: {}", priority);
        return bugRepository.findByPriority(priority);
    }

    public List<Bug> findBySeverity(BugSeverity severity) {
        log.info("Finding bugs with severity: {}", severity);
        return bugRepository.findBySeverity(severity);
    }
}