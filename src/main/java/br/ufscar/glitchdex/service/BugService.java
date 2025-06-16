package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.*;
import br.ufscar.glitchdex.dto.BugDTO;
import br.ufscar.glitchdex.dto.BugRequest;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.mapper.BugMapper;
import br.ufscar.glitchdex.repository.BugRepository;
import br.ufscar.glitchdex.repository.TestSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BugService {

    private final BugRepository bugRepository;
    private final TestSessionRepository testSessionRepository;
    private final BugMapper bugMapper;
    private final FileStorageService fileStorageService;

    public BugDTO findById(Long id) {
        Bug bug = bugRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bug not found with id " + id));
        return bugMapper.toBugDTO(bug);
    }

    public List<BugDTO> findByTestSession(TestSession testSession) {
        return bugMapper.toBugDTOs(bugRepository.findByTestSession(testSession));
    }

    @Transactional
    public BugDTO create(BugRequest bugRequest, User reporter, MultipartFile attachment) throws IllegalStatusChangeException {
        TestSession testSession = testSessionRepository.findById(bugRequest.getTestSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Test Session not found with id: " + bugRequest.getTestSessionId()));

        if (SessionStatus.IN_EXECUTION != testSession.getStatus()) {
            throw new IllegalStatusChangeException("Bugs can only be reported for sessions that are in execution.");
        }

        String attachmentFilename = fileStorageService.store(attachment);

        Bug bug = new Bug();
        bug.setTitle(bugRequest.getTitle());
        bug.setDescription(bugRequest.getDescription());
        bug.setStepsToReproduce(bugRequest.getStepsToReproduce());
        bug.setStatus(bugRequest.getStatus());
        bug.setSeverity(bugRequest.getSeverity());
        bug.setPriority(bugRequest.getPriority());
        bug.setTestSession(testSession);
        bug.setReporter(reporter);
        bug.setAttachmentFilename(attachmentFilename);

        Bug savedBug = bugRepository.save(bug);
        return bugMapper.toBugDTO(savedBug);
    }

    @Transactional
    public BugDTO update(Long id, BugRequest bugRequest) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + id));

        bug.setTitle(bugRequest.getTitle());
        bug.setDescription(bugRequest.getDescription());
        bug.setStepsToReproduce(bugRequest.getStepsToReproduce());
        bug.setStatus(bugRequest.getStatus());
        bug.setSeverity(bugRequest.getSeverity());
        bug.setPriority(bugRequest.getPriority());

        Bug updatedBug = bugRepository.save(bug);
        return bugMapper.toBugDTO(updatedBug);
    }

    public void delete(Long id) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + id));

        fileStorageService.delete(bug.getAttachmentFilename());

        bugRepository.deleteById(id);
    }

    public List<Bug> findRecentBugs() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return bugRepository.findByReportDateAfter(sevenDaysAgo);
    }

    public List<Bug> findByStatus(BugStatus status) {
        return bugRepository.findByStatus(status);
    }

    public List<Bug> findByPriority(BugPriority priority) {
        return bugRepository.findByPriority(priority);
    }

    public List<Bug> findBySeverity(BugSeverity severity) {
        return bugRepository.findBySeverity(severity);
    }
}