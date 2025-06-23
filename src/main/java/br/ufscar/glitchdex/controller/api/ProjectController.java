package br.ufscar.glitchdex.controller.api;

import br.ufscar.glitchdex.dto.ProjectDTO;
import br.ufscar.glitchdex.dto.ProjectRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing projects.
 * Provides endpoints for listing, viewing, creating, updating, and deleting projects,
 * as well as managing project members and searching for projects.
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;

    /**
     * Lists all projects for the authenticated user, with optional sorting.
     *
     * @param user  The authenticated user.
     * @param sort  The field to sort by (default: "name").
     * @param order The sort order (default: "asc").
     * @return A list of ProjectDTOs.
     */
    @GetMapping
    public List<ProjectDTO> listProjects(UserDTO user, @RequestParam(defaultValue = "name") String sort, @RequestParam(defaultValue = "asc") String order) {
        log.info("User {} is listing projects with sort: {} and order: {}", user.getEmail(), sort, order);
        return projectService.findByMember(user, sort, order);
    }

    /**
     * Retrieves a single project by its ID.
     *
     * @param id The ID of the project to retrieve.
     * @return The ProjectDTO for the requested project.
     */
    @GetMapping("/{id}")
    public ProjectDTO viewProject(@PathVariable Long id) {
        log.info("Request to view project with id: {}", id);
        return projectService.findById(id);
    }

    /**
     * Creates a new project.
     * This endpoint is restricted to users with the 'ADMIN' authority.
     *
     * @param projectRequest The request body containing the project details.
     * @return The created ProjectDTO.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ProjectDTO createProject(@RequestBody ProjectRequest projectRequest) {
        log.info("Request to create project with name: {}", projectRequest.getName());
        return projectService.create(projectRequest);
    }

    /**
     * Updates an existing project.
     * This endpoint is restricted to users with the 'ADMIN' authority.
     *
     * @param id             The ID of the project to update.
     * @param projectRequest The request body containing the updated project details.
     * @return The updated ProjectDTO.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ProjectDTO updateProject(@PathVariable Long id, @RequestBody ProjectRequest projectRequest) {
        log.info("Request to update project with id: {}", id);
        return projectService.update(id, projectRequest);
    }

    /**
     * Deletes a project by its ID.
     *
     * @param id The ID of the project to delete.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        log.info("Request to delete project with id: {}", id);
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds a member to a project.
     *
     * @param projectId The ID of the project.
     * @param userId    The ID of the user to add as a member.
     * @return The updated ProjectDTO.
     */
    @PostMapping("/{projectId}/members/{userId}")
    public ProjectDTO addMember(@PathVariable Long projectId, @PathVariable Long userId) {
        log.info("Request to add member with id: {} to project with id: {}", userId, projectId);
        return projectService.addMember(projectId, userId);
    }

    /**
     * Removes a member from a project.
     *
     * @param projectId The ID of the project.
     * @param userId    The ID of the user to remove from the project.
     * @return The updated ProjectDTO.
     */
    @DeleteMapping("/{projectId}/members/{userId}")
    public ProjectDTO removeMember(@PathVariable Long projectId, @PathVariable Long userId) {
        log.info("Request to remove member with id: {} from project with id: {}", userId, projectId);
        return projectService.removeMember(projectId, userId);
    }

    /**
     * Searches for projects by name.
     *
     * @param query The search query.
     * @return A list of ProjectDTOs matching the search query.
     */
    @GetMapping("/search")
    public List<ProjectDTO> searchProjects(@RequestParam String query) {
        log.info("Request to search projects with query: {}", query);
        return projectService.searchByName(query);
    }
}