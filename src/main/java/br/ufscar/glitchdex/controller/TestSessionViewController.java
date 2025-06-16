package br.ufscar.glitchdex.controller;


import br.ufscar.glitchdex.domain.Project;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.TestSessionRequest;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.service.ProjectService;
import br.ufscar.glitchdex.service.StrategyService;
import br.ufscar.glitchdex.service.TestSessionService;
import br.ufscar.glitchdex.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class TestSessionViewController {

    private final StrategyService strategyService;
    private final ProjectService projectService;
    private final TestSessionService testSessionService;
    private final UserService userService;

    @GetMapping("/new")
    public String showTestSessionForm(@RequestParam("projectId") Long projectId, Model model) {
        // 1. Fetch the DTO and assign it to 'projectDto'.
        var projectDto = projectService.findById(projectId);

        // 2. Create a 'Project' entity required by the strategy service.
        Project projectEntity = new Project();
        projectEntity.setId(projectId);

        // 3. Call the service using the correct variable: 'projectEntity'.
        var strategies = strategyService.findByProject(projectEntity);

        // 4. Add attributes to the model for the view to use.
        model.addAttribute("testSessionRequest", new TestSessionRequest());
        model.addAttribute("strategies", strategies);
        model.addAttribute("projectId", projectId);
        model.addAttribute("projectName", projectDto.getName());

        return "session/form";
    }

    public String createTestSession(@Valid @ModelAttribute("testSessionRequest") TestSessionRequest request,
                                    @RequestParam("projectId") Long projectId, // Get projectId to redirect
                                    BindingResult result,
                                    Authentication authentication) {
        if (result.hasErrors()) {
            return "session/form"; // If validation fails, show the form again
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        testSessionService.create(request, user);

        // Redirect back to the project's detail page after creation
        return "redirect:/projects/" + projectId;
    }
}