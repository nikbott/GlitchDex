package br.ufscar.glitchdex.controller.web;

import br.ufscar.glitchdex.dto.StrategyDTO;
import br.ufscar.glitchdex.dto.StrategyRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.mapper.StrategyMapper;
import br.ufscar.glitchdex.service.StrategyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/strategies")
@RequiredArgsConstructor
public class StrategyViewController {

    private static final Logger log = LoggerFactory.getLogger(StrategyViewController.class);
    private final StrategyService strategyService;
    private final StrategyMapper strategyMapper;

    @GetMapping
    public String listStrategies(Model model) {
        log.info("Request to list all strategies");
        model.addAttribute("strategies", strategyService.findAll());
        return "strategy/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showNewForm(Model model) {
        log.info("Request to show new strategy form");
        model.addAttribute("strategyRequest", new StrategyRequest());
        return "strategy/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public String createStrategy(@Valid @ModelAttribute("strategyRequest") StrategyRequest strategyRequest,
                                 BindingResult result,
                                 @RequestParam("imageFile") MultipartFile imageFile,
                                 UserDTO currentUser,
                                 RedirectAttributes redirectAttributes) {
        log.info("Request from user {} to create a new strategy with name: {}", currentUser.getEmail(), strategyRequest.getName());
        if (result.hasErrors()) {
            log.warn("Validation errors while creating strategy: {}", result.getAllErrors());
            return "strategy/form";
        }

        strategyService.create(strategyRequest, currentUser, imageFile);
        redirectAttributes.addFlashAttribute("successMessage", "Strategy created successfully!");
        log.info("Strategy with name: {} created successfully", strategyRequest.getName());
        return "redirect:/strategies";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.info("Request to show edit form for strategy with id: {}", id);
        StrategyDTO strategy = strategyService.findById(id);
        StrategyRequest request = strategyMapper.toStrategyRequest(strategy);

        model.addAttribute("strategyRequest", request);
        model.addAttribute("strategy", strategy);
        return "strategy/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateStrategy(@PathVariable Long id,
                                 @Valid @ModelAttribute("strategyRequest") StrategyRequest strategyRequest,
                                 BindingResult result,
                                 @RequestParam("imageFile") MultipartFile imageFile,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        log.info("Request to update strategy with id: {}", id);
        if (result.hasErrors()) {
            log.warn("Validation errors while updating strategy: {}", result.getAllErrors());
            StrategyDTO strategy = strategyService.findById(id);
            model.addAttribute("strategy", strategy);
            return "strategy/form";
        }

        strategyService.update(id, strategyRequest, imageFile);
        redirectAttributes.addFlashAttribute("successMessage", "Strategy updated successfully!");
        log.info("Strategy with id: {} updated successfully", id);
        return "redirect:/strategies";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteStrategy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Request to delete strategy with id: {}", id);
        strategyService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Strategy deleted successfully!");
        log.info("Strategy with id: {} deleted successfully", id);
        return "redirect:/strategies";
    }
}