package br.ufscar.glitchdex.controller.web;

import br.ufscar.glitchdex.config.Constants;
import br.ufscar.glitchdex.dto.TestSessionDTO;
import br.ufscar.glitchdex.dto.TestSessionRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import br.ufscar.glitchdex.mapper.TestSessionMapper;
import br.ufscar.glitchdex.service.ProjectService;
import br.ufscar.glitchdex.service.StrategyService;
import br.ufscar.glitchdex.service.TestSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/sessions")
@RequiredArgsConstructor
public class TestSessionViewController {

    private static final Logger log = LoggerFactory.getLogger(TestSessionViewController.class);
    private final StrategyService strategyService;
    private final ProjectService projectService;
    private final TestSessionService testSessionService;
    private final TestSessionMapper testSessionMapper;
    private final MessageSource messageSource;

    @GetMapping("/new")
    public String showNewForm(@PathVariable Long projectId, Model model) {
        log.info("Request to show new session form for project with id: {}", projectId);
        var projectDto = projectService.findById(projectId);
        var strategies = strategyService.findAll();

        TestSessionRequest testSessionRequest = new TestSessionRequest();
        testSessionRequest.setProjectId(projectId);
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
                                    @AuthenticationPrincipal UserDTO currentUser,
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

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long projectId, @PathVariable Long id, Model model) {
        log.info("Request to show edit form for session with id: {}", id);
        TestSessionDTO sessionDto = testSessionService.findById(id);
        TestSessionRequest request = testSessionMapper.toTestSessionRequest(sessionDto);

        model.addAttribute("testSessionRequest", request);
        model.addAttribute("strategies", strategyService.findAll());
        model.addAttribute("project", projectService.findById(projectId));
        model.addAttribute("isEditMode", true);
        model.addAttribute("sessionId", id);
        return "session/form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize(Constants.HAS_ANY_AUTHORITY_ADMIN_TESTER)
    public String updateTestSession(@PathVariable Long projectId,
                                    @PathVariable Long id,
                                    @Valid @ModelAttribute("testSessionRequest") TestSessionRequest request,
                                    BindingResult result,
                                    @AuthenticationPrincipal UserDTO currentUser,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        log.info("Request from user {} to update session with id: {}", currentUser.getEmail(), id);
        if (result.hasErrors()) {
            log.warn("Validation errors while updating session: {}", result.getAllErrors());
            model.addAttribute("strategies", strategyService.findAll());
            model.addAttribute("project", projectService.findById(projectId));
            model.addAttribute("isEditMode", true);
            model.addAttribute("sessionId", id);
            return "session/form";
        }

        try {
            testSessionService.update(id, request, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("session.form.success.update", null, LocaleContextHolder.getLocale()));
        } catch (IllegalStatusChangeException e) {
            log.warn("Illegal status change while updating session: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/projects/" + projectId + "/sessions/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize(Constants.HAS_ANY_AUTHORITY_ADMIN_TESTER)
    public String deleteSession(@PathVariable Long projectId,
                                @PathVariable Long id,
                                @AuthenticationPrincipal UserDTO user,
                                RedirectAttributes redirectAttributes) {
        testSessionService.verifyOwnership(id, user.getId());
        testSessionService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("session.form.success.delete", null, LocaleContextHolder.getLocale()));
        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/{id}/start")
    @PreAuthorize(Constants.HAS_ANY_AUTHORITY_ADMIN_TESTER)
    public String startSession(@PathVariable Long projectId,
                               @PathVariable Long id,
                               @AuthenticationPrincipal UserDTO user,
                               RedirectAttributes attr) {
        try {
            testSessionService.startSession(id, user);
            attr.addFlashAttribute("success", messageSource.getMessage("session.form.success.start", null, LocaleContextHolder.getLocale()));
        } catch (IllegalStatusChangeException e) {
            attr.addFlashAttribute("error", messageSource.getMessage("session.form.error.start", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
        }

        return "redirect:/projects/" + projectId + "/sessions/" + id;
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize(Constants.HAS_ANY_AUTHORITY_ADMIN_TESTER)
    public String finishSession(@PathVariable Long projectId,
                                @PathVariable Long id,
                                @AuthenticationPrincipal UserDTO user,
                                RedirectAttributes attr) {
        try {
            testSessionService.finishSession(id, user);
            attr.addFlashAttribute("success", messageSource.getMessage("session.form.success.finish", null, LocaleContextHolder.getLocale()));
        } catch (IllegalStatusChangeException e) {
            attr.addFlashAttribute("error", messageSource.getMessage("session.form.error.finish", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
        }
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
        attr.addFlashAttribute("success", messageSource.getMessage("session.form.success.update_description", null, LocaleContextHolder.getLocale()));
        return "redirect:/projects/" + projectId + "/sessions/" + id;
    }

    @GetMapping
    public String listSessions(@PathVariable Long projectId,
                               @AuthenticationPrincipal UserDTO currentUser,
                               Model model) {
        log.info("Listando sessões do projeto {}", projectId);
        projectService.verifyUserAssociation(currentUser.getId(), projectId);

        List<TestSessionDTO> sessions = testSessionService.findByProjectId(projectId);
        model.addAttribute("sessions", sessions);
        model.addAttribute("projectId", projectId);
        return "session/list";
    }

    @GetMapping("/{id}")
    public String viewSession(@PathVariable Long projectId, @PathVariable Long id, Model model) {
        log.info("Request to view session with id: {} for project with id: {}", id, projectId);

        testSessionService.updateExpiredSessionsByProject(projectId);

        model.addAttribute("testSession", testSessionService.findById(id));
        model.addAttribute("project", projectService.findById(projectId));
        return "session/view";
    }
}