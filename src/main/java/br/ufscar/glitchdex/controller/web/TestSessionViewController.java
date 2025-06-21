package br.ufscar.glitchdex.controller.web;

import br.ufscar.glitchdex.config.Constants;
import br.ufscar.glitchdex.domain.TestSessionStateMachine;
import br.ufscar.glitchdex.dto.TestSessionDTO;
import br.ufscar.glitchdex.dto.TestSessionRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.domain.SessionStatus;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import br.ufscar.glitchdex.service.ProjectService;
import br.ufscar.glitchdex.service.StrategyService;
import br.ufscar.glitchdex.service.TestSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/sessions")
@RequiredArgsConstructor
public class TestSessionViewController {

    private static final Logger log = LoggerFactory.getLogger(TestSessionViewController.class);
    private final StrategyService strategyService;
    private final ProjectService projectService;
    private final TestSessionService testSessionService;

    @GetMapping("/new")
    public String showNewForm(@PathVariable Long projectId, Model model) {
        log.info("Request to show new session form for project with id: {}", projectId);
        var projectDto = projectService.findById(projectId);
        var strategies = strategyService.findAll();

        TestSessionRequest testSessionRequest = new TestSessionRequest();
        testSessionRequest.setProjectId(projectId); // Set projectId on the request object
        model.addAttribute("testSessionRequest", testSessionRequest);

        model.addAttribute("strategies", strategies);
        model.addAttribute("project", projectDto);
        return "session/form";
    }

    @PostMapping
    @PreAuthorize(Constants.HAS_ANY_AUTHORITY_ADMIN_TESTER)
    public String createTestSession(@PathVariable Long projectId,
                                    @Valid @ModelAttribute("testSessionRequest") TestSessionRequest request,
                                    BindingResult result,
                                    UserDTO currentUser,
                                    Model model) {
        log.info("Request from user {} to create a new session for project with id: {}", currentUser.getEmail(), projectId);
        if (result.hasErrors()) {
            log.warn("Validation errors while creating session: {}", result.getAllErrors());
            var projectDto = projectService.findById(projectId);
            var strategies = strategyService.findAll();
            model.addAttribute("strategies", strategies);
            model.addAttribute("project", projectDto);
            return "session/form";
        }

        testSessionService.create(request, currentUser);
        log.info("Session for project with id: {} created successfully", projectId);
        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/{id}/start")
    @PreAuthorize(Constants.HAS_ANY_AUTHORITY_ADMIN_TESTER)
    public String startSession(@PathVariable Long projectId,
                               @PathVariable Long id,
                               @AuthenticationPrincipal UserDTO user,
                               RedirectAttributes attr) {
        var session = testSessionService.findByIdEntity(id);
        testSessionService.verifyOwnership(session.getId(), user.getId());

        try {
            TestSessionStateMachine machine = new TestSessionStateMachine(session);
            machine.startSession();
            session.setStartTimestamp(LocalDateTime.now());
            testSessionService.save(session);

            attr.addFlashAttribute("success", "Sessão iniciada com sucesso.");
        } catch (IllegalStatusChangeException e) {
            attr.addFlashAttribute("error", "Não foi possível iniciar a sessão: " + e.getMessage());
        }

        return "redirect:/projects/" + projectId + "/sessions/" + id;
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize(Constants.HAS_ANY_AUTHORITY_ADMIN_TESTER)
    public String finishSession(@PathVariable Long projectId,
                                @PathVariable Long id,
                                @AuthenticationPrincipal UserDTO user,
                                RedirectAttributes attr) throws IllegalStatusChangeException {
        testSessionService.verifyOwnership(id, user.getId());
        testSessionService.updateFinish(id, LocalDateTime.now(), SessionStatus.FINALIZED);
        attr.addFlashAttribute("success", "Sessão finalizada com sucesso.");
        return "redirect:/projects/" + projectId + "/sessions/" + id;
    }

    @PostMapping("/{id}/update-description")
    @PreAuthorize(Constants.HAS_ANY_AUTHORITY_ADMIN_TESTER)
    public String updateDescription(@PathVariable Long projectId,
                                    @PathVariable Long id,
                                    @RequestParam String description,
                                    @AuthenticationPrincipal UserDTO user,
                                    RedirectAttributes attr) {
        testSessionService.verifyOwnership(id, user.getId());
        testSessionService.appendDescriptionIfInExecution(id, description);
        attr.addFlashAttribute("success", "Descrição atualizada com sucesso.");
        return "redirect:/projects/" + projectId + "/sessions/" + id;
    }

    @GetMapping
    public String listSessions(@PathVariable Long projectId,
                               @AuthenticationPrincipal UserDTO currentUser,
                               Model model) {
        log.info("Listando sessões do projeto {}", projectId);
        projectService.verifyUserAssociation(currentUser.getId(), projectId);

        List<TestSessionDTO> sessions = testSessionService.findByStrategyProject(projectId);
        model.addAttribute("sessions", sessions);
        model.addAttribute("projectId", projectId);
        return "session/list";
    }

    @GetMapping("/{id}")
    public String viewSession(@PathVariable Long projectId, @PathVariable Long id, Model model) {
        log.info("Request to view session with id: {} for project with id: {}", id, projectId);

        testSessionService.updateExpiredSessionsByProject(projectId);

        model.addAttribute("testSession", testSessionService.findById(id)); // ← Mudança aqui
        model.addAttribute("project", projectService.findById(projectId));
        return "session/view";
    }
}
