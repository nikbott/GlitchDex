package br.ufscar.glitchdex.controller.api;

import br.ufscar.glitchdex.dto.StrategyDTO;
import br.ufscar.glitchdex.dto.StrategyRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.service.StrategyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing testing strategies.
 * Provides endpoints for listing, viewing, creating, updating, and deleting strategies.
 */
@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
public class StrategyController {

    private static final Logger log = LoggerFactory.getLogger(StrategyController.class);
    private final StrategyService strategyService;

    /**
     * Lists all available testing strategies.
     *
     * @return A list of StrategyDTOs.
     */
    @GetMapping
    public List<StrategyDTO> listStrategies() {
        log.info("Request to list all strategies");
        return strategyService.findAll();
    }

    /**
     * Retrieves a single strategy by its ID.
     *
     * @param id The ID of the strategy to retrieve.
     * @return The StrategyDTO for the requested strategy.
     */
    @GetMapping("/{id}")
    public StrategyDTO getStrategy(@PathVariable Long id) {
        log.info("Request to get strategy with id: {}", id);
        return strategyService.findById(id);
    }

    /**
     * Creates a new testing strategy.
     * This endpoint is restricted to users with the 'ADMIN' authority.
     *
     * @param strategyRequest The request body containing the strategy details.
     * @param user            The authenticated user creating the strategy.
     * @return A ResponseEntity with the created StrategyDTO and a location header.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StrategyDTO> createStrategy(@Valid @RequestBody StrategyRequest strategyRequest, UserDTO user) {
        log.info("Request to create a new strategy with name: {}", strategyRequest.getName());
        StrategyDTO createdStrategy = strategyService.create(strategyRequest, user);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdStrategy.getId())
                .toUri();
        log.info("Strategy created successfully with id: {}", createdStrategy.getId());
        return ResponseEntity.created(location).body(createdStrategy);
    }

    /**
     * Updates an existing testing strategy.
     * This endpoint is restricted to users with the 'ADMIN' authority.
     *
     * @param id              The ID of the strategy to update.
     * @param strategyRequest The request body containing the updated strategy details.
     * @return The updated StrategyDTO.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public StrategyDTO updateStrategy(@PathVariable Long id, @Valid @RequestBody StrategyRequest strategyRequest) {
        log.info("Request to update strategy with id: {}", id);
        return strategyService.update(id, strategyRequest, null);
    }

    /**
     * Deletes a testing strategy by its ID.
     * This endpoint is restricted to users with the 'ADMIN' authority.
     *
     * @param id The ID of the strategy to delete.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteStrategy(@PathVariable Long id) {
        log.info("Request to delete strategy with id: {}", id);
        strategyService.delete(id);
        log.info("Strategy with id: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}