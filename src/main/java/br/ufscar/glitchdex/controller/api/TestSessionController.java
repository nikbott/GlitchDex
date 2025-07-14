package br.ufscar.glitchdex.controller.api;

import br.ufscar.glitchdex.dto.TestSessionDTO;
import br.ufscar.glitchdex.dto.TestSessionRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import br.ufscar.glitchdex.service.TestSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing test sessions.
 * Provides endpoints for listing, retrieving, creating, updating, and deleting test sessions,
 * as well as starting and finalizing them.
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class TestSessionController {

    private static final Logger log = LoggerFactory.getLogger(TestSessionController.class);
    private final TestSessionService testSessionService;

    /**
     * Lists all test sessions for a given project.
     *
     * @param projectId The ID of the project.
     * @return A list of TestSessionDTOs.
     */
    @GetMapping
    public List<TestSessionDTO> listSessions(@RequestParam Long projectId) {
        log.info("Request to list all sessions for projectId: {}", projectId);
        return testSessionService.findByProjectId(projectId);
    }

    /**
     * Retrieves a single test session by its ID.
     *
     * @param id The ID of the test session.
     * @return The TestSessionDTO for the requested session.
     */
    @GetMapping("/{id}")
    public TestSessionDTO getSession(@PathVariable Long id) {
        log.info("Request to get session with id: {}", id);
        return testSessionService.findById(id);
    }

    /**
     * Creates a new test session.
     * This endpoint is restricted to users with 'ADMIN' or 'TESTER' authority.
     *
     * @param sessionRequest The request body containing the test session details.
     * @param user           The authenticated user creating the session.
     * @return A ResponseEntity with the created TestSessionDTO and a location header.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TESTER')")
    public ResponseEntity<TestSessionDTO> createSession(@Valid @RequestBody TestSessionRequest sessionRequest, @AuthenticationPrincipal UserDTO user) {
        log.info("Request from user {} to create a new session for project: {}", user.getEmail(), sessionRequest.getProjectId());
        TestSessionDTO createdSession = testSessionService.create(sessionRequest, user);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdSession.getId())
                .toUri();
        log.info("Session created successfully with id: {}", createdSession.getId());
        return ResponseEntity.created(location).body(createdSession);
    }

    /**
     * Updates an existing test session.
     * This endpoint is restricted to users with 'ADMIN' or 'TESTER' authority.
     *
     * @param id             The ID of the test session to update.
     * @param sessionRequest The request body containing the updated session details.
     * @param user           The authenticated user updating the session.
     * @return The updated TestSessionDTO.
     * @throws IllegalStatusChangeException if the session status change is invalid.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TESTER')")
    public TestSessionDTO updateSession(@PathVariable Long id, @Valid @RequestBody TestSessionRequest sessionRequest, @AuthenticationPrincipal UserDTO user) throws IllegalStatusChangeException {
        log.info("Request from user {} to update session with id: {}", user.getEmail(), id);
        return testSessionService.update(id, sessionRequest, user);
    }


    /**
     * Deletes a test session by its ID.
     * This endpoint is restricted to users with 'ADMIN' or 'TESTER' authority.
     *
     * @param id The ID of the test session to delete.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TESTER')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        log.info("Request to delete session with id: {}", id);
        testSessionService.delete(id);
        log.info("Session with id: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Starts a test session.
     * This endpoint is restricted to users with 'ADMIN' or 'TESTER' authority.
     *
     * @param id The ID of the test session to start.
     * @return A ResponseEntity with the updated TestSessionDTO.
     * @throws IllegalStatusChangeException if the session cannot be started.
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TESTER')")
    public ResponseEntity<TestSessionDTO> startSession(@PathVariable Long id, @AuthenticationPrincipal UserDTO user) throws IllegalStatusChangeException {
        log.info("Request to start session with id: {}", id);
        TestSessionDTO session = testSessionService.startSession(id, user);
        return ResponseEntity.ok(session);
    }

    /**
     * Finalizes a test session.
     * This endpoint is restricted to users with 'ADMIN' or 'TESTER' authority.
     *
     * @param id The ID of the test session to finalize.
     * @return A ResponseEntity with the updated TestSessionDTO.
     * @throws IllegalStatusChangeException if the session cannot be finalized.
     */
    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TESTER')")
    public ResponseEntity<TestSessionDTO> finalizeSession(@PathVariable Long id, @AuthenticationPrincipal UserDTO user) throws IllegalStatusChangeException {
        log.info("Request to finalize session with id: {}", id);
        TestSessionDTO session = testSessionService.finishSession(id, user);
        return ResponseEntity.ok(session);
    }
}