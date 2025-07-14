package br.ufscar.glitchdex.controller.api;

import br.ufscar.glitchdex.config.Constants;
import br.ufscar.glitchdex.dto.BugDTO;
import br.ufscar.glitchdex.dto.BugRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.service.BugService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing bugs.
 */
@RestController
@RequestMapping("/api/bugs")
@RequiredArgsConstructor
public class BugRestController {

    private final BugService bugService;

    /**
     * Lists bugs for a specific test session.
     *
     * @param testSessionId The ID of the test session to filter bugs by.
     * @return A list of BugDTOs.
     */
    @GetMapping
    public List<BugDTO> listBugs(@RequestParam Long testSessionId) {
        // Corrected to list bugs by test session ID instead of assuming a findAll() method.
        return bugService.findByTestSessionId(testSessionId);
    }

    /**
     * Retrieves a single bug by its ID.
     *
     * @param id The ID of the bug.
     * @return The BugDTO for the requested bug.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BugDTO> getBug(@PathVariable Long id) {
        BugDTO bug = bugService.findById(id);
        return ResponseEntity.ok(bug);
    }

    /**
     * Creates a new bug.
     * Restricted to ADMIN or TESTER.
     *
     * @param bugRequest The request body containing the bug details.
     * @param user       The authenticated user.
     * @return The created BugDTO.
     */
    @PostMapping
    @PreAuthorize(Constants.HAS_ANY_AUTHORITY_ADMIN_TESTER)
    public ResponseEntity<BugDTO> createBug(
            @Valid @RequestBody BugRequest bugRequest,
            @AuthenticationPrincipal UserDTO user) {

        // Passing null for attachments list as this is a JSON endpoint.
        BugDTO createdBug = bugService.create(bugRequest, user, null);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdBug.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdBug);
    }

    /**
     * Updates an existing bug.
     * Restricted to ADMIN or TESTER.
     *
     * @param id          The ID of the bug to update.
     * @param bugRequest  The request body containing the updated bug details.
     * @return The updated BugDTO.
     */
    @PutMapping("/{id}")
    @PreAuthorize(Constants.HAS_ANY_AUTHORITY_ADMIN_TESTER)
    public ResponseEntity<BugDTO> updateBug(@PathVariable Long id,
                                            @Valid @RequestBody BugRequest bugRequest) {

        // Passing null for attachments list as this is a JSON endpoint.
        BugDTO updatedBug = bugService.update(id, bugRequest, null);
        return ResponseEntity.ok(updatedBug);
    }

    /**
     * Deletes a bug by its ID.
     * Restricted to ADMIN or TESTER.
     *
     * @param id The ID of the bug to delete.
     * @return ResponseEntity with NO_CONTENT status.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize(Constants.HAS_ANY_AUTHORITY_ADMIN_TESTER)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteBug(@PathVariable Long id) {
        bugService.delete(id);
        return ResponseEntity.noContent().build();
    }
}