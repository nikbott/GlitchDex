package br.ufscar.glitchdex.controller;

import br.ufscar.glitchdex.domain.Strategy;
import br.ufscar.glitchdex.domain.TestSession;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.TestSessionDTO;
import br.ufscar.glitchdex.dto.TestSessionRequest;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.repository.StrategyRepository;
import br.ufscar.glitchdex.service.TestSessionService;
import br.ufscar.glitchdex.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class TestSessionController {

    private final TestSessionService testSessionService;
    private final UserService userService;
    private final StrategyRepository strategyRepository; // Can be removed if a StrategyService method is preferred

    @GetMapping
    public List<TestSessionDTO> listSessions(@RequestParam Long strategyId) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found with id: " + strategyId));
        return testSessionService.findByStrategy(strategy);
    }

    @GetMapping("/{id}")
    public TestSessionDTO getSession(@PathVariable Long id) {
        return testSessionService.findById(id);
    }

    @PostMapping
    public ResponseEntity<TestSessionDTO> createSession(@Valid @RequestBody TestSessionRequest sessionRequest, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TestSessionDTO createdSession = testSessionService.create(sessionRequest, user);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdSession.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdSession);
    }

    @PutMapping("/{id}")
    public TestSessionDTO updateSession(@PathVariable Long id, @Valid @RequestBody TestSessionRequest sessionRequest, Authentication authentication) throws IllegalStatusChangeException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return testSessionService.update(id, sessionRequest, user);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        testSessionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> startSession(@PathVariable Long id) {
        TestSession session = testSessionService.startSession(id); // You will need to implement startSession in your service
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/finalize")
    public ResponseEntity<Void> finalizeSession(@PathVariable Long id) {
        TestSession session = testSessionService.finalizeSession(id); // You will need to implement finalizeSession in your service
        return ResponseEntity.ok().build();
    }
}