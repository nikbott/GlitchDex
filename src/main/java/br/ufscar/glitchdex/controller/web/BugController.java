package br.ufscar.glitchdex.controller.web;

import br.ufscar.glitchdex.domain.BugPriority;
import br.ufscar.glitchdex.domain.BugSeverity;
import br.ufscar.glitchdex.domain.BugStatus;
import br.ufscar.glitchdex.dto.BugDTO;
import br.ufscar.glitchdex.dto.BugRequest;
import br.ufscar.glitchdex.dto.TestSessionDTO;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.service.BugService;
import br.ufscar.glitchdex.service.TestSessionService;
import br.ufscar.glitchdex.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType; // Importe MediaType
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/bugs")
@RequiredArgsConstructor
public class BugController {

    private static final Logger log = LoggerFactory.getLogger(BugController.class);
    private final BugService bugService;
    private final TestSessionService testSessionService;
    private final MessageSource messageSource;

    @GetMapping("/new")
    public String newBugForm(@RequestParam("testSessionId") Long testSessionId, Model model, RedirectAttributes redirectAttributes) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        try {
            TestSessionDTO session = testSessionService.findById(testSessionId);

            BugRequest bugRequest = new BugRequest();
            bugRequest.setTestSessionId(testSessionId);
            bugRequest.setProjectId(session.getProjectId());

            model.addAttribute("bugRequest", bugRequest);
            model.addAttribute("session", session);
            model.addAttribute("isEditMode", false);
            model.addAttribute("bugStatus", BugStatus.values());
            model.addAttribute("bugSeverity", BugSeverity.values());
            model.addAttribute("bugPriority", BugPriority.values());
            model.addAttribute("formAction", "/bugs");

            return "bug/form";
        } catch (ResourceNotFoundException e) {
            log.error("Error finding test session for new bug form: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/error";
        } catch (Exception e) {
            log.error("Unexpected error in new bug form: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("error.unexpected", null, currentLocale) + ": " + e.getMessage());
            return "redirect:/error";
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createBug(@Valid @ModelAttribute BugRequest bugRequest,
                            BindingResult bindingResult,
                            @RequestParam(value = "attachment", required = false) List<MultipartFile> attachments, // <-- ALTERADO AQUI
                            RedirectAttributes redirectAttributes,
                            Model model) {

        Locale currentLocale = LocaleContextHolder.getLocale();
        Long projectId = bugRequest.getProjectId();
        Long testSessionId = bugRequest.getTestSessionId();

        // Verifique se attachments não é nulo antes de verificar o tamanho
        if (attachments != null && attachments.size() > 5) {
            String errorMessage = messageSource.getMessage("bug.form.error.max_files", null, currentLocale);
            model.addAttribute("fileAttachmentError", errorMessage);
        }

        if (bindingResult.hasErrors() || model.containsAttribute("fileAttachmentError")) {
            try {
                TestSessionDTO session = testSessionService.findById(testSessionId);
                model.addAttribute("session", session);
                model.addAttribute("isEditMode", false);
            } catch (ResourceNotFoundException e) {
                log.error("Error finding test session for bug form validation: {}", e.getMessage(), e);
            }
            model.addAttribute("bugStatus", BugStatus.values());
            model.addAttribute("bugSeverity", BugSeverity.values());
            model.addAttribute("bugPriority", BugPriority.values());
            model.addAttribute("formAction", "/bugs");
            return "bug/form";
        }

        try {
            UserDTO currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("error.user.not_found_context", null, currentLocale));
                return "redirect:/error";
            }

            // Garante que attachments seja uma lista vazia se for nulo
            MultipartFile[] attachmentArray = (attachments != null) ? attachments.toArray(new MultipartFile[0]) : new MultipartFile[0];
            bugService.create(bugRequest, currentUser, attachmentArray);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("bug.form.success.create", null, currentLocale));

            return "redirect:/projects/" + projectId + "/sessions/" + testSessionId;
        } catch (ResourceNotFoundException | IllegalStatusChangeException e) {
            log.error("Error creating bug ({}): {}", e.getClass().getSimpleName(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/projects/" + projectId + "/sessions/" + testSessionId;
        } catch (IOException e) {
            log.error("IO Error creating bug: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("error.file_operation", null, currentLocale) + ": " + e.getMessage());
            return "redirect:/projects/" + projectId + "/sessions/" + testSessionId;
        } catch (Exception e) {
            log.error("Unexpected error creating bug: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("error.unexpected", null, currentLocale) + ": " + e.getMessage());
            return "redirect:/projects/" + projectId + "/sessions/" + testSessionId;
        }
    }

    @GetMapping("/{id}")
    public String viewBugDetails(@PathVariable("id") Long bugId, Model model, RedirectAttributes redirectAttributes) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        try {
            BugDTO bug = bugService.findById(bugId);
            TestSessionDTO session = testSessionService.findById(bug.getTestSessionId());

            model.addAttribute("bug", bug);
            model.addAttribute("session", session);

            return "bug/view";
        } catch (ResourceNotFoundException e) {
            log.error("Bug or associated session not found for ID {}: {}", bugId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/error";
        } catch (Exception e) {
            log.error("An unexpected error occurred while viewing bug details for ID {}: {}", bugId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("error.unexpected", null, currentLocale) + ": " + e.getMessage());
            return "redirect:/error";
        }
    }

    @GetMapping("/{id}/edit")
    public String editBugForm(@PathVariable("id") Long bugId, Model model, RedirectAttributes redirectAttributes) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        try {
            BugDTO bug = bugService.findById(bugId);
            TestSessionDTO session = testSessionService.findById(bug.getTestSessionId());

            bug.setProjectId(session.getProjectId());

            BugRequest bugRequest = new BugRequest(bug);
            bugRequest.setProjectId(session.getProjectId());

            model.addAttribute("bugRequest", bugRequest);
            model.addAttribute("bug", bug);
            model.addAttribute("session", session);
            model.addAttribute("isEditMode", true);
            model.addAttribute("bugId", bugId);
            model.addAttribute("bugStatus", BugStatus.values());
            model.addAttribute("bugSeverity", BugSeverity.values());
            model.addAttribute("bugPriority", BugPriority.values());
            model.addAttribute("formAction", "/bugs/" + bugId + "/edit");
            return "bug/form";
        } catch (ResourceNotFoundException e) {
            log.error("Error finding bug or session for edit form: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/error";
        } catch (Exception e) {
            log.error("Unexpected error in edit bug form: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("error.unexpected", null, currentLocale) + ": " + e.getMessage());
            return "redirect:/error";
        }
    }

    @PostMapping(path = "/{id}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateBug(@PathVariable("id") Long bugId,
                            @Valid @ModelAttribute BugRequest bugRequest,
                            BindingResult bindingResult,
                            @RequestParam(value = "attachment", required = false) List<MultipartFile> attachments, // <-- ALTERADO AQUI
                            RedirectAttributes redirectAttributes,
                            Model model) {

        Locale currentLocale = LocaleContextHolder.getLocale();
        Long projectId = bugRequest.getProjectId();
        Long testSessionId = bugRequest.getTestSessionId();

        // Verifique se attachments não é nulo antes de verificar o tamanho
        if (attachments != null && attachments.size() > 5) {
            String errorMessage = messageSource.getMessage("bug.form.error.max_files", null, currentLocale);
            model.addAttribute("fileAttachmentError", errorMessage);
        }

        if (bindingResult.hasErrors() || model.containsAttribute("fileAttachmentError")) {
            try {
                BugDTO bug = bugService.findById(bugId);
                TestSessionDTO session = testSessionService.findById(bug.getTestSessionId());
                bug.setProjectId(session.getProjectId());

                model.addAttribute("bug", bug);
                model.addAttribute("session", session);
                model.addAttribute("isEditMode", true);
                model.addAttribute("bugId", bugId);
            } catch (ResourceNotFoundException e) {
                log.error("Error finding bug/session for bug form validation during update: {}", e.getMessage(), e);
            }
            model.addAttribute("bugStatus", BugStatus.values());
            model.addAttribute("bugSeverity", BugSeverity.values());
            model.addAttribute("bugPriority", BugPriority.values());
            model.addAttribute("formAction", "/bugs/" + bugId + "/edit");
            return "bug/form";
        }

        try {
            // Garante que newAttachments seja uma lista vazia se for nulo
            MultipartFile[] attachmentArray = (attachments != null) ? attachments.toArray(new MultipartFile[0]) : new MultipartFile[0];
            bugService.update(bugId, bugRequest, attachmentArray);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("bug.form.success.update", null, currentLocale));

            return "redirect:/projects/" + projectId + "/sessions/" + testSessionId;
        } catch (ResourceNotFoundException | IllegalStateException e) {
            log.error("Error updating bug ({}): {}", e.getClass().getSimpleName(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/projects/" + projectId + "/sessions/" + testSessionId;
        } catch (IOException e) {
            log.error("IO Error updating bug: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("error.file_operation", null, currentLocale) + ": " + e.getMessage());
            return "redirect:/projects/" + projectId + "/sessions/" + testSessionId;
        } catch (Exception e) {
            log.error("Unexpected error updating bug: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("error.unexpected", null, currentLocale) + ": " + e.getMessage());
            return "redirect:/projects/" + projectId + "/sessions/" + testSessionId;
        }
    }

    @PostMapping("/{bugId}/attachments/{attachmentId}/delete")
    public String deleteAttachment(@PathVariable Long bugId,
                                   @PathVariable Long attachmentId,
                                   RedirectAttributes redirectAttributes) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        Long projectId = null;
        Long testSessionId = null;

        try {
            BugDTO bug = bugService.findById(bugId);
            testSessionId = bug.getTestSessionId();
            projectId = bug.getProjectId();

            bugService.deleteAttachment(bugId, attachmentId);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("bug.attachment.delete.success", null, currentLocale));
        } catch (ResourceNotFoundException e) {
            log.error("Error deleting attachment {}: {}", attachmentId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (projectId != null && testSessionId != null) {
                return "redirect:/projects/" + projectId + "/sessions/" + testSessionId;
            }
            return "redirect:/error";
        } catch (IOException e) {
            log.error("IO Error deleting attachment {}: {}", attachmentId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("error.file_operation", null, currentLocale) + ": " + e.getMessage());
            if (projectId != null && testSessionId != null) {
                return "redirect:/projects/" + projectId + "/sessions/" + testSessionId;
            }
            return "redirect:/error";
        } catch (Exception e) {
            log.error("Unexpected error deleting attachment {}: {}", attachmentId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("error.unexpected", null, currentLocale) + ": " + e.getMessage());
            return "redirect:/error";
        }
        return "redirect:/bugs/" + bugId + "/edit"; // Redireciona de volta para a página de edição do bug
    }

    @PostMapping("/{id}/delete")
    public String deleteBug(@PathVariable("id") Long bugId, RedirectAttributes redirectAttributes) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        Long testSessionId = null;
        Long projectId = null;

        try {
            BugDTO bug = bugService.findById(bugId);
            testSessionId = bug.getTestSessionId();
            projectId = bug.getProjectId();

            bugService.delete(bugId);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("bug.form.success.delete", null, currentLocale));

            return "redirect:/projects/" + projectId + "/sessions/" + testSessionId;
        } catch (ResourceNotFoundException e) {
            log.error("Error deleting bug {}: {}", bugId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/error";
        } catch (IOException e) {
            log.error("IO Error deleting bug {}: {}", bugId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("error.file_operation", null, currentLocale) + ": " + e.getMessage());
            return "redirect:/error";
        } catch (Exception e) {
            log.error("Unexpected error deleting bug {}: {}", bugId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("error.unexpected", null, currentLocale) + ": " + e.getMessage());
            return "redirect:/error";
        }
    }
}