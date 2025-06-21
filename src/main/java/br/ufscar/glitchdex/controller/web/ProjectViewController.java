package br.ufscar.glitchdex.controller.web;

import br.ufscar.glitchdex.dto.ProjectDTO;
import br.ufscar.glitchdex.dto.ProjectRequest;
import br.ufscar.glitchdex.mapper.ProjectMapper;
import br.ufscar.glitchdex.service.ProjectService;
import br.ufscar.glitchdex.service.TestSessionService;
import br.ufscar.glitchdex.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectViewController {

    private static final Logger log = LoggerFactory.getLogger(ProjectViewController.class);
    private final ProjectService projectService;
    private final UserService userService;
    private final TestSessionService testSessionService;
    private final ProjectMapper projectMapper;

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showNewForm(Model model) {
        log.info("Request to show new project form");
        model.addAttribute("projectRequest", new ProjectRequest());
        model.addAttribute("allUsers", userService.findAll());
        model.addAttribute("isEditMode", false);
        return "project/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public String createProject(@ModelAttribute("projectRequest") ProjectRequest projectRequest,
                                BindingResult result,
                                Model model) {
        log.info("Request to create a new project with name: {}", projectRequest.getName());
        if (result.hasErrors()) {
            log.warn("Validation errors while creating project: {}", result.getAllErrors());
            model.addAttribute("isEditMode", false);
            return "project/form";
        }

        projectService.create(projectRequest);
        log.info("Project with name: {} created successfully", projectRequest.getName());
        return "redirect:/home";
    }

    @GetMapping("/{id}")
    public String viewProject(@PathVariable Long id, Model model) {
        log.info("Request to view project with id: {}", id);
        var project = projectService.findById(id);
        testSessionService.updateExpiredSessionsByProject(id);

        model.addAttribute("project", project);
        model.addAttribute("sessions", testSessionService.findByProjectId(id));
        model.addAttribute("projectId", id);
        return "project/view";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.info("Request to show edit form for project with id: {}", id);
        ProjectDTO projectDto = projectService.findById(id);
        ProjectRequest projectRequest = projectMapper.toProjectRequest(projectDto);

        model.addAttribute("projectRequest", projectRequest);
        model.addAttribute("allUsers", userService.findAll());
        model.addAttribute("isEditMode", true);
        model.addAttribute("projectId", id);
        return "project/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateProject(@PathVariable Long id,
                                @ModelAttribute("projectRequest") ProjectRequest projectRequest,
                                BindingResult result, Model model) {
        log.info("Request to update project with id: {}", id);
        if (result.hasErrors()) {
            log.warn("Validation errors while updating project: {}", result.getAllErrors());
            model.addAttribute("isEditMode", true);
            model.addAttribute("projectId", id);
            return "project/form";
        }
        projectService.update(id, projectRequest);
        log.info("Project with id: {} updated successfully", id);
        return "redirect:/projects/" + id;
    }
}