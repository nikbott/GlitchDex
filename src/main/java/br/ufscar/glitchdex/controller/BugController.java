package br.ufscar.glitchdex.controller;

import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.BugDTO;
import br.ufscar.glitchdex.dto.BugRequest;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.service.BugService;
import br.ufscar.glitchdex.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;
    private final UserService userService;

    @GetMapping("/new")
    public String newBugForm(@RequestParam("sessionId") Long sessionId, Model model) {
        model.addAttribute("bugRequest", new BugRequest());
        model.addAttribute("testSessionId", sessionId);
        return "bug/form";
    }

    @PostMapping("/create")
    public String createBug(@Valid @ModelAttribute BugRequest bugRequest,
                            @RequestParam("attachment") MultipartFile attachment,
                            Authentication authentication) throws IllegalStatusChangeException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BugDTO createdBug = bugService.create(bugRequest, user, attachment);
        return "redirect:/bugs/" + createdBug.getId();
    }

    @GetMapping("/{id}")
    public String viewBug(@PathVariable Long id, Model model) {
        BugDTO bug = bugService.findById(id);
        model.addAttribute("bug", bug);
        return "bug/view";
    }

    @GetMapping("/delete/{id}")
    public String deleteBug(@PathVariable Long id) {
        Long testSessionId = bugService.findById(id).getTestSessionId();
        bugService.delete(id);
        return "redirect:/sessions/" + testSessionId;
    }
}