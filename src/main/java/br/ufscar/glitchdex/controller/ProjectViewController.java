package br.ufscar.glitchdex.controller;

import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.ProjectRequest;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.service.ProjectService;
import br.ufscar.glitchdex.service.TestSessionService;
import br.ufscar.glitchdex.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectViewController {

    private final ProjectService projectService;
    private final UserService userService;
    private final TestSessionService testSessionService;

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showProjectForm(Model model) {
        model.addAttribute("projectRequest", new ProjectRequest());
        return "project/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public String createProject(@Valid @ModelAttribute("projectRequest") ProjectRequest projectRequest,
                                BindingResult result,
                                Authentication authentication,
                                Model model) {
        if (result.hasErrors()) {
            return "project/form";
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        projectService.create(projectRequest, user);
        return "redirect:/home";
    }

    @GetMapping("/{id}")
    public String viewProject(@PathVariable Long id, Model model) {
        var project = projectService.findById(id);
        model.addAttribute("project", project);
        model.addAttribute("sessions", testSessionService.findByProjectId(id));
        return "project/view";
    }
}