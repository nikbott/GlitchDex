package br.ufscar.glitchdex.controller.web;

import br.ufscar.glitchdex.dto.BugDTO;
import br.ufscar.glitchdex.dto.BugRequest;
import br.ufscar.glitchdex.dto.TestSessionDTO;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import br.ufscar.glitchdex.service.BugService;
import br.ufscar.glitchdex.service.TestSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bugs")
@RequiredArgsConstructor
public class BugController {

    private static final Logger log = LoggerFactory.getLogger(BugController.class);
    private final BugService bugService;
    private final TestSessionService testSessionService;
    private final MessageSource messageSource;

    @GetMapping("/new")
    public String newBugForm(@RequestParam("testSessionId") Long testSessionId, Model model) {
        log.info("Request to show new bug form for test session with id: {}", testSessionId);
        BugRequest bugRequest = new BugRequest();
        bugRequest.setTestSessionId(testSessionId);
        TestSessionDTO session = testSessionService.findById(testSessionId); // Fetch session
        model.addAttribute("bugRequest", bugRequest);
        model.addAttribute("isEditMode", false);
        model.addAttribute("session", session); // Add session to model
        return "bug/form";
    }

    @PostMapping
    public String createBug(@Valid @ModelAttribute("bugRequest") BugRequest bugRequest,
                            BindingResult result,
                            @RequestParam(value = "attachment", required = false) MultipartFile[] attachments,
                            UserDTO user,
                            RedirectAttributes redirectAttributes,
                            Model model) throws IllegalStatusChangeException {

        if (attachments != null && attachments.length > 5) {
            result.rejectValue("attachment", "error.bugRequest", messageSource.getMessage("bug.form.error.max_files", null, LocaleContextHolder.getLocale()));
        }

        if (result.hasErrors()) {
            log.warn("Validation errors occurred during bug creation for test session: {}", bugRequest.getTestSessionId());
            TestSessionDTO session = testSessionService.findById(bugRequest.getTestSessionId());
            model.addAttribute("isEditMode", false);
            model.addAttribute("session", session);
            return "bug/form";
        }

        log.info("User {} is creating a new bug for test session: {}", user.getEmail(), bugRequest.getTestSessionId());
        BugDTO createdBug = bugService.create(bugRequest, user, attachments);
        log.info("Bug created successfully with id: {}", createdBug.getId());
        redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("bug.form.success.create", null, LocaleContextHolder.getLocale()));

        return "redirect:/bugs/" + createdBug.getId();
    }

    @GetMapping("/{id}")
    public String viewBug(@PathVariable Long id, Model model) {
        log.info("Request to view bug with id: {}", id);
        BugDTO bug = bugService.findById(id);
        TestSessionDTO session = testSessionService.findById(bug.getTestSessionId());
        model.addAttribute("bug", bug);
        model.addAttribute("session", session);
        return "bug/view";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.info("Request to show edit form for bug with id: {}", id);
        BugDTO bugDTO = bugService.findById(id);
        TestSessionDTO session = testSessionService.findById(bugDTO.getTestSessionId());
        BugRequest bugRequest = bugService.toBugRequest(bugDTO);
        model.addAttribute("bugRequest", bugRequest);
        model.addAttribute("bugId", id);
        model.addAttribute("isEditMode", true);
        model.addAttribute("session", session);
        return "bug/form";
    }

    @PostMapping("/{id}/edit")
    public String updateBug(@PathVariable Long id,
                            @Valid @ModelAttribute("bugRequest") BugRequest bugRequest,
                            BindingResult result,
                            @RequestParam(value = "attachment", required = false) MultipartFile[] attachments,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        log.info("Request to update bug with id: {}", id);
        if (attachments != null && attachments.length > 5) {
            result.rejectValue("attachment", "error.bugRequest", messageSource.getMessage("bug.form.error.max_files", null, LocaleContextHolder.getLocale()));
        }
        if (result.hasErrors()) {
            log.warn("Validation errors while updating bug: {}", result.getAllErrors());
            TestSessionDTO session = testSessionService.findById(bugRequest.getTestSessionId());
            model.addAttribute("bugId", id);
            model.addAttribute("isEditMode", true);
            model.addAttribute("session", session);
            return "bug/form";
        }

        bugService.update(id, bugRequest, attachments);
        redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("bug.form.success.update", null, LocaleContextHolder.getLocale()));
        log.info("Bug with id: {} updated successfully", id);
        return "redirect:/bugs/" + id;
    }


    @PostMapping("/{id}/delete")
    public String deleteBug(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Request to delete bug with id: {}", id);
        Long testSessionId = bugService.delete(id);
        TestSessionDTO session = testSessionService.findById(testSessionId);
        redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("bug.form.success.delete", null, LocaleContextHolder.getLocale()));
        log.info("Bug with id: {} deleted successfully", id);
        return "redirect:/projects/" + session.getProjectId() + "/sessions/" + testSessionId;
    }
}