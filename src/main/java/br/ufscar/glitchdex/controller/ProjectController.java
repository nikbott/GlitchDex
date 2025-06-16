package br.ufscar.glitchdex.controller;

import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.ProjectDTO;
import br.ufscar.glitchdex.dto.ProjectRequest;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.service.ProjectService;
import br.ufscar.glitchdex.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    @GetMapping
    public List<ProjectDTO> listProjects(Authentication authentication, @RequestParam(defaultValue = "name") String sort, @RequestParam(defaultValue = "asc") String order) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return projectService.findByMember(user, sort, order);
    }

    @GetMapping("/{id}")
    public ProjectDTO viewProject(@PathVariable Long id) {
        return projectService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ProjectDTO createProject(@Valid @RequestBody ProjectRequest projectRequest,
                                    Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return projectService.create(projectRequest, user);
    }

    @PutMapping("/{id}")
    public ProjectDTO updateProject(@PathVariable Long id,
                                    @Valid @RequestBody ProjectRequest projectRequest) {
        return projectService.update(id, projectRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/members/{userId}")
    public ProjectDTO addMember(@PathVariable Long projectId, @PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return projectService.addMember(projectId, user);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ProjectDTO removeMember(@PathVariable Long projectId, @PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return projectService.removeMember(projectId, user);
    }

    @GetMapping("/search")
    public List<ProjectDTO> searchProjects(@RequestParam String query) {
        return projectService.searchByName(query);
    }
}