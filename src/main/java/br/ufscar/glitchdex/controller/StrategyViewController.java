package br.ufscar.glitchdex.controller;

import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.StrategyRequest;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.service.StrategyService;
import br.ufscar.glitchdex.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/strategies")
@RequiredArgsConstructor
public class StrategyViewController {

    private final StrategyService strategyService;
    private final UserService userService;

    @GetMapping
    public String listStrategies(Model model) {
        model.addAttribute("strategies", strategyService.findAll());
        return "strategy/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showStrategyForm(Model model) {
        model.addAttribute("strategyRequest", new StrategyRequest());
        // You would also need to pass a list of projects to select from
        // model.addAttribute("projects", projectService.findAll());
        return "strategy/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public String createStrategy(@Valid @ModelAttribute("strategyRequest") StrategyRequest strategyRequest,
                                 BindingResult result,
                                 Authentication authentication,
                                 Model model) {
        if (result.hasErrors()) {
            // Repopulate projects if necessary
            // model.addAttribute("projects", projectService.findAll());
            return "strategy/form";
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        strategyService.create(strategyRequest, user);
        return "redirect:/strategies";
    }
}