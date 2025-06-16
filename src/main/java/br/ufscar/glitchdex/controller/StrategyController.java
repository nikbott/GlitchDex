package br.ufscar.glitchdex.controller;

import br.ufscar.glitchdex.domain.Project;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.StrategyDTO;
import br.ufscar.glitchdex.dto.StrategyRequest;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.repository.ProjectRepository;
import br.ufscar.glitchdex.service.StrategyService;
import br.ufscar.glitchdex.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
public class StrategyController {

    private final StrategyService strategyService;
    private final UserService userService;
    private final ProjectRepository projectRepository; // Can be removed if a ProjectService method is preferred

    @GetMapping
    public List<StrategyDTO> listStrategies(@RequestParam Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        return strategyService.findByProject(project);
    }

    @GetMapping("/{id}")
    public StrategyDTO getStrategy(@PathVariable Long id) {
        return strategyService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StrategyDTO> createStrategy(@Valid @RequestBody StrategyRequest strategyRequest, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        StrategyDTO createdStrategy = strategyService.create(strategyRequest, user);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdStrategy.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdStrategy);
    }

    @PutMapping("/{id}")
    public StrategyDTO updateStrategy(@PathVariable Long id, @Valid @RequestBody StrategyRequest strategyRequest) {
        return strategyService.update(id, strategyRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStrategy(@PathVariable Long id) {
        strategyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}